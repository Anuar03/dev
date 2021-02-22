package kz.report.dev.controller.datacontrollers;

import com.fasterxml.jackson.databind.ObjectMapper;
import kz.report.dev.models.excelmodels.Region;
import kz.report.dev.models.excelmodels.State;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.Iterator;
import java.util.Map;
import java.util.TreeMap;

@RestController
@RequestMapping("/getdata")
public class ReestrDataController {

    @Autowired
    private ObjectMapper mapper;

    @GetMapping("/{point}")
    public String getStatesData(@PathVariable String point) throws IOException {
        if ("state".equalsIgnoreCase(point)) {
            Map map = parseStates();
            String data = mapper.writeValueAsString(map);
            return data;
        }
        if ("region".equalsIgnoreCase(point)) {
            Map map = parseRegions();
            String data = mapper.writeValueAsString(map);
            return data;
        }

        return "";

    }



    private Map parseStates() throws IOException {
        ClassPathResource c = new ClassPathResource("excel/states.xlsx");
        Map<Integer, State> stateMap = new TreeMap<>();
        XSSFWorkbook workbook = new XSSFWorkbook(c.getInputStream());
        XSSFSheet sheet = workbook.getSheetAt(0);
        Iterator<Row> rowIterator = sheet.rowIterator();

        while (rowIterator.hasNext()) {

            XSSFRow row = (XSSFRow) rowIterator.next();
            if (row.getRowNum() == 0) {
                continue;
            }

            State state = new State();
            int id = Integer.MAX_VALUE;
            Iterator<Cell> cellIterator = row.cellIterator();

            while (cellIterator.hasNext()) {
                XSSFCell cell = (XSSFCell) cellIterator.next();

                switch (cell.getCellTypeEnum()) {
                    case NUMERIC: {
                        id = (int) cell.getNumericCellValue();
                        break;
                    }
                    case STRING: {
                        state.setName(cell.getStringCellValue());
                        break;
                    }
                    default: {
                        break;
                    }
                }
            }
            stateMap.put(id, state);
        }
        return stateMap;
    }

    private Map parseRegions() throws IOException {
        ClassPathResource c = new ClassPathResource("excel/regions.xlsx");
        Map<Integer, Region> regionMap = new TreeMap<>();
        XSSFWorkbook workbook = new XSSFWorkbook(c.getInputStream());
        XSSFSheet sheet = workbook.getSheetAt(0);
        Iterator<Row> rowIterator = sheet.rowIterator();

        while (rowIterator.hasNext()) {

            XSSFRow row = (XSSFRow) rowIterator.next();
            if (row.getRowNum() == 0) {
                continue;
            }
            Region region = new Region();
            int id = Integer.MAX_VALUE;
            Iterator<Cell> cellIterator = row.cellIterator();

            while (cellIterator.hasNext()) {
                XSSFCell cell = (XSSFCell) cellIterator.next();
                if (cell.getColumnIndex() == 0) {
                    id = (int) cell.getNumericCellValue();
                } else if (cell.getColumnIndex() == 1) {
                    region.setName(cell.getStringCellValue());
                } else if (cell.getColumnIndex() == 2) {
                    region.setState_id((int)cell.getNumericCellValue());
                }
            }
            regionMap.put(id, region);
        }
        return regionMap;
    }
}
