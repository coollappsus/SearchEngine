package main.dto;

import main.model.FoundPage;

import java.util.ArrayList;
import java.util.List;

public class SearchDto {

    private ResultDto result;
    private long count;
    private List<FoundPage> data = new ArrayList<>();

    public SearchDto(List<FoundPage> data, long count) {
        result = new ResultDto();
        this.data = data;
        this.count = count;
    }

    public SearchDto() {
    }

    public ResultDto getResult() {
        return result;
    }

    public void setResult(ResultDto result) {
        this.result = result;
    }

    public long getCount() {
        return count;
    }

    public void setCount(long count) {
        this.count = count;
    }

    public List<FoundPage> getData() {
        return data;
    }

    public void setData(List<FoundPage> data) {
        this.data = data;
    }
}

