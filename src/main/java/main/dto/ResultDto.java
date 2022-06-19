package main.dto;

public class ResultDto {

    private boolean result;
    private String error;

    public ResultDto() {
        result = true;
    }

    public ResultDto(String error) {
        this.result = false;
        this.error = error;
    }

    public boolean isResult() {
        return result;
    }

    public void setResult(boolean result) {
        this.result = result;
    }

    public String getError() {
        return error;
    }

    public void setError(String error) {
        this.error = error;
    }
}
