package lk.ijse.dep7.util;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;

public class SearchOrderTM implements Serializable {
    private String orderID;
    private LocalDate date;
    private String customerID;
    private String customerName;
    private BigDecimal total;

    public SearchOrderTM() {
    }

    public SearchOrderTM(String orderID, LocalDate date, String customerID, String customerName, BigDecimal total) {
        this.orderID = orderID;
        this.date = date;
        this.customerID = customerID;
        this.customerName = customerName;
        this.total = total;
    }

    public String getOrderID() {
        return orderID;
    }

    public void setOrderID(String orderID) {
        this.orderID = orderID;
    }

    public LocalDate getDate() {
        return date;
    }

    public void setDate(LocalDate date) {
        this.date = date;
    }

    public String getCustomerID() {
        return customerID;
    }

    public void setCustomerID(String customerID) {
        this.customerID = customerID;
    }

    public String getCustomerName() {
        return customerName;
    }

    public void setCustomerName(String customerName) {
        this.customerName = customerName;
    }

    public BigDecimal getTotal() {
        return total;
    }

    public void setTotal(BigDecimal total) {
        this.total = total;
    }

    @Override
    public String toString() {
        return "SearchOrderTM{" +
                "orderID='" + orderID + '\'' +
                ", date=" + date +
                ", customerID='" + customerID + '\'' +
                ", customerName='" + customerName + '\'' +
                ", total=" + total +
                '}';
    }
}
