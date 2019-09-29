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

package de.hasait.fathome.project;

import java.io.IOException;
import java.io.StringReader;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

import de.hasait.fathome.util.xpp.XppWalker;

/**
 *
 */
public class FahProjectParser {

	public static void parse(String xml, FahProject fahProject) {
		XppWalker walker = //
				new XppWalker( //
				).putTag("project", //
						 new XppWalker( //
						 ).putTag("sysap", //
								  new XppWalker( //
								  ).putTag("value", //
										   valueParser -> {
											   String name = valueParser.getAttributeValue(null, "name");
											   String value = valueParser.nextText();
											   fahProject.addSysap(name, value);
										   }
								  )
						 ).putTag("config", //
								  new XppWalker( //
								  ).putTag("var", //
										   varParser -> {
											   String name = varParser.getAttributeValue(null, "name");
											   String value = varParser.nextText();
											   fahProject.addConfig(name, value);
										   }
								  )
						 ).putTag("strings", //
								  new XppWalker( //
								  ).putTag("string", //
										   stringParser -> {
											   int nameId = parseId(stringParser.getAttributeValue(null, "nameId"));
											   String value = stringParser.nextText();
											   FahString fahString = new FahString(nameId);
											   fahString.setValue(value);
											   fahProject.addPart(fahString);
										   }
								  )
						 ).putTag("definitions", //
								  new XppWalker( //
								  ).putTag("functions", //
										   new XppWalker( //
										   ).putTag("function", //
													functionParser -> {
														int nameId = parseId(functionParser.getAttributeValue(null, "nameId"));
														int functionId = parseId(functionParser.getAttributeValue(null, "functionId"));
														String name = functionParser.getAttributeValue(null, "name");
														FahFunction fahFunction = new FahFunction(fahProject, functionId);
														fahFunction.setFidName(name);
														fahFunction.setName(fahProject.getStringByNameId(nameId));
														fahProject.addPart(fahFunction);
													}
										   )
								  )
						 ).putTag("floorplan", //
								  new XppWalker( //
								  ).putTag("floor", //
										   floorParser -> {
											   String floorUid = floorParser.getAttributeValue(null, "uid");
											   int floorLevel = Integer.parseInt(floorParser.getAttributeValue(null, "level"));
											   String floorName = floorParser.getAttributeValue(null, "name");
											   FahFloor fahFloor = new FahFloor(floorUid);
											   fahFloor.setLevel(floorLevel);
											   fahFloor.setName(floorName);
											   fahProject.addPart(fahFloor);

											   new XppWalker( //
											   ).putTag("room", //
														roomParser -> {
															String roomUid = roomParser.getAttributeValue(null, "uid");
															String roomName = roomParser.getAttributeValue(null, "name");
															FahRoom fahRoom = new FahRoom(fahFloor, roomUid);
															fahRoom.setName(roomName);
															fahProject.addPart(fahRoom);
														}
											   ).parse(floorParser);
										   }
								  )
						 ).putTag("devices", //
								  new XppWalker( //
								  ).putTag("device", //
										   deviceParser -> {
											   String deviceSerialNumber = deviceParser.getAttributeValue(null, "serialNumber");
											   int deviceNameId = parseId(deviceParser.getAttributeValue(null, "nameId"));
											   FahDevice fahDevice = new FahDevice(deviceSerialNumber);
											   fahDevice.setType(fahProject.getStringByNameId(deviceNameId));

											   new XppWalker( //
											   ).putTag("attribute", //
														deviceAttributeParser -> {
															String name = deviceAttributeParser.getAttributeValue(null, "name");
															String value = deviceAttributeParser.nextText();
															if ("displayName".equals(name)) {
																fahDevice.setName(value);
															}
															if ("room".equals(name)) {
																fahDevice.setRoom(fahProject.getRoomByUid(value));
															}
														}
											   ).putTag("channels", //
														new XppWalker( //
														).putTag("channel", //
																 channelParser -> {
																	 String channelI = channelParser.getAttributeValue(null, "i");
																	 FahChannel fahChannel = new FahChannel(fahDevice, channelI);

																	 new XppWalker( //
																	 ).putTag("attribute", //
																			  deviceAttributeParser -> {
																				  String name = deviceAttributeParser
																						  .getAttributeValue(null, "name");
																				  String value = deviceAttributeParser.nextText();
																				  if ("displayName".equals(name)) {
																					  fahChannel.setName(value);
																				  }
																				  if ("functionId".equals(name)) {
																					  int functionId = Integer.parseInt(value, 16);
																					  fahChannel.setFunction(fahProject
																													 .getFunctionByFunctionId(
																															 functionId));
																				  }
																			  }
																	 ).putTag("inputs", //
																			  new XppWalker( //
																			  ).putTag("dataPoint", //
																					   dataPointParser -> {
																						   String dataPointI = dataPointParser
																								   .getAttributeValue(null, "i");
																						   fahChannel.addInputDataPoint(dataPointI);
																					   }
																			  )
																	 ).parse(channelParser);

																	 if (fahChannel.getName() == null) {
																		 String name = fahDevice.getName();
																		 if (name != null) {
																			 fahChannel.setName(name);
																		 }
																	 }

																	 fahProject.addPart(fahChannel);
																 }
														)
											   ).parse(deviceParser);

											   fahProject.addPart(fahDevice);
										   }
								  )
						 )
				);

		// FID_SwitchingActuator, FID_DimmingActuator
		try {
			XmlPullParser parser = XmlPullParserFactory.newInstance().newPullParser();
			parser.setInput(new StringReader(xml));
			walker.parse(parser);
		} catch (XmlPullParserException | IOException e) {
			throw new RuntimeException(e);
		}
	}

	private static int parseId(String id) {
		return Integer.parseInt(id, 16);
	}

	private FahProjectParser() {
		super();
	}

}
