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

package de.hasait.fathome.xml.update;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;

/**
 *
 */
@XmlRootElement(name = "project")
public class UxProject {

	@XmlAttribute
	public String mrhaVersion;
	@XmlAttribute
	public String mrhaBuild;
	@XmlAttribute
	public String type;

	@XmlElementWrapper(name = "sysap")
	@XmlElement(name = "value")
	public List<UxKeyValue> sysapValues = new ArrayList<>();

	@XmlElementWrapper(name = "devices")
	@XmlElement(name = "device")
	public List<UxDevice> devices = new ArrayList<>();

}
