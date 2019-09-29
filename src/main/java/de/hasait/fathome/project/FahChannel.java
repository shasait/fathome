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

import java.util.Collections;
import java.util.Set;
import java.util.TreeSet;

/**
 *
 */
public class FahChannel extends AbstractFahProjectPart {

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
		getProject().getFreeAtHome().switchActuator(this, state);
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
