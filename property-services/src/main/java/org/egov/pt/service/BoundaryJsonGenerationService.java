package org.egov.pt.service;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;

import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.egov.pt.config.PropertyConfiguration;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import lombok.extern.slf4j.Slf4j;



@Service
@Slf4j
public class BoundaryJsonGenerationService {

	@Autowired
	private PropertyConfiguration config;

	public JsonObject createBoundaryJson(String tenantId, String moduleName ,String cityName )
	{

		log.info(" tenantId {} moduleName {} cityName {} ",tenantId,moduleName,cityName);

		String filename = "" ;
		JsonObject egovJson = new JsonObject();

		if(cityName.equalsIgnoreCase("bareilly"))
			filename = config.getBareillyBoundaryFile();
		else if(cityName.equalsIgnoreCase("moradabad"))
			filename = config.getMoradabadBoundaryFile();

		String[] acceptedHeaders = new String[] {"SR.NO","ZONE CODE","ZONE NAME","WARD CODE","WARD NAME","LOCALITY (MOHALLA) NAME","RCC or RBC Pakka House",
				"Other Pakka House","Kachha House","Empty Land"};
		//Note :- The above headers must in same order and roadwidth should be in the order of More or Equal 24 Meter Wide,More or Equal 12 Meter Wide
		//		& less,More Than 3 Meter and Less Than
		try
		{
			LinkedHashMap<String, Integer>  headersMap =  new LinkedHashMap<>();
			LinkedHashMap<String, Integer>  zonesMap =  new LinkedHashMap<>();
			LinkedHashMap<String, Integer>  wardMap =  new LinkedHashMap<>();
			LinkedHashMap<String, Integer>  skipMap =  new LinkedHashMap<>();

			FileInputStream file = new FileInputStream(new File(filename));


			org.apache.poi.ss.usermodel.Workbook workBook = null ;
			org.apache.poi.ss.usermodel.Sheet sheet = null;

			if(filename.endsWith("xls"))
			{
				workBook = new HSSFWorkbook(file);

				if(workBook!= null)
					sheet = workBook.getSheetAt(0);

			}else
			{
				workBook = new XSSFWorkbook(file);

				if(workBook!= null)
					sheet = workBook.getSheetAt(0);
			}

			if(sheet == null)
			{
				log.error(" sheet is empty ");
				return null;
			}

			//Iterate through each rows one by one
			Iterator<Row> rowIterator = sheet.iterator();

			JsonArray zonesArray =  new JsonArray();
			JsonArray wardsArray = null ;
			JsonArray locationsArray = null ;

			ArrayList<String> zoneIdList = new ArrayList<String>();
			ArrayList<String> wardIdList = new ArrayList<String>();
			int idIncrementer = 1 ;
			int localityIncrementer = 1 ;
			int wardIncrementer  = 1 ;
			boolean headerRow = false ;
			String[] headersArray = Arrays.stream(acceptedHeaders).map(e -> e.toLowerCase().replaceAll("\\s", "")).toArray(String[]::new);
			List<String> headersList = Arrays.asList(headersArray);  

			row : while (rowIterator.hasNext()) 
			{
				ArrayList<String> localityDetails = new ArrayList<String>(); 

				Row row = rowIterator.next();

				//For each row, iterate through all the columns
				Iterator<Cell> cellIterator = row.cellIterator();
				int cellNumber = 0 ;
				boolean zoneSameRow =  false ;
				boolean wardSameRow =  false ;
				headerRow = false ;
				JsonObject zoneObj = null ;
				JsonObject wardObj = null ;
				column : while (cellIterator.hasNext()) 
				{

					Cell cell = cellIterator.next();

					if(skipMap.containsValue(cellNumber))
					{
						cellNumber++;
						continue ;
					}


					//check the header details
					if(cell.getCellType() == CellType.STRING)
					{
						String header = cell.getStringCellValue().replaceAll("\\s", "")+"" ;
						if(header.equalsIgnoreCase("SR.NO"))
						{
							headersMap.put(header, cellNumber);
							headerRow = true ;
							System.out.println(" header found ");

						}

					}

					if(headerRow)
					{
						if(cell.getCellType() == CellType.STRING)
						{
							String header = cell.getStringCellValue().replaceAll("\\s", "")+"" ;
							headersMap.putIfAbsent(header, cellNumber);
							if(header.equalsIgnoreCase("ZONECODE") || header.equalsIgnoreCase("ZONENAME"))
							{
								zonesMap.put(header, cellNumber);
							}

							if( header.equalsIgnoreCase("WARDCODE") || header.equalsIgnoreCase("WARDNAME"))
							{
								wardMap.put(header, cellNumber);
							}





							if( header.equalsIgnoreCase("SR.NO") || !headersList.contains(header.toLowerCase()))
							{
								skipMap.put(header, cellNumber);
							}

						}
						cellNumber++;
						continue column ;
					}

					if(headersMap.isEmpty())
					{
						continue row;
					}

					//Checks the zone details

					if(zonesMap.containsValue(cellNumber)  )
					{
						String zoneIdFromFile = "" ;
						switch (cell.getCellType()) 
						{
						case NUMERIC:
							zoneIdFromFile = new BigDecimal(cell.getNumericCellValue()+"").intValue()+"";
							break;
						case STRING:
							zoneIdFromFile = cell.getStringCellValue()+"" ;

							break;
						}

						if(!zoneIdList.contains(zoneIdFromFile))
						{
							zoneIdList.add(zoneIdFromFile);
							if(!zoneSameRow)
							{
								zoneObj = new JsonObject();
								wardsArray =  new JsonArray();
								zoneObj.addProperty("id", idIncrementer);
								zoneObj.addProperty("boundaryNum", 1);
								zoneObj.addProperty("name", zoneIdFromFile);
								zoneObj.addProperty("localname", zoneIdFromFile);
								zoneObj.addProperty("label", "Zone");
								zoneObj.addProperty("code", "Z"+(zonesArray.size()+1));
								idIncrementer++ ;
								zoneObj.add("children", wardsArray);
								zonesArray.add(zoneObj);
								zoneSameRow = true ;
							}else
							{
								zoneObj.addProperty("zonename", zoneIdFromFile);
							}
						}   


					}


					//Checks the ward details
					if(wardMap.containsValue(cellNumber)  )
					{
						String wardIdFromFile = "" ;
						switch (cell.getCellType()) 
						{
						case NUMERIC:
							wardIdFromFile =  new BigDecimal(cell.getNumericCellValue()+"").intValue()+"";
							break;
						case STRING:
							wardIdFromFile = cell.getStringCellValue()+"";


							break;
						}

						if(!wardIdList.contains(wardIdFromFile))
						{
							wardIdList.add(wardIdFromFile);
							if(!wardSameRow)
							{
								wardObj = new JsonObject();
								locationsArray = new JsonArray();
								wardObj.addProperty("id", idIncrementer);
								wardObj.addProperty("boundaryNum", 1);
								wardObj.addProperty("name", wardIdFromFile);
								wardObj.addProperty("localname", wardIdFromFile);
								wardObj.addProperty("label", "Ward");
								wardObj.addProperty("code", "B"+wardIncrementer);
								wardIncrementer++;
								idIncrementer++ ;
								wardsArray.add(wardObj);
								wardObj.add("children", locationsArray);
								wardSameRow = true ;
							}else
							{
								wardObj.addProperty("wardname", wardIdFromFile);
							}
						}   

					}


					//Checks the locality details
					if(!wardMap.containsValue(cellNumber)   && !zonesMap.containsValue(cellNumber) && !skipMap.containsValue(cellNumber) )
					{
						String localityDetailsFromFile = "" ;
						switch (cell.getCellType()) 
						{
						case NUMERIC:
							localityDetailsFromFile = cell.getNumericCellValue()+"" ;
							break;
						case STRING:
							localityDetailsFromFile = cell.getStringCellValue()+"" ;
							break;
						}

						localityDetails.add(localityDetailsFromFile);

					}

					cellNumber++;

					//Start preparing locality Object
					if(!cellIterator.hasNext())
					{
						JsonObject locality = new JsonObject();
						JsonObject road_w_12_24 = new JsonObject();
						JsonObject road_w_l_12 = new JsonObject();
						JsonObject road_w_m_24 = new JsonObject();


						locality.addProperty("id", idIncrementer);
						locality.addProperty("boundaryNum", 1);
						locality.addProperty("name", localityDetails.get(0));
						locality.addProperty("localname", localityDetails.get(0));
						locality.addProperty("label", "Locality");
						locality.addProperty("area", "Area1");
						locality.addProperty("code", "BAR00"+localityIncrementer );
						idIncrementer++;
						localityIncrementer++;

						road_w_m_24.addProperty("rcc_rbc", localityDetails.get(1));
						road_w_m_24.addProperty("other_pucca", localityDetails.get(2));
						road_w_m_24.addProperty("kachha", localityDetails.get(3));
						road_w_m_24.addProperty("vacant_land", localityDetails.get(4));

						road_w_12_24.addProperty("rcc_rbc", localityDetails.get(5));
						road_w_12_24.addProperty("other_pucca", localityDetails.get(6));
						road_w_12_24.addProperty("kachha", localityDetails.get(7));
						road_w_12_24.addProperty("vacant_land", localityDetails.get(8));

						road_w_l_12.addProperty("rcc_rbc", localityDetails.get(9));
						road_w_l_12.addProperty("other_pucca", localityDetails.get(10));
						road_w_l_12.addProperty("kachha", localityDetails.get(11));
						road_w_l_12.addProperty("vacant_land", localityDetails.get(12));

						locality.add("road_w_l_12", road_w_l_12);
						locality.add("road_w_12_24", road_w_12_24);
						locality.add("road_w_m_24", road_w_m_24);

						locationsArray.add(locality);


					}

				}


			}

			file.close();
			workBook.close();

			JsonObject zonesArrayObj = new JsonObject();
			zonesArrayObj.addProperty("id", 1);
			zonesArrayObj.addProperty("name", cityName);
			zonesArrayObj.addProperty("localname", cityName);
			zonesArrayObj.addProperty("label", "City");
			zonesArrayObj.addProperty("code", "up.cityd");
			zonesArrayObj.add("children", zonesArray);

			JsonObject heirarchyTypeValue = new JsonObject();
			heirarchyTypeValue.addProperty("code", "REVENUE");
			heirarchyTypeValue.addProperty("name", "REVENUE");

			JsonObject heirarchyType = new JsonObject();

			heirarchyType.add("hierarchyType", heirarchyTypeValue);
			heirarchyType.add("boundary", zonesArrayObj);


			JsonArray tenantBoundaryArray = new  JsonArray();
			tenantBoundaryArray.add(heirarchyType);

			

			egovJson.addProperty("tenantId", tenantId);
			egovJson.addProperty("moduleName", moduleName);
			egovJson.add("TenantBoundary", tenantBoundaryArray);


			

			log.info("jsonOutput -- {}",egovJson );



		} 
		catch (Exception e) 
		{
			log.error(" error while creating json {}",e);
			return null ;
		}

		return egovJson;

	}

