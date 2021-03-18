package org.egov.pt.web.contracts;

import java.util.HashMap;
import java.util.List;

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
public class NiveshMitraDemandResponse {

  @JsonProperty("asOnDateTime")
  private String asOnDateTimeStr;
  @JsonProperty("commercialAmount")
  private String commercialAmount;
  @JsonProperty("commercialCount")
  private String commercialCount;
  @JsonProperty("commercialDemand")
  private String commercialDemand;
  @JsonProperty("financialYear")
  private String financialYear;
  @JsonProperty("mixedAmount")
  private String mixedAmount;
  @JsonProperty("mixedCount")
  private String mixedCount;
  @JsonProperty("mixedDemand")
  private String mixedDemand;
  @JsonProperty("residentialAmount")
  private String residentialAmount;
  @JsonProperty("residentialCount")
  private String residentialCount;
  @JsonProperty("residentialDemand")
  private String residentialDemand;
  @JsonProperty("ulbcode")
  private String ulbcode;

  public static NiveshMitraDemandResponse objFromDemandRepositoryHashMap (HashMap<String, String> map1) {
    NiveshMitraDemandResponse rp = new NiveshMitraDemandResponse();
    Double amt = CommonUtils.stringToDouble(String.valueOf(map1.get("totalcollection")));
		Double cnt = CommonUtils.stringToDouble(String.valueOf(map1.get("count")));
		Double dmdAmt = CommonUtils.stringToDouble(String.valueOf(map1.get("totaldemand")));
    switch (map1.get("usagecategory").toString()) {
      case "RESIDENTIAL": 
        rp.setResidentialAmount(String.valueOf(amt));
        rp.setResidentialCount(String.valueOf(cnt));
        rp.setResidentialDemand(String.valueOf(dmdAmt));
        break;
      case "MIXED":
        rp.setMixedAmount(String.valueOf(amt));
        rp.setMixedCount(String.valueOf(cnt));
        rp.setMixedDemand(String.valueOf(dmdAmt));
        break;
      default:
        rp.setCommercialAmount(String.valueOf(amt));
        rp.setCommercialCount(String.valueOf(cnt));
        rp.setCommercialDemand(String.valueOf(dmdAmt));
    }
    rp.setUlbcode(CommonUtils.getULBCodeForTenantId(map1.get("tenantid")));
    rp.setFinancialYear(CommonUtils.currentFinancialYear());
    return rp;
  }
  public static NiveshMitraDemandResponse mergeDemandResponses(NiveshMitraDemandResponse rp1, NiveshMitraDemandResponse rp2) {
    rp1.setCommercialAmount(String.valueOf(CommonUtils.stringToDouble(rp1.commercialAmount) + CommonUtils.stringToDouble(rp2.commercialAmount)));
    rp1.setCommercialCount(String.valueOf(CommonUtils.stringToDouble(rp1.commercialCount) + CommonUtils.stringToDouble(rp2.commercialCount)));
    rp1.setCommercialDemand(String.valueOf(CommonUtils.stringToDouble(rp1.commercialDemand) + CommonUtils.stringToDouble(rp2.commercialDemand)));

    rp1.setMixedAmount(String.valueOf(CommonUtils.stringToDouble(rp1.mixedAmount) + CommonUtils.stringToDouble(rp2.mixedAmount)));
    rp1.setMixedCount(String.valueOf(CommonUtils.stringToDouble(rp1.mixedCount) + CommonUtils.stringToDouble(rp2.mixedCount)));
    rp1.setMixedDemand(String.valueOf(CommonUtils.stringToDouble(rp1.mixedDemand) + CommonUtils.stringToDouble(rp2.mixedDemand)));
    
    rp1.setResidentialAmount(String.valueOf(CommonUtils.stringToDouble(rp1.residentialAmount) + CommonUtils.stringToDouble(rp2.residentialAmount)));
    rp1.setResidentialCount(String.valueOf(CommonUtils.stringToDouble(rp1.residentialCount) + CommonUtils.stringToDouble(rp2.residentialCount)));
    rp1.setResidentialDemand(String.valueOf(CommonUtils.stringToDouble(rp1.residentialDemand) + CommonUtils.stringToDouble(rp2.residentialDemand)));
    return rp1;
  }

}
