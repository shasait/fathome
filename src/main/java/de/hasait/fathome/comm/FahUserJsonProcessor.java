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

package de.hasait.fathome.comm;

import java.util.Base64;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONObject;

/**
 *
 */
public final class FahUserJsonProcessor {

	public static void processSettingsJson(String contentString, Map<String, FahUser> fahUsers) {
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
	}

	private FahUserJsonProcessor() {
		super();
	}

}
