package org.egov.pt.service;

import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.egov.common.contract.request.RequestInfo;
import org.egov.pt.config.PropertyConfiguration;
import org.egov.pt.models.Assessment;
import org.egov.pt.models.AuditDetails;
import org.egov.pt.models.ConstructionDetail;
import org.egov.pt.models.GeoLocation;
import org.egov.pt.models.Locality;
import org.egov.pt.models.OwnerInfo;
import org.egov.pt.models.Property;
import org.egov.pt.models.UnitUsage;
import org.egov.pt.models.Assessment.Source;
import org.egov.pt.models.enums.Channel;
import org.egov.pt.models.enums.CreationReason;
import org.egov.pt.models.enums.Status;
import org.egov.pt.models.oldProperty.OldProperty;
import org.egov.pt.producer.Producer;
import org.egov.pt.repository.ServiceRequestRepository;
import org.egov.pt.util.PTConstants;
import org.egov.pt.web.contracts.AssessmentRequest;
import org.egov.pt.web.contracts.PropertyRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.extern.slf4j.Slf4j;


@Service
@Slf4j
public class JsonMigration {

	@Autowired
	private Producer producer;

	@Autowired
	private PropertyConfiguration config;

	@Autowired
	private TranslationService translationService;
	
    @Autowired
    private ServiceRequestRepository serviceRequestRepository;

	
    
    
    
