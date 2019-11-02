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

import de.hasait.fathome.project.AbstractFahChannel;
import de.hasait.fathome.project.FahDevice;
import de.hasait.fathome.project.FahFunction;

/**
 *
 */
public class FahSwitch extends AbstractFahChannel {

	static final String DEFAULT_SWITCH_DP = "idp0000";

	private final String switchDatapoint;

	public FahSwitch(FahDevice device, String id, FahFunction function) {
		this(device, id, function, DEFAULT_SWITCH_DP);
	}

	public FahSwitch(FahDevice device, String id, FahFunction function, String switchDatapoint) {
		super(device, id, function);

		this.switchDatapoint = switchDatapoint;
	}

	public boolean isOff() {
		return !isOn();
	}

	public boolean isOn() {
		String state = getDataPointValue(switchDatapoint);
		return "1".equals(state);
	}

	public void setSwitchState(boolean state) {
		rpcSetDataPoint(switchDatapoint, state ? "1" : "0");
	}

	public void switchOff() {
		setSwitchState(false);
	}

	public void switchOn() {
		setSwitchState(true);
	}

	public void toggle() {
		setSwitchState(!isOn());
	}

}
