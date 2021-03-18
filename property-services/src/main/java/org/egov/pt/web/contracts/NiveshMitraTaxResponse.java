package org.egov.pt.web.contracts;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.TimeZone;

import org.egov.common.contract.response.ResponseInfo;
import org.egov.pt.models.Property;
import org.egov.pt.util.CommonUtils;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

/**
 * Contains the ResponseHeader and the created/updated property
 */

@ToString
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NiveshMitraTaxResponse {


  @JsonProperty("asOnDateTime")
  private String asOnDateTimeStr;
  @JsonProperty("amountinRupees")
  private String amountinRupees;
  @JsonProperty("transactionCount")
  private String transactionCount;
  @JsonProperty("financialYear")
  private String financialYear;
  @JsonProperty("ulbcode")
  private String ulbcode;

  public static NiveshMitraTaxResponse taxRespObjFromHashMap(HashMap<String, String> houseTaxObj) {
    NiveshMitraTaxResponse taxResp = new NiveshMitraTaxResponse();
    Date date = new Date();
		TimeZone timezone = TimeZone.getTimeZone("Asia/Kolkata");
		SimpleDateFormat formatter = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss");
		formatter.setTimeZone(timezone);
		String asOnDate = formatter.format(date);
    
    taxResp.setAsOnDateTimeStr(asOnDate);
    taxResp.setFinancialYear(CommonUtils.currentFinancialYear());
    taxResp.setAmountinRupees(houseTaxObj.get("amount"));
    taxResp.setTransactionCount(houseTaxObj.get("count"));
    taxResp.setUlbcode(CommonUtils.getULBCodeForTenantId(houseTaxObj.get("tenantid")));
    return taxResp;
  }
}
