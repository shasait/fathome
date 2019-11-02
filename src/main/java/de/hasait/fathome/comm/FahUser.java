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

import java.util.HashMap;
import java.util.Map;

/**
 *
 */
public class FahUser {

	private final String name;
	private final Map<String, AuthMethod> authMethods = new HashMap<>();
	private String jid;

	public FahUser(String name) {
		this.name = name;
	}

	public Map<String, AuthMethod> getAuthMethods() {
		return authMethods;
	}

	public String getJid() {
		return jid;
	}

	public String getName() {
		return name;
	}

	public void setJid(String jid) {
		this.jid = jid;
	}

	public static class AuthMethod {

		private final String name;
		private int iterations;
		private byte[] salt;

		public AuthMethod(String name) {
			this.name = name;
		}

		public int getIterations() {
			return iterations;
		}

		public String getName() {
			return name;
		}

		public byte[] getSalt() {
			return salt;
		}

		public void setIterations(int iterations) {
			this.iterations = iterations;
		}

		public void setSalt(byte[] salt) {
			this.salt = salt;
		}

	}

}
