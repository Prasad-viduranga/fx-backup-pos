package lk.ijse.dep7.dto;

import java.io.Serializable;
import java.math.BigDecimal;

public class OrderDetailsDTO implements Serializable {
    private String itemCode;
    private int qty;
    private BigDecimal unitPrice;

    public OrderDetailsDTO() {
    }

    public OrderDetailsDTO(String itemCode, int qty, BigDecimal unitPrice) {
        this.itemCode = itemCode;
        this.qty = qty;
        this.unitPrice = unitPrice;
    }

    public String getItemCode() {
        return itemCode;
    }

    public void setItemCode(String itemCode) {
        this.itemCode = itemCode;
    }

    public int getQty() {
        return qty;
    }

    public void setQty(int qty) {
        this.qty = qty;
    }

    public BigDecimal getUnitPrice() {
        return unitPrice;
    }

    public void setUnitPrice(BigDecimal unitPrice) {
        this.unitPrice = unitPrice;
    }

    @Override
    public String toString() {
        return "OrderDetailsDTO{" +
                "itemCode='" + itemCode + '\'' +
                ", qty=" + qty +
                ", unitPrice=" + unitPrice +
                '}';
    }
}
