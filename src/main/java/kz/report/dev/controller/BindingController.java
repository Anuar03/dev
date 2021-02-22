package kz.report.dev.controller;

import kz.report.dev.reportpackage.ReportContainer;
import kz.report.dev.reportpackage.interfaces.IReport;
import org.apache.commons.io.IOUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import java.io.File;
import java.io.FileInputStream;
import java.lang.reflect.Constructor;
import java.util.Objects;

@RestController
@RequestMapping("/generateReport")
public class BindingController {

    @Autowired
    private ReportContainer reportContainer;

    @PostMapping("/{reportName}")
    public HttpEntity<byte[]> repProcess(@PathVariable String reportName, @RequestBody String param) throws Exception {
        String reportClazz;
        Constructor cns;
        IReport generated;
        byte[] reportBytes;
        if (Objects.isNull(reportName)) return null;
        reportClazz = reportContainer.getReportClass(reportName);
        if (Objects.isNull(reportClazz)) return null;
        cns = Class.forName(reportClazz).getConstructor(String.class);
        generated = (IReport) cns.newInstance(param);
        reportBytes = generated.genReport();
        if (Objects.isNull(reportBytes)) return null;
        HttpHeaders header = new HttpHeaders();
        header.setContentType(new MediaType("application", "vnd.openxmlformats-officedocument.spreadsheetml.sheet"));
        header.set(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=report.xlsx");
        header.setContentLength(reportBytes.length);
        return new HttpEntity<>(reportBytes, header);
    }

    @PostMapping("/genExcel")
    public HttpEntity<byte[]> gen() throws Exception {
        ClassPathResource c = new ClassPathResource("excel/testdata.xlsx");
        byte[] bytes = IOUtils.toByteArray(c.getInputStream());
        HttpHeaders header = new HttpHeaders();
        header.setContentType(new MediaType("application", "vnd.openxmlformats-officedocument.spreadsheetml.sheet"));
        header.set(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=my_file.xls");
        header.setContentLength(bytes.length);
        return new HttpEntity<>(bytes, header);
    }
}
