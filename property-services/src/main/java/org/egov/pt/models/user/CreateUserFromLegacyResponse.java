package org.egov.pt.models.user;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;
import org.egov.common.contract.request.RequestInfo;

@AllArgsConstructor
@Getter
@NoArgsConstructor
@Builder
@Setter
public class CreateUserFromLegacyResponse {

    @JsonProperty("ResponseInfo")
    private RequestInfo requestInfo;

    @JsonProperty("UserRequest")
    private UserLegacy user;

    
}


