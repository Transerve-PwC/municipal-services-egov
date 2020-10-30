package org.egov.pt.models.excel;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.autoconfigure.domain.EntityScan;

import javax.persistence.*;
import java.math.BigDecimal;
import java.sql.Timestamp;

@Entity
@Table(name = "eg_user")
@SequenceGenerator(name="seq_eg_user", initialValue=1, allocationSize=1)
public class User {
    private String title;
    private String salutation;
    private Timestamp dob;
    private String locale;
    private String username;
    private String password;
    private Timestamp pwdexpirydate;
    private String mobilenumber;
    private String altcontactnumber;
    private String emailid;
    private Timestamp createddate;
    private Timestamp lastmodifieddate;
    private Long createdby;
    private Long lastmodifiedby;
    private Boolean active;
    private String name;
    private short gender;
    private String pan;
    private String aadhaarnumber;
    private String type;
    private BigDecimal version;
    private String guardian;
    private String guardianrelation;
    private String signature;
    private Boolean accountlocked;
    private String bloodgroup;
    private String photo;
    private String identificationmark;
    private String tenantid;
    @GeneratedValue(strategy= GenerationType.SEQUENCE, generator="seq_eg_user")
    @Id
    private Long id;
    @Column(name = "uuid")
    private String uuid;
    private Long accountlockeddate;

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getSalutation() {
        return salutation;
    }

    public void setSalutation(String salutation) {
        this.salutation = salutation;
    }

    public Timestamp getDob() {
        return dob;
    }

    public void setDob(Timestamp dob) {
        this.dob = dob;
    }

    public String getLocale() {
        return locale;
    }

    public void setLocale(String locale) {
        this.locale = locale;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public Timestamp getPwdexpirydate() {
        return pwdexpirydate;
    }

    public void setPwdexpirydate(Timestamp pwdexpirydate) {
        this.pwdexpirydate = pwdexpirydate;
    }

    public String getMobilenumber() {
        return mobilenumber;
    }

    public void setMobilenumber(String mobilenumber) {
        this.mobilenumber = mobilenumber;
    }

    public String getAltcontactnumber() {
        return altcontactnumber;
    }

    public void setAltcontactnumber(String altcontactnumber) {
        this.altcontactnumber = altcontactnumber;
    }

    public String getEmailid() {
        return emailid;
    }

    public void setEmailid(String emailid) {
        this.emailid = emailid;
    }

    public Timestamp getCreateddate() {
        return createddate;
    }

    public void setCreateddate(Timestamp createddate) {
        this.createddate = createddate;
    }

    public Timestamp getLastmodifieddate() {
        return lastmodifieddate;
    }

    public void setLastmodifieddate(Timestamp lastmodifieddate) {
        this.lastmodifieddate = lastmodifieddate;
    }

    public Long getCreatedby() {
        return createdby;
    }

    public void setCreatedby(Long createdby) {
        this.createdby = createdby;
    }

    public Long getLastmodifiedby() {
        return lastmodifiedby;
    }

    public void setLastmodifiedby(Long lastmodifiedby) {
        this.lastmodifiedby = lastmodifiedby;
    }

    public Boolean getActive() {
        return active;
    }

    public void setActive(Boolean active) {
        this.active = active;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public short getGender() {
        return gender;
    }

    public void setGender(short gender) {
        this.gender = gender;
    }

    public String getPan() {
        return pan;
    }

    public void setPan(String pan) {
        this.pan = pan;
    }

    public String getAadhaarnumber() {
        return aadhaarnumber;
    }

    public void setAadhaarnumber(String aadhaarnumber) {
        this.aadhaarnumber = aadhaarnumber;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public BigDecimal getVersion() {
        return version;
    }

    public void setVersion(BigDecimal version) {
        this.version = version;
    }

    public String getGuardian() {
        return guardian;
    }

    public void setGuardian(String guardian) {
        this.guardian = guardian;
    }

    public String getGuardianrelation() {
        return guardianrelation;
    }

    public void setGuardianrelation(String guardianrelation) {
        this.guardianrelation = guardianrelation;
    }

    public String getSignature() {
        return signature;
    }

    public void setSignature(String signature) {
        this.signature = signature;
    }

    public Boolean getAccountlocked() {
        return accountlocked;
    }

    public void setAccountlocked(Boolean accountlocked) {
        this.accountlocked = accountlocked;
    }

    public String getBloodgroup() {
        return bloodgroup;
    }

    public void setBloodgroup(String bloodgroup) {
        this.bloodgroup = bloodgroup;
    }

    public String getPhoto() {
        return photo;
    }

    public void setPhoto(String photo) {
        this.photo = photo;
    }

    public String getIdentificationmark() {
        return identificationmark;
    }

    public void setIdentificationmark(String identificationmark) {
        this.identificationmark = identificationmark;
    }

    public String getTenantid() {
        return tenantid;
    }

    public void setTenantid(String tenantid) {
        this.tenantid = tenantid;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    public Long getAccountlockeddate() {
        return accountlockeddate;
    }

    public void setAccountlockeddate(Long accountlockeddate) {
        this.accountlockeddate = accountlockeddate;
    }
}


