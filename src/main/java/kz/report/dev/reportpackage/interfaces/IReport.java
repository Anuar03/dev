package kz.report.dev.reportpackage.interfaces;

/**
    При реализации данного интерфейса
    обязательно должен быть конструктор класса принимающий один строковой параметр
 */

public interface IReport {
    byte[] genReport() throws Exception;
    String getSql();
}
