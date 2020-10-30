package org.egov.pt.repository;

import org.egov.pt.models.excel.Property;
import org.egov.pt.models.excel.PropertyPayment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;


@Repository
public interface PropertyPaymentExcelRepository extends JpaRepository<PropertyPayment, Long> {



}