    public List<Property> PtmsJsonMigration(RequestInfo requestInfo2, String ward)
			throws JsonParseException, JsonMappingException, IOException {

		List<Property> properties = new ArrayList<>();

		String Id = UUID.randomUUID().toString();

		TypeReference<List<Object>> ty = new TypeReference<List<Object>>() {
		};

		
		String ptmsDataMigrationFile = config.getPtmsDataMigrationFile();
		
		InputStream inptstrm = TypeReference.class.getResourceAsStream(ptmsDataMigrationFile);
		List<Object> readValueobj = new ObjectMapper().readValue(inptstrm, ty);

		if (readValueobj != null & !readValueobj.isEmpty()) {


			RequestInfo requestinfo = null;


			for (Object object : readValueobj) {

				PropertyRequest request = null;

				Property property = new Property();

				LinkedHashMap<String, Object> da = (LinkedHashMap<String, Object>) object;

//            	if(da.get("ward").toString().equalsIgnoreCase(ward)) {
				OldProperty oldProperty = new OldProperty();

				String tenantId = "up." + (da.get("city").toString()).toLowerCase();

				property.setId(UUID.randomUUID().toString());
				property.setPropertyId(da.get("propertyUniqueId").toString());
				property.setTenantId(tenantId);
				property.setAccountId(requestInfo2.getUserInfo().getUuid());
				property.setOldPropertyId(da.get("propertyUniqueId").toString());
				property.setStatus(Status.ACTIVE);

//                  if(oldProperty.getAddress()!=null)
				property.setAddress(migrateAddress(da));
////                  else
//                      property.setAddress(null);
				property.setAcknowldgementNumber(oldProperty.getAcknowldgementNumber());

//                  if(oldProperty.getPropertyDetails().get(i) != null){
//                      property.setPropertyType(migratePropertyType(oldProperty.getPropertyDetails().get(i)));
				property.setPropertyType((PTConstants.PT_TYPE_BUILTUP));
//                      property.setOwnershipCategory(migrateOwnwershipCategory(oldProperty.getPropertyDetails().get(i)));
				property.setOwnershipCategory(da.get("respondentStatus").toString());
//                      property.setUsageCategory(migrateUsageCategory(oldProperty.getPropertyDetails().get(i)));
				property.setUsageCategory((da.get("houseCategory").toString()));
//                  }
//                  else{
//                      property.setPropertyType(null);
//                      property.setOwnershipCategory(null);
//                      property.setUsageCategory(null);
//                  }

//                  if(oldProperty.getPropertyDetails().get(i).getInstitution() == null)
				property.setInstitution(null);
//                  else
//                      property.setInstitution(migrateInstitution(oldProperty.getPropertyDetails().get(i).getInstitution()));

//                  if(!StringUtils.isEmpty(oldProperty.getCreationReason()))
//                      property.setCreationReason(CreationReason.fromValue(String.valueOf(oldProperty.getCreationReason())));
				property.setCreationReason(CreationReason.CREATE);

//                  property.setNoOfFloors(oldProperty.getPropertyDetails().get(i).getNoOfFloors());
				property.setNoOfFloors(1L);

//                  if(!StringUtils.isEmpty(oldProperty.getPropertyDetails().get(i).getBuildUpArea()))
//                      property.setSuperBuiltUpArea(BigDecimal.valueOf(oldProperty.getPropertyDetails().get(i).getBuildUpArea()));

//                  property.setSuperBuiltUpArea( BigDecimal.valueOf((long) da.get("totalbuildingArea")));

//                  		if(!StringUtils.isEmpty(oldProperty.getPropertyDetails().get(i).getLandArea()))
//                      property.setLandArea(Double.valueOf(oldProperty.getPropertyDetails().get(i).getLandArea()));
				property.setLandArea(Double.parseDouble(da.get("plotArea").toString()));

//                  if(!StringUtils.isEmpty(Source.fromValue(String.valueOf(oldProperty.getPropertyDetails().get(i).getSource()))))
//                      property.setSource(Source.fromValue(String.valueOf(oldProperty.getPropertyDetails().get(i).getSource())));
//                  else
				property.setSource(org.egov.pt.models.enums.Source.FIELD_SURVEY);

//                  if(!StringUtils.isEmpty(Channel.fromValue(String.valueOf(oldProperty.getPropertyDetails().get(i).getChannel()))))
//                      property.setChannel(Channel.fromValue(String.valueOf(oldProperty.getPropertyDetails().get(i).getChannel())));
//                  else
				property.setChannel(Channel.fromValue("MIGRATION"));

//                  if(oldProperty.getPropertyDetails().get(i).getDocuments() == null)
//                      property.setDocuments(null);
//                  else
//                      property.setDocuments(migrateDocument());

				List<org.egov.pt.models.Unit> units = new ArrayList<>();
//                  if(oldProperty.getPropertyDetails().get(i).getUnits() == null)
//                      property.setUnits(null);
//                  else{
				units = migrateUnit((List<Object>) da.get("floorAreaSplits"), tenantId);
				property.setUnits(units);

//                  }

//                  if(oldProperty.getPropertyDetails().get(i).getAdditionalDetails() == null)
				property.setAdditionalDetails(null);
//                  else{
//                      JsonNode additionalDetails = mapper.convertValue(oldProperty.getPropertyDetails().get(i).getAdditionalDetails(),JsonNode.class);
//                      property.setAdditionalDetails(additionalDetails);
//                  }

//                  if( oldProperty.getPropertyDetails().get(i).getAuditDetails() == null)
//                      property.setAuditDetails(null);
//                  else
				property.setAuditDetails(migrateAuditDetails(requestInfo2));

//                  if(oldProperty.getPropertyDetails().get(i).getOwners()!=null){
				List<OwnerInfo> ownerInfos = migrateOwnerInfo(da);
				property.setOwners(ownerInfos);
//                  }
//                  else
//                      property.setOwners(null);

				request = PropertyRequest.builder().requestInfo(requestInfo2).property(property).build();
//                  try{
//                      propertyMigrationValidator.validatePropertyCreateRequest(request,masters,errorMap);
//                  } catch (Exception e) {
//                     /*errorMap.put(property.getPropertyId(), String.valueOf(e));
//                     throw new CustomException(errorMap);*/
//                  }


				producer.push(config.getSavePropertyTopic(), request);
				properties.add(property);

//                  if(oldProperty.getPropertyDetails().get(i)!=null)
//                      migrateAssesment(oldProperty.getPropertyDetails().get(i),property,requestInfo,errorMap,masters,units);


			}

			return properties;
//     			System.out.println(readValueobj.get(0)); 
		} else {
			System.out.println("Get Error !!!!!!!!");
		}

		return properties;

	}

