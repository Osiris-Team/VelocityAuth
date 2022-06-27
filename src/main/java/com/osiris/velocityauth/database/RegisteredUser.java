package com.osiris.velocityauth.database;

import java.util.List;
import java.util.ArrayList;
import java.sql.*;

public class RegisteredUser{
    private static java.sql.Connection con;
    private static java.util.concurrent.atomic.AtomicInteger idCounter = new java.util.concurrent.atomic.AtomicInteger(0);
    static {
        try{
            con = java.sql.DriverManager.getConnection(Database.url, Database.username, Database.password);
            try (Statement s = con.createStatement()) {
                s.executeUpdate("CREATE TABLE IF NOT EXISTS `RegisteredUser` (id INT NOT NULL PRIMARY KEY)");
                try{s.executeUpdate("ALTER TABLE `RegisteredUser` ADD COLUMN username TEXT NOT NULL");}catch(Exception ignored){}
                s.executeUpdate("ALTER TABLE `RegisteredUser` MODIFY COLUMN username TEXT NOT NULL");
                try{s.executeUpdate("ALTER TABLE `RegisteredUser` ADD COLUMN password TEXT NOT NULL");}catch(Exception ignored){}
                s.executeUpdate("ALTER TABLE `RegisteredUser` MODIFY COLUMN password TEXT NOT NULL");
            }
            try (PreparedStatement ps = con.prepareStatement("SELECT id FROM `RegisteredUser` ORDER BY id DESC LIMIT 1")) {
                ResultSet rs = ps.executeQuery();
                if (rs.next()) idCounter.set(rs.getInt(1));
            }
        }
        catch(Exception e){ throw new RuntimeException(e); }
    }
    private RegisteredUser(){}
    /**
     Use the static create method instead of this constructor,
     if you plan to add this object to the database in the future, since
     that method fetches and sets/reserves the {@link #id}.
     */
    public RegisteredUser (int id, String username, String password){
        this.id = id;this.username = username;this.password = password;
    }
    /**
     Database field/value. Not null. <br>
     */
    public int id;
    /**
     Database field/value. Not null. <br>
     */
    public String username;
    /**
     Database field/value. Not null. <br>
     */
    public String password;
    /**
     Increments the id and sets it for this object (basically reserves a space in the database).
     @return object with latest id. Should be added to the database next by you.
     */
    public static RegisteredUser create( String username, String password) {
        int id = idCounter.incrementAndGet();
        RegisteredUser obj = new RegisteredUser(id, username, password);
        return obj;
    }

    /**
     @return a list containing all objects in this table.
     */
    public static List<RegisteredUser> get() throws Exception {return get(null);}
    /**
     @return object with the provided id.
     @throws Exception on SQL issues, or if there is no object with the provided id in this table.
     */
    public static RegisteredUser get(int id) throws Exception {
        return get("id = "+id).get(0);
    }
    /**
     Example: <br>
     get("username=? AND age=?", "Peter", 33);  <br>
     @param where can be null. Your SQL WHERE statement (without the leading WHERE).
     @param whereValues can be null. Your SQL WHERE statement values to set for '?'.
     @return a list containing only objects that match the provided SQL WHERE statement.
     if that statement is null, returns all the contents of this table.
     */
    public static List<RegisteredUser> get(String where, Object... whereValues) throws Exception {
        List<RegisteredUser> list = new ArrayList<>();
        try (PreparedStatement ps = con.prepareStatement(
                "SELECT id,username,password" +
                        " FROM `RegisteredUser`" +
                        (where != null ? ("WHERE "+where) : ""))) {
            if(where!=null && whereValues!=null)
                for (int i = 0; i < whereValues.length; i++) {
                    Object val = whereValues[i];
                    ps.setObject(i+1, val);
                }
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                RegisteredUser obj = new RegisteredUser();
                list.add(obj);
                obj.id = rs.getInt(1);
                obj.username = rs.getString(2);
                obj.password = rs.getString(3);
            }
        }
        return list;
    }

    /**
     Searches the provided object in the database (by its id),
     and updates all its fields.
     @throws Exception when failed to find by id.
     */
    public static void update(RegisteredUser obj) throws Exception {
        try (PreparedStatement ps = con.prepareStatement(
                "UPDATE `RegisteredUser` SET id=?,username=?,password=?")) {
            ps.setInt(1, obj.id);
            ps.setString(2, obj.username);
            ps.setString(3, obj.password);
            ps.executeUpdate();
        }
    }

    /**
     Adds the provided object to the database (note that the id is not checked for duplicates).
     */
    public static void add(RegisteredUser obj) throws Exception {
        try (PreparedStatement ps = con.prepareStatement(
                "INSERT INTO `RegisteredUser` (id,username,password) VALUES (?,?,?)")) {
            ps.setInt(1, obj.id);
            ps.setString(2, obj.username);
            ps.setString(3, obj.password);
            ps.executeUpdate();
        }
    }

    /**
     Deletes the provided object from the database.
     */
    public static void remove(RegisteredUser obj) throws Exception {
        remove("id = "+obj.id);
    }
    /**
     Example: <br>
     remove("username=?", "Peter"); <br>
     Deletes the objects that are found by the provided SQL WHERE statement, from the database.
     @param whereValues can be null. Your SQL WHERE statement values to set for '?'.
     */
    public static void remove(String where, Object... whereValues) throws Exception {
        java.util.Objects.requireNonNull(where);
        try (PreparedStatement ps = con.prepareStatement(
                "DELETE FROM `RegisteredUser` WHERE "+where)) {
            if(whereValues != null)
                for (int i = 0; i < whereValues.length; i++) {
                    Object val = whereValues[i];
                    ps.setObject(i+1, val);
                }
            ps.executeUpdate();
        }
    }

    public RegisteredUser clone(){
        return new RegisteredUser(this.id,this.username,this.password);
    }
}
