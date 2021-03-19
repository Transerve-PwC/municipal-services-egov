package org.egov.pt.repository;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.egov.pt.repository.rowmapper.NiveshMitraDemandCountMapper;
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
	
	@Autowired
	private NiveshMitraDemandCountMapper demandResultMapper;
    
	public List<HashMap<String,String>> getHouseTaxData() {
		String query = "select edv.tenantid ,sum(edv.collectionamount), count(edv.id) from egbs_demanddetail_v1 edv inner join egbs_demand_v1 edd on edd.id=edv.demandid  where edv.taxheadcode = 'PT_HOUSE_TAX' and edv.collectionamount > 0 and edd.status='ACTIVE' and edd.taxperiodfrom = '1554076799000' and edd.taxperiodto = '1585679399000' GROUP BY edv.tenantid";
		return jdbcTemplate.query(query, rowMapper);
	}

	public List<HashMap<String,String>> getWaterTaxData() {
		String query = "select edv.tenantid ,sum(edv.collectionamount), count(edv.id) from egbs_demanddetail_v1 edv inner join egbs_demand_v1 edd on edd.id=edv.demandid  where edv.taxheadcode = 'PT_WATER_TAX' and edv.collectionamount > 0 and edd.status='ACTIVE' and edd.taxperiodfrom = '1554076799000' and edd.taxperiodto = '1585679399000' GROUP BY edv.tenantid";
		return jdbcTemplate.query(query, rowMapper);
	}

	public List<HashMap<String,String>> getDemandData(String startEpoch, String endEpoch) {
		List<Object> preparedStmtList = new ArrayList<>();
		String query = "select sum(edv.totalamount) as totalDemand, sum(eb.amountpaid) as totalCollection, epp.tenantid, epp.usagecategory, count(ebv3.id)  from eg_pt_property epp inner join egbs_billdetail_v1 edv on epp.propertyid = edv.consumercode inner join egbs_bill_v1 ebv3 on edv.billid = ebv3.id left join egcl_billdetial eb on eb.billid = edv.billid where ebv3.status != 'EXPIRED' and ebv3.status !='CANCELLED' and edv.fromperiod = ? and edv.toperiod = ? group by epp.tenantid, epp.usagecategory";
		preparedStmtList.add(Long.valueOf(startEpoch));
		preparedStmtList.add(Long.valueOf(endEpoch));
		return  jdbcTemplate.query(query, preparedStmtList.toArray(), demandResultMapper);
	}

}
