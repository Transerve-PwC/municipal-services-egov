package org.egov.pt.service;

import java.io.*;
import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;
import java.util.stream.Collectors;

import com.jayway.jsonpath.JsonPath;
import org.egov.common.contract.request.RequestInfo;
import org.egov.common.contract.request.Role;
import org.egov.mdms.model.MasterDetail;
import org.egov.mdms.model.MdmsCriteria;
import org.egov.mdms.model.MdmsCriteriaReq;
import org.egov.mdms.model.ModuleDetail;
import org.egov.pt.config.PropertyConfiguration;
import org.egov.pt.models.OwnerInfo;
import org.egov.pt.models.Property;
import org.egov.pt.models.PropertyCriteria;
import org.egov.pt.models.enums.CreationReason;
import org.egov.pt.models.enums.Status;
import org.egov.pt.models.excel.*;
import org.egov.pt.models.user.User;
import org.egov.pt.models.user.UserDetailResponse;
import org.egov.pt.models.user.UserSearchRequest;
import org.egov.pt.models.workflow.State;
import org.egov.pt.producer.Producer;
import org.egov.pt.repository.*;
import org.egov.pt.repository.rowmapper.LegacyExcelRowMapper;
import org.egov.pt.util.PTConstants;
import org.egov.pt.util.PropertyUtil;
import org.egov.pt.validator.PropertyValidator;
import org.egov.pt.web.contracts.PropertyRequest;
import org.egov.pt.web.controllers.PropertyController;
import org.egov.tracer.model.CustomException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.Sets;

@Service
public class PropertyService {
	@Autowired
	private PropertyUtil propertyutil;

	@Autowired
	private BoundaryService boundaryService;

    @Autowired
    private Producer producer;

    @Autowired
    private PropertyConfiguration config;

    @Autowired
    private PropertyRepository repository;

    @Autowired
    private EnrichmentService enrichmentService;

    @Autowired
    private PropertyValidator propertyValidator;

    @Autowired
    private UserService userService;

    @Autowired
	private WorkflowService wfService;
    
    @Autowired
    private PropertyUtil util;
    
    @Autowired
    private ObjectMapper mapper;
    
    @Autowired
	private CalculationService calculatorService;


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
	private ServiceRequestRepository restRepo;
	/**
	 * Enriches the Request and pushes to the Queue
	 *
	 * @param request PropertyRequest containing list of properties to be created
	 * @return List of properties successfully created
	 */
	public Property createProperty(PropertyRequest request) {

		propertyValidator.validateCreateRequest(request);
		enrichmentService.enrichCreateRequest(request);
		userService.createUser(request);
		if (config.getIsWorkflowEnabled()
				&& !request.getProperty().getCreationReason().equals(CreationReason.DATA_UPLOAD)) {
			wfService.updateWorkflow(request, request.getProperty().getCreationReason());

		} else {

			request.getProperty().setStatus(Status.ACTIVE);
		}

		producer.push(config.getSavePropertyTopic(), request);
		request.getProperty().setWorkflow(null);
		return request.getProperty();
	}
	
	/**
	 * Updates the property
	 * 
	 * handles multiple processes 
	 * 
	 * Update
	 * 
	 * Mutation
	 *
	 * @param request PropertyRequest containing list of properties to be update
	 * @return List of updated properties
	 */
	public Property updateProperty(PropertyRequest request) {

		Property propertyFromSearch = propertyValidator.validateCommonUpdateInformation(request);
		
		boolean isRequestForOwnerMutation = CreationReason.MUTATION.equals(request.getProperty().getCreationReason());
		
		if (isRequestForOwnerMutation)
			processOwnerMutation(request, propertyFromSearch);
		else
			processPropertyUpdate(request, propertyFromSearch);

		request.getProperty().setWorkflow(null);
		return request.getProperty();
	}

