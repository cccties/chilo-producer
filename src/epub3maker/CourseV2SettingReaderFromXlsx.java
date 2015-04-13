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

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import static epub3maker.Epub3MakerV2.*;

public class CourseV2SettingReaderFromXlsx extends CourseSettingReaderFromXlsx implements CourseSettingReader {
    static private final String META_SHEET_NAME = "series-information";
    static private final String BOOKLIST_SHEET_NAME = "book-list"; 
//    static public final String SHEETS_PREFIX = "vol";
//    static public final String SHEETS_SEPARATOR = "-";
//    static public final String SHHETS_PREFIX_WITH_SEPARATOR = SHEETS_PREFIX
//            + SHEETS_SEPARATOR;
//    private Path filePath;
//    private List<String> pageSettingAttributeNames;
//    private List<String> pageSettingKeyNames;
    
    int ver2VolKeys;
    Map<String, String> meta;
    List<Map<String, String>> bookList;
    private List<String> bookListKeyNames;
    
    public CourseV2SettingReaderFromXlsx(Path courseDir) throws IOException, Epub3MakerException
	{
    	super(courseDir);
    }

    public Course read() throws Epub3MakerException {
        XSSFWorkbook workBook = null;
        try {
            workBook = new XSSFWorkbook(new FileInputStream(
                    this.filePath.toString()));
            
            meta = readMetaSheet(workBook);
            bookList = readBookList(workBook);
            
            for (Map<String, String> map : bookList) {
            	String k = map.get("vol");
            	String v = map.get("series-title");
            	meta.put(k,	v);
            }
            
            return new Course(meta,
                    readVolSheets(workBook), bookList, pageSettingAttributeNames,
                    pageSettingKeyNames);
        } catch (IOException e) {
            // TODO 自動生成された catch ブロック
            e.printStackTrace();
        } finally {
            try {
                workBook.close();
            } catch (IOException e) {
                // TODO 自動生成された catch ブロック
                e.printStackTrace();
            }
        }
        return null;
    }

    protected Map<String, String> readMetaSheet(Workbook workBook)
            throws FileNotFoundException, IOException {
        Map<String, String> meta = readSheetVKey(workBook, META_SHEET_NAME);

        String val = "";
        for (Map.Entry<String, String> e: meta.entrySet()) {
        	if (e.getKey().equals(Course.KEY_COURSE_ID2)) {
        		val = e.getValue();
        		break;
        	}
        }
        if (Util.isValueValid(val)) {
        	meta.put(Course.KEY_COURSE_ID, val);
        	meta.remove(Course.KEY_COURSE_ID2);
        }
        
        val = "";
        for (Map.Entry<String, String> e: meta.entrySet()) {
        	if (e.getKey().equals(Course.KEY_COURSE_NAME2)) {
        		val = e.getValue();
        		break;
        	}
        }
        if (Util.isValueValid(val)) {
        	meta.put(Course.KEY_COURSE_NAME, val);
        	meta.remove(Course.KEY_COURSE_NAME2);
        }
        
        setInputPathAbs(meta, Course.KEY_INPUT_PATH);
        setOutputPathAbs(meta, Course.KEY_OUTPUT_PATH);
        return meta;
    }
    
    protected List<Map<String, String>> readBookList(Workbook wb)
    {
    	bookListKeyNames = new ArrayList<String>();
    	
    	bookListKeyNames.add(Course.KEY_BOOKLIST_VOL);
    	bookListKeyNames.add(Course.KEY_BOOKLIST_SERIES_TITLE);
    	bookListKeyNames.add(Course.KEY_BOOKLIST_BOOK_SUMMARY);
//    	bookListKeyNames.add(Course.KEY_BOOKLIST_BREADCRUMBS);
//    	bookListKeyNames.add(Course.KEY_BOOKLIST_BREADCRUMBS_URL);
    	bookListKeyNames.add(Course.KEY_BOOKLIST_COMMUNITY_URL);
        bookListKeyNames.add(Course.KEY_BOOKLIST_EPUB_DOWNLOAD_URL);
    	
    	Sheet sheet = wb.getSheet(BOOKLIST_SHEET_NAME);
    	Iterator<Row> rowIte = sheet.iterator();

    	if (rowIte.hasNext())
    		rowIte.next();
    	
    	List<Map<String, String>> list = new ArrayList<>();
    	
    	while (rowIte.hasNext()) {
    		Row row = rowIte.next();
    		Map<String, String> map = new HashMap<>();
    		for (int i = 0; i < bookListKeyNames.size(); i++) {
    			Cell cell = row.getCell(i);
    			String val = getStringValue(wb, cell);
    			map.put(bookListKeyNames.get(i), val);
    		}
    		list.add(map);
    	}
    	return list;
    }