	public org.egov.pt.models.Address migrateAddress(LinkedHashMap<String, Object> da) {
		org.egov.pt.models.Address address = new org.egov.pt.models.Address();
		address.setTenantId("up." + (da.get("city").toString()).toLowerCase());
		address.setDoorNo(null);
		// address.setPlotNo();
		address.setId(UUID.randomUUID().toString());
		address.setLandmark(null);
		address.setCity(da.get("city").toString());
		// address.setDistrict();
		// address.setRegion();
		// address.setState();
		// address.setCountry();
		address.setPincode(null);
		address.setBuildingName(null);
		address.setStreet(da.get("city").toString());
		address.setLocality(migrateLocality(da));
		// address.setAdditionalDetails(oldAddress.getAdditionalDetails());
		address.setGeoLocation(migrateGeoLocation(da));

		return address;
	}

	public Locality migrateLocality(LinkedHashMap<String, Object> da) {
		Locality locality = new Locality();
		locality.setCode("BAR001");
		locality.setName(da.get("city").toString());
//	        locality.setLabel(oldLocality.getLabel());
//	        locality.setLatitude(oldLocality.getLatitude());
//	        locality.setLongitude(oldLocality.getLongitude());
		locality.setArea(da.get("city").toString());
//	        locality.setMaterializedPath(oldLocality.getMaterializedPath());
//	        if(oldLocality.getChildren() != null)
//	            locality.setChildren(setmigrateLocalityList(oldLocality.getChildren()));
//	        else
//	            locality.setChildren(null);
		return locality;
	}

	public GeoLocation migrateGeoLocation(LinkedHashMap<String, Object> da) {
		GeoLocation geoLocation = new GeoLocation();
		if (da.get("latitude") == null)
			geoLocation.setLatitude(null);
		else
			geoLocation.setLatitude(Double.valueOf(da.get("latitude").toString()));

		if (da.get("longitude") == null)
			geoLocation.setLongitude(null);
		else
			geoLocation.setLongitude(Double.valueOf(da.get("longitude").toString()));
		return geoLocation;
	}

//	   public String migratePropertyType(PropertyDetail propertyDetail){
//	        StringBuilder propertyType = new StringBuilder();
//	        if(StringUtils.isEmpty(propertyDetail.getPropertyType()))
//	            return null;
//	        else
//	            propertyType.append(propertyDetail.getPropertyType());
//
//	        if(!StringUtils.isEmpty(propertyDetail.getPropertySubType()))
//	            propertyType.append(".").append(propertyDetail.getPropertySubType());
//
//	        return propertyType.toString();
//	    }

//	   public String migrateOwnwershipCategory(PropertyDetail propertyDetail){
//	        StringBuilder ownershipCategory = new StringBuilder();
//	        if(StringUtils.isEmpty(propertyDetail.getOwnershipCategory()))
//	            return null;
//	        else
//	            ownershipCategory.append(propertyDetail.getOwnershipCategory());
//
//	        if(!StringUtils.isEmpty(propertyDetail.getSubOwnershipCategory()))
//	            ownershipCategory.append(".").append(propertyDetail.getSubOwnershipCategory());
//
//	        return ownershipCategory.toString();
//	    }

//	    public String migrateUsageCategory(PropertyDetail propertyDetail){
//	        StringBuilder usageCategory = new StringBuilder();
//
//	        if(StringUtils.isEmpty(propertyDetail.getUsageCategoryMajor()))
//	            return null;
//	        else
//	            usageCategory.append(propertyDetail.getUsageCategoryMajor());
//
//	        if(!StringUtils.isEmpty(propertyDetail.getUsageCategoryMinor()))
//	            usageCategory.append(".").append(propertyDetail.getUsageCategoryMinor());
//
//	        return usageCategory.toString();
//	    }

//	    public List<Document> migrateDocument(Set<OldDocument> oldDocumentList){
//	        List<Document> documentList = new ArrayList<>();
//	        for(OldDocument oldDocument: oldDocumentList){
//	            Document doc = new Document();
//	            doc.setId(UUID.randomUUID().toString());
//	            doc.setDocumentType(oldDocument.getDocumentType());
//	            if(oldDocument.getFileStore() == null)
//	                doc.setFileStoreId(oldDocument.getId());
//	            else
//	                doc.setFileStoreId(oldDocument.getFileStore());
//	            if(oldDocument.getDocumentUid() == null)
//	                doc.setDocumentUid(oldDocument.getId());
//	            else
//	                doc.setDocumentUid(oldDocument.getDocumentUid());
//	            documentList.add(doc);
//	        }
//	        return  documentList;
//	    }

