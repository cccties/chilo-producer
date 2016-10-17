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
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

public class AuthorReader extends ExcelReader {
	static final String KEY_PICTURE = "picture";
    static final String KEY_NAME = "name";
    static final String KEY_NAME2 = "name2";
	static final String KEY_ORGANIZATION = "organization";
	static final String KEY_ADDITIONAL_TITLES= "additionalTitles";
	static final String KEY_ADDITIONAL_VALUES= "additionalValues";
	
	String[] keys = {KEY_PICTURE, KEY_NAME, KEY_NAME2, KEY_ORGANIZATION};
	
	Path filePath;
	Map<String, Map<String, List<String>>> object;

	public AuthorReader(Path path)
	{
		this.filePath = path;
		object = new HashMap<>();
	}
	
	public void read() {
		try {
			workBook = new XSSFWorkbook(new FileInputStream(filePath.toString()));
			
			Iterator<XSSFSheet> sheetIte = workBook.iterator();
			
			while (sheetIte.hasNext()) {
				Sheet sheet = sheetIte.next();
				Map<String, List<String>> map = readSheet(sheet);
				object.put(sheet.getSheetName(), map);
			}
			
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				workBook.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
	
	Map<String, List<String>> readSheet(Sheet sheet) {
        Map<String, List<String>> map = new HashMap<>();
        List<String> additionalTitles = new ArrayList<>();
        List<String> additionalValues = new ArrayList<>();
        
        Iterator<Row> it = sheet.rowIterator();
        while (it.hasNext()) {
            Row row = it.next();
            Cell key = row.getCell(0);
            Cell value = row.getCell(1);
            if (key == null || value == null) {
                continue;
            }
            String keyStr = getStringValue(key);
            String valStr = getStringValue(value);
            
            if (keyStr == null || isAdditional(keyStr)) {
            	additionalTitles.add(keyStr);
            	additionalValues.add(valStr);
            } else {
            	List<String> tmp = new ArrayList<>();
            	tmp.add(valStr);
            	map.put(keyStr, tmp);
            }
        }
        map.put(KEY_ADDITIONAL_TITLES, additionalTitles);
        map.put(KEY_ADDITIONAL_VALUES, additionalValues);
        return map;		
	}
	
	boolean isAdditional(String key)
	{
		for (String e : keys) {
			if (e.equals(key)) {
				return false;
			}
		}
		return true;
	}
	
	boolean isAuthorExist(String author) {
	    return object.get(author) != null;
	}
	
	String getPicture(String sheetName) {
		return object.get(sheetName).get(KEY_PICTURE).get(0);
	}

	String getName(String sheetName) {
		return object.get(sheetName).get(KEY_NAME).get(0);
	}
	
	String getName2(String sheetName) {
		return object.get(sheetName).get(KEY_NAME2).get(0);
	}
	
	String getOrganization(String sheetName) {
		return object.get(sheetName).get(KEY_ORGANIZATION).get(0);
	}
	
	List<String >getAdditionalTitles(String sheetName) {
		return object.get(sheetName).get(KEY_ADDITIONAL_TITLES);
	}
	
	List<String >getAdditionalValues(String sheetName) {
		return object.get(sheetName).get(KEY_ADDITIONAL_VALUES);
	}
}
