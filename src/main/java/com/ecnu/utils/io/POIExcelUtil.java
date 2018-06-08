package com.ecnu.utils.io;

import com.ecnu.utils.algorithm.RailFenceUtil;
import org.apache.poi.hssf.usermodel.*;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * 利用apache POI 读取excel表格
 * @author zou yuanyuan
 */
public class POIExcelUtil {
    private static Logger logger = LoggerFactory.getLogger(POIExcelUtil.class);
    private final static String XLS = "xls";
    private final static String XLSX = "xlsx";

    public static List<String[]> readExcel(MultipartFile file) throws IOException {
        // 检查文件
        checkFile(file);
        Workbook workBook = getWorkBook(file);
        // 返回对象,每行作为一个数组，放在集合返回
        //List中存放String[],表示为excel的一行。
        ArrayList<String[]> rowList = new ArrayList<>();
        if (workBook != null) {
            for (int sheetNum = 0; sheetNum < workBook.getNumberOfSheets(); sheetNum++) {
                // 获得当前sheet工作表
                Sheet sheet = workBook.getSheetAt(sheetNum);
                if (sheet == null) {
                    continue;
                }
                // 获得当前sheet的开始行
                int firstRowNum = sheet.getFirstRowNum();
                // 获得当前sheet的结束行
                int lastRowNum = sheet.getLastRowNum();
                // 循环所有行(第一行为标题)
                for (int rowNum = firstRowNum; rowNum < lastRowNum; rowNum++) {
                    // 获得当前行
                    Row row = sheet.getRow(rowNum);
                    if (row == null) {
                        continue;
                    }
                    // 获得当前行开始的列
                    short firstCellNum = row.getFirstCellNum();
                    // 获得当前行的列数
                    int lastCellNum = row.getPhysicalNumberOfCells();
                    //把每一行的数据放在cells中
                    String[] cells = new String[row.getPhysicalNumberOfCells()];
                    // 循环当前行
                    for (int cellNum = firstCellNum; cellNum < lastCellNum; cellNum++) {
                        Cell cell = row.getCell(cellNum);
                        cells[cellNum] = getCellValue(cell);
                    }
                    rowList.add(cells);
                }
            }
        }
        return rowList;
    }

    /**
     * HSSFWorkbook:是操作Excel2003以前（包括2003）的版本，扩展名是.xls
     * XSSFWorkbook:是操作Excel 2007/2010之后的版本，扩展名是.xlsx
     * 创建对应版本的excel文件并往excel写数据。
     * @param filePath 文件全路径
     * @param allRecords 数据
     * @param fields 标题行数据
     * @throws IOException
     */
    public static void writeExcel(String filePath, List<String[]> allRecords, List<String> fields) throws IOException{
        File excelFile = new File(filePath);
        // 第一步，创建一个workbook，对应一个Excel文件
        Workbook wb = getWorkBook(excelFile);
        // 第二步，在workbook中添加一个sheet,对应Excel文件中的sheet
        Sheet sheet = wb.createSheet("maskedDataSheet");
        // 第三步，在sheet中添加表头第0行,注意老版本poi对Excel的行数列数有限制short
        Row row = sheet.createRow(0);
        // 第四步，创建单元格，并设置值表头 设置表头居中
        CellStyle style = wb.createCellStyle();
        style.setAlignment(HSSFCellStyle.ALIGN_CENTER);
        // 写入标题行
        int fieldSize = fields.size();
        for (int i = 0; i < fieldSize; i++) {
            Cell cell = row.createCell(i);
            cell.setCellValue(fields.get(i));
            cell.setCellStyle(style);
        }
        // 写入每行
        int size = allRecords.size();
        for (int i = 0; i < size; i++) {
            String[] rowData = allRecords.get(i);
            // 新行
            row = sheet.createRow(i + 1);
            // 每列
            int rowSize = rowData.length;
            for (int j = 0; j < rowSize; j++) {
                Cell cell = row.createCell(j);
                cell.setCellValue(rowData[j]);
                cell.setCellStyle(style);
            }
        }

        // 第六步，将文件存到指定位置
        FileOutputStream fos = new FileOutputStream(filePath);
        wb.write(fos);
        fos.close();
    }

    /**
     * 获得工作簿对象
     */
    private static Workbook getWorkBook(File file) throws IOException {
        String filename = file.getName();
        Workbook workbook = null;
        if (filename.endsWith(XLS)) {
            // 2003
            workbook = new HSSFWorkbook();
        } else if (filename.endsWith(XLSX)) {
            // 2007
            workbook = new XSSFWorkbook();
        }
        return workbook;
    }

    /**
     * 取单元格的值
     */
    private static String getCellValue(Cell cell) {
        String cellValue = "";
        if (cell == null) {
            return cellValue;
        }
        // 把数字当成String来读，防止1读成1.0
        if (cell.getCellType() == Cell.CELL_TYPE_NUMERIC) {
            cell.setCellType(Cell.CELL_TYPE_STRING);
        }
        // 判断数据的类型
        switch (cell.getCellType()) {
            // 数字
            case Cell.CELL_TYPE_NUMERIC:
                cellValue = String.valueOf(cell.getNumericCellValue());
                break;
            // 字符串
            case Cell.CELL_TYPE_STRING:
                cellValue = String.valueOf(cell.getStringCellValue());
                break;
            // 布尔
            case Cell.CELL_TYPE_BOOLEAN:
                cellValue = String.valueOf(cell.getBooleanCellValue());
                break;
            // 公式
            case Cell.CELL_TYPE_FORMULA:
                cellValue = String.valueOf(cell.getCellFormula());
                break;
            // 空
            case Cell.CELL_TYPE_BLANK:
                cellValue = "";
                break;
            // 错误
            case Cell.CELL_TYPE_ERROR:
                cellValue = "非法字符";
                break;
            default:
                cellValue = "未知类型";
                break;
        }
        return cellValue;
    }

    /**
     * 获得工作簿对象
     */
    private static Workbook getWorkBook(MultipartFile file) {
        String filename = file.getOriginalFilename();
        Workbook workbook = null;
        try {
            InputStream is = file.getInputStream();
            if (filename.endsWith(XLS)) {
                // 2003
                workbook = new HSSFWorkbook(is);
            } else if (filename.endsWith(XLSX)) {
                // 2007
                workbook = new XSSFWorkbook(is);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return workbook;
    }

    /**
     * 检查文件
     */
    private static void checkFile(MultipartFile file) throws IOException {
        if (file == null) {
            logger.error("文件不存在");
            throw new FileNotFoundException("文件不存在！");
        }
        // 获取文件名
        String filename = file.getOriginalFilename();
        // 判断是否为excel文件
        if (!filename.endsWith(XLS) && !filename.endsWith(XLSX)) {
            logger.error(filename + "不是excel文件");
            throw new IOException(filename + "不是excel文件");
        }
    }

}

