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

package de.hasait.fathome.util.sodium;

/**
 *
 */
public class KeyPair {

	private final String primitive;
	private final byte[] publicKey;
	private final byte[] secretKey;

	public KeyPair(String primitive, byte[] publicKey, byte[] secretKey) {
		this.primitive = primitive;
		this.publicKey = publicKey;
		this.secretKey = secretKey;
	}

	public String getPrimitive() {
		return primitive;
	}

	public byte[] getPublicKey() {
		return publicKey;
	}

	public byte[] getSecretKey() {
		return secretKey;
	}

}
