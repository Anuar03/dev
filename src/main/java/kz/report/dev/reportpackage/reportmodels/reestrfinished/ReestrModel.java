package kz.report.dev.reportpackage.reportmodels.reestrfinished;

import java.sql.Date;

public class ReestrModel implements Comparable<ReestrModel> {
    private String dbId;
    private String documentID;
    private Date date;
    private String stateId;
    private String regionId;
    private String resName;
    private String labName;
    private String labNameCode;
    private String resGroup;
    private String resGroupCode;
    private String subResGroup;
    private String subResGroupCode;
    private String resMethod;
    private String serUnit;
    private String coeficient;
    private String tarCode;
    private String baseRate;
    private String serAmount;
    private String mSerAmount;
    private String subLabName;
    private String subLabNameCode;
    private String endDate;

    public String getDocumentID() {
        return documentID;
    }

    public void setDocumentID(String documentID) {
        this.documentID = documentID;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public String getStateId() {
        return stateId;
    }

    public void setStateId(String stateId) {
        this.stateId = stateId;
    }

    public String getRegionId() {
        return regionId;
    }

    public void setRegionId(String regionId) {
        this.regionId = regionId;
    }

    public String getResName() {
        return resName;
    }

    public void setResName(String resName) {
        this.resName = resName;
    }

    public String getLabName() {
        return labName;
    }

    public void setLabName(String labName) {
        this.labName = labName;
    }

    public String getLabNameCode() {
        return labNameCode;
    }

    public void setLabNameCode(String labNameCode) {
        this.labNameCode = labNameCode;
    }

    public String getResGroup() {
        return resGroup;
    }

    public void setResGroup(String resGroup) {
        this.resGroup = resGroup;
    }

    public String getResGroupCode() {
        return resGroupCode;
    }

    public void setResGroupCode(String resGroupCode) {
        this.resGroupCode = resGroupCode;
    }

    public String getSubResGroup() {
        return subResGroup;
    }

    public void setSubResGroup(String subResGroup) {
        this.subResGroup = subResGroup;
    }

    public String getSubResGroupCode() {
        return subResGroupCode;
    }

    public void setSubResGroupCode(String subResGroupCode) {
        this.subResGroupCode = subResGroupCode;
    }

    public String getResMethod() {
        return resMethod;
    }

    public void setResMethod(String resMethod) {
        this.resMethod = resMethod;
    }

    public String getSerUnit() {
        return serUnit;
    }

    public void setSerUnit(String serUnit) {
        this.serUnit = serUnit;
    }

    public String getCoeficient() {
        return coeficient;
    }

    public void setCoeficient(String coeficient) {
        this.coeficient = coeficient;
    }

    public String getTarCode() {
        return tarCode;
    }

    public void setTarCode(String tarCode) {
        this.tarCode = tarCode;
    }

    public String getBaseRate() {
        return baseRate;
    }

    public void setBaseRate(String baseRate) {
        this.baseRate = baseRate;
    }

    public String getSerAmount() {
        return serAmount;
    }

    public void setSerAmount(String serAmount) {
        this.serAmount = serAmount;
    }

    public String getmSerAmount() {
        return mSerAmount;
    }

    public void setmSerAmount(String mSerAmount) {
        this.mSerAmount = mSerAmount;
    }

    public String getSubLabName() {
        return subLabName;
    }

    public void setSubLabName(String subLabName) {
        this.subLabName = subLabName;
    }

    public String getSubLabNameCode() {
        return subLabNameCode;
    }

    public void setSubLabNameCode(String subLabNameCode) {
        this.subLabNameCode = subLabNameCode;
    }

    public String getEndDate() {
        return endDate;
    }

    public void setEndDate(String endDate) {
        this.endDate = endDate;
    }

    @Override
    public int compareTo(ReestrModel model) {
        if (Integer.parseInt(this.getLabNameCode()) == 16) {
            if (Integer.parseInt(model.getLabNameCode()) == 16) {
                return Long.compare(Long.parseLong(this.getTarCode()), Long.parseLong(model.getTarCode()));
            } else {
                return 1;
            }
        } else {
            if (Integer.parseInt(model.getLabNameCode()) == 16) {
                return -1;
            } else {
                return Long.compare(Long.parseLong(this.getTarCode()), Long.parseLong(model.getTarCode()));
            }
        }
    }

    public String getDbId() {
        return dbId;
    }

    public void setDbId(String dbId) {
        this.dbId = dbId;
    }
}
