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
import java.util.List;

public class Volume {
    public Volume(int volume) {
        this(volume, new ArrayList<PageSetting>());
    }

    public Volume(int volume, List<PageSetting> settings) {
        this.volume = volume;
        pageSettings = settings;
    }

    public List<PageSetting> getPageSettings() {
        return pageSettings;
    }

    public int getVolume() {
        return volume;
    }

    public void setVolume(int volume) {
        this.volume = volume;
    }
    
    public String getVolumeStr() {
    	return KEY_VOLUME_PREFIX + volume;
    }
    
    public PageSetting getPage(int page) {
    	for (PageSetting p : pageSettings) {
    		if (p.getPage() == page)
    			return p;
    	}
    	return null;
    }
    
    public int getMaxPage() { 
    	int maxPage = 0;
    	for (PageSetting pageSetting : pageSettings) {
    		if (pageSetting.getPage() > maxPage)
    			maxPage = pageSetting.getPage();
    	}
    	return maxPage;
	}

    private List<PageSetting> pageSettings;
    int volume;
    public static final String KEY_VOLUME_PREFIX = "vol-";

    public String getCoverImage() {
    	String ret = null;
    	for (PageSetting p : pageSettings) {
    		if (p.getPageType().equals(PageSetting.VALUE_KEY_PAGE_TYPE_COVER)) {
    			Path tmp = p.getCoverImagePath();
    			if (tmp != null) {
    				ret = tmp.toString();
    				break;
    			}
    		}
    	}
    	return ret;
    }
}
