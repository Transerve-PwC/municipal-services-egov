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
public class NiveshMitraMutationResponse   {

	@JsonProperty("ResponseInfo")
  private ResponseInfo responseInfo;
  @JsonProperty("valid")
  private String valid;
  @JsonProperty("asOnDateTime")
  private String asOnDateTimeStr;
  @JsonProperty("propertyID")
  private String propertyID;
  @JsonProperty("zoneName")
  private String zoneName;
  @JsonProperty("wardName")
  private String wardName;
  @JsonProperty("mohallaName")
  private String mohallaName;
  @JsonProperty("ownerName")
  private String ownerName;
  @JsonProperty("fatherName")
  private String fatherName;
  @JsonProperty("address")
  private String address;
  @JsonProperty("mobile")
  private String mobile;
  @JsonProperty("areaOfLand")
  private String areaOfLand;
  @JsonProperty("fullhousetaxpaid")
  private String fullhousetaxpaid;
  @JsonProperty("uibcode")
  private String uibcode;
}
