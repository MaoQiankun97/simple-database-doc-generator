package com.riddle.tools.database.doc;

import com.riddle.tools.database.doc.dependencies.AbstractDocGenerator;
import com.riddle.tools.database.doc.dependencies.PgsqlDocGenerator;

import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class SimpleDocGenerator {

    public static void main(String[] args) throws IOException, SQLException {
        final String URL = "";
        final String USER = "";
        final String PASSWORD = "";
        Connection conn = null;
        try {
            //1.加载驱动程序
            Class.forName("org.postgresql.Driver");
            //2. 获得数据库连接
            conn = DriverManager.getConnection(URL, USER, PASSWORD);
        } catch (ClassNotFoundException | SQLException e) {
            e.printStackTrace();
        }
        AbstractDocGenerator docGenerator = new PgsqlDocGenerator(conn);
        docGenerator.generate("C:\\Users\\riddle\\workspace\\github\\simple-database-doc-generator\\src\\main\\resources\\" + File.separator + System.currentTimeMillis() + ".md");
        System.out.println("generate success!");
    }
}
