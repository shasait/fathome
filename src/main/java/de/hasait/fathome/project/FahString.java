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

/**
 *
 */
public class FahString extends AbstractFahPart {

	private final int id;
	private String value;

	FahString(int id) {
		super();

		this.id = id;
	}

	public int getId() {
		return id;
	}

	public String getValue() {
		return value;
	}

	void setValue(String value) {
		this.value = value;
	}

}
