package org.egov.pt.models.excel;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import java.math.BigDecimal;

@Entity
@Table(name = "eg_pt_address")
public class Address {

    private String tenantid;
    @Id
    private String id;
    private String propertyid;
    private String doorno;
    private String plotno;
    private String buildingname;
    private String street;
    private String landmark;
    private String city;
    private String pincode;
    private String locality;
    private String district;
    private String region;
    private String state;
    private String country;
    private BigDecimal latitude;
    private BigDecimal longitude;
    private String createdby;
    private Long createdtime;
    private String lastmodifiedby;
    private Long lastmodifiedtime;
    private String taxward;
    private String wardno;
    private String zone;
    private String wardname;

    public String getTaxward() {
        return taxward;
    }

    public void setTaxward(String taxward) {
        this.taxward = taxward;
    }

    public String getWardno() {
        return wardno;
    }

    public void setWardno(String wardno) {
        this.wardno = wardno;
    }

    public String getZone() {
        return zone;
    }

    public void setZone(String zone) {
        this.zone = zone;
    }

    public String getWardname() {
        return wardname;
    }

    public void setWardname(String wardname) {
        this.wardname = wardname;
    }

    public String getTenantid() {
        return tenantid;
    }

    public String getId() {
        return id;
    }

    public String getPropertyid() {
        return propertyid;
    }

    public String getDoorno() {
        return doorno;
    }

    public String getPlotno() {
        return plotno;
    }

    public String getBuildingname() {
        return buildingname;
    }

    public String getStreet() {
        return street;
    }

    public String getLandmark() {
        return landmark;
    }

    public String getCity() {
        return city;
    }

    public String getPincode() {
        return pincode;
    }

    public String getLocality() {
        return locality;
    }

    public String getDistrict() {
        return district;
    }

    public String getRegion() {
        return region;
    }

    public String getState() {
        return state;
    }

    public String getCountry() {
        return country;
    }

    public BigDecimal getLatitude() {
        return latitude;
    }

    public BigDecimal getLongitude() {
        return longitude;
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


    public void setTenantid(String tenantid) {
        this.tenantid = tenantid;
    }

    public void setId(String id) {
        this.id = id;
    }

    public void setPropertyid(String propertyid) {
        this.propertyid = propertyid;
    }

    public void setDoorno(String doorno) {
        this.doorno = doorno;
    }

    public void setPlotno(String plotno) {
        this.plotno = plotno;
    }

    public void setBuildingname(String buildingname) {
        this.buildingname = buildingname;
    }

    public void setStreet(String street) {
        this.street = street;
    }

    public void setLandmark(String landmark) {
        this.landmark = landmark;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public void setPincode(String pincode) {
        this.pincode = pincode;
    }

    public void setLocality(String locality) {
        this.locality = locality;
    }

    public void setDistrict(String district) {
        this.district = district;
    }

    public void setRegion(String region) {
        this.region = region;
    }

    public void setState(String state) {
        this.state = state;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    public void setLatitude(BigDecimal latitude) {
        this.latitude = latitude;
    }

    public void setLongitude(BigDecimal longitude) {
        this.longitude = longitude;
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


