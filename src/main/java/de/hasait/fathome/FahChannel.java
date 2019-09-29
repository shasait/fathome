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
import java.util.Set;
import java.util.TreeSet;

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

	private final Set<String> idps = new TreeSet<>();

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
		if (state < 0 || state > 100) {
			throw new IllegalArgumentException("state not in range [0, 100]: " + state);
		}
		if (!canDim()) {
			throw new IllegalArgumentException("Dim not supported: " + name + " (" + function.getFidName() + ")");
		}

		Value dpPath = Value.of(device.getSerialNumber() + "/" + i + "/" + "idp002");
		Value dpValue = Value.of(Integer.toString(state));
		Value result = getFreeAtHome().rpcCall("RemoteInterface.setDatapoint", dpPath, dpValue);
		log.info("result: " + result);
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

	public Set<String> getIdps() {
		return Collections.unmodifiableSet(idps);
	}

	public String getName() {
		return name;
	}

	public void switchActuator(boolean state) {
		if (!canSwitch()) {
			throw new IllegalArgumentException("Switch not supported: " + name + " (" + function.getFidName() + ")");
		}
		Value dpPath = Value.of(device.getSerialNumber() + "/" + i + "/" + "idp000");
		Value dpValue = Value.of(state ? "1" : "0");

		Value result = getFreeAtHome().rpcCall("RemoteInterface.setDatapoint", dpPath, dpValue);
		log.info("result: " + result);
	}

	void addInputDataPoint(String idp) {
		idps.add(idp);
	}

	void setFunction(FahFunction function) {
		this.function = function;
	}

	void setName(String name) {
		this.name = name;
	}

}