	public JsonObject generateCategoriesJson(String label)
	{
		JsonObject categoriesArrayObj = new JsonObject();
		try {
			log.info(" label {} ",label);
			String filename = config.getCategoriesFile();  
			//The only two headers accepted are given below and they should be in same order , the remaining fields are skipped
			
			String[] acceptedHeaders = new String[] {"Rate Multiplier","Description as per"};

			LinkedHashMap<String, Integer>  headersMap =  new LinkedHashMap<>();
			LinkedHashMap<String, Integer>  skipMap =  new LinkedHashMap<>();

			FileInputStream	file = new FileInputStream(new File(filename));


			//Create Workbook instance holding reference to .xls file
			org.apache.poi.ss.usermodel.Workbook workBook = null ;
			org.apache.poi.ss.usermodel.Sheet sheet = null;

			if(filename.endsWith("xls"))
			{
				workBook = new HSSFWorkbook(file);

				if(workBook!= null)
					sheet = workBook.getSheetAt(0);

			}else
			{
				workBook = new XSSFWorkbook(file);

				if(workBook!= null)
					sheet = workBook.getSheetAt(0);
			}

			if(sheet == null)
			{
				log.error(" Sheet is empty ");
				return null;
			}

			//Iterate through each rows one by one
			Iterator<Row> rowIterator = sheet.iterator();

			JsonArray categoriesArray =  new JsonArray();


			int idIncrementer = 1 ;
			int categoryIncrementer = 1 ;
			boolean headerRow = false ;
			String[] headersArray = Arrays.stream(acceptedHeaders).map(e -> e.toLowerCase().replaceAll("\\s", "")).toArray(String[]::new);
			List<String> headersList = Arrays.asList(headersArray);  
			

			row : while (rowIterator.hasNext()) 
			{

				Row row = rowIterator.next();
				ArrayList<String> categoryDetails = new ArrayList<String>(); 

				//For each row, iterate through all the columns
				Iterator<Cell> cellIterator = row.cellIterator();
				int cellNumber = 0 ;
				headerRow = false ;

				column : while (cellIterator.hasNext()) 
				{

					Cell cell = cellIterator.next();

					if(skipMap.containsValue(cellNumber))
					{
						cellNumber++;
						continue ;
					}


					//check the header details
					if(cell.getCellType() == CellType.STRING)
					{
						String header = cell.getStringCellValue().replaceAll("\\s", "")+"" ;
						if(header.equalsIgnoreCase("CategoryId"))
						{
							headersMap.put(header, cellNumber);
							headerRow = true ;
							System.out.println(" header found ");

						}

					}

					if(headerRow)
					{
						if(cell.getCellType() == CellType.STRING)
						{
							String header = cell.getStringCellValue().replaceAll("\\s", "")+"" ;
							headersMap.putIfAbsent(header, cellNumber);

							if(  !headersList.contains(header.toLowerCase()))
							{
								skipMap.put(header, cellNumber);
							}

						}
						cellNumber++;
						continue column ;
					}

					if(headersMap.isEmpty())
					{
						continue row;
					}

					//Checks the locality details
					if( !skipMap.containsValue(cellNumber) )
					{
						String categoryDetailsFromFile = "" ;
						switch (cell.getCellType()) 
						{
						case NUMERIC:
							categoryDetailsFromFile = cell.getNumericCellValue()+"" ;
							categoryDetailsFromFile = getNumber(categoryDetailsFromFile);
							break;
						case STRING:
							categoryDetailsFromFile = cell.getStringCellValue()+"" ;
							break;
						}

						categoryDetails.add(categoryDetailsFromFile);

					}

					cellNumber++;

					//Start preparing locality Object
					if(!cellIterator.hasNext())
					{
						if(categoryDetails.get(1).length()>1)
						{
							JsonObject categoryObj  = new JsonObject();

							categoryObj.addProperty("id", idIncrementer);
							categoryObj.addProperty("ratemultiplier", categoryDetails.get(0));
							categoryObj.addProperty("localname", categoryDetails.get(1));
							categoryObj.addProperty("label", label);
							categoryObj.addProperty("name", categoryDetails.get(1));
							categoryObj.addProperty("code", "CAT00"+categoryIncrementer );
							idIncrementer++;
							categoryIncrementer++;
							categoriesArray.add(categoryObj);

						}
					}



				}
			}
			file.close();
			workBook.close();

			
			categoriesArrayObj.addProperty("tenantId", "up");
			categoriesArrayObj.addProperty("moduleName", "PropertyTax");
			categoriesArrayObj.add("Categories", categoriesArray);

			
			log.info("jsonOutput -- {}",categoriesArrayObj );



		} catch (FileNotFoundException e) {
			log.error(" error while creating json file is not found {}",e);
			return null ;
		} catch (IOException e) {
			log.error(" error while creating json {}",e);
			return null ;
		}


		return categoriesArrayObj ;
	}

	private  String getNumber(String number){
		double doubleNumber = Double.parseDouble(number);
		if(Math.ceil(doubleNumber) == Math.floor(doubleNumber))
		{
			return new BigDecimal(number).intValue()+"";
		}
		
	    return number; 
	}


}
