package org.egov.pt.web.controllers;

import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.validation.Valid;

import org.egov.common.contract.response.ResponseInfo;
import org.egov.pt.config.PropertyConfiguration;
import org.egov.pt.models.Assessment;
import org.egov.pt.models.Property;
import org.egov.pt.models.PropertyCriteria;
import org.egov.pt.models.Assessment.Source;
import org.egov.pt.models.enums.Channel;
import org.egov.pt.models.oldProperty.OldPropertyCriteria;
import org.egov.pt.models.oldProperty.PropertyDetail.ChannelEnum;
import org.egov.pt.service.BoundaryJsonGenerationService;
import org.egov.pt.service.CalculationService;
import org.egov.pt.service.MigrationService;
import org.egov.pt.service.NiveshMitraReportService;
import org.egov.pt.service.PropertyService;
import org.egov.pt.service.UPMigrationService;
import org.egov.pt.util.ResponseInfoFactory;
import org.egov.pt.validator.PropertyValidator;
import org.egov.pt.web.contracts.AssessmentRequest;
import org.egov.pt.web.contracts.CalculationRes;
import org.egov.pt.web.contracts.NiveshMitraMutationResponse;
import org.egov.pt.web.contracts.PropertyRequest;
import org.egov.pt.web.contracts.PropertyResponse;
import org.egov.pt.web.contracts.RequestInfoWrapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;

@Controller
@RequestMapping("/property-reports")
public class NiveshMitraReportsController {

	final ClassLoader loader = NiveshMitraReportsController.class.getClassLoader();

	@Autowired
	private NiveshMitraReportService reportService;

	@Autowired
	private UPMigrationService upMigrationService;

	@Autowired
	private ResponseInfoFactory responseInfoFactory;

	@Autowired
	private MigrationService migrationService;

	@Autowired
	private PropertyValidator propertyValidator;
	
	@Autowired
	private PropertyConfiguration config;
	
	@Autowired
	private BoundaryJsonGenerationService boundaryJson ;
	
	@Autowired
	private CalculationService calculationService;

	@GetMapping("/mutationCheck/{pID}")
	public ResponseEntity<NiveshMitraMutationResponse> mutationCheck(@PathVariable (required = true, name="pID") String propertyID) {
		PropertyCriteria propertyCriteria = new PropertyCriteria();
		propertyCriteria.setPropertyIds(Collections.singleton(propertyID));
		List<Property> properties = reportService.searchProperty(propertyCriteria);


		NiveshMitraMutationResponse resp = new NiveshMitraMutationResponse();
		Date date = new Date();  
		SimpleDateFormat formatter = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss");  
		String asOnDate= formatter.format(date);  
		resp.setAsOnDateTimeStr(asOnDate);
		resp.setValid("NO");
		resp.setPropertyID(propertyID);
		resp.setUibcode("");

		if (properties != null && !properties.isEmpty()) {
			Property prop = properties.get(0);
			AssessmentRequest assessmentReq = new AssessmentRequest();
			Assessment assessment= new Assessment();
			assessment.setChannel(Channel.CFC_COUNTER);
			assessment.setFinancialYear("2019-20");
			assessment.setPropertyId(propertyID);
			assessment.setSource(Source.MUNICIPAL_RECORDS);
			assessment.setTenantId(prop.getTenantId());

			assessmentReq.setAssessment(assessment);
			assessmentReq.setRequestInfo(reportService.getRequestInfo());
			CalculationRes res = calculationService.getEstimate(assessmentReq);
			if (res != null) {
				resp.setValid("YES");
				resp.setFullhousetaxpaid( res.getCalculation().get(0).getTotalAmount().doubleValue() > 0 ? "NO" : "YES");
				resp.setZoneName(prop.getAddress().getZone());
				resp.setWardName(prop.getAddress().getWard());
				resp.setMohallaName(prop.getAddress().getLocality().getName());
				resp.setAddress(prop.getAddress().getDoorNo());
				resp.setAreaOfLand(prop.getLandArea().toString());
				resp.setMobile(prop.getOwners().get(0).getMobileNumber());
				resp.setOwnerName(prop.getOwners().get(0).getName());
				resp.setFatherName(prop.getOwners().get(0).getFatherOrHusbandName());
			} 
		} 

		return new ResponseEntity<>(resp, HttpStatus.OK);
	}

	@PostMapping("/_search")
	public ResponseEntity<PropertyResponse> search(@Valid @RequestBody RequestInfoWrapper requestInfoWrapper,
			@Valid @ModelAttribute PropertyCriteria propertyCriteria) {

		if (propertyCriteria.getLocality() != null && !propertyCriteria.getLocality().isEmpty()) {
			propertyCriteria.setStreet(propertyCriteria.getLocality());
		}

		propertyValidator.validatePropertyCriteria(propertyCriteria, requestInfoWrapper.getRequestInfo());
		List<Property> properties = propertyService.searchProperty(propertyCriteria,
				requestInfoWrapper.getRequestInfo());
		PropertyResponse response = PropertyResponse.builder().properties(properties).responseInfo(
				responseInfoFactory.createResponseInfoFromRequestInfo(requestInfoWrapper.getRequestInfo(), true))
				.build();
		return new ResponseEntity<>(response, HttpStatus.OK);
	}

