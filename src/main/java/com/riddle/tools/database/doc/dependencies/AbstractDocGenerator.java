package com.riddle.tools.database.doc.dependencies;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;

/**
 * 文档生成器.
 */
public abstract class AbstractDocGenerator {
    protected Connection connection = null;

    public AbstractDocGenerator(Connection connection) {
        this.connection = connection;
    }

    public abstract void generate(String fileFullName) throws SQLException, IOException;
}
