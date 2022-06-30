package com.osiris.velocityauth.database;

import java.util.List;
import java.util.ArrayList;
import java.sql.*;

public class Session{
    private static java.sql.Connection con;
    private static java.util.concurrent.atomic.AtomicInteger idCounter = new java.util.concurrent.atomic.AtomicInteger(0);
    static {
        try{
            con = java.sql.DriverManager.getConnection(Database.url, Database.username, Database.password);
            try (Statement s = con.createStatement()) {
                s.executeUpdate("CREATE TABLE IF NOT EXISTS `Session` (id INT NOT NULL PRIMARY KEY)");
                try{s.executeUpdate("ALTER TABLE `Session` ADD COLUMN userId INT NOT NULL");}catch(Exception ignored){}
                s.executeUpdate("ALTER TABLE `Session` MODIFY COLUMN userId INT NOT NULL");
                try{s.executeUpdate("ALTER TABLE `Session` ADD COLUMN ipAddress TEXT NOT NULL");}catch(Exception ignored){}
                s.executeUpdate("ALTER TABLE `Session` MODIFY COLUMN ipAddress TEXT NOT NULL");
                try{s.executeUpdate("ALTER TABLE `Session` ADD COLUMN timestampExpires BIGINT NOT NULL");}catch(Exception ignored){}
                s.executeUpdate("ALTER TABLE `Session` MODIFY COLUMN timestampExpires BIGINT NOT NULL");
                try{s.executeUpdate("ALTER TABLE `Session` ADD COLUMN isActive TINYINT");}catch(Exception ignored){}
                s.executeUpdate("ALTER TABLE `Session` MODIFY COLUMN isActive TINYINT");
                try{s.executeUpdate("ALTER TABLE `Session` ADD COLUMN username TEXT NOT NULL");}catch(Exception ignored){}
                s.executeUpdate("ALTER TABLE `Session` MODIFY COLUMN username TEXT NOT NULL");
            }
            try (PreparedStatement ps = con.prepareStatement("SELECT id FROM `Session` ORDER BY id DESC LIMIT 1")) {
                ResultSet rs = ps.executeQuery();
                if (rs.next()) idCounter.set(rs.getInt(1) + 1);
            }
        }
        catch(Exception e){ throw new RuntimeException(e); }
    }
    private Session(){}
    /**
     Use the static create method instead of this constructor,
     if you plan to add this object to the database in the future, since
     that method fetches and sets/reserves the {@link #id}.
     */
    public Session (int id, int userId, String ipAddress, long timestampExpires, String username){
        this.id = id;this.userId = userId;this.ipAddress = ipAddress;this.timestampExpires = timestampExpires;this.username = username;
    }
    /**
     Use the static create method instead of this constructor,
     if you plan to add this object to the database in the future, since
     that method fetches and sets/reserves the {@link #id}.
     */
    public Session (int id, int userId, String ipAddress, long timestampExpires, byte isActive, String username){
        this.id = id;this.userId = userId;this.ipAddress = ipAddress;this.timestampExpires = timestampExpires;this.isActive = isActive;this.username = username;
    }
    /**
     Database field/value. Not null. <br>
     */
    public int id;
    /**
     Database field/value. Not null. <br>
     */
    public int userId;
    /**
     Database field/value. Not null. <br>
     */
    public String ipAddress;
    /**
     Database field/value. Not null. <br>
     */
    public long timestampExpires;
    /**
     Database field/value. <br>
     */
    public byte isActive;
    /**
     Database field/value. Not null. <br>
     */
    public String username;
    /**
     Increments the id and sets it for this object (basically reserves a space in the database).
     @return object with latest id. Should be added to the database next by you.
     */
    public static Session create( int userId, String ipAddress, long timestampExpires, String username) {
        int id = idCounter.getAndIncrement();
        Session obj = new Session(id, userId, ipAddress, timestampExpires, username);
        return obj;
    }

    public static Session create( int userId, String ipAddress, long timestampExpires, byte isActive, String username) {
        int id = idCounter.getAndIncrement();
        Session obj = new Session();
        obj.id=id; obj.userId=userId; obj.ipAddress=ipAddress; obj.timestampExpires=timestampExpires; obj.isActive=isActive; obj.username=username;
        return obj;
    }

    /**
     @return a list containing all objects in this table.
     */
    public static List<Session> get() throws Exception {return get(null);}
    /**
     @return object with the provided id.
     @throws Exception on SQL issues, or if there is no object with the provided id in this table.
     */
    public static Session get(int id) throws Exception {
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
    public static List<Session> get(String where, Object... whereValues) throws Exception {
        List<Session> list = new ArrayList<>();
        try (PreparedStatement ps = con.prepareStatement(
                "SELECT id,userId,ipAddress,timestampExpires,isActive,username" +
                        " FROM `Session`" +
                        (where != null ? ("WHERE "+where) : ""))) {
            if(where!=null && whereValues!=null)
                for (int i = 0; i < whereValues.length; i++) {
                    Object val = whereValues[i];
                    ps.setObject(i+1, val);
                }
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                Session obj = new Session();
                list.add(obj);
                obj.id = rs.getInt(1);
                obj.userId = rs.getInt(2);
                obj.ipAddress = rs.getString(3);
                obj.timestampExpires = rs.getLong(4);
                obj.isActive = rs.getByte(5);
                obj.username = rs.getString(6);
            }
        }
        return list;
    }

    /**
     Searches the provided object in the database (by its id),
     and updates all its fields.
     @throws Exception when failed to find by id.
     */
    public static void update(Session obj) throws Exception {
        try (PreparedStatement ps = con.prepareStatement(
                "UPDATE `Session` SET id=?,userId=?,ipAddress=?,timestampExpires=?,isActive=?,username=?")) {
            ps.setInt(1, obj.id);
            ps.setInt(2, obj.userId);
            ps.setString(3, obj.ipAddress);
            ps.setLong(4, obj.timestampExpires);
            ps.setByte(5, obj.isActive);
            ps.setString(6, obj.username);
            ps.executeUpdate();
        }
    }

    /**
     Adds the provided object to the database (note that the id is not checked for duplicates).
     */
    public static void add(Session obj) throws Exception {
        try (PreparedStatement ps = con.prepareStatement(
                "INSERT INTO `Session` (id,userId,ipAddress,timestampExpires,isActive,username) VALUES (?,?,?,?,?,?)")) {
            ps.setInt(1, obj.id);
            ps.setInt(2, obj.userId);
            ps.setString(3, obj.ipAddress);
            ps.setLong(4, obj.timestampExpires);
            ps.setByte(5, obj.isActive);
            ps.setString(6, obj.username);
            ps.executeUpdate();
        }
    }

    /**
     Deletes the provided object from the database.
     */
    public static void remove(Session obj) throws Exception {
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
                "DELETE FROM `Session` WHERE "+where)) {
            if(whereValues != null)
                for (int i = 0; i < whereValues.length; i++) {
                    Object val = whereValues[i];
                    ps.setObject(i+1, val);
                }
            ps.executeUpdate();
        }
    }

    public Session clone(){
        return new Session(this.id,this.userId,this.ipAddress,this.timestampExpires,this.isActive,this.username);
    }
    public String toPrintString(){
        return  ""+"id="+this.id+" "+"userId="+this.userId+" "+"ipAddress="+this.ipAddress+" "+"timestampExpires="+this.timestampExpires+" "+"isActive="+this.isActive+" "+"username="+this.username+" ";
    }
}
