package org.egov.pt.models.user;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;
import org.egov.common.contract.request.RequestInfo;

import java.util.Map;

@AllArgsConstructor
@Getter
@NoArgsConstructor
@Builder
@Setter
public class UserLegacy {

    private Long id;
    private String uuid;
    private String userName;
    private String name;
    private String mobileNumber;
    private String permanentCity;
    private Boolean active;
    private String locale;
    private String type;
    private String password;
    private String otpReference;
    private String tenantId;
    private String fatherOrHusbandName;
    
}


