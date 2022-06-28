package com.osiris.velocityauth.database;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class BannedUser {
    private static final java.sql.Connection con;
    private static final java.util.concurrent.atomic.AtomicInteger idCounter = new java.util.concurrent.atomic.AtomicInteger(0);

    static {
        try {
            con = java.sql.DriverManager.getConnection(Database.url, Database.username, Database.password);
            try (Statement s = con.createStatement()) {
                s.executeUpdate("CREATE TABLE IF NOT EXISTS `BannedUser` (id INT NOT NULL PRIMARY KEY)");
                try {
                    s.executeUpdate("ALTER TABLE `BannedUser` ADD COLUMN username TEXT NOT NULL");
                } catch (Exception ignored) {
                }
                s.executeUpdate("ALTER TABLE `BannedUser` MODIFY COLUMN username TEXT NOT NULL");
                try {
                    s.executeUpdate("ALTER TABLE `BannedUser` ADD COLUMN ipAddress TEXT NOT NULL");
                } catch (Exception ignored) {
                }
                s.executeUpdate("ALTER TABLE `BannedUser` MODIFY COLUMN ipAddress TEXT NOT NULL");
                try {
                    s.executeUpdate("ALTER TABLE `BannedUser` ADD COLUMN timestampExpires BIGINT NOT NULL");
                } catch (Exception ignored) {
                }
                s.executeUpdate("ALTER TABLE `BannedUser` MODIFY COLUMN timestampExpires BIGINT NOT NULL");
                try {
                    s.executeUpdate("ALTER TABLE `BannedUser` ADD COLUMN uuid TEXT NOT NULL");
                } catch (Exception ignored) {
                }
                s.executeUpdate("ALTER TABLE `BannedUser` MODIFY COLUMN uuid TEXT NOT NULL");
                try {
                    s.executeUpdate("ALTER TABLE `BannedUser` ADD COLUMN reason TEXT");
                } catch (Exception ignored) {
                }
                s.executeUpdate("ALTER TABLE `BannedUser` MODIFY COLUMN reason TEXT");
            }
            try (PreparedStatement ps = con.prepareStatement("SELECT id FROM `BannedUser` ORDER BY id DESC LIMIT 1")) {
                ResultSet rs = ps.executeQuery();
                if (rs.next()) idCounter.set(rs.getInt(1));
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Database field/value. Not null. <br>
     */
    public int id;
    /**
     * Database field/value. Not null. <br>
     */
    public String username;
    /**
     * Database field/value. Not null. <br>
     */
    public String ipAddress;
    /**
     * Database field/value. Not null. <br>
     */
    public long timestampExpires;
    /**
     * Database field/value. Not null. <br>
     */
    public String uuid;
    /**
     * Database field/value. <br>
     */
    public String reason;
    private BannedUser() {
    }
    /**
     * Use the static create method instead of this constructor,
     * if you plan to add this object to the database in the future, since
     * that method fetches and sets/reserves the {@link #id}.
     */
    public BannedUser(int id, String username, String ipAddress, long timestampExpires, String uuid) {
        this.id = id;
        this.username = username;
        this.ipAddress = ipAddress;
        this.timestampExpires = timestampExpires;
        this.uuid = uuid;
    }
    /**
     * Use the static create method instead of this constructor,
     * if you plan to add this object to the database in the future, since
     * that method fetches and sets/reserves the {@link #id}.
     */
    public BannedUser(int id, String username, String ipAddress, long timestampExpires, String uuid, String reason) {
        this.id = id;
        this.username = username;
        this.ipAddress = ipAddress;
        this.timestampExpires = timestampExpires;
        this.uuid = uuid;
        this.reason = reason;
    }

    /**
     * Increments the id and sets it for this object (basically reserves a space in the database).
     *
     * @return object with latest id. Should be added to the database next by you.
     */
    public static BannedUser create(String username, String ipAddress, long timestampExpires, String uuid) {
        int id = idCounter.incrementAndGet();
        BannedUser obj = new BannedUser(id, username, ipAddress, timestampExpires, uuid);
        return obj;
    }

    public static BannedUser create(String username, String ipAddress, long timestampExpires, String uuid, String reason) {
        int id = idCounter.incrementAndGet();
        BannedUser obj = new BannedUser();
        obj.id = id;
        obj.username = username;
        obj.ipAddress = ipAddress;
        obj.timestampExpires = timestampExpires;
        obj.uuid = uuid;
        obj.reason = reason;
        return obj;
    }

    /**
     * @return a list containing all objects in this table.
     */
    public static List<BannedUser> get() throws Exception {
        return get(null);
    }

    /**
     * @return object with the provided id.
     * @throws Exception on SQL issues, or if there is no object with the provided id in this table.
     */
    public static BannedUser get(int id) throws Exception {
        return get("id = " + id).get(0);
    }

    /**
     * Example: <br>
     * get("username=? AND age=?", "Peter", 33);  <br>
     *
     * @param where       can be null. Your SQL WHERE statement (without the leading WHERE).
     * @param whereValues can be null. Your SQL WHERE statement values to set for '?'.
     * @return a list containing only objects that match the provided SQL WHERE statement.
     * if that statement is null, returns all the contents of this table.
     */
    public static List<BannedUser> get(String where, Object... whereValues) throws Exception {
        List<BannedUser> list = new ArrayList<>();
        try (PreparedStatement ps = con.prepareStatement(
                "SELECT id,username,ipAddress,timestampExpires,uuid,reason" +
                        " FROM `BannedUser`" +
                        (where != null ? ("WHERE " + where) : ""))) {
            if (where != null && whereValues != null)
                for (int i = 0; i < whereValues.length; i++) {
                    Object val = whereValues[i];
                    ps.setObject(i + 1, val);
                }
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                BannedUser obj = new BannedUser();
                list.add(obj);
                obj.id = rs.getInt(1);
                obj.username = rs.getString(2);
                obj.ipAddress = rs.getString(3);
                obj.timestampExpires = rs.getLong(4);
                obj.uuid = rs.getString(5);
                obj.reason = rs.getString(6);
            }
        }
        return list;
    }

    /**
     * Searches the provided object in the database (by its id),
     * and updates all its fields.
     *
     * @throws Exception when failed to find by id.
     */
    public static void update(BannedUser obj) throws Exception {
        try (PreparedStatement ps = con.prepareStatement(
                "UPDATE `BannedUser` SET id=?,username=?,ipAddress=?,timestampExpires=?,uuid=?,reason=?")) {
            ps.setInt(1, obj.id);
            ps.setString(2, obj.username);
            ps.setString(3, obj.ipAddress);
            ps.setLong(4, obj.timestampExpires);
            ps.setString(5, obj.uuid);
            ps.setString(6, obj.reason);
            ps.executeUpdate();
        }
    }

    /**
     * Adds the provided object to the database (note that the id is not checked for duplicates).
     */
    public static void add(BannedUser obj) throws Exception {
        try (PreparedStatement ps = con.prepareStatement(
                "INSERT INTO `BannedUser` (id,username,ipAddress,timestampExpires,uuid,reason) VALUES (?,?,?,?,?,?)")) {
            ps.setInt(1, obj.id);
            ps.setString(2, obj.username);
            ps.setString(3, obj.ipAddress);
            ps.setLong(4, obj.timestampExpires);
            ps.setString(5, obj.uuid);
            ps.setString(6, obj.reason);
            ps.executeUpdate();
        }
    }

    /**
     * Deletes the provided object from the database.
     */
    public static void remove(BannedUser obj) throws Exception {
        remove("id = " + obj.id);
    }

    /**
     * Example: <br>
     * remove("username=?", "Peter"); <br>
     * Deletes the objects that are found by the provided SQL WHERE statement, from the database.
     *
     * @param whereValues can be null. Your SQL WHERE statement values to set for '?'.
     */
    public static void remove(String where, Object... whereValues) throws Exception {
        java.util.Objects.requireNonNull(where);
        try (PreparedStatement ps = con.prepareStatement(
                "DELETE FROM `BannedUser` WHERE " + where)) {
            if (whereValues != null)
                for (int i = 0; i < whereValues.length; i++) {
                    Object val = whereValues[i];
                    ps.setObject(i + 1, val);
                }
            ps.executeUpdate();
        }
    }

    public static boolean isBanned(String uuid, String ipAddress) throws Exception {
        return getBannedUUIDs(uuid).isEmpty() && getBannedIpAddresses(ipAddress).isEmpty();
    }

    public static List<BannedUser> getBannedUUIDs(String uuid) throws Exception {
        return get("uuid=? AND timestampExpires>?", uuid, System.currentTimeMillis());
    }

    public static List<BannedUser> getBannedUsernames(String username) throws Exception {
        return get("username=? AND timestampExpires>?", username, System.currentTimeMillis());
    }

    public static List<BannedUser> getBannedIpAddresses(String ipAddress) throws Exception {
        return get("ipAddress=? AND timestampExpires>?", ipAddress, System.currentTimeMillis());
    }

    public BannedUser clone() {
        return new BannedUser(this.id, this.username, this.ipAddress, this.timestampExpires, this.uuid, this.reason);
    }
    public String toPrintString(){
        return  ""+"id="+this.id+" "+"username="+this.username+" "+"ipAddress="+this.ipAddress+" "+"timestampExpires="+this.timestampExpires+" "+"uuid="+this.uuid+" "+"reason="+this.reason+" ";
    }
}

