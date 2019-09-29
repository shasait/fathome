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

package de.hasait.fathome.util.xpp;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

/**
 *
 */
public class XppWalker implements XppHandler {

	private static final Logger log = LoggerFactory.getLogger(XppWalker.class);

	private final Map<String, XppHandler> tags = new HashMap<>();

	public void parse(XmlPullParser parser) throws IOException, XmlPullParserException {
		int initialDepth = parser.getDepth();

		while (true) {
			int eventType = parser.next();
			switch (eventType) {
				case XmlPullParser.START_TAG:
					String tagName = parser.getName();
					XppHandler xppHandler = tags.get(tagName);
					if (xppHandler != null) {
						xppHandler.parse(parser);
					} else {
						log.warn("Ignored tag: " + tagName);
					}
					break;
				case XmlPullParser.END_TAG:
					if (parser.getDepth() == initialDepth) {
						return;
					}
					break;
				case XmlPullParser.END_DOCUMENT:
					return;
			}
		}
	}

	public XppWalker putTag(String tagName, XppHandler xppHandler) {
		tags.put(tagName, xppHandler);
		return this;
	}

}
