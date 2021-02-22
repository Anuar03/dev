package kz.report.dev.reportpackage.report;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import kz.report.dev.reportpackage.annotation.Report;
import kz.report.dev.reportpackage.interfaces.IReport;
import kz.report.dev.reportpackage.reportmodels.reestrfinished.LabModel;
import kz.report.dev.reportpackage.reportmodels.reestrfinished.ReestrModel;
import kz.report.dev.reportpackage.reportmodels.reestrfinished.ReestrReportInfoClass;
import kz.report.dev.reportpackage.reportmodels.reestrfinished.SubLabModel;
import kz.report.dev.services.ReestrFinishedWorkDbService;
import kz.report.dev.services.ReestrFinishedWorkDbServiceNew;
import kz.report.dev.utils.Converter;
import kz.report.dev.utils.StrUtils.StrUlits;
import kz.report.dev.utils.numberutils.NumberUtils;
import org.apache.commons.io.IOUtils;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.*;
import java.text.DecimalFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Report(reportName = "reestrFinishedWork")
public class ReestrFinishedWork implements IReport {

    private JsonNode jsonNode;

    private List<LabModel> labModelList;
    DecimalFormat decimalFormat = new DecimalFormat("###.##");


    public ReestrFinishedWork(String param) throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        jsonNode = mapper.readTree(param);
    }

    @Override
    public byte[] genReport() throws Exception {

        System.out.println("getReport");

        ReestrReportInfoClass reportInfoClass = new ReestrReportInfoClass(jsonNode);
        System.out.println("reportInfoClass");
        if (jsonNode.get("newVersion").asBoolean()) {
            labModelList = new ReestrFinishedWorkDbServiceNew(reportInfoClass).getLabModelList();
        } else {
            labModelList = new ReestrFinishedWorkDbService(reportInfoClass).getLabModelList();
        }
//        labModelList = new ReestrFinishedWorkDbServiceNew(reportInfoClass).getLabModelList();

        Workbook workbook = new XSSFWorkbook();

        Sheet sheet = workbook.createSheet();

        int rowIndex = genHeadOfTable(workbook, sheet, reportInfoClass);
        rowIndex++;

        Font font = workbook.createFont();
        font.setFontName("Calibri");
        font.setFontHeightInPoints((short) 8);
        CellStyle style1 = workbook.createCellStyle();
        style1.setVerticalAlignment(VerticalAlignment.CENTER);
        style1.setAlignment(HorizontalAlignment.LEFT);
        style1.setFont(font);
        style1.setBorderBottom(BorderStyle.MEDIUM);
        style1.setBorderTop(BorderStyle.MEDIUM);
        style1.setBorderRight(BorderStyle.MEDIUM);
        style1.setBorderLeft(BorderStyle.MEDIUM);

        Row row;
        Cell cell;
        CellStyle style;
        int rIndex = rowIndex;
        CellStyle style2 = workbook.createCellStyle();
        int serMainAmount = 0;
        double baseMainRate = 0;
        double costMain = 0;
        double mSerAmountMain = 0;
        if (!Objects.isNull(labModelList)) {

            int OserMainAmount = 0;
            double ObaseMainRate = 0;
            double OcostMain = 0;
            double OmSerAmountMain = 0;
            for (LabModel m : labModelList) {
                if (!m.isSubLabModelEmpty()) {
                    for (SubLabModel subLabModel : m.getLabModelList()) {
                        if (!subLabModel.isReestrModelsEmpty()) {
                            for (List<ReestrModel> l : subLabModel.getReestrModelList().values()) {
                                Collections.sort(l);
                            }
                        }
                    }
                }
                if (!m.isReestrModelsEmpty()) {
                    for (List<ReestrModel> l : m.getReestrModels().values()) {
                        Collections.sort(l);
                    }
                }
            }
            for (LabModel m : labModelList) {
                int serAmount = 0;
                double baseRate = 0;
                double cost = 0;
                double mSerAmount = 0;
                if (Objects.isNull(m.getLabName())) continue;
                rIndex++;
                row = sheet.createRow(rIndex);
                Font font1 = workbook.createFont();
                font1.setBold(true);
                font1.setFontName("Calibri");
                font1.setFontHeightInPoints((short) 8);
                style2.setFont(font1);
                style2.setVerticalAlignment(VerticalAlignment.CENTER);
                style2.setAlignment(HorizontalAlignment.LEFT);
                style2.setWrapText(true);
                cell = row.createCell(0);
                cell.setCellStyle(style2);
                cell.setCellValue(m.getLabName());
                sheet.addMergedRegion(new CellRangeAddress(rIndex, rIndex, 0, 7));
                if (m.getLabModelList().size() > 0) {

                    for (SubLabModel subLabModel : m.getLabModelList()) {
                        int serSubAmount = 0;
                        double baseSubRate = 0;
                        double subCost = 0;
                        double mSubSerAmount = 0;
                        rIndex++;
                        row = sheet.createRow(rIndex);
                        cell = row.createCell(0);
                        cell.setCellStyle(style2);
                        cell.setCellValue("            " + subLabModel.getSubLabName());
                        sheet.addMergedRegion(new CellRangeAddress(rIndex, rIndex, 0, 7));

                        for (Map.Entry<String, List<ReestrModel>> entry : subLabModel.getReestrModelList().entrySet()) {
                            if (entry.getValue().size() == 0) continue;
                            rIndex++;
                            int index = 1;
                            row = sheet.createRow(rIndex);
                            cell = row.createCell(0);
                            cell.setCellStyle(style2);
                            cell.setCellValue("                      " + entry.getKey());
                            sheet.addMergedRegion(new CellRangeAddress(rIndex, rIndex, 0, 7));

                            for (ReestrModel reestrModel : entry.getValue()) {
                                rIndex++;
                                row = sheet.createRow(rIndex);
                                row.setHeightInPoints(40);

                                style = workbook.createCellStyle();
                                style.setVerticalAlignment(VerticalAlignment.CENTER);
                                style.setAlignment(HorizontalAlignment.RIGHT);
                                style.setBorderBottom(BorderStyle.MEDIUM);
                                style.setBorderTop(BorderStyle.MEDIUM);
                                style.setBorderRight(BorderStyle.MEDIUM);
                                style.setBorderLeft(BorderStyle.MEDIUM);
                                style.setFont(font);

                                cell = row.createCell(0);
                                cell.setCellStyle(style);
                                cell.setCellValue(index++);

                                cell = row.createCell(1);
                                cell.setCellStyle(style);
                                cell.setCellValue(reestrModel.getTarCode());

                                cell = row.createCell(2);
                                cell.setCellStyle(style1);
                                cell.setCellValue(String.format("%s \n\\%s \n (%s)", reestrModel.getSubResGroup(), reestrModel.getResMethod(), reestrModel.getSerUnit()));


                                cell = row.createCell(3);
                                cell.setCellStyle(style);
                                cell.setCellValue(reestrModel.getCoeficient());

                                cell = row.createCell(4);
                                cell.setCellStyle(style);
                                cell.setCellValue(reestrModel.getSerAmount());
                                serAmount += NumberUtils.parseInt(reestrModel.getSerAmount());
                                serSubAmount += NumberUtils.parseInt(reestrModel.getSerAmount());


                                cell = row.createCell(5);
                                cell.setCellStyle(style);
                                cell.setCellValue(decimalFormat.format(NumberUtils.parseDouble(reestrModel.getSerAmount()) * NumberUtils.parseDouble(reestrModel.getCoeficient())));
                                baseRate += (NumberUtils.parseDouble(reestrModel.getSerAmount()) * NumberUtils.parseDouble(reestrModel.getCoeficient()));
                                baseSubRate += (NumberUtils.parseDouble(reestrModel.getSerAmount()) * NumberUtils.parseDouble(reestrModel.getCoeficient()));

                                cell = row.createCell(6);
                                cell.setCellStyle(style);
                                cell.setCellValue(decimalFormat.format((NumberUtils.parseDouble(reestrModel.getCoeficient()) * NumberUtils.parseDouble(reestrModel.getSerAmount())) * NumberUtils.parseDouble(reestrModel.getBaseRate())));
                                cost += ((NumberUtils.parseDouble(reestrModel.getCoeficient()) * NumberUtils.parseDouble(reestrModel.getSerAmount())) * NumberUtils.parseDouble(reestrModel.getBaseRate()));
                                subCost += ((NumberUtils.parseDouble(reestrModel.getCoeficient()) * NumberUtils.parseDouble(reestrModel.getSerAmount())) * NumberUtils.parseDouble(reestrModel.getBaseRate()));

                                cell = row.createCell(7);
                                cell.setCellStyle(style);
                                cell.setCellValue(reestrModel.getmSerAmount());
                                mSerAmount += NumberUtils.parseInt(reestrModel.getmSerAmount());
                                mSubSerAmount += NumberUtils.parseInt(reestrModel.getmSerAmount());
                                if (NumberUtils.parseInt(reestrModel.getLabNameCode()) == 16) {
                                    OserMainAmount += NumberUtils.parseInt(reestrModel.getSerAmount());
                                    ObaseMainRate += (NumberUtils.parseDouble(reestrModel.getSerAmount()) * NumberUtils.parseDouble(reestrModel.getCoeficient()));
                                    OcostMain += ((NumberUtils.parseDouble(reestrModel.getCoeficient()) * NumberUtils.parseDouble(reestrModel.getSerAmount())) * NumberUtils.parseDouble(reestrModel.getBaseRate()));
                                    OmSerAmountMain += NumberUtils.parseInt(reestrModel.getmSerAmount());
                                }
                            }
                        }
                        row = sheet.createRow(++rIndex);
                        cell = row.createCell(0);
                        cell.setCellStyle(style2);
                        cell.setCellValue( "            " + "Итого (" + subLabModel.getSubLabName() + ")");
                        sheet.addMergedRegion(new CellRangeAddress(rIndex, rIndex, 0, 3));

                        style = workbook.createCellStyle();
                        style.setVerticalAlignment(VerticalAlignment.CENTER);
                        style.setAlignment(HorizontalAlignment.RIGHT);
                        style.setFont(font);
                        cell = row.createCell(4);
                        cell.setCellStyle(style);
                        cell.setCellValue(serSubAmount);

                        cell = row.createCell(5);
                        cell.setCellStyle(style);
                        cell.setCellValue(baseSubRate);

                        cell = row.createCell(6);
                        cell.setCellStyle(style);
                        cell.setCellValue(decimalFormat.format(subCost));

                        cell = row.createCell(7);
                        cell.setCellStyle(style);
                        cell.setCellValue(mSubSerAmount);
                    }
                }
                if (m.getReestrModels().size() > 0) {
                    for (Map.Entry<String, List<ReestrModel>> entry : m.getReestrModels().entrySet()) {
                        if (entry.getValue().size() == 0) continue;
                        rIndex++;
                        int index = 1;
                        row = sheet.createRow(rIndex);
                        cell = row.createCell(0);
                        cell.setCellStyle(style2);
                        cell.setCellValue("            " + entry.getKey());
                        sheet.addMergedRegion(new CellRangeAddress(rIndex, rIndex, 0, 7));

                        for (ReestrModel reestrModel : entry.getValue()) {
                            rIndex++;
                            row = sheet.createRow(rIndex);
                            row.setHeightInPoints(40);

                            style = workbook.createCellStyle();
                            style.setVerticalAlignment(VerticalAlignment.CENTER);
                            style.setAlignment(HorizontalAlignment.RIGHT);
                            style.setBorderBottom(BorderStyle.MEDIUM);
                            style.setBorderTop(BorderStyle.MEDIUM);
                            style.setBorderRight(BorderStyle.MEDIUM);
                            style.setBorderLeft(BorderStyle.MEDIUM);
                            style.setFont(font);

                            cell = row.createCell(0);
                            cell.setCellStyle(style);
                            cell.setCellValue(index++);

                            cell = row.createCell(1);
                            cell.setCellStyle(style);
                            cell.setCellValue(reestrModel.getTarCode());

                            cell = row.createCell(2);
                            cell.setCellStyle(style1);
                            cell.setCellValue(String.format("%s \n\\%s \n (%s)", reestrModel.getSubResGroup(), reestrModel.getResMethod(), reestrModel.getSerUnit()));


                            cell = row.createCell(3);
                            cell.setCellStyle(style);
                            cell.setCellValue(reestrModel.getCoeficient());

                            cell = row.createCell(4);
                            cell.setCellStyle(style);
                            cell.setCellValue(reestrModel.getSerAmount());
                            serAmount += NumberUtils.parseInt(reestrModel.getSerAmount());


                            cell = row.createCell(5);
                            cell.setCellStyle(style);
                            cell.setCellValue(decimalFormat.format(NumberUtils.parseDouble(reestrModel.getSerAmount()) * NumberUtils.parseDouble(reestrModel.getCoeficient())));
                            baseRate += (NumberUtils.parseDouble(reestrModel.getSerAmount()) * NumberUtils.parseDouble(reestrModel.getCoeficient()));

                            cell = row.createCell(6);
                            cell.setCellStyle(style);
                            cell.setCellValue(decimalFormat.format((NumberUtils.parseDouble(reestrModel.getCoeficient()) * NumberUtils.parseDouble(reestrModel.getSerAmount())) * NumberUtils.parseDouble(reestrModel.getBaseRate())));
                            cost += ((NumberUtils.parseDouble(reestrModel.getCoeficient()) * NumberUtils.parseDouble(reestrModel.getSerAmount())) * NumberUtils.parseDouble(reestrModel.getBaseRate()));

                            cell = row.createCell(7);
                            cell.setCellStyle(style);
                            cell.setCellValue(reestrModel.getmSerAmount());
                            mSerAmount += NumberUtils.parseInt(reestrModel.getmSerAmount());

                            if (NumberUtils.parseInt(reestrModel.getLabNameCode()) == 16) {
                                OserMainAmount += NumberUtils.parseInt(reestrModel.getSerAmount());
                                ObaseMainRate += (NumberUtils.parseDouble(reestrModel.getSerAmount()) * NumberUtils.parseDouble(reestrModel.getCoeficient()));
                                OcostMain += ((NumberUtils.parseDouble(reestrModel.getCoeficient()) * NumberUtils.parseDouble(reestrModel.getSerAmount())) * NumberUtils.parseDouble(reestrModel.getBaseRate()));
                                OmSerAmountMain += NumberUtils.parseInt(reestrModel.getmSerAmount());
                            }
                        }
                    }
                }
                row = sheet.createRow(++rIndex);
                cell = row.createCell(0);
                cell.setCellStyle(style2);
                cell.setCellValue("Итого (" + m.getLabName() + ")");
                sheet.addMergedRegion(new CellRangeAddress(rIndex, rIndex, 0, 3));

                style = workbook.createCellStyle();
                style.setVerticalAlignment(VerticalAlignment.CENTER);
                style.setAlignment(HorizontalAlignment.RIGHT);
                style.setFont(font);
                cell = row.createCell(4);
                cell.setCellStyle(style);
                cell.setCellValue(serAmount);
                serMainAmount += serAmount;

                cell = row.createCell(5);
                cell.setCellStyle(style);
                cell.setCellValue(baseRate);
                baseMainRate += baseRate;

                cell = row.createCell(6);
                cell.setCellStyle(style);
                cell.setCellValue(decimalFormat.format(cost));
                costMain += cost;

                cell = row.createCell(7);
                cell.setCellStyle(style);
                cell.setCellValue(mSerAmount);
                mSerAmountMain += mSerAmount;
            }
            row = sheet.createRow(++rIndex);
            cell = row.createCell(0);
            cell.setCellStyle(style2);
            cell.setCellValue("Итого");
            sheet.addMergedRegion(new CellRangeAddress(rIndex, rIndex, 0, 3));

            style = workbook.createCellStyle();
            style.setVerticalAlignment(VerticalAlignment.CENTER);
            style.setAlignment(HorizontalAlignment.RIGHT);
            style.setFont(font);
            cell = row.createCell(4);
            cell.setCellStyle(style);
            cell.setCellValue(serMainAmount);

            cell = row.createCell(5);
            cell.setCellStyle(style);
            cell.setCellValue(baseMainRate);

            cell = row.createCell(6);
            cell.setCellStyle(style);
            cell.setCellValue(decimalFormat.format(costMain));

            cell = row.createCell(7);
            cell.setCellStyle(style);
            cell.setCellValue(mSerAmountMain);

            row = sheet.createRow(++rIndex);
            cell = row.createCell(0);
            cell.setCellStyle(style2);
            cell.setCellValue("Итого (в том числе: по лабораторным исследованиям включая инструментальные замеры)");
            sheet.addMergedRegion(new CellRangeAddress(rIndex, rIndex, 0, 3));

            style = workbook.createCellStyle();
            style.setVerticalAlignment(VerticalAlignment.CENTER);
            style.setAlignment(HorizontalAlignment.RIGHT);
            style.setFont(font);
            cell = row.createCell(4);
            cell.setCellStyle(style);
            cell.setCellValue(serMainAmount - OserMainAmount);

            cell = row.createCell(5);
            cell.setCellStyle(style);
            cell.setCellValue(baseMainRate - ObaseMainRate);

            cell = row.createCell(6);
            cell.setCellStyle(style);
            if (reportInfoClass.isStateActive()) {
                cell.setCellValue(decimalFormat.format(costMain - OcostMain));
            } else {
                cell.setCellValue(decimalFormat.format(costMain - OcostMain + 0.01));
            }

            cell = row.createCell(7);
            cell.setCellStyle(style);
            cell.setCellValue(mSerAmountMain - OmSerAmountMain);

            row = sheet.createRow(++rIndex);
            cell = row.createCell(0);
            cell.setCellStyle(style2);
            cell.setCellValue("Итого (по очаговой дезинфекции, дезинсекции и дератизации)");
            sheet.addMergedRegion(new CellRangeAddress(rIndex, rIndex, 0, 3));

            style = workbook.createCellStyle();
            style.setVerticalAlignment(VerticalAlignment.CENTER);
            style.setAlignment(HorizontalAlignment.RIGHT);
            style.setFont(font);
            cell = row.createCell(4);
            cell.setCellStyle(style);
            cell.setCellValue(OserMainAmount);

            cell = row.createCell(5);
            cell.setCellStyle(style);
            cell.setCellValue(ObaseMainRate);

            cell = row.createCell(6);
            cell.setCellStyle(style);
            cell.setCellValue(decimalFormat.format(OcostMain));

            cell = row.createCell(7);
            cell.setCellStyle(style);
            cell.setCellValue(OmSerAmountMain);
        }

        row = sheet.createRow(++rIndex);
        cell = row.createCell(0);
        cell.setCellStyle(style2);
        cell.setCellValue("Сумма прописью: " + Converter.convert(decimalFormat.format(costMain)));
        sheet.addMergedRegion(new CellRangeAddress(rIndex, rIndex, 0, 3));

        rIndex += 2;
        Font fontStyle = workbook.createFont();
        fontStyle.setFontName("Calibri");
        fontStyle.setFontHeightInPoints((short) 10);
        Font fontStyle1 = workbook.createFont();
        fontStyle1.setFontName("Calibri");
        fontStyle1.setFontHeightInPoints((short) 14);
        row = sheet.createRow(rIndex);
        cell = row.createCell(1);
        CellStyle cellStyle = cell.getCellStyle();
        CellStyle cellStyle1 = cell.getCellStyle();
        cellStyle.setFont(fontStyle);
        cellStyle1.setFont(fontStyle);

        cell.setCellStyle(cellStyle);
        cell.setCellValue("Исполнитель");
        sheet.addMergedRegion(new CellRangeAddress(rIndex, rIndex, 1, 3));

        rIndex++;
        row = sheet.createRow(rIndex);
        cell = row.createCell(1);
        cell.setCellStyle(cellStyle);
        cell.setCellValue("Телефон");
        sheet.addMergedRegion(new CellRangeAddress(rIndex, rIndex, 1, 3));

        rIndex++;
        row = sheet.createRow(rIndex);
        cell = row.createCell(1);
        cell.setCellStyle(cellStyle);
        cell.setCellValue("E-mail");
        sheet.addMergedRegion(new CellRangeAddress(rIndex, rIndex, 1, 3));

        cell = row.createCell(4);
        cell.setCellStyle(cellStyle1);
        cell.setCellValue("_______________________________________________________________________");
        sheet.addMergedRegion(new CellRangeAddress(rIndex, rIndex, 4, 12));

        rIndex++;

        row = sheet.createRow(rIndex);
        cell = row.createCell(5);
        cell.setCellStyle(cellStyle);
        cell.setCellValue("Подпись");

        cell = row.createCell(10);
        cell.setCellStyle(cellStyle);
        cell.setCellValue("ФИО");

        rIndex += 2;

        row = sheet.createRow(rIndex);
        cell = row.createCell(1);

        CellStyle cellStyle2 = cell.getCellStyle();
        cellStyle2.setFont(fontStyle1);
        cell.setCellStyle(cellStyle2);
        cell.setCellValue("Директор");
        sheet.addMergedRegion(new CellRangeAddress(rIndex, rIndex, 1, 3));

        cell = row.createCell(4);
        cell.setCellStyle(cellStyle1);
        cell.setCellValue("_______________________________________________________________________");
        sheet.addMergedRegion(new CellRangeAddress(rIndex, rIndex, 4, 12));

        rIndex++;

        row = sheet.createRow(rIndex);
        cell = row.createCell(5);
        cell.setCellStyle(cellStyle);
        cell.setCellValue("Подпись");

        cell = row.createCell(10);
        cell.setCellStyle(cellStyle);
        cell.setCellValue("ФИО");

        rIndex += 2;

        row = sheet.createRow(rIndex);
        cell = row.createCell(1);
        cell.setCellStyle(cellStyle2);
        cell.setCellValue("Главный бухгалтер");
        sheet.addMergedRegion(new CellRangeAddress(rIndex, rIndex, 1, 3));

        cell = row.createCell(4);
        cell.setCellStyle(cellStyle1);
        cell.setCellValue("_______________________________________________________________________");
        sheet.addMergedRegion(new CellRangeAddress(rIndex, rIndex, 4, 12));

        rIndex++;

        row = sheet.createRow(rIndex);
        cell = row.createCell(5);
        cell.setCellStyle(cellStyle);
        cell.setCellValue("Подпись");

        cell = row.createCell(10);
        cell.setCellStyle(cellStyle);
        cell.setCellValue("ФИО");

        String text = (reportInfoClass.isStateActive()) ? "Согласовано (только для филиала Национального центра экспертизы)\n" +
                "РЕСПУБЛИКАНСКОЕ ГОСУДАРСТВЕННОЕ\n" +
                "УЧРЕЖДЕНИЕ “ДЕПАРТАМЕНТ КОНТРОЛЯ\n" +
                "КАЧЕСТВА И БЕЗОПАСНОСТИ ТОВАРОВ И УСЛУГ \n" +
                jsonNode.get("areaName").asText() +
                " КОМИТЕТА КОНТРОЛЯ КАЧЕСТВА И \n" +
                "БЕЗОПАСНОСТИ ТОВАРОВ И УСЛУГ”\n"
                : "Согласовано (только для филиала Национального центра экспертизы)\n" +
                "РЕСПУБЛИКАНСКОЕ ГОСУДАРСТВЕННОЕ\n" +
                "УЧРЕЖДЕНИЕ “ДЕПАРТАМЕНТ КОНТРОЛЯ\n" +
                "КАЧЕСТВА И БЕЗОПАСНОСТИ ТОВАРОВ И УСЛУГ\n";
        rIndex++;
        row = sheet.createRow(rIndex);
        row.setHeightInPoints(80);
        cell = row.createCell(1);
        Font font22 = workbook.createFont();
        font22.setFontName("Calibri");
        font22.setFontHeightInPoints((short) 8);
        CellStyle style44 = cell.getCellStyle();
        style44.setFont(font22);
        style44.setWrapText(true);
        style44.setAlignment(HorizontalAlignment.LEFT);
        style44.setVerticalAlignment(VerticalAlignment.TOP);
        cell.setCellStyle(style44);
        cell.setCellValue(text);
        sheet.addMergedRegion(new CellRangeAddress(rIndex, rIndex, 1, 5));

        rIndex += 2;

        row = sheet.createRow(rIndex);
        cell = row.createCell(1);
        cell.setCellStyle(cellStyle);
        cell.setCellValue("Руководитель");
        sheet.addMergedRegion(new CellRangeAddress(rIndex, rIndex, 1, 3));

        cell = row.createCell(4);
        cell.setCellStyle(cellStyle1);
        cell.setCellValue("_______________________________________________________________________");
        sheet.addMergedRegion(new CellRangeAddress(rIndex, rIndex, 4, 12));

        rIndex++;

        row = sheet.createRow(rIndex);
        cell = row.createCell(5);
        cell.setCellStyle(cellStyle);
        cell.setCellValue("Подпись");

        cell = row.createCell(10);
        cell.setCellStyle(cellStyle);
        cell.setCellValue("ФИО");


        sheet.setColumnWidth(0, 2000);
        sheet.setColumnWidth(2, 9000);

        if (reportInfoClass.isExcel()) {
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            sheet.getPrintSetup().setLandscape(true);
            workbook.write(bos);
            return bos.toByteArray();
        } else {
            Random random = new Random(LocalDateTime.now().getNano());
            int intRandom = random.nextInt();
            String excelFile = "file" + intRandom + ".xlsx";
            File file = new File("/opt/synergy/jboss/standalone/data/excel/", excelFile);
            while (file.exists()) {
                intRandom = random.nextInt();
                excelFile = "file" + intRandom + ".xlsx";
                file = new File("/opt/synergy/jboss/standalone/data/excel/", excelFile);
            }


            try {
                boolean created = file.createNewFile();
                if (created) {
                    System.out.println("File was created");
                }
            } catch (IOException e) {
                e.getMessage();
            }
            FileOutputStream outputStream = new FileOutputStream(file);
            sheet.getPrintSetup().setLandscape(true);
            workbook.write(outputStream);
            outputStream.close();
            Process p = Runtime.getRuntime().exec("soffice --headless --convert-to pdf:writer_pdf_Export /opt/synergy/jboss/standalone/data/excel/"+ excelFile  +"  --outdir /opt/synergy/jboss/standalone/data/excel/");
            p.waitFor();
            InputStream stream = p.getErrorStream();
            System.out.println("Error stream " + IOUtils.toString(stream, "UTF-8"));
            stream.close();

            String pdfFile = "file" + intRandom + ".pdf";
            file = new File("/opt/synergy/jboss/standalone/data/excel/", pdfFile);
            FileInputStream fileInputStream = new FileInputStream(file);
            byte[] bytes = IOUtils.toByteArray(fileInputStream);
            fileInputStream.close();

            if (file.delete()) System.out.println("Pdf file was deleted");

            file = new File("/opt/synergy/jboss/standalone/data/excel/", excelFile);
            if (file.delete()) System.out.println("Excel file was deleted");

            return bytes;
        }
    }

    @Override
    public String getSql() {
        return "";
    }

    private int genHeadOfTable(Workbook workbook, Sheet sheet, ReestrReportInfoClass infoClass) {

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy");

        int indexRow = 0;
        // Первая строка
        Font font;

        CellStyle style;

        Row row = sheet.createRow(indexRow);
        row.setHeightInPoints(50);


        font = workbook.createFont();
        font.setFontName("Calibri");
        font.setFontHeightInPoints((short) 8);


        Cell cell = row.createCell(4);
        style = workbook.createCellStyle();
        style.setFont(font);
        style.setWrapText(true);
        style.setAlignment(HorizontalAlignment.RIGHT);
        style.setVerticalAlignment(VerticalAlignment.CENTER);
        cell.setCellStyle(style);
        cell.setCellValue("Приложение \n к приказу Председателя Комитета охраны общественного \n здоровья Министерства здравоохранения \n Республики Казахстан от 17 января 2019 года №9-НК");
        sheet.addMergedRegion(new CellRangeAddress(indexRow, indexRow, 4, 7));


        row = sheet.createRow(++indexRow);
        style = workbook.createCellStyle();
        font = workbook.createFont();
        font.setFontName("Calibri");
        font.setBold(true);
        font.setFontHeightInPoints((short) 11);
        style.setFont(font);
        style.setWrapText(true);
        style.setAlignment(HorizontalAlignment.CENTER);
        style.setVerticalAlignment(VerticalAlignment.CENTER);
        cell = row.createCell(0);
        cell.setCellStyle(style);
        cell.setCellValue("Счет реестр");
        sheet.addMergedRegion(new CellRangeAddress(indexRow,indexRow,0,7));
        // Закончили с первой строкой

        //Вторая строка
        font = workbook.createFont();
        font.setBold(true);
        font.setFontName("Calibri");
        font.setFontHeightInPoints((short) 10);

        style = workbook.createCellStyle();
        style.setFont(font);
        style.setWrapText(true);
        style.setAlignment(HorizontalAlignment.CENTER);
        style.setVerticalAlignment(VerticalAlignment.CENTER);
        row = sheet.createRow(++indexRow);
        row.setHeightInPoints((short) 25);
        cell = row.createCell(0);
        cell.setCellStyle(style);
        cell.setCellValue("за оказанные услуги по проведению лабораторных исследований, включая инструментальные замеры при проведении санитарно-");
        sheet.addMergedRegion(new CellRangeAddress(indexRow, indexRow, 0, 7));
        //Вторая строка закончена

        //Третья строка
        row = sheet.createRow(++indexRow);
        row.setHeightInPoints((short) 25);
        cell = row.createCell(0);
        cell.setCellStyle(style);
        cell.setCellValue("эпидемиологической экспертизы,очаговой дезинфекции,дезинсекции,дератизации в очагах инфекционных и паразитарных заболеваний человека");
        sheet.addMergedRegion(new CellRangeAddress(indexRow, indexRow, 0, 7));
        // Конец третье строки

        //4 строка
        row = sheet.createRow(++indexRow);
        cell = row.createCell(0);
        cell.setCellStyle(style);
        cell.setCellValue(String.format("отчетный период c %s по %s", infoClass.getStartDate().format(formatter), infoClass.getEndDate().format(formatter)));
        sheet.addMergedRegion(new CellRangeAddress(indexRow, indexRow, 0, 7));

        //5 строка
        font = workbook.createFont();
        font.setFontName("Calibri");
        font.setFontHeightInPoints((short) 9);
        style = workbook.createCellStyle();
        style.setFont(font);
        style.setVerticalAlignment(VerticalAlignment.CENTER);
        style.setWrapText(true);
        row = sheet.createRow(++indexRow);
        row.setHeightInPoints((short) 40);
        cell = row.createCell(0);
        cell.setCellStyle(style);
        cell.setCellValue(String.format("Наименование организации: %s %s", StrUlits.isEmpty(infoClass.getStateName()) ? "РГП на ПВХ 'Национальный центр экспертизы' КККБТУ МЗ РК" : infoClass.getStateName(), StrUlits.isEmpty(infoClass.getRegionName()) ? "" : infoClass.getRegionName()));
        sheet.addMergedRegion(new CellRangeAddress(indexRow, indexRow, 0, 7));

        //6 строка
        row = sheet.createRow(++indexRow);
        cell = row.createCell(0);
        row.setHeightInPoints((short) 40);
        cell.setCellStyle(style);
        cell.setCellValue("Код и наименование бюджетной программы : 088 \"Реализация мероприятий в области санитарно-эпидемиологического благополучия населения\"");
        sheet.addMergedRegion(new CellRangeAddress(indexRow,indexRow,0,7));

        //7 строка
        row = sheet.createRow(++indexRow);
        cell = row.createCell(0);
        row.setHeightInPoints((short) 40);
        cell.setCellStyle(style);
        cell.setCellValue("Код и наименование бюджетной подпрограммы : 100 \"Обеспечение санитарно эпидемиологического благополучия населения\"");
        sheet.addMergedRegion(new CellRangeAddress(indexRow,indexRow,0,7));

        // 8 строка
        row = sheet.createRow(++indexRow);
        style = workbook.createCellStyle();
        style.setFont(font);
        style.setVerticalAlignment(VerticalAlignment.CENTER);
        style.setAlignment(HorizontalAlignment.CENTER);
        style.setWrapText(true);
        cell = row.createCell(0);
        cell.setCellStyle(style);
        cell.setCellValue("Источник финансирования: Республиканский бюджет");
        sheet.addMergedRegion(new CellRangeAddress(indexRow,indexRow,0,7));


        font = workbook.createFont();
        font.setBold(true);
        font.setFontName("Calibri");
        font.setFontHeightInPoints((short) 8);
        style = workbook.createCellStyle();
        style.setFont(font);
        style.setAlignment(HorizontalAlignment.CENTER);
        style.setVerticalAlignment(VerticalAlignment.CENTER);
        style.setBorderBottom(BorderStyle.DASHED);
        style.setBorderLeft(BorderStyle.DASHED);
        style.setBorderRight(BorderStyle.DASHED);
        style.setBorderTop(BorderStyle.DASHED);
        style.setWrapText(true);
        row = sheet.createRow(++indexRow);
        row.setHeightInPoints(30);

        cell = row.createCell(0);
        cell.setCellStyle(style);
        cell.setCellValue("№ п/п");

        cell = row.createCell(1);
        cell.setCellStyle(style);
        cell.setCellValue("Код услуги");

        cell = row.createCell(2);
        cell.setCellStyle(style);
        cell.setCellValue("Наименование");

        cell = row.createCell(3);
        cell.setCellStyle(style);
        cell.setCellValue("Весовой коэффициент");

        cell = row.createCell(4);
        cell.setCellStyle(style);
        cell.setCellValue("Количество услуг");

        cell = row.createCell(5);
        cell.setCellStyle(style);
        cell.setCellValue("Количество \nбазовых ставок");

        cell = row.createCell(6);
        cell.setCellStyle(style);
        cell.setCellValue("Стоимость \nв тенге");

        cell = row.createCell(7);
        cell.setCellStyle(style);
        cell.setCellValue("Количество исследований \nпо которым получены \nрезультаты не соответствующие \nтребованиям НТД");

        return indexRow;
    }
}
