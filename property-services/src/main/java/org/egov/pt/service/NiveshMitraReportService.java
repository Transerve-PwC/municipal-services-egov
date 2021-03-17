package org.egov.pt.service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.Sets;

import org.egov.common.contract.request.RequestInfo;
import org.egov.pt.config.PropertyConfiguration;
import org.egov.pt.models.OwnerInfo;
import org.egov.pt.models.Property;
import org.egov.pt.models.PropertyCriteria;
import org.egov.pt.models.enums.CreationReason;
import org.egov.pt.models.enums.Status;
import org.egov.pt.models.user.UserDetailResponse;
import org.egov.pt.models.user.UserSearchRequest;
import org.egov.pt.models.workflow.State;
import org.egov.pt.producer.Producer;
import org.egov.pt.repository.PropertyRepository;
import org.egov.pt.util.PTConstants;
import org.egov.pt.util.PropertyUtil;
import org.egov.pt.validator.PropertyValidator;
import org.egov.pt.web.contracts.PropertyRequest;
import org.egov.tracer.model.CustomException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.MultiValueMap;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestTemplate;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class NiveshMitraReportService {

	@Autowired
    private RestTemplate restTemplate;
	
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


	/**
	 * Search property with given PropertyCriteria
	 *
	 * @param criteria PropertyCriteria containing fields on which search is based
	 * @return list of properties satisfying the containing fields in criteria
	 */

	
	public List<Property> searchProperty(PropertyCriteria criteria) {

		List<Property> properties;

		/*
		 * throw error if audit request is with no proeprty id or multiple propertyids
		 */
		if (CollectionUtils.isEmpty(criteria.getPropertyIds())
				|| (!CollectionUtils.isEmpty(criteria.getPropertyIds()) && criteria.getPropertyIds().size() > 1))) {

			throw new CustomException("EG_PT_PROPERTY_ERROR",
					"PropertyID not set");
		}
		properties = repository.getPropertiesWithOwnerInfo(criteria, null, true);

		List<Property> propertiesWithBoundaries = new ArrayList<Property>();
		properties.forEach(property -> {
			try {
				enrichmentService.enrichBoundary(property, null);
				propertiesWithBoundaries.add(property);
			} catch (CustomException e) {
				log.error("EnrichBoundary failed for the property id  {} ", property.getPropertyId());
				log.error(" Error message {} ,  Error code   {}", e.getMessage(), e.getCode());

				// e.printStackTrace();
			}
		});

		return propertiesWithBoundaries;
	}
	public RequestInfo getRequestInfo()
    {
    	Object token  = getTokenCitizen();
    	String authToken = "" ;

    	if(token != null)
    	{
    		HashMap   userMap =  (HashMap)(token);
    		authToken = userMap.get("access_token").toString();
    	}
    	RequestInfo requestInfo =
    			RequestInfo.builder().action("").apiId("Rainmaker")
    			.did("1").key("").msgId("20170310130900|en_IN").ver(".01").authToken(authToken).build();
    	return requestInfo;
    }
	public Object getTokenCitizen() {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
            headers.set("Authorization", "Basic ZWdvdi11c2VyLWNsaWVudDplZ292LXVzZXItc2VjcmV0");
            MultiValueMap<String, String> map = new LinkedMultiValueMap<>();
            map.add("username", config.getNiveshMitraUser());
            map.add("password", "123456");
            map.add("tenantId", config.getNiveshMitraTenant());
            map.add("grant_type", "password");
            map.add("scope", "read");
            map.add("isInternal", "true");
            map.add("userType", config.getNiveshMitraUserType());

            HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<MultiValueMap<String, String>>(map,
                    headers);
            return restTemplate.postForEntity(config.getUserHost() + config.getNiveshMitraUserContext() + config.getNiveshMitraUserEndpoint(), request, Map.class).getBody();

        } catch (Exception e) {
            throw e;
        }
    }
}