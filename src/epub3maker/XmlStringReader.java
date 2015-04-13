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

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;


public class XmlStringReader {
	String fileName;
	Element rootElement;
	
	public XmlStringReader(String fileName)
	{
		this.fileName = fileName;
	}
	
	public Element read() throws SAXException, IOException, ParserConfigurationException
	{
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		DocumentBuilder documentBuilder = factory.newDocumentBuilder();
		Document document = documentBuilder.parse(fileName);
	
		rootElement = document.getDocumentElement();
		return rootElement;
	}
	
	public String get(List<String> keys) {
		return getInternal(rootElement, keys);
	}
	
	public String get(String keyStr) {
		String [] strs = keyStr.split("/");
		List<String> keys = new ArrayList<>();
		for (String str : strs) {
			keys.add(str);
		}
		return get(keys);
	}
	
	private String getInternal(Node root, List<String> keys) {
		if (root.getNodeType() != Node.ELEMENT_NODE) {
			return null;
		}
		
		for (Node ch = root.getFirstChild(); ch != null; ch = ch.getNextSibling()) {
			if (keys.get(0).equals(ch.getNodeName())) {
				keys.remove(0);
				if (keys.isEmpty()) {
					return ch.getFirstChild().getTextContent();
				} else {
					return getInternal(ch, keys);
				}
			}
		}
		return null;
	}			

}
