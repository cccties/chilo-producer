/**
 * 
 * :-::-:+:-:+:-:+:-:+:-:+:-:+:-:+:-:+:-:+:-:+:-:+:-:+:-+:-+:-+:-+:-++:-:+:-:+:-:+:-:
 * 
 * This file is part of CHiLOⓇ  - http://www.cccties.org/en/activities/chilo/
 *   CHiLOⓇ is a next-generation learning system utilizing ebooks,  aiming 
 *   at dissemination of open education.
 *                          Copyright 2015 NPO CCC-TIES
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 *
 * :-::-:+:-:+:-:+:-:+:-:+:-:+:-:+:-:+:-:+:-:+:-:+:-:+:-+:-+:-+:-+:-++:-:+:-:+:-:+:-:
 * 
 */
package epub3maker;

import java.util.HashMap;
import java.util.Map;

import org.apache.velocity.VelocityContext;

public class Content {
	private Map<String, Object> content;

	public Content()
	{
		content = new HashMap<String, Object>();
	}
	
	public void put(String key, Object value)
	{
		content.put(key, value);
	}
	
	public VelocityContext getVelocityContext() {
    	VelocityContext context = new VelocityContext();
		for (Map.Entry<String, Object> e : content.entrySet()) {
			if (e.getValue() != null) {
				context.put(e.getKey(), e.getValue());
			}
		}
    	return context;
	}

}
