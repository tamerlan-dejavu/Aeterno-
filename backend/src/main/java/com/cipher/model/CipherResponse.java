package com.cipher.model;

public class CipherResponse {
    private String result;
    private boolean success;
    private String message;

    public CipherResponse() {}

    public CipherResponse(String result, boolean success, String message) {
        this.result = result;
        this.success = success;
        this.message = message;
    }

    public static CipherResponse ok(String result, String message) {
        return new CipherResponse(result, true, message);
    }

    public static CipherResponse error(String message) {
        return new CipherResponse("", false, message);
    }

    public String getResult() { return result; }
    public void setResult(String result) { this.result = result; }

    public boolean isSuccess() { return success; }
    public void setSuccess(boolean success) { this.success = success; }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
}
