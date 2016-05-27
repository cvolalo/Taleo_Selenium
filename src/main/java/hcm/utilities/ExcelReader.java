package hcm.utilities;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.sql.Time;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.apache.poi.ss.usermodel.DateUtil;
import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

public class ExcelReader {
	
	private XSSFWorkbook excelWorkbook;
	private XSSFSheet excelSheet;
	
	
	public String currentSheet;
	public int sheetCount = 0;
	private String excelFile;
	
	public void loadExcelFile(String path) {
		
		excelFile = path;
		try {
			FileInputStream file = new FileInputStream(path);
			excelWorkbook = new XSSFWorkbook(file);
		} catch (Exception e) {
			e.printStackTrace();
		}
		
	}
	
	public void setActiveSheet(String sheetname) {
		excelSheet = excelWorkbook.getSheet(sheetname);
	}
	
	public String getCellData(int row, int col) {
		String cellData = null;
		try {
			XSSFCell cell = excelSheet.getRow(row).getCell(col);
			if (cell.getCellType() == XSSFCell.CELL_TYPE_NUMERIC){
				if(DateUtil.isCellDateFormatted(cell)){
					Date data = cell.getDateCellValue();
					DateFormat df = new SimpleDateFormat("MM/dd/yyyy");
					cellData = df.format(data);
					return cellData;
				}
				
			}
			
				cell.setCellType(cell.CELL_TYPE_STRING);
				cellData = cell.getStringCellValue();
		}catch(NullPointerException e){
			return "";
		}
		
		return cellData;
	}
	
	public String getCellData(int row, int col, String type) {
		String cellData = null;
		try {
			XSSFCell cell = excelSheet.getRow(row).getCell(col);
			if(type.toLowerCase().contentEquals("time")){
				cell.setCellType(cell.CELL_TYPE_NUMERIC);
      	  		Date time = cell.getDateCellValue();
      	  		SimpleDateFormat timesdf = new SimpleDateFormat("HH:mm");
      	  		String timeCellData = timesdf.format(time);
      	  			return timeCellData;
			}
			if (type.toLowerCase().contentEquals("date")){
				if(DateUtil.isCellDateFormatted(cell)){
					Date data = cell.getDateCellValue();
					DateFormat df = new SimpleDateFormat("MM/dd/yyyy");
					cellData = df.format(data);
					return cellData;
				}
				
			}
			if (type.toLowerCase().contentEquals("number")){
				//if(DateUtil.isCellDateFormatted(cell)){
					cell.setCellType(cell.CELL_TYPE_NUMERIC);
					//cellData = ""+Math.floor(cell.getNumericCellValue());
					Double data = cell.getNumericCellValue();
					cellData = ""+data.intValue();
					return cellData;
				//}
				
			}
				cell.setCellType(cell.CELL_TYPE_STRING);
				cellData = cell.getStringCellValue();
		}catch(NullPointerException e){
			return "";
		}
		
		return cellData;
	}
}