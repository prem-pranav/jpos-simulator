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
    private String cvd;

    public CardInfo(String[] fields) {
        if (fields.length >= 12) {
            this.prefix = fields[0];
            this.panLength = Integer.parseInt(fields[1]);
            this.pan = fields[2];
            this.expiry = fields[3];
            this.pin = fields[4];
            this.pvv = fields[5];
            this.cvd = fields.length >= 7 ? fields[6] : "000";
            this.status = fields.length >= 8 ? fields[7] : "ACTIVE";
            this.product = fields.length >= 9 ? fields[8] : "STANDARD";
            this.scheme = fields.length >= 10 ? fields[9] : "VISA";
            this.perTxLimit = fields.length >= 11 ? fields[10] : "0.00";
            this.dailyLimit = fields.length >= 12 ? fields[11] : "0.00";
            this.sourceId = fields.length >= 13 ? fields[12] : "GEN_SRC";
            this.selected = fields.length >= 14 && "Y".equalsIgnoreCase(fields[13]);
        }
    }

    // ... (existing getters)

    public void setPvv(String pvv) {
        this.pvv = pvv;
    }

    public String getCvd() {
        return cvd;
    }

    public void setCvd(String cvd) {
        this.cvd = cvd;
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
