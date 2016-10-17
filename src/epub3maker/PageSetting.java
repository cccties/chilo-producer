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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class PageSetting {

	@SuppressWarnings("unused")
	private static Log log = LogFactory.getLog(PageSetting.class);

    public static final String KEY_PAGE_TYPE = "page-type";
    public static final String KEY_SECTION = "section";
    public static final String KEY_TOPIC = "topic";
    public static final String KEY_COMMUNITY = "community";
    public static final String KEY_OBJECT = "object";
    public static final String KEY_VIDEO_IMAGE = "video-image";
    public static final String KEY_TEXT = "text";
    public static final String KEY_JAVASCRIPT_FILE = "javascript-file";
    public static final String KEY_YOUTUBE_ID = "youtube-id";
    public static final String KEY_CC = "CC";

    public static final String KEY_ATTR_ATTRIBUTE = "attribute";
    public static final String KEY_ATTR_TYPE = "type";
    public static final String KEY_ATTR_VALUE = "value";
    public static final String KEY_ATTR_CLASS = "class";
    public static final String KEY_ATTR_OPTION = "option";

    public static final String VALUE_KEY_PAGE_TYPE_COVER = "cover";
    public static final String VALUE_KEY_PAGE_TYPE_DOCUMENT = "document";
    public static final String VALUE_KEY_PAGE_TYPE_TEST = "test";
    public static final String VALUE_KEY_PAGE_TYPE_INSIDE_COVER = "inside-cover";

    public static final String VALUE_ATTR_ATTRIBUTE_OBJECT_IMAGE = "image";
    public static final String VALUE_ATTR_ATTRIBUTE_OBJECT_VIDEO = "video";
    public static final String VALUE_ATTR_ATTRIBUTE_OBJECT_SCRIPT = "script";

    public static final String VALUE_ATTR_ATTRIBUTE_TEXT_STRING = "string";
    public static final String VALUE_ATTR_ATTRIBUTE_TEXT_FILE = "file";
    public static final String VALUE_ATTR_ATTRIBUTE_TEXT_PREFORMAT = "preformat";
    public static final String VALUE_ATTR_ATTRIBUTE_TEXT_SVG = "svg";

    public static final String VALUE_ATTR_TYPE_COMMON = "common";
    public static final String VALUE_ATTR_TYPE_VOLUME = "volume";

    public static final String VALUE_KEY_ITEM_PROPERTY_SVG = "svg";
    public static final String VALUE_KEY_ITEM_PROPERTY_MATHML = "mathml";
    public static final String VALUE_KEY_ITEM_PROPERTY_SCRIPTED = "scripted";
    public static final String VALUE_KEY_ITEM_PROPERTY_REMOTE_RESOURCES = "remote-resources";
    public static final String VALUE_KEY_ITEM_PROPERTY_SWITCH = "switch";
    public static final String VALUE_KEY_ITEM_PROPERTY_COVER_IMAGE = "cover-image";    

    public static final String KEY_ITEM_PROPERTY = "item-property";

    private int volume, page;
    private Map<String, List<Map<String, String>>> settings;
    private Path textPathForArchiveFile;
    private List<Path> textPaths;

    private List<Path> objectPaths;
    private List<Path> javascriptFilePaths;

    public PageSetting(int volume, int page, Map<String, List<Map<String, String>>> settings) {
        this.volume = volume;
        this.page = page;
        this.settings = settings;

        setTextPaths();
        setObjectPaths();
        setTextPathForArchiveFile();
        setJavascriptFilePaths();
    }

    public PageSetting(int volume, int page, String type, String section, String topic, String text) {
        this.volume = volume;
        this.page = page;
        
    	Map<String, List<Map<String, String>>> map0 = new HashMap<>();

    	Map<String, String> map = new HashMap<>();
    	map.put(PageSetting.KEY_ATTR_ATTRIBUTE, "string");
    	map.put(PageSetting.KEY_ATTR_TYPE, type);
    	map.put(PageSetting.KEY_ATTR_VALUE, text);
    	List<Map<String, String>> list = new ArrayList<>();
    	list.add(map);
    	map0.put(PageSetting.KEY_TEXT, list);
    	
    	map = new HashMap<>();
    	map.put(PageSetting.KEY_ATTR_ATTRIBUTE, "svg0");
    	map.put(PageSetting.KEY_ATTR_VALUE, section);
    	list = new ArrayList<>();
    	list.add(map);
    	map0.put(PageSetting.KEY_SECTION, list);
    	
    	map = new HashMap<>();
    	map.put(PageSetting.KEY_ATTR_ATTRIBUTE, "string");
    	map.put(PageSetting.KEY_ATTR_TYPE, type);
    	if (section.equalsIgnoreCase("copyright")) {
    		map.put(PageSetting.KEY_ATTR_VALUE, "");
    	} else {
    		map.put(PageSetting.KEY_ATTR_VALUE, VALUE_KEY_ITEM_PROPERTY_SVG);
    	}
    	list = new ArrayList<>();
    	list.add(map);
    	map0.put(PageSetting.KEY_ITEM_PROPERTY, list);

    	map = new HashMap<>();
    	map.put(PageSetting.KEY_ATTR_ATTRIBUTE, "string");
    	map.put(PageSetting.KEY_ATTR_VALUE, "additional");
    	list = new ArrayList<>();
    	list.add(map);
    	map0.put(PageSetting.KEY_PAGE_TYPE, list);

    	map = new HashMap<>();
    	map.put(PageSetting.KEY_ATTR_ATTRIBUTE, "svg2");
    	map.put(PageSetting.KEY_ATTR_VALUE, topic);
    	list = new ArrayList<>();
    	list.add(map);
    	map0.put(PageSetting.KEY_TOPIC, list);

    	this.settings = map0;    	

    	setTextPaths();
    	setTextPathForArchiveFile();
    }

    public int getPage() {
        return page;
    }
    
    public void setPage(int page) {
    	this.page = page;
    	setTextPathForArchiveFile();
    }

    public Map<String, List<Map<String, String>>> getSettings() {
        return settings;
    }

    public String getRelativePath(String target, int i) {
        String type = settings.get(target).get(i).get(PageSetting.KEY_ATTR_TYPE);
        StringBuilder sb = new StringBuilder(TYPE_PATH.get(type));
        if (type.equals(PageSetting.VALUE_ATTR_TYPE_VOLUME)) {
            sb.append(volume);
        }
        return sb.toString();
    }

    public Path getTextPathForArchiveFile() {
        return textPathForArchiveFile;
    }

    private void setTextPathForArchiveFile() {
    	String suffix = ".xhtml";
    	String type = getPageType();
    	if(type != null && type.equals(PageSetting.VALUE_KEY_PAGE_TYPE_TEST)){
    		suffix = "-test" + suffix;
    	}

    	StringBuilder textPath = new StringBuilder();
        textPath.append(getRelativePath(KEY_TEXT, 0)).append('/')
                .append("text").append('/').append(Book.VOLUME_PREFIX)
                .append(String.format("%03d", volume)).append('-')
                .append(String.format("%03d", page)).append(suffix);

        textPathForArchiveFile = Paths.get(textPath.toString());
    }

    public Path getObjectPath(int i) {
    	if (objectPaths == null) {
    		return null;
    	} else {
    		return objectPaths.get(i);
    	}
    }

    public int getObjectPathsSize() {
        return objectPaths.size();
    }

    private void setObjectPaths() {
        final String key = KEY_OBJECT;
        String val = settings.get(key).get(0).get(KEY_ATTR_VALUE);
    	if (val != null && (val.startsWith("http://") || val.startsWith("https://"))) {
    		return;
    	}
    	objectPaths = new ArrayList<>();
    	setPaths(key,
    			(i, attributes) -> objectPaths.add(Paths.get(getRelativePath(key, i),attributes.get(KEY_ATTR_ATTRIBUTE) + "s",attributes.get(KEY_ATTR_VALUE))),
    			(i,	attributes) -> objectPaths.add(null));
    }

    public Path getJavascriptFilePath(int i) {
        return javascriptFilePaths.get(i);
    }

    public int getJavascriptFilePathsSize() {
    	if (javascriptFilePaths == null) {
    		return 0;
    	} else {
    		return javascriptFilePaths.size();
    	}
    }

    private void setJavascriptFilePaths() {
        javascriptFilePaths = new ArrayList<>();
        final String key = KEY_JAVASCRIPT_FILE;
        setPaths(key,
        		(i, attributes) -> javascriptFilePaths.add(Paths.get(getRelativePath(key, i),"scripts",attributes.get(KEY_ATTR_VALUE))),
        		(i, attributes) -> javascriptFilePaths.add(null));
    }

    public Path getTextPath(int i) {
        return textPaths.get(i);
    }

    public int getTextPathsSize() {
        return textPaths.size();
    }

    private void setTextPaths() {
        textPaths = new ArrayList<>();
        final String key = KEY_TEXT;
        setPaths(key,
        		(i, attributes) -> textPaths.add(Paths.get(getRelativePath(key, i), "text", getSettings().get(key).get(i).get(KEY_ATTR_VALUE))),
        		(i, attributes) -> textPaths.add(null));
    }

    private void setPaths(String key,
            BiConsumer<Integer, Map<String, String>> addingProcess,
            BiConsumer<Integer, Map<String, String>> addingNullProcess) {
        for (int i = 0, size = settings.get(key).size(); i < size; ++i) {
            final Map<String, String> attributes = settings.get(key).get(i);
            final String value = attributes.get(KEY_ATTR_VALUE);
            if (value != null) {
                addingProcess.accept(i, attributes);
            } else {
                addingNullProcess.accept(i, attributes);
            }
        }
    }

    public String getSection()
    {
    	return settings.get(PageSetting.KEY_SECTION).get(0).get(KEY_ATTR_VALUE);
    }

    public String getTopic()
    {
    	return settings.get(PageSetting.KEY_TOPIC).get(0).get(KEY_ATTR_VALUE);
    }

    public String getPageType()
    {
    	return settings.get(PageSetting.KEY_PAGE_TYPE).get(0).get(KEY_ATTR_VALUE);
    }

    public String getObject(int i)
    {
    	return settings.get(PageSetting.KEY_OBJECT).get(i).get(KEY_ATTR_VALUE);
    }

    public String getText(int i)
    {
    	return settings.get(PageSetting.KEY_TEXT).get(i).get(KEY_ATTR_VALUE);
    }

    public String getItemProperty()
    {
    	return settings.get(PageSetting.KEY_ITEM_PROPERTY).get(0).get(PageSetting.KEY_ATTR_VALUE);
    }

    public String getCC()
    {
    	List<Map<String, String>> list = settings.get(PageSetting.KEY_CC);
    	if (list == null)
    		return null;
    	
    	String ccStr = list.get(0).get(KEY_ATTR_VALUE);
    	if (!Util.isValueValid(ccStr))
    		return null;
    	
    	return ccStr.toLowerCase().replace("cc ", "").trim();
    }

    public List<String> getJavaScriptFilePath() {
    	List<String> list = null;
    
    	final int size = getJavascriptFilePathsSize();
        for(int i = 0; i < size; ++i)
        {
            final Path scriptFilePath = getJavascriptFilePath(i);
            if(scriptFilePath == null || scriptFilePath.toString().endsWith("scripts"))
            {
                continue;
            }
            if (list == null) {
            	list = new ArrayList<String>();
            }
            Path textPath = getTextPathForArchiveFile().getParent();
            list.add(Util.path2str(textPath.relativize(scriptFilePath)));
        }
        return list;
    }
    
    public String getVideoImage() {
    	List<Map<String, String>> list = settings.get(PageSetting.KEY_VIDEO_IMAGE);
    	if (list == null)
    		return null;
    	return list.get(0).get(PageSetting.KEY_ATTR_VALUE);
    }
    
    public String getYoutubeId()
    {
    	List<Map<String, String>> list = settings.get(PageSetting.KEY_YOUTUBE_ID);
    	if (list == null)
    		return null;
    	
    	String str = list.get(0).get(KEY_ATTR_VALUE);
    	if (!Util.isValueValid(str))
    		return null;
    	
    	return str;
    }
    
    public String getAttribute(String key) {
    	return settings.get(key).get(0).get(PageSetting.KEY_ATTR_ATTRIBUTE);
    }

    public boolean isCommunity() {
        List<Map<String, String>> list = settings.get(PageSetting.KEY_COMMUNITY);
        if (list == null)
            return false;
        return "true".equalsIgnoreCase(list.get(0).get(PageSetting.KEY_ATTR_VALUE));
    }

    private final Map<String, String> TYPE_PATH = new HashMap<String, String>() {
        {
            put("common", "common");
            put("volume", "vol-");
        }
    };
}
