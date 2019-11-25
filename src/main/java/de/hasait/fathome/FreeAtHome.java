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
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.apache.http.client.utils.HttpClientUtils;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
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

import de.hasait.fathome.comm.FahCommunication;
import de.hasait.fathome.comm.FahCryptContext;
import de.hasait.fathome.comm.FahUser;
import de.hasait.fathome.comm.FahUserJsonProcessor;
import de.hasait.fathome.project.AbstractFahChannel;
import de.hasait.fathome.project.FahChannelFactory;
import de.hasait.fathome.project.FahDevice;
import de.hasait.fathome.project.FahDeviceProcessor;
import de.hasait.fathome.project.FahFloor;
import de.hasait.fathome.project.FahFunction;
import de.hasait.fathome.project.FahProject;
import de.hasait.fathome.project.FahXmlProcessor;
import de.hasait.fathome.things.FahBlind;
import de.hasait.fathome.things.FahDimmer;
import de.hasait.fathome.things.FahScene;
import de.hasait.fathome.things.FahSensor;
import de.hasait.fathome.things.FahSwitch;
import de.hasait.fathome.things.FahUnknown;
import de.hasait.fathome.util.http.AsStringContentHandler;
import de.hasait.fathome.util.http.HttpUtil;

/**
 *
 */
public class FreeAtHome {

	private static final Logger log = LoggerFactory.getLogger(FreeAtHome.class);

	private final Map<String, FahChannelFactory> channelFactoriesByFidName = new HashMap<>();
	private final Map<Integer, FahChannelFactory> channelFactoriesByFunctionId = new HashMap<>();
	private final List<FahDeviceProcessor> deviceProcessors = new ArrayList<>();

	private final FahCommunication communication = new FahCommunication() {
		@Override
		public Value rpcCall(String methodName, Value... parameters) {
			try {
				RpcManager rpcManager = xmppClient.getManager(RpcManager.class);
				AsyncResult<Value> asyncResult = rpcManager.call(rpcJid, methodName, parameters);
				return asyncResult.getResult();
			} catch (XmppException e) {
				throw new RuntimeException(e);
			}
		}
	};

	private final FahChannelFactory channelFactory = (device, id, function) -> {
		FahChannelFactory channelFactory = getChannelFactory(function);
		if (channelFactory != null) {
			return channelFactory.createChannel(device, id, function);
		}
		return new FahUnknown(device, id, function);
	};

	private XmppClient xmppClient;
	private String updateNamespace;
	private Jid rpcJid;
	private FahCryptContext cryptContext;
	private FahUser user;
	private FahProject project = new FahProject(communication);

	public FreeAtHome() {
		super();

		registerChannelFactoryForFidName("FID_SwitchingActuator", FahSwitch::new);
		registerChannelFactoryForFidName("FID_DimmingActuator", FahDimmer::new);
		registerChannelFactoryForFidName("FID_BlindActuator", FahBlind::new);
		registerChannelFactoryForFidName("FID_ShutterActuator", FahBlind::new);
		registerChannelFactoryForFunctionId(0x4800, FahScene::new);
		registerDeviceProcessor(FahSensor::processDeviceForSensors);
	}

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
					HttpUtil.httpGet(httpClient, "http://" + fahSysApHostname + "/settings.json", new AsStringContentHandler(
							contentString -> FahUserJsonProcessor.processSettingsJson(contentString, fahUsers)));
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

	public Collection<AbstractFahChannel> getAllChannels() {
		return project.getAllChannels();
	}

	public Collection<FahFloor> getAllFloors() {
		return project.getAllFloors();
	}

	public FahBlind getBlind(String name) {
		return getChannel(name, FahBlind.class);
	}

	public AbstractFahChannel getChannel(String name) {
		return project.getChannel(name);
	}

	public <T extends AbstractFahChannel> T getChannel(String name, Class<T> type) {
		AbstractFahChannel channel = project.getChannel(name);
		return type.isInstance(channel) ? (T) channel : null;
	}

	public FahDevice getDevice(String serialNumber) {
		return project.getDeviceBySerialNumber(serialNumber);
	}

	public FahDimmer getDimmer(String name) {
		return getChannel(name, FahDimmer.class);
	}

	public FahScene getScene(String name) {
		return getChannel(name, FahScene.class);
	}

	public FahSwitch getSwitch(String name) {
		return getChannel(name, FahSwitch.class);
	}

	public void registerChannelFactoryForFidName(String fidName, FahChannelFactory channelFactory) {
		channelFactoriesByFidName.put(fidName, channelFactory);
	}

	public void registerChannelFactoryForFunctionId(int functionId, FahChannelFactory channelFactory) {
		channelFactoriesByFunctionId.put(functionId, channelFactory);
	}

	public void registerDeviceProcessor(FahDeviceProcessor deviceProcessor) {
		deviceProcessors.add(deviceProcessor);
	}

	void loadAll() {
		Value result = communication.rpcCall("RemoteInterface.getAll", Value.of("de"), Value.of(4), Value.of(0), Value.of(0));
		String projectXml = result.getAsString();
		if (projectXml != null) {
			FahProject project = new FahProject(communication);
			FahXmlProcessor.processProjectXml(projectXml, project, channelFactory,
											  device -> deviceProcessors.forEach(deviceProcessor -> deviceProcessor.processDevice(device))
			);
			this.project = project;
		}
	}

	private FahChannelFactory getChannelFactory(FahFunction function) {
		if (function == null) {
			return null;
		}
		FahChannelFactory channelFactoryByFidName = channelFactoriesByFidName.get(function.getFidName());
		if (channelFactoryByFidName != null) {
			return channelFactoryByFidName;
		}
		FahChannelFactory channelFactoryByFunctionId = channelFactoriesByFunctionId.get(function.getId());
		if (channelFactoryByFunctionId != null) {
			return channelFactoryByFunctionId;
		}
		return null;
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
								FahXmlProcessor.processUpdateXml(updateXml, project);
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
		cryptContext = new FahCryptContext(communication);
		cryptContext.init(user, fahPassword.toCharArray());
	}

}