	/**
	 * Method to process Property update 
	 * 
	 * @param request
	 * @param propertyFromSearch
	 */
	private void processPropertyUpdate(PropertyRequest request, Property propertyFromSearch) {
		
		propertyValidator.validateRequestForUpdate(request, propertyFromSearch);
		userService.createUser(request);
		request.getProperty().setOwners(propertyFromSearch.getOwners());
		enrichmentService.enrichAssignes(request.getProperty());
		enrichmentService.enrichUpdateRequest(request, propertyFromSearch);
		
		PropertyRequest OldPropertyRequest = PropertyRequest.builder()
				.requestInfo(request.getRequestInfo())
				.property(propertyFromSearch)
				.build();
		
		util.mergeAdditionalDetails(request, propertyFromSearch);
		
		if(config.getIsWorkflowEnabled()) {
			
			State state = wfService.updateWorkflow(request, CreationReason.UPDATE);

			if (state.getIsStartState() == true
					&& state.getApplicationStatus().equalsIgnoreCase(Status.INWORKFLOW.toString())
					&& !propertyFromSearch.getStatus().equals(Status.INWORKFLOW)) {

				propertyFromSearch.setStatus(Status.INACTIVE);
				producer.push(config.getUpdatePropertyTopic(), OldPropertyRequest);
				util.saveOldUuidToRequest(request, propertyFromSearch.getId());
				producer.push(config.getSavePropertyTopic(), request);

			} else if (state.getIsTerminateState()
					&& !state.getApplicationStatus().equalsIgnoreCase(Status.ACTIVE.toString())) {

				terminateWorkflowAndReInstatePreviousRecord(request, propertyFromSearch);
			}else {
				/*
				 * If property is In Workflow then continue
				 */
				producer.push(config.getUpdatePropertyTopic(), request);
			}

		} else {

			/*
			 * If no workflow then update property directly with mutation information
			 */
			producer.push(config.getUpdatePropertyTopic(), request);
		}
	}

	/**
	 * method to process owner mutation
	 * 
	 * @param request
	 * @param propertyFromSearch
	 */
	private void processOwnerMutation(PropertyRequest request, Property propertyFromSearch) {
		
		propertyValidator.validateMutation(request, propertyFromSearch);
		userService.createUser(request);
		enrichmentService.enrichAssignes(request.getProperty());
		enrichmentService.enrichMutationRequest(request, propertyFromSearch);
		calculatorService.calculateMutationFee(request.getRequestInfo(), request.getProperty());
		
		// TODO FIX ME block property changes FIXME
		util.mergeAdditionalDetails(request, propertyFromSearch);
		PropertyRequest oldPropertyRequest = PropertyRequest.builder()
				.requestInfo(request.getRequestInfo())
				.property(propertyFromSearch)
				.build();
		
		if (config.getIsMutationWorkflowEnabled()) {

			State state = wfService.updateWorkflow(request, CreationReason.MUTATION);
      
			/*
			 * updating property from search to INACTIVE status
			 * 
			 * to create new entry for new Mutation
			 */
			if (state.getIsStartState() == true
					&& state.getApplicationStatus().equalsIgnoreCase(Status.INWORKFLOW.toString())
					&& !propertyFromSearch.getStatus().equals(Status.INWORKFLOW)) {
				
				propertyFromSearch.setStatus(Status.INACTIVE);
				producer.push(config.getUpdatePropertyTopic(), oldPropertyRequest);

				util.saveOldUuidToRequest(request, propertyFromSearch.getId());
				/* save new record */
				producer.push(config.getSavePropertyTopic(), request);

			} else if (state.getIsTerminateState()
					&& !state.getApplicationStatus().equalsIgnoreCase(Status.ACTIVE.toString())) {

				terminateWorkflowAndReInstatePreviousRecord(request, propertyFromSearch);
			} else {
				/*
				 * If property is In Workflow then continue
				 */
				producer.push(config.getUpdatePropertyTopic(), request);
			}

		} else {

			/*
			 * If no workflow then update property directly with mutation information
			 */
			producer.push(config.getUpdatePropertyTopic(), request);
		}
	}

