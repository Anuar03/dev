package kz.report.dev.services.reload.reloadimpl;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import kz.arta.synergy.forms.common.object.ASFData;
import kz.arta.synergy.forms.common.object.ASFDataWrapperExt;
import kz.arta.synergy.forms.common.util.rest.operations.AsfDataApi;
import kz.report.dev.reportpackage.reportmodels.reestrfinished.ReestrModel;
import kz.report.dev.services.reload.Reload;
import kz.report.dev.utils.dbutils.ConnectionPool;
import kz.report.dev.utils.dbutils.ConnectionPoolImpl;
import kz.report.dev.utils.httputils.HttpSynergyUtils;
import kz.report.dev.utils.numberutils.NumberUtils;

import javax.xml.bind.DatatypeConverter;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.sql.*;
import java.sql.Date;
import java.util.*;

import static org.apache.commons.codec.binary.Base64.encodeBase64String;

public class ReloadFromSynergy implements Reload {

    private String documentsInJsonForm;
    private final ConnectionPool connectionPool = ConnectionPoolImpl.getInstance(20);
    private final ObjectMapper objectMapper = new ObjectMapper();;

    public ReloadFromSynergy() {

        try {
            // Первый вызов для получения общего количетсва документов в реестре, далее используется перегруженный метод getDocumentsInJsonForm(int) для получения доков по страницам
            documentsInJsonForm = getDocumentsInJsonForm();
        } catch (Exception e) {
            System.out.println("!-----ОШИБКА ПРИ ЧТЕНИИ ДАННЫХ-----!");
        }
    }

    @Override
    public void reload() throws Exception {
        insertDataToDataBase();
    }

    private String getDocumentsInJsonForm() throws Exception {
//        URL url = new URL("http://test-lis.nce.kz/Synergy/rest/api/registry/data_ext?registryCode=reestr_registratsiya_ob_ekta&countInPart=1&field=status&condition=EQUALS&key=7");
        URL url = new URL("http://127.0.0.1/Synergy/rest/api/registry/data_ext?registryCode=reestr_registratsiya_ob_ekta&countInPart=1&field=status&condition=EQUALS&key=7");
        return getDocumentsFromInputStream(getHttpConnection(url));
    }
    private String getDocumentsInJsonForm(int pageNumber) throws Exception {
        String urlString = String.format("http://127.0.0.1/Synergy/rest/api/registry/data_ext?registryCode=reestr_registratsiya_ob_ekta&countInPart=50000&&pageNumber=%d&field=status&condition=EQUALS&key=7", pageNumber);
//        String urlString = String.format("http://test-lis.nce.kz/Synergy/rest/api/registry/data_ext?registryCode=reestr_registratsiya_ob_ekta&countInPart=50000&&pageNumber=%d&field=status&condition=EQUALS&key=7", pageNumber);
        URL url = new URL(urlString);
        return getDocumentsFromInputStream(getHttpConnection(url));
    }

    private InputStream getHttpConnection(URL url) throws IOException {
        HttpSynergyUtils utils = new HttpSynergyUtils("admincrm", Base64.getEncoder().encodeToString("Adm1nCRM".getBytes()));
        String login = "admincrm";
        String psw = "Adm1nCRM";
        String encoded = DatatypeConverter.printBase64Binary((login + ":" + psw).getBytes());
        return utils.openGetConnection(url, "Basic " + encoded).getInputStream();
    }

    private String getDocumentsFromInputStream(InputStream stream) {
        Scanner scanner = new Scanner(stream).useDelimiter("\\A");
        return  scanner.hasNext() ? scanner.next() : "";
    }

    private void insertDataToDataBase() throws Exception {
        JsonNode node = readJsonString(documentsInJsonForm);
        int recordsCount = node.get("recordsCount").asInt();
        long iter = Math.round(Math.ceil(recordsCount/50000));
        for (int i = 0; i < iter; i++) {
            String docsFromSynergy = getDocumentsInJsonForm(i);
            JsonNode innerNode = readJsonString(docsFromSynergy);
            for (Iterator<JsonNode> iterator = innerNode.get("result").elements(); iterator.hasNext();) {
                JsonNode arrNode = iterator.next();
                getDataFromSynergyAndInsertToDb(arrNode);
            }
        }
    }

    private JsonNode readJsonString(String jsonInString) throws Exception{
        return objectMapper.readTree(jsonInString);
    }

    private void getDataFromSynergyAndInsertToDb(JsonNode node) throws Exception {
        ASFDataWrapperExt docData = getFromSynergy(node.get("dataUUID").asText());
        int count = rowsCountInDocument(docData.getData("dynamic_table").getData());
        if (count == -1) return;
        insertToDb(fillByLevels(docData, node, count));
    }

