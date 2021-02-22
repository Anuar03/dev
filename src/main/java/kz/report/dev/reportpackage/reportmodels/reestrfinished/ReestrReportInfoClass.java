package kz.report.dev.reportpackage.reportmodels.reestrfinished;

import com.fasterxml.jackson.databind.JsonNode;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Objects;

public class ReestrReportInfoClass {
    private String stateName;
    private String regionName;
    private String strStartDate;
    private String strEndDate;

    private boolean isStateActive;
    private boolean isRegionActive;
    private boolean isAllUsers;
    private boolean isExcel;

    private int labId;
    private int stateDictId;
    private int regionDictId;

    private LocalDate startDate;
    private LocalDate endDate;

    public ReestrReportInfoClass(JsonNode jsonNode) {
        if (Objects.isNull(jsonNode)) return;
        strStartDate = jsonNode.get("startDate").asText();
        strEndDate = jsonNode.get("endDate").asText();
        isAllUsers = jsonNode.get("isAllUsers").asBoolean();
        isExcel = jsonNode.get("isExcel").asBoolean();
        loadStateAndStateDict(jsonNode);
        loadRegionAndRegionDict(jsonNode);
        loadLabId(jsonNode);
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
    private void loadLabId(JsonNode jsonNode) {
        if (!isAllUsers) labId = jsonNode.get("lab").asInt();
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

    public boolean isStateActive() {
        return isStateActive;
    }

    public boolean isRegionActive() {
        return isRegionActive;
    }

    public boolean isAllUsers() {
        return isAllUsers;
    }

    public boolean isExcel() {
        return isExcel;
    }

    public int getLabId() {
        return labId;
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
