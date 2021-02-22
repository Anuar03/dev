package kz.report.dev.reportpackage.reportmodels.covid;

public class CovidDataClass {
    private String serviceName;
    private long amount;
    private double price;
    private double sum;

    public CovidDataClass() {
    }

    public CovidDataClass(String serviceName, long amount, double price, double sum) {
        this.serviceName = serviceName;
        this.amount = amount;
        this.price = price;
        this.sum = sum;
    }

    public String getServiceName() {
        return serviceName;
    }

    public void setServiceName(String serviceName) {
        this.serviceName = serviceName;
    }

    public long getAmount() {
        return amount;
    }

    public void setAmount(long amount) {
        this.amount = amount;
    }

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    public double getSum() {
        return sum;
    }

    public void setSum(double sum) {
        this.sum = sum;
    }
}
