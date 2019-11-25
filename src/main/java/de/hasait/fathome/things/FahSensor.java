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

package de.hasait.fathome.things;

import java.util.ArrayList;
import java.util.List;

import de.hasait.fathome.project.AbstractFahChannel;
import de.hasait.fathome.project.FahDevice;
import de.hasait.fathome.project.FahFunction;

/**
 *
 */
public class FahSensor extends AbstractFahChannel {

	static final String DEFAULT_SENSOR_DP = "odp0000";

	public static void processDeviceForSensors(FahDevice device) {
		List<Object[]> args = new ArrayList<>();
		String deviceId = device.getDeviceId();
		if ("1000".equals(deviceId)) {
			String value = device.getParameterValue("pm0000");
			if ("1".equals(value)) {
				args.add(new Object[]{
						"0",
						"NS"
				});
			} else if ("2".equals(value)) {
				args.add(new Object[]{
						"1",
						"IS1"
				});
				args.add(new Object[]{
						"2",
						"IS2"
				});
			}
		} else if ("1002".equals(deviceId)) {
			String value = device.getParameterValue("pm0000");
			if ("1".equals(value) || "2".equals(value)) {
				args.add(new Object[]{
						"0",
						"LNS"
				});
			}
			if ("3".equals(value) || "4".equals(value)) {
				args.add(new Object[]{
						"1",
						"LIS1"
				});
				args.add(new Object[]{
						"2",
						"LIS2"
				});
			}
			if ("1".equals(value) || "3".equals(value)) {
				args.add(new Object[]{
						"3",
						"RNS"
				});
			}
			if ("2".equals(value) || "4".equals(value)) {
				args.add(new Object[]{
						"4",
						"RIS1"
				});
				args.add(new Object[]{
						"5",
						"RIS2"
				});
			}
		}
		for (Object[] arg : args) {
			FahSensor fahSensor = new FahSensor(device, "ch000" + arg[0], device.getFunction());
			fahSensor.setName(device.getName() + "-" + arg[1]);
			device.getProject().addPart(fahSensor);
		}
	}

	private final String sensorDatapoint;


	public FahSensor(FahDevice device, String id, FahFunction function) {
		this(device, id, function, DEFAULT_SENSOR_DP);
	}

	public FahSensor(FahDevice device, String id, FahFunction function, String sensorDatapoint) {
		super(device, id, function);

		this.sensorDatapoint = sensorDatapoint;
	}

	/**
	 * @deprecated Will be replaced by higher level variants.
	 */
	@Deprecated
	public String getRawState() {
		return getDataPointValue(sensorDatapoint);
	}

}
