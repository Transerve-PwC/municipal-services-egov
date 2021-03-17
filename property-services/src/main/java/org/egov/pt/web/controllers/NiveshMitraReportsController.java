package org.egov.pt.web.controllers;

import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;

import javax.validation.Valid;

import org.egov.common.contract.request.RequestInfo;
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
import org.egov.pt.service.Cachebaleservice;
import org.egov.pt.service.CalculationService;
import org.egov.pt.service.MigrationService;
import org.egov.pt.service.NiveshMitraReportService;
import org.egov.pt.service.PropertyService;
import org.egov.pt.service.UPMigrationService;
import org.egov.pt.util.CommonUtils;
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


	@Autowired
	private NiveshMitraReportService reportService;


	@Autowired
	private CalculationService calculationService;
	
	


	@GetMapping("/mutationCheck")
	public ResponseEntity<NiveshMitraMutationResponse> mutationCheck(@RequestParam(required = true) String propertyID ) {
		PropertyCriteria propertyCriteria = new PropertyCriteria();
		propertyCriteria.setPropertyIds(Collections.singleton(propertyID));
		RequestInfo requestInfo = reportService.getRequestInfo();
		
		List<Property> properties = reportService.searchProperty(propertyCriteria ,requestInfo );


		NiveshMitraMutationResponse resp = new NiveshMitraMutationResponse();
		Date date = new Date();  
		TimeZone timezone = TimeZone.getTimeZone("Asia/Kolkata");     
		SimpleDateFormat formatter = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss");  
		formatter.setTimeZone(timezone);
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
			assessment.setFinancialYear(CommonUtils.currentFinancialYear());
			assessment.setPropertyId(propertyID);
			assessment.setSource(Source.MUNICIPAL_RECORDS);
			assessment.setTenantId(prop.getTenantId());

			assessmentReq.setAssessment(assessment);
			assessmentReq.setRequestInfo(requestInfo);
			CalculationRes res = calculationService.getEstimate(assessmentReq);
			if (res != null) {
				resp.setValid("YES");
				resp.setFullhousetaxpaid( res.getCalculation().get(0).getTotalAmount().doubleValue() > 0.0 ? "NO" : "YES");

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

	
	
}
