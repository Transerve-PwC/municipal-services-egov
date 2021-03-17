package org.egov.pt.web.contracts;

import java.util.List;

import org.egov.common.contract.response.ResponseInfo;
import org.egov.pt.models.Property;

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
public class NiveshMitraPropertyCountDemandResponse {

	@JsonProperty("ResponseInfo")
  private ResponseInfo responseInfo;
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
  @JsonProperty("uibcode")
  private String uibcode;
}
