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
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class Book {

    public static final String VOLUME_PREFIX = "vol-";

    private int volume;
    private List<PageSetting> pageSettings;
    private Map<String, String> bookInfo;

    public Book(int volume, Map<String, String> bookInfo) {
        this.volume = volume;
        pageSettings = new ArrayList<PageSetting>();
        this.bookInfo = bookInfo;
    }

    public int getVolume() {
        return volume;
    }

    public String getVolumeStr() {
    	return VOLUME_PREFIX + volume;
    }
    
    public List<PageSetting> getPageSettings() {
        return pageSettings;
    }

    public void addPageSetting(PageSetting p) {
    	pageSettings.add(p);
    }

    public PageSetting getPageSetting(int num) {
    	return pageSettings.get(num);
    }

    public PageSetting getPage(int page) {
    	for (PageSetting p: pageSettings) {
    		if (p.getPage() == page) {
    			return p;
    		}
    	}
    	return null;
    }
    
    public int getMaxPage() { 
    	int max = 0;
    	for (PageSetting p: pageSettings) {
    		int tmp = p.getPage();
    		if (tmp > max) {
    			max = tmp;
    		}
    	}
    	return max;
	}

    public Path getCoverImagePath() {
    	Path ret = null;
    	String cover = bookInfo.get(Series.KEY_BOOKLIST_BOOK_COVER);
    	if (cover != null) {
    		ret = Paths.get(getVolumeStr(), "images", cover);
    	}
        return ret;
    }

    public String get(String key){
    	return bookInfo.get(key);
    }
}
