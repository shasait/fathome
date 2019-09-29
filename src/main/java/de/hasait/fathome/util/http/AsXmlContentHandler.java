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

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.util.function.Consumer;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.http.entity.ContentType;
import org.w3c.dom.Document;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

/**
 *
 */
public class AsXmlContentHandler implements HttpContentHandler {

	private final DocumentBuilder documentBuilder;

	private final Consumer<Document> documentConsumer;

	public AsXmlContentHandler(final Consumer<Document> documentConsumer) {
		this.documentConsumer = documentConsumer;

		try {
			this.documentBuilder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
		} catch (ParserConfigurationException e) {
			throw new RuntimeException(e);
		}
	}


	@Override
	public void handle(byte[] contentBytes, ContentType contentType) {
		Charset charsetMayBeNull = contentType.getCharset();
		Charset charset = charsetMayBeNull != null ? charsetMayBeNull : Charset.defaultCharset();
		InputSource inputSource = new InputSource(new InputStreamReader(new ByteArrayInputStream(contentBytes), charset));
		Document document;
		try {
			document = documentBuilder.parse(inputSource);
		} catch (SAXException | IOException e) {
			throw new RuntimeException(e);
		}
		documentConsumer.accept(document);
	}

}
