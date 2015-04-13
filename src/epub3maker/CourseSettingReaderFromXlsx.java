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
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellValue;
import org.apache.poi.ss.usermodel.CreationHelper;
import org.apache.poi.ss.usermodel.DateUtil;
import org.apache.poi.ss.usermodel.FormulaEvaluator;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

public class CourseSettingReaderFromXlsx implements CourseSettingReader {
    static public final String META_SHEET_NAME = "Meta";
    static public final String SHEETS_PREFIX = "vol";
    static public final String SHEETS_SEPARATOR = "-";
    static public final String SHHETS_PREFIX_WITH_SEPARATOR = SHEETS_PREFIX
            + SHEETS_SEPARATOR;
    protected Path filePath;
    protected List<String> pageSettingAttributeNames;
    protected List<String> pageSettingKeyNames;

public CourseSettingReaderFromXlsx(Path courseDir) throws IOException, Epub3MakerException
	{
	    DirectoryStream<Path> ds = Files.newDirectoryStream(courseDir, "{[!~][!$]}*.xlsx");
	    int i = 0;
	    for (Path p : ds) {
	        this.filePath = p;
	        System.out.println("CourseSettingReaderFromXlsx: " + p);
	        i++;
	    }
	    if (i > 1) {
            throw new Epub3MakerException("!!! TOO MANY META FILES !!!");
	    }
        if (i < 1) {
            throw new Epub3MakerException("!!! TOO LESS META FILES !!!");
        }
    }

