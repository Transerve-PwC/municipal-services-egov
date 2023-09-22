package org.egov.pt.models.excel;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class LegacyRow {


	@CellAnnotation(index = 0)
	@CellReplaceAnnotation(target = "\\.0", source = "")
	private String ULBCode;
	@CellAnnotation(index = 1)
	private String ULBName;
	@CellAnnotation(index = 2)
	private String financialYear;
	@CellAnnotation(index = 3)
	private String PTIN;
	@CellAnnotation(index = 4)
	private String ownerName;
	@CellAnnotation(index = 5)
	private String FHName;
	@CellAnnotation(index = 6)
	private String houseNo;
	@CellAnnotation(index = 7)
	private String locality;
	@CellAnnotation(index = 8)
	private String taxWard;
	@CellAnnotation(index = 9)
	private String wardNo;
	@CellAnnotation(index = 10)
	private String zone;
	@CellAnnotation(index = 11)
	private String propertyTypeClassification;
	@CellAnnotation(index = 12)
	private String RCARV;
	@CellAnnotation(index = 13)
	@CellReplaceAnnotation(target = "Y|N", source = "0")
	private String arrearHouseTax;
	@CellAnnotation(index = 14)
	@CellReplaceAnnotation(target = "Y|N", source = "0")
	private String arrearWaterTax;
	@CellAnnotation(index = 15)
	@CellReplaceAnnotation(target = "Y|N", source = "0")
	private String arrearSewerTax;
	@CellAnnotation(index = 16)
	@CellReplaceAnnotation(target = "Y|N", source = "0")
	private String houseTax;
	@CellAnnotation(index = 17)
	@CellReplaceAnnotation(target = "Y|N", source = "0")
	private String waterTax;
	@CellAnnotation(index = 18)
	@CellReplaceAnnotation(target = "Y|N", source = "0")
	private String sewerTax;
	@CellAnnotation(index = 19)
	@CellReplaceAnnotation(target = "Y|N", source = "0")
	private String surchareHouseTax;
	@CellAnnotation(index = 20)
	@CellReplaceAnnotation(target = "Y|N", source = "0")
	private String surchareWaterTax;
	@CellAnnotation(index = 21)
	@CellReplaceAnnotation(target = "Y|N", source = "0")
	private String surchareSewerTax;
	@CellAnnotation(index = 22)
	private String billGeneratedTotal;
	@CellAnnotation(index = 23)
	private String totalPaidAmount;
	@CellAnnotation(index = 24)
	private String lastPaymentDate;
	@CellAnnotation(index = 25)
	private String address;
	@CellAnnotation(index = 26)
	private String mobile;
	@CellAnnotation(index = 27)
	private String buildingUsage;
	@CellAnnotation(index = 28)
	private String wardName;
	@CellAnnotation(index = 29)
	private String plotArea;
	@CellAnnotation(index = 30)
	private String totalCarpetArea;
	@CellAnnotation(index = 31)
	private String constructionYear;
	@CellAnnotation(index = 32)
	private String ptmsPropertyId;

}

