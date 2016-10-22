package com.ea7jmf.nytarticles.models;

import java.text.SimpleDateFormat;
import java.util.Date;

public class SearchQuery {

    public enum SortDirection {
        NEWEST,
        OLDEST
    }

    private final String query;
    private final Date beginDate;
    private final Date endDate;
    private final SortDirection sort;
    private final int page;


    public String getQuery() {
        return query;
    }

    public Date getBeginDate() {
        return beginDate;
    }

    public Date getEndDate() {
        return endDate;
    }

    public String getFormattedBeginDate() {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyymmDD");
        return beginDate != null ? dateFormat.format(beginDate) : null;
    }

    public String getFormattedEndDate() {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyymmDD");
        return endDate != null ? dateFormat.format(endDate) : null;
    }

    public SortDirection getSort() {
        return sort;
    }

    public String getFormattedSort() {
        String val = null;

        if (sort == null) {
            return val;
        }

        switch (sort) {
            case NEWEST:
                val = "newest";
                break;
            case OLDEST:
                val = "oldest";
                break;
        }
        return val;
    }

    public Integer getPage() {
        return page;
    }

    public static class Builder {

        private String query;
        private Date beginDate;
        private Date endDate;
        private SortDirection sort;
        private int page = 0;

        public Builder(String query) {
            this.query = query;
        }

        public Builder(SearchQuery instance) {
            this.query = instance.getQuery();
            this.beginDate = instance.getBeginDate();
            this.endDate = instance.getEndDate();
            this.sort = instance.getSort();
            this.page = instance.getPage();
        }

        public Builder query(String query) {
            this.query = query;
            return this;
        }

        public Builder beginDate(Date beginDate) {
            this.beginDate = beginDate;
            return this;
        }

        public Builder endDate(Date endDate) {
            this.endDate = endDate;
            return this;
        }

        public Builder sort(SortDirection dir) {
            this.sort = dir;
            return this;
        }

        public Builder page(int page) {
            this.page = page;
            return this;
        }

        public SearchQuery build() {
            return new SearchQuery(this);
        }
    }

    private SearchQuery(SearchQuery.Builder builder) {
        this.query = builder.query;
        this.beginDate = builder.beginDate;
        this.endDate = builder.endDate;
        this.sort = builder.sort;
        this.page = builder.page;
    }
}
