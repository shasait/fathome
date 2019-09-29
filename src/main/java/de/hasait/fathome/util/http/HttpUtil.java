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

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLContext;

import org.apache.commons.fileupload.MultipartStream;
import org.apache.commons.io.IOUtils;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.utils.HttpClientUtils;
import org.apache.http.conn.ssl.NoopHostnameVerifier;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.conn.ssl.TrustSelfSignedStrategy;
import org.apache.http.entity.ByteArrayEntity;
import org.apache.http.entity.ContentType;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.message.BasicHeader;
import org.apache.http.message.HeaderGroup;
import org.apache.http.ssl.SSLContextBuilder;

/**
 *
 */
public final class HttpUtil {

	public static BasicCredentialsProvider buildCredentialsProvider(String username, String password) {
		BasicCredentialsProvider credentialsProvider = new BasicCredentialsProvider();
		UsernamePasswordCredentials credentials = new UsernamePasswordCredentials(username, password);
		credentialsProvider.setCredentials(new AuthScope(AuthScope.ANY_HOST, AuthScope.ANY_PORT), credentials);
		return credentialsProvider;
	}

	public static SSLConnectionSocketFactory buildInsecureSSLConnectionSocketFactory() {
		SSLContext sslContext;
		try {
			sslContext = SSLContextBuilder.create().loadTrustMaterial(new TrustSelfSignedStrategy()).build();
		} catch (NoSuchAlgorithmException | KeyManagementException | KeyStoreException e) {
			throw new RuntimeException("SSL initialization failed", e);
		}

		HostnameVerifier allowAllHosts = new NoopHostnameVerifier();
		SSLConnectionSocketFactory sslConnectionSocketFactory = new SSLConnectionSocketFactory(sslContext, allowAllHosts);
		return sslConnectionSocketFactory;
	}

	public static void handleMultipart(HttpEntity entity, HttpContentHandler handler) throws IOException {
		if (entity != null) {
			ContentType contentType = ContentType.parse(entity.getContentType().getValue());
			if (contentType.getMimeType().equals("multipart/mixed")) {
				parseMultipart(entity, contentType, handler);
			} else {
				byte[] contentBytes = IOUtils.toByteArray(entity.getContent());
				handler.handle(contentBytes, contentType);
			}
		}
	}

	public static void httpGet(HttpClient httpClient, String url, HttpContentHandler handler) throws IOException {
		HttpGet httpGet = new HttpGet(url);
		HttpResponse httpResponse = httpClient.execute(httpGet);
		try {
			int statusCode = httpResponse.getStatusLine().getStatusCode();
			if (statusCode == HttpStatus.SC_GATEWAY_TIMEOUT || statusCode == HttpStatus.SC_REQUEST_TIMEOUT) {
				throw new IOException("Timeout " + statusCode);
			}
			if (statusCode == HttpStatus.SC_INTERNAL_SERVER_ERROR) {
				throw new IOException("Internal Server Error " + statusCode);
			}
			if (statusCode != HttpStatus.SC_OK) {
				throw new RuntimeException("Failed " + statusCode);
			}
			handleMultipart(httpResponse.getEntity(), handler);
		} finally {
			HttpClientUtils.closeQuietly(httpResponse);
		}
	}

	private static HeaderGroup parseHeaders(String rawHeaders) {
		HeaderGroup headerGroup = new HeaderGroup();
		String[] headers = rawHeaders.split("\r?\n");
		for (String header : headers) {
			int ioc = header.indexOf(':');
			if (ioc < 1) {
				throw new RuntimeException("Invalid header: " + header);
			}
			String name = header.substring(0, ioc);
			String value = header.substring(ioc + 1);
			BasicHeader basicHeader = new BasicHeader(name, value);
			headerGroup.addHeader(basicHeader);
		}
		return headerGroup;
	}

	private static void parseMultipart(HttpEntity entity, ContentType contentType, HttpContentHandler handler) throws IOException {
		String boundary = contentType.getParameter("boundary");
		MultipartStream multipartStream = new MultipartStream(entity.getContent(), boundary.getBytes(), 4096, null);
		boolean hasMoreParts = multipartStream.skipPreamble();
		while (hasMoreParts && !Thread.currentThread().isInterrupted()) {
			HeaderGroup partHeaders = parseHeaders(multipartStream.readHeaders());
			ContentType partContentType = ContentType.parse(partHeaders.getFirstHeader("Content-Type").getValue());
			ByteArrayOutputStream partContentBaos = new ByteArrayOutputStream();
			multipartStream.readBodyData(partContentBaos);
			byte[] partContentBytes = partContentBaos.toByteArray();
			ByteArrayEntity partEntity = new ByteArrayEntity(partContentBytes, partContentType);
			handleMultipart(partEntity, handler);
			hasMoreParts = multipartStream.readBoundary();
		}
	}

	private HttpUtil() {
		super();
	}

}
