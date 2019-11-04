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
public abstract class AbstractFahChannel extends AbstractFahPart {

	private static final Logger log = LoggerFactory.getLogger(AbstractFahChannel.class);

	private final FahDevice device;
	private final String id;
	private final FahFunction function;
	private final Map<String, String> dataPointValues = new ConcurrentHashMap<>();
	private String name;

	protected AbstractFahChannel(FahDevice device, String id, FahFunction function) {
		super();

		this.device = device;
		this.id = id;
		this.function = function;

		device.addChannel(this);
	}

	public final FahDevice getDevice() {
		return device;
	}

	public final String getId() {
		return id;
	}

	public final String getName() {
		return name;
	}

	final void setDataPoint(String dataPointId, String value) {
		String oldValue = value != null ? dataPointValues.put(dataPointId, value) : dataPointValues.remove(dataPointId);
		if (!Objects.equals(oldValue, value)) {
			log.info(name + "@" + dataPointId + " changed: " + oldValue + " -> " + value);
		}
	}

	final void setName(String name) {
		this.name = name;
	}

	protected Set<String> getDataPointIds() {
		return Collections.unmodifiableSet(dataPointValues.keySet());
	}

	protected String getDataPointValue(String dataPointId) {
		return dataPointValues.get(dataPointId);
	}

	protected FahFunction getFunction() {
		return function;
	}

	protected void rpcSetDataPoint(String dataPointId, String value) {
		Value dpPath = Value.of(device.getSerialNumber() + "/" + id + "/" + dataPointId);
		Value dpValue = Value.of(value);

		Value result = getProject().getCommunication().rpcCall("RemoteInterface.setDatapoint", dpPath, dpValue);
		log.info("result: " + result);
		setDataPoint(dataPointId, value);
	}

	private void assertFunction(boolean assertion, String type) {
		if (!assertion) {
			throw new RuntimeException("Not a " + type + ": " + name + " (" + function.getFidName() + "/" + function.getId() + ")");
		}
	}

}
