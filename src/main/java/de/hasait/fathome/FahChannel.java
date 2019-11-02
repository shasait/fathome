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
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import rocks.xmpp.extensions.rpc.model.Value;

/**
 *
 */
public class FahChannel extends AbstractFahPart {

	private static final Logger log = LoggerFactory.getLogger(FahChannel.class);

	private final FahDevice device;
	private final String i;

	private final Map<String, String> dataPointValueByI = new ConcurrentHashMap<>();

	private FahFunction function;
	private String name;

	FahChannel(FahDevice device, String i) {
		super();

		this.device = device;
		this.i = i;

		device.addChannel(this);
	}

	public FahBlind asBlind() {
		assertFunction(isBlind(), "blind");
		return new FahBlind(this);
	}

	public FahDimmer asDimmer() {
		assertFunction(isDimmer(), "dimmer");
		return new FahDimmer(this);
	}

	public FahScene asScene() {
		assertFunction(isScene(), "scene");
		return new FahScene(this);
	}

	public FahSwitch asSwitch() {
		assertFunction(isSwitch(), "switch");
		return new FahSwitch(this);
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

	public FahFunction getFunction() {
		return function;
	}

	public String getI() {
		return i;
	}

	public String getName() {
		return name;
	}

	public boolean isBlind() {
		String fidName = function.getFidName();
		return "FID_BlindActuator".equals(fidName) || "FID_ShutterActuator".equals(fidName);
	}

	public boolean isDimmer() {
		String fidName = function.getFidName();
		return "FID_DimmingActuator".equals(fidName);
	}

	public boolean isScene() {
		return function.getFunctionId() == 0x4800;
	}

	public boolean isSwitch() {
		String fidName = function.getFidName();
		return "FID_SwitchingActuator".equals(fidName) || "FID_DimmingActuator".equals(fidName);
	}

	void rpcSetDataPoint(String dp, String value) {
		Value dpPath = Value.of(device.getSerialNumber() + "/" + i + "/" + dp);
		Value dpValue = Value.of(value);

		Value result = getFreeAtHome().rpcCall("RemoteInterface.setDatapoint", dpPath, dpValue);
		log.info("result: " + result);
		setDataPoint(dp, value);
	}

	void setDataPoint(String i, String value) {
		String oldValue = value != null ? dataPointValueByI.put(i, value) : dataPointValueByI.remove(i);
		if (!Objects.equals(oldValue, value)) {
			log.info(name + "@" + i + " changed: " + oldValue + " -> " + value);
		}
	}

	void setFunction(FahFunction function) {
		this.function = function;
	}

	void setName(String name) {
		this.name = name;
	}

	private void assertFunction(boolean assertion, String type) {
		if (!assertion) {
			throw new RuntimeException("Not a " + type + ": " + name + " (" + function.getFidName() + "/" + function.getFunctionId() + ")");
		}
	}

}
