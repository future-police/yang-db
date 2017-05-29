package com.kayhut.fuse.stat.model.configuration;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.kayhut.fuse.stat.model.histogram.Histogram;

import java.util.List;

/**
 * Created by benishue on 30-Apr-17.
 */
@JsonInclude(JsonInclude.Include.NON_EMPTY)
@JsonIgnoreProperties(ignoreUnknown = true)
public class Field {

    //region Ctrs
    public Field() {
        //needed for Jackson
    }

    public Field(String field, Histogram histogram) {
        this.field = field;
        this.histogram = histogram;
    }

    public Field(String field, Histogram histogram, List<Filter> filter) {
        this.field = field;
        this.histogram = histogram;
        this.filter = filter;
    }

    //endregion

    //region Getters & Setters
    public String getField() {
        return field;
    }

    public void setField(String field) {
        this.field = field;
    }

    public Histogram getHistogram() {
        return histogram;
    }

    public void setHistogram(Histogram histogram) {
        this.histogram = histogram;
    }

    public List<Filter> getFilter() {
        return filter;
    }

    public void setFilter(List<Filter> filter) {
        this.filter = filter;
    }

    //endregion

    //region Fields
    //The name of the field for which statistics are being calculated
    private String field;
    //histogram buckets definition
    private Histogram histogram;
    //Used in edges for example
    private List<Filter> filter;

    //endregion
}