	@PostMapping("/_migration")
	public ResponseEntity<?> propertyMigration(@Valid @RequestBody RequestInfoWrapper requestInfoWrapper,
			@Valid @ModelAttribute OldPropertyCriteria propertyCriteria) {
		long startTime = System.nanoTime();
		Map<String, String> resultMap = null;
		Map<String, String> errorMap = new HashMap<>();

		resultMap = migrationService.initiateProcess(requestInfoWrapper, propertyCriteria, errorMap);

		long endtime = System.nanoTime();
		long elapsetime = endtime - startTime;
		System.out.println("Elapsed time--->" + elapsetime);

		return new ResponseEntity<>(resultMap, HttpStatus.OK);
	}

	@RequestMapping(value = "/_plainsearch", method = RequestMethod.POST)
	public ResponseEntity<PropertyResponse> plainsearch(@Valid @RequestBody RequestInfoWrapper requestInfoWrapper,
			@Valid @ModelAttribute PropertyCriteria propertyCriteria) {
		List<Property> properties = propertyService.searchPropertyPlainSearch(propertyCriteria,
				requestInfoWrapper.getRequestInfo());
		PropertyResponse response = PropertyResponse.builder().properties(properties).responseInfo(
				responseInfoFactory.createResponseInfoFromRequestInfo(requestInfoWrapper.getRequestInfo(), true))
				.build();
		return new ResponseEntity<>(response, HttpStatus.OK);
	}
	// @RequestMapping(value = "/_cancel", method = RequestMethod.POST)
	// public ResponseEntity<PropertyResponse> cancel(@Valid @RequestBody
	// RequestInfoWrapper requestInfoWrapper,
	// @Valid @ModelAttribute PropertyCancelCriteria propertyCancelCriteria) {
	//
	// List<Property> properties =
	// propertyService.cancelProperty(propertyCancelCriteria,requestInfoWrapper.getRequestInfo());
	// PropertyResponse response =
	// PropertyResponse.builder().properties(properties).responseInfo(
	// responseInfoFactory.createResponseInfoFromRequestInfo(requestInfoWrapper.getRequestInfo(),
	// true))
	// .build();
	// return new ResponseEntity<>(response, HttpStatus.OK);
	// }

	@PostMapping("/_import")
	public ResponseEntity<?> propertyImport(@RequestParam(required = false) Long limit,
			@RequestParam(required = false, defaultValue = "1") Long skip ,@RequestParam(required = false, defaultValue = "false") Boolean parallelProcessing ) throws Exception {
		long startTime = System.nanoTime();
		final InputStream excelFile = loader.getResourceAsStream(config.getMigrationFileName());
		
		final InputStream matchedFile = loader.getResourceAsStream("matched.csv");
	
		if(parallelProcessing)			
		upMigrationService.importPropertiesParallel(excelFile, matchedFile, skip, limit);
		else
		upMigrationService.importProperties(excelFile, matchedFile, skip, limit);
		
		long endtime = System.nanoTime();
		long elapsetime = endtime - startTime;
		System.out.println("Elapsed time--->" + elapsetime);

		return new ResponseEntity<>(true, HttpStatus.OK);
	}
	
	@PostMapping("/_createboundaries")
	public ResponseEntity<?> createBoundariesLocalityJson(@RequestParam(required = true) String tenantId,
			@RequestParam(required = false, defaultValue = "egov-location") String moduleName ,@RequestParam(required = true) String cityName ) throws Exception {
		long startTime = System.nanoTime();
		
		JsonObject result = boundaryJson.createBoundaryJson(tenantId, moduleName, cityName);
		String jsonOutput = "" ;
		if(result != null)
		{
		Gson gson = new GsonBuilder().setPrettyPrinting().create();
		 jsonOutput = gson.toJson(result);
		}
		
		long endtime = System.nanoTime();
		long elapsetime = endtime - startTime;
		System.out.println("Elapsed time--->" + elapsetime);

		return new ResponseEntity<>(jsonOutput, HttpStatus.OK);
	}
	
	@PostMapping("/_createcategories")
	public ResponseEntity<?> createCategoriesJson(@RequestParam(required = false, defaultValue = "category") String label ) throws Exception {
		long startTime = System.nanoTime();
		
		JsonObject categoriesArrayObj = boundaryJson.generateCategoriesJson(label);
		String jsonOutput = "" ;
		if(categoriesArrayObj != null)
		{
		Gson gson = new GsonBuilder().setPrettyPrinting().create();
		jsonOutput = gson.toJson(categoriesArrayObj);
		}
		
		long endtime = System.nanoTime();
		long elapsetime = endtime - startTime;
		System.out.println("Elapsed time--->" + elapsetime);

		return new ResponseEntity<>(jsonOutput, HttpStatus.OK);
	}
	
}
