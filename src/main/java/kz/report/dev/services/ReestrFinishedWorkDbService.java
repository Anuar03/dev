package kz.report.dev.services;

import kz.report.dev.reportpackage.reportmodels.reestrfinished.LabModel;
import kz.report.dev.reportpackage.reportmodels.reestrfinished.ReestrModel;
import kz.report.dev.reportpackage.reportmodels.reestrfinished.ReestrReportInfoClass;
import kz.report.dev.reportpackage.reportmodels.reestrfinished.SubLabModel;
import kz.report.dev.utils.StrUtils.StrUlits;
import kz.report.dev.utils.dbutils.ConnectionPool;
import kz.report.dev.utils.dbutils.ConnectionPoolImpl;
import kz.report.dev.utils.numberutils.NumberUtils;

import java.sql.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

public class ReestrFinishedWorkDbService {
    private ReestrReportInfoClass reportInfoClass;


    private List<String> documentIds;
    private Map<String, Integer> rowsCount;
    private List<ReestrModel> reestrModels;
    private final ConnectionPool connectionPool = ConnectionPoolImpl.getInstance(20);

    public ReestrFinishedWorkDbService(ReestrReportInfoClass reestrReportInfoClass) {
        this.reportInfoClass = reestrReportInfoClass;
        documentIds = new ArrayList<>();
        rowsCount = new HashMap<>();
        reestrModels = new ArrayList<>();

    }

    public List<LabModel> getLabModelList() {
        getDocumentIdList();
        getDynCount();
        getDynData();
        groupByTarCode();
        return groupSubLanName();
    }

    private String getDocumentIdList() {
        Connection con = connectionPool.getConnection();
        try (PreparedStatement preparedStatement = con.prepareStatement(sqlDocumentIdList(reportInfoClass.getStateDictId(), reportInfoClass.getRegionDictId()))) {
            preparedStatement.setString(1, reportInfoClass.getStartDate().format(DateTimeFormatter.ofPattern("yyyy-MM-dd 00:00:00")));
            preparedStatement.setString(2, reportInfoClass.getEndDate().format(DateTimeFormatter.ofPattern("yyyy-MM-dd 23:59:59")));
            ResultSet set = preparedStatement.executeQuery();
            while (set.next()) {
                documentIds.add(set.getString("documentID"));
            }
        } catch (SQLException e) {
            System.out.println("Документы не получены");
        } finally {
            connectionPool.returnConnection(con);
        }
        return "";
    }


    private String sqlDocumentIdList(int stateId, int regionId) {
        String stateFilter = "";
        String regionFilter = "";
        if (stateId != 0) {
            stateFilter = "join asf_data_index state on state.uuid = rd.asfDataID and state.cmp_id = 'personal_card_name_company_ru1' and state.cmp_key =" + stateId + " ";
        } else {
            stateFilter = "join asf_data_index state on state.uuid = rd.asfDataID and state.cmp_id = 'personal_card_name_company_ru1' and state.cmp_key in (2,3,4,5,6,7,8,9,10,11,12,13,14,15,16,17,18,19,20,21) ";
        }
        if (regionId != 0) {
            regionFilter = "join asf_data_index region on region.uuid = rd.asfDataID and region.cmp_id = 'personal_card_name_department_ru1' and region.cmp_key =" + regionId + " ";
        }
        return "select rd.documentID from registries " +
                "left join registry_documents rd on registries.registryID = rd.registryID " +
                "left join asf_data_index adi on adi.uuid = rd.asfDataID and adi.cmp_id = 'research_end_date' " +
                "left join asf_data_index status on status.uuid = rd.asfDataID and status.cmp_id = 'status' " +
                stateFilter +
                regionFilter +
                "where code = 'reestr_registratsiya_ob_ekta' and adi.cmp_key between ? and ? and rd.deleted is null and status.cmp_key = 7";
    }

    private void getDynCount() {
        Connection con = connectionPool.getConnection();
        try (PreparedStatement preparedStatement = con.prepareStatement(sqlDynCount())) {
            ResultSet set = preparedStatement.executeQuery();
            while (set.next()) {
                rowsCount.put(set.getString("documentID"), set.getInt("rows"));
            }
        } catch (SQLException e) {
            System.out.println("Количество не получено");
        } finally {
            connectionPool.returnConnection(con);
        }
    }