	private void terminateWorkflowAndReInstatePreviousRecord(PropertyRequest request, Property propertyFromSearch) {
		
		/* current record being rejected */
		producer.push(config.getUpdatePropertyTopic(), request);
		
		/* Previous record set to ACTIVE */
		@SuppressWarnings("unchecked")
		Map<String, Object> additionalDetails = mapper.convertValue(propertyFromSearch.getAdditionalDetails(), Map.class);
		if(null == additionalDetails) 
			return;
		
		String propertyUuId = (String) additionalDetails.get(PTConstants.PREVIOUS_PROPERTY_PREVIOUD_UUID);
		if(StringUtils.isEmpty(propertyUuId)) 
			return;
		
		PropertyCriteria criteria = PropertyCriteria.builder().uuids(Sets.newHashSet(propertyUuId))
				.tenantId(propertyFromSearch.getTenantId()).build();
		Property previousPropertyToBeReInstated = searchProperty(criteria, request.getRequestInfo()).get(0);
		previousPropertyToBeReInstated.setAuditDetails(util.getAuditDetails(request.getRequestInfo().getUserInfo().getUuid().toString(), true));
		previousPropertyToBeReInstated.setStatus(Status.ACTIVE);
		request.setProperty(previousPropertyToBeReInstated);
		
		producer.push(config.getUpdatePropertyTopic(), request);
	}

    /**
     * Search property with given PropertyCriteria
     *
     * @param criteria PropertyCriteria containing fields on which search is based
     * @return list of properties satisfying the containing fields in criteria
     */
	public List<Property> searchProperty(PropertyCriteria criteria, RequestInfo requestInfo) {

		List<Property> properties;

		/*
		 * throw error if audit request is with no proeprty id or multiple propertyids
		 */
		if (criteria.isAudit() && (CollectionUtils.isEmpty(criteria.getPropertyIds())
				|| (!CollectionUtils.isEmpty(criteria.getPropertyIds()) && criteria.getPropertyIds().size() > 1))) {

			throw new CustomException("EG_PT_PROPERTY_AUDIT_ERROR", "Audit can only be provided for a single propertyId");
		}

		if (criteria.getMobileNumber() != null || criteria.getName() != null || criteria.getOwnerIds() != null) {

			/* converts owner information to associated property ids */
			Boolean shouldReturnEmptyList = repository.enrichCriteriaFromUser(criteria, requestInfo);

			if (shouldReturnEmptyList)
				return Collections.emptyList();

			properties = repository.getPropertiesWithOwnerInfo(criteria, requestInfo, false);
		} else {
			properties = repository.getPropertiesWithOwnerInfo(criteria, requestInfo, false);
		}

		properties.forEach(property -> {
			enrichmentService.enrichBoundary(property, requestInfo);
		});
		
		return properties;
	}

	public List<Property> searchPropertyPlainSearch(PropertyCriteria criteria, RequestInfo requestInfo) {
		List<Property> properties = getPropertiesPlainSearch(criteria, requestInfo);
		for(Property property:properties)
			enrichmentService.enrichBoundary(property,requestInfo);
		return properties;
	}


	List<Property> getPropertiesPlainSearch(PropertyCriteria criteria, RequestInfo requestInfo) {
		if (criteria.getLimit() != null && criteria.getLimit() > config.getMaxSearchLimit())
			criteria.setLimit(config.getMaxSearchLimit());
		if(criteria.getLimit()==null)
			criteria.setLimit(config.getDefaultLimit());
		if(criteria.getOffset()==null)
			criteria.setOffset(config.getDefaultOffset());
		PropertyCriteria propertyCriteria = new PropertyCriteria();
		if (criteria.getUuids() != null || criteria.getPropertyIds() != null) {
			if (criteria.getUuids() != null)
				propertyCriteria.setUuids(criteria.getUuids());
			if (criteria.getPropertyIds() != null)
				propertyCriteria.setPropertyIds(criteria.getPropertyIds());

		} else {
			List<String> uuids = repository.fetchIds(criteria);
			if (uuids.isEmpty())
				return Collections.emptyList();
			propertyCriteria.setUuids(new HashSet<>(uuids));
		}
		propertyCriteria.setLimit(criteria.getLimit());
		List<Property> properties = repository.getPropertiesForBulkSearch(propertyCriteria);
		if(properties.isEmpty())
			return Collections.emptyList();
		Set<String> ownerIds = properties.stream().map(Property::getOwners).flatMap(List::stream)
				.map(OwnerInfo::getUuid).collect(Collectors.toSet());

		UserSearchRequest userSearchRequest = userService.getBaseUserSearchRequest(criteria.getTenantId(), requestInfo);
		userSearchRequest.setUuid(ownerIds);
		UserDetailResponse userDetailResponse = userService.getUser(userSearchRequest);
		util.enrichOwner(userDetailResponse, properties, false);
		return properties;
	}

