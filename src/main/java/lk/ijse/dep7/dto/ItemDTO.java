package lk.ijse.dep7.dto;

import java.math.BigDecimal;

public class ItemDTO {
    private String code;
    private String description;
    private int qty;
    private BigDecimal unitPrice;

    public ItemDTO() {
    }

    public ItemDTO(String code, String description, int qty, BigDecimal unitPrice) {
        this.code = code;
        this.description = description;
        this.qty = qty;
        this.unitPrice = unitPrice;
    }


    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
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
}