    private String sqlDynCount() {
        String prepareList = documentIds.stream().
                map(s-> "'"+s+"'")
                .collect(Collectors.joining(",", "(", ")"));
        String sql = "select * from " +
                "( " +
                "    select rd.documentID, max(adi.cmp_row) rows from registries " +
                "                                                         left join registry_documents rd on registries.registryID = rd.registryID " +
                "                                                         left join asf_data_index adi on adi.uuid = rd.asfDataID and adi.cmp_id like '%lab_name-%' " +
                "    where code = 'reestr_registratsiya_ob_ekta' " +
                "    and rd.documentID in (?) " +
                "    group by rd.documentID " +
                ") t1 " +
                "where t1.rows is not null";
        sql = sql.replace("(?)", prepareList);
        return sql;
    }

    private void getDynData() {
        Connection con = connectionPool.getConnection();
        try (PreparedStatement preparedStatement = con.prepareStatement(sqlDynData())) {
            String.join(", ", rowsCount.keySet());
            for (Map.Entry<String, Integer> entry : rowsCount.entrySet()) {
                int count = entry.getValue();
                for (int i = 1; i <= count; i++) {
                    preparedStatement.setString(1, "research_register-b" + i);
                    preparedStatement.setString(2, "lab_name-b" + i);
                    preparedStatement.setString(3, "research_group-b" + i);
                    preparedStatement.setString(4, "research_subgroup-b" + i);
                    preparedStatement.setString(5, "research_method-b" + i);
                    preparedStatement.setString(6, "service_unit-b" + i);
                    preparedStatement.setString(7, "cost_coefficient-b" + i);
                    preparedStatement.setString(8, "tarificator_research_code-b" + i);
                    preparedStatement.setString(9, "base_rate-b" + i);
                    preparedStatement.setString(10, "services_amount-b" + i);
                    preparedStatement.setString(11, "mismatch_services_amount-b" + i);
                    preparedStatement.setString(12, entry.getKey());
                    ResultSet set = preparedStatement.executeQuery();
                    while (set.next()) {
                        if (reportInfoClass.getLabId() > 0) {
                            int labCode = NumberUtils.parseInt(getValue(set, "labCode"));
                            if (labCode != reportInfoClass.getLabId()) continue;
                        }
                        ReestrModel reestrModel = new ReestrModel();
                        reestrModel.setDocumentID(entry.getKey());
                        reestrModel.setResName(getValue(set, "resname"));
                        reestrModel.setLabName(getValue(set, "labname"));
                        reestrModel.setLabNameCode(getValue(set, "labCode"));
                        reestrModel.setResGroup(getValue(set, "resgroup"));
                        reestrModel.setResGroupCode(getValue(set, "resGroupCode"));
                        reestrModel.setSubResGroup(getValue(set, "subresgroup"));
                        reestrModel.setSubResGroupCode(getValue(set, "subresgroupCode"));
                        reestrModel.setResMethod(getValue(set, "resmethod"));
                        reestrModel.setSerUnit(getValue(set, "serunit"));
                        reestrModel.setCoeficient(getValue(set, "coeficient"));
                        reestrModel.setTarCode(getValue(set, "tarcode"));
                        reestrModel.setBaseRate(getValue(set, "base_rate"));
                        reestrModel.setSerAmount(getValue(set, "seramount"));
                        reestrModel.setmSerAmount(getValue(set, "mseramount"));
                        reestrModel.setSubLabName(getValue(set, "subLabName"));
                        reestrModel.setSubLabNameCode(getValue(set, "subLabNameCode"));
                        reestrModel.setEndDate(getValue(set, "endDate"));
                        reestrModels.add(reestrModel);
                    }
                }
            }
        } catch (Exception e) {
            System.out.println("Записи не получены");
        } finally {
            connectionPool.returnConnection(con);
        }
    }

