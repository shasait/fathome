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

import java.security.NoSuchAlgorithmException;
import java.util.LinkedHashMap;
import java.util.Map;

import javax.crypto.Mac;

import rocks.xmpp.extensions.rpc.model.Value;

import de.hasait.fathome.util.CryptUtil;
import de.hasait.fathome.util.sodium.KeyPair;
import de.hasait.fathome.util.sodium.Sodium;

/**
 *
 */
public class FahCryptContext {

	private static final Map<String, String> HASH_HMAC_ALGORITHMS = new LinkedHashMap<>();

	static {
		registerAlgoritms("SHA-256", "HmacSHA256");
		registerAlgoritms("SHA-1", "HmacSHA1");
	}

	public static void registerAlgoritms(String hashAlgorithm, String hmacAlgorithm) {
		try {
			Mac.getInstance(hmacAlgorithm);
		} catch (NoSuchAlgorithmException e) {
			throw new RuntimeException(e);
		}

		HASH_HMAC_ALGORITHMS.put(hashAlgorithm, hmacAlgorithm);
	}

	private final FahCommunication communication;

	private KeyPair keyPair;
	private byte[] clientNonce;

	public FahCryptContext(FahCommunication communication) {
		super();

		this.communication = communication;

	}

	public boolean init(FahUser user, char[] password) {
		FahUserAuthMethod authMethod = getBestAuthMethod(user);
		if (authMethod == null) {
			return false;
		}

		keyPair = Sodium.crypto_box_keypair();
		clientNonce = Sodium.randombytes_buf(16);

		byte[] clientKey = createClientKey(password, authMethod);
		Value result = communication.rpcCall("RemoteInterface.cryptExchangeLocalKeys2", Value.of(user.getJid()), Value.of(clientKey),
											 Value.of(authMethod.getScramAlgorithm()), Value.of(0)
		);
		byte[] serverResponse = result.getAsByteArray();
		// serverResponse currently ignored

		return true;
	}

	private byte[] createClientKey(char[] password, FahUserAuthMethod authMethod) {
		byte[] saltedPassword = CryptUtil
				.computeSaltedPassword(authMethod.getHmacAlgorithm(), password, authMethod.getSalt(), authMethod.getIterations());
		if (saltedPassword.length != 32) {
			throw new RuntimeException();
		}

		int otaKeyLength = Sodium.crypto_onetimeauth_keybytes();
		byte[] otaKey = Sodium.crypto_generichash(otaKeyLength, saltedPassword, clientNonce);
		byte[] publicKey = keyPair.getPublicKey();
		byte[] ota = Sodium.crypto_onetimeauth(publicKey, otaKey);

		int resultLength = 64;
		if (publicKey.length + clientNonce.length + ota.length != resultLength) {
			throw new RuntimeException();
		}

		byte[] result = new byte[resultLength];
		System.arraycopy(publicKey, 0, result, 0, publicKey.length);
		System.arraycopy(clientNonce, 0, result, publicKey.length, clientNonce.length);
		System.arraycopy(ota, 0, result, publicKey.length + clientNonce.length, ota.length);
		return result;
	}

	private FahUserAuthMethod getBestAuthMethod(FahUser user) {
		for (Map.Entry<String, String> algorithm : HASH_HMAC_ALGORITHMS.entrySet()) {
			String hashAlgorithm = algorithm.getKey();
			String hmacAlgorithm = algorithm.getValue();
			String scramAlgorithm = "SCRAM-" + hashAlgorithm;
			FahUser.AuthMethod authMethod = user.getAuthMethods().get(scramAlgorithm);
			if (authMethod != null) {
				return new FahUserAuthMethod(hashAlgorithm, hmacAlgorithm, scramAlgorithm, authMethod);
			}
		}
		return null;
	}

	private static class FahUserAuthMethod {

		private final String hashAlgorithm;
		private final String hmacAlgorithm;
		private final String scramAlgorithm;
		private final FahUser.AuthMethod authMethod;

		private FahUserAuthMethod(String hashAlgorithm, String hmacAlgorithm, String scramAlgorithm, FahUser.AuthMethod authMethod) {
			this.hashAlgorithm = hashAlgorithm;
			this.hmacAlgorithm = hmacAlgorithm;
			this.scramAlgorithm = scramAlgorithm;
			this.authMethod = authMethod;
		}

		public String getHashAlgorithm() {
			return hashAlgorithm;
		}

		public String getHmacAlgorithm() {
			return hmacAlgorithm;
		}

		public int getIterations() {
			return authMethod.getIterations();
		}

		public byte[] getSalt() {
			return authMethod.getSalt();
		}

		public String getScramAlgorithm() {
			return scramAlgorithm;
		}
	}

}
