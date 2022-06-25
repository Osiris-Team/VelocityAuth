package com.osiris.velocityauth;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

public class Database{
    // TODO: Insert credentials and update url.
    public static String rawUrl = "jdbc:mysql://localhost/";
    public static String url = "jdbc:mysql://localhost/velocityauth";
    public static String name = "velocityauth";
    public static String username;
    public static String password;

    public static void create() {
        try(Connection c = DriverManager.getConnection(Database.rawUrl, Database.username, Database.password);
            Statement s = c.createStatement();) {
            s.executeUpdate("CREATE DATABASE IF NOT EXISTS `"+Database.name+"`");
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

}





