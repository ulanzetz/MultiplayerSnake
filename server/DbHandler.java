package com.snakegame.server;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.sqlite.JDBC;

import java.sql.*;
import java.security.*;
import java.util.Objects;

class DbHandler {
    private static final String dbAddress = "jdbc:sqlite:players.s3db";
    private static final String salt ="somesalt";
    static DbHandler dbHandler = new DbHandler();

    private Connection connection;
    private Statement statement;
    private ResultSet resultSet;

    private DbHandler()  {
        try {
            DriverManager.registerDriver(new JDBC());
            connection = DriverManager.getConnection(dbAddress);
            statement = connection.createStatement();
        }
        catch (SQLException e) {
            e.printStackTrace();
            connection = null;
        }
    }

    Integer loadPlayerScore(String name, String passwd) throws SQLException {
        if(incorrectName(name))
            return null;
        resultSet = statement.executeQuery("SELECT passwd, score FROM players WHERE " +
                "name = " + withQuotes(name) + " LIMIT 1");
        String hashedPaswd = MD5(MD5(passwd + salt) + salt);
        if(resultSet.next()) {
            if (!Objects.equals(resultSet.getString("passwd"), hashedPaswd))
                return null;
            return resultSet.getInt("score");
        }
        insertPlayer(name, hashedPaswd);
        return 0;
    }

    private void insertPlayer(String name, String passwd) throws SQLException {
        statement.execute("INSERT INTO players (name, passwd, score) " +
        "VALUES (" + withQuotes(name) + ", " + withQuotes(passwd) + ", " + " '0')");
    }

    void updatePlayer(String name, int score) throws SQLException {
        statement.execute("UPDATE players SET score = " + withQuotes(String.valueOf(score)) + "WHERE name = " +
                withQuotes(name));
    }

    @NotNull
    @Contract(pure = true)
    private static String withQuotes(String s) {
        return "'" + s + "'";
    }

    private static boolean incorrectName(String name) {
        return name.contains("'\"-()");
    }

    @Nullable
    private static String MD5(String s) {
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            byte[] array = md.digest(s.getBytes());
            StringBuilder sb = new StringBuilder();
            for(int i = 0; i != array.length; ++i)
                sb.append(Integer.toHexString((array[i] & 0xFF) | 0x100).substring(1,3));
            return sb.toString();
        }
        catch (NoSuchAlgorithmException ignored) { }
        return null;
    }
}
