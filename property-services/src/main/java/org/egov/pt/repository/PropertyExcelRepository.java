package org.egov.pt.repository;

import org.egov.pt.models.excel.Owner;
import org.egov.pt.models.excel.Property;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;


@Repository
public interface PropertyExcelRepository extends JpaRepository<Property, Long> {



}

