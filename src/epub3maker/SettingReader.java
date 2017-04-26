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
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

public class SettingReader extends ExcelReader {
	
	private static Log log = LogFactory.getLog(SettingReader.class);

    static private final String META_SHEET_NAME = "series-information";
    static private final String BOOKLIST_SHEET_NAME = "book-list"; 
    static private final String BOOK_SHEETS_PREFIX = "vol-";

    Path filePath;
    boolean sideBySide = true;

    Map<String, String> meta;
    List<Map<String, String>> bookList;
    List<String> attributeNames;
    List<String> keyNames;
    int ver2VolKeys;
    private List<String> bookListKeyNames;
    
    public SettingReader(Path seriesPath) throws IOException, Epub3MakerException
	{
	    int i = 0;

	    if(seriesPath.toString().endsWith(".xlsx")){
	    	filePath = seriesPath;
//	    	sideBySide = true;
    		i++;
	    } else {
	    	DirectoryStream<Path> ds = Files.newDirectoryStream(seriesPath, "{[!~][!$]}*.xlsx");
	    	for (Path p : ds) {
	    		if(p.getFileName().toString().compareTo("authors.xlsx") == 0){
	    			continue;
	    		}
	    		filePath = p;
	    		i++;
	    	}
	    }

	    if (i > 1) {
            throw new Epub3MakerException("!!! TOO MANY META FILES !!!");
	    }
        if (i < 1) {
            throw new Epub3MakerException("!!! TOO LESS META FILES !!!");
        }

		log.info("open " + filePath);
        initPageSettingNames();
    }

