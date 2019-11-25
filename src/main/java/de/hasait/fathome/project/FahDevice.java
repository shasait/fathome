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
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.TreeMap;
import java.util.concurrent.ConcurrentHashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 */
public class FahDevice extends AbstractFahPart {

	private static final Logger log = LoggerFactory.getLogger(FahDevice.class);

	private final String serialNumber;

	private final Map<String, AbstractFahChannel> channelsById = new TreeMap<>();
	private final Map<String, String> parameterValues = new ConcurrentHashMap<>();
	private FahString type;
	private String deviceId;
	private FahFunction function;
	private String name;
	private FahRoom room;

	FahDevice(String serialNumber) {
		super();

		this.serialNumber = serialNumber;
	}

	public Collection<AbstractFahChannel> getAllChannels() {
		return Collections.unmodifiableCollection(channelsById.values());
	}

	public AbstractFahChannel getChannel(String channelId) {
		return channelsById.get(channelId);
	}

	public String getDeviceId() {
		return deviceId;
	}

	public FahFunction getFunction() {
		return function;
	}

	public String getName() {
		return name;
	}

	public Set<String> getParameterIds() {
		return Collections.unmodifiableSet(parameterValues.keySet());
	}

	public String getParameterValue(String parameterId) {
		return parameterValues.get(parameterId);
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

	void addChannel(AbstractFahChannel channel) {
		channelsById.put(channel.getId(), channel);
	}

	void setDeviceId(String deviceId) {
		this.deviceId = deviceId;
	}

	void setFunction(FahFunction function) {
		this.function = function;
	}

	void setName(String name) {
		this.name = name;
	}

	final void setParameter(String parameterId, String value) {
		String oldValue = value != null ? parameterValues.put(parameterId, value) : parameterValues.remove(parameterId);
		if (!Objects.equals(oldValue, value)) {
			log.info(name + "@" + parameterId + " changed: " + oldValue + " -> " + value);
		}
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
