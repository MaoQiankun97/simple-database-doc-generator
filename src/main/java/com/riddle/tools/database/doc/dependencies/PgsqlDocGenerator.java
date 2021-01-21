package com.riddle.tools.database.doc.dependencies;

import com.riddle.tools.database.doc.model.Column;
import com.riddle.tools.database.doc.model.Table;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class PgsqlDocGenerator extends AbstractDocGenerator {
    /**
     * 获取schema名称集合的sql.
     */
    private static final String PGSQL_SELECT_ALL_SCHEMA_SQL = "select nspname from pg_namespace;";
    /**
     * 获取表信息的sql.
     */
    private static final String PGSQL_SELECT_ALL_TABLE_SQL =
            "select A.oid, A.relname as NAME, b.description as COMMENT\n" +
                    "from pg_class A\n" +
                    "         inner join pg_namespace pn on A.relnamespace = pn.oid\n" +
                    "         left outer join pg_description b on b.objsubid = 0 and A.oid = b.objoid\n" +
                    "where A.relkind = 'r'\n" +
                    "  and pn.nspname = ?\n" +
                    "order by A.relname";

    /**
     * 获取表字段信息的sql.
     */
    private static final String PGSQL_SELECT_COLUMN_SQL =
            "select a.attname     as field,\n" +
                    "       t.typname     as type,\n" +
                    "       a.attlen      as length,\n" +
                    "       a.atttypmod   as lengthvar,\n" +
                    "       a.attnotnull  as \"notnull\",\n" +
                    "       b.description as comment\n" +
                    "from pg_class c\n" +
                    "         inner join pg_namespace pn on c.relnamespace = pn.oid,\n" +
                    "     pg_attribute a\n" +
                    "         left outer join pg_description b on a.attrelid = b.objoid and a.attnum = b.objsubid,\n" +
                    "     pg_type t\n" +
                    "where pn.nspname = ?\n" +
                    "  and c.relname = ?\n" +
                    "  and a.attnum > 0\n" +
                    "  and a.attrelid = c.oid\n" +
                    "  and a.atttypid = t.oid\n" +
                    "order by a.attnum;";

    public PgsqlDocGenerator(Connection connection) {
        super(connection);
    }


    @Override
    public void generate(String fileFullName) throws SQLException, IOException {
        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(System.in));
        // 获取schema列表
        List<String> schemaList = this.getSchemaList();
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("schema list: \n");
        for (int i = 0; i < schemaList.size(); i++) {
            stringBuilder.append("(").append(i).append("): ");
            stringBuilder.append(schemaList.get(i));
            stringBuilder.append("\n");
        }
        System.out.println(stringBuilder.toString());
        String schemaName = "public";
        System.out.print("choose a schema index (default 'public'): ");
        int index = -1;
        String s = bufferedReader.readLine();
        try {
            index = Integer.parseInt(s);
        } catch (Exception ex) {
            // ignore
        }
        if (0 <= index && index < schemaList.size()) {
            schemaName = schemaList.get(index);
        }
        // 获取table列表
        List<Table> tableData = this.getTableData(schemaName);
        for (int i = 0; i < tableData.size(); i++) {
            System.out.print(this.formatStringLengthTo40(tableData.get(i).getTableName()));
            if ((i & 0b11) == 0) {
                System.out.println();
            } else {
                System.out.print("\t");
            }
        }
        System.out.println(((tableData.size() & 0b11) != 0 ? "\n" : "") + "please choose table that you want generate doc, end with '#'");
        String tmp;
        List<String> tableNames = new ArrayList<>();
        while (!"#".equals((tmp = bufferedReader.readLine()))) {
            tableNames.add(tmp);
        }
        tableData = tableData.stream().filter(o -> tableNames.contains(o.getTableName())).collect(Collectors.toList());

        // 生成markdown文档.
        this.saveToMarkdown(tableData, fileFullName);
    }

    /**
     * 将获取到的表数据保存到文档中.
     *
     * @param tableData 表数据
     */
    private void saveToMarkdown(List<Table> tableData, String fileFullName) {
        int i = 1;
        StringBuilder builder = new StringBuilder();
        for (Table table : tableData) {
            builder.append("#### 2.2.").append(i).append(" ").append(table.getTableName()).append(" - ").append(table.getComment()).append("\r\n");
            builder.append("| 列名   | 类型    | 可否为空 | 注释   |").append("\r\n");
            builder.append("| ---- | ---- | ---- | ---- |").append("\r\n");
            List<Column> columnVos = table.getColumns();
            for (Column column : columnVos) {
                builder.append("|").append(column.getFieldName())
                        .append("|").append(column.getType())
                        .append("|").append(column.getNotNull())
                        .append("|").append(column.getComment())
                        .append("|\r\n");
            }
            i++;
        }
        try {
            Files.write(Paths.get(fileFullName),
                    builder.toString().getBytes(StandardCharsets.UTF_8)
            );
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private List<String> getSchemaList() throws SQLException {
        try (PreparedStatement preparedStatement = connection.prepareStatement(PGSQL_SELECT_ALL_SCHEMA_SQL)) {

            ResultSet resultSet = preparedStatement.executeQuery();

            List<String> result = new ArrayList<>();
            while (resultSet.next()) {
                result.add(resultSet.getString("nspname"));
            }

            return result;
        }
    }


    private List<Table> getTableData(String schemaName) throws SQLException {
        try (PreparedStatement preparedStatement = connection.prepareStatement(PGSQL_SELECT_ALL_TABLE_SQL)) {
            preparedStatement.setString((int) 1L, schemaName);
            ResultSet resultSet = preparedStatement.executeQuery();
            List<Table> result = new ArrayList<>();

            while (resultSet.next()) {
                Table table = new Table();
                table.setSchemaName(schemaName);
                table.setTableName(resultSet.getString("NAME"));
                table.setComment(resultSet.getString("COMMENT"));
                table.setColumns(this.getColumnData(schemaName, table.getTableName()));

                result.add(table);
            }

            return result;
        }
    }

    /**
     * 获取字段信息.
     */
    private List<Column> getColumnData(String schemaName, String tableName) throws SQLException {
        try (PreparedStatement preparedStatement = connection.prepareStatement(PGSQL_SELECT_COLUMN_SQL)) {
            preparedStatement.setString((int) 1L, schemaName);
            preparedStatement.setString((int) 2L, tableName);

            ResultSet resultSet = preparedStatement.executeQuery();
            List<Column> result = new ArrayList<>();

            while (resultSet.next()) {
                Column column = new Column();
                column.setFieldName(resultSet.getString("field"));
                column.setType(resultSet.getString("type"));
                column.setNotNull(resultSet.getString("notnull"));
                column.setComment(resultSet.getString("comment"));

                result.add(column);
            }

            return result;
        }
    }

    private String formatStringLengthTo40(String s) {
        if (s.length() <= 40) {
            StringBuilder stringBuilder = new StringBuilder();
            stringBuilder.append(s);
            while (stringBuilder.length() < 40) {
                stringBuilder.append(" ");
            }

            return stringBuilder.toString();
        } else {
            return s.substring(0, 40);
        }
    }
}
