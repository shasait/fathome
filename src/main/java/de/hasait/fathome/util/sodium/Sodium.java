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

import jnr.ffi.LibraryLoader;
import jnr.ffi.Platform;
import jnr.ffi.annotations.In;
import jnr.ffi.annotations.Out;
import jnr.ffi.types.u_int64_t;

/**
 * Wrapper for native sodium library.
 */
public final class Sodium {

	public static final Native NATIVE;

	static {
		String libraryName;
		switch (Platform.getNativePlatform().getOS()) {
			case WINDOWS:
				libraryName = "libsodium";
				break;
			default:
				libraryName = "sodium";
				break;
		}
		NATIVE = LibraryLoader.create(Native.class).search("/usr/local/lib").search("/usr/lib").search("lib").load(libraryName);
		NATIVE.sodium_init();
		String version = sodium_version_string();
		String[] versionSplitted = version.split("\\.");
		boolean versionSupported = Integer.parseInt(versionSplitted[0]) <= 1
				&& Integer.parseInt(versionSplitted[1]) <= 0
				&& Integer.parseInt(versionSplitted[2]) <= 16;
		if (!versionSupported) {
			throw new RuntimeException("Unsupported version: " + libraryName + " " + version);
		}
	}

	public static KeyPair crypto_box_keypair() {
		String primitive = crypto_box_primitive();
		int publicKeyLength = crypto_box_publickeybytes();
		int secretKeyLength = crypto_box_secretkeybytes();
		byte[] publicKey = new byte[publicKeyLength];
		byte[] secretKey = new byte[secretKeyLength];
		NATIVE.crypto_box_keypair(publicKey, secretKey);
		return new KeyPair(primitive, publicKey, secretKey);
	}

	public static String crypto_box_primitive() {
		return NATIVE.crypto_box_primitive();
	}

	public static int crypto_box_publickeybytes() {
		return NATIVE.crypto_box_publickeybytes();
	}

	public static int crypto_box_secretkeybytes() {
		return NATIVE.crypto_box_secretkeybytes();
	}

	public static byte[] crypto_generichash(int length, byte[] in, byte[] key) {
		byte[] out = new byte[length];
		NATIVE.crypto_generichash(out, out.length, in, in.length, key, key.length);
		return out;
	}

	public static byte[] crypto_onetimeauth(byte[] in, byte[] key) {
		int onetimeauthLength = crypto_onetimeauth_bytes();
		int onetimeauthKeyLength = crypto_onetimeauth_keybytes();
		if (key.length != onetimeauthKeyLength) {
			throw new IllegalArgumentException("Invalid key length - actual: " + key.length + ", expected: " + onetimeauthKeyLength);
		}
		byte[] onetimeauth = new byte[onetimeauthLength];
		NATIVE.crypto_onetimeauth(onetimeauth, in, in.length, key);
		return onetimeauth;
	}

	public static int crypto_onetimeauth_bytes() {
		return NATIVE.crypto_onetimeauth_bytes();
	}

	public static int crypto_onetimeauth_keybytes() {
		return NATIVE.crypto_onetimeauth_keybytes();
	}

	public static void main(String[] args) throws Exception {
		System.out.println(Sodium.sodium_version_string());
	}

	public static byte[] randombytes_buf(int length) {
		byte[] out = new byte[length];
		NATIVE.randombytes_buf(out, out.length);
		return out;
	}

	public static String sodium_version_string() {
		return NATIVE.sodium_version_string();
	}

	private Sodium() {
		super();
	}

	public interface Native {

		int crypto_box_keypair(@Out byte[] publicKey, @Out byte[] secretKey);

		String crypto_box_primitive();

		int crypto_box_publickeybytes();

		int crypto_box_secretkeybytes();

		int crypto_generichash(@Out byte[] out, @In @u_int64_t int outLen, @In byte[] in, @u_int64_t int inLen, @In byte[] key,
				@In @u_int64_t int keyLen);

		int crypto_onetimeauth(@Out byte[] out, @In byte[] in, @u_int64_t int inLen, @In byte[] key);

		int crypto_onetimeauth_bytes();

		int crypto_onetimeauth_keybytes();

		int randombytes_buf(@Out byte[] out, @In @u_int64_t int outLen);

		int sodium_init();

		String sodium_version_string();

	}

}
