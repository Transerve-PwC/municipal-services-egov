package org.egov.pt.repository;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.egov.pt.repository.rowmapper.NiveshMitraTaxResultMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class NiveshMitraReportsRepository {

	@Autowired
	private JdbcTemplate jdbcTemplate;

	@Autowired
	private NiveshMitraTaxResultMapper rowMapper;
	
    
	public List<HashMap<String,String>> getHouseTaxData() {
		String query = "select edv.tenantid ,sum(edv.collectionamount), count(edv.id) from egbs_demanddetail_v1 edv where edv.taxheadcode = 'PT_HOUSE_TAX' and edv.collectionamount > 0 GROUP BY edv.tenantid";
		return jdbcTemplate.query(query, rowMapper);
	}

	public List<HashMap<String,String>> getWaterTaxData() {
		String query = "select edv.tenantid ,sum(edv.collectionamount), count(edv.id) from egbs_demanddetail_v1 edv where edv.taxheadcode = 'PT_WATER_TAX' and edv.collectionamount > 0 GROUP BY edv.tenantid";
		return jdbcTemplate.query(query, rowMapper);
	}

}
