package main.dto;

import main.model.Status;

import java.util.ArrayList;

public class StatisticsDto {
    private ResultDto result;
    private Statistics statistics;

    public StatisticsDto(Statistics statistics) {
        this.result = new ResultDto();
        this.statistics = statistics;
    }

    public ResultDto getResult() {
        return result;
    }

    public void setResult(ResultDto result) {
        this.result = result;
    }

    public Statistics getStatistics() {
        return statistics;
    }

    public void setStatistics(Statistics statistics) {
        this.statistics = statistics;
    }

    public static class Statistics {

        private Total total;
        private ArrayList<Detailed> detailed;

        public Statistics(Total total, ArrayList<Detailed> detailed) {
            this.total = total;
            this.detailed = detailed;
        }

        public Total getTotal() {
            return total;
        }

        public void setTotal(Total total) {
            this.total = total;
        }

        public ArrayList<Detailed> getDetailed() {
            return detailed;
        }

        public void setDetailed(ArrayList<Detailed> detailed) {
            this.detailed = detailed;
        }
    }

    public static class Total {

        private int sites;
        private int pages;
        private int lemmas;
        private boolean isIndexing;

        public Total(int sites, int pages, int lemmas, boolean isIndexing) {
            this.sites = sites;
            this.pages = pages;
            this.lemmas = lemmas;
            this.isIndexing = isIndexing;
        }

        public int getSites() {
            return sites;
        }

        public void setSites(int sites) {
            this.sites = sites;
        }

        public int getPages() {
            return pages;
        }

        public void setPages(int pages) {
            this.pages = pages;
        }

        public int getLemmas() {
            return lemmas;
        }

        public void setLemmas(int lemmas) {
            this.lemmas = lemmas;
        }

        public boolean getIsIndexing() {
            return isIndexing;
        }

        public void setIndexing(boolean isIndexing) {
            this.isIndexing = isIndexing;
        }
    }

    public static class Detailed {
        private String url;
        private String name;
        private Status status;
        private Long statusTime;
        private String error;
        private int pages;
        private int lemmas;

        public Detailed(String url, String name, Status status, Long statusTime, String error, int pages, int lemmas) {
            this.url = url;
            this.name = name;
            this.status = status;
            this.statusTime = statusTime;
            this.error = error;
            this.pages = pages;
            this.lemmas = lemmas;
        }

        public String getUrl() {
            return url;
        }

        public void setUrl(String url) {
            this.url = url;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public Status getStatus() {
            return status;
        }

        public void setStatus(Status status) {
            this.status = status;
        }

        public Long getStatusTime() {
            return statusTime;
        }

        public void setStatusTime(Long statusTime) {
            this.statusTime = statusTime;
        }

        public String getError() {
            return error;
        }

        public void setError(String error) {
            this.error = error;
        }

        public int getPages() {
            return pages;
        }

        public void setPages(int pages) {
            this.pages = pages;
        }

        public int getLemmas() {
            return lemmas;
        }

        public void setLemmas(int lemmas) {
            this.lemmas = lemmas;
        }
    }
}