    protected Map<Integer, Volume> readVolSheets(XSSFWorkbook workBook)
            throws FileNotFoundException, IOException, Epub3MakerException {
        Map<Integer, Volume> volumes = new HashMap<>();
        final String prefix = SHHETS_PREFIX_WITH_SEPARATOR;
        
        int currentVolume = -1;
        Iterator<XSSFSheet> sheetsIte = workBook.iterator();
        while (sheetsIte.hasNext()) {
            Sheet sheet = sheetsIte.next();
            if (!isPageSettingSheet(sheet, prefix)) {
            	continue;
            }
            
            setPageSettingNames(sheet);
            
            /*
             * Iterator で回すと空行を自動で飛ばしてしまう
             */
//            Iterator<Row> rowIte = sheet.iterator();
//            if (rowIte.hasNext())
//            	rowIte.next(); // skip key
            
            int page = VOLUME_COVER_PAGE;
            String curChapter = null;
            int lastrow = sheet.getLastRowNum();
            for (int rownum = 1; rownum <= lastrow ; rownum++) {
                // 1 行目はスキップ
            	PageSetting sectionCover = null;
            	Row row = sheet.getRow(rownum);
            	if (row == null) {
                    Util.infoPrintln(LogLevel.LOG_INFO, "readVolSheets done, because empty Row found !!!");
                    break;
            	}
            	PageSetting setting = readPageSetting(row, prefix, page);
            	if (setting == null) {
            		break;
            	}
//                Util.infoPrintln(LogLevel.LOG_DEBUG, "### pagetype ### " + setting.getPageType());
            	/*
            	 * page-type が空白の場合はそこで終わる
            	 */
            	if (!Util.isValueValid(setting.getPageType())) {
            	    Util.infoPrintln(LogLevel.LOG_INFO, "readVolSheets done, because empty page-type found.");
            	    break;
            	}
            	String chapter = setting.getChapter();
            	if (curChapter != null && !chapter.equals(curChapter) && !setting.getPageType().equals(PageSetting.VALUE_KEY_PAGE_TYPE_TEST)) {
            		// then create section cover page
            		sectionCover = createSectionCover(setting.getVolume(), page, chapter);
            	}
            	if (chapter == null) { // page-type is cover
            		curChapter = "";
            	} else {
            		curChapter = chapter;
            	}

            	if (currentVolume != setting.getVolume()) {
            		volumes.put((setting.getVolume()),
            				new Volume(setting.getVolume()));

            		currentVolume = setting.getVolume();
            	}
            	if (sectionCover != null) {
            		volumes.get(currentVolume).getPageSettings().add(sectionCover);
            		setting.setPage(++page);
            	}
            	volumes.get(currentVolume).getPageSettings().add(setting);
            	
            	if (page == Epub3MakerV2.VOLUME_COVER_PAGE) {
            		page = DOCUMENT_START_PAGE;
            	} else {
            		page++;
            	}
            }
        }
        return volumes;
    }

    protected boolean isPageSettingSheet(Sheet sheet, String prefix) {
        String sheetName = sheet.getSheetName();
        return sheetName.substring(0, prefix.length()).equals(prefix);
    }
    

    protected PageSetting readPageSetting(Row row, String prefix, int page) throws Epub3MakerException {
    	setPageSettingNames(row.getSheet());

        String sheetName = row.getSheet().getSheetName();
        String vol = sheetName.substring(prefix.length());
        Map<String, List<Map<String, String>>> settings = readPageSettingRow(row);
        
        return settings != null ? new PageSetting(Integer.parseInt(vol), page, settings) : null;
    }
    
    private String getExt(String str) {
    	int i = str.indexOf(".");
		if (i == -1) {
			return null;
		} 
		return str.substring(i + 1);
    }
 
