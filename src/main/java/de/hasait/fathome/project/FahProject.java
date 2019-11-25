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

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import de.hasait.fathome.comm.FahCommunication;

/**
 *
 */
public class FahProject {

	private final FahCommunication communication;

	private final Set<AbstractFahPart> parts = new HashSet<>();
	private final Map<String, String> sysap = new TreeMap<>();
	private final Map<String, String> config = new TreeMap<>();
	private final Map<Integer, FahString> stringById = new TreeMap<>();
	private final Map<Integer, FahFunction> functionById = new TreeMap<>();
	private final Map<Integer, FahFloor> floorById = new TreeMap<>();
	private final Map<String, FahFloor> floorByName = new TreeMap<>();
	private final Map<Integer, FahRoom> roomById = new TreeMap<>();
	private final Map<String, FahDevice> deviceBySerialNumber = new TreeMap<>();
	private final Map<String, AbstractFahChannel> channelByName = new TreeMap<>();

	public FahProject(FahCommunication communication) {
		super();

		this.communication = communication;
	}

	public void addPart(AbstractFahPart part) {
		if (parts.contains(part)) {
			return;
		}
		part.setProject(this);

		if (part instanceof FahString) {
			FahString fah = (FahString) part;
			stringById.put(fah.getId(), fah);
		}
		if (part instanceof FahFunction) {
			FahFunction fah = (FahFunction) part;
			functionById.put(fah.getId(), fah);
		}
		if (part instanceof FahFloor) {
			FahFloor fah = (FahFloor) part;
			floorById.put(fah.getId(), fah);
			String name = fah.getName();
			if (name != null) {
				floorByName.put(name, fah);
			}
		}
		if (part instanceof FahRoom) {
			FahRoom fah = (FahRoom) part;
			roomById.put(fah.getId(), fah);
		}
		if (part instanceof FahDevice) {
			FahDevice fah = (FahDevice) part;
			deviceBySerialNumber.put(fah.getSerialNumber(), fah);
		}
		if (part instanceof AbstractFahChannel) {
			AbstractFahChannel fah = (AbstractFahChannel) part;
			String name = fah.getName();
			if (name != null) {
				channelByName.put(name, fah);
			}
		}
	}

	public Collection<AbstractFahChannel> getAllChannels() {
		return Collections.unmodifiableCollection(channelByName.values());
	}

	public Collection<FahFloor> getAllFloors() {
		return Collections.unmodifiableCollection(floorById.values());
	}

	public AbstractFahChannel getChannel(String name) {
		return name != null ? channelByName.get(name) : null;
	}

	public FahCommunication getCommunication() {
		return communication;
	}

	public FahDevice getDeviceBySerialNumber(String serialNumber) {
		return serialNumber != null ? deviceBySerialNumber.get(serialNumber) : null;
	}

	public FahFloor getFloorByName(String name) {
		return name != null ? floorByName.get(name) : null;
	}

	FahFunction getFunctionById(Integer id) {
		return id != null ? functionById.get(id) : null;
	}

	FahRoom getRoomById(Integer id) {
		return id != null ? roomById.get(id) : null;
	}

	FahString getStringById(Integer id) {
		return id != null ? stringById.get(id) : null;
	}

	void setFahConfigValue(String name, String value) {
		config.put(name, value);
	}

	void setFahSysapValue(String name, String value) {
		sysap.put(name, value);
	}

}
