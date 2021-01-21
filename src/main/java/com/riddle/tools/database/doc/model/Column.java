package com.riddle.tools.database.doc.model;

public class Column {
    /**
     * 字段名.
     */
    private String fieldName;
    /**
     * 字段类型.
     */
    private String type;
    /**
     * 是否非空.
     */
    private String notNull;
    /**
     * 注释.
     */
    private String comment;


    public String getFieldName() {
        return fieldName;
    }

    public void setFieldName(String fieldName) {
        this.fieldName = fieldName;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getNotNull() {
        return notNull;
    }

    public void setNotNull(String notNull) {
        this.notNull = notNull;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }
}
