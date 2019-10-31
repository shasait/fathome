/*
 * Copyright (C) 2019 by Sebastian Hasait (sebastian at hasait dot de)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package de.hasait.fathome;

import java.io.IOException;
import java.util.Base64;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import org.apache.http.client.utils.HttpClientUtils;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Element;
import rocks.xmpp.addr.Jid;
import rocks.xmpp.core.XmppException;
import rocks.xmpp.core.net.ChannelEncryption;
import rocks.xmpp.core.net.client.SocketConnectionConfiguration;
import rocks.xmpp.core.session.Extension;
import rocks.xmpp.core.session.XmppClient;
import rocks.xmpp.core.session.XmppSessionConfiguration;
import rocks.xmpp.core.session.debug.ConsoleDebugger;
import rocks.xmpp.core.stanza.MessageEvent;
import rocks.xmpp.core.stanza.PresenceEvent;
import rocks.xmpp.core.stanza.model.Message;
import rocks.xmpp.core.stanza.model.Presence;
import rocks.xmpp.core.stanza.model.client.ClientMessage;
import rocks.xmpp.extensions.pubsub.model.Item;
import rocks.xmpp.extensions.pubsub.model.event.Event;
import rocks.xmpp.extensions.rpc.RpcManager;
import rocks.xmpp.extensions.rpc.model.Value;
import rocks.xmpp.im.subscription.PresenceManager;
import rocks.xmpp.util.concurrent.AsyncResult;

import de.hasait.fathome.util.http.AsStringContentHandler;
import de.hasait.fathome.util.http.HttpUtil;

/**
 *
 */
public class FreeAtHome {

	private static final Logger log = LoggerFactory.getLogger(FreeAtHome.class);

	private final Set<AbstractFahPart> parts = new HashSet<>();
	private final Map<String, String> sysap = new TreeMap<>();
	private final Map<String, String> config = new TreeMap<>();
	private final Map<Integer, FahString> stringByNameId = new TreeMap<>();
	private final Map<Integer, FahFunction> functionByFunctionId = new TreeMap<>();
	private final Map<String, FahFloor> floorByUid = new TreeMap<>();
	private final Map<String, FahFloor> floorByName = new TreeMap<>();
	private final Map<String, FahRoom> roomByUid = new TreeMap<>();
	private final Map<String, FahDevice> deviceBySerialNumber = new TreeMap<>();
	private final Map<String, FahChannel> channelByName = new TreeMap<>();

	private XmppClient xmppClient;
	private String updateNamespace;
	private Jid rpcJid;
	private FahCryptContext cryptContext;
	private FahUser user;

	public void connect(FreeAtHomeConfiguration configuration) {
		try {
			String fahUsername = configuration.getUsername();
			String fahPassword = configuration.getPassword();
			String fahSysApHostname = configuration.getHostOrIp();

			String xmppDomain = "busch-jaeger.de";
			updateNamespace = "http://abb.com/protocol/update";
			rpcJid = Jid.of("mrha@" + xmppDomain + "/rpc");

			CloseableHttpClient httpClient = HttpClients.createDefault();
			Map<String, FahUser> fahUsers = new TreeMap<>();
			try {
				try {
					HttpUtil.httpGet(httpClient, "http://" + fahSysApHostname + "/settings.json",
									 new AsStringContentHandler(contentString -> {
										 JSONObject root = new JSONObject(contentString);
										 JSONArray users = root.getJSONArray("users");
										 for (int userIndex = 0; userIndex < users.length(); userIndex++) {
											 JSONObject user = users.getJSONObject(userIndex);
											 FahUser fahUser = new FahUser(user.getString("name"));
											 fahUser.setJid(user.getString("jid"));

											 JSONObject authmethods = user.getJSONObject("authmethods");
											 if (authmethods != null) {
												 for (String key : authmethods.keySet()) {
													 JSONObject authmethod = authmethods.getJSONObject(key);
													 FahUser.AuthMethod fahAuthMethod = new FahUser.AuthMethod(key);
													 fahAuthMethod.setIterations(authmethod.getInt("iterations"));
													 fahAuthMethod.setSalt(Base64.getDecoder().decode(authmethod.getString("salt")));
													 fahUser.getAuthMethods().put(key, fahAuthMethod);
												 }
											 }

											 fahUsers.put(fahUser.getName(), fahUser);
										 }
									 })
					);
				} catch (IOException e) {
					throw new RuntimeException(e);
				}
			} finally {
				HttpClientUtils.closeQuietly(httpClient);
			}

			if (!fahUsers.containsKey(fahUsername)) {
				throw new IllegalArgumentException("Invalid username: " + fahUsername + " not in " + fahUsers.keySet());
			}

			user = fahUsers.get(fahUsername);
			Jid userJid = Jid.of(user.getJid());
			String xmppUsername = userJid.getLocal();
			log.info("Login using " + xmppUsername + "...");

			SocketConnectionConfiguration connectionConfiguration = //
					SocketConnectionConfiguration.builder() //
												 .hostname(fahSysApHostname) //
												 .port(5222) //
												 .channelEncryption(ChannelEncryption.OPTIONAL) //
												 .build();

			XmppSessionConfiguration sessionConfiguration = //
					XmppSessionConfiguration.builder() //
											.extensions(Extension.of(updateNamespace, null, true, true),
														Extension.of(updateNamespace, null, true)
											) //
											.debugger(ConsoleDebugger.class) //
											.build();

			xmppClient = XmppClient.create(xmppDomain, sessionConfiguration, connectionConfiguration);

			xmppClient.addInboundPresenceListener(this::handlePresenceEvent);

			xmppClient.connect();
			xmppClient.login(xmppUsername, fahPassword);

			xmppClient.addInboundMessageListener(this::handleInboundMessage);

			Presence presence = new Presence(rpcJid, Presence.Type.SUBSCRIBE, null, null, null, null, userJid, null, null, null);
			xmppClient.send(presence);
			xmppClient.send(new Presence());

			// initCryptContext(user, fahPassword);

			loadAll();
		} catch (XmppException e) {
			throw new RuntimeException(e);
		}
	}

