package kz.report.dev.services;

import kz.report.dev.reportpackage.reportmodels.covid.CovidDataClass;
import kz.report.dev.reportpackage.reportmodels.covid.CovidInfoClass;
import kz.report.dev.reportpackage.reportmodels.covid.CovidMonthData;
import kz.report.dev.utils.dbutils.ConnectionPool;
import kz.report.dev.utils.dbutils.ConnectionPoolImpl;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class CovidDbService {

    private CovidInfoClass covidInfoClass;
    private List<CovidDataClass> covidDataClassList;
    private List<CovidMonthData> covidMonthDataList;

    private final ConnectionPool connectionPool = ConnectionPoolImpl.getInstance(20);

    public CovidDbService(CovidInfoClass covidInfoClass) {
        this.covidInfoClass = covidInfoClass;
        this.covidDataClassList = new ArrayList<>();
        this.covidMonthDataList = new ArrayList<>();
    }

    public List<CovidDataClass> getData() {
        getTarif();
        genCovidData();

        return this.covidDataClassList;
    }

    private void getTarif() {
        Connection con = connectionPool.getConnection();
        try (PreparedStatement preparedStatement = con.prepareStatement(tarifSql())) {
            LocalDate dbeg = covidInfoClass.getStartDate();
            dbeg = dbeg.withDayOfMonth(1);
            preparedStatement.setString(1, dbeg.format(DateTimeFormatter.ofPattern("yyyy-MM-dd 00:00:00")));
            preparedStatement.setString(2, covidInfoClass.getEndDate().format(DateTimeFormatter.ofPattern("yyyy-MM-dd 23:59:59")));
            ResultSet set = preparedStatement.executeQuery();
            while (set.next()) {
                CovidMonthData covidMonthData = new CovidMonthData();
                covidMonthData.setPrice(set.getDouble("price"));
                covidMonthData.setStrDate(set.getString("strDate"));
                covidMonthData.setDate(set.getDate("date").toLocalDate());
                this.covidMonthDataList.add(covidMonthData);
            }
        } catch (SQLException e) {
            System.out.println("Документы не получены");
        } finally {
            connectionPool.returnConnection(con);
        }
    }

    private void genCovidData() {
        if (this.covidMonthDataList.isEmpty()) {
            return;
        }

        if (this.covidMonthDataList.size() == 1) {
            int serviceCount = getServiceCount(covidInfoClass.getStartDate().format(DateTimeFormatter.ofPattern("yyyy-MM-dd 00:00:00")), covidInfoClass.getEndDate().format(DateTimeFormatter.ofPattern("yyyy-MM-dd 23:59:59")));
            CovidDataClass covidDataClass = new CovidDataClass();
            covidDataClass.setServiceName("Определение  РНК вирус COVID-19 из биологического материала методом полимеразной цепной реакций");
            covidDataClass.setAmount(serviceCount);
            covidDataClass.setPrice(this.covidMonthDataList.get(0).getPrice());
            covidDataClass.setSum(serviceCount * covidDataClass.getPrice());
            this.covidDataClassList.add(covidDataClass);
        } else {
            for (int i = 0; i < this.covidMonthDataList.size(); i++) {
                int serviceCount;
                if (i == 0) {
                    String dbeg = covidInfoClass.getStartDate().format(DateTimeFormatter.ofPattern("yyyy-MM-dd 00:00:00"));
                    String dend = this.covidMonthDataList.get(1).getDate().minusDays(1).format(DateTimeFormatter.ofPattern("yyyy-MM-dd 23:59:59"));
                    serviceCount = getServiceCount(dbeg, dend);
                } else {
                    String dbeg = this.covidMonthDataList.get(i).getDate().format(DateTimeFormatter.ofPattern("yyyy-MM-dd 00:00:00"));
                    String dend;
                    if (i+1 == this.covidMonthDataList.size()) {
                        dend = this.covidInfoClass.getEndDate().format(DateTimeFormatter.ofPattern("yyyy-MM-dd 23:59:59"));
                    } else {
                        dend = this.covidMonthDataList.get(i+1).getDate().minusDays(1).format(DateTimeFormatter.ofPattern("yyyy-MM-dd 23:59:59"));
                    }
                    serviceCount = getServiceCount(dbeg, dend);
                }
                CovidDataClass covidDataClass = new CovidDataClass();
                covidDataClass.setServiceName("Определение  РНК вирус COVID-19 из биологического материала методом полимеразной цепной реакций");
                covidDataClass.setAmount(serviceCount);
                covidDataClass.setPrice(this.covidMonthDataList.get(i).getPrice());
                covidDataClass.setSum(serviceCount * covidDataClass.getPrice());
                this.covidDataClassList.add(covidDataClass);
            }
        }
    }

    private int getServiceCount(String dbeg, String dend) {
        Connection con = connectionPool.getConnection();
        try (PreparedStatement preparedStatement = con.prepareStatement(docs())) {
            preparedStatement.setString(1, dbeg);
            preparedStatement.setString(2, dend);
            ResultSet set = preparedStatement.executeQuery();
            int serviceCount = 0;
            while (set.next()) {
                serviceCount = set.getInt("sum");
            }
            return serviceCount;
        } catch (SQLException e) {
            System.out.println("Документы не получены");
        } finally {
            connectionPool.returnConnection(con);
        }
        return -1;
    }

    private String tarifSql() {
        return "select\n" +
                "    aasf.cmp_data price,\n" +
                "    asf.cmp_data strDate,\n" +
                "    asf.cmp_key date\n" +
                "from registries\n" +
                "left join registry_documents rd on registries.registryID = rd.registryID\n" +
                "left join asf_data_index aasf on aasf.uuid = rd.asfDataID and aasf.cmp_id = 'tarificator_research_code'\n" +
                "left join asf_data_index asf on asf.uuid = rd.asfDataID and asf.cmp_id = 'mounth'\n" +
                "where code = 'tarif_dlya_issledovaniya_08120012'\n" +
                "and asf.cmp_key between ? and ?";
    }

    private String docs() {
        String stateFilter = "";
        String regionFilter = "";
        if (this.covidInfoClass.getStateDictId() != 0) {
            stateFilter = "and stateid = " + this.covidInfoClass.getStateDictId();
        }
        if (this.covidInfoClass.getRegionDictId() != 0) {
            regionFilter = "and regionid = " + this.covidInfoClass.getRegionDictId();
        }

        return "select sum(seramount) sum from finished_work where tarcode = '08120012' and dbeg between ? and ? " + stateFilter + " " + regionFilter;
    }


}
