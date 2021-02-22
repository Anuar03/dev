package kz.report.dev.reportpackage.reportmodels.covid;

import java.time.LocalDate;

public class CovidMonthData {
    private double price;
    private LocalDate date;
    private String strDate;

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    public LocalDate getDate() {
        return date;
    }

    public void setDate(LocalDate date) {
        this.date = date;
    }

    public String getStrDate() {
        return strDate;
    }

    public void setStrDate(String strDate) {
        this.strDate = strDate;
    }
}
