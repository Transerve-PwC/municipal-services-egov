package org.egov.pt.models.excel;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import java.math.BigDecimal;

@Entity
@Table(name = "eg_pt_property_payment")
public class PropertyPayment {
    @Id
    private String id;
    private String propertyid;
    private String financialyear;
    private BigDecimal arrearhousetax;
    private BigDecimal arrearwatertax;
    private BigDecimal arrearsewertax;
    private BigDecimal housetax;
    private BigDecimal watertax;
    private BigDecimal sewertax;
    private BigDecimal surcharehousetax;
    private BigDecimal surcharewatertax;
    private BigDecimal surcharesewertax;
    private BigDecimal billgeneratedtotal;
    private BigDecimal totalpaidamount;
    private String lastpaymentdate;

    public String getId() {
        return id;
    }

    public String getPropertyid() {
        return propertyid;
    }

    public String getFinancialyear() {
        return financialyear;
    }

    public BigDecimal getArrearhousetax() {
        return arrearhousetax;
    }

    public BigDecimal getArrearwatertax() {
        return arrearwatertax;
    }

    public BigDecimal getArrearsewertax() {
        return arrearsewertax;
    }

    public BigDecimal getHousetax() {
        return housetax;
    }

    public BigDecimal getWatertax() {
        return watertax;
    }

    public BigDecimal getSewertax() {
        return sewertax;
    }

    public BigDecimal getSurcharehousetax() {
        return surcharehousetax;
    }

    public BigDecimal getSurcharewatertax() {
        return surcharewatertax;
    }

    public BigDecimal getSurcharesewertax() {
        return surcharesewertax;
    }

    public BigDecimal getBillgeneratedtotal() {
        return billgeneratedtotal;
    }

    public BigDecimal getTotalpaidamount() {
        return totalpaidamount;
    }

    public String getLastpaymentdate() {
        return lastpaymentdate;
    }

    public void setId(String id) {
        this.id = id;
    }

    public void setPropertyid(String propertyid) {
        this.propertyid = propertyid;
    }

    public void setFinancialyear(String financialyear) {
        this.financialyear = financialyear;
    }


    public void setArrearhousetax(BigDecimal arrearhousetax) {
        this.arrearhousetax = arrearhousetax;
    }

    public void setArrearwatertax(BigDecimal arrearwatertax) {
        this.arrearwatertax = arrearwatertax;
    }

    public void setArrearsewertax(BigDecimal arrearsewertax) {
        this.arrearsewertax = arrearsewertax;
    }

    public void setHousetax(BigDecimal housetax) {
        this.housetax = housetax;
    }

    public void setWatertax(BigDecimal watertax) {
        this.watertax = watertax;
    }

    public void setSewertax(BigDecimal sewertax) {
        this.sewertax = sewertax;
    }

    public void setSurcharehousetax(BigDecimal surcharehousetax) {
        this.surcharehousetax = surcharehousetax;
    }

    public void setSurcharewatertax(BigDecimal surcharewatertax) {
        this.surcharewatertax = surcharewatertax;
    }

    public void setSurcharesewertax(BigDecimal surcharesewertax) {
        this.surcharesewertax = surcharesewertax;
    }

    public void setBillgeneratedtotal(BigDecimal billgeneratedtotal) {
        this.billgeneratedtotal = billgeneratedtotal;
    }

    public void setTotalpaidamount(BigDecimal totalpaidamount) {
        this.totalpaidamount = totalpaidamount;
    }

    public void setLastpaymentdate(String lastpaymentdate) {
        this.lastpaymentdate = lastpaymentdate;
    }
}
