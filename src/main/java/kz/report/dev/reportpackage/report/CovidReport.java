package kz.report.dev.reportpackage.report;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import kz.report.dev.reportpackage.annotation.Report;
import kz.report.dev.reportpackage.interfaces.IReport;
import kz.report.dev.reportpackage.reportmodels.covid.CovidDataClass;
import kz.report.dev.reportpackage.reportmodels.covid.CovidInfoClass;
import kz.report.dev.services.CovidDbService;
import kz.report.dev.utils.Converter;
import kz.report.dev.utils.ExcelUtils.FontBuilder;
import kz.report.dev.utils.ExcelUtils.StyleBuilder;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.text.DecimalFormat;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Report(reportName = "covid")
public class CovidReport implements IReport {

    private JsonNode jsonNode;
    private DecimalFormat decimalFormat = new DecimalFormat("###.##");
    private List<CovidDataClass> covidDataClasses;
    private CovidInfoClass covidInfoClass;


    public CovidReport(String param) throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        jsonNode = mapper.readTree(param);
    }

    @Override
    public byte[] genReport() throws Exception {

        this.covidInfoClass = new CovidInfoClass(jsonNode);
        this.covidDataClasses = new CovidDbService(covidInfoClass).getData();


        Workbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet();
        sheet.setColumnWidth(1, 2000);
        sheet.setColumnWidth(2, 15000);
        sheet.setColumnWidth(3, 6000);
        sheet.setColumnWidth(4, 6000);
        sheet.setColumnWidth(5, 6000);
        int indexRow = 0;

        indexRow = genHeader(workbook, sheet, indexRow);
        indexRow = genBody(workbook, sheet, indexRow);
        genFooter(workbook, sheet, indexRow);

        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        sheet.getPrintSetup().setLandscape(true);
        workbook.write(bos);
        return bos.toByteArray();
    }

    private int genHeader(Workbook workbook, Sheet sheet, int rowPosition) {
        Font font;
        CellStyle style;


        Row row = sheet.createRow(rowPosition);

        font = new FontBuilder(workbook)
                .withFontName("Times New Roman")
                .withFontHeight((short) 12)
                .setItalic()
                .build();

        style = new StyleBuilder(workbook)
                .withFont(font)
                .withWrappedText()
                .withAlignment(HorizontalAlignment.RIGHT)
                .withVerticalAlignment(VerticalAlignment.CENTER)
                .build();

        Cell cell = row.createCell(0);

        cell.setCellStyle(style);
        cell.setCellValue("Приложение к Договору № 32 от 30 июня 2020г.");
        sheet.addMergedRegion(new CellRangeAddress(rowPosition, rowPosition, 0, 5));

        rowPosition++;

        row = sheet.createRow(rowPosition);
        cell = row.createCell(0);

        font = new FontBuilder(workbook)
                .from(font)
                .resetItalic()
                .build();
        style = new StyleBuilder(workbook)
                .from(style)
                .withFont(font)
                .withAlignment(HorizontalAlignment.CENTER)
                .build();

        cell.setCellStyle(style);
        cell.setCellValue("Счет реестр");
        sheet.addMergedRegion(new CellRangeAddress(rowPosition, rowPosition, 0, 6));

        rowPosition++;

        row = sheet.createRow(rowPosition);
        cell = row.createCell(0);
        cell.setCellStyle(style);
        cell.setCellValue("за оказанные услуги, на выявление РНК вируса COVID-19 из биологического материала методом полимеразной цепной реакций");
        sheet.addMergedRegion(new CellRangeAddress(rowPosition, rowPosition, 0, 6));

        rowPosition++;

        row = sheet.createRow(rowPosition);
        cell = row.createCell(0);
        cell.setCellStyle(style);
        cell.setCellValue("отчетный период с " + this.covidInfoClass.getStartDate().format(DateTimeFormatter.ofPattern("dd.MM.yyyy")) + " по " + this.covidInfoClass.getEndDate().format(DateTimeFormatter.ofPattern("dd.MM.yyyy")));
        sheet.addMergedRegion(new CellRangeAddress(rowPosition, rowPosition, 0, 6));

        return rowPosition + 2;
    }

    private int genBody(Workbook workbook, Sheet sheet, int rowPosition) {
        Font font;
        CellStyle style;

        Row row = sheet.createRow(rowPosition);

        font = new FontBuilder(workbook)
                .withFontName("Times New Roman")
                .withFontHeight((short) 12)
                .setBold()
                .build();
        style = new StyleBuilder(workbook)
                .withFont(font)
                .withAlignment(HorizontalAlignment.CENTER)
                .withVerticalAlignment(VerticalAlignment.CENTER)
                .withWrappedText()
                .withTopBorder(BorderStyle.THIN)
                .withLeftBorder(BorderStyle.THIN)
                .withRightBorder(BorderStyle.THIN)
                .build();

        Cell cell = row.createCell(1);
        cell.setCellStyle(style);
        cell.setCellValue("№ лота");

        cell = row.createCell(2);
        cell.setCellStyle(style);
        cell.setCellValue("Наименование услуг");

        cell = row.createCell(3);
        cell.setCellStyle(style);
        cell.setCellValue("Количество, объем");

        cell = row.createCell(4);
        cell.setCellStyle(style);
        cell.setCellValue("Цена за ед., тенге");

        cell = row.createCell(5);
        cell.setCellStyle(style);
        cell.setCellValue("Сумма, тенге");

        rowPosition++;

        row = sheet.createRow(rowPosition);

        cell = row.createCell(1);
        cell.setCellStyle(style);
        cell.setCellValue("1");

        cell = row.createCell(2);
        cell.setCellStyle(style);
        cell.setCellValue("2");

        cell = row.createCell(3);
        cell.setCellStyle(style);
        cell.setCellValue("3");

        cell = row.createCell(4);
        cell.setCellStyle(style);
        cell.setCellValue("4");

        cell = row.createCell(5);
        cell.setCellStyle(style);
        cell.setCellValue("5");

        font = new FontBuilder(workbook)
                .from(font)
                .resetBold()
                .build();
        style = new StyleBuilder(workbook)
                .from(style)
                .withFont(font)
                .withBottomBorder(BorderStyle.THIN)
                .build();

        if (this.covidDataClasses == null || this.covidDataClasses.isEmpty()) {
            return rowPosition +2;
        } else {
            for (int i = 0; i < this.covidDataClasses.size(); i++) {
                CovidDataClass covidDataClass = this.covidDataClasses.get(i);
                if (i+1 == this.covidDataClasses.size()) {
                    rowPosition++;
                    row = sheet.createRow(rowPosition);
                    row.setHeightInPoints(30);

                    cell = row.createCell(1);
                    cell.setCellStyle(style);
                    cell.setCellValue(""+ (i+1));

                    cell = row.createCell(2);
                    cell.setCellStyle(style);
                    cell.setCellValue(covidDataClass.getServiceName());

                    cell = row.createCell(3);
                    cell.setCellStyle(style);
                    cell.setCellValue(covidDataClass.getAmount());

                    cell = row.createCell(4);
                    cell.setCellStyle(style);
                    cell.setCellValue(decimalFormat.format(covidDataClass.getPrice()));

                    cell = row.createCell(5);
                    cell.setCellStyle(style);
                    cell.setCellValue(decimalFormat.format(covidDataClass.getSum()));

                } else {
                    rowPosition++;

                    row = sheet.createRow(rowPosition);
                    row.setHeightInPoints(30);

                    cell = row.createCell(1);
                    cell.setCellStyle(style);
                    cell.setCellValue(""+(i+1));

                    cell = row.createCell(2);
                    cell.setCellStyle(style);
                    cell.setCellValue(covidDataClass.getServiceName());

                    cell = row.createCell(3);
                    cell.setCellStyle(style);
                    cell.setCellValue(covidDataClass.getAmount());

                    cell = row.createCell(4);
                    cell.setCellStyle(style);
                    cell.setCellValue(decimalFormat.format(covidDataClass.getPrice()));

                    cell = row.createCell(5);
                    cell.setCellStyle(style);
                    cell.setCellValue(decimalFormat.format(covidDataClass.getSum()));
                }
            }
        }


        return rowPosition + 2;
    }

    private void genFooter(Workbook workbook, Sheet sheet, int rowPosition) {
        Font font = new FontBuilder(workbook)
                .withFontName("Times New Roman")
                .withFontHeight((short) 12)
                .build();
        CellStyle style = new StyleBuilder(workbook)
                .withFont(font)
                .withWrappedText()
                .withVerticalAlignment(VerticalAlignment.CENTER)
                .withAlignment(HorizontalAlignment.LEFT)
                .build();

        long serviceCount = 0;
        double sumCount = 0.0;
        for (CovidDataClass covidDataClass : this.covidDataClasses) {
            serviceCount = serviceCount + covidDataClass.getAmount();
            sumCount = sumCount + covidDataClass.getSum();
        }

        Row row = sheet.createRow(rowPosition);
        Cell cell = row.createCell(1);
        cell.setCellStyle(style);
        cell.setCellValue("Итого (лабораторные исследования): " + serviceCount);
        sheet.addMergedRegion(new CellRangeAddress(rowPosition, rowPosition, 1, 8));

        rowPosition++;
        rowPosition++;



        row = sheet.createRow(rowPosition);
        cell = row.createCell(1);
        cell.setCellStyle(style);
        cell.setCellValue("Сумма прописью: " + Converter.convert(decimalFormat.format(sumCount)));
        sheet.addMergedRegion(new CellRangeAddress(rowPosition, rowPosition, 1, 8));

        rowPosition++;
        rowPosition++;

        font = new FontBuilder(workbook)
                .from(font)
                .setBold()
                .build();

        style = new StyleBuilder(workbook)
                .from(style)
                .withFont(font)
                .build();

        row = sheet.createRow(rowPosition);
        cell = row.createCell(1);
        cell.setCellStyle(style);
        cell.setCellValue("Заказчик:");
        sheet.addMergedRegion(new CellRangeAddress(rowPosition, rowPosition, 1, 2));

        cell = row.createCell(3);
        cell.setCellStyle(style);
        cell.setCellValue("Поставщик:");
        sheet.addMergedRegion(new CellRangeAddress(rowPosition, rowPosition, 3, 5));

        rowPosition++;
        rowPosition++;

        font = new FontBuilder(workbook)
                .from(font)
                .resetBold()
                .build();

        style = new StyleBuilder(workbook)
                .from(style)
                .withFont(font)
                .build();

        row = sheet.createRow(rowPosition);
        cell = row.createCell(1);
        cell.setCellStyle(style);
        cell.setCellValue("Департамент «Комитет контроля качества и");
        sheet.addMergedRegion(new CellRangeAddress(rowPosition, rowPosition, 1, 2));

        cell = row.createCell(3);
        cell.setCellStyle(style);
        cell.setCellValue(((this.covidInfoClass.getStateName() == null || this.covidInfoClass.getStateName().isEmpty()) ? "" : this.covidInfoClass.getStateName()) + " " + ((this.covidInfoClass.getRegionName() == null || this.covidInfoClass.getRegionName().isEmpty()) ? "" : this.covidInfoClass.getRegionName()));
        sheet.addMergedRegion(new CellRangeAddress(rowPosition, rowPosition+1, 3, 5));

        rowPosition++;

        row = sheet.createRow(rowPosition);
        cell = row.createCell(1);
        cell.setCellStyle(style);
        cell.setCellValue("безопасности товаров и услуг Министерства");
        sheet.addMergedRegion(new CellRangeAddress(rowPosition, rowPosition, 1, 2));

        rowPosition++;

        row = sheet.createRow(rowPosition);
        cell = row.createCell(1);
        cell.setCellStyle(style);
        cell.setCellValue("здравоохранения Республики Казахстан»");
        sheet.addMergedRegion(new CellRangeAddress(rowPosition, rowPosition, 1, 2));

        cell = row.createCell(3);
        cell.setCellStyle(style);
        cell.setCellValue("Директор                        ________________________________________");
        sheet.addMergedRegion(new CellRangeAddress(rowPosition, rowPosition, 3, 5));

        rowPosition++;

        style = new StyleBuilder(workbook)
                .from(style)
                .withAlignment(HorizontalAlignment.RIGHT)
                .build();

        row = sheet.createRow(rowPosition);
        cell = row.createCell(4);
        cell.setCellStyle(style);
        cell.setCellValue("Подпись");

        cell = row.createCell(5);
        cell.setCellStyle(style);
        cell.setCellValue("ФИО");

        rowPosition++;

        style = new StyleBuilder(workbook)
                .from(style)
                .withFont(font)
                .withAlignment(HorizontalAlignment.LEFT)
                .build();


        row = sheet.createRow(rowPosition);
        cell = row.createCell(1);
        cell.setCellStyle(style);
        cell.setCellValue("Руководитель      __________________________________________");
        sheet.addMergedRegion(new CellRangeAddress(rowPosition, rowPosition, 1, 2));

        cell = row.createCell(3);
        cell.setCellStyle(style);
        cell.setCellValue("Главный бухгалтер        ________________________________________");
        sheet.addMergedRegion(new CellRangeAddress(rowPosition, rowPosition, 3, 5));

        rowPosition++;

        style = new StyleBuilder(workbook)
                .from(style)
                .withAlignment(HorizontalAlignment.LEFT)
                .build();


        row = sheet.createRow(rowPosition);
        cell = row.createCell(2);
        cell.setCellStyle(style);
        cell.setCellValue("         Подпись                           ФИО");

        style = new StyleBuilder(workbook)
                .from(style)
                .withAlignment(HorizontalAlignment.RIGHT)
                .build();


        cell = row.createCell(4);
        cell.setCellStyle(style);
        cell.setCellValue("Подпись");

        cell = row.createCell(5);
        cell.setCellStyle(style);
        cell.setCellValue("ФИО");

    }

    @Override
    public String getSql() {
        return null;
    }
}
