package org.egov.pt.service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.egov.common.contract.request.RequestInfo;
import org.egov.mdms.model.MasterDetail;
import org.egov.mdms.model.MdmsCriteria;
import org.egov.mdms.model.MdmsCriteriaReq;
import org.egov.mdms.model.ModuleDetail;
import org.egov.pt.config.PropertyConfiguration;
import org.egov.pt.models.user.User;
import org.egov.pt.repository.ServiceRequestRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import com.jayway.jsonpath.JsonPath;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class Cachebaleservice {
	
	 @Autowired
	 private ServiceRequestRepository restRepo;
	 
	@Autowired
    private UserService userService;
	
	@Autowired
    private PropertyConfiguration config;
	
	 private static final String TENANTS_MORADABAD = "up.moradabad";
	 private static final String TENANTS_BAREILLY = "up.bareilly";

	 @Cacheable(value="finYearRange", key="#financeRange", sync = true)
	  public Map<String, String> getFinancialYearData(String financeRange, org.egov.common.contract.request.RequestInfo requestinfo){
			StringBuilder uri = new StringBuilder(config.getMdmsHost()).append(config.getMdmsEndpoint());
	        MdmsCriteriaReq criteriaReq = prepareMdMsRequest("up", "egf-master", Arrays.asList(new String[] { "FinancialYear" }), "$[?(@.module=='PT')]", requestinfo);
	            Optional<Object> response = restRepo.fetchResult(uri, criteriaReq);
	            List<Map<String, String>> financialYears = JsonPath.read(response.get(),"$.MdmsRes.egf-master.FinancialYear");
	            
	           List<Map<String, String>> data = financialYears.stream().filter(obj -> obj.get("code").toString().equalsIgnoreCase(financeRange)).collect(Collectors.toList());
				if (data != null && data.size() > 0) {
					return data.get(0);
				}
				return null;
	  }
	
	 @Cacheable(value="localities" ,key ="#tenantId" ,sync = true)
	    public Map<String, String> getLocalityMap(String tenantId, org.egov.common.contract.request.RequestInfo requestinfo) {
	    	if (TENANTS_MORADABAD.equalsIgnoreCase(tenantId)) {
	            final Map<String, String> localityMap = new HashMap<String, String>();
	            localityMap.put("Adarsh Nagar_08", "MOR00169");
	            localityMap.put("Adarsh_Nagar(08)", "MOR00169");
	            localityMap.put("Ashok_Nagar", "MOR00254");
	            localityMap.put("Gopalpur", "MOR00132");
	            localityMap.put("Gopalpur_03", "MOR00132");
	            localityMap.put("Gyani_Wali_Basti", "MOR0061");
	            localityMap.put("Kumar_Kunj", "MOR00201");
	            localityMap.put("Majholi", "MOR00134");
	            
	            return ((HashMap<String, String>) localityMap).entrySet().parallelStream().collect(Collectors.toMap(entry -> entry.getKey().toLowerCase(), Map.Entry::getValue));
	        }
	        if (TENANTS_BAREILLY.equalsIgnoreCase(tenantId)) {
	            StringBuilder uri = new StringBuilder(config.getMdmsHost()).append(config.getMdmsEndpoint());
	            MdmsCriteriaReq criteriaReq = prepareMdMsRequest(tenantId, "egov-location",
	                    Arrays.asList(new String[] { "TenantBoundary" }), "$..[?(@.label=='Locality')]", requestinfo);
	            Optional<Object> response = restRepo.fetchResult(uri, criteriaReq);
	            List<Map<String, String>> boundaries = JsonPath.read(response.get(),"$.MdmsRes.egov-location.TenantBoundary");
	            
	            Map<String, String> localityMap = boundaries.stream().collect(Collectors.toMap(b -> b.get("name") , b -> b.get("code"),(oldval,newval) -> newval));
	            
	            return ((HashMap<String, String>) localityMap).entrySet().parallelStream().collect(Collectors.toMap(entry -> entry.getKey().toLowerCase(), Map.Entry::getValue));
	           
	         

	        }
	        return null;
	    }
	 
	 @Cacheable(value="ward" ,key ="#tenantId" ,sync = true)
	    public Map<String, String> getWardMap(String tenantId, org.egov.common.contract.request.RequestInfo requestinfo) {
	     
	    	if (TENANTS_MORADABAD.equalsIgnoreCase(tenantId) || TENANTS_BAREILLY.equalsIgnoreCase(tenantId)) {
	            try {
					StringBuilder uri = new StringBuilder(config.getMdmsHost()).append(config.getMdmsEndpoint());
					MdmsCriteriaReq criteriaReq = prepareMdMsRequest(tenantId, "egov-location",
					        Arrays.asList(new String[] { "TenantBoundary" }), "$..[?(@.label=='Ward')]", requestinfo);
					Optional<Object> response = restRepo.fetchResult(uri, criteriaReq);
					List<Map<String, ArrayList<Map<Object, Object>>>> boundaries = JsonPath.read(response.get(),"$.MdmsRes.egov-location.TenantBoundary");
					
					HashMap<String, String> wardsMap  = new HashMap<String, String>();
					
					for (Map<String, ArrayList<Map<Object, Object>>> zoneMap : boundaries) {
						
						ArrayList<Map<Object, Object>> childrenList = (ArrayList<Map<Object, Object>>)zoneMap.get("children");
						
						System.out.println(zoneMap.get("code"));
						
						for (Map<Object, Object> localityMap : childrenList) {
							wardsMap.put(localityMap.get("code").toString(), String.valueOf(zoneMap.get("code")));
						}
						
					}
					
					log.info(" wardsMap size {}  ",wardsMap.size());
					
					
					return  wardsMap;
				} catch (Exception e) {
					e.printStackTrace();
				}
	           

	        }
	        
	        return null;
	    }
	 
	 @Cacheable(value="wardname" ,key ="#localityCode" ,sync = true)
	 public String getWardNameFromLocalityCode(String localityCode, String tenantId, org.egov.common.contract.request.RequestInfo requestinfo)
	 {
		 

         try {
				StringBuilder uri = new StringBuilder(config.getMdmsHost()).append(config.getMdmsEndpoint());
				MdmsCriteriaReq criteriaReq = prepareMdMsRequest(tenantId, "egov-location",
				        Arrays.asList(new String[] { "TenantBoundary" }), "$..[?(@.label=='Ward')]", requestinfo);
				Optional<Object> response = restRepo.fetchResult(uri, criteriaReq);
				List<Map<String, ArrayList<Map<Object, Object>>>> boundaries = JsonPath.read(response.get(),"$.MdmsRes.egov-location.TenantBoundary");
				
				
				for (Map<String, ArrayList<Map<Object, Object>>> wardMap : boundaries) {
					
					ArrayList<Map<Object, Object>> childrenList = (ArrayList<Map<Object, Object>>)wardMap.get("children");
					
					System.out.println(wardMap.get("code"));
					
					for (Map<Object, Object> localityMap : childrenList) {
						
						if(String.valueOf(localityMap.get("code")).equalsIgnoreCase(localityCode))
						{
							System.out.println(" ward name  "+String.valueOf(wardMap.get("wardname")));
							return String.valueOf(wardMap.get("wardname"));
						}
						
					}
					
				}
				
			} catch (Exception e) {
				e.printStackTrace();
			}

		return "";
		 
	 }
	 
	 @Cacheable(value="zonename" ,key ="#localitycode" ,sync = true)
	 public String getZoneNameFromLocalityCode(String localityCode, String tenantId, org.egov.common.contract.request.RequestInfo requestinfo)
	 {
		 try {
				StringBuilder uri = new StringBuilder(config.getMdmsHost()).append(config.getMdmsEndpoint());
				MdmsCriteriaReq criteriaReq = prepareMdMsRequest(tenantId, "egov-location",
				        Arrays.asList(new String[] { "TenantBoundary" }), "$..[?(@.label=='Zone')]", requestinfo);
				Optional<Object> response = restRepo.fetchResult(uri, criteriaReq);
				List<Map<String, List<Map<String, ArrayList<Map<Object, Object>>>>>> boundaries = JsonPath.read(response.get(),"$.MdmsRes.egov-location.TenantBoundary");
				
				
				for (Map<String, List<Map<String, ArrayList<Map<Object, Object>>>>> zoneMap : boundaries) {
					
					System.out.println(zoneMap.get("code"));
					
					for (Map<String, ArrayList<Map<Object, Object>>> wards : zoneMap.get("children")) {
						
						ArrayList<Map<Object, Object>> childrenList = (ArrayList<Map<Object, Object>>)wards.get("children");
						
						System.out.println(zoneMap.get("code"));
						
						for (Map<Object, Object> localityMap : childrenList) {
							
							if(String.valueOf(localityMap.get("code")).equalsIgnoreCase(localityCode))
							{
								System.out.println(" zone name  "+String.valueOf(zoneMap.get("localname")));
								return String.valueOf(zoneMap.get("localname"));
							}
							
						}
					}
				}
				
				
			} catch (Exception e) {
				e.printStackTrace();
			}
		 
		 
		 return "";
	 }
	 
	 
	 
	 @Cacheable(value="zone" ,key ="#tenantId" ,sync = true)
	    public Map<String, String> getZoneMap(String tenantId, org.egov.common.contract.request.RequestInfo requestinfo) {
	     
	    	if (TENANTS_MORADABAD.equalsIgnoreCase(tenantId) || TENANTS_BAREILLY.equalsIgnoreCase(tenantId)) {
	            try {
					StringBuilder uri = new StringBuilder(config.getMdmsHost()).append(config.getMdmsEndpoint());
					MdmsCriteriaReq criteriaReq = prepareMdMsRequest(tenantId, "egov-location",
					        Arrays.asList(new String[] { "TenantBoundary" }), "$..[?(@.label=='Zone')]", requestinfo);
					Optional<Object> response = restRepo.fetchResult(uri, criteriaReq);
					List<Map<String, List<Map<String, ArrayList<Map<Object, Object>>>>>> boundaries = JsonPath.read(response.get(),"$.MdmsRes.egov-location.TenantBoundary");
					
					HashMap<String, String> zonesMap  = new HashMap<String, String>();
					
					for (Map<String, List<Map<String, ArrayList<Map<Object, Object>>>>> zoneMap : boundaries) {
						
						System.out.println(zoneMap.get("code"));
						
						for (Map<String, ArrayList<Map<Object, Object>>> wards : zoneMap.get("children")) {
							
							ArrayList<Map<Object, Object>> childrenList = (ArrayList<Map<Object, Object>>)wards.get("children");
							
							System.out.println(zoneMap.get("code"));
							
							for (Map<Object, Object> localityMap : childrenList) {
								zonesMap.put(localityMap.get("code").toString(), String.valueOf(zoneMap.get("code")));
							}
						}
					}
					
					log.info(" zones map size {}  ",zonesMap.size());
					
					
					return  zonesMap;
				} catch (Exception e) {
					e.printStackTrace();
				}
	           
	        }
	        return null;
	    }
	 
	 
	 @Cacheable(value="duplicateLocalities" ,key ="#tenantId" ,sync = true)
	    public HashMap<String,HashMap<String,HashMap<String, String>>> getDuplicateLocalitiesMap(String tenantId, org.egov.common.contract.request.RequestInfo requestinfo) {
	     
	    	if (TENANTS_MORADABAD.equalsIgnoreCase(tenantId) || TENANTS_BAREILLY.equalsIgnoreCase(tenantId)) {
	            try {
					StringBuilder uri = new StringBuilder(config.getMdmsHost()).append(config.getMdmsEndpoint());
					MdmsCriteriaReq criteriaReq = prepareMdMsRequest(tenantId, "egov-location",
					        Arrays.asList(new String[] { "TenantBoundary" }), "$..[?(@.label=='Zone')]", requestinfo);
					Optional<Object> response = restRepo.fetchResult(uri, criteriaReq);
					List<Map<String, List<Map<String, ArrayList<Map<Object, Object>>>>>> boundaries = JsonPath.read(response.get(),"$.MdmsRes.egov-location.TenantBoundary");
					
					HashMap<String,HashMap<String,HashMap<String, String>>> duplicateLocalityMap  = new HashMap<String,HashMap<String,HashMap<String, String>>>();
					
					HashSet<String> dupliacteLocalities = new HashSet();
					HashSet<String> allLocalities = new HashSet();
					
					
					for (Map<String, List<Map<String, ArrayList<Map<Object, Object>>>>> zoneMap : boundaries) {
						for (Map<String, ArrayList<Map<Object, Object>>> wards : zoneMap.get("children")) {
							ArrayList<Map<Object, Object>> childrenList = (ArrayList<Map<Object, Object>>)wards.get("children");
							for (Map<Object, Object> localityMap : childrenList) {
								if(!allLocalities.add(localityMap.get("name").toString()))
								{
									dupliacteLocalities.add(localityMap.get("name").toString());
									System.out.println(" Already contained element "+localityMap.get("name"));
								}
							}
						}
					}
					
					
					for (Map<String, List<Map<String, ArrayList<Map<Object, Object>>>>> zoneMap : boundaries) {
						for (Map<String, ArrayList<Map<Object, Object>>> wards : zoneMap.get("children")) {
							ArrayList<Map<Object, Object>> childrenList = (ArrayList<Map<Object, Object>>)wards.get("children");
							for (Map<Object, Object> localityMap : childrenList) {
								if(dupliacteLocalities.contains(localityMap.get("name").toString()))
								{
									HashMap<String, String> wardsMap  = new HashMap<String, String>();
									HashMap<String, HashMap<String, String>> zonesMap  = new HashMap<String, HashMap<String, String>>();
									
									wardsMap.put(String.valueOf(wards.get("name")), String.valueOf(localityMap.get("code")));
									
									zonesMap.put(String.valueOf(zoneMap.get("name")), wardsMap);
									
									if(duplicateLocalityMap.containsKey(localityMap.get("name").toString().toLowerCase()))
									{
										HashMap<String, HashMap<String, String>> existingZonesMap = duplicateLocalityMap.get(localityMap.get("name").toString().toLowerCase());
										
										if(existingZonesMap.containsKey(zoneMap.get("name")))
										{
											HashMap<String, String>  existingWardMap =  existingZonesMap.get(zoneMap.get("name"));
											
											if(existingWardMap.containsKey(wards.get("name")))
											{
												existingWardMap.put(String.valueOf(wards.get("name")), String.valueOf(localityMap.get("code")));
											}else
											{
												existingWardMap.put(String.valueOf(wards.get("name")), String.valueOf(localityMap.get("code")));
											}
											
										}else
										{
											existingZonesMap.put(String.valueOf(zoneMap.get("name")), wardsMap);
										}
										
									}else
									duplicateLocalityMap.put(localityMap.get("name").toString().toLowerCase(), zonesMap);
								}
							}
						}
					}
					
					
					
					log.info(" duplicateLocalityMap map size {}  ",duplicateLocalityMap.size());
					
					
					return  duplicateLocalityMap;
				} catch (Exception e) {
					e.printStackTrace();
				}
	           
	        }
	        return null;
	    }
	 
	 
	 @Cacheable(value="token" ,key ="#tenantId")
		public String getUserToken(String tenantId, User user) {
			Map<String, String> tokenResponse = (Map<String, String>)
			  userService.getToken(user.getUserName(), "123456", tenantId, user.getType());
			  String token = (String) tokenResponse.get("access_token");
			return token;
		}

	 private MdmsCriteriaReq prepareMdMsRequest(String tenantId, String moduleName, List<String> names, String filter,
	            RequestInfo requestInfo) {

	        List<MasterDetail> masterDetails = new ArrayList<>();

	        names.forEach(name -> {
	            masterDetails.add(MasterDetail.builder().name(name).filter(filter).build());
	        });

	        ModuleDetail moduleDetail = ModuleDetail.builder().moduleName(moduleName).masterDetails(masterDetails).build();
	        List<ModuleDetail> moduleDetails = new ArrayList<>();
	        moduleDetails.add(moduleDetail);
	        MdmsCriteria mdmsCriteria = MdmsCriteria.builder().tenantId(tenantId).moduleDetails(moduleDetails).build();
	        return MdmsCriteriaReq.builder().requestInfo(requestInfo).mdmsCriteria(mdmsCriteria).build();
	    }

}
