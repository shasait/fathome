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

package de.hasait.fathome;

import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import rocks.xmpp.extensions.rpc.model.Value;

/**
 *
 */
public class FahChannel extends AbstractFahPart {

	private static final Logger log = LoggerFactory.getLogger(FahChannel.class);

	private static final String SWITCH_IDP = "idp000";
	private static final String DIM_IDP = "idp002";

	private final FahDevice device;
	private final String i;

	private final Map<String, String> dataPointValueByI = new TreeMap<>();

	private FahFunction function;
	private String name;

	FahChannel(FahDevice device, String i) {
		super();

		this.device = device;
		this.i = i;

		device.addChannel(this);
	}

	public boolean canDim() {
		String fidName = function.getFidName();
		return "FID_DimmingActuator".equals(fidName);
	}

	public boolean canSwitch() {
		String fidName = function.getFidName();
		return "FID_SwitchingActuator".equals(fidName) || "FID_DimmingActuator".equals(fidName);
	}

	public void dimActuator(int state) {
		assertCanDim();

		if (state < 0 || state > 100) {
			throw new IllegalArgumentException("state not in range [0, 100]: " + state);
		}

		String value = Integer.toString(state);
		rpcSetDataPoint(DIM_IDP, value);
	}

	public Set<String> getDataPointIs() {
		return Collections.unmodifiableSet(dataPointValueByI.keySet());
	}

	public String getDataPointValue(String i) {
		return dataPointValueByI.get(i);
	}

	public FahDevice getDevice() {
		return device;
	}

	public int getDimState() {
		assertCanDim();

		String state = getDataPointValue(DIM_IDP);
		return state == null ? 0 : Integer.parseInt(state);
	}

	public FahFunction getFunction() {
		return function;
	}

	public String getI() {
		return i;
	}

	public String getName() {
		return name;
	}

	public boolean getSwitchState() {
		assertCanSwitch();

		String state = getDataPointValue(SWITCH_IDP);
		return "1".equals(state);
	}

	public void switchActuator(boolean state) {
		assertCanSwitch();

		rpcSetDataPoint(SWITCH_IDP, state ? "1" : "0");
	}

	void setDataPoint(String i, String value) {
		dataPointValueByI.put(i, value);
	}

	void setFunction(FahFunction function) {
		this.function = function;
	}

	void setName(String name) {
		this.name = name;
	}

	private void assertCanDim() {
		if (!canDim()) {
			throw new RuntimeException("Dim not supported: " + name + " (" + function.getFidName() + ")");
		}
	}

	private void assertCanSwitch() {
		if (!canSwitch()) {
			throw new RuntimeException("Switch not supported: " + name + " (" + function.getFidName() + ")");
		}
	}

	private void rpcSetDataPoint(String dp, String value) {
		Value dpPath = Value.of(device.getSerialNumber() + "/" + i + "/" + dp);
		Value dpValue = Value.of(value);

		Value result = getFreeAtHome().rpcCall("RemoteInterface.setDatapoint", dpPath, dpValue);
		log.info("result: " + result);
		setDataPoint(dp, value);
	}

}
