package org.egov.pt.repository.rowmapper;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.ResultSetExtractor;
import org.springframework.stereotype.Component;
@Component
public class NiveshMitraDemandCountMapper implements ResultSetExtractor<List<HashMap<String, String>>> {


	@Override
	public List<HashMap<String, String>> extractData(ResultSet rs) throws SQLException, DataAccessException {

		ResultSetMetaData md = rs.getMetaData();
		int columns = md.getColumnCount();
		ArrayList list = new ArrayList(50);
		while (rs.next()){
		   HashMap row = new HashMap(columns);
		   for(int i=1; i<=columns; ++i){           
			row.put(md.getColumnName(i),rs.getObject(i));
		   }
			list.add(row);
		}
	  
	   return list;

	}


	


	
}
