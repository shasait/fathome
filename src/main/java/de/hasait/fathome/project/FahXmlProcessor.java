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

import java.io.StringReader;
import java.util.List;
import java.util.stream.Stream;

import javax.xml.bind.JAXB;

import de.hasait.fathome.xml.project.PxKeyValue;
import de.hasait.fathome.xml.project.PxProject;
import de.hasait.fathome.xml.update.UxProject;

/**
 *
 */
public class FahXmlProcessor {

	public static void processProjectXml(String xml, FahProject fahProject, FahChannelFactory channelFactory) {
		PxProject xProject = JAXB.unmarshal(new StringReader(xml), PxProject.class);
		xProject.sysapValues.forEach(xSysapValue -> fahProject.setFahSysapValue(xSysapValue.name, xSysapValue.value));
		xProject.configValues.forEach(xConfigValue -> fahProject.setFahConfigValue(xConfigValue.name, xConfigValue.value));
		xProject.strings.forEach(xString -> {
			FahString fahString = new FahString(parseId(xString.nameId));
			fahString.setValue(xString.value);
			fahProject.addPart(fahString);
		});
		xProject.fahDefinitions.functions.forEach(xFunction -> {
			FahFunction fahFunction = new FahFunction(parseId(xFunction.functionId));
			fahFunction.setFidName(xFunction.name);
			fahFunction.setName(fahProject.getStringByNameId(parseId(xFunction.nameId)));
			fahProject.addPart(fahFunction);
		});
		xProject.floors.forEach(xFloor -> {
			FahFloor fahFloor = new FahFloor(xFloor.uid);
			fahFloor.setName(xFloor.name);
			fahFloor.setLevel(xFloor.level);
			xFloor.rooms.forEach(xRoom -> {
				FahRoom fahRoom = new FahRoom(fahFloor, xRoom.uid);
				fahRoom.setName(xRoom.name);
				fahProject.addPart(fahRoom);
			});
			fahProject.addPart(fahFloor);
		});
		xProject.devices.forEach(xDevice -> {
			FahDevice fahDevice = new FahDevice(xDevice.serialNumber);
			fahDevice.setType(fahProject.getStringByNameId(parseId(xDevice.nameId)));
			fahDevice.setFunction(fahProject.getFunctionByFunctionId(parseId(xDevice.functionId)));
			fahDevice.setName(findKV(xDevice.attributes, "displayName"));
			String roomUid = findKV(xDevice.attributes, "room");
			fahDevice.setRoom(fahProject.getRoomByUid(roomUid));
			xDevice.channels.forEach(xChannel -> {
				Integer functionId = parseId(findKV(xChannel.attributes, "functionId"));
				FahFunction function = fahProject.getFunctionByFunctionId(functionId);
				AbstractFahChannel fahChannel = channelFactory.createChannel(fahDevice, xChannel.i, function);
				fahChannel.setName(findKV(xChannel.attributes, "displayName"));
				Stream.concat(xChannel.inputs.stream(), xChannel.outputs.stream()).forEach(pxDataPoint -> {
					String value = pxDataPoint.values.stream().findFirst().orElse(null);
					fahChannel.setDataPoint(pxDataPoint.i, value);
				});
				fahProject.addPart(fahChannel);
			});
			fahProject.addPart(fahDevice);
		});
	}

	public static void processUpdateXml(String xml, FahProject fahProject) {
		UxProject xProject = JAXB.unmarshal(new StringReader(xml), UxProject.class);
		xProject.sysapValues.forEach(xSysapValue -> {
			fahProject.setFahSysapValue(xSysapValue.name, xSysapValue.value);
		});
		xProject.devices.forEach(xDevice -> {
			FahDevice fahDevice = fahProject.getDeviceBySerialNumber(xDevice.serialNumber);
			if (fahDevice != null) {
				xDevice.channels.forEach(xChannel -> {
					AbstractFahChannel fahChannel = fahDevice.getChannel(xChannel.i);
					if (fahChannel != null) {
						Stream.concat(xChannel.inputs.stream(), xChannel.outputs.stream()).forEach(xDataPoint -> {
							String value = xDataPoint.values.stream().findFirst().orElse(null);
							fahChannel.setDataPoint(xDataPoint.i, value);
						});
					}
				});
			}
		});
	}

	private static String findKV(List<PxKeyValue> keyValueList, String key) {
		return keyValueList.stream().filter(kv -> key.equals(kv.name)).findAny().map(kv -> kv.value).orElse(null);
	}

	private static Integer parseId(String id) {
		return id != null ? Integer.parseInt(id, 16) : null;
	}


	private FahXmlProcessor() {
		super();
	}

}
