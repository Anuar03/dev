package kz.report.dev.reportpackage.reportmodels.reestrfinished;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

public class LabModel {
    private String labName;
    private List<SubLabModel> subLabModelList;
    private Map<String, List<ReestrModel>> reestrModels;

    public LabModel() {
        this.reestrModels = new TreeMap<>();
        this.subLabModelList = new ArrayList<>();
    }

    public String getLabName() {
        return labName;
    }

    public void setLabName(String labName) {
        this.labName = labName;
    }

    public List<SubLabModel> getLabModelList() {
        return subLabModelList;
    }

    public void setSubLabModelList(List<SubLabModel> subLabModelList) {
        this.subLabModelList = subLabModelList;
    }

    public Map<String, List<ReestrModel>> getReestrModels() {
        return reestrModels;
    }

    public void setReestrModels(Map<String, List<ReestrModel>> reestrModels) {
        this.reestrModels = reestrModels;
    }

    public boolean isSubLabModelEmpty() {
        return subLabModelList.isEmpty();
    }

    public boolean isReestrModelsEmpty() {
        return reestrModels.isEmpty();
    }
}
