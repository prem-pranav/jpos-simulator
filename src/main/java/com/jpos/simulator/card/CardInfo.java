package com.jpos.simulator.card;

public class CardInfo {
    private String pan;
    private String expiry;
    private String pin;
    private String pvv;
    private String status;
    private String product;
    private String scheme;
    private String perTxLimit;
    private String dailyLimit;
    private String sourceId;
    private String prefix;
    private int panLength;
    private boolean selected;

    public CardInfo(String[] fields) {
        if (fields.length >= 12) {
            this.prefix = fields[0];
            this.panLength = Integer.parseInt(fields[1]);
            this.pan = fields[2];
            this.expiry = fields[3];
            this.pin = fields[4];
            this.pvv = fields[5];
            this.status = fields[6];
            this.product = fields[7];
            this.scheme = fields[8];
            this.perTxLimit = fields[9];
            this.dailyLimit = fields[10];
            this.sourceId = fields[11];
            this.selected = fields.length >= 13 && "Y".equalsIgnoreCase(fields[12]);
        }
    }

    public String getPan() {
        return pan;
    }

    public String getExpiry() {
        return expiry;
    }

    public String getPin() {
        return pin;
    }

    public String getPvv() {
        return pvv;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getProduct() {
        return product;
    }

    public String getScheme() {
        return scheme;
    }

    public String getPerTxLimit() {
        return perTxLimit;
    }

    public String getDailyLimit() {
        return dailyLimit;
    }

    public String getSourceId() {
        return sourceId;
    }

    public String getPrefix() {
        return prefix;
    }

    public int getPanLength() {
        return panLength;
    }

    public boolean isSelected() {
        return selected;
    }

    public void setSelected(boolean selected) {
        this.selected = selected;
    }

    @Override
    public String toString() {
        return scheme + " [" + pan + "]";
    }
}