	public Collection<FahChannel> getAllChannels() {
		return Collections.unmodifiableCollection(channelByName.values());
	}

	public Collection<FahFloor> getAllFloors() {
		return Collections.unmodifiableCollection(floorByUid.values());
	}

	public FahBlind getBlind(String name) {
		FahChannel channel = getChannel(name);
		return channel != null && channel.isBlind() ? channel.asBlind() : null;
	}

	public FahChannel getChannel(String name) {
		return name != null ? channelByName.get(name) : null;
	}

	public FahDevice getDeviceBySerialNumber(String serialNumber) {
		return serialNumber != null ? deviceBySerialNumber.get(serialNumber) : null;
	}

	public FahDimmer getDimmer(String name) {
		FahChannel channel = getChannel(name);
		return channel != null && channel.isDimmer() ? channel.asDimmer() : null;
	}

	public FahFloor getFloorByName(String name) {
		return name != null ? floorByName.get(name) : null;
	}

	public FahFunction getFunctionByFunctionId(Integer functionId) {
		return functionId != null ? functionByFunctionId.get(functionId) : null;
	}

	public FahRoom getRoomByUid(String uid) {
		return uid != null ? roomByUid.get(uid) : null;
	}

	public FahScene getScene(String name) {
		FahChannel channel = getChannel(name);
		return channel != null && channel.isScene() ? channel.asScene() : null;
	}

	public FahString getStringByNameId(Integer nameId) {
		return nameId != null ? stringByNameId.get(nameId) : null;
	}

	public FahSwitch getSwitch(String name) {
		FahChannel channel = getChannel(name);
		return channel != null && channel.isSwitch() ? channel.asSwitch() : null;
	}

	void addPart(AbstractFahPart part) {
		if (parts.contains(part)) {
			return;
		}
		part.setFreeAtHome(this);

		if (part instanceof FahString) {
			FahString fah = (FahString) part;
			stringByNameId.put(fah.getId(), fah);
		}
		if (part instanceof FahFunction) {
			FahFunction fah = (FahFunction) part;
			functionByFunctionId.put(fah.getFunctionId(), fah);
		}
		if (part instanceof FahFloor) {
			FahFloor fah = (FahFloor) part;
			floorByUid.put(fah.getUid(), fah);
			String name = fah.getName();
			if (name != null) {
				floorByName.put(name, fah);
			}
		}
		if (part instanceof FahRoom) {
			FahRoom fah = (FahRoom) part;
			roomByUid.put(fah.getUid(), fah);
		}
		if (part instanceof FahDevice) {
			FahDevice fah = (FahDevice) part;
			deviceBySerialNumber.put(fah.getSerialNumber(), fah);
		}
		if (part instanceof FahChannel) {
			FahChannel fah = (FahChannel) part;
			String name = fah.getName();
			if (name != null) {
				channelByName.put(name, fah);
			}
		}
	}

	void loadAll() {
		Value result = rpcCall("RemoteInterface.getAll", Value.of("de"), Value.of(4), Value.of(0), Value.of(0));
		String projectXml = result.getAsString();
		if (projectXml != null) {
			FahXmlProcessor.processProjectXml(projectXml, this);
		}
	}

	Value rpcCall(String methodName, Value... parameters) {
		try {
			RpcManager rpcManager = xmppClient.getManager(RpcManager.class);
			AsyncResult<Value> asyncResult = rpcManager.call(rpcJid, methodName, parameters);
			return asyncResult.getResult();
		} catch (XmppException e) {
			throw new RuntimeException(e);
		}
	}

	void setFahConfigValue(String name, String value) {
		config.put(name, value);
	}

	void setFahSysapValue(String name, String value) {
		sysap.put(name, value);
	}

	private void handleInboundMessage(MessageEvent messageEvent) {
		Message message = messageEvent.getMessage();
		try {
			if (message instanceof ClientMessage) {
				ClientMessage clientMessage = (ClientMessage) message;
				Event event = clientMessage.getExtension(Event.class);
				if (event != null && updateNamespace.equals(event.getNode())) {
					for (Item item : event.getItems()) {
						Object payload = item.getPayload();
						if (payload instanceof Element) {
							Element updateElement = (Element) payload;
							if ("update".equals(updateElement.getTagName())) {
								String updateXml = updateElement.getElementsByTagName("data").item(0).getTextContent();
								log.debug("updateXml: " + updateXml);
								FahXmlProcessor.processUpdateXml(updateXml, this);
								messageEvent.consume();
							}
						}
					}
				}
			} else {
				log.debug("message: " + message);
			}
		} catch (RuntimeException e) {
			log.warn("Could not process message", e);
		}
	}

	private void handlePresenceEvent(PresenceEvent presenceEvent) {
		Presence presence = presenceEvent.getPresence();
		Jid from = presence.getFrom();
		if (presence.getType() == Presence.Type.SUBSCRIBE) {
			log.info("Subscribe from: {}", from);
			xmppClient.getManager(PresenceManager.class).approveSubscription(from);
		}
	}

	private void initCryptContext(FahUser user, String fahPassword) {
		cryptContext = new FahCryptContext(this);
		cryptContext.init(user, fahPassword.toCharArray());
	}

}
