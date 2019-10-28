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

package de.hasait.fathome.util;

import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.text.Bidi;
import java.text.Normalizer;
import java.util.regex.Pattern;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

/**
 *
 */
public final class CryptUtil {

	private static final byte[] INT1 = new byte[]{
			0,
			0,
			0,
			1
	};

	private static final Pattern SASL_PREP_SPACE = Pattern.compile("([\u00A0\u1680\u2000-\u200B\u202F\u205F\u3000])");
	private static final Pattern SASL_PREP_REMOVE = Pattern
			.compile("([\u00AD\u034F\u1806\u180B-\u180D\u200B-\u200D\u2060\uFE00-\uFE0F\uFEFF])");
	private static final Pattern SASL_PREP_PROHIBITED = Pattern.compile("[^\\p{L}\\p{M}\\p{N}\\p{P}\\p{S}\\s]");

	public static byte[] computeSaltedPassword(String hmacAlgorithm, char[] password, byte[] salt, int iterations) {
		return hi(hmacAlgorithm, saslPrepare(new String(password)).getBytes(StandardCharsets.UTF_8), salt, iterations);
	}

	public static byte[] hi(String hmacAlgorithm, byte[] password, byte[] salt, int iterations) {
		Mac mac = hmac(hmacAlgorithm, password);
		mac.update(salt);
		mac.update(INT1);

		byte[] uPrev = mac.doFinal();
		byte[] result = uPrev;

		for (int c = 1; c < iterations; c++) {
			mac.update(uPrev);
			uPrev = mac.doFinal();
			result = xor(result, uPrev);
		}

		return result;
	}

	public static byte[] hmac(String hmacAlgorithm, byte[] key, byte[] str) {
		Mac mac = hmac(hmacAlgorithm, key);
		mac.update(str);
		return mac.doFinal();
	}

	public static Mac hmac(String hmacAlgorithm, byte[] key) {
		Mac mac;
		try {
			mac = Mac.getInstance(hmacAlgorithm);
		} catch (NoSuchAlgorithmException e) {
			throw new RuntimeException(e);
		}
		try {
			mac.init(new SecretKeySpec(key, hmacAlgorithm));
		} catch (InvalidKeyException e) {
			throw new RuntimeException(e);
		}
		return mac;
	}

	public static String saslPrepare(String input) {
		String inputAfterSpace = SASL_PREP_SPACE.matcher(input).replaceAll(" ");
		String inputAfterRemove = SASL_PREP_REMOVE.matcher(inputAfterSpace).replaceAll("");
		String inputNormalized = Normalizer.normalize(inputAfterRemove, Normalizer.Form.NFKC);
		if (SASL_PREP_PROHIBITED.matcher(inputNormalized).find()) {
			throw new IllegalArgumentException("String contains prohibited characters");
		}
		if (Bidi.requiresBidi(inputNormalized.toCharArray(), 0, inputNormalized.length())) {
			Bidi bidi = new Bidi(input, Bidi.DIRECTION_LEFT_TO_RIGHT);
			if (bidi.isMixed()) {
				if (!(bidi.getLevelAt(0) == Bidi.DIRECTION_RIGHT_TO_LEFT && bidi.getLevelAt(0) == bidi.getLevelAt(input.length() - 1))) {
					throw new IllegalArgumentException("String contains mixed bidi characters");
				}
			}
		}

		return inputNormalized;
	}

	private static byte[] xor(byte[] a, byte[] b) {
		byte[] c = new byte[a.length];
		for (int i = 0; i < a.length; i++) {
			c[i] = (byte) (a[i] ^ b[i]);
		}
		return c;
	}

	private CryptUtil() {
		super();
	}

}
