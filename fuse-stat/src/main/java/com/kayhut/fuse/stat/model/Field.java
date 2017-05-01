package com.kayhut.fuse.stat.model;

/**
 * Created by benishue on 30-Apr-17.
 */
public class Field {

    public Field() {
    }

    public Field(String field, Histogram histogram) {
        this.field = field;
        this.histogram = histogram;
    }

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

    //region Fields

    //The name of the field for which statistics are being calculated
    private String field;
    //histogram buckets defenition
    private Histogram histogram;
    //endregion
}