    private ASFDataWrapperExt getFromSynergy(String uuid) throws Exception {
        if (Objects.isNull(uuid) || "".equalsIgnoreCase(uuid)) return null;
        AsfDataApi asfDataApi = new AsfDataApi("http://test-lis.nce.kz/Synergy", "Basic " + encodeBase64String(("admincrm" + ":" + "Adm1nCRM").getBytes()));
//        AsfDataApi asfDataApi = new AsfDataApi("http://127.0.0.1/Synergy", "Basic " + encodeBase64String(("admincrm" + ":" + "Adm1nCRM").getBytes()));
        return asfDataApi.getAsfData(uuid);
    }

    private int rowsCountInDocument(List<ASFData.Data> list) {
        if (list == null) return  -1;
        if (list.size() > 13) {
            String lastId = list.get(list.size() - 1).getId();
            return Integer.parseInt(lastId.substring(lastId.length() - 1));
        }
        return -1;
    }

    private List<ReestrModel> fillByLevels(ASFDataWrapperExt asfData, JsonNode node, int count) {
        List<ASFData.Data> asfDataList = asfData.getData("dynamic_table").getData();
        String stringSate = asfData.getData("research_end_date").getKey();
        if (stringSate == null || stringSate.equalsIgnoreCase("")) {
            return new ArrayList<>();
        }
        Date date = Date.valueOf(stringSate.split(" ")[0]);
        String stateId = asfData.getData("personal_card_name_company_ru1").getKey();
        String regionId = asfData.getData("personal_card_name_department_ru1").getKey();
        List<ReestrModel> asfList = new ArrayList<>();
        for (int i = 1; i <= count; i++) {
            ReestrModel model = fillLevel(asfDataList, i);
            model.setDocumentID(node.get("documentID").asText());
            model.setSubLabName(asfData.getData("list_department").getValue());
            model.setSubLabNameCode(asfData.getData("list_department").getKey());
            model.setDate(date);
            model.setStateId(stateId);
            model.setRegionId(regionId);
            asfList.add(model);
        }
        return asfList;
    }

    private ReestrModel fillLevel(List<ASFData.Data> asfList, int level) {
        if (asfList == null) return null;
        ReestrModel reestrModel = new ReestrModel();
        for (ASFData.Data com : asfList) {
            if (com.getId().equalsIgnoreCase("research_register-b" + level)) {
                reestrModel.setResName(com.getValue());
            }
            else if (com.getId().equalsIgnoreCase("lab_name-b" + level)) {
                reestrModel.setLabName(com.getValue());
                reestrModel.setLabNameCode(com.getKey());
            }
            else if (com.getId().equalsIgnoreCase("research_group-b" + level)) {
                reestrModel.setResGroup(com.getValue());
                reestrModel.setResGroupCode(com.getKey());
            }
            else if (com.getId().equalsIgnoreCase("research_subgroup-b" + level)) {
                reestrModel.setSubResGroup(com.getValue());
                reestrModel.setSubResGroupCode(com.getKey());
            }
            else if (com.getId().equalsIgnoreCase("research_method-b" + level)) {
                reestrModel.setResMethod(com.getValue());
            }
            else if (com.getId().equalsIgnoreCase("service_unit-b" + level)) {
                reestrModel.setSerUnit(com.getValue());
            }
            else if (com.getId().equalsIgnoreCase("cost_coefficient-b" + level)) {
                reestrModel.setCoeficient(com.getValue());
            }
            else if (com.getId().equalsIgnoreCase("tarificator_research_code-b" + level)) {
                reestrModel.setTarCode(com.getValue());
            }
            else if (com.getId().equalsIgnoreCase("base_rate-b" + level)) {
                reestrModel.setBaseRate(com.getValue());
            }
            else if (com.getId().equalsIgnoreCase("services_amount-b" + level)) {
                reestrModel.setSerAmount(com.getValue());
            }
            else if (com.getId().equalsIgnoreCase("mismatch_services_amount-b" + level)) {
                reestrModel.setmSerAmount(com.getValue());
            }
            else if (com.getId().equalsIgnoreCase("base_rate-b" + level)) {
                reestrModel.setBaseRate(com.getValue());
            }
        }
        return reestrModel;
    }

    private void insertToDb(List<ReestrModel> reestrModelList) {
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
                System.out.println("Здесь");
                pre.close();
                conn.rollback();

            } catch (SQLException ee) {
                System.out.println("Что то пошло не так");
            }
        } finally {
            connectionPool.returnConnection(conn);
        }
    }

    private String insertSql() {
        return "INSERT INTO finished_work(documentID, dbeg, stateid, regionid, resname, labname, labcode, resgroup, resgroupCode, subresgroup, subresgroupCode, resmethod, serunit, coeficient, tarcode, base_rate, seramount, mseramount, subLabName, subLabNameCode) VALUES(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
    }
}