	public List<org.egov.pt.models.Unit> migrateUnit(List<Object> oldUnits, String tenantId) {
		List<org.egov.pt.models.Unit> units = new ArrayList<>();
		for (Object oldUnit : oldUnits) {
			LinkedHashMap<String, Object> da = (LinkedHashMap<String, Object>) oldUnit;
			org.egov.pt.models.Unit unit = new org.egov.pt.models.Unit();
			unit.setId(UUID.randomUUID().toString());
			unit.setTenantId(tenantId);

			Integer floor = (Integer) da.get("floorNo");
			unit.setFloorNo(floor);
			unit.setUnitType("OTHERS");
			unit.setUsageCategory("OTHERS");
			unit.setOccupancyType("SELFOCCUPIED");
			unit.setOccupancyDate(0L);
//	            if(oldUnit.getActive() == null)
			unit.setActive(Boolean.TRUE);
//	            else
//	                unit.setActive(oldUnit.getActive());
			unit.setConstructionDetail(migrateConstructionDetail(da));
			// unit.setAdditionalDetails(oldUnit.getAdditionalDetails());
			// unit.setAuditDetails();
			
//			String totalARVinString = da.get("totalARV").toString();
//	            unit.setArv(new BigDecimal(totalARVinString));
			units.add(unit);
		}

		return units;
	}

	public ConstructionDetail migrateConstructionDetail(LinkedHashMap<String, Object> da) {
		ConstructionDetail constructionDetail = new ConstructionDetail();
		
//		String totalCarpetAreainString = da.get("totalCarpetArea").toString();
//	        constructionDetail.setBuiltUpArea( new BigDecimal(totalCarpetAreainString));
	       
//	        String riAreainString = da.get("riArea").toString();
//	        constructionDetail.setCarpetArea(new BigDecimal(riAreainString));
		
	        constructionDetail.setConstructionType(da.get("constType").toString());
	        

//	        if ((da.get("constType").toString()) == null){
//	            constructionDetail.setConstructionType(null); )
//	            return constructionDetail;
//	        }

//	        StringBuilder constructionType = new StringBuilder(oldUnit.getConstructionType());
//	        if(oldUnit.getConstructionSubType() != null)
//	            constructionType.append(".").append(oldUnit.getConstructionSubType());
//	        constructionDetail.setConstructionType(constructionType.toString());

		return constructionDetail;
	}

