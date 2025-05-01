package com.prismix.server.core;

import com.prismix.server.utils.DatabaseManager;
import com.prismix.server.utils.PropertiesLoader;

import java.sql.SQLException;

public class test {
    public static void main(String[] args) throws SQLException {
        System.out.println("Hello World!");
        DatabaseManager.getConnection().close();
    }
}
