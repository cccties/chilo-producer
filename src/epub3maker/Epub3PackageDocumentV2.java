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

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

public class Epub3PackageDocumentV2 extends Epub3PackageDocument {

	public Epub3PackageDocumentV2(Path opfPath) {
		super(opfPath);
	}
	
    public void setManifest(Map<Path, String> paths, Map<String, String> mediaTypes, Content content) throws Epub3MakerException {
    	List<Map<String, String>> list = new ArrayList<Map<String, String>>();

        for (Iterator<Entry<Path, String>> ite = paths.entrySet().iterator(); ite.hasNext();) {
            Entry<Path, String> e = ite.next();
            Path path = e.getKey();
            Map<String, String> map = new HashMap<String, String>();
            
            Path partPath = Epub3Maker.subtractBasePath(path);            
            map.put(TAG_ITEM.ATTR_HREF.getLabel(), partPath.toString().replaceAll("\\\\", "/"));
            
            String tempPathStr = partPath.toString().replaceAll("\\\\", "/");
            map.put(TAG_ITEM.ATTR_ID.getLabel(), tempPathStr.replaceAll("/", "-"));

            map.put(TAG_ITEM.ATTR_MEDIA_TYPE.getLabel(), getMediaType(path.getFileName().toString(), mediaTypes));

            if (Util.isValueValid(e.getValue())) {
            	map.put(TAG_ITEM.ATTR_PROPERTIES.getLabel(), e.getValue());
            }
            list.add(map);
        }
        Map<String, String> map = new HashMap<String, String>();
        final String fileName = "nav.xhtml";
        map.put(TAG_ITEM.ATTR_HREF.getLabel(), fileName);
        map.put(TAG_ITEM.ATTR_ID.getLabel(), fileName);
        map.put(TAG_ITEM.ATTR_MEDIA_TYPE.getLabel(), getMediaType(fileName, mediaTypes));
        map.put(TAG_ITEM.ATTR_PROPERTIES.getLabel(), "nav");
        list.add(map);
        content.put("manifest-items", list);

        Map<String, String> map2 = new HashMap<String, String>();
        final String fileName2 = "cardview.xhtml";
        map2.put(TAG_ITEM.ATTR_HREF.getLabel(), fileName2);
        map2.put(TAG_ITEM.ATTR_ID.getLabel(), fileName2);
        map2.put(TAG_ITEM.ATTR_MEDIA_TYPE.getLabel(), getMediaType(fileName2, mediaTypes));
        map2.put(TAG_ITEM.ATTR_PROPERTIES.getLabel(), "svg");
        list.add(map2);
        content.put("manifest-items", list);
    }

    public void setSpine(List<String> paths, Content content) {
        
        /*
         * debug
         */
        if (LogLevel.LOG_DEBUG.compareTo(LogLevel.valueOf(Config.getCurrentLogLevel())) <= 0) {
            Util.infoPrintln(LogLevel.LOG_DEBUG, "Epub3PackageDocument#setSpine: ");
            for (String p : paths) {
                Util.infoPrintln(LogLevel.LOG_DEBUG, "  " + p);
            }
        }

        List<String> list = new ArrayList<String>();
        for (String path : paths) {
        	list.add(path.replace("/", "-"));
        }

        content.put("spine-list", list);
    }


}
