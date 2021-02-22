package kz.report.dev.reportpackage.reportmodels.reestrfinished;

import java.util.List;
import java.util.Map;
import java.util.TreeMap;

public class SubLabModel {
    private String subLabName;
    private Map<String, List<ReestrModel>> reestrModelList;

    public SubLabModel() {
        this.reestrModelList = new TreeMap<>();
    }

    public String getSubLabName() {
        return subLabName;
    }

    public void setSubLabName(String subLabName) {
        this.subLabName = subLabName;
    }

    public Map<String, List<ReestrModel>> getReestrModelList() {
        return reestrModelList;
    }

    public void put(String s, List<ReestrModel> list) {
        reestrModelList.put(s, list);
    }

    public boolean isReestrModelsEmpty() {
        return reestrModelList.isEmpty();
    }
}
