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

import de.hasait.fathome.FreeAtHome;

/**
 *
 */
public class FahProject {

	private final Set<AbstractFahProjectPart> parts = new HashSet<>();
	private final Map<String, String> sysap = new TreeMap<>();
	private final Map<String, String> config = new TreeMap<>();
	private final Map<Integer, FahString> stringByNameId = new TreeMap<>();
	private final Map<Integer, FahFunction> functionByFunctionId = new TreeMap<>();
	private final Map<String, FahFloor> floorByUid = new TreeMap<>();
	private final Map<String, FahRoom> roomByUid = new TreeMap<>();
	private final Map<String, FahDevice> deviceBySerialNumber = new TreeMap<>();
	private final Map<String, FahChannel> channelByName = new TreeMap<>();

	private final FreeAtHome freeAtHome;

	public FahProject(FreeAtHome freeAtHome) {
		this.freeAtHome = freeAtHome;
	}

	public Collection<FahChannel> getAllChannels() {
		return Collections.unmodifiableCollection(channelByName.values());
	}

	public FahChannel getChannel(String name) {
		return channelByName.get(name);
	}

	public FreeAtHome getFreeAtHome() {
		return freeAtHome;
	}

	public FahFunction getFunctionByFunctionId(int functionId) {
		return functionByFunctionId.get(functionId);
	}

	public FahRoom getRoomByUid(String uid) {
		return roomByUid.get(uid);
	}

	public FahString getStringByNameId(int nameId) {
		return stringByNameId.get(nameId);
	}

	void addConfig(String name, String value) {
		config.put(name, value);
	}

	void addPart(AbstractFahProjectPart part) {
		if (parts.contains(part)) {
			return;
		}
		part.setProject(this);

		if (part instanceof FahString) {
			FahString fah = (FahString) part;
			stringByNameId.put(fah.getId(), fah);
		}
		if (part instanceof FahFunction) {
			FahFunction fah = (FahFunction) part;
			functionByFunctionId.put(fah.getFunctionId(), fah);
		}
		if (part instanceof FahFloor) {
			FahFloor fah = (FahFloor) part;
			floorByUid.put(fah.getUid(), fah);
		}
		if (part instanceof FahRoom) {
			FahRoom fah = (FahRoom) part;
			roomByUid.put(fah.getUid(), fah);
		}
		if (part instanceof FahDevice) {
			FahDevice fah = (FahDevice) part;
			deviceBySerialNumber.put(fah.getSerialNumber(), fah);
		}
		if (part instanceof FahChannel) {
			FahChannel fah = (FahChannel) part;
			String name = fah.getName();
			if (name != null) {
				channelByName.put(name, fah);
			}
		}
	}

	void addSysap(String name, String value) {
		sysap.put(name, value);
	}

}
