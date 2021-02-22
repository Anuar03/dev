package kz.report.dev.services.reload.reloadimpl;

import kz.report.dev.reportpackage.reportmodels.reestrfinished.ReestrModel;
import kz.report.dev.services.reload.Reload;
import kz.report.dev.utils.dbutils.ConnectionPool;
import kz.report.dev.utils.dbutils.ConnectionPoolImpl;
import kz.report.dev.utils.numberutils.NumberUtils;

import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class ReloadFromDbLast implements Reload {

    private List<String> documentIds;
    private Map<String, Integer> rowsCount;
    private List<ReestrModel> reestrModels;
    private ConnectionPool connectionPool = ConnectionPoolImpl.getInstance(20);
    private String dbeg;
    private String dend;
    private boolean isPeriod;


    public ReloadFromDbLast() {
        this.documentIds = new ArrayList<>();
        this.rowsCount = new HashMap<>();
        this.reestrModels = new ArrayList<>();
    }

    public ReloadFromDbLast(String dbeg, String dend) {
        this.documentIds = new ArrayList<>();
        this.rowsCount = new HashMap<>();
        this.reestrModels = new ArrayList<>();
        this.dbeg = dbeg;
        this.dend = dend;
        this.isPeriod = true;
        System.out.println("Reload period " + dbeg + " - " + dend);
    }

    public void reload() {
        process();
    }

    private void process() {
        try {
            if (isPeriod) {
                getPeriodDocumentIdList();
            } else {
                getDocumentIdList();
            }
            System.out.println("Documents count = " + documentIds.size());
            if (!documentIds.isEmpty()) {
                long iter = Math.round(Math.ceil(documentIds.size()/1000));

                int b = (int) iter * 1000;
                getDynCount(b);
                if (rowsCount.size() > 0) {
                    if (isPeriod) {
                        deleteExistingDocuments();
                    }
                    getDynData();
                    insertToDb(reestrModels);
                }
                rowsCount.clear();
                reestrModels.clear();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private String getDocumentIdList() {
        Connection con = connectionPool.getConnection();
        try ( PreparedStatement preparedStatement = con.prepareStatement(sqlDocumentIdList())) {
            ResultSet set = preparedStatement.executeQuery();
            while (set.next()) {
                documentIds.add(set.getString("documentID"));
            }
        } catch (SQLException e) {
            System.out.println("Documents were not get");
        } finally {
            connectionPool.returnConnection(con);
        }

        return "";
    }

    private String sqlDocumentIdList() {

        return "select rd.documentID from registries " +
                "left join registry_documents rd on registries.registryID = rd.registryID " +
                "left join asf_data_index status on status.uuid = rd.asfDataID and status.cmp_id = 'status' " +
                "join asf_data_index state on state.uuid = rd.asfDataID and state.cmp_id = 'personal_card_name_company_ru1' and state.cmp_key in (2,3,4,5,6,7,8,9,10,11,12,13,14,15,16,17,18,19,20,21) " +
                "where code = 'reestr_registratsiya_ob_ekta' and rd.deleted is null and status.cmp_key = 7";
    }

    private String getPeriodDocumentIdList() throws Exception {
        Connection con = connectionPool.getConnection();
        try ( PreparedStatement preparedStatement = con.prepareStatement(sqlPeriodDocumentIdList())) {
            preparedStatement.setString(1, this.dbeg);
            preparedStatement.setString(2, this.dend);
            ResultSet set = preparedStatement.executeQuery();
            while (set.next()) {
                documentIds.add(set.getString("documentID"));
            }
        } catch (SQLException e) {
            System.out.println("Документы не получены");
        } finally {
//            con.close();
            connectionPool.returnConnection(con);
        }

        return "";
    }

    private String sqlPeriodDocumentIdList() {

        return "select rd.documentID\n" +
                "from registries r\n" +
                "LEFT JOIN (\n" +
                "    SELECT\n" +
                "        registryID,\n" +
                "        documentID,\n" +
                "        asfDataId,\n" +
                "        deleted\n" +
                "    FROM registry_documents\n" +
                ") AS rd ON rd.registryID = r.registryID\n" +
                "LEFT JOIN (\n" +
                "    SELECT\n" +
                "        uuid,\n" +
                "        cmp_data,\n" +
                "        cmp_key,\n" +
                "        cmp_id\n" +
                "    FROM asf_data_index\n" +
                "    WHERE cmp_id = 'research_end_date'\n" +
                ") a ON a.uuid = rd.asfDataId AND a.cmp_id = 'research_end_date'\n" +
                "LEFT JOIN (\n" +
                "    SELECT\n" +
                "        uuid,\n" +
                "        cmp_data,\n" +
                "        cmp_key,\n" +
                "        cmp_id\n" +
                "    FROM asf_data_index\n" +
                "    WHERE cmp_id = 'status'\n" +
                ") b ON b.uuid = rd.asfDataId AND b.cmp_id = 'status'\n" +
                "LEFT JOIN (\n" +
                "    SELECT\n" +
                "        uuid,\n" +
                "        cmp_data,\n" +
                "        cmp_key,\n" +
                "        cmp_id\n" +
                "    FROM asf_data_index\n" +
                "    WHERE cmp_id = 'personal_card_name_company_ru1'\n" +
                "    AND cmp_key in (2,3,4,5,6,7,8,9,10,11,12,13,14,15,16,17,18, 19,20,21,22,23)\n" +
                ") c ON c.uuid = rd.asfDataId AND c.cmp_id = 'personal_card_name_company_ru1'\n" +
                "\n" +
                "WHERE r.code = 'reestr_registratsiya_ob_ekta'\n" +
                "AND a.cmp_key between ? and ?\n" +
                "AND rd.deleted is null\n" +
                "AND b.cmp_key = 7";
    }

    private void getDynCount(int index) throws Exception {
        Connection con = connectionPool.getConnection();
        try (PreparedStatement preparedStatement = con.prepareStatement(sqlDynCount(index))) {
            ResultSet set = preparedStatement.executeQuery();
            while (set.next()) {
                rowsCount.put(set.getString("documentID"), set.getInt("rows"));
            }
        } catch (SQLException e) {
            System.out.println("Count was not get");
        } finally {
//            con.close();
            connectionPool.returnConnection(con);
        }
    }

    private String sqlDynCount(int index) {
        System.out.println("Interval = " + index + "-" + documentIds.size() + " for period " + this.dbeg + " - " + this.dend);
        List<String> subDocumentIds;
        try {
            subDocumentIds = documentIds.subList(index, documentIds.size());
        } catch (IndexOutOfBoundsException e) {
            subDocumentIds = documentIds.subList(index, documentIds.size());
        }
        String prepareList = subDocumentIds.stream().
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

    private void deleteExistingDocuments() throws Exception {
        Connection con = connectionPool.getConnection();
        try (PreparedStatement preparedStatement = con.prepareStatement(sqlDeleteExistingDocuments())) {
            for (String documentId : rowsCount.keySet()) {
                preparedStatement.setString(1, documentId);
                int result = preparedStatement.executeUpdate();
                if (result == 0) {
                    System.out.println("Records were not deleted");
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
//            con.close();
            connectionPool.returnConnection(con);
        }
    }

    private String sqlDeleteExistingDocuments() {
        return "delete from finished_work where documentId = ?";
    }

    private void getDynData() throws Exception {
        Connection con = connectionPool.getConnection();
        try (PreparedStatement preparedStatement = con.prepareStatement(sqlDynData())) {
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
                        ReestrModel reestrModel = new ReestrModel();
                        String stringSate = getValue(set, "endDate");
                        if (stringSate == null || stringSate.equalsIgnoreCase("")) {
                            continue;
                        }
                        Date date = Date.valueOf(stringSate.split(" ")[0]);
                        reestrModel.setDate(date);
                        reestrModel.setDocumentID(entry.getKey());
                        reestrModel.setResName(getValue(set, "resname"));
                        reestrModel.setLabName(getValue(set, "labname"));
                        reestrModel.setStateId(getValue(set, "stateCode"));
                        reestrModel.setRegionId(getValue(set, "regionCode"));
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
                        reestrModels.add(reestrModel);
                    }
                }
            }
        } catch (Exception e) {
            System.out.println("Records were not get");
        } finally {
//            con.close();
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
                "    state.cmp_key stateCode, \n"+
                "    region.cmp_key regionCode, \n"+
                "    endDate.cmp_key endDate \n" +
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
                "left join asf_data_index state on state.uuid = rd.asfDataID and state.cmp_id = 'personal_card_name_company_ru1' \n" +
                "left join asf_data_index region on region.uuid = rd.asfDataID and region.cmp_id = 'personal_card_name_department_ru1' \n" +
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

    private void insertToDb(List<ReestrModel> reestrModelList) throws Exception {
        Connection conn = connectionPool.getConnection();
        PreparedStatement pre = null;

        try {
            conn.setAutoCommit(false);

            pre = conn.prepareStatement(insertSql(), Statement.RETURN_GENERATED_KEYS);

            for (ReestrModel md : reestrModelList) {
                pre.setNString(1, md.getDocumentID());
                pre.setDate(2, md.getDate());
                pre.setInt(3, NumberUtils.parseInt(md.getStateId()));
                pre.setInt(4, NumberUtils.parseInt(md.getRegionId()));
                pre.setNString(5, md.getResName());
                pre.setNString(6, md.getLabName());
                pre.setInt(7, NumberUtils.parseInt(md.getLabNameCode()));
                pre.setNString(8, md.getResGroup());
                pre.setInt(9,  NumberUtils.parseInt(md.getResGroupCode()));
                pre.setNString(10, md.getSubResGroup());
                pre.setInt(11, NumberUtils.parseInt(md.getSubResGroupCode()));
                pre.setNString(12, md.getResMethod());
                pre.setNString(13, md.getSerUnit());
                pre.setDouble(14, NumberUtils.parseDouble(md.getCoeficient()));
                pre.setNString(15, md.getTarCode());
                pre.setDouble(16, NumberUtils.parseDouble(md.getBaseRate()));
                pre.setInt(17, NumberUtils.parseInt(md.getSerAmount()));
                pre.setInt(18, NumberUtils.parseInt(md.getmSerAmount()));
                pre.setNString(19, md.getSubLabName());
                pre.setNString(20, md.getSubLabNameCode());
                int code = pre.executeUpdate();
                if (code == 0) {
                    System.out.println("Что то пошло не так");
                }
                try (ResultSet generatedKeys = pre.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        md.setDbId(generatedKeys.getString(1));
                    }
                    else {
                        throw new SQLException("Creating user failed, no ID obtained.");
                    }
                }
            }
            conn.commit();
        } catch (SQLException e) {
            try {
                System.out.println("Error while inserting");
                pre.close();
                conn.rollback();

            } catch (SQLException ee) {
                System.out.println("Error while inserting");
            }
        } finally {
//            conn.close();
            connectionPool.returnConnection(conn);
        }
    }

    private String insertSql() {
        return "INSERT INTO finished_work(documentID, dbeg, stateid, regionid, resname, labname, labcode, resgroup, resgroupCode, subresgroup, subresgroupCode, resmethod, serunit, coeficient, tarcode, base_rate, seramount, mseramount, subLabName, subLabNameCode) VALUES(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
    }

    public static <T>List<List<T>> chopIntoParts(final List<T> ls, final int iParts ) {
        final List<List<T>> lsParts = new ArrayList<List<T>>();
        final int iChunkSize = ls.size() / iParts;
        int iLeftOver = ls.size() % iParts;
        int iTake = iChunkSize;

        for( int i = 0, iT = ls.size(); i < iT; i += iTake ) {

            if( iLeftOver > 0 ) {
                iLeftOver--;

                iTake = iChunkSize + 1;
            }
            else {
                iTake = iChunkSize;
            }

            lsParts.add( new ArrayList<T>( ls.subList( i, Math.min( iT, i + iTake ) ) ) );
        }

        return lsParts;
    }

    private long getRange(int size) {
        long iter = Math.round(Math.ceil(size/1000));
        if ((size % 1000) > 0) {
            iter++;
        }
        return iter;
    }
}
