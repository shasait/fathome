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

package de.hasait.fathome.things;

import java.util.Set;

import de.hasait.fathome.project.AbstractFahChannel;
import de.hasait.fathome.project.FahDevice;
import de.hasait.fathome.project.FahFunction;

/**
 *
 */
public class FahUnknown extends AbstractFahChannel {

	public FahUnknown(FahDevice device, String id, FahFunction function) {
		super(device, id, function);
	}

	@Override
	public Set<String> getDataPointIds() {
		return super.getDataPointIds();
	}

	@Override
	public String getDataPointValue(String dataPointId) {
		return super.getDataPointValue(dataPointId);
	}

	@Override
	public FahFunction getFunction() {
		return super.getFunction();
	}

	@Override
	public void rpcSetDataPoint(String dataPointId, String value) {
		super.rpcSetDataPoint(dataPointId, value);
	}

}
