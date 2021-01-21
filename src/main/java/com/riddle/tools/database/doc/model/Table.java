package com.riddle.tools.database.doc.model;

import java.util.List;

public class Table {
    /**
     * schema名.
     */
    private String schemaName;
    /**
     * 表名.
     */
    private String tableName;
    /**
     * 备注.
     */
    private String comment;
    /**
     * 字段信息.
     */
    private List<Column> columns;

    public String getSchemaName() {
        return schemaName;
    }

    public void setSchemaName(String schemaName) {
        this.schemaName = schemaName;
    }

    public String getTableName() {
        return tableName;
    }

    public void setTableName(String tableName) {
        this.tableName = tableName;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public List<Column> getColumns() {
        return columns;
    }

    public void setColumns(List<Column> columns) {
        this.columns = columns;
    }
}