	private MdmsCriteriaReq prepareMdMsRequest(String tenantId,String moduleName, List<String> names, String filter, RequestInfo requestInfo) {

		List<MasterDetail> masterDetails = new ArrayList<>();

		names.forEach(name -> {
			masterDetails.add(MasterDetail.builder().name(name).filter(filter).build());
		});

		ModuleDetail moduleDetail = ModuleDetail.builder()
				.moduleName(moduleName).masterDetails(masterDetails).build();
		List<ModuleDetail> moduleDetails = new ArrayList<>();
		moduleDetails.add(moduleDetail);
		MdmsCriteria mdmsCriteria = MdmsCriteria.builder().tenantId(tenantId).moduleDetails(moduleDetails).build();
		return MdmsCriteriaReq.builder().requestInfo(requestInfo).mdmsCriteria(mdmsCriteria).build();
	}
	public void importProperties(InputStream file, InputStream matchedFile, Long skip, Long limit) throws Exception {

		BufferedReader  br = new BufferedReader(new InputStreamReader(matchedFile));
		String line= "";
		Map<String, String> matched = new HashMap<>();
		while ((line = br.readLine()) != null) {
			String[] values = line.split(",");
			matched.put(values[0],values[1]);
		}

		AtomicInteger numOfSuccess = new AtomicInteger();
		AtomicInteger numOfErrors = new AtomicInteger();
		excelService.read(file, skip, limit, (RowExcel row)->{
			LegacyRow legacyRow = null;
			try {
				legacyRow = legacyExcelRowMapper.map(row);
				String tenantId = "up."+legacyRow.getULBName().toLowerCase();

				RequestInfo userCreateRequestInfo = RequestInfo.builder().action("_create")
						.apiId("Rainmaker").did("1").key("").msgId("20170310130900|en_IN").ver(".01").build();


				User userRequest = new User();
				userRequest.setActive(true);
				userRequest.setMobileNumber(legacyRow.getMobile() != null && legacyRow.getMobile() != "" ? legacyRow.getMobile(): convertPTINToMobileNumber("9"+legacyRow.getPTIN()));
				userRequest.setUserName(UUID.randomUUID().toString());
				userRequest.setPassword("123456");
				userRequest.setFatherOrHusbandName(legacyRow.getFHName() != null && legacyRow.getFHName().length() > 100 ? legacyRow.getFHName().substring(0, 99):legacyRow.getFHName());
				userRequest.setType("CITIZEN");
				userRequest.setTenantId(tenantId);
				userRequest.setPermanentCity(tenantId);
				userRequest.setPermanentAddress(legacyRow.getAddress());
				userRequest.setRoles(Arrays.asList(Role.builder().code("CITIZEN").build()));
				userRequest.setName(legacyRow.getOwnerName() != null && legacyRow.getOwnerName().length() > 100 ? legacyRow.getOwnerName().substring(0, 99):legacyRow.getOwnerName());
				userRequest.setCorrespondenceCity(tenantId);
				userRequest.setCorrespondenceAddress(legacyRow.getAddress());
				UserDetailResponse userDetailResponse = userService.createUser(userCreateRequestInfo, userRequest);
				User user = userDetailResponse.getUser().get(0);

				Map<String, String> tokenResponse = (Map<String, String>) userService.getToken(user.getUserName(), "123456", tenantId, user.getType());
				String token = (String) tokenResponse.get("access_token");

				RequestInfo requestinfo = RequestInfo.builder().authToken(token).action("token")
						.apiId("Rainmaker").did("1").key("").msgId("20170310130900|en_IN").ver(".01")
						.userInfo(org.egov.common.contract.request.User.builder().type(user.getType()).tenantId("up").userName(user.getUserName()).build()).build();


				StringBuilder uri = new StringBuilder(config.getMdmsHost()).append(config.getMdmsEndpoint());
				MdmsCriteriaReq criteriaReq = prepareMdMsRequest(tenantId,"egov-location",Arrays.asList(new String[]{"TenantBoundary"}),"[?(@.hierarchyType.code in [REVENUE])]",requestinfo);
				Optional<Object> response = restRepo.fetchResult(uri, criteriaReq);

				List<Map<String, String>> boundaries = JsonPath.read(response.get(), "$..[?(@.name=='"+legacyRow.getLocality()+"')]");

				String localityCode = "";
				if(boundaries.size() > 0){
					Map<String, String> boundary = boundaries.get(0);
					localityCode = boundary.get("code");
				}


				String pId = propertyutil.getIdList(requestinfo, tenantId, config.getPropertyIdGenName(), config.getPropertyIdGenFormat(), 1).get(0);
				String ackNo = propertyutil.getIdList(requestinfo, tenantId, config.getAckIdGenName(), config.getAckIdGenFormat(), 1).get(0);
				//String ackNo = propertyutil.getIdList(requestInfo, tenantId, config.getAckIdGenName(), config.getAckIdGenFormat(), 1).get(0);
				org.egov.pt.models.excel.Property property = new org.egov.pt.models.excel.Property();
				property.setId(UUID.randomUUID().toString());
				property.setPropertyid(pId);
				property.setTenantid(tenantId);
				property.setAccountid(user.getUuid());
				property.setStatus("APPROVED");
				property.setAcknowldgementnumber(ackNo);
				property.setPropertytype("BUILTUP");
				property.setOwnershipcategory("INDIVIDUAL.SINGLEOWNER");
				if(matched.containsKey(legacyRow.getPropertyTypeClassification())){
					property.setUsagecategory(matched.get(legacyRow.getPropertyTypeClassification()));
				} else {
					property.setUsagecategory("OTHERS");
				}
				//property.setCreationreason()
				property.setNooffloors(1L);
				property.setLandarea(BigDecimal.valueOf(Double.valueOf(legacyRow.getPlotArea() != null? legacyRow.getPlotArea():"0")));
				//property.setSuperbuiltuparea()
				//property.setLinkedproperties()
				property.setOldpropertyid(legacyRow.getPTIN());
				property.setSource("DATA_MIGRATION");
				property.setChannel("MIGRATION");
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
				if(matched.containsKey(legacyRow.getPropertyTypeClassification())){
					unit.setUnittype(matched.get(legacyRow.getPropertyTypeClassification()));
				} else {
					unit.setUnittype("OTHERS");
				}

				if(matched.containsKey(legacyRow.getPropertyTypeClassification())){
					unit.setUsagecategory(matched.get(legacyRow.getPropertyTypeClassification()));
				} else {
					unit.setUsagecategory("OTHERS");
				}
				unit.setOccupancytype("SELFOCCUPIED");
				unit.setOccupancydate(0L);
				unit.setCarpetarea(BigDecimal.valueOf(Double.valueOf(legacyRow.getTotalCarpetArea() != null? legacyRow.getTotalCarpetArea(): "0" )));

				//unit.setBuiltuparea(BigDecimal builtuparea)
				//unit.setPlintharea(BigDecimal plintharea)
				//unit.setSuperbuiltuparea(BigDecimal superbuiltuparea)
				unit.setArv(BigDecimal.valueOf(Double.valueOf(legacyRow.getRCARV() != null? legacyRow.getRCARV(): "0")));

				unit.setConstructiontype("PUCCA");
				//unit.setConstructiondate(Long constructiondate)
				//unit.setDimensions(String dimensions)
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
				//address.setPlotno(String plotno)
				//address.setBuildingname(String buildingname)
				address.setStreet(legacyRow.getAddress());
				//address.setLandmark(String landmark)
				address.setCity(legacyRow.getULBName());
				address.setPincode("123456");
				address.setLocality(localityCode);
				//address.setLocality(legacyRow.getLocality() != null? legacyRow.getLocality(): "OTHERS");
				address.setDistrict(legacyRow.getULBName());
				//address.setRegion(String region)
				address.setState("Uttar Pradesh");
				address.setCountry("India");
				//address.setLatitude(BigDecimal latitude)
				//address.setLongitude(BigDecimal longitude)
				address.setCreatedby(user.getUuid());
				address.setLastmodifiedby(user.getUuid());
				address.setCreatedtime(new Date().getTime());
				address.setLastmodifiedtime(new Date().getTime());
				address.setTaxward(legacyRow.getTaxWard());
				address.setWardname(legacyRow.getWardName());
				address.setWardno(legacyRow.getWardNo());
				address.setZone(legacyRow.getZone());
				addressExcelRepository.save(address);
				//address.setAdditionaldetails(String additionaldetails)


				PropertyPayment payment = new PropertyPayment();
				payment.setId(UUID.randomUUID().toString());
				payment.setPropertyid(property.getId());
				payment.setFinancialyear(legacyRow.getFinancialYear());
				payment.setArrearhousetax(BigDecimal.valueOf(Double.valueOf(legacyRow.getArrearHouseTax() != null? legacyRow.getArrearHouseTax(): "0")));
				payment.setArrearwatertax(BigDecimal.valueOf(Double.valueOf(legacyRow.getArrearWaterTax() != null? legacyRow.getArrearWaterTax():"0")));
				payment.setArrearsewertax(BigDecimal.valueOf(Double.valueOf(legacyRow.getArrearSewerTax() != null? legacyRow.getArrearSewerTax():"0")));
				payment.setHousetax(BigDecimal.valueOf(Double.valueOf(legacyRow.getHouseTax() != null ? legacyRow.getHouseTax(): "0")));
				payment.setWatertax(BigDecimal.valueOf(Double.valueOf(legacyRow.getWaterTax() != null ? legacyRow.getWaterTax(): "0")));
				payment.setSewertax(BigDecimal.valueOf(Double.valueOf(legacyRow.getSewerTax() != null? legacyRow.getSewerTax():"0")));
				payment.setSurcharehousetax(BigDecimal.valueOf(Double.valueOf(legacyRow.getSurchareHouseTax() != null ? legacyRow.getSurchareHouseTax():"0")));
				payment.setSurcharewatertax(BigDecimal.valueOf(Double.valueOf(legacyRow.getSurchareWaterTax() != null ? legacyRow.getSurchareWaterTax(): "0")));
				payment.setSurcharesewertax(BigDecimal.valueOf(Double.valueOf(legacyRow.getSurchareSewerTax() != null ? legacyRow.getSurchareSewerTax():"0")));
				payment.setBillgeneratedtotal(BigDecimal.valueOf(Double.valueOf(legacyRow.getBillGeneratedTotal() != null ? legacyRow.getBillGeneratedTotal():"0")));
				payment.setTotalpaidamount(BigDecimal.valueOf(Double.valueOf(legacyRow.getTotalPaidAmount() != null ? legacyRow.getTotalPaidAmount():"0")));

				payment.setLastpaymentdate(legacyRow.getLastPaymentDate());
				propertyPaymentExcelRepository.save(payment);
				numOfSuccess.getAndIncrement();
			} catch (Exception e) {
				numOfErrors.getAndIncrement();
				System.out.println("Row["+row.getRowIndex()+"] - ["+legacyRow.toString()+"] -"+e.getMessage());
			}
			return true;
		});
		System.out.println("Import Completed - Success="+numOfSuccess+" Errors="+numOfErrors);
	}

	private String convertPTINToMobileNumber(String ptin){
		String curPtin = ptin;
		while(curPtin.length() < 9){
			curPtin = "0"+curPtin;
		}
		return "9"+curPtin;
	}
}