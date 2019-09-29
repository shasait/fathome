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
import java.util.Map;
import java.util.TreeMap;

import org.apache.http.client.utils.HttpClientUtils;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import rocks.xmpp.addr.Jid;
import rocks.xmpp.core.XmppException;
import rocks.xmpp.core.net.client.SocketConnectionConfiguration;
import rocks.xmpp.core.session.XmppClient;
import rocks.xmpp.extensions.rpc.RpcManager;
import rocks.xmpp.extensions.rpc.model.Value;
import rocks.xmpp.util.concurrent.AsyncResult;

import de.hasait.fathome.project.FahChannel;
import de.hasait.fathome.project.FahProject;
import de.hasait.fathome.project.FahProjectParser;
import de.hasait.fathome.util.http.AsStringContentHandler;
import de.hasait.fathome.util.http.HttpUtil;

/**
 *
 */
public class FreeAtHome {

	private static final Logger log = LoggerFactory.getLogger(FreeAtHome.class);

	private XmppClient xmppClient;
	private Jid rpcJid;

	private FahProject project;

	public void connect(FreeAtHomeConfiguration configuration) {
		try {
			String fahUsername = configuration.getUsername();
			String fahPassword = configuration.getPassword();
			String fahSysApHostname = configuration.getHostOrIp();

			String xmppDomain = "busch-jaeger.de";
			rpcJid = Jid.of("mrha@" + xmppDomain + "/rpc");

			CloseableHttpClient httpClient = HttpClients.createDefault();
			Map<String, String> jidStrings = new TreeMap<>();
			try {
				try {
					HttpUtil.httpGet(httpClient, "http://" + fahSysApHostname + "/settings.json",
									 new AsStringContentHandler(contentString -> {
										 JSONObject root = new JSONObject(contentString);
										 JSONArray users = root.getJSONArray("users");
										 for (int i = 0; i < users.length(); i++) {
											 JSONObject user = users.getJSONObject(i);
											 String name = user.getString("name");
											 String jidString = user.getString("jid");
											 jidStrings.put(name, jidString);
										 }
									 })
					);
				} catch (IOException e) {
					throw new RuntimeException(e);
				}
			} finally {
				HttpClientUtils.closeQuietly(httpClient);
			}

			if (!jidStrings.containsKey(fahUsername)) {
				throw new IllegalArgumentException("Invalid username: " + fahUsername + " not in " + jidStrings.keySet());
			}
			Jid userJid = Jid.of(jidStrings.get(fahUsername));
			String xmppUsername = userJid.getLocal();
			log.info("Login using " + xmppUsername + "...");

			SocketConnectionConfiguration connectionConfiguration = SocketConnectionConfiguration.builder() //
																								 .hostname(fahSysApHostname) //
																								 .port(5222) //
																								 .build();

			xmppClient = XmppClient.create(xmppDomain, connectionConfiguration);
			xmppClient.connect();
			xmppClient.login(xmppUsername, fahPassword);
			fahGetAll();
		} catch (XmppException e) {
			throw new RuntimeException(e);
		}
	}

	public FahProject getProject() {
		return project;
	}

	public void switchActuator(FahChannel channel, boolean state) {
		try {
			String fidName = channel.getFunction().getFidName();
			if (!"FID_SwitchingActuator".equals(fidName)) {
				throw new IllegalArgumentException("Invalid FID: " + fidName);
			}
			String dpPath = channel.getDevice().getSerialNumber() + "/" + channel.getI() + "/" + channel.getIdps().iterator().next();
			String dpValue = state ? "1" : "0";

			RpcManager rpcManager = xmppClient.getManager(RpcManager.class);
			AsyncResult<Value> asyncResult = rpcManager.call(rpcJid, "RemoteInterface.setDatapoint", Value.of(dpPath), Value.of(dpValue));
			Value result = asyncResult.getResult();
			log.info("result: " + result);
		} catch (XmppException e) {
			throw new RuntimeException(e);
		}
	}

	private void fahGetAll() {
		try {
			RpcManager rpcManager = xmppClient.getManager(RpcManager.class);
			AsyncResult<Value> asyncResult = rpcManager
					.call(rpcJid, "RemoteInterface.getAll", Value.of("de"), Value.of("4"), Value.of("0"), Value.of("0"));
			Value result = asyncResult.getResult();
			String projectXml = result.getAsString();
			FahProject project = new FahProject(this);
			FahProjectParser.parse(projectXml, project);
			this.project = project;
		} catch (XmppException e) {
			throw new RuntimeException(e);
		}
	}

}