    public Series read() throws Epub3MakerException {
        try {
            workBook = new XSSFWorkbook(new FileInputStream(filePath.toString()));
            
            meta = readMetaSheet();
            bookList = readBookList();
            Map<Integer, Book> books = readVolSheets(); 

            return new Series(meta, books);
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                workBook.close();
                workBook = null;
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return null;
    }

    protected Map<String, String> readSheetVKey(String sheetName) {
    	Sheet sheet = workBook.getSheet(sheetName);
        Map<String, String> map = new HashMap<>();
        Iterator<Row> it = sheet.rowIterator();
        while (it.hasNext()) {
            Row row = it.next();
            Cell key = row.getCell(0);
            Cell value = row.getCell(1);
            if (key == null || value == null) {
                continue;
            }
            map.put(getStringValue(key),getStringValue(value));
        }
        return map;
    }

    protected Map<String, String> readMetaSheet() throws FileNotFoundException, IOException {
        Map<String, String> meta = readSheetVKey(META_SHEET_NAME);
        
        /*
         * input-path, output-path, output-name はコマンドライン引数から
         */
        meta.put(Series.KEY_INPUT_PATH, Config.getInputPath());
        meta.put(Series.KEY_OUTPUT_PATH, Config.getOutputPath());
        meta.put(Series.KEY_OUTPUT_NAME, Config.getOutputName());

        setInputPathAbs(meta, Series.KEY_INPUT_PATH);
        setOutputPathAbs(meta, Series.KEY_OUTPUT_PATH);

        //
        // allow 'creator' instead of 'author'
        //
        String author = meta.get(Series.KEY_AUTHOR);
        if(author == null){
        	meta.put(Series.KEY_AUTHOR, meta.get(Series.KEY_V2_CREATOR));
        }

        return meta;
    }
    
    protected void setOutputPathAbs(Map<String, String> meta, String key) {
        String pathString = meta.get(key);
        Path path;

        if (pathString == null) {
            pathString = "";
        }

        if(sideBySide){
        	path = filePath.getParent();
        	if(path == null){
        		path = Paths.get(pathString);
        	} else {
        		path = path.resolve(pathString);
        	}
        	log.debug("setOutputPathAbs: " + path.resolve("epub3").normalize().toString());
            meta.put(key, path.resolve("epub3").normalize().toString());
            return;
        }

        path = Paths.get(pathString);
        if (!path.isAbsolute()) {
            Path seriesName = filePath.getParent().getFileName();
            Path outputDir = Paths.get(Config.getOutputBaseDir()).resolve(path);
            meta.put(key, outputDir.resolve(seriesName).resolve("epub3").normalize().toString());
        }
    }

    protected void setInputPathAbs(Map<String, String> meta, String key) {
        String pathString = meta.get(key);
        if (pathString == null) {
            pathString = "";
        }
        Path path = Paths.get(pathString);
        if (!path.isAbsolute() && filePath.getParent() != null){
            meta.put(key, filePath.getParent().resolve(path).normalize().toString());
        }
    }

	private String bookListKeyAll[] = {
			Series.KEY_BOOKLIST_VOL,
			Series.KEY_VERSION,
			Series.KEY_LANGUAGE,
			Series.KEY_AUTHOR,
			Series.KEY_PUBLISHER,
			Series.KEY_EDITOR,
			Series.KEY_PUBLISHED,
			Series.KEY_REVISED,
			Series.KEY_RIGHTS,
			Series.KEY_BOOKLIST_BOOK_TITLE,
			Series.KEY_BOOKLIST_BOOK_SUMMARY,
			Series.KEY_BOOKLIST_BOOK_COVER,
			Series.KEY_BOOKLIST_INSIDE_COVER,
			Series.KEY_BOOKLIST_ID,
			Series.KEY_BOOKLIST_COMMUNITY_URL,
	};

    private void initBookListKeyNames(Row row) throws Epub3MakerException {
        bookListKeyNames = new ArrayList<String>();

		for (int i = 0; i < bookListKeyAll.length; i++) {
			Cell cell = row.getCell(i);
			String val = getStringValue(cell);
			if(val != null && val.length() > 0) {
				String found = null;
				for (String key: bookListKeyAll){
					if(val.startsWith(key)){
						found = key;
						break;
					}
				}
				if(found == null){
    				throw new Epub3MakerException("サポートしていない入力項目名です: " + val);
				} else if(bookListKeyNames.contains(found)) {
    				throw new Epub3MakerException("入力項目名が重複しています: " + val);
				}
				bookListKeyNames.add(found);
			}
		}
    }

	protected List<Map<String, String>> readBookList() throws Epub3MakerException {
    	Sheet sheet = workBook.getSheet(BOOKLIST_SHEET_NAME);
    	Iterator<Row> rowIte = sheet.iterator();

    	// initialize key list using first row
    	if (rowIte.hasNext()) {
    		initBookListKeyNames(rowIte.next());
    	}
    	
    	List<Map<String, String>> list = new ArrayList<>();
    	
    	while (rowIte.hasNext()) {
    		Row row = rowIte.next();
    		Map<String, String> map = new HashMap<>();
    		for (int i = 0; i < bookListKeyNames.size(); i++) {
    			Cell cell = row.getCell(i);
    			String val = getStringValue(cell);
    			map.put(bookListKeyNames.get(i), val);
    		}
    		list.add(map);
    	}
    	return list;
    }

    protected Map<Integer, Book> readVolSheets() throws FileNotFoundException, IOException, Epub3MakerException {
        Map<Integer, Book> books = new HashMap<>();
        
        Iterator<XSSFSheet> sheetsIte = workBook.iterator();
        while (sheetsIte.hasNext()) {
            Sheet sheet = sheetsIte.next();
            String sheetName = sheet.getSheetName();
            if (!sheetName.startsWith(BOOK_SHEETS_PREFIX)) {
            	continue;
            }

            //
            // search bookInfo
            //
            Map<String, String> bookInfo = null;
            for(Map<String, String> b: bookList){
            	String volStr = b.get(Series.KEY_BOOKLIST_VOL);
            	if(volStr != null && volStr.equals(sheetName)){
            		bookInfo = b;
            		break;
            	}
            }

            if(bookInfo == null){
            	continue;
            }

            //
            // create cover page
            //
            int vol = Integer.parseInt(sheetName.substring(BOOK_SHEETS_PREFIX.length()));
            int page = Process.VOLUME_COVER_PAGE;
            String bookCover = bookInfo.get(Series.KEY_BOOKLIST_BOOK_COVER);
            String bookId = bookInfo.get(Series.KEY_BOOKLIST_ID);
            
            PageSetting coverSetting = null;
            if (bookCover != null && bookCover.length() != 0) {
                log.debug("Found cover in BookList: " + bookCover +  " for " + sheetName);
                coverSetting = createBookCoverPage(vol, page, bookCover, bookId);
            }

            Book book = new Book(vol, bookInfo);
            books.put(vol, book);
            if(coverSetting != null) {
            	book.addPageSetting(coverSetting);
            }
            
            page = Process.DOCUMENT_START_PAGE;
            
            String curSection = null;
            int lastrow = sheet.getLastRowNum();

            for (int rownum = 1; rownum <= lastrow ; rownum++) {
            	//
                // skip first row
            	//
            	PageSetting insideCover = null;
            	Row row = sheet.getRow(rownum);
            	if (row == null) {
                    log.debug("readVolSheets done, because empty Row found !!!");
                    break;
            	}
            	
            	PageSetting setting = readPageSetting(row, vol, page);
            	if (setting == null) {
            		break;
            	}

            	if (!Util.isValueValid(setting.getPageType())) {
            	    log.debug("readVolSheets done, because empty page-type found.");
            	    break;
            	}

            	String section = setting.getSection();
            	if (section == null) {
            	    log.info("readVolSheets done, section-title is required.");
            	    break;
            	}

            	//
        		// create inside cover page
            	//
            	if ((curSection == null || !section.equals(curSection)) && !setting.getPageType().equals(PageSetting.VALUE_KEY_PAGE_TYPE_TEST)) {
            		insideCover = createInsideCover(vol, page, section);
            	}
            	curSection = section;

            	//
            	// add settings
            	//
            	if (insideCover != null) {
            		book.addPageSetting(insideCover);
            		setting.setPage(++page);
            	}
            	book.addPageSetting(setting);
            	
            	page++;
            }
        }
        return books;
    }

    protected PageSetting readPageSetting(Row row, int vol, int page) throws Epub3MakerException {
        Map<String, List<Map<String, String>>> settings = readPageSettingRow(row);
        return settings != null ? new PageSetting(vol, page, settings) : null;
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
        boolean hasJavaScript = false;
        boolean hasSvg = false;

        for (int i = 0; i < ver2VolKeys; i++) {
        	Map<String, String> pageSetting = new HashMap<>();
        	key = keyNames.get(i);
        	
        	cell = row.getCell(i);
        	if (cell == null) {
        		value = null;
        	} else {
        		value = getStringValue(cell);
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
        		    log.debug("readPageSettingsRow: no page type !!!");
        		    return null;
        		}

            	pageSetting.put(PageSetting.KEY_ATTR_TYPE, where);

            	String type = Util.getMainContentType(value); 
            	if(type != null) {
            		pageSetting.put(PageSetting.KEY_ATTR_ATTRIBUTE, type); 
            	}
        	}
        	
        	if (key.equals(PageSetting.KEY_COMMUNITY) && value != null && value.equalsIgnoreCase("true")) {
                pageSetting.put(PageSetting.KEY_ATTR_ATTRIBUTE, "community");
        		pageSetting.put(PageSetting.KEY_ATTR_VALUE, "true");
        	}
        	
    		if (key.equals(PageSetting.KEY_SECTION)) {
    			pageSetting.put(PageSetting.KEY_ATTR_ATTRIBUTE, "svg2");
    			pageSetting.put(PageSetting.KEY_ATTR_CLASS, "subject");
    			if (Util.isValueValid(value)) {
    				hasSvg = true;
    			}
    		}

    		if (key.equals(PageSetting.KEY_TOPIC)) {
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
        			if (ext == null || (!ext.equals("xhtml") && !ext.equals("txt"))) {
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

        for (int i = ver2VolKeys; i < keyNames.size(); i++) {
        	Map<String, String> pageSetting = new HashMap<>();
        	
        	key = keyNames.get(i);
        	
        	pageSetting.put(PageSetting.KEY_ATTR_ATTRIBUTE, "string");
        	pageSetting.put(PageSetting.KEY_ATTR_TYPE, null);
        	pageSetting.put(PageSetting.KEY_ATTR_VALUE, null);
        	pageSetting.put(PageSetting.KEY_ATTR_CLASS, null);
        	pageSetting.put(PageSetting.KEY_ATTR_OPTION, null);
        	
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
        	
    PageSetting createInsideCover(int vol, int page, String section) {
    	Map<String, List<Map<String, String>>> settings = new HashMap<>();
    	String key, value = null;
    	for (int i = 0; i < ver2VolKeys; i++) {
    		Map<String, String> pageSetting = new HashMap<>();
    		key = keyNames.get(i);
            	
    		pageSetting.put(PageSetting.KEY_ATTR_ATTRIBUTE, "string");
    		pageSetting.put(PageSetting.KEY_ATTR_TYPE, null);
    		pageSetting.put(PageSetting.KEY_ATTR_VALUE, "");
    		pageSetting.put(PageSetting.KEY_ATTR_CLASS, null);
    		pageSetting.put(PageSetting.KEY_ATTR_OPTION, null);
            	
    		if (key.equals(PageSetting.KEY_OBJECT)) {
    			value = meta.get(Series.KEY_V2_COVER);
    			pageSetting.put(PageSetting.KEY_ATTR_VALUE, value);
    			pageSetting.put(PageSetting.KEY_ATTR_TYPE, "common");
    			pageSetting.put(PageSetting.KEY_ATTR_ATTRIBUTE, "image");
    		}
            	
    		if (key.equals(PageSetting.KEY_SECTION)) {
    			pageSetting.put(PageSetting.KEY_ATTR_VALUE, section);
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
    			pageSetting.put(PageSetting.KEY_ATTR_VALUE, PageSetting.VALUE_KEY_PAGE_TYPE_INSIDE_COVER);
    		}
    		
    		List<Map<String, String>> list = new ArrayList<>();
    		list.add(pageSetting);
    		settings.put(key, list);
    	}

    	for (int i = ver2VolKeys; i < keyNames.size(); i++) {
        	Map<String, String> pageSetting = new HashMap<>();
        	
        	key = keyNames.get(i);
        	
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

    protected void initPageSettingNames() {
        if (keyNames == null) {
        	keyNames = new ArrayList<>();
        	keyNames.add(PageSetting.KEY_PAGE_TYPE);
        	keyNames.add(PageSetting.KEY_SECTION);
        	keyNames.add(PageSetting.KEY_TOPIC);
        	keyNames.add(PageSetting.KEY_COMMUNITY);
        	keyNames.add(PageSetting.KEY_OBJECT);
        	keyNames.add(PageSetting.KEY_VIDEO_IMAGE);
        	keyNames.add(PageSetting.KEY_TEXT);
        	keyNames.add(PageSetting.KEY_JAVASCRIPT_FILE);
        	keyNames.add(PageSetting.KEY_YOUTUBE_ID);
        	keyNames.add(PageSetting.KEY_CC);
        	keyNames.add(PageSetting.KEY_CLIP_BEGIN);
        	keyNames.add(PageSetting.KEY_CLIP_END);
        	ver2VolKeys = keyNames.size();
        	
        	keyNames.add(PageSetting.KEY_ITEM_PROPERTY);
        }
        if (attributeNames == null) {
            attributeNames = new ArrayList<>();
            attributeNames.add("attribute");
            attributeNames.add("type");
            attributeNames.add("value");
            attributeNames.add("class");
        }
    }

    private PageSetting createBookCoverPage(int vol, int page, String bookCover, String bookId) {
        Map<String, List<Map<String, String>>> settings = new HashMap<>();
        String key;

        /*
         * まず "main" カラム相当を作成
         */
        for (int i = 0; i < ver2VolKeys; i++) {
            /*
             * とりあえず loop を回して，定義された項目分キーを入れておかないといけないらしい
             */
            Map<String, String> pageSetting = new HashMap<>();
            key = keyNames.get(i);

            pageSetting.put(PageSetting.KEY_ATTR_ATTRIBUTE, "string");
            pageSetting.put(PageSetting.KEY_ATTR_TYPE, null);
            pageSetting.put(PageSetting.KEY_ATTR_VALUE, null);
            pageSetting.put(PageSetting.KEY_ATTR_CLASS, null);
            pageSetting.put(PageSetting.KEY_ATTR_OPTION, null);
            
            if (key.equals(PageSetting.KEY_PAGE_TYPE)) {
                pageSetting.put(PageSetting.KEY_ATTR_VALUE, PageSetting.VALUE_KEY_PAGE_TYPE_COVER);
            }
            
            if (key.equals(PageSetting.KEY_OBJECT) && bookCover != null && bookCover.length() > 0) {
                pageSetting.put(PageSetting.KEY_ATTR_ATTRIBUTE, "string");
                pageSetting.put(PageSetting.KEY_ATTR_TYPE, "volume");
                pageSetting.put(PageSetting.KEY_ATTR_VALUE, bookCover);
                pageSetting.put(PageSetting.KEY_ATTR_CLASS, null);
                pageSetting.put(PageSetting.KEY_ATTR_OPTION, null);

            	String type = Util.getMainContentType(bookCover); 
            	if(type != null) {
            		pageSetting.put(PageSetting.KEY_ATTR_ATTRIBUTE, type); 
            	}
            }
            
            if (key.equals(PageSetting.KEY_SECTION)) {
                pageSetting.put(PageSetting.KEY_ATTR_ATTRIBUTE, "svg2");
                pageSetting.put(PageSetting.KEY_ATTR_CLASS, "subject");
            }

            if (key.equals(PageSetting.KEY_TOPIC)) {
                pageSetting.put(PageSetting.KEY_ATTR_ATTRIBUTE, "svg0");
                pageSetting.put(PageSetting.KEY_ATTR_CLASS, "subsubject");
            }
            
            if (key.equals(PageSetting.KEY_TEXT)) {
                pageSetting.put(PageSetting.KEY_ATTR_TYPE, "volume");
            }
            
            if (key.equals(PageSetting.KEY_JAVASCRIPT_FILE)) {
                pageSetting.put(PageSetting.KEY_ATTR_TYPE, "volume");
                pageSetting.put(PageSetting.KEY_ATTR_ATTRIBUTE, "file");
            }

            List<Map<String, String>> list = new ArrayList<>();
            list.add(pageSetting);
            settings.put(key, list);
        }

        /*
         * 次にpage-type情報を作成
         */
        for (int i = ver2VolKeys; i < keyNames.size(); i++) {
            Map<String, String> pageSetting = new HashMap<>();
            
            key = keyNames.get(i);
        
            pageSetting.put(PageSetting.KEY_ATTR_ATTRIBUTE, "string");
            pageSetting.put(PageSetting.KEY_ATTR_TYPE, null);
            pageSetting.put(PageSetting.KEY_ATTR_VALUE, null);
            pageSetting.put(PageSetting.KEY_ATTR_CLASS, null);
            pageSetting.put(PageSetting.KEY_ATTR_OPTION, null);
            
            List<Map<String, String>> list = new ArrayList<>();
            list.add(pageSetting);
            settings.put(key, list);
        }

        return new PageSetting(vol, page, settings);
    }

    /**
	 * @throws Epub3MakerException
	 */
	protected void showPageSettings(Book book) throws Epub3MakerException {
	    List<PageSetting> pageSettings = book.getPageSettings();
	    log.trace("PageSettings");
	    for (Iterator<PageSetting> psIte = pageSettings.iterator(); psIte.hasNext();) {
	        PageSetting setting = psIte.next();
	        log.trace(book.getVolumeStr() + "-" + setting.getPage());
	
	        for (Iterator<String> keyIte = keyNames.iterator(); keyIte.hasNext();) {
	            String keyName = keyIte.next();
	            List<Map<String, String>> keyValues = setting.getSettings().get(keyName);
	            String msg = "";
	            if (keyValues == null) {
	                msg = "keyValueが null です。ページ設定に空行があります。";
	                throw new Epub3MakerException(msg);
	            }
	            for (int i = 0, size = keyValues.size(); i < size; ++i) {
	                msg += keyName + ": ";
	                for (Iterator<String> anIte = attributeNames.iterator(); anIte.hasNext();) {
	                    String attributeName = anIte.next();
	                    msg += (keyValues.get(i).get(attributeName) + ", ");
	                }
	                log.trace(msg);
	            }
	        }
	    }
	}
}
