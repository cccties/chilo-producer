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
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class Series {

	private static Log log = LogFactory.getLog(Series.class);

	public static final String KEY_INPUT_PATH = "input-path";
    public static final String KEY_OUTPUT_PATH = "output-path";
    public static final String KEY_OUTPUT_NAME= "output-name";

    public static final String KEY_VERSION = "version";
    public static final String KEY_LANGUAGE = "language";
    public static final String KEY_AUTHOR = "author";
    public static final String KEY_V2_CREATOR = "creator";
    public static final String KEY_PUBLISHER = "publisher";
    public static final String KEY_EDITOR = "editor";
    public static final String KEY_PUBLISHED = "published";
    public static final String KEY_REVISED = "revised";
    public static final String KEY_RIGHTS = "rights";

    public static final String KEY_SERIES_NAME = "series-name";
    public static final String KEY_SERIES_INTRODUCTION = "series-introduction";
    public static final String KEY_V2_COVER = "cover";
    public static final String KEY_V2_COVER2 = "inside-cover";

    public static final String KEY_BOOKLIST_VOL = "vol";
    public static final String KEY_BOOKLIST_BOOK_TITLE = "book-title";
    public static final String KEY_BOOKLIST_BOOK_SUMMARY = "book-summary";
    public static final String KEY_BOOKLIST_BOOK_COVER = "book-cover";
    public static final String KEY_BOOKLIST_INSIDE_COVER = "inside-cover";
    public static final String KEY_BOOKLIST_ID = "identifier"; // contentopf.xhtml
    public static final String KEY_BOOKLIST_COMMUNITY_URL = "community-url";

    private Map<String, String> meta;
    private Map<Integer, Book> books;
    
    public Series(Map<String, String> meta, Map<Integer, Book> books) {
        this.meta = meta;
        this.books = books;
    }

    public String getInputPath() {
        return meta.get(KEY_INPUT_PATH);
    }

    public String getOutputPath() {
        return meta.get(KEY_OUTPUT_PATH);
    }

    public String getOutputName() {
        return meta.get(KEY_OUTPUT_NAME);
    }

    public String getMeta(String key) {
        return meta.get(key);
    }

    public Map<Integer, Book> getBooks() {
        return books;
    }

    public Path getEpubFilePath(Book book){
        String outputName = getOutputName();
        return Paths.get(
        		getOutputPath(),
                (outputName != null ? outputName : "") + 
                // getMeta(KEY_SERIES_NAME) + "-vol-"
                //        + book.getVolume() + ".epub");
                String.format("%02d", book.getVolume()) + ".epub");
    }

	/**
	 */
	protected void showMeta() {
	    log.trace("Meta");
	    for (String key: meta.keySet()) {
	        log.trace(key + ": " + meta.get(key));
	    }
	}
}
