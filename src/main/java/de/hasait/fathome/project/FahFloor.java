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
import java.util.HashSet;
import java.util.Set;

/**
 *
 */
public class FahFloor extends AbstractFahPart {

	private final int id;

	private final Set<FahRoom> rooms = new HashSet<>();

	private String name;

	private int level;

	FahFloor(int id) {
		super();

		this.id = id;
	}

	public int getLevel() {
		return level;
	}

	public String getName() {
		return name;
	}

	public Set<FahRoom> getRooms() {
		return Collections.unmodifiableSet(rooms);
	}

	public int getId() {
		return id;
	}

	void addRoom(FahRoom room) {
		rooms.add(room);
	}

	void setLevel(int level) {
		this.level = level;
	}

	void setName(String name) {
		this.name = name;
	}
}
