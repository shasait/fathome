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
import java.util.HashSet;
import java.util.Set;

/**
 *
 */
public class FahRoom extends AbstractFahPart {

	private final FahFloor floor;
	private final String uid;

	private final Set<FahDevice> devices = new HashSet<>();

	private String name;

	FahRoom(FahFloor floor, String uid) {
		super();

		this.floor = floor;
		this.uid = uid;

		floor.addRoom(this);
	}

	public Set<FahDevice> getDevices() {
		return Collections.unmodifiableSet(devices);
	}

	public FahFloor getFloor() {
		return floor;
	}

	public String getName() {
		return name;
	}

	public String getUid() {
		return uid;
	}

	void addDevice(FahDevice device) {
		devices.add(device);
	}

	void removeDevice(FahDevice device) {
		devices.remove(device);
	}

	void setName(String name) {
		this.name = name;
	}

}
