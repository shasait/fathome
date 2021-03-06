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

import de.hasait.fathome.project.AbstractFahChannel;
import de.hasait.fathome.project.FahDevice;
import de.hasait.fathome.project.FahFunction;

/**
 *
 */
public class FahScene extends AbstractFahChannel {

	static final String DEFAULT_ACTIVATE_DP = "odp0000";

	private final String activateDatapoint;

	public FahScene(FahDevice device, String id, FahFunction function) {
		this(device, id, function, DEFAULT_ACTIVATE_DP);
	}

	public FahScene(FahDevice device, String id, FahFunction function, String activateDatapoint) {
		super(device, id, function);

		this.activateDatapoint = activateDatapoint;
	}

	public void activate() {
		rpcSetDataPoint(activateDatapoint, "1");
	}

}
