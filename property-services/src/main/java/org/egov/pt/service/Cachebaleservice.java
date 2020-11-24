package org.egov.pt.service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
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

@Service
public class Cachebaleservice {
	
	 @Autowired
	 private ServiceRequestRepository restRepo;
	 
	@Autowired
    private UserService userService;
	
	@Autowired
    private PropertyConfiguration config;
	
	 private static final String TENANTS_MORADABAD = "up.moradabad";
	 private static final String TENANTS_BAREILLY = "up.bareilly";

	
	 @Cacheable(value="localities" ,key ="#tenantId")
	    public Map<String, String> getLocalityMap(String tenantId, org.egov.common.contract.request.RequestInfo requestinfo) {
	     
	    	if (TENANTS_MORADABAD.equalsIgnoreCase(tenantId)) {
	            final Map<String, String> localityMap = new HashMap<String, String>();
	            localityMap.put("Adarsh Nagar_08", "MOR275");
	            localityMap.put("Adarsh_Nagar(08)", "MOR275");
	            localityMap.put("Ashok_Nagar", "MOR222");
	            localityMap.put("Gopalpur", "MOR130");
	            localityMap.put("Gopalpur_03", "MOR130");
	            localityMap.put("Gyani_Wali_Basti", "MOR273");
	            localityMap.put("Kumar_Kunj", "MOR029");
	            localityMap.put("Majholi", "MOR132");
	            return localityMap;
	        }
	        if (TENANTS_BAREILLY.equalsIgnoreCase(tenantId)) {
	            StringBuilder uri = new StringBuilder(config.getMdmsHost()).append(config.getMdmsEndpoint());
	            MdmsCriteriaReq criteriaReq = prepareMdMsRequest(tenantId, "egov-location",
	                    Arrays.asList(new String[] { "TenantBoundary" }), "$..[?(@.label=='Locality')]", requestinfo);
	            Optional<Object> response = restRepo.fetchResult(uri, criteriaReq);
	            List<Map<String, String>> boundaries = JsonPath.read(response.get(),"$.MdmsRes.egov-location.TenantBoundary");
	            
	            Map<String, String> localityMap = boundaries.stream().collect(Collectors.toMap(b -> b.get("name") , b -> b.get("code"),(oldval,newval) -> newval));
	            
	            return ((HashMap<String, String>) localityMap).entrySet().parallelStream().collect(Collectors.toMap(entry -> entry.getKey().toLowerCase(), Map.Entry::getValue));
	           
	            
	            // List<Map<String, String>> boundaries = JsonPath.read(response.get(),
	            // "$..[?(@.name=='" + legacyRow.getLocality() + "')]");

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
