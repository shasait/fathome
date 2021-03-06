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
public class FahBlind extends AbstractFahChannel {

	static final String DEFAULT_BLIND_DP = "idp0000";
	static final String DEFAULT_BLIND_STOP_DP = "idp0001";
	static final String DEFAULT_BLIND_MOVE_STATE_DP = "odp0000";
	static final String DEFAULT_BLIND_POS_STATE_DP = "odp0001";

	private final String blindDatapoint;
	private final String blindStopDatapoint;
	private final String blindMoveStateDp;
	private final String blindPosStateDp;

	public FahBlind(FahDevice device, String id, FahFunction function) {
		this(device, id, function, DEFAULT_BLIND_DP, DEFAULT_BLIND_STOP_DP, DEFAULT_BLIND_MOVE_STATE_DP, DEFAULT_BLIND_POS_STATE_DP);
	}

	public FahBlind(FahDevice device, String id, FahFunction function, String blindDatapoint, String blindStopDatapoint,
			String blindMoveStateDp, String blindPosStateDp) {
		super(device, id, function);

		this.blindDatapoint = blindDatapoint;
		this.blindStopDatapoint = blindStopDatapoint;
		this.blindMoveStateDp = blindMoveStateDp;
		this.blindPosStateDp = blindPosStateDp;
	}

	/**
	 * @return 0..100 (0 = up; 100 = down).
	 */
	public int getPosition() {
		String state = getDataPointValue(blindPosStateDp);
		return state == null ? 0 : Integer.parseInt(state);
	}

	public boolean isMovingDown() {
		String state = getDataPointValue(blindMoveStateDp);
		return "3".equals(state);
	}

	public boolean isMovingUp() {
		String state = getDataPointValue(blindMoveStateDp);
		return "2".equals(state);
	}

	public void moveDown() {
		rpcSetDataPoint(blindDatapoint, "1");
	}

	public void moveUp() {
		rpcSetDataPoint(blindDatapoint, "0");
	}

	public void stop() {
		rpcSetDataPoint(blindStopDatapoint, "1");
	}

}
