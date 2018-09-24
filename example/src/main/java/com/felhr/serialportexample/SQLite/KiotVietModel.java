package com.felhr.serialportexample.SQLite;

/**
 * Created by Ho Dong Trieu on 09/20/2018
 */
public class KiotVietModel {

    private int id_index;
    private long productID;
    private String productCode;
    private int quantity;
    private long branchId;

    public int getId_index() {
        return id_index;
    }

    public void setId_index(int id_index) {
        this.id_index = id_index;
    }

    public long getProductID() {
        return productID;
    }

    public void setProductID(long productID) {
        this.productID = productID;
    }

    public String getProductCode() {
        return productCode;
    }

    public void setProductCode(String productCode) {
        this.productCode = productCode;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public long getBranchId() {
        return branchId;
    }

    public void setBranchId(long branchId) {
        this.branchId = branchId;
    }
}
