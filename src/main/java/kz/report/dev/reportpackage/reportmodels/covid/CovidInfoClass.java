package kz.report.dev.reportpackage.reportmodels.covid;

import com.fasterxml.jackson.databind.JsonNode;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Objects;

public class CovidInfoClass {

    private String stateName;
    private String regionName;
    private String strStartDate;
    private String strEndDate;

    private boolean isExcel;
    private boolean isStateActive;
    private boolean isRegionActive;

    private int stateDictId;
    private int regionDictId;

    private LocalDate startDate;
    private LocalDate endDate;

    public CovidInfoClass(JsonNode jsonNode) {
        if (Objects.isNull(jsonNode)) return;
        strStartDate = jsonNode.get("startDate").asText();
        strEndDate = jsonNode.get("endDate").asText();
        isExcel = jsonNode.get("isExcel").asBoolean();
        loadStateAndStateDict(jsonNode);
        loadRegionAndRegionDict(jsonNode);
        loadStartDate();
        loadEndDate();
    }

    private void loadStateAndStateDict(JsonNode jsonNode) {
        if (!jsonNode.get("state").asText().equalsIgnoreCase("Выбор...")) isStateActive = true;

        if (isStateActive) {
            stateName = jsonNode.get("stateName").asText();
            stateDictId = jsonNode.get("state").asInt();
        }
    }

    private void loadRegionAndRegionDict(JsonNode jsonNode) {
        if (!jsonNode.get("region").asText().equalsIgnoreCase("Выбор...")) isRegionActive = true;

        if (isRegionActive) {
            regionName = jsonNode.get("regionName").get("value").asText();
            regionDictId = jsonNode.get("region").asInt();
        }
    }

    private void loadStartDate() {
        startDate = LocalDate.parse(strStartDate, DateTimeFormatter.ISO_LOCAL_DATE);
    }

    private void loadEndDate() {
        endDate = LocalDate.parse(strEndDate, DateTimeFormatter.ISO_LOCAL_DATE);
    }

    public String getStateName() {
        return stateName;
    }

    public String getRegionName() {
        return regionName;
    }

    public String getStrStartDate() {
        return strStartDate;
    }

    public String getStrEndDate() {
        return strEndDate;
    }

    public boolean isExcel() {
        return isExcel;
    }

    public int getStateDictId() {
        return stateDictId;
    }

    public int getRegionDictId() {
        return regionDictId;
    }

    public LocalDate getStartDate() {
        return startDate;
    }

    public LocalDate getEndDate() {
        return endDate;
    }
}
