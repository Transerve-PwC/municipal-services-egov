package org.egov.pt.service;

import com.monitorjbl.xlsx.StreamingReader;

import org.apache.poi.EncryptedDocumentException;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.egov.pt.config.PropertyConfiguration;
import org.egov.pt.models.excel.LegacyRow;
import org.egov.pt.models.excel.RowExcel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.function.Function;

@Service
public class ExcelService {
	
	 @Autowired
	    private PropertyConfiguration config;
	
	final String[]  headersArray = new String[]{"ULB Code","ULB Name","Financial Year","PTIN","Owner Name","Father/Husband Name","House No","Locality","Tax Ward","Ward No","Zone","Property Type Classification","Residential/Commercial ARV","ArrearHouseTax","ArrearWaterTax","ArrearSewerTax","HouseTax","WaterTax","SewerTax",
											"SurchareHouseTax","SurchareWaterTax","SurchareSewerTax","Bill Generated Total","Total Paid Amount","Last Payment Date","Address","Mobile","Building Usage","WardName","PlotArea","TotalCarpetArea","ConstructionYear"};


    public void read(InputStream is, Long skip, Long limit, Function<RowExcel, Boolean> func) throws Exception{
        Workbook workbook = StreamingReader.builder().rowCacheSize(100).bufferSize(4096).open(is);
        for (Sheet sheet: workbook) {
            Map<Integer, String> headerMap = new HashMap<Integer, String>();
            Iterator<Row> itr =  sheet.rowIterator();
            Row r = itr.next();
            for (Cell cell: r) {
                headerMap.put(cell.getColumnIndex(), cell.getStringCellValue());
            }

            while(itr.hasNext()){
                while ( (skip != null && r.getRowNum()+2 <=skip)) { if(itr.hasNext()) r= itr.next();}

                if(itr.hasNext()) r = itr.next();

                if(r.getPhysicalNumberOfCells() <= 0) break;
                Map<Integer, Cell> cellMap = new HashMap<Integer, Cell>();
                for (Cell cell: r) {
                    cellMap.put(cell.getColumnIndex(),cell);
                }
                if(r.getRowNum() > 0) func.apply(RowExcel.builder().rowIndex(r.getRowNum()+1).cells(cellMap).header(headerMap).build());

                if(limit != null && r.getRowNum()+2 >= limit) break;
            }
        }
        workbook.close();
    }
    
    public void writeFailedRecords(ArrayList<LegacyRow>  failedRecordsList)
    {
    	try {
    		File f = new File(config.getFailedRecordsMigrationFilePath());
    		
    		if(f.exists()){
    			FileInputStream inputStream = new FileInputStream(new File(config.getFailedRecordsMigrationFilePath()));
    			 Workbook workbook = null;
				try {
					workbook = WorkbookFactory.create(inputStream);
					Sheet sheet = workbook.getSheetAt(0);
					int rowCount = sheet.getLastRowNum();
					
					for (LegacyRow legacyrow : failedRecordsList) {
	    				Row row = sheet.createRow(++rowCount);

	    				int columnCount = 0;

	    				Field[] fields = legacyrow.getClass().getDeclaredFields();

	    				for (Field field : fields) {
	    					Cell cell = row.createCell(columnCount++);
	    					try {
	    						field.setAccessible(true);
	    						cell.setCellValue(field.get(legacyrow)==null?"":field.get(legacyrow).toString());
	    					} catch (IllegalArgumentException | IllegalAccessException 
	    							| SecurityException e) {
	    						e.printStackTrace();
	    					}
	    				}
	    			}
					
					
					try (FileOutputStream 	outputStream = new FileOutputStream(config.getFailedRecordsMigrationFilePath())) {
						workbook.write(outputStream);
					}catch (IOException e) {
	    				e.printStackTrace();
	    			}
		            
		            
		            
				} catch (EncryptedDocumentException | IOException e1) {
					
					e1.printStackTrace();
				}finally {
					try {
						if(inputStream!=null)
						inputStream.close();
						
						if(workbook != null)
						workbook.close();
					
					} catch (IOException e) {
						e.printStackTrace();
					}
					
				}
    	            
    	       
    		}else
    		{

    			XSSFWorkbook workbook = new XSSFWorkbook();
    			XSSFSheet sheet = workbook.createSheet("Sheet 1");

    			int rowCount = 0;

    			Row headerRow = sheet.createRow(rowCount++);
    			int headerColumnCount = 0;
    			for (String header : headersArray) {
    				Cell cell = headerRow.createCell(headerColumnCount++);
    				cell.setCellValue(header);
    			}


    			for (LegacyRow legacyrow : failedRecordsList) {
    				Row row = sheet.createRow(rowCount++);

    				int columnCount = 0;

    				Field[] fields = legacyrow.getClass().getDeclaredFields();

    				for (Field field : fields) {
    					Cell cell = row.createCell(columnCount++);
    					try {
    						field.setAccessible(true);
    						cell.setCellValue(field.get(legacyrow)==null?"":field.get(legacyrow).toString());
    					} catch (IllegalArgumentException | IllegalAccessException 
    							| SecurityException e) {
    						e.printStackTrace();
    					}
    				}
    			}
    			try (FileOutputStream newFileoutputStream = new FileOutputStream(config.getFailedRecordsMigrationFilePath())) {
    				workbook.write(newFileoutputStream);
    				
    			} catch (IOException e) {
    				e.printStackTrace();
    			}finally {
    				try {
    					if(workbook != null)
						workbook.close();
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
    		}
    	} catch (FileNotFoundException e) {
    		e.printStackTrace();
    	}
    }
    
   
}