    protected Map<String, List<Map<String, String>>> readPageSettingRow(Row row) throws Epub3MakerException {
    	Map<String, List<Map<String, String>>> settings = new HashMap<>();
        String key, value;
        Cell cell;
        String coverFile = null;
        boolean communityPage = false;
        boolean hasJavaScript = false;
        boolean hasSvg = false;

        for (int i = 0; i < ver2VolKeys; i++) {
        	Map<String, String> pageSetting = new HashMap<>();
        	key = pageSettingKeyNames.get(i);
        	
        	cell = row.getCell(i);
        	if (cell == null) {
        		value = null;
        	} else {
        		value = getStringValue(cell.getRow().getSheet().getWorkbook(), cell);
        	}

        	pageSetting.put(PageSetting.KEY_ATTR_ATTRIBUTE, "string");
        	pageSetting.put(PageSetting.KEY_ATTR_TYPE, null);
        	pageSetting.put(PageSetting.KEY_ATTR_VALUE, value);
        	pageSetting.put(PageSetting.KEY_ATTR_CLASS, null);
        	pageSetting.put(PageSetting.KEY_ATTR_OPTION, null);
        	
        	if (key.equals(PageSetting.KEY_OBJECT) && value != null) {
        		String where = "volume";
        		/*
        		 * ちょっとadhocだが，KEY_PAGE_TYPEで値が取れなかったら nullを返す事にする
        		 */
        		if (settings.get(PageSetting.KEY_PAGE_TYPE).get(0).get(PageSetting.KEY_ATTR_VALUE) == null) {
        		    Util.infoPrintln(LogLevel.LOG_INFO, "readPageSettingsRow: no page type !!!");
        		    return null;
        		}
            	if (settings.get(PageSetting.KEY_PAGE_TYPE).get(0).get(PageSetting.KEY_ATTR_VALUE).equals(PageSetting.VALUE_KEY_PAGE_TYPE_COVER)) {
            		coverFile = value;
            	}
            	pageSetting.put(PageSetting.KEY_ATTR_TYPE, where);
            	
            	String ext = getExt(value);
        		if (ext != null) {
        			String type = "string";
        			if (ext.equals("jpg") || ext.equals("png")) {
        				type = "image";
        			} else if (ext.equals("mp4")) {
        				type = "video";
        			}
        			pageSetting.put(PageSetting.KEY_ATTR_ATTRIBUTE, type);
        		}
        	}
        	
        	if (key.equals(PageSetting.KEY_COMMUNITY) && value != null && value.equalsIgnoreCase("true")) {
        		communityPage = true;
        	}
        	
    		if (key.equals(PageSetting.KEY_SUBJECT)) {
    			pageSetting.put(PageSetting.KEY_ATTR_ATTRIBUTE, "svg2");
    			pageSetting.put(PageSetting.KEY_ATTR_CLASS, "subject");
    			if (Util.isValueValid(value)) {
    				hasSvg = true;
    			}
    		}

    		if (key.equals(PageSetting.KEY_SUBSUBJECT)) {
    			pageSetting.put(PageSetting.KEY_ATTR_ATTRIBUTE, "svg0");
    			pageSetting.put(PageSetting.KEY_ATTR_CLASS, "subsubject");
    			if (Util.isValueValid(value)) {
    				hasSvg = true;
    			}
    		}
    		
        	if (key.equals(PageSetting.KEY_TEXT)) {
        		pageSetting.put(PageSetting.KEY_ATTR_TYPE, "volume");
        		if (value != null) {
        			String type = "string";
        			String ext = getExt(value);
        			if (ext == null || !ext.equals("xhtml")) {
        				throw new Epub3MakerException("不正なファイルフォーマットです: " + ext);
        			}
        			type = "preformat";
        			pageSetting.put(PageSetting.KEY_ATTR_ATTRIBUTE, type);
        		}
        	}
        	
        	if (key.equals(PageSetting.KEY_JAVASCRIPT_FILE)) {
        		pageSetting.put(PageSetting.KEY_ATTR_TYPE, "volume");
        		pageSetting.put(PageSetting.KEY_ATTR_ATTRIBUTE, "file");
        		if (Util.isValueValid(value)) {
        			hasJavaScript = true;
        		}
        	}

        	List<Map<String, String>> list = new ArrayList<>();
        	list.add(pageSetting);
        	settings.put(key, list);
        }

        for (int i = ver2VolKeys; i < pageSettingKeyNames.size(); i++) {
        	Map<String, String> pageSetting = new HashMap<>();
        	
        	key = pageSettingKeyNames.get(i);
        	
        	pageSetting.put(PageSetting.KEY_ATTR_ATTRIBUTE, "string");
        	pageSetting.put(PageSetting.KEY_ATTR_TYPE, null);
        	pageSetting.put(PageSetting.KEY_ATTR_VALUE, null);
        	pageSetting.put(PageSetting.KEY_ATTR_CLASS, null);
        	pageSetting.put(PageSetting.KEY_ATTR_OPTION, null);
        	
           	if (key.equals(PageSetting.KEY_IDENTIFIER)) {
           		String volStr = row.getSheet().getSheetName().replaceFirst("vol", "");
           		String id = meta.get(Course.KEY_COURSE_ID) + volStr;
           		pageSetting.put(PageSetting.KEY_ATTR_VALUE, id);
           	}
           	
           	if (key.equals(PageSetting.KEY_SHOW_TOC)) {
           		pageSetting.put(PageSetting.KEY_ATTR_VALUE, "true");
           	}
           	
           	if (key.equals(PageSetting.KEY_PUBLISHED)) {
           		pageSetting.put(PageSetting.KEY_ATTR_VALUE, meta.get(PageSetting.KEY_PUBLISHED)); 
           	}

        	if (key.equals(PageSetting.KEY_COVER)) {
        		pageSetting.put(PageSetting.KEY_ATTR_VALUE, coverFile);
        		pageSetting.put(PageSetting.KEY_ATTR_TYPE, "volume");
        		pageSetting.put(PageSetting.KEY_ATTR_ATTRIBUTE, "image");
        	}
        	
        	if (key.equals(PageSetting.KEY_COVER_PAGE)) {
        		pageSetting.put(PageSetting.KEY_ATTR_TYPE, "common");
        		pageSetting.put(PageSetting.KEY_ATTR_ATTRIBUTE, "file");
        	}
        	
        	if (key.equals(PageSetting.KEY_COMMUNITY_BUTTON) && communityPage) {
        		pageSetting.put(PageSetting.KEY_ATTR_VALUE, getBookListElement(row.getSheet().getSheetName(), Course.KEY_BOOKLIST_COMMUNITY_URL));
        	}
        	
        	if (key.equals(PageSetting.KEY_COMMUNITY_BUTTON_IMAGE) && communityPage) {
        		pageSetting.put(PageSetting.KEY_ATTR_ATTRIBUTE, "image");
        		pageSetting.put(PageSetting.KEY_ATTR_TYPE, "common");
        		pageSetting.put(PageSetting.KEY_ATTR_CLASS, "community");
        		pageSetting.put(PageSetting.KEY_ATTR_VALUE, Const.COMMUNITY_IMG);
        	}
        	
        	if (key.equals(PageSetting.KEY_ITEM_PROPERTY)) {
        		String s = "";
        		if (hasSvg) {
        			s += PageSetting.VALUE_KEY_ITEM_PROPERTY_SVG + " ";
        		}
        		if (hasJavaScript) {
        			s += PageSetting.VALUE_KEY_ITEM_PROPERTY_SCRIPTED;
        		}
        		if (Util.isValueValid(s)) {
        			pageSetting.put(PageSetting.KEY_ATTR_VALUE, s.trim());
        		}
        	}
        	
        	List<Map<String, String>> list = new ArrayList<>();
        	list.add(pageSetting);
        	
        	settings.put(key, list);
        }
        return settings;
    }
        	