    private String sqlDynData() {
        return "select\n" +
                "    rd.documentID,\n" +
                "    resname.cmp_data resname,\n" +
                "    labname.cmp_data labname,\n" +
                "    labname.cmp_key labCode,\n" +
                "    resgroup.cmp_data resgroup,\n" +
                "    resgroup.cmp_key resGroupCode,\n" +
                "    subresgroup.cmp_data subresgroup,\n" +
                "    subresgroup.cmp_key subresgroupCode,\n" +
                "    resmethod.cmp_data resmethod,\n" +
                "    serunit.cmp_data serunit,\n" +
                "    coeficient.cmp_data coeficient,\n" +
                "    tarcode.cmp_data tarcode,\n" +
                "    base_rate.cmp_data base_rate,\n" +
                "    seramount.cmp_data seramount,\n" +
                "    mseramount.cmp_data mseramount,\n" +
                "    subLabName.cmp_data subLabName,\n" +
                "    subLabName.cmp_key subLabNameCode, \n" +
                "    endDate.cmp_data endDate \n" +
                "from registries\n" +
                "left join registry_documents rd on registries.registryID = rd.registryID\n" +
                "left join asf_data_index resname on resname.uuid = rd.asfDataID and resname.cmp_id = ?\n" +
                "left join asf_data_index labname on labname.uuid = rd.asfDataID and labname.cmp_id = ?\n" +
                "left join asf_data_index resgroup on resgroup.uuid = rd.asfDataID and resgroup.cmp_id = ?\n" +
                "left join asf_data_index subresgroup on subresgroup.uuid = rd.asfDataID and subresgroup.cmp_id = ?\n" +
                "left join asf_data_index resmethod on resmethod.uuid = rd.asfDataID and resmethod.cmp_id = ?\n" +
                "left join asf_data_index serunit on serunit.uuid = rd.asfDataID and serunit.cmp_id = ?\n" +
                "left join asf_data_index coeficient on coeficient.uuid = rd.asfDataID and coeficient.cmp_id = ?\n" +
                "left join asf_data_index tarcode on tarcode.uuid = rd.asfDataID and tarcode.cmp_id = ?\n" +
                "left join asf_data_index base_rate on base_rate.uuid = rd.asfDataID and base_rate.cmp_id = ?\n" +
                "left join asf_data_index seramount on seramount.uuid = rd.asfDataID and seramount.cmp_id = ?\n" +
                "left join asf_data_index mseramount on mseramount.uuid = rd.asfDataID and mseramount.cmp_id = ?\n" +
                "left join asf_data_index subLabName on subLabName.uuid = rd.asfDataID and subLabName.cmp_id = 'list_department' \n" +
                "left join asf_data_index endDate on endDate.uuid = rd.asfDataID and endDate.cmp_id = 'research_end_date' \n" +
                "where code = 'reestr_registratsiya_ob_ekta' and rd.documentID = ?";
    }

    private String getValue(ResultSet set, String fieldName) {
        String value;
        try {
            value = set.getString(fieldName);
        } catch (SQLException e) {
            value = "";
        }
        return value;
    }

    private void groupByTarCode() {
        Set<String> set = new HashSet<>();
        for (ReestrModel model : reestrModels) {
            set.add(model.getTarCode());
        }
        for (String s : set) {
            List<ReestrModel> list = new ArrayList<>();
            Iterator<ReestrModel> l = reestrModels.listIterator();
            while (l.hasNext()) {
                ReestrModel m = l.next();
                if (Objects.isNull(m.getTarCode())) {
                    l.remove();
                    continue;
                }
                if (m.getTarCode().equalsIgnoreCase(s)) {
                    list.add(m);
                    l.remove();
                }
            }
            int serviceCount = 0;
            int researchCount = 0;
            for (ReestrModel m : list) {
                serviceCount += NumberUtils.parseInt(m.getSerAmount());
                researchCount += NumberUtils.parseInt(m.getmSerAmount());
            }
            if (Objects.isNull(s)) {
                continue;
            }
            ReestrModel r = new ReestrModel();
            r.setDocumentID(list.get(0).getDocumentID());
            r.setResName(list.get(0).getResName());
            r.setLabName(list.get(0).getLabName());
            r.setLabNameCode(list.get(0).getLabNameCode());
            r.setResGroup(list.get(0).getResGroup());
            r.setResGroupCode(list.get(0).getResGroupCode());
            r.setSubResGroup(list.get(0).getSubResGroup());
            r.setSubResGroupCode(list.get(0).getSubResGroupCode());
            r.setResMethod(list.get(0).getResMethod());
            r.setSerUnit(list.get(0).getSerUnit());
            r.setCoeficient(list.get(0).getCoeficient());
            r.setTarCode(list.get(0).getTarCode());
            r.setSubLabName(list.get(0).getSubLabName());
            r.setSubLabNameCode(list.get(0).getSubLabNameCode());
            r.setBaseRate(list.get(0).getBaseRate());
            r.setSerAmount(String.valueOf(serviceCount));
            r.setmSerAmount(String.valueOf(researchCount));
            reestrModels.add(r);
            list.clear();
        }
    }

