package org.egov.pt.repository;

import org.egov.pt.models.excel.Property;
import org.egov.pt.models.excel.Unit;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;


@Repository
public interface UnitExcelRepository extends JpaRepository<Unit, Long> {



}

