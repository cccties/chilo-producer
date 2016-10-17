package epub3maker;

import java.text.SimpleDateFormat;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellValue;
import org.apache.poi.ss.usermodel.CreationHelper;
import org.apache.poi.ss.usermodel.DateUtil;
import org.apache.poi.ss.usermodel.FormulaEvaluator;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

public abstract class ExcelReader {

	XSSFWorkbook workBook = null;

    public String getStringValue(Cell cell) {
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
            return getStringRangeValue(cell);
        default:
            System.out.println(cell.getCellType());
            return null;
        }
    }

    public String getStringFormulaValue(Cell cell) {
        assert cell.getCellType() == Cell.CELL_TYPE_FORMULA;

        CreationHelper helper = workBook.getCreationHelper();
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

    public String getStringRangeValue(Cell cell) {
        int rowIndex = cell.getRowIndex();
        int columnIndex = cell.getColumnIndex();

        Sheet sheet = cell.getSheet();
        int size = sheet.getNumMergedRegions();
        for (int i = 0; i < size; i++) {
            CellRangeAddress range = sheet.getMergedRegion(i);
            if (range.isInRange(rowIndex, columnIndex)) {
                Cell firstCell = getCell(sheet, range.getFirstRow(),
                        range.getFirstColumn()); // 左上のセルを取得
                return getStringValue(firstCell);
            }
        }
        return null;
    }

    public Cell getCell(Sheet sheet, int rowIndex, int columnIndex) {
        Row row = sheet.getRow(rowIndex);
        if (row != null) {
            Cell cell = row.getCell(columnIndex);
            return cell;
        }
        return null;
    }
}