    private List<LabModel> groupSubLanName() {

        List<LabModel> labModels = new ArrayList<>();

        Set<String> labNameSet = new TreeSet<>();
        for (ReestrModel model : reestrModels) {
            labNameSet.add(model.getLabName());
        }
        Map<String, Set<String>> subLabNames = new HashMap<>();
        for (String s : labNameSet) {
            if (Objects.isNull(s)) continue;
            Set<String> set = new HashSet<>();
            for (ReestrModel model : reestrModels) {
                if (model.getLabName().equalsIgnoreCase(s) && !model.getSubLabNameCode().equalsIgnoreCase("00")) set.add(model.getSubLabName());
            }
            subLabNames.put(s, set);
        }
        Set<String> resGroupSet = new TreeSet<>();
        for (ReestrModel model : reestrModels) {
            resGroupSet.add(model.getResGroup());
        }

        for (String labName : labNameSet) {
            if (Objects.isNull(labName)) continue;
            LabModel labModel = new LabModel();
            labModel.setLabName(labName);
            List<SubLabModel> subLabModels = new ArrayList<>();
            if (subLabNames.get(labName).size() > 0) {
                for (String s : subLabNames.get(labName)) {
                    SubLabModel subLabModel = new SubLabModel();
                    subLabModel.setSubLabName(s);
                    if (Objects.isNull(s)) continue;
                    for (String resGroup : resGroupSet) {
                        List<ReestrModel> list = new ArrayList<>();
                        if (Objects.isNull(resGroup)) continue;
                        for (ReestrModel model : reestrModels) {
                            if (model.getLabName().equalsIgnoreCase(labName) && model.getResGroup().equalsIgnoreCase(resGroup) && model.getSubLabName().equalsIgnoreCase(s)) {
                                list.add(model);
                            }
                        }
                        subLabModel.put(resGroup, list);
                    }
                    subLabModels.add(subLabModel);
                }
                labModel.setSubLabModelList(subLabModels);
                Map<String, List<ReestrModel>> mapModels = new HashMap<>();
                for (String resGroup : resGroupSet) {
                    List<ReestrModel> list = new ArrayList<>();
                    if (Objects.isNull(resGroup)) continue;
                    for (ReestrModel model : reestrModels) {
                        if (model.getLabName().equalsIgnoreCase(labName) && model.getResGroup().equalsIgnoreCase(resGroup) && NumberUtils.parseInt(model.getSubLabNameCode()) == 0) {
                            list.add(model);
                        }
                    }
                    mapModels.put(resGroup, list);
                }
                if (mapModels.size() > 0) {
                    labModel.setReestrModels(mapModels);
                }
                labModels.add(labModel);
            } else {
                Map<String, List<ReestrModel>> mapModels = new HashMap<>();
                for (String resGroup : resGroupSet) {
                    List<ReestrModel> list = new ArrayList<>();
                    if (Objects.isNull(resGroup)) continue;
                    for (ReestrModel model : reestrModels) {
                        if (model.getLabName().equalsIgnoreCase(labName) && model.getResGroup().equalsIgnoreCase(resGroup)) {
                            list.add(model);
                        }
                    }
                    mapModels.put(resGroup, list);
                }
                labModel.setReestrModels(mapModels);
                labModels.add(labModel);
            }
        }
        return labModels;
    }
}
