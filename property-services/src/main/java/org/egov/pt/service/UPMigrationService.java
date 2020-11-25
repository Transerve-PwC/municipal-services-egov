package org.egov.pt.service;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import org.egov.common.contract.request.RequestInfo;
import org.egov.common.contract.request.Role;
import org.egov.pt.config.PropertyConfiguration;
import org.egov.pt.models.OwnerInfo;
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
import org.egov.pt.repository.ServiceRequestRepository;
import org.egov.pt.repository.UnitExcelRepository;
import org.egov.pt.repository.rowmapper.LegacyExcelRowMapper;
import org.egov.pt.util.PTConstants;
import org.egov.pt.util.PropertyUtil;
import org.egov.pt.web.controllers.PropertyController;
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

    

   
    

   

    private static final RequestInfo userCreateRequestInfo = RequestInfo.builder().action("_create").apiId("Rainmaker")
            .did("1").key("").msgId("20170310130900|en_IN").ver(".01").build();

    private User createUserIfNotExists(LegacyRow legacyRow, HashMap<String, User> existingUser) {
        final String tenantId = "up." + legacyRow.getULBName().toLowerCase();
        User userRequest = new User();
        userRequest.setActive(true);
        userRequest.setMobileNumber((legacyRow.getMobile() != null && legacyRow.getMobile() != ""
                && (new BigDecimal(legacyRow.getMobile()).longValue() != 0)) ? legacyRow.getMobile()
                        : convertPTINToMobileNumber("5" + legacyRow.getPTIN()));
        userRequest.setUserName(UUID.randomUUID().toString());
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
			  UserDetailResponse  userDetailResponse1 = userService.createUser(userCreateRequestInfo, userRequest); 
			  user = userDetailResponse1.getUser().get(0);
			  existingUser.put(userRequest.getMobileNumber(), user);
		  }

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
        
        Set<String> duplicateMobileNumbers = new HashSet<>();
        HashMap<String, User>  existingUser = new HashMap<String, User>();
        final ClassLoader loader = PropertyController.class.getClassLoader();

        final InputStream excelFile = loader.getResourceAsStream(config.getMigrationFileName());

        excelService.read(excelFile, skip, limit, (RowExcel row) -> {
        	 LegacyRow legacyRow = null;
             
                 try {
					legacyRow = legacyExcelRowMapper.map(row);
					
					String name = legacyRow.getOwnerName() != null && legacyRow.getOwnerName() != "" ? legacyRow.getOwnerName()
			                : "Owner of " + legacyRow.getPTIN();
			        // Invalid name. Only alphabets and special characters -, ',`, .
			        name = name.replaceAll("[\\$\"'<>?\\\\~`!@#$%^()+={}\\[\\]*,.:;“”‘’]*", "");
			        name = name.length() > 100 ? name.substring(0, 99) : name;
					
					  if(!duplicateMobileNumbers.add(legacyRow.getMobile().trim()+name.trim()))
					  {
						  existingUser.put(legacyRow.getMobile().trim()+name.trim(), null);
					  }
					  
				} catch (Exception e) {
					e.printStackTrace();
				}
               
                 return true;
        });
        
        
        log.info("  existingUsers size {}",existingUser.size());
        
        existingUser.forEach((key, value) -> {log.info("Key: {}",  key );});
        
        	ArrayList<LegacyRow>  failedRecordsList = new ArrayList();
        excelService.read(file, skip, limit, (RowExcel row) -> {
            LegacyRow legacyRow = null;
            try {
                legacyRow = legacyExcelRowMapper.map(row);
                String tenantId = "up." + legacyRow.getULBName().toLowerCase();

                // Create user if not exists in db.
                User user = this.createUserIfNotExists(legacyRow , existingUser);

				
				  String token = cachebaleservice.getUserToken(tenantId, user);
				 

                RequestInfo requestinfo = RequestInfo.builder().authToken(token).action("token").apiId("Rainmaker")
                        .did("1").key("").msgId("20170310130900|en_IN").ver(".01")
                        .userInfo(org.egov.common.contract.request.User.builder().type(user.getType()).tenantId("up")
                                .userName(user.getUserName()).build())
                        .build();

                Map<String, String> localityMap = cachebaleservice.getLocalityMap(tenantId, requestinfo);
                String localityCode = localityMap.get(legacyRow.getLocality().trim().toLowerCase());
                if (localityCode == null) {
                    log.warn("Empty locality code for the property {}", legacyRow.getLocality());
                }

                // Generate unique property id and acknowledgement no
                String pId = propertyutil.getIdList(requestinfo, tenantId, config.getPropertyIdGenName(),
                        config.getPropertyIdGenFormat(), 1).get(0);
                String ackNo = propertyutil
                        .getIdList(requestinfo, tenantId, config.getAckIdGenName(), config.getAckIdGenFormat(), 1)
                        .get(0);
                org.egov.pt.models.excel.Property property = new org.egov.pt.models.excel.Property();
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
                        .valueOf(Double.valueOf(legacyRow.getPlotArea() != null ? legacyRow.getPlotArea() : "0")));
                property.setOldpropertyid(legacyRow.getPTIN());
                property.setSource("DATA_MIGRATION");
                property.setChannel(Channel.MIGRATION.toString());
                property.setConstructionyear(legacyRow.getConstructionYear());
                property.setCreatedby(user.getUuid());
                property.setLastmodifiedby(user.getUuid());
                property.setCreatedtime(new Date().getTime());
                property.setLastmodifiedtime(new Date().getTime());
                propertyExcelRepository.save(property);

                Owner owner = new Owner();
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

                Unit unit = new Unit();
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
                        Double.valueOf(legacyRow.getTotalCarpetArea() != null ? legacyRow.getTotalCarpetArea() : "0")));

                // unit.setBuiltuparea(BigDecimal builtuparea)
                // unit.setPlintharea(BigDecimal plintharea)
                // unit.setSuperbuiltuparea(BigDecimal superbuiltuparea)
                unit.setArv(BigDecimal
                        .valueOf(Double.parseDouble(legacyRow.getRCARV() != null ? legacyRow.getRCARV() : "0")));
                unit.setConstructiontype("PUCCA");
                // unit.setConstructiondate(Long constructiondate)
                // unit.setDimensions(String dimensions)
                unit.setActive(true);
                unit.setCreatedby(user.getUuid());
                unit.setLastmodifiedby(user.getUuid());
                unit.setCreatedtime(new Date().getTime());
                unit.setLastmodifiedtime(new Date().getTime());
                unitExcelRepository.save(unit);

                Address address = new Address();
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
                address.setWardno(legacyRow.getWardNo());
                address.setZone(legacyRow.getZone());
                addressExcelRepository.save(address);
                // address.setAdditionaldetails(String additionaldetails)

                PropertyPayment payment = new PropertyPayment();
                payment.setId(UUID.randomUUID().toString());
                payment.setPropertyid(property.getId());
                payment.setFinancialyear(legacyRow.getFinancialYear());
                payment.setArrearhousetax(BigDecimal.valueOf(
                        Double.valueOf(legacyRow.getArrearHouseTax() != null ? legacyRow.getArrearHouseTax() : "0")));
                payment.setArrearwatertax(BigDecimal.valueOf(
                        Double.valueOf(legacyRow.getArrearWaterTax() != null ? legacyRow.getArrearWaterTax() : "0")));
                payment.setArrearsewertax(BigDecimal.valueOf(
                        Double.valueOf(legacyRow.getArrearSewerTax() != null ? legacyRow.getArrearSewerTax() : "0")));
                payment.setHousetax(BigDecimal
                        .valueOf(Double.valueOf(legacyRow.getHouseTax() != null ? legacyRow.getHouseTax() : "0")));
                payment.setWatertax(BigDecimal
                        .valueOf(Double.valueOf(legacyRow.getWaterTax() != null ? legacyRow.getWaterTax() : "0")));
                payment.setSewertax(BigDecimal
                        .valueOf(Double.valueOf(legacyRow.getSewerTax() != null ? legacyRow.getSewerTax() : "0")));
                payment.setSurcharehousetax(BigDecimal.valueOf(Double
                        .valueOf(legacyRow.getSurchareHouseTax() != null ? legacyRow.getSurchareHouseTax() : "0")));
                payment.setSurcharewatertax(BigDecimal.valueOf(Double
                        .valueOf(legacyRow.getSurchareWaterTax() != null ? legacyRow.getSurchareWaterTax() : "0")));
                payment.setSurcharesewertax(BigDecimal.valueOf(Double
                        .valueOf(legacyRow.getSurchareSewerTax() != null ? legacyRow.getSurchareSewerTax() : "0")));
                payment.setBillgeneratedtotal(BigDecimal.valueOf(Double
                        .valueOf(legacyRow.getBillGeneratedTotal() != null ? legacyRow.getBillGeneratedTotal() : "0")));
                payment.setTotalpaidamount(BigDecimal.valueOf(
                        Double.valueOf(legacyRow.getTotalPaidAmount() != null ? legacyRow.getTotalPaidAmount() : "0")));

                payment.setLastpaymentdate(legacyRow.getLastPaymentDate());
                propertyPaymentExcelRepository.save(payment);
                numOfSuccess.getAndIncrement();
            } catch (Exception e) {
                numOfErrors.getAndIncrement();
                failedRecordsList.add(legacyRow);
                log.error("Row[{}] - [{}] , errorMessage: {}", row.getRowIndex(), legacyRow.toString(), e.getMessage());
            }
            return true;
        });
        log.info("Import Completed - Success={} Errors={}", numOfSuccess, numOfErrors);
        excelService.writeFailedRecords(failedRecordsList);
        log.info("failedRecordsList size {}", failedRecordsList.size());
    }

    private String convertPTINToMobileNumber(String ptin) {
        String curPtin = ptin;
        while (curPtin.length() < 9) {
            curPtin = "0" + curPtin;
        }
        return "5" + curPtin;
    }
}
