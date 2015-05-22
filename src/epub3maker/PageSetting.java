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

public class PageSetting {

    public PageSetting(int volume, int page,
            Map<String, List<Map<String, String>>> settings) {
        this.volume = volume;
        this.page = page;
        this.settings = settings;

        this.setTextPaths();
        this.setObjectPaths();
        this.setTextPathForArchiveFile();
        this.setCoverImagePath();
        this.setCoverPagePath();
        this.setCommunityButtonImagePath();
        this.setJavascriptFilePaths();
    }

    public PageSetting(int volume, int page, String type, String subject, String subsubject, String text) {
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
    	map.put(PageSetting.KEY_ATTR_VALUE, subject);
    	list = new ArrayList<>();
    	list.add(map);
    	map0.put(PageSetting.KEY_SUBJECT, list);
    	
    	map = new HashMap<>();
    	map.put(PageSetting.KEY_ATTR_ATTRIBUTE, "string");
    	map.put(PageSetting.KEY_ATTR_TYPE, type);
    	if (subject.equalsIgnoreCase("copyright")) {
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
    	map.put(PageSetting.KEY_ATTR_VALUE, subsubject);
    	list = new ArrayList<>();
    	list.add(map);
    	map0.put(PageSetting.KEY_SUBSUBJECT, list);

    	this.settings = map0;    	

    	setTextPaths();
    	setTextPathForArchiveFile();
    }

    public int getVolume() {
        return volume;
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
        String type = settings.get(target).get(i)
                .get(PageSetting.KEY_ATTR_TYPE);
        StringBuilder sb = new StringBuilder(TYPE_PATH.get(type));
        if (type.equals(PageSetting.VALUE_ATTR_TYPE_VOLUME)) {
            sb.append(volume);
        }
        return sb.toString();
    }

    public Path getTextPathForArchiveFile() {
        return this.textPathForArchiveFile;
    }

    private void setTextPathForArchiveFile() {
    	String suffix = ".xhtml";
    	if (Util.isPublishHtml()) {
    		suffix = ".html";
    	}

        StringBuilder textPath = new StringBuilder();
        textPath.append(getRelativePath(KEY_TEXT, 0)).append('/')
                .append("text").append('/').append(Volume.KEY_VOLUME_PREFIX)
                .append(String.format("%03d", volume)).append('-')
                .append(String.format("%03d", page)).append(suffix);

        this.textPathForArchiveFile = Paths.get(textPath.toString());
    }

    public Path getObjectPath(int i) {
    	if (this.objectPaths == null) {
    		return null;
    	} else {
    		return this.objectPaths.get(i);
    	}
    }

    public int getObjectPathsSize() {
        return this.objectPaths.size();
    }

    private void setObjectPaths() {
        final String key = KEY_OBJECT;
        String val = settings.get(key).get(0).get(KEY_ATTR_VALUE);
    	if (val != null && val.startsWith("http://")) {
    		return;
    	}
    	this.objectPaths = new ArrayList<>();
    	this.setPaths(key, (i, attributes) -> this.objectPaths.add(Paths.get(
    			this.getRelativePath(key, i),
    			attributes.get(KEY_ATTR_ATTRIBUTE) + "s",
    			attributes.get(KEY_ATTR_VALUE))), (i,
    					attributes) -> this.objectPaths.add(null));
    }

    public Path getJavascriptFilePath(int i) {
        return this.javascriptFilePaths.get(i);
    }

    public int getJavascriptFilePathsSize() {
    	if (javascriptFilePaths == null) {
    		return 0;
    	} else {
    		return this.javascriptFilePaths.size();
    	}
    }

    private void setJavascriptFilePaths() {
        this.javascriptFilePaths = new ArrayList<>();
        final String key = KEY_JAVASCRIPT_FILE;
        this.setPaths(key, (i, attributes) -> this.javascriptFilePaths.add(Paths.get(
                this.getRelativePath(key, i),
                "scripts",
                attributes.get(KEY_ATTR_VALUE))), (i,
                attributes) -> this.javascriptFilePaths.add(null));
    }

    public Path getTextPath(int i) {
        return this.textPaths.get(i);
    }

    public int getTextPathsSize() {
        return this.textPaths.size();
    }

    private void setTextPaths() {
        this.textPaths = new ArrayList<>();
        final String key = KEY_TEXT;
        setPaths(key, (i, attributes) -> this.textPaths.add(Paths.get(
                this.getRelativePath(key, i), "text",
                this.getSettings().get(key).get(i).get(KEY_ATTR_VALUE))), (i,
                attributes) -> this.textPaths.add(null));
    }

    private void setPaths(String key,
            BiConsumer<Integer, Map<String, String>> addingProcess,
            BiConsumer<Integer, Map<String, String>> addingNullProcess) {
        for (int i = 0, size = this.settings.get(key).size(); i < size; ++i) {
            final Map<String, String> attributes = settings.get(key).get(i);
            final String value = attributes.get(KEY_ATTR_VALUE);
            if (value != null) {
                addingProcess.accept(i, attributes);
            } else {
                addingNullProcess.accept(i, attributes);
            }
        }
    }

    public Path getCoverImagePath() {
        return this.coverImagePath;
    }

    private void setCoverImagePath() {
        final String key = KEY_COVER;
        this.coverImagePath = this.getCoverXXXPath(key, "images");
    }

    public Path getCoverPagePath() {
        return this.coverPagePath;
    }

    private void setCoverPagePath() {
        final String key = KEY_COVER_PAGE;
        this.coverPagePath = this.getCoverXXXPath(key, "text");
    }

    private Path getCoverXXXPath(String key, String pathPart)
    {
    	final String fileName = this.getSettings().get(key).get(0)
                .get(KEY_ATTR_VALUE);
        if (fileName == null || fileName.equals("")) {
            return null;
        }

        return Paths.get(this.getRelativePath(key, 0), pathPart, fileName);
    }


    public String getChapter()
    {
    	return this.settings.get(PageSetting.KEY_SUBJECT).get(0).get(KEY_ATTR_VALUE);
    }

    public String getChapterClass()
    {
    	return this.settings.get(PageSetting.KEY_SUBJECT).get(0).get(KEY_ATTR_CLASS);
    }

    public String getSection()
    {
    	return this.settings.get(PageSetting.KEY_SUBSUBJECT).get(0).get(KEY_ATTR_VALUE);
    }

    public String getSectionClass()
    {
    	return this.settings.get(PageSetting.KEY_SUBSUBJECT).get(0).get(KEY_ATTR_CLASS);
    }

    public String getPageType()
    {
    	return this.settings.get(PageSetting.KEY_PAGE_TYPE).get(0).get(KEY_ATTR_VALUE);
    }

    public String getObject(int i)
    {
    	return this.settings.get(PageSetting.KEY_OBJECT).get(i).get(KEY_ATTR_VALUE);
    }

    public Map<String, String> getObjectData(int i)
    {
    	return this.settings.get(PageSetting.KEY_OBJECT).get(i);
    }

    public String getObjectClass(int i)
    {
    	return this.settings.get(PageSetting.KEY_OBJECT).get(i).get(KEY_ATTR_CLASS);
    }

    public String getText(int i)
    {
    	return this.settings.get(PageSetting.KEY_TEXT).get(i).get(KEY_ATTR_VALUE);
    }

    public Map<String, String> getTextData(int i)
    {
    	return this.settings.get(PageSetting.KEY_TEXT).get(i);
    }

    public String getTextClass(int i)
    {
    	return this.settings.get(PageSetting.KEY_TEXT).get(i).get(KEY_ATTR_CLASS);
    }

    public String getPublished()
    {
    	return this.settings.get(PageSetting.KEY_PUBLISHED).get(0).get(KEY_ATTR_VALUE);
    }

    public String getCommunityButton()
    {
    	return this.settings.get(PageSetting.KEY_COMMUNITY_BUTTON).get(0).get(KEY_ATTR_VALUE);
    }

    public String getCommunityButtonClass()
    {
    	return this.settings.get(PageSetting.KEY_COMMUNITY_BUTTON).get(0).get(KEY_ATTR_CLASS);
    }

    public Path getCommunityButtonImagePath()
    {
    	return this.communityButtonImagePath;
    }

    private void setCommunityButtonImagePath()
    {
    	final String key = KEY_COMMUNITY_BUTTON_IMAGE;
        this.communityButtonImagePath = this.getCoverXXXPath(key, "images");
    }

    public String getCommunityButtonImage()
    {
    	return this.settings.get(PageSetting.KEY_COMMUNITY_BUTTON_IMAGE).get(0).get(KEY_ATTR_VALUE);
    }

    public String getCommunityButtonImageClass()
    {
    	return this.settings.get(PageSetting.KEY_COMMUNITY_BUTTON_IMAGE).get(0).get(KEY_ATTR_CLASS);
    }

    public boolean getShowToc()
    {
        boolean ret = false;
        Map<String, String> setting = this.settings.get(PageSetting.KEY_SHOW_TOC).get(0);
        if (setting != null) {
            String attr = setting.get(PageSetting.KEY_ATTR_VALUE);
            if (attr != null) {
                ret = attr.toLowerCase().equals("true");
            } else {
                Util.infoPrintln(LogLevel.LOG_DEBUG, "SHOW_TOC: NO ATTRIBUTE");
            }
        } else {
            Util.infoPrintln(LogLevel.LOG_DEBUG, "SHOW_TOC: NO SETTING");
        }
//    	return this.settings.get(PageSetting.KEY_SHOW_TOC).get(0).get(PageSetting.KEY_ATTR_VALUE).toLowerCase().equals("true");
        Util.infoPrintln(LogLevel.LOG_DEBUG, "SHOW_TOC: " + ret);
        return ret;
    }

    public String getItemProperty()
    {
    	return this.settings.get(PageSetting.KEY_ITEM_PROPERTY).get(0).get(PageSetting.KEY_ATTR_VALUE);
    }

    public String getIdentifier()
    {
    	return this.settings.get(PageSetting.KEY_IDENTIFIER).get(0).get(PageSetting.KEY_ATTR_VALUE);
    }
    
    public Path getCCPath()
    {
    	List<Map<String, String>> list = settings.get(PageSetting.KEY_CC);
    	if (list == null)
    		return null;
    	
    	String ccStr = list.get(0).get(KEY_ATTR_VALUE);
    	if (!Util.isValueValid(ccStr))
    		return null;
    	
    	ccStr = "cc-" + ccStr.toLowerCase().replace("cc ", "") + ".png";
    	return Paths.get("common", "images", ccStr);    	
    }
    
    public String getCC()
    {
    	String ccStr = settings.get(PageSetting.KEY_CC).get(0).get(KEY_ATTR_VALUE);
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
            list.add(textPath.relativize(scriptFilePath).toString().replaceAll("\\\\", "/"));
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
    
    public String getQuizPage()
    {
    	List<Map<String, String>> list = settings.get(PageSetting.KEY_QUIZ_PAGE);
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

    private int volume, page;
    private Map<String, List<Map<String, String>>> settings;
    private Path textPathForArchiveFile;
    private List<Path> textPaths;

    private List<Path> objectPaths;
    private List<Path> javascriptFilePaths;
    private Path coverImagePath;
    private Path coverPagePath;
    private Path communityButtonImagePath;

    private final Map<String, String> TYPE_PATH = new HashMap<String, String>() {
        {
            put("common", "common");
            put("volume", "vol-");
        }
    };

    public static final String VALUE_KEY_PAGE_TYPE_BASIC = "basic";
    public static final String VALUE_KEY_PAGE_TYPE_PROFILE = "profile";
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
    public static final String KEY_ATTR_ATTRIBUTE = "attribute";
    public static final String KEY_ATTR_TYPE = "type";
    public static final String KEY_ATTR_VALUE = "value";
    public static final String KEY_PAGE_TYPE = "page-type";
    public static final String KEY_SUBJECT = "chapter";
    public static final String KEY_SUBSUBJECT = "section";
    public static final String KEY_OBJECT = "object";
    public static final String KEY_TEXT = "text";
    public static final String KEY_COVER = "cover";
    public static final String KEY_PUBLISHED = "published";
    public static final String KEY_ATTR_CLASS = "class";
    public static final String KEY_COMMUNITY_BUTTON = "community-button";
    public static final String KEY_COVER_PAGE = "cover-page";
    public static final String KEY_SHOW_TOC = "show-toc";
    public static final String KEY_ITEM_PROPERTY = "item-property";
    public static final String KEY_COMMUNITY_BUTTON_IMAGE = "community-button-image";
    public static final String KEY_JAVASCRIPT_FILE = "javascript-file";
    public static final String KEY_IDENTIFIER = "identifier";

    public static final String KEY_ATTR_OPTION = "option";
    
    // for ver. 2
    public static final String KEY_VIDEO_IMAGE = "video-image";
    public static final String KEY_COMMUNITY = "community";
    public static final String KEY_IMAGE = "image";
    public static final String KEY_YOUTUBE_ID = "youtube-id";
    public static final String KEY_QUIZ_PAGE = "quiz-page";
    public static final String KEY_CC = "CC";
    public static final String KEY_DOCUMENT_TEXT = "document-text";
    
    public static final String VALUE_KEY_PAGE_TYPE_COVER = "cover";
    public static final String VALUE_KEY_PAGE_TYPE_DOCUMENT = "document";
    public static final String VALUE_KEY_PAGE_TYPE_TEST = "test";
    public static final String VALUE_KEY_PAGE_TYPE_SECTION_COVER = "section-cover";

    public static final String VALUE_KEY_ITEM_PROPERTY_COVER_IMAGE = "cover-image";    
}
