package com.kayhut.fuse.model.transport.cursor;

import com.kayhut.fuse.model.transport.CreatePageRequest;
import javaslang.collection.Stream;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by roman.margolis on 11/03/2018.
 */
public class CreateCsvCursorRequest extends CreateCursorRequest {
    //region Constructors
    public CreateCsvCursorRequest() {
        this(null, null);
    }

    public CreateCsvCursorRequest(CsvElement[] csvElements) {
        this(csvElements, null);
    }

    public CreateCsvCursorRequest(CsvElement[] csvElements, CreatePageRequest createPageRequest) {
        super(createPageRequest);
        this.csvElements = csvElements;
    }
    //endregion

    //region Properties
    public CsvElement[] getCsvElements() {
        return csvElements;
    }

    public void setCsvElements(CsvElement[] csvElements) {
        this.csvElements = csvElements;
    }
    //endregion

    //region Fields
    private CsvElement[] csvElements;
    //endregion

    //region Builder
    public static final class Builder{
        public Builder() {
            csvElements = new ArrayList<>();
        }

        public static Builder instance() {
            return new Builder();
        }

        public Builder withElement(CsvElement csvElement){
            this.csvElements.add(csvElement);
            return this;
        }

        public CreateCsvCursorRequest request(){
            return new CreateCsvCursorRequest(Stream.ofAll(this.csvElements).toJavaArray(CsvElement.class));
        }

        private List<CsvElement> csvElements;
    }
    //endregion

    //region CsvElement
    public static class CsvElement{
        public CsvElement() {
        }

        public CsvElement(String tag1, String property, ElementType elementType) {
            this.tag1 = tag1;
            this.property = property;
            this.elementType = elementType;
        }

        public CsvElement(String tag1, String tag2, String property, ElementType elementType) {
            this.tag1 = tag1;
            this.tag2 = tag2;
            this.property = property;
            this.elementType = elementType;
        }

        public String getTag1() {
            return tag1;
        }

        public String getTag2() {
            return tag2;
        }

        public void setTag2(String tag2) {
            this.tag2 = tag2;
        }

        public String getProperty() {
            return property;
        }

        public ElementType getElementType() {
            return elementType;
        }

        public void setTag1(String tag1) {
            this.tag1 = tag1;
        }

        public void setProperty(String property) {
            this.property = property;
        }

        public void setElementType(ElementType elementType) {
            this.elementType = elementType;
        }

        private String tag1;
        private String tag2;
        private String property;
        private ElementType elementType;
    }
    //endregion

    public enum ElementType{
        Entity,
        Rel
    }
}