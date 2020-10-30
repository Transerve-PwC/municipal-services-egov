package org.egov.pt.models.excel;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import java.math.BigDecimal;

@Entity
@Table(name = "eg_pt_unit")
public class Unit {
    @Id
    private String id;
    private String tenantid;
    private String propertyid;
    private Long floorno;
    private String unittype;
    private String usagecategory;
    private String occupancytype;
    private Long occupancydate;
    private BigDecimal carpetarea;
    private BigDecimal builtuparea;
    private BigDecimal plintharea;
    private BigDecimal superbuiltuparea;
    private BigDecimal arv;
    private String constructiontype;
    private Long constructiondate;

    private Boolean active;
    private String createdby;
    private Long createdtime;
    private String lastmodifiedby;
    private Long lastmodifiedtime;

    public String getId() {
        return id;
    }

    public String getTenantid() {
        return tenantid;
    }

    public String getPropertyid() {
        return propertyid;
    }

    public Long getFloorno() {
        return floorno;
    }

    public String getUnittype() {
        return unittype;
    }

    public String getUsagecategory() {
        return usagecategory;
    }

    public String getOccupancytype() {
        return occupancytype;
    }

    public Long getOccupancydate() {
        return occupancydate;
    }

    public BigDecimal getCarpetarea() {
        return carpetarea;
    }

    public BigDecimal getBuiltuparea() {
        return builtuparea;
    }

    public BigDecimal getPlintharea() {
        return plintharea;
    }

    public BigDecimal getSuperbuiltuparea() {
        return superbuiltuparea;
    }

    public BigDecimal getArv() {
        return arv;
    }

    public String getConstructiontype() {
        return constructiontype;
    }

    public Long getConstructiondate() {
        return constructiondate;
    }


    public Boolean getActive() {
        return active;
    }

    public String getCreatedby() {
        return createdby;
    }

    public Long getCreatedtime() {
        return createdtime;
    }

    public String getLastmodifiedby() {
        return lastmodifiedby;
    }

    public Long getLastmodifiedtime() {
        return lastmodifiedtime;
    }

    public void setId(String id) {
        this.id = id;
    }

    public void setTenantid(String tenantid) {
        this.tenantid = tenantid;
    }

    public void setPropertyid(String propertyid) {
        this.propertyid = propertyid;
    }

    public void setFloorno(Long floorno) {
        this.floorno = floorno;
    }

    public void setUnittype(String unittype) {
        this.unittype = unittype;
    }

    public void setUsagecategory(String usagecategory) {
        this.usagecategory = usagecategory;
    }

    public void setOccupancytype(String occupancytype) {
        this.occupancytype = occupancytype;
    }

    public void setOccupancydate(Long occupancydate) {
        this.occupancydate = occupancydate;
    }

    public void setCarpetarea(BigDecimal carpetarea) {
        this.carpetarea = carpetarea;
    }

    public void setBuiltuparea(BigDecimal builtuparea) {
        this.builtuparea = builtuparea;
    }

    public void setPlintharea(BigDecimal plintharea) {
        this.plintharea = plintharea;
    }

    public void setSuperbuiltuparea(BigDecimal superbuiltuparea) {
        this.superbuiltuparea = superbuiltuparea;
    }

    public void setArv(BigDecimal arv) {
        this.arv = arv;
    }

    public void setConstructiontype(String constructiontype) {
        this.constructiontype = constructiontype;
    }

    public void setConstructiondate(Long constructiondate) {
        this.constructiondate = constructiondate;
    }


    public void setActive(Boolean active) {
        this.active = active;
    }

    public void setCreatedby(String createdby) {
        this.createdby = createdby;
    }

    public void setCreatedtime(Long createdtime) {
        this.createdtime = createdtime;
    }

    public void setLastmodifiedby(String lastmodifiedby) {
        this.lastmodifiedby = lastmodifiedby;
    }

    public void setLastmodifiedtime(Long lastmodifiedtime) {
        this.lastmodifiedtime = lastmodifiedtime;
    }
}


