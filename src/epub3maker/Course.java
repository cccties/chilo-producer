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

import java.util.List;
import java.util.Map;

public class Course {
    public static final String KEY_INPUT_PATH = "input-path";
    public static final String KEY_OUTPUT_PATH = "output-path";
    public static final String KEY_VERSION = "version";
    public static final String KEY_COURSE_ID = "course-id";
    public static final String KEY_COURSE_NAME = "course-name";
    public static final String KEY_CREATOR = "creator";
    public static final String KEY_PUBLISHER = "publisher";
    public static final String KEY_RIGHTS = "rights";
    public static final String KEY_LANGUAGE = "language";
    
    // ----- for ver. 2 -----
    public static final String KEY_COURSE_ID2 = "identifier";
    public static final String KEY_COURSE_NAME2 = "series-name";
    
    public static final String KEY_OUTPUT_NAME= "output-name";
    public static final String KEY_EDITOR = "editor";
    public static final String KEY_PUBLISHED = "published";
    public static final String KEY_REVISED = "revised";
    public static final String KEY_SERIES_INTRODUCTION = "series-introduction";
    public static final String KEY_SERIES_URL = "series-url";
    public static final String KEY_COVER = "cover";
    public static final String KEY_DEPLOY_URL = "deploy-url";
    public static final String KEY_FB_ADMINS = "fb:admins";
    public static final String KEY_OG_SITE_NAME = "og:site_name";
    public static final String KEY_GOOGLE_ANALYTICS_ID = "google-analytics-id";
    public static final String KEY_FB_APP_ID = "fb-app-id";

    public static final String KEY_BOOKLIST_ID = "identifier"; // contentpf.xhtml
    public static final String KEY_BOOKLIST_VOL = "vol";
    public static final String KEY_BOOKLIST_SERIES_TITLE = "series-title";
    public static final String KEY_BOOKLIST_BOOK_SUMMARY = "book-summary";
//    public static final String KEY_BOOKLIST_BREADCRUMBS = "breadcrumbs";
//    public static final String KEY_BOOKLIST_BREADCRUMBS_URL = "breadcrumbs-url";
    public static final String KEY_BOOKLIST_COVER = "cover";
  public static final String KEY_BOOKLIST_EPUB_DOWNLOAD_URL = "epub-download-url";
    public static final String KEY_BOOKLIST_COMMUNITY_URL = "community-url";


    public Course(Map<String, String> meta, Map<Integer, Volume> volumes,
            List<String> pageSettingAttributeNames,
            List<String> pageSettingKeyNames) {
        this.meta = meta;
        this.volumes = volumes;
        this.pageSettingAttributeNames = pageSettingAttributeNames;
        this.pageSettingKeyNames = pageSettingKeyNames;
    }

    public Course(Map<String, String> meta, Map<Integer, Volume> volumes, List<Map<String,String>> bookList,
            List<String> pageSettingAttributeNames,
            List<String> pageSettingKeyNames) {
        this.meta = meta;
        this.volumes = volumes;
        this.bookList = bookList;
        this.pageSettingAttributeNames = pageSettingAttributeNames;
        this.pageSettingKeyNames = pageSettingKeyNames;
    }

    public Map<String, String> getMeta() {
        return this.meta;
    }

    public String getMeta(String key) {
        return this.meta.get(key);
    }

    public List<String> getAttributeNames() {
        return this.pageSettingAttributeNames;
    }

    public List<String> getKeyNames() {
        return this.pageSettingKeyNames;
    }

    public Map<Integer, Volume> getVolumes() {
        return this.volumes;
    }

    public List<Map<String, String>> getBookList() {
    	return bookList;
    }

    public static final String HEADER_KEY = "key";
    private Map<String, String> meta;
    private Map<Integer, Volume> volumes;
    private List<Map<String, String>> bookList;
    private List<String> pageSettingAttributeNames;
    private List<String> pageSettingKeyNames;
    
    private String getBookListElement(String vol, String key) 
    {
    	for (Map<String, String> e : bookList) {
    		if (e.get(KEY_BOOKLIST_VOL).equals(vol)) {
    			return e.get(key);
    		}
    	}
    	return null;
    }
    
    public String bookSeriesTitle(String vol) {
    	return getBookListElement(vol, KEY_BOOKLIST_SERIES_TITLE);
    }

    public String bookIdentifier(String vol) {
        return getBookListElement(vol, KEY_BOOKLIST_ID);
    }
    
    public String bookBookSummary(String vol) {
    	return getBookListElement(vol, KEY_BOOKLIST_BOOK_SUMMARY);
    }
    
    public String bookCover(String vol) {
        return getBookListElement(vol, KEY_BOOKLIST_COVER);
    }

    public String bookEpubDownloadUrl(String vol) {
        return getBookListElement(vol, KEY_BOOKLIST_EPUB_DOWNLOAD_URL);
    }

    public String bookCommunityUrl(String vol) {
    	return getBookListElement(vol, KEY_BOOKLIST_COMMUNITY_URL);
    }
    
}
