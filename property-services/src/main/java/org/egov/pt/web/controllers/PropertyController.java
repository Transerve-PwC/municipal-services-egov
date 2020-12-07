package org.egov.pt.web.controllers;

import java.io.InputStream;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.validation.Valid;

import org.egov.common.contract.response.ResponseInfo;
import org.egov.pt.config.PropertyConfiguration;
import org.egov.pt.models.Property;
import org.egov.pt.models.PropertyCriteria;
import org.egov.pt.models.oldProperty.OldPropertyCriteria;
import org.egov.pt.service.BoundaryJsonGenerationService;
import org.egov.pt.service.MigrationService;
import org.egov.pt.service.PropertyService;
import org.egov.pt.service.UPMigrationService;
import org.egov.pt.util.ResponseInfoFactory;
import org.egov.pt.validator.PropertyValidator;
import org.egov.pt.web.contracts.PropertyRequest;
import org.egov.pt.web.contracts.PropertyResponse;
import org.egov.pt.web.contracts.RequestInfoWrapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;

@Controller
@RequestMapping("/property")
public class PropertyController {

	final ClassLoader loader = PropertyController.class.getClassLoader();

	@Autowired
	private PropertyService propertyService;

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
	

	@PostMapping("/_create")
	public ResponseEntity<PropertyResponse> create(@Valid @RequestBody PropertyRequest propertyRequest) {

		Property property = propertyService.createProperty(propertyRequest);
		ResponseInfo resInfo = responseInfoFactory.createResponseInfoFromRequestInfo(propertyRequest.getRequestInfo(),
				true);
		PropertyResponse response = PropertyResponse.builder().properties(Arrays.asList(property)).responseInfo(resInfo)
				.build();
		return new ResponseEntity<>(response, HttpStatus.CREATED);
	}

	@PostMapping("/_update")
	public ResponseEntity<PropertyResponse> update(@Valid @RequestBody PropertyRequest propertyRequest) {

		Property property = propertyService.updateProperty(propertyRequest);
		ResponseInfo resInfo = responseInfoFactory.createResponseInfoFromRequestInfo(propertyRequest.getRequestInfo(),
				true);
		PropertyResponse response = PropertyResponse.builder().properties(Arrays.asList(property)).responseInfo(resInfo)
				.build();
		return new ResponseEntity<>(response, HttpStatus.OK);
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