    public Course read() throws Epub3MakerException {
        XSSFWorkbook workBook = null;
        try {
            workBook = new XSSFWorkbook(new FileInputStream(
                    this.filePath.toString()));
            return new Course(this.readMetaSheet(workBook),
                    this.readVolSheets(workBook), pageSettingAttributeNames,
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
    
    protected Map<String, String> readSheetVKey(Workbook wb, String sheetName) {
    	Sheet sheet = wb.getSheet(sheetName);
        Map<String, String> map = new HashMap<>();
        Iterator<Row> it = sheet.rowIterator();
        while (it.hasNext()) {
            Row row = it.next();
            Cell key = row.getCell(0);
            Cell value = row.getCell(1);
            if (key == null || value == null) {
                continue;
            }
            map.put(getStringValue(wb, key),
                    getStringValue(wb, value));
        }
        return map;
    }

    protected Map<String, String> readMetaSheet(Workbook workBook)
            throws FileNotFoundException, IOException {
        Map<String, String> meta = readSheetVKey(workBook, META_SHEET_NAME);
        setInputPathAbs(meta, Course.KEY_INPUT_PATH);
        setOutputPathAbs(meta, Course.KEY_OUTPUT_PATH);
        return meta;
    }

    protected void setOutputPathAbs(Map<String, String> meta, String key) {
        String pathString = meta.get(key);
        if (pathString == null) {
            pathString = "";
        }
        Path path = Paths.get(pathString);
        if (!path.isAbsolute()) {
            Path courseName = this.filePath.getParent().getFileName();
            Path outputDir = Paths.get(Config.getOutputBaseDir()).resolve(path);
            if (Util.isPublishHtml()) {
                meta.put(key, outputDir.resolve(courseName).resolve("html").normalize()
                        .toString());
            } else {
                meta.put(key, outputDir.resolve(courseName).resolve("epub3").normalize()
                        .toString());
            }
        }
    }

    protected void setInputPathAbs(Map<String, String> meta, String key) {
        String pathString = meta.get(key);
        if (pathString == null) {
            pathString = "";
        }
        Path path = Paths.get(pathString);
        if (!path.isAbsolute()) {
            meta.put(key, this.filePath.getParent().resolve(path).normalize()
                    .toString());
        }
    }

    protected Map<Integer, Volume> readVolSheets(XSSFWorkbook workBook)
            throws FileNotFoundException, IOException, Epub3MakerException {
        Map<Integer, Volume> volumes = new HashMap<>();
        final String prefix = SHHETS_PREFIX_WITH_SEPARATOR;

        int currentVolume = -1;
        Iterator<XSSFSheet> sheetsIte = workBook.iterator();
        while (sheetsIte.hasNext()) {
            Sheet sheet = sheetsIte.next();
            PageSetting setting = readPageSetting(sheet, prefix);
            if (setting == null) {
                continue;
            }

            // pageSettings.add(setting);

            if (currentVolume != setting.getVolume()) {
                volumes.put((setting.getVolume()),
                        new Volume(setting.getVolume()));

                currentVolume = setting.getVolume();
            }
            volumes.get(currentVolume).getPageSettings().add(setting);
        }
        return volumes;
    }

    protected boolean isPageSettingSheet(Sheet sheet, String prefix) {
        String sheetName = sheet.getSheetName();
        return sheetName.substring(0, prefix.length()).equals(prefix);
    }

    protected PageSetting readPageSetting(Sheet sheet, String prefix) {
        // シート名の先頭文字列が、volごとの設定シート名っぽくなかったら処理を飛ばす。Metaとかattributeとか。。
        if (!this.isPageSettingSheet(sheet, prefix)) {
            return null;
        }

        this.setPageSettingNames(sheet);

        String sheetName = sheet.getSheetName();
        final int pageNumberPos = sheetName.indexOf(SHEETS_SEPARATOR,
                prefix.length()) + 1;
        final String vol = sheetName.substring(prefix.length(), pageNumberPos
                - (SHEETS_SEPARATOR.length()));
        final String page = sheetName.substring(pageNumberPos);

        Iterator<Row> rowIte = sheet.rowIterator();

        Cell firstCell = null;
        Row firstRow = null;
        // 最初の行はヘッダなので飛ばす。
        if (rowIte.hasNext()) {
            Row row = rowIte.next();
            // 最初の列を記録しておく。
            firstCell = row.getCell(row.getFirstCellNum());
            firstRow = row;
        }

        Map<String, List<Map<String, String>>> settings = new HashMap<>();
        while (rowIte.hasNext()) {
            Row row = rowIte.next();

            Map<String, String> pageSetting = this.readPageSettingRow(row,
                    firstRow);
            if (pageSetting == null) {
                continue;
            }

            final String keyName = getStringValue(sheet.getWorkbook(),
                    row.getCell(firstCell.getColumnIndex()));
            this.putPageSettings(settings, keyName, pageSetting);
        }
        return new PageSetting(Integer.parseInt(vol), Integer.parseInt(page),
                settings);
    }

    protected Map<String, String> readPageSettingRow(Row row, Row firstRow) {
        // はじめのセルからおわりのセルまでの数(範囲)が、属性数より小さいのはおかしいので、飛ばす。
        if (row.getPhysicalNumberOfCells() < this.pageSettingAttributeNames
                .size()) {
            return null;
        }

        Iterator<Cell> cellIte = row.cellIterator();

        // 最初の列はキーなので飛ばす
        if (cellIte.hasNext()) {
            cellIte.next();
        }

        Map<String, String> pageSetting = new HashMap<>();
        while (cellIte.hasNext()) {
            Cell cell = cellIte.next();

            final String attributeName = getStringValue(row.getSheet()
                    .getWorkbook(), firstRow.getCell(cell.getColumnIndex()));
            pageSetting.put(attributeName,
                    getStringValue(row.getSheet().getWorkbook(), cell));
        }
        return pageSetting;
    }

    protected void setPageSettingNames(Sheet sheet) {
        if (this.pageSettingKeyNames == null) {
            this.pageSettingKeyNames = this.getPageSettingKeyNames(sheet);
        }

        if (this.pageSettingAttributeNames == null) {
            this.pageSettingAttributeNames = this
                    .getPageSettingAttributeNames(sheet);
        }
    }

    protected void putPageSettings(
            Map<String, List<Map<String, String>>> settings, String key,
            Map<String, String> value) {
        if (!settings.containsKey(key)) {
            settings.put(key, new ArrayList<Map<String, String>>());
        }
        settings.get(key).add(value);
    }

    protected List<String> getPageSettingKeyNames(Sheet sheet) {
        List<String> keyNames = new ArrayList<>();
        Iterator<Row> rowIte = sheet.rowIterator();

        // 一行目はヘッダなので飛ばす。
        if (rowIte.hasNext()) {
            rowIte.next();
        }

        while (rowIte.hasNext()) {
            Row row = rowIte.next();
            Iterator<Cell> cellIte = row.cellIterator();
            if (cellIte.hasNext()) {

                keyNames.add(getStringValue(row.getSheet().getWorkbook(),
                        cellIte.next()));
            }
        }
        return keyNames;
    }

    protected List<String> getPageSettingAttributeNames(Sheet sheet) {
        List<String> attributeNames = new ArrayList<>();
        Iterator<Row> rowIte = sheet.rowIterator();
        if (rowIte.hasNext()) {
            Row row = rowIte.next();
            Iterator<Cell> cellIte = row.cellIterator();

            // 一列目はヘッダなので飛ばす。
            if (cellIte.hasNext()) {
                cellIte.next();
            }

            while (cellIte.hasNext()) {
                attributeNames.add(getStringValue(row.getSheet().getWorkbook(),
                        cellIte.next()));
            }
        }
        return attributeNames;
    }

    public static String getStringValue(Workbook wb, Cell cell) {
        if (cell == null) {
            return null;
        }
        switch (cell.getCellType()) {
        case Cell.CELL_TYPE_STRING:
            return cell.getStringCellValue();
        case Cell.CELL_TYPE_NUMERIC:
            if (DateUtil.isCellDateFormatted(cell)) {
                SimpleDateFormat format = new SimpleDateFormat("yyyy'-'MM'-'dd");
                return format.format(cell.getDateCellValue());
            }
            return Double.toString(cell.getNumericCellValue());
        case Cell.CELL_TYPE_BOOLEAN:
            return Boolean.toString(cell.getBooleanCellValue());
        case Cell.CELL_TYPE_FORMULA:
            return getStringFormulaValue(cell);
        case Cell.CELL_TYPE_BLANK:
            return getStringRangeValue(wb, cell);
        default:
            System.out.println(cell.getCellType());
            return null;
        }
    }

    public static String getStringFormulaValue(Cell cell) {
        assert cell.getCellType() == Cell.CELL_TYPE_FORMULA;

        Workbook book = cell.getSheet().getWorkbook();
        CreationHelper helper = book.getCreationHelper();
        FormulaEvaluator evaluator = helper.createFormulaEvaluator();
        CellValue value = evaluator.evaluate(cell);
        switch (value.getCellType()) {
        case Cell.CELL_TYPE_STRING:
            return value.getStringValue();
        case Cell.CELL_TYPE_NUMERIC:
            return Double.toString(value.getNumberValue());
        case Cell.CELL_TYPE_BOOLEAN:
            return Boolean.toString(value.getBooleanValue());
        default:
            System.out.println(value.getCellType());
            return null;
        }
    }

    public static String getStringRangeValue(Workbook wb, Cell cell) {
        int rowIndex = cell.getRowIndex();
        int columnIndex = cell.getColumnIndex();

        Sheet sheet = cell.getSheet();
        int size = sheet.getNumMergedRegions();
        for (int i = 0; i < size; i++) {
            CellRangeAddress range = sheet.getMergedRegion(i);
            if (range.isInRange(rowIndex, columnIndex)) {
                Cell firstCell = getCell(sheet, range.getFirstRow(),
                        range.getFirstColumn()); // 左上のセルを取得
                return getStringValue(wb, firstCell);
            }
        }
        return null;
    }

    public static Cell getCell(Sheet sheet, int rowIndex, int columnIndex) {
        Row row = sheet.getRow(rowIndex);
        if (row != null) {
            Cell cell = row.getCell(columnIndex);
            return cell;
        }
        return null;
    }
}
