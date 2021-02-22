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

public class ReestrFinishedWorkDbServiceNew {
    private ReestrReportInfoClass reportInfoClass;

    private List<ReestrModel> reestrModels;

    private Map<String, String> rateMap = new HashMap<>();

    private Map<String, String> coeficientMap = new HashMap<>();

    private final ConnectionPool connectionPool = ConnectionPoolImpl.getInstance(20);

    public ReestrFinishedWorkDbServiceNew(ReestrReportInfoClass reestrReportInfoClass) {
        this.reportInfoClass = reestrReportInfoClass;
        reestrModels = new ArrayList<>();
    }

    public List<LabModel> getLabModelList() {
        getTarif();
        getCoeficient();
        collectData();
        groupByTarCode();
        return groupSubLanName();
    }

    private void collectData() {
        Connection conn = connectionPool.getConnection();
        try (PreparedStatement pre = conn.prepareStatement(sql())) {
            pre.setString(1, reportInfoClass.getStartDate().format(DateTimeFormatter.ofPattern("yyyy-MM-dd 00:00:00")));
            pre.setString(2, reportInfoClass.getEndDate().format(DateTimeFormatter.ofPattern("yyyy-MM-dd 23:59:59")));
            ResultSet set =  pre.executeQuery();
            long cc = 0;
            while (set.next()) {
                cc++;
                ReestrModel reestrModel = new ReestrModel();
                reestrModel.setDocumentID(getValue(set, "documentID"));
                reestrModel.setResName(getValue(set, "resname"));
                reestrModel.setLabName(getValue(set, "labname"));
                reestrModel.setLabNameCode(getValue(set, "labCode"));
                reestrModel.setResGroup(getValue(set, "resgroup"));
                reestrModel.setResGroupCode(getValue(set, "resgroupCode"));
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
                reestrModel.setEndDate(getValue(set, "dbeg"));
                reestrModels.add(reestrModel);
            }
            System.out.println("Count rows = " + cc);
        } catch (SQLException e) {
            System.out.println("Error while getting data from db");
        } finally {
            connectionPool.returnConnection(conn);
        }
    }

    private String sql() {
        String stateFilter = "";
        String regionFilter = "";
        String labFilter = "";
        if (reportInfoClass.getStateDictId() != 0) {
            stateFilter = "stateid = " + reportInfoClass.getStateDictId();
        } else {
            stateFilter = "stateid in (1, 2, 3, 4, 5, 6, 7, 8, 9, 10 ,11, 12, 13, 14, 15,16, 17, 18, 19, 20, 21, 22, 23)";
//            return sqlAll();
        }
        if (reportInfoClass.getRegionDictId() != 0) {
            regionFilter = "regionid = " + reportInfoClass.getRegionDictId();
        }
        if (reportInfoClass.getLabId() != 0 ) {
            labFilter = "labcode = " + reportInfoClass.getLabId();
        }
        return "select * from finished_work where dbeg between ? and ? and tarcode != '08120012' and " +
                stateFilter + ((reportInfoClass.getRegionDictId() != 0) ? " and " + regionFilter : "") +
                ((reportInfoClass.getLabId() != 0) ? " and " + labFilter : "") + " order by tarcode";
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
            if (this.coeficientMap.containsKey(s)) {
                r.setCoeficient(this.coeficientMap.get(s));
            } else {
                System.out.println("Tarcode doesn't find");
                r.setCoeficient(list.get(0).getCoeficient());
            }
            r.setTarCode(list.get(0).getTarCode());
            r.setSubLabName(list.get(0).getSubLabName());
            r.setSubLabNameCode(list.get(0).getSubLabNameCode());
            if (this.rateMap.containsKey(s)) {
                r.setBaseRate(this.rateMap.get(s));
            } else {
                System.out.println("Tarcode doesn't find");
                r.setBaseRate(list.get(0).getBaseRate());
            }
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

    private void getTarif() {
        Connection conn = connectionPool.getConnection();
        try (PreparedStatement pre = conn.prepareStatement(tarifSql())) {
            pre.setString(1, reportInfoClass.getStartDate().format(DateTimeFormatter.ofPattern("yyyy-MM-dd 00:00:00")));
            pre.setString(2, reportInfoClass.getEndDate().format(DateTimeFormatter.ofPattern("yyyy-MM-dd 23:59:59")));
            ResultSet set =  pre.executeQuery();
            while (set.next()) {
                this.rateMap.put(getValue(set, "tarcode"), getValue(set, "av"));
            }
        } catch (SQLException e) {
            System.out.println("Error while getting data from db");
        } finally {
            connectionPool.returnConnection(conn);
        }

        this.rateMap.forEach((k, v) -> {
            System.out.println("Tarif value -> " + k + " => " + v);
        });
    }

    private String tarifSql() {
        return "select\n" +
                "    tarcode,\n" +
                "    format(av_r, 2) av \n" +
                "from (\n" +
                "         select tarcode,\n" +
                "                avg(base_rate) av_r\n" +
                "         from finished_work\n" +
                "         where dbeg between ? and ? \n" +
                "           and stateid in (1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20, 21, 22, 23)\n" +
                "           and tarcode != '08120012'\n" +
                "         group by tarcode\n" +
                ") t";
    }

    private void getCoeficient() {
        Connection conn = connectionPool.getConnection();
        try (PreparedStatement pre = conn.prepareStatement(coefSql())) {
            pre.setString(1, reportInfoClass.getStartDate().format(DateTimeFormatter.ofPattern("yyyy-MM-dd 00:00:00")));
            pre.setString(2, reportInfoClass.getEndDate().format(DateTimeFormatter.ofPattern("yyyy-MM-dd 23:59:59")));
            ResultSet set =  pre.executeQuery();
            while (set.next()) {
                this.coeficientMap.put(getValue(set, "tarcode"), getValue(set, "av"));
            }
        } catch (SQLException e) {
            System.out.println("Error while getting data from db");
        } finally {
            connectionPool.returnConnection(conn);
        }
    }

    private String coefSql() {
        return "select\n" +
                "    tarcode,\n" +
                "    format(av_r, 2) av \n" +
                "from (\n" +
                "         select tarcode,\n" +
                "                avg(coeficient) av_r\n" +
                "         from finished_work\n" +
                "         where dbeg between ? and ? \n" +
                "           and stateid in (1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20, 21, 22, 23)\n" +
                "           and tarcode != '08120012'\n" +
                "         group by tarcode\n" +
                ") t";
    }

}
