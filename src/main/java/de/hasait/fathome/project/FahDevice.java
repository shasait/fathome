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

import java.util.HashSet;
import java.util.Set;

/**
 *
 */
public class FahDevice extends AbstractFahProjectPart {

	private final String serialNumber;

	private final Set<FahChannel> channels = new HashSet<>();

	private FahString type;
	private String name;
	private FahRoom room;

	FahDevice(String serialNumber) {
		super();

		this.serialNumber = serialNumber;
	}

	public String getName() {
		return name;
	}

	public FahRoom getRoom() {
		return room;
	}

	public String getSerialNumber() {
		return serialNumber;
	}

	public FahString getType() {
		return type;
	}

	void addChannel(FahChannel channel) {
		channels.add(channel);
	}

	void setName(String name) {
		this.name = name;
	}

	void setRoom(FahRoom newRoom) {
		if (room != null) {
			room.removeDevice(this);
		}
		room = newRoom;
		if (room != null) {
			room.addDevice(this);
		}
	}

	void setType(FahString type) {
		this.type = type;
	}

}