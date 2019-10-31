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

/**
 *
 */
public class FahDimmer extends FahSwitch {

	static final String DEFAULT_DIM_DP = "idp0002";

	private final String dimDatapoint;

	FahDimmer(FahChannel channel) {
		this(channel, DEFAULT_SWITCH_DP, DEFAULT_DIM_DP);
	}

	FahDimmer(FahChannel channel, String switchDatapoint, String dimDatapoint) {
		super(channel, switchDatapoint);

		this.dimDatapoint = dimDatapoint;
	}

	/**
	 * @return 0..100 (0 = off; 100 = on).
	 */
	public int getDimLevel() {
		String state = channel.getDataPointValue(dimDatapoint);
		return state == null ? 0 : Integer.parseInt(state);
	}

	/**
	 * @param level 0..100 (0 = off; 100 = on).
	 */
	public void setDimLevel(int level) {
		if (level < 0 || level > 100) {
			throw new IllegalArgumentException("level not in range [0, 100]: " + level);
		}

		String levelAsString = Integer.toString(level);
		channel.rpcSetDataPoint(dimDatapoint, levelAsString);
	}

}
