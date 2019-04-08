package com.mc.streaming.model;

public class TransactionResult {

    private boolean hasError;
    private String errorMsg;

    public TransactionResult() { }

    public TransactionResult(String errorMsg, boolean hasError) {
        this.errorMsg = errorMsg;
        this.hasError = hasError;
    }

    public boolean hasError() {
        return hasError;
    }

    public String getErrorMsg() {
        return errorMsg;
    }

    @Override
    public String toString() {
        return "TransactionResult{" +
                "hasError=" + hasError +
                ", errorMsg='" + errorMsg + '\'' +
                '}';
    }
}
