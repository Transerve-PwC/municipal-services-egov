package org.egov.pt.service;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.egov.pt.config.PropertyConfiguration;
import org.egov.tracer.model.CustomException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.BadSqlGrammarException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import com.google.common.base.Charsets;
import com.google.common.io.Files;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class PropertyIdGenerationService {
	
	@Autowired
	private PropertyConfiguration config;
	
	@Autowired
    private JdbcTemplate jdbcTemplate;
	
	@Autowired
    private Cachebaleservice cachebaleservice ;
	
	public  String generatePropertyId(String tenantId, org.egov.common.contract.request.RequestInfo requestinfo , String zone , String ward ,String usageType)
	{
		
		String stateCode = config.getStateCode();
		if(stateCode == null || stateCode.length()==0)
		{
			log.error(" State code missing in application.properties . Setting it to default 09 ");
			stateCode = "09" ;
		}
		
		HashMap<String, String> zoneNamesMap  = (HashMap<String, String>) cachebaleservice.getZoneNamesMap(tenantId, requestinfo);
		
		HashMap<String, String> wardNamesMap  = (HashMap<String, String>) cachebaleservice.getWardNameMap(tenantId, requestinfo);
		
		
		
		
		
		HashMap<String, String> ulbMap  	  =	(HashMap<String, String>) cachebaleservice.getUlbMap("up", requestinfo);
		
		try {
			
			String sequenceCode  = ulbMap.get(tenantId.replace("up.", ""));
			
			String sequenceName = "SEQ_EG_PT_ULBCODE_"+sequenceCode+"_PTID" ;
			
			List<String> sequenceLists = generateSequenceNumber(sequenceName, requestinfo, 1);
			
			
			String zoneCode = zoneNamesMap.get(zone);
			
			String wardCode = wardNamesMap.get(ward);
			
			String  sequence = sequenceLists.get(0);
			
			String  propertyUsageType = ""; 
			
			if(usageType.equalsIgnoreCase("Residential"))
			{
				propertyUsageType = "R" ;
			}else if(usageType.equalsIgnoreCase("Mixed"))
			{
				propertyUsageType = "M" ;
			}else
			{
				propertyUsageType = "N" ;
			}
			
			
			String  actualCode = stateCode+sequenceCode+zoneCode+wardCode+sequence+propertyUsageType;
			
			return actualCode ;
			
		} catch (Exception e) {
			throw new CustomException("PROPERTY_ID_GNERATION_ERROR","Error in CREATING PROPERTY ID");
		}
		
		
	}

	
	/**
     * Description : This method to generate sequence number
     *
     * @param sequenceName
     * @param requestInfo
     * @return seqNumber
     */
    private List<String> generateSequenceNumber(String sequenceName, org.egov.common.contract.request.RequestInfo requestInfo, Integer count) throws Exception {
        List<String> sequenceList = new LinkedList<>();
        List<String> sequenceLists = new LinkedList<>();
        // To generate a block of seq numbers

        String sequenceSql = "SELECT NEXTVAL ('" + sequenceName + "') FROM GENERATE_SERIES(1,?)";
        try {
            sequenceList = jdbcTemplate.queryForList(sequenceSql, new Object[]{count}, String.class);
        } catch (BadSqlGrammarException ex) {
                throw new CustomException("SEQ_NUMBER_ERROR","Error in retrieving seq number from DB");
        } catch (Exception ex) {
            log.error("Error retrieving seq number from DB",ex);
            throw new CustomException("SEQ_NUMBER_ERROR","Error retrieving seq number from existing seq in DB");
        }
        for (String seqId : sequenceList) {
            String seqNumber = String.format("%06d", Integer.parseInt(seqId)).toString();
            sequenceLists.add(seqNumber.toString());
        }
        return sequenceLists;
    }
	


	
}
