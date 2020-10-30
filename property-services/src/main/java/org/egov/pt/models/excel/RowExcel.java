package org.egov.pt.models.excel;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.apache.poi.ss.usermodel.Cell;

import java.util.Map;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class RowExcel {
    Map<Integer, String> header;
    Map<Integer, Cell> cells;
    int rowIndex;
}
