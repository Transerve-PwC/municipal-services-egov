package org.egov.pt.web.controllers;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.TimeZone;
import java.util.stream.Collectors;

import org.egov.common.contract.request.RequestInfo;
import org.egov.pt.models.Assessment;
import org.egov.pt.models.Property;
import org.egov.pt.models.PropertyCriteria;
import org.egov.pt.models.Assessment.Source;
import org.egov.pt.models.enums.Channel;
import org.egov.pt.repository.NiveshMitraReportsRepository;
import org.egov.pt.service.Cachebaleservice;
import org.egov.pt.service.CalculationService;
import org.egov.pt.service.NiveshMitraReportService;
import org.egov.pt.util.CommonUtils;
import org.egov.pt.web.contracts.AssessmentRequest;
import org.egov.pt.web.contracts.CalculationRes;
import org.egov.pt.web.contracts.NiveshMitraMutationResponse;
import org.egov.pt.web.contracts.NiveshMitraDemandResponse;
import org.egov.pt.web.contracts.NiveshMitraTaxResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequestMapping("/property-reports")
public class NiveshMitraReportsController {

	@Autowired
	private NiveshMitraReportService reportService;

	@Autowired
	private CalculationService calculationService;

	@Autowired
	private NiveshMitraReportsRepository repository;

	@Autowired
	 private Cachebaleservice cacheService;

	@GetMapping("/mutationCheck")
	public ResponseEntity<NiveshMitraMutationResponse> mutationCheck(@RequestParam(required = true) String propertyID) {
		PropertyCriteria propertyCriteria = new PropertyCriteria();
		propertyCriteria.setPropertyIds(Collections.singleton(propertyID));
		RequestInfo requestInfo = reportService.getRequestInfo();

		List<Property> properties = reportService.searchProperty(propertyCriteria, requestInfo);

		NiveshMitraMutationResponse resp = new NiveshMitraMutationResponse();
		Date date = new Date();
		TimeZone timezone = TimeZone.getTimeZone("Asia/Kolkata");
		SimpleDateFormat formatter = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss");
		formatter.setTimeZone(timezone);
		String asOnDate = formatter.format(date);
		resp.setAsOnDateTimeStr(asOnDate);
		resp.setValid("NO");
		resp.setPropertyID(propertyID);
		resp.setUlbcode("");

		if (properties != null && !properties.isEmpty()) {
			Property prop = properties.get(0);
			AssessmentRequest assessmentReq = new AssessmentRequest();
			Assessment assessment = new Assessment();
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
				resp.setFullhousetaxpaid(
						res.getCalculation().get(0).getTotalAmount().doubleValue() > 0.0 ? "NO" : "YES");

				resp.setZoneName(prop.getAddress().getZone());
				resp.setWardName(cacheService.getWardNameFromLocalityCode(prop.getAddress().getLocality().getCode(), prop.getTenantId(), requestInfo));
				resp.setMohallaName(prop.getAddress().getLocality().getName());
				resp.setAddress(prop.getAddress().getDoorNo());
				resp.setAreaOfLand(prop.getLandArea().toString());
				resp.setMobile(prop.getOwners().get(0).getMobileNumber());
				resp.setOwnerName(prop.getOwners().get(0).getName());
				resp.setFatherName(prop.getOwners().get(0).getFatherOrHusbandName());
				resp.setUlbcode(CommonUtils.getULBCodeForTenantId(prop.getTenantId()));
			}
		}

