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

package de.hasait.fathome.util.http;

import java.nio.charset.Charset;
import java.util.function.Consumer;

import org.apache.http.entity.ContentType;

/**
 *
 */
public class AsStringContentHandler implements HttpContentHandler {

	private final Consumer<String> stringConsumer;

	public AsStringContentHandler(Consumer<String> stringConsumer) {
		this.stringConsumer = stringConsumer;
	}


	@Override
	public void handle(byte[] contentBytes, ContentType contentType) {
		Charset charsetMayBeNull = contentType.getCharset();
		Charset charset = charsetMayBeNull != null ? charsetMayBeNull : Charset.defaultCharset();
		String contentString = new String(contentBytes, charset);
		stringConsumer.accept(contentString);
	}

}