    PageSetting createSectionCover(int vol, int page, String chapter) {
    	Map<String, List<Map<String, String>>> settings = new HashMap<>();
    	String key, value = null;
    	for (int i = 0; i < ver2VolKeys; i++) {
    		Map<String, String> pageSetting = new HashMap<>();
    		key = pageSettingKeyNames.get(i);
            	
    		pageSetting.put(PageSetting.KEY_ATTR_ATTRIBUTE, "string");
    		pageSetting.put(PageSetting.KEY_ATTR_TYPE, null);
    		pageSetting.put(PageSetting.KEY_ATTR_VALUE, "");
    		pageSetting.put(PageSetting.KEY_ATTR_CLASS, null);
    		pageSetting.put(PageSetting.KEY_ATTR_OPTION, null);
            	
    		if (key.equals(PageSetting.KEY_OBJECT)) {
    			value = meta.get(PageSetting.KEY_COVER);
    			pageSetting.put(PageSetting.KEY_ATTR_VALUE, value);
    			pageSetting.put(PageSetting.KEY_ATTR_TYPE, "common");
    			pageSetting.put(PageSetting.KEY_ATTR_ATTRIBUTE, "image");
    		}
            	
    		if (key.equals(PageSetting.KEY_SUBJECT)) {
    			pageSetting.put(PageSetting.KEY_ATTR_VALUE, chapter);
    			pageSetting.put(PageSetting.KEY_ATTR_ATTRIBUTE, "svg2");
    		}
    		
    		if (key.equals(PageSetting.KEY_TEXT)) {
    			pageSetting.put(PageSetting.KEY_ATTR_TYPE, "volume");
    		}
    		
    		if (key.equals(PageSetting.KEY_JAVASCRIPT_FILE)) {
    			pageSetting.put(PageSetting.KEY_ATTR_TYPE, "volume");
    			pageSetting.put(PageSetting.KEY_ATTR_ATTRIBUTE, "file");
    		}
    		
    		if (key.equals(PageSetting.KEY_PAGE_TYPE)){
    			pageSetting.put(PageSetting.KEY_ATTR_VALUE, PageSetting.VALUE_KEY_PAGE_TYPE_SECTION_COVER);
    		}
    		
    		List<Map<String, String>> list = new ArrayList<>();
    		list.add(pageSetting);
    		settings.put(key, list);
    	}

    	for (int i = ver2VolKeys; i < pageSettingKeyNames.size(); i++) {
        	Map<String, String> pageSetting = new HashMap<>();
        	
        	key = pageSettingKeyNames.get(i);
        	
        	pageSetting.put(PageSetting.KEY_ATTR_ATTRIBUTE, "string");
        	pageSetting.put(PageSetting.KEY_ATTR_TYPE, null);
        	pageSetting.put(PageSetting.KEY_ATTR_VALUE, null);
        	pageSetting.put(PageSetting.KEY_ATTR_CLASS, null);
        	pageSetting.put(PageSetting.KEY_ATTR_OPTION, null);
        	
    		if (key.equals(PageSetting.KEY_ITEM_PROPERTY)) {
    			pageSetting.put(PageSetting.KEY_ATTR_VALUE, "svg");
    		}
       	
        	List<Map<String, String>> list = new ArrayList<>();
        	list.add(pageSetting);
        	
        	settings.put(key, list);
        }
        return new PageSetting(vol, page, settings);
    }

