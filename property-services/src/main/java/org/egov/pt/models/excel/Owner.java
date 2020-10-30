package org.egov.pt.models.excel;

import javax.persistence.*;
import java.math.BigDecimal;
import java.sql.Timestamp;

@Entity
@Table(name = "eg_pt_owner")
public class Owner {
    @Id
    private String ownerinfouuid;
    private String tenantid;
    private String propertyid;
    private String userid;
    private String status;
    private Boolean isprimaryowner;
    private String ownertype;
    private String ownershippercentage;
    private String institutionid;
    private String relationship;
    private String createdby;
    private Long createdtime;
    private String lastmodifiedby;
    private Long lastmodifiedtime;

    public String getOwnerinfouuid() {
        return ownerinfouuid;
    }

    public void setOwnerinfouuid(String ownerinfouuid) {
        this.ownerinfouuid = ownerinfouuid;
    }

    public String getTenantid() {
        return tenantid;
    }

    public void setTenantid(String tenantid) {
        this.tenantid = tenantid;
    }

    public String getPropertyid() {
        return propertyid;
    }

    public void setPropertyid(String propertyid) {
        this.propertyid = propertyid;
    }

    public String getUserid() {
        return userid;
    }

    public void setUserid(String userid) {
        this.userid = userid;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Boolean getIsprimaryowner() {
        return isprimaryowner;
    }

    public void setIsprimaryowner(Boolean isprimaryowner) {
        this.isprimaryowner = isprimaryowner;
    }

    public String getOwnertype() {
        return ownertype;
    }

    public void setOwnertype(String ownertype) {
        this.ownertype = ownertype;
    }

    public String getOwnershippercentage() {
        return ownershippercentage;
    }

    public void setOwnershippercentage(String ownershippercentage) {
        this.ownershippercentage = ownershippercentage;
    }

    public String getInstitutionid() {
        return institutionid;
    }

    public void setInstitutionid(String institutionid) {
        this.institutionid = institutionid;
    }

    public String getRelationship() {
        return relationship;
    }

    public void setRelationship(String relationship) {
        this.relationship = relationship;
    }

    public String getCreatedby() {
        return createdby;
    }

    public void setCreatedby(String createdby) {
        this.createdby = createdby;
    }

    public Long getCreatedtime() {
        return createdtime;
    }

    public void setCreatedtime(Long createdtime) {
        this.createdtime = createdtime;
    }

    public String getLastmodifiedby() {
        return lastmodifiedby;
    }

    public void setLastmodifiedby(String lastmodifiedby) {
        this.lastmodifiedby = lastmodifiedby;
    }

    public Long getLastmodifiedtime() {
        return lastmodifiedtime;
    }

    public void setLastmodifiedtime(Long lastmodifiedtime) {
        this.lastmodifiedtime = lastmodifiedtime;
    }
}