		return new ResponseEntity<>(resp, HttpStatus.OK);
	}

	@GetMapping("/houseTaxData")
	public ResponseEntity<?> houseTaxData() {
		RequestInfo requestInfo = reportService.getRequestInfo();
		Map<String, String> financeData = cacheService.getFinancialYearData(CommonUtils.currentFinancialYear(), requestInfo);

		ArrayList<NiveshMitraTaxResponse> taxCollectionRespArray = new ArrayList<NiveshMitraTaxResponse> ();

		List<HashMap<String, String>> resp = repository.getHouseTaxData(String.valueOf(financeData.get("startingDate")),String.valueOf(financeData.get("endingDate")));

		if (resp != null) {
			Iterator<HashMap<String, String>> iterator = resp.iterator();
			while (iterator.hasNext()) {
				HashMap<String, String> houseTaxObj = iterator.next();
				NiveshMitraTaxResponse taxResp = NiveshMitraTaxResponse.taxRespObjFromHashMap(houseTaxObj);
				taxCollectionRespArray.add(taxResp);
			}
		}
		return new ResponseEntity<>(taxCollectionRespArray, HttpStatus.OK);
	}

	@GetMapping("/waterTaxData")
	public ResponseEntity<?> waterTaxData() {
		RequestInfo requestInfo = reportService.getRequestInfo();
		Map<String, String> financeData = cacheService.getFinancialYearData(CommonUtils.currentFinancialYear(), requestInfo);
		
		ArrayList<NiveshMitraTaxResponse> taxCollectionRespArray = new ArrayList<NiveshMitraTaxResponse> ();

		List<HashMap<String, String>> resp = repository.getWaterTaxData(String.valueOf(financeData.get("startingDate")),String.valueOf(financeData.get("endingDate")));

		if (resp != null) {
			Iterator<HashMap<String, String>> iterator = resp.iterator();
			while (iterator.hasNext()) {
				HashMap<String, String> houseTaxObj = iterator.next();
				NiveshMitraTaxResponse taxResp = NiveshMitraTaxResponse.taxRespObjFromHashMap(houseTaxObj);
				taxCollectionRespArray.add(taxResp);
			}
		}
		return new ResponseEntity<>(taxCollectionRespArray, HttpStatus.OK);
	}

	// @GetMapping("/ptCountDemand1")
	// public ResponseEntity<?> ptCountDemand1() {
	// 	RequestInfo requestInfo = reportService.getRequestInfo();
	// 	Map<String, String> financeData = cacheService.getFinancialYearData(CommonUtils.currentFinancialYear(), requestInfo);
	// 	List<HashMap<String, String>> resp = repository.getDemandData(String.valueOf(financeData.get("startingDate")),String.valueOf(financeData.get("endingDate")));
	// 	Date date = new Date();
	// 	TimeZone timezone = TimeZone.getTimeZone("Asia/Kolkata");
	// 	SimpleDateFormat formatter = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss");
	// 	formatter.setTimeZone(timezone);
	// 	String asOnDate = formatter.format(date);

	// 	ArrayList<Object> taxCollectionRespArray = null;
	// 	if (resp != null) {

	// 		HashMap<String, NiveshMitraDemandResponse> acc = new HashMap<String, NiveshMitraDemandResponse>();
	// 		resp.stream().reduce(acc, (initialVal, m2) -> {
	// 			if( initialVal.containsKey(m2.get("tenantid"))) {

	// 			} else {

	// 			}
	// 			return acc;
	// 		});
	// 		// Map<String, HashMap<String,NiveshMitraDemandResponse>> result = 
	// 		// resp.stream().collect(Collectors.groupingBy (map -> map.get("tenantid").toString(),
    //         //                    Collectors.reducing ( new HashMap<String,NiveshMitraDemandResponse>(), m1,m2-> {
	// 		// 					NiveshMitraDemandResponse rp1 = new NiveshMitraDemandResponse();
	// 		// 						if (m1.getClass() == rp1.getClass()) {
	// 		// 							rp1 = (NiveshMitraDemandResponse) m1;
	// 		// 						} else {
	// 		// 							rp1 = NiveshMitraDemandResponse.objFromDemandRepositoryHashMap((HashMap<String, String>)m1);
	// 		// 						}
	// 		// 						NiveshMitraDemandResponse rp2 =  NiveshMitraDemandResponse.objFromDemandRepositoryHashMap((HashMap<String, String>)m2);
	// 		// 						NiveshMitraDemandResponse.mergeDemandResponses(rp1, rp2);
	// 		// 						rp1.setAsOnDateTimeStr(asOnDate);
	// 		// 						rp1.setUlbcode(rp2.getUlbcode());
	// 		// 						rp1.setFinancialYear(CommonUtils.currentFinancialYear());
    //         //                        return rp1;
    //         //                    })));

	// 		// System.out.println("Values in the reduced map is" + taxCollectionRespArray.toString());
	// 		// taxCollectionRespArray = new ArrayList<Object>(result.values());


	// 		// System.out.println("Values in the reduced map is" + taxCollectionRespArray.toString());
	// 	}

	// 	return new ResponseEntity<>(taxCollectionRespArray, HttpStatus.OK);
	// }


	@GetMapping("/ptCountDemand")
	public ResponseEntity<?> ptCountDemand() {
		RequestInfo requestInfo = reportService.getRequestInfo();
		Map<String, String> financeData = cacheService.getFinancialYearData(CommonUtils.currentFinancialYear(), requestInfo);
		List<HashMap<String, String>> resp = repository.getDemandData(String.valueOf(financeData.get("startingDate")),String.valueOf(financeData.get("endingDate")));
		Date date = new Date();
		TimeZone timezone = TimeZone.getTimeZone("Asia/Kolkata");
		SimpleDateFormat formatter = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss");
		formatter.setTimeZone(timezone);
		String asOnDate = formatter.format(date);

		ArrayList<NiveshMitraDemandResponse> taxCollectionRespArray = new ArrayList<NiveshMitraDemandResponse> ();
		if (resp != null) {
			Map<String,Optional<Map<String,String>>> result = 
			resp.stream ().collect(Collectors.groupingBy (map -> map.get("tenantid"),
                               Collectors.reducing ((m1,m2)-> {
								   
                                   Map<String,String> m = new HashMap<>();

								   double amt = CommonUtils.stringToDouble(String.valueOf(m2.get("totalcollection")));
								   int cnt = CommonUtils.stringToInt(String.valueOf(m2.get("count")));
								   double dmdAmt = CommonUtils.stringToDouble(String.valueOf(m2.get("totaldemand")));
								   if (m1.containsKey("ulbCode")) {
										m = m1;
								   } else {
									double m1Amt = CommonUtils.stringToDouble(String.valueOf(m1.get("totalcollection")));
									int m1Cnt = CommonUtils.stringToInt(String.valueOf(m1.get("count")));
									double m1DmdAmt = CommonUtils.stringToDouble(String.valueOf(m1.get("totaldemand")));
										switch (m1.get("usagecategory").toString()) {   
											case "RESIDENTIAL": 
											m.put("residentialAmount", String.valueOf(m1Amt));
											m.put("residentialCount", String.valueOf(m1Cnt));
											m.put("residentialDemand", String.valueOf(m1DmdAmt));
											break;
										case "MIXED":
											m.put("mixedAmount",String.valueOf(m1Amt));
											m.put("mixedCount", String.valueOf(m1Cnt));
											m.put("mixedDemand", String.valueOf(m1DmdAmt));
											break;
										default:
											m.put("commercialAmount", String.valueOf(m1Amt));
											m.put("commercialCount", String.valueOf(m1Cnt));
											m.put("commercialDemand", String.valueOf(m1DmdAmt));
										}
								   }
									switch (m2.get("usagecategory").toString()) {
										case "RESIDENTIAL": 
											m.put("residentialAmount", String.valueOf(CommonUtils.stringToDouble(m1.get("residentialAmount")) + amt));
											m.put("residentialCount", String.valueOf(CommonUtils.stringToInt(m1.get("residentialCount")) + cnt));
											m.put("residentialDemand", String.valueOf(CommonUtils.stringToDouble(m1.get("residentialDemand")) + dmdAmt));
											break;
										case "MIXED":
											m.put("mixedAmount", String.valueOf(CommonUtils.stringToDouble(m1.get("mixedAmount")) + amt));
											m.put("mixedCount", String.valueOf(CommonUtils.stringToInt(m1.get("mixedCount")) + cnt));
											m.put("mixedDemand", String.valueOf(CommonUtils.stringToDouble(m1.get("mixedDemand")) + dmdAmt));
											break;
										default:
											m.put("commercialAmount", String.valueOf(CommonUtils.stringToDouble(m1.get("commercialAmount")) + amt));
											m.put("commercialCount", String.valueOf(CommonUtils.stringToInt(m1.get("commercialCount")) + cnt));
											m.put("commercialDemand", String.valueOf(CommonUtils.stringToDouble(m1.get("commercialDemand")) + dmdAmt));
									}
									m.put("ulbCode", CommonUtils.getULBCodeForTenantId(m2.get("tenantid")));
									m.put("asOnDate", asOnDate);
									m.put("financialYear", CommonUtils.currentFinancialYear());
                                   return m;
                               })));
				System.out.println("Response should be" + result.toString());
				Iterator<Map.Entry<String, Optional<Map<String, String>>>> itr = result.entrySet().iterator();
				while (itr.hasNext()) {
					Optional<Map<String, String>> valMap = itr.next().getValue();
					if(valMap.isPresent()) {
						Map<String, String> demandResp = valMap.get();
						NiveshMitraDemandResponse rp = new NiveshMitraDemandResponse();
						rp.setAsOnDateTimeStr(demandResp.get("asOnDate"));
						rp.setUlbcode(demandResp.get("ulbCode"));
						rp.setFinancialYear(demandResp.get("financialYear"));
						rp.setCommercialAmount(demandResp.get("commercialAmount"));
						rp.setCommercialCount(demandResp.get("commercialCount"));
						rp.setCommercialDemand(demandResp.get("commercialDemand"));
						rp.setMixedAmount(demandResp.get("mixedAmount"));
						rp.setMixedCount(demandResp.get("mixedCount"));
						rp.setMixedDemand(demandResp.get("mixedDemand"));
						rp.setResidentialAmount(demandResp.get("residentialAmount"));
						rp.setResidentialCount(demandResp.get("residentialCount"));
						rp.setResidentialDemand(demandResp.get("residentialDemand"));
						taxCollectionRespArray.add(rp);
					}
					
				}
				

		}
		
		return new ResponseEntity<>(taxCollectionRespArray, HttpStatus.OK);
	}

}
