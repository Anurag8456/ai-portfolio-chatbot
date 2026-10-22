package com.portfoliochatbot.backend.dto;

public class JobMatchResponse {
    private boolean success;
    private String analysis;
    private String errorMessage;

    public JobMatchResponse() {
    }

    public JobMatchResponse(boolean success, String analysis, String errorMessage) {
        this.success = success;
        this.analysis = analysis;
        this.errorMessage = errorMessage;
    }

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public String getAnalysis() {
        return analysis;
    }

    public void setAnalysis(String analysis) {
        this.analysis = analysis;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }
}
