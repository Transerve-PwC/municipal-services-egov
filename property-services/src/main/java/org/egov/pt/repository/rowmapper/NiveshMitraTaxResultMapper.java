package org.egov.pt.repository.rowmapper;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.ResultSetExtractor;
import org.springframework.stereotype.Component;
@Component
public class NiveshMitraTaxResultMapper implements ResultSetExtractor<List<?>> {


	@Override
	public List<HashMap<String, String>> extractData(ResultSet rs) throws SQLException, DataAccessException {

		ArrayList<HashMap<String, String>> respList = new ArrayList<HashMap<String, String>>();

		while (rs.next()) {
			HashMap<String,String> map = new HashMap<String,String>();
			map.put("tenantid",rs.getString("tenantid"));
			map.put("amount",rs.getString("sum"));
			map.put("count",rs.getString("count"));

			respList.add(map);
		}
		return respList;

	}


	


	
}