	public List<OwnerInfo> migrateOwnerInfo(LinkedHashMap<String, Object> da) {
		List<OwnerInfo> ownerInfolist = new ArrayList<>();
//	        for(OldOwnerInfo oldOwnerInfo : oldOwnerInfosSet){
		OwnerInfo ownerInfo = new OwnerInfo();
		ownerInfo.setId(1692790963510L);
		ownerInfo.setOwnerInfoUuid(UUID.randomUUID().toString());
		ownerInfo.setUuid(UUID.randomUUID().toString());
		ownerInfo.setUserName("Test");
		ownerInfo.setPassword(null);
		ownerInfo.setSalutation(null);
		ownerInfo.setName(da.get("ownerName").toString());
		ownerInfo.setEmailId(null);
		ownerInfo.setAltContactNumber(null);
		ownerInfo.setPan(null);
		ownerInfo.setAadhaarNumber(null);
		ownerInfo.setPermanentAddress(da.get("propertyAddress").toString());
		ownerInfo.setPermanentCity(da.get("city").toString());
		ownerInfo.setPermanentPincode(null);
		ownerInfo.setCorrespondenceAddress(da.get("propertyAddress").toString());
//	            ownerInfo.setCorrespondenceCity(oldOwnerInfo.getCorrespondenceCity());
//	            ownerInfo.setCorrespondencePincode(oldOwnerInfo.getCorrespondencePincode());
//	            ownerInfo.setActive(oldOwnerInfo.getActive());
//	            ownerInfo.setDob(oldOwnerInfo.getDob());
//	            ownerInfo.setPwdExpiryDate(oldOwnerInfo.getPwdExpiryDate());
//	            ownerInfo.setLocale(oldOwnerInfo.getLocale());
//	            ownerInfo.setType(oldOwnerInfo.getType());
//	            ownerInfo.setSignature(oldOwnerInfo.getSignature());
//	            ownerInfo.setAccountLocked(oldOwnerInfo.getAccountLocked());
//	            ownerInfo.setRoles(oldOwnerInfo.getRoles());
//	            ownerInfo.setBloodGroup(oldOwnerInfo.getBloodGroup());
//	            ownerInfo.setIdentificationMark(oldOwnerInfo.getIdentificationMark());
//	            ownerInfo.setPhoto(oldOwnerInfo.getPhoto());
//	            ownerInfo.setCreatedBy(oldOwnerInfo.getCreatedBy());
//	            ownerInfo.setCreatedDate(oldOwnerInfo.getCreatedDate());
//	            ownerInfo.setLastModifiedBy(oldOwnerInfo.getLastModifiedBy());
//	            ownerInfo.setLastModifiedDate(oldOwnerInfo.getLastModifiedDate());
//	            ownerInfo.setTenantId(oldOwnerInfo.getTenantId());
//	            ownerInfo.setOwnerInfoUuid(UUID.randomUUID().toString());
//	            ownerInfo.setMobileNumber(oldOwnerInfo.getMobileNumber());
//	            ownerInfo.setGender(oldOwnerInfo.getGender());
//	            ownerInfo.setFatherOrHusbandName(oldOwnerInfo.getFatherOrHusbandName());
//	            ownerInfo.setCorrespondenceAddress(oldOwnerInfo.getCorrespondenceAddress());
//	            ownerInfo.setIsPrimaryOwner(oldOwnerInfo.getIsPrimaryOwner());
//	            ownerInfo.setOwnerShipPercentage(oldOwnerInfo.getOwnerShipPercentage());
		ownerInfo.setOwnerType("FREEDOMFIGHTER");
//	            ownerInfo.setInstitutionId(oldOwnerInfo.getInstitutionId());
		ownerInfo.setStatus(Status.ACTIVE);
//	            if(oldOwnerInfo.getOldDocuments() == null)
		ownerInfo.setDocuments(null);
//	            else
//	                ownerInfo.setDocuments(migrateDocument(oldOwnerInfo.getOldDocuments()));

//	            ownerInfo.setRelationship(da.get(ownerInfo));


		ownerInfolist.add(ownerInfo);


		return ownerInfolist;
	}

	public AuditDetails migrateAuditDetails(RequestInfo requestInfo2) {
		AuditDetails details = new AuditDetails();
		details.setCreatedBy(requestInfo2.getUserInfo().getId().toString());
		details.setCreatedTime(new Date().getTime());
		details.setLastModifiedBy(requestInfo2.getUserInfo().getId().toString());
		details.setLastModifiedTime(new Date().getTime());
		return details;
	}
}
