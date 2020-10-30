package org.egov.pt.models.excel;

import javax.persistence.*;
import java.math.BigDecimal;

@Entity
@Table(name = "eg_pt_property")
public class Property {
    @Id
    private String id;
    private String propertyid;
    private String tenantid;
    private String surveyid;
    private String accountid;
    private String oldpropertyid;
    private String status;
    private String acknowldgementnumber;
    private String propertytype;
    private String ownershipcategory;
    private String usagecategory;
    private String creationreason;
    private Long nooffloors;
    private BigDecimal landarea;
    private BigDecimal superbuiltuparea;
    private String linkedproperties;
    private String source;
    private String channel;
    private String createdby;
    private String lastmodifiedby;
    private Long createdtime;
    private Long lastmodifiedtime;
    private String constructionyear;

    public String getConstructionyear() {
        return constructionyear;
    }

    public void setConstructionyear(String constructionyear) {
        this.constructionyear = constructionyear;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getPropertyid() {
        return propertyid;
    }

    public void setPropertyid(String propertyid) {
        this.propertyid = propertyid;
    }

    public String getTenantid() {
        return tenantid;
    }

    public void setTenantid(String tenantid) {
        this.tenantid = tenantid;
    }

    public String getSurveyid() {
        return surveyid;
    }

    public void setSurveyid(String surveyid) {
        this.surveyid = surveyid;
    }

    public String getAccountid() {
        return accountid;
    }

    public void setAccountid(String accountid) {
        this.accountid = accountid;
    }

    public String getOldpropertyid() {
        return oldpropertyid;
    }

    public void setOldpropertyid(String oldpropertyid) {
        this.oldpropertyid = oldpropertyid;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getAcknowldgementnumber() {
        return acknowldgementnumber;
    }

    public void setAcknowldgementnumber(String acknowldgementnumber) {
        this.acknowldgementnumber = acknowldgementnumber;
    }

    public String getPropertytype() {
        return propertytype;
    }

    public void setPropertytype(String propertytype) {
        this.propertytype = propertytype;
    }

    public String getOwnershipcategory() {
        return ownershipcategory;
    }

    public void setOwnershipcategory(String ownershipcategory) {
        this.ownershipcategory = ownershipcategory;
    }

    public String getUsagecategory() {
        return usagecategory;
    }

    public void setUsagecategory(String usagecategory) {
        this.usagecategory = usagecategory;
    }

    public String getCreationreason() {
        return creationreason;
    }

    public void setCreationreason(String creationreason) {
        this.creationreason = creationreason;
    }

    public Long getNooffloors() {
        return nooffloors;
    }

    public void setNooffloors(Long nooffloors) {
        this.nooffloors = nooffloors;
    }

    public BigDecimal getLandarea() {
        return landarea;
    }

    public void setLandarea(BigDecimal landarea) {
        this.landarea = landarea;
    }

    public BigDecimal getSuperbuiltuparea() {
        return superbuiltuparea;
    }

    public void setSuperbuiltuparea(BigDecimal superbuiltuparea) {
        this.superbuiltuparea = superbuiltuparea;
    }

    public String getLinkedproperties() {
        return linkedproperties;
    }

    public void setLinkedproperties(String linkedproperties) {
        this.linkedproperties = linkedproperties;
    }

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }

    public String getChannel() {
        return channel;
    }

    public void setChannel(String channel) {
        this.channel = channel;
    }

    public String getCreatedby() {
        return createdby;
    }

    public void setCreatedby(String createdby) {
        this.createdby = createdby;
    }

    public String getLastmodifiedby() {
        return lastmodifiedby;
    }

    public void setLastmodifiedby(String lastmodifiedby) {
        this.lastmodifiedby = lastmodifiedby;
    }

    public Long getCreatedtime() {
        return createdtime;
    }

    public void setCreatedtime(Long createdtime) {
        this.createdtime = createdtime;
    }

    public Long getLastmodifiedtime() {
        return lastmodifiedtime;
    }

    public void setLastmodifiedtime(Long lastmodifiedtime) {
        this.lastmodifiedtime = lastmodifiedtime;
    }

}
