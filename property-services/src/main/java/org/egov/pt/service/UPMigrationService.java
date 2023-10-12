package org.egov.pt.service;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

import org.egov.common.contract.request.RequestInfo;
import org.egov.common.contract.request.Role;
import org.egov.pt.config.PropertyConfiguration;
import org.egov.pt.models.Assessment;
import org.egov.pt.models.Assessment.Source;
import org.egov.pt.models.OwnerInfo;
import org.egov.pt.models.PropertyCriteria;
import org.egov.pt.models.enums.Channel;
import org.egov.pt.models.enums.Status;
import org.egov.pt.models.excel.Address;
import org.egov.pt.models.excel.LegacyRow;
import org.egov.pt.models.excel.Owner;
import org.egov.pt.models.excel.PropertyPayment;
import org.egov.pt.models.excel.RowExcel;
import org.egov.pt.models.excel.Unit;
import org.egov.pt.models.user.User;
import org.egov.pt.models.user.UserDetailResponse;
import org.egov.pt.repository.AddressExcelRepository;
import org.egov.pt.repository.OwnerExcelRepository;
import org.egov.pt.repository.PropertyExcelRepository;
import org.egov.pt.repository.PropertyPaymentExcelRepository;
import org.egov.pt.repository.PropertyRepository;
import org.egov.pt.repository.ServiceRequestRepository;
import org.egov.pt.repository.UnitExcelRepository;
import org.egov.pt.repository.rowmapper.LegacyExcelRowMapper;
import org.egov.pt.util.PTConstants;
import org.egov.pt.util.PropertyUtil;
import org.egov.pt.web.contracts.AssessmentRequest;
import org.egov.pt.web.controllers.PropertyController;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class UPMigrationService {

    @Autowired
    private PropertyUtil propertyutil;

    @Autowired
    private PropertyConfiguration config;

    @Autowired
    private ExcelService excelService;

    @Autowired
    private OwnerExcelRepository ownerExcelRepository;

    @Autowired
    private PropertyExcelRepository propertyExcelRepository;

    @Autowired
    private UnitExcelRepository unitExcelRepository;

    @Autowired
    private LegacyExcelRowMapper legacyExcelRowMapper;

    @Autowired
    private AddressExcelRepository addressExcelRepository;

    @Autowired
    private PropertyPaymentExcelRepository propertyPaymentExcelRepository;

    @Autowired
    private UserService userService;

    @Autowired
    private ServiceRequestRepository restRepo;
    
    @Autowired
    private Cachebaleservice cachebaleservice ;
    
    @Autowired
    private PropertyRepository propertyRepository;
    
    @Autowired
	private AssessmentService assessmentService;
   

    private static final RequestInfo userCreateRequestInfo = RequestInfo.builder().action("_create").apiId("Rainmaker")
            .did("1").key("").msgId("20170310130900|en_IN").ver(".01").build();

    private synchronized User  createUserIfNotExists(LegacyRow legacyRow, HashMap<String, User> existingUser) {
        final String tenantId = "up." + legacyRow.getULBName().toLowerCase();
        User userRequest = new User();
        userRequest.setActive(true);
        userRequest.setMobileNumber((legacyRow.getMobile() != null && legacyRow.getMobile() != ""
                && (new BigDecimal(legacyRow.getMobile()).longValue() != 0)) ? legacyRow.getMobile()
                        : convertPtUniqueIdToMobileNumber(legacyRow.getPtmsPropertyId()));
        userRequest.setUserName(userRequest.getMobileNumber());
        userRequest.setPassword("123456");
        userRequest.setFatherOrHusbandName(legacyRow.getFHName() != null && legacyRow.getFHName().length() > 100
                ? legacyRow.getFHName().substring(0, 99)
                : legacyRow.getFHName());
        userRequest.setType("CITIZEN");
        userRequest.setTenantId(tenantId);
        userRequest.setPermanentCity(tenantId);
        userRequest.setPermanentAddress(legacyRow.getAddress());
        userRequest.setRoles(Arrays.asList(Role.builder().code("CITIZEN").build()));
        String name = legacyRow.getOwnerName() != null && legacyRow.getOwnerName() != "" ? legacyRow.getOwnerName()
                : "Owner of " + legacyRow.getPTIN();
        // Invalid name. Only alphabets and special characters -, ',`, .
        name = name.replaceAll("[\\$\"'<>?\\\\~`!@#$%^()+={}\\[\\]*,.:;“”‘’]*", "");
        name = name.length() > 100 ? name.substring(0, 99) : name;

        userRequest.setName(name);
        userRequest.setCorrespondenceCity(tenantId);
        userRequest.setCorrespondenceAddress(legacyRow.getAddress());
        
		
		  OwnerInfo owner = new OwnerInfo();
		  owner.setMobileNumber(userRequest.getMobileNumber());
		  owner.setName(name);
		  owner.setType("CITIZEN"); 
		  owner.setTenantId("up");
		  
		  RequestInfo userCreateRequestInfo1 =
		  RequestInfo.builder().action("").apiId("Rainmaker")
		  .did("1").key("").msgId("20170310130900|en_IN").ver(".01").build();
		  

		  User user = null;
		  
		  if(existingUser.containsKey(userRequest.getMobileNumber().trim()+userRequest.getName().trim()) && (existingUser.get(userRequest.getMobileNumber().trim()+userRequest.getName().trim()) != null))
		  {
			   user = existingUser.get(userRequest.getMobileNumber().trim()+userRequest.getName().trim());
		  }
		  else {
			  UserDetailResponse userDetailResponse = userService.userExists(owner,userCreateRequestInfo1);

			  if(userDetailResponse.getUser().size() > 0)
			  {
				  user =  userDetailResponse.getUser().get(0);
			  }
		  }

		  if(user == null)
		  {
			  UserDetailResponse userDetailResponse1 = null;
			try {
				userDetailResponse1 = userService.createUser(userCreateRequestInfo, userRequest);
			} catch (Exception e) {
				log.error(" duplicate phone number hence creating duplicate user for mobilenumber {}",userRequest.getUserName() );
				 userRequest.setUserName(UUID.randomUUID().toString());
				 log.error(legacyRow.toString());
				userDetailResponse1 = userService.createUser(userCreateRequestInfo, userRequest);
			} 
			  user = userDetailResponse1.getUser().get(0);
		  }
		  existingUser.put(userRequest.getMobileNumber().trim()+userRequest.getName().trim(), user);

        return user;
    }

    public void importProperties(InputStream file, InputStream matchedFile, Long skip, Long limit) throws Exception {

        // Build matched usage type map
        BufferedReader br = new BufferedReader(new InputStreamReader(matchedFile));
        String line = "";
        Map<String, String> matched = new HashMap<>();
        while ((line = br.readLine()) != null) {
            String[] values = line.split(",");
            matched.put(values[0], values[1]);
        }

        AtomicInteger numOfSuccess = new AtomicInteger();
        AtomicInteger numOfErrors = new AtomicInteger();
        Set<Integer> skippedRows = new HashSet<>();
        
        Set<String> duplicateMobileNumbers = new HashSet<>();
        HashMap<String, User>  existingUser = new HashMap<String, User>();
        final ClassLoader loader = PropertyController.class.getClassLoader();

        final InputStream excelFile = loader.getResourceAsStream(config.getMigrationFileName());
        
        List<String> PTMSUIdList = fetchPtmsUidFromDB();
        excelService.read(excelFile, skip, limit, (RowExcel row) -> {
        	 LegacyRow legacyRow = null;
             
        	 try {
        		 legacyRow = legacyExcelRowMapper.map(row);
        		 if(PTMSUIdList.contains(legacyRow.getPtmsPropertyId())) {
        			 return true;
        		 }

        		 String name = legacyRow.getOwnerName() != null && legacyRow.getOwnerName() != "" ? legacyRow.getOwnerName()
        				 : "Owner of " + legacyRow.getPTIN();
        		 // Invalid name. Only alphabets and special characters -, ',`, .
        		 name = name.replaceAll("[\\$\"'<>?\\\\~`!@#$%^()+={}\\[\\]*,.:;“”‘’]*", "");
        		 name = name.length() > 100 ? name.substring(0, 99) : name;
        		 
        		 String tenantId = "up." + legacyRow.getULBName().toLowerCase();
                 checkIfSaharanpurAndHandleExponentialMobile(tenantId, legacyRow);

        		 if(legacyRow.getMobile()!= null)
        		 {
        			 if(!duplicateMobileNumbers.add(legacyRow.getMobile().trim()+name.trim()))
        			 {
        				 existingUser.put(legacyRow.getMobile().trim()+name.trim(), null);
        			 }
        		 }
        	 } catch (Exception e) {
					e.printStackTrace();
				}
               
                 return true;
        });
        
        
        log.info("  existingUsers size {}",existingUser.size());
        
        existingUser.forEach((key, value) -> {log.info("Key: {}",  key );});
        
        excelService.read(file, skip, limit, (RowExcel row) -> {
        	int failedCode = 0 ;
            LegacyRow legacyRow = null;
            org.egov.pt.models.excel.Property property = new org.egov.pt.models.excel.Property();
            Owner owner = new Owner();
            Unit unit = new Unit();
            Address address = new Address();
            PropertyPayment payment = new PropertyPayment();
            try {
                legacyRow = legacyExcelRowMapper.map(row);
                
                if(PTMSUIdList.contains(legacyRow.getPtmsPropertyId())) {
                	log.info("property with PTMS unique ID {} is already present",legacyRow.getPtmsPropertyId());
                	numOfErrors.getAndIncrement();
                    skippedRows.add(row.getRowIndex());
                	return true;
       		 	}
                
                String tenantId = "up." + legacyRow.getULBName().toLowerCase();
                checkIfSaharanpurAndHandleExponentialMobile(tenantId, legacyRow);

                // Create user if not exists in db.
                User user = this.createUserIfNotExists(legacyRow , existingUser);

				
				  String token = cachebaleservice.getUserToken(tenantId, user);
				 

                RequestInfo requestinfo = RequestInfo.builder().authToken(token).action("token").apiId("Rainmaker")
                        .did("1").key("").msgId("20170310130900|en_IN").ver(".01")
                        .userInfo(org.egov.common.contract.request.User.builder().type(user.getType()).tenantId("up")
                                .userName(user.getUserName()).build())
                        .build();

                Map<String, String> localityMap = cachebaleservice.getLocalityMap(tenantId, requestinfo);
                log.info("*************1:"+legacyRow.getLocality().trim().toLowerCase());
                log.info("*************2:"+localityMap.get(legacyRow.getLocality().trim().toLowerCase()));
                String localityCode = localityMap.get(legacyRow.getLocality().trim().toLowerCase());
                if (localityCode == null) {
                    log.warn("Empty locality code for the property {}", legacyRow.getLocality());
                }
                
                if(tenantId.equalsIgnoreCase("up.Bareilly") || tenantId.equalsIgnoreCase("up.saharanpur"))
                {
              	  HashMap<String,HashMap<String,HashMap<String, String>>> duplicateMap = cachebaleservice.getDuplicateLocalitiesMap(tenantId, requestinfo);
              	  log.info("*******************duplicateLocalityMap:"+duplicateMap);
              	  if(duplicateMap.containsKey(legacyRow.getLocality().trim().toLowerCase()))
              	  {
              		  log.warn("Locality code for the property {} was found from duplicateLocalityMap : {}", legacyRow.getLocality(),duplicateMap.get(legacyRow.getLocality().trim().toLowerCase()));
              		  String zone = legacyRow.getZone();
              		  String wardNo = legacyRow.getWardNo();
              		
              		  wardNo = wardNo.replace(".0", "");
  					  if (Objects.nonNull(zone) && !tenantId.equalsIgnoreCase("up.saharanpur"))
  							zone = zone.replaceAll("Zone-", "");
              		  localityCode = duplicateMap.get(legacyRow.getLocality().trim().toLowerCase()).get(zone).get(wardNo);
              	  }            	  
              	  
                }

                // Generate unique property id and acknowledgement no
//                String pId = propertyutil.getIdList(requestinfo, tenantId, config.getPropertyIdGenName(),
//                        config.getPropertyIdGenFormat(), 1).get(0);
                String ackNo = propertyutil
                        .getIdList(requestinfo, tenantId, config.getAckIdGenName(), config.getAckIdGenFormat(), 1)
                        .get(0);
              
                property.setId(UUID.randomUUID().toString());
                property.setPropertyid(legacyRow.getPtmsPropertyId());
                property.setTenantid(tenantId);
                property.setAccountid(user.getUuid());
                property.setStatus(config.getWfStatusActive());
                property.setAcknowldgementnumber(ackNo);
                property.setPropertytype(PTConstants.PT_TYPE_BUILTUP);
                property.setOwnershipcategory("INDIVIDUAL.SINGLEOWNER");
                if (matched.containsKey(legacyRow.getPropertyTypeClassification())) {
                    property.setUsagecategory(matched.get(legacyRow.getPropertyTypeClassification()));
                } else {
                    property.setUsagecategory("OTHERS");
                }
                property.setNooffloors(1L);
                property.setLandarea(BigDecimal
                        .valueOf(Double.valueOf(legacyRow.getPlotArea() != null && !legacyRow.getPlotArea().isEmpty() ? legacyRow.getPlotArea() : "0")));
                property.setOldpropertyid(legacyRow.getPTIN().matches("nomatch|null")?null:legacyRow.getPTIN().toString());
                property.setSource("DATA_MIGRATION");
                property.setChannel(Channel.MIGRATION.toString());
                property.setConstructionyear(legacyRow.getConstructionYear());
                property.setCreatedby(user.getUuid());
                property.setLastmodifiedby(user.getUuid());
                property.setCreatedtime(new Date().getTime());
                property.setLastmodifiedtime(new Date().getTime());
                property.setHouseTax(BigDecimal
                        .valueOf(Double.valueOf(legacyRow.getHouseTax()!= null && !legacyRow.getHouseTax().isEmpty() ? legacyRow.getHouseTax() : "0")));
                property.setWaterTax(BigDecimal
                		.valueOf(Double.valueOf(legacyRow.getWaterTax()!= null && !legacyRow.getWaterTax().isEmpty() ? legacyRow.getWaterTax() : "0")));
                property.setSewerTax(BigDecimal
                		.valueOf(Double.valueOf(legacyRow.getSewerTax()!= null && !legacyRow.getSewerTax().isEmpty() ? legacyRow.getSewerTax() : "0")));
               
                property.setPropertyIDPTMS(legacyRow.getPtmsPropertyId());
                
                propertyExcelRepository.save(property);
                failedCode = 1;

               
                owner.setOwnerinfouuid(UUID.randomUUID().toString());
                owner.setStatus(Status.ACTIVE.toString());
                owner.setTenantid(tenantId);
                owner.setPropertyid(property.getId());
                owner.setUserid(user.getUuid());
                owner.setOwnertype("NONE");
                owner.setRelationship("FATHER");
                owner.setCreatedby(user.getUuid());
                owner.setLastmodifiedby(user.getUuid());
                owner.setCreatedtime(new Date().getTime());
                owner.setLastmodifiedtime(new Date().getTime());
                ownerExcelRepository.save(owner);
                failedCode = 2;
               
                unit.setId(UUID.randomUUID().toString());
                unit.setTenantid(tenantId);
                unit.setPropertyid(property.getId());
                unit.setFloorno(1L);
                if (matched.containsKey(legacyRow.getPropertyTypeClassification())) {
                    unit.setUnittype(matched.get(legacyRow.getPropertyTypeClassification()));
                    unit.setUsagecategory(matched.get(legacyRow.getPropertyTypeClassification()));
                } else {
                    unit.setUnittype("OTHERS");
                    unit.setUsagecategory("OTHERS");
                }

                unit.setOccupancytype("SELFOCCUPIED");
                unit.setOccupancydate(0L);
                unit.setCarpetarea(BigDecimal.valueOf(
                        Double.valueOf(legacyRow.getTotalCarpetArea() != null && !legacyRow.getTotalCarpetArea().isEmpty() ? legacyRow.getTotalCarpetArea() : "0")));

                // unit.setBuiltuparea(BigDecimal builtuparea)
                // unit.setPlintharea(BigDecimal plintharea)
                // unit.setSuperbuiltuparea(BigDecimal superbuiltuparea)
                unit.setArv(BigDecimal
                        .valueOf(Double.parseDouble(legacyRow.getRCARV() != null && !legacyRow.getRCARV().isEmpty() ? legacyRow.getRCARV() : "0")));
                unit.setConstructiontype("PUCCA");
                // unit.setConstructiondate(Long constructiondate)
                // unit.setDimensions(String dimensions)
                unit.setActive(true);
                unit.setCreatedby(user.getUuid());
                unit.setLastmodifiedby(user.getUuid());
                unit.setCreatedtime(new Date().getTime());
                unit.setLastmodifiedtime(new Date().getTime());
                unitExcelRepository.save(unit);
                failedCode = 3;

                
                address.setTenantid(tenantId);
                address.setId(UUID.randomUUID().toString());
                address.setPropertyid(property.getId());
                address.setDoorno(legacyRow.getHouseNo());
                // address.setPlotno(String plotno)
                // address.setBuildingname(String buildingname)
                address.setStreet(legacyRow.getAddress());
                // address.setLandmark(String landmark)
                address.setCity(legacyRow.getULBName());
                address.setPincode("123456");
                address.setLocality(localityCode);
                // address.setLocality(legacyRow.getLocality() != null? legacyRow.getLocality():
                // "OTHERS");
                address.setDistrict(legacyRow.getULBName());
                // address.setRegion(String region)
                address.setState("Uttar Pradesh");
                address.setCountry("India");
                // address.setLatitude(BigDecimal latitude)
                // address.setLongitude(BigDecimal longitude)
                address.setCreatedby(user.getUuid());
                address.setLastmodifiedby(user.getUuid());
                address.setCreatedtime(new Date().getTime());
                address.setLastmodifiedtime(new Date().getTime());
                address.setTaxward(legacyRow.getTaxWard());
                address.setWardname(legacyRow.getWardName());
                Map<String, String>  wardsMap = cachebaleservice.getWardMap(tenantId, requestinfo);
                Map<String, String> zonesMap = cachebaleservice.getZoneMap(tenantId, requestinfo);
               
                String zoneCode = zonesMap.get(localityCode);
                String wardCode = wardsMap.get(localityCode);
                
                if (zoneCode == null) {
                    log.warn("Empty zones code for the property {}", legacyRow.getLocality());
                }
                
                if (wardsMap == null) {
                    log.warn("Empty wards code for the property {}", legacyRow.getLocality());
                }
                
                address.setWardno(wardCode);
                address.setZone(zoneCode);
                addressExcelRepository.save(address);
                failedCode = 4;
                // address.setAdditionaldetails(String additionaldetails)

                
                payment.setId(UUID.randomUUID().toString());
                payment.setPropertyid(property.getId());
                payment.setFinancialyear(legacyRow.getFinancialYear());
                payment.setArrearhousetax(BigDecimal.valueOf(
                        Double.valueOf(legacyRow.getArrearHouseTax() != null && !legacyRow.getArrearHouseTax().isEmpty() ? legacyRow.getArrearHouseTax() : "0")));
                payment.setArrearwatertax(BigDecimal.valueOf(
                        Double.valueOf(legacyRow.getArrearWaterTax() != null && !legacyRow.getArrearWaterTax().isEmpty() ? legacyRow.getArrearWaterTax() : "0")));
                payment.setArrearsewertax(BigDecimal.valueOf(
                        Double.valueOf(legacyRow.getArrearSewerTax() != null  &&  !legacyRow.getArrearSewerTax().isEmpty() ? legacyRow.getArrearSewerTax() : "0")));
                payment.setHousetax(BigDecimal
                        .valueOf(Double.valueOf(legacyRow.getHouseTax() != null && !legacyRow.getHouseTax().isEmpty() ? legacyRow.getHouseTax() : "0")));
                payment.setWatertax(BigDecimal
                        .valueOf(Double.valueOf(legacyRow.getWaterTax() != null && !legacyRow.getWaterTax().isEmpty() ?  legacyRow.getWaterTax() : "0")));
                payment.setSewertax(BigDecimal
                        .valueOf(Double.valueOf(legacyRow.getSewerTax() != null && !legacyRow.getSewerTax().isEmpty() ? legacyRow.getSewerTax() : "0")));
                payment.setSurcharehousetax(BigDecimal.valueOf(Double
                        .valueOf(legacyRow.getSurchareHouseTax() != null  && !legacyRow.getSurchareHouseTax().isEmpty() ? legacyRow.getSurchareHouseTax() : "0")));
                payment.setSurcharewatertax(BigDecimal.valueOf(Double
                        .valueOf(legacyRow.getSurchareWaterTax() != null  && !legacyRow.getSurchareWaterTax().isEmpty() ? legacyRow.getSurchareWaterTax() : "0")));
                payment.setSurcharesewertax(BigDecimal.valueOf(Double
                        .valueOf(legacyRow.getSurchareSewerTax() != null && !legacyRow.getSurchareSewerTax().isEmpty() ? legacyRow.getSurchareSewerTax() : "0")));
                payment.setBillgeneratedtotal(BigDecimal.valueOf(Double
                        .valueOf(legacyRow.getBillGeneratedTotal() != null && !legacyRow.getBillGeneratedTotal().isEmpty() ? legacyRow.getBillGeneratedTotal() : "0")));
                payment.setTotalpaidamount(BigDecimal.valueOf(
                        Double.valueOf(legacyRow.getTotalPaidAmount() != null && !legacyRow.getTotalPaidAmount().isEmpty() ? legacyRow.getTotalPaidAmount() : "0")));

                payment.setLastpaymentdate(legacyRow.getLastPaymentDate());
                propertyPaymentExcelRepository.save(payment);
                failedCode = 5;
                
        //Property Assessment         
          
                RequestInfo AssessmentRequestinfo = RequestInfo.builder().authToken(token).action("token").apiId("Rainmaker")
                        .did("1").key("").msgId("20170310130900|en_IN").ver(".01")
                        .userInfo(org.egov.common.contract.request.User.builder().type(user.getType()).tenantId("up")
                                .userName(user.getUserName()).uuid(user.getUuid()).roles(new ArrayList<>(Arrays.asList(Role.builder()
                        				.code("CITIZEN").name("Citizen").build()))).build()).build();           
                
                Assessment assessment = Assessment.builder().tenantId(tenantId).propertyId(property.getPropertyid()).source(Source.MUNICIPAL_RECORDS)
                		.channel(Channel.CFC_COUNTER).assessmentDate(new Timestamp(System.currentTimeMillis()).getTime())
                		.financialYear(legacyRow.getFinancialYear()).build();
                
                AssessmentRequest assessmentReq = AssessmentRequest.builder().requestInfo(AssessmentRequestinfo)
                		.assessment(assessment).build();
                
                Assessment assessmentRes = assessmentService.createAssessment(assessmentReq);
                
                if(assessmentRes !=null)
                	log.info("Assessment done for property:"+property.getPropertyid());
                else
                	log.info("Assessment failed for property:"+property.getPropertyid());
                
                numOfSuccess.getAndIncrement();
            } catch (Exception e) {
                numOfErrors.getAndIncrement();
                skippedRows.add(row.getRowIndex());
//FaieldCodes 1 = failed at owner insertion , 2 = failed at unit insertion , 3 = failed at address insertion , 4 = failed at payment insertion
                if( failedCode == 1)
                {
                	propertyExcelRepository.delete(property);
                }else if( failedCode == 2)
                {
                	ownerExcelRepository.delete(owner);
                	propertyExcelRepository.delete(property);
                }else if( failedCode == 3)
                {
                	unitExcelRepository.delete(unit);
                	ownerExcelRepository.delete(owner);
                	propertyExcelRepository.delete(property);
                }else if( failedCode == 4 || failedCode == 5)
                {
                	addressExcelRepository.delete(address);
                	unitExcelRepository.delete(unit);
                	ownerExcelRepository.delete(owner);
                	propertyExcelRepository.delete(property);
                }
                
                
				/*
				 * if(numOfErrors.get() == 1) { excelService.createFailedRecordsFile(); }
				 * 
				 * excelService.writeFailedRecords(legacyRow);
				 */
                
                
                log.info("Row[{}] - [{}] , errorMessage: {}", row.getRowIndex(), legacyRow.toString(), e.getMessage());
            }
            return true;
        });
        log.info("Import Completed - Success={} Errors={} SkippedRows={}", numOfSuccess, numOfErrors,skippedRows);
//        excelService.writeToFileandClose();
      
    }
    
 
    private List<String> fetchPtmsUidFromDB() {
    	List<String>ptmsIds=propertyRepository.getPtmsIds();
    	
		return ptmsIds;
	}

	private String convertPTINToMobileNumber(String ptin) {
        String curPtin = ptin;
        while (curPtin.length() < 9) {
            curPtin = "0" + curPtin;
        }
        return "5" + curPtin;
    }
    
    private String convertPtUniqueIdToMobileNumber(String ptUniqueId) {
    	String ptUId = ptUniqueId;
    	if(ptUId.length()>=17)
    		return ptUId.substring(6, 16);
    	else {
    		 while (ptUId.length() < 9) {
    			 ptUId = "0" + ptUId;
    	        }
    		 return "5" + ptUId;
    	}
    }
 
    public void importPropertiesParallel(InputStream file, InputStream matchedFile, Long skip, Long limit) throws Exception {

      // Build matched usage type map
      BufferedReader br = new BufferedReader(new InputStreamReader(matchedFile));
      String line = "";
      Map<String, String> matched = new HashMap<>();
      while ((line = br.readLine()) != null) {
          String[] values = line.split(",");
          matched.put(values[0], values[1]);
      }
      
      AtomicInteger numOfSuccess = new AtomicInteger();
      AtomicInteger numOfErrors = new AtomicInteger();
      AtomicInteger totalNumber = new AtomicInteger();
      
      Set<String> duplicateMobileNumbers = new HashSet<>();
      HashMap<String, User>  existingUser = new HashMap<String, User>();
      final ClassLoader loader = PropertyController.class.getClassLoader();
	System.out.println("*********************************migrationFileName:"+config.getMigrationFileName());

      final InputStream excelFile = loader.getResourceAsStream(config.getMigrationFileName());

      excelService.read(excelFile, skip, limit, (RowExcel row) -> {
    	  totalNumber.getAndIncrement();
      	 LegacyRow legacyRow = null;
           
               try {
					legacyRow = legacyExcelRowMapper.map(row);
					
					String name = legacyRow.getOwnerName() != null && legacyRow.getOwnerName() != "" ? legacyRow.getOwnerName()
			                : "Owner of " + legacyRow.getPTIN();
			        // Invalid name. Only alphabets and special characters -, ',`, .
			        name = name.replaceAll("[\\$\"'<>?\\\\~`!@#$%^()+={}\\[\\]*,.:;“”‘’]*", "");
			        name = name.length() > 100 ? name.substring(0, 99) : name;
			        
			        String tenantId = "up." + legacyRow.getULBName().toLowerCase();
				    checkIfSaharanpurAndHandleExponentialMobile(tenantId, legacyRow);
			        
			        if(legacyRow.getMobile()!= null)
			        {
					  if(!duplicateMobileNumbers.add(legacyRow.getMobile().trim()+name.trim()))
					  {
						  existingUser.put(legacyRow.getMobile().trim()+name.trim(), null);
					  }
			        }
					  
				} catch (Exception e) {
					e.printStackTrace();
				}
             
               return true;
      });
      
      
      log.info("  existingUsers size {}",existingUser.size());
      
      existingUser.forEach((key, value) -> {log.info("Key: {}",  key );});
System.out.println("creating failed file from method importParallel");
      excelService.createFailedRecordsFile();
 System.out.println("checking availability of created fail file from method importParallel");
      File f = new File(config.getFailedRecordsMigrationFilePath());
System.out.println("path of file created:"+f.getPath());
System.out.println("getAbsolutePath:"+f.getAbsolutePath());
      System.out.println("getCanonicalPath:"+f.getCanonicalPath());
      System.out.println("listFiles:"+f.listFiles());
      System.out.println("toPath:"+f.toPath());
      System.out.println("toURI:"+f.toURI());
      System.out.println("toURL:"+f.toURL());
System.out.println("listRootsLength:"+File.listRoots().length);
      System.out.println("listRoots:"+File.listRoots()[0].getAbsolutePath());
      if(!f.exists())
    	  System.out.println("fail file not found");
      else
    	  System.out.println("fail file found");
      int cores = Runtime.getRuntime().availableProcessors();
      ExecutorService executorService = Executors.newFixedThreadPool(cores);
      excelService.read(file, skip, limit, (RowExcel row) -> {
    	  
    	  executorService.submit(() -> { 
      	  int failedCode = 0 ;
          LegacyRow legacyRow = null;
          org.egov.pt.models.excel.Property property = new org.egov.pt.models.excel.Property();
          Owner owner = new Owner();
          Unit unit = new Unit();
          Address address = new Address();
          PropertyPayment payment = new PropertyPayment();
          try {
              legacyRow = legacyExcelRowMapper.map(row);
              String tenantId = "up." + legacyRow.getULBName().toLowerCase();
              checkIfSaharanpurAndHandleExponentialMobile(tenantId, legacyRow);

              // Create user if not exists in db.
              User user = this.createUserIfNotExists(legacyRow , existingUser);

				  String token = cachebaleservice.getUserToken(tenantId, user);
				 

              RequestInfo requestinfo = RequestInfo.builder().authToken(token).action("token").apiId("Rainmaker")
                      .did("1").key("").msgId("20170310130900|en_IN").ver(".01")
                      .userInfo(org.egov.common.contract.request.User.builder().type(user.getType()).tenantId("up")
                              .userName(user.getUserName()).build())
                      .build();

              Map<String, String> localityMap = cachebaleservice.getLocalityMap(tenantId, requestinfo);
              log.info("*************1:"+legacyRow.getLocality().trim().toLowerCase());
              log.info("*************2:"+localityMap.get(legacyRow.getLocality().trim().toLowerCase()));
              String localityCode = localityMap.get(legacyRow.getLocality().trim().toLowerCase());
              if (localityCode == null) {
                  log.warn("Empty locality code for the property {}", legacyRow.getLocality());
              }
              
              
              if(tenantId.equalsIgnoreCase("up.Bareilly") || tenantId.equalsIgnoreCase("up.saharanpur"))
              {
            	  HashMap<String,HashMap<String,HashMap<String, String>>> duplicateMap = cachebaleservice.getDuplicateLocalitiesMap(tenantId, requestinfo);
            	  log.info("*******************duplicateLocalityMap:"+duplicateMap);
            	  if(duplicateMap.containsKey(legacyRow.getLocality().trim().toLowerCase()))
            	  {
            		  log.warn("Locality codes for the property {} found from duplicateLocalityMap : {}", legacyRow.getLocality(),duplicateMap.get(legacyRow.getLocality().trim().toLowerCase()));
            		  String zone = legacyRow.getZone();
            		  String wardNo = legacyRow.getWardNo();
            		
            		  wardNo = wardNo.replace(".0", "");
					  if (Objects.nonNull(zone) && !tenantId.equalsIgnoreCase("up.saharanpur"))
							zone = zone.replaceAll("Zone-", "");
            		  localityCode = duplicateMap.get(legacyRow.getLocality().trim().toLowerCase()).get(zone).get(wardNo);
            	  }            	  
            	  
              }

              // Generate unique property id and acknowledgement no
              String pId = propertyutil.getIdList(requestinfo, tenantId, config.getPropertyIdGenName(),
                      config.getPropertyIdGenFormat(), 1).get(0);
              String ackNo = propertyutil
                      .getIdList(requestinfo, tenantId, config.getAckIdGenName(), config.getAckIdGenFormat(), 1)
                      .get(0);
            
              property.setId(UUID.randomUUID().toString());
              property.setPropertyid(pId);
              property.setTenantid(tenantId);
              property.setAccountid(user.getUuid());
              property.setStatus(config.getWfStatusActive());
              property.setAcknowldgementnumber(ackNo);
              property.setPropertytype(PTConstants.PT_TYPE_BUILTUP);
              property.setOwnershipcategory("INDIVIDUAL.SINGLEOWNER");
              if (matched.containsKey(legacyRow.getPropertyTypeClassification())) {
                  property.setUsagecategory(matched.get(legacyRow.getPropertyTypeClassification()));
              } else {
                  property.setUsagecategory("OTHERS");
              }
              property.setNooffloors(1L);
              property.setLandarea(BigDecimal
                      .valueOf(Double.valueOf(legacyRow.getPlotArea() != null &&  !legacyRow.getPlotArea().isEmpty() ? legacyRow.getPlotArea() : "0")));
              property.setOldpropertyid(legacyRow.getPTIN());
              property.setSource("DATA_MIGRATION");
              property.setChannel(Channel.MIGRATION.toString());
              property.setConstructionyear(legacyRow.getConstructionYear());
              property.setCreatedby(user.getUuid());
              property.setLastmodifiedby(user.getUuid());
              property.setCreatedtime(new Date().getTime());
              property.setLastmodifiedtime(new Date().getTime());
              propertyExcelRepository.save(property);
              failedCode = 1;

             
              owner.setOwnerinfouuid(UUID.randomUUID().toString());
              owner.setStatus(Status.ACTIVE.toString());
              owner.setTenantid(tenantId);
              owner.setPropertyid(property.getId());
              owner.setUserid(user.getUuid());
              owner.setOwnertype("NONE");
              owner.setRelationship("FATHER");
              owner.setCreatedby(user.getUuid());
              owner.setLastmodifiedby(user.getUuid());
              owner.setCreatedtime(new Date().getTime());
              owner.setLastmodifiedtime(new Date().getTime());
              ownerExcelRepository.save(owner);
              failedCode = 2;
             
              unit.setId(UUID.randomUUID().toString());
              unit.setTenantid(tenantId);
              unit.setPropertyid(property.getId());
              unit.setFloorno(1L);
              if (matched.containsKey(legacyRow.getPropertyTypeClassification())) {
                  unit.setUnittype(matched.get(legacyRow.getPropertyTypeClassification()));
                  unit.setUsagecategory(matched.get(legacyRow.getPropertyTypeClassification()));
              } else {
                  unit.setUnittype("OTHERS");
                  unit.setUsagecategory("OTHERS");
              }

              unit.setOccupancytype("SELFOCCUPIED");
              unit.setOccupancydate(0L);
              unit.setCarpetarea(BigDecimal.valueOf(
                      Double.valueOf(legacyRow.getTotalCarpetArea() != null && !legacyRow.getTotalCarpetArea().isEmpty() ? legacyRow.getTotalCarpetArea() : "0")));

              // unit.setBuiltuparea(BigDecimal builtuparea)
              // unit.setPlintharea(BigDecimal plintharea)
              // unit.setSuperbuiltuparea(BigDecimal superbuiltuparea)
              unit.setArv(BigDecimal
                      .valueOf(Double.parseDouble(legacyRow.getRCARV() != null && !legacyRow.getRCARV().isEmpty() ? legacyRow.getRCARV() : "0")));
              unit.setConstructiontype("PUCCA");
              // unit.setConstructiondate(Long constructiondate)
              // unit.setDimensions(String dimensions)
              unit.setActive(true);
              unit.setCreatedby(user.getUuid());
              unit.setLastmodifiedby(user.getUuid());
              unit.setCreatedtime(new Date().getTime());
              unit.setLastmodifiedtime(new Date().getTime());
              unitExcelRepository.save(unit);
              failedCode = 3;

              
              address.setTenantid(tenantId);
              address.setId(UUID.randomUUID().toString());
              address.setPropertyid(property.getId());
              address.setDoorno(legacyRow.getHouseNo());
              // address.setPlotno(String plotno)
              // address.setBuildingname(String buildingname)
              address.setStreet(legacyRow.getAddress());
              // address.setLandmark(String landmark)
              address.setCity(legacyRow.getULBName());
              address.setPincode("123456");
              address.setLocality(localityCode);
              // address.setLocality(legacyRow.getLocality() != null? legacyRow.getLocality():
              // "OTHERS");
              address.setDistrict(legacyRow.getULBName());
              // address.setRegion(String region)
              address.setState("Uttar Pradesh");
              address.setCountry("India");
              // address.setLatitude(BigDecimal latitude)
              // address.setLongitude(BigDecimal longitude)
              address.setCreatedby(user.getUuid());
              address.setLastmodifiedby(user.getUuid());
              address.setCreatedtime(new Date().getTime());
              address.setLastmodifiedtime(new Date().getTime());
              address.setTaxward(legacyRow.getTaxWard());
              address.setWardname(legacyRow.getWardName());
              
              Map<String, String>  wardsMap = cachebaleservice.getWardMap(tenantId, requestinfo);
              Map<String, String> zonesMap = cachebaleservice.getZoneMap(tenantId, requestinfo);
              
             
              
              String zoneCode = zonesMap.get(localityCode);
              String wardCode = wardsMap.get(localityCode);
              
              if (zoneCode == null) {
                  log.warn("Empty zones code for the property {}", legacyRow.getLocality());
              }
              
              if (wardsMap == null) {
                  log.warn("Empty wards code for the property {}", legacyRow.getLocality());
              }
              
              address.setWardno(wardCode);
              address.setZone(zoneCode);
              addressExcelRepository.save(address);
              failedCode = 4;
              // address.setAdditionaldetails(String additionaldetails)

              
              payment.setId(UUID.randomUUID().toString());
              payment.setPropertyid(property.getId());
              payment.setFinancialyear(legacyRow.getFinancialYear());
              payment.setArrearhousetax(BigDecimal.valueOf(
                      Double.valueOf(legacyRow.getArrearHouseTax() != null && !legacyRow.getArrearHouseTax().isEmpty() ? legacyRow.getArrearHouseTax() : "0")));
              payment.setArrearwatertax(BigDecimal.valueOf(
                      Double.valueOf(legacyRow.getArrearWaterTax() != null && !legacyRow.getArrearWaterTax().isEmpty() ? legacyRow.getArrearWaterTax() : "0")));
              payment.setArrearsewertax(BigDecimal.valueOf(
                      Double.valueOf(legacyRow.getArrearSewerTax() != null  &&  !legacyRow.getArrearSewerTax().isEmpty() ? legacyRow.getArrearSewerTax() : "0")));
              payment.setHousetax(BigDecimal
                      .valueOf(Double.valueOf(legacyRow.getHouseTax() != null && !legacyRow.getHouseTax().isEmpty() ? legacyRow.getHouseTax() : "0")));
              payment.setWatertax(BigDecimal
                      .valueOf(Double.valueOf(legacyRow.getWaterTax() != null && !legacyRow.getWaterTax().isEmpty() ?  legacyRow.getWaterTax() : "0")));
              payment.setSewertax(BigDecimal
                      .valueOf(Double.valueOf(legacyRow.getSewerTax() != null && !legacyRow.getSewerTax().isEmpty() ? legacyRow.getSewerTax() : "0")));
              payment.setSurcharehousetax(BigDecimal.valueOf(Double
                      .valueOf(legacyRow.getSurchareHouseTax() != null  && !legacyRow.getSurchareHouseTax().isEmpty() ? legacyRow.getSurchareHouseTax() : "0")));
              payment.setSurcharewatertax(BigDecimal.valueOf(Double
                      .valueOf(legacyRow.getSurchareWaterTax() != null  && !legacyRow.getSurchareWaterTax().isEmpty() ? legacyRow.getSurchareWaterTax() : "0")));
              payment.setSurcharesewertax(BigDecimal.valueOf(Double
                      .valueOf(legacyRow.getSurchareSewerTax() != null && !legacyRow.getSurchareSewerTax().isEmpty() ? legacyRow.getSurchareSewerTax() : "0")));
              payment.setBillgeneratedtotal(BigDecimal.valueOf(Double
                      .valueOf(legacyRow.getBillGeneratedTotal() != null && !legacyRow.getBillGeneratedTotal().isEmpty() ? legacyRow.getBillGeneratedTotal() : "0")));
              payment.setTotalpaidamount(BigDecimal.valueOf(
                      Double.valueOf(legacyRow.getTotalPaidAmount() != null && !legacyRow.getTotalPaidAmount().isEmpty() ? legacyRow.getTotalPaidAmount() : "0")));

              payment.setLastpaymentdate(legacyRow.getLastPaymentDate());
              propertyPaymentExcelRepository.save(payment);
              failedCode = 5;
              numOfSuccess.getAndIncrement();
              System.out.println("---------------------------------------Success----------------------------------------------------"+numOfSuccess.get());
              System.out.println("---------------------------------------Error----------------------------------------------------"+numOfErrors.get());
          } catch (Exception e) {
              numOfErrors.getAndIncrement();
//FaieldCodes 1 = failed at owner insertion , 2 = failed at unit insertion , 3 = failed at address insertion , 4 = failed at payment insertion
              if( failedCode == 1)
              {
              	propertyExcelRepository.delete(property);
              }else if( failedCode == 2)
              {
              	ownerExcelRepository.delete(owner);
              	propertyExcelRepository.delete(property);
              }else if( failedCode == 3)
              {
              	unitExcelRepository.delete(unit);
              	ownerExcelRepository.delete(owner);
              	propertyExcelRepository.delete(property);
              }else if( failedCode == 4)
              {
              	addressExcelRepository.delete(address);
              	unitExcelRepository.delete(unit);
              	ownerExcelRepository.delete(owner);
              	propertyExcelRepository.delete(property);
              }
              log.info("---------------------------------------No.of Success----------------------------------------------------{} ",numOfSuccess.get());
              log.info("---------------------------------------No.of Errors----------------------------------------------------{} ",numOfErrors.get());
              
            
              excelService.writeFailedRecords(legacyRow);
              
              
              log.error("Row[{}] - [{}] , errorMessage: {}", row.getRowIndex(), legacyRow.toString(), e.getMessage());
          }
    	  });
    	  
			
    	  
          return true;
      });
      
      
      if(!executorService.isShutdown())
      {
    	  executorService.shutdown();
    	  
      }
      
      while(true)
	  {
    	  Thread.sleep(1000);
    	  if(totalNumber.get() == numOfErrors.get()+numOfSuccess.get())
    	  break;
	  }
	  
      excelService.writeToFileandClose();
      
      log.info("Import Completed - Success={} Errors={}", numOfSuccess, numOfErrors);
      
    
     
  }
    
	private void checkIfSaharanpurAndHandleExponentialMobile(String tenantId, LegacyRow legacyRow) {
		if (tenantId.equalsIgnoreCase("up.saharanpur") && legacyRow.getMobile() != null
				&& legacyRow.getMobile().contains(".") && legacyRow.getMobile().contains("E")) {
			String exponentialFormat = legacyRow.getMobile();
			String[] parts = exponentialFormat.split("E");
			String numberFormat = new BigDecimal(parts[0] + "E+" + parts[1]).toBigInteger().toString();
			legacyRow.setMobile(numberFormat);
			log.debug("mobile:" + legacyRow.getMobile());
		}
	}
  
    
    
}