    protected void setPageSettingNames(Sheet sheet) {
        if (this.pageSettingKeyNames == null) {
        	pageSettingKeyNames = new ArrayList<>();
        	pageSettingKeyNames.add(PageSetting.KEY_PAGE_TYPE);
        	pageSettingKeyNames.add(PageSetting.KEY_SUBJECT);
        	pageSettingKeyNames.add(PageSetting.KEY_SUBSUBJECT);
        	pageSettingKeyNames.add(PageSetting.KEY_COMMUNITY);
        	pageSettingKeyNames.add(PageSetting.KEY_OBJECT);
        	pageSettingKeyNames.add(PageSetting.KEY_VIDEO_IMAGE);
        	pageSettingKeyNames.add(PageSetting.KEY_TEXT);
        	pageSettingKeyNames.add(PageSetting.KEY_JAVASCRIPT_FILE);
        	pageSettingKeyNames.add(PageSetting.KEY_YOUTUBE_ID);
        	pageSettingKeyNames.add(PageSetting.KEY_CC);
        	this.ver2VolKeys = pageSettingKeyNames.size();
        	
        	// for compatibility with ver.1             	
        	pageSettingKeyNames.add(PageSetting.KEY_IDENTIFIER);
        	pageSettingKeyNames.add(PageSetting.KEY_PUBLISHED);
        	pageSettingKeyNames.add(PageSetting.KEY_COVER);
        	pageSettingKeyNames.add(PageSetting.KEY_COVER_PAGE);
        	pageSettingKeyNames.add(PageSetting.KEY_SHOW_TOC);
        	pageSettingKeyNames.add(PageSetting.KEY_COMMUNITY_BUTTON);
        	pageSettingKeyNames.add(PageSetting.KEY_COMMUNITY_BUTTON_IMAGE);
        	pageSettingKeyNames.add(PageSetting.KEY_ITEM_PROPERTY);
        }
        if (pageSettingAttributeNames == null) {
            pageSettingAttributeNames = new ArrayList<>();
            pageSettingAttributeNames.add("attribute");
            pageSettingAttributeNames.add("type");
            pageSettingAttributeNames.add("value");
            pageSettingAttributeNames.add("class");
        }
    }

    private String getBookListElement(String vol, String key) 
    {
    	for (Map<String, String> e : bookList) {
    		if (e.get(Course.KEY_BOOKLIST_VOL).equals(vol)) {
    			return e.get(key);
    		}
    	}
    	return null;
    }
}
