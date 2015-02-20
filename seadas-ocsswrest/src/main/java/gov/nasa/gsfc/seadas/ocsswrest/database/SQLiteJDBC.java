package gov.nasa.gsfc.seadas.ocsswrest.database;

import java.sql.*;

/**
 * Created by IntelliJ IDEA.
 * User: Aynur Abdurazik (aabduraz)
 * Date: 2/5/15
 * Time: 5:30 PM
 * To change this template use File | Settings | File Templates.
 */

public class SQLiteJDBC {

    public static String JOB_DB_URL = "jdbc:sqlite:ocssw.db";
    public static String DB_CLASS_FOR_NAME = "org.sqlite.JDBC";
    public static String SQL_INSERT_STRING = "INSERT INTO JOBS (JOB_ID,CLIENT_ID,PROCESSOR_ID,COMMAND_ARRAY, STATUS) ";
    private static String username = "aynur";
    private static String password = "aynur";

    public SQLiteJDBC() {

    }

    public void addJob() {
        try {
            Connection db = DriverManager.getConnection(JOB_DB_URL, username, password);
            db.close();
        } catch (SQLException sqle) {

        }

    }

    public static void main(String args[]) {
        Connection c = null;
        Statement stmt = null;
        try {
            Class.forName(DB_CLASS_FOR_NAME);
            c = DriverManager.getConnection(JOB_DB_URL);

            stmt = c.createStatement();
            String sql = "CREATE TABLE IF NOT EXISTS JOBS " +
                    "(JOB_ID INT PRIMARY KEY     NOT NULL, " +
                    " CLIENT_ID           INT    NOT NULL, " +
                    " PROCESSOR_ID            INT     NOT NULL, " +
                    " COMMAND_ARRAY  CHAR(500), " +
                    " STATUS        CHAR(50) )";
            stmt.executeUpdate(sql);
            stmt.close();
            c.close();

        } catch (Exception e) {
            System.err.println(e.getClass().getName() + ": " + e.getMessage());
            System.exit(0);
        }
        System.out.println("Opened database successfully");
    }

    public static void createTable() {
        Connection c = null;
        Statement stmt = null;
        try {
            Class.forName(DB_CLASS_FOR_NAME);
            c = DriverManager.getConnection(JOB_DB_URL, username, password);

            stmt = c.createStatement();
            String sql = "CREATE TABLE IF NOT EXISTS JOBS " +
                    "(JOB_ID INT PRIMARY KEY     NOT NULL, " +
                    " CLIENT_ID           INT    NOT NULL, " +
                    " PROCESSOR_ID            INT     NOT NULL, " +
                    " COMMAND_ARRAY  CHAR(500), " +
                    " STATUS        CHAR(50)) ";
            stmt.executeUpdate(sql);
            stmt.close();
            c.close();

        } catch (Exception e) {
            System.err.println(e.getClass().getName() + ": " + e.getMessage());
            System.exit(0);
        }
        System.out.println("Opened database successfully");

    }

    public static void insertJob(String jobId, String clientId, String processorID, String status, String cmdArray) {
        Connection c = null;
        Statement stmt = null;
        try {
            Class.forName(DB_CLASS_FOR_NAME);
            c = DriverManager.getConnection(JOB_DB_URL, username, password);
            c.setAutoCommit(false);
            System.out.println("Opened database successfully");

            stmt = c.createStatement();
            String sql = SQL_INSERT_STRING +
                    "VALUES (" + jobId + "," + clientId + "," + processorID + "," + status + "," + cmdArray + ");";
            stmt.executeUpdate(sql);

            stmt.close();
            c.commit();
            c.close();
        } catch (Exception e) {
            System.err.println(e.getClass().getName() + ": " + e.getMessage());
            System.exit(0);
        }
        System.out.println("Records created successfully");
    }

    public void selectJob() {
        Connection c = null;
        Statement stmt = null;
        try {
            Class.forName(DB_CLASS_FOR_NAME);
            c = DriverManager.getConnection(JOB_DB_URL, username, password);
            c.setAutoCommit(false);
            System.out.println("Opened database successfully");

            stmt = c.createStatement();
            ResultSet rs = stmt.executeQuery("SELECT * FROM JOBS;");
            while (rs.next()) {
                int id = rs.getInt("id");
                String name = rs.getString("name");
                int age = rs.getInt("age");
                String address = rs.getString("address");
                float salary = rs.getFloat("salary");
                System.out.println("ID = " + id);
                System.out.println("NAME = " + name);
                System.out.println("AGE = " + age);
                System.out.println("ADDRESS = " + address);
                System.out.println("SALARY = " + salary);
                System.out.println();
            }
            rs.close();
            stmt.close();
            c.close();
        } catch (Exception e) {
            System.err.println(e.getClass().getName() + ": " + e.getMessage());
            System.exit(0);
        }
        System.out.println("Operation done successfully");
    }

    public void updateJob() {
        Connection c = null;
        Statement stmt = null;
        try {
            Class.forName(DB_CLASS_FOR_NAME);
            c = DriverManager.getConnection(JOB_DB_URL, username, password);
            c.setAutoCommit(false);
            System.out.println("Opened database successfully");

            stmt = c.createStatement();
            String sql = "UPDATE JOBS set SALARY = 25000.00 where ID=1;";
            stmt.executeUpdate(sql);
            c.commit();

            ResultSet rs = stmt.executeQuery("SELECT * FROM JOBS;");
            while (rs.next()) {
                int id = rs.getInt("id");
                String name = rs.getString("name");
                int age = rs.getInt("age");
                String address = rs.getString("address");
                float salary = rs.getFloat("salary");
                System.out.println("ID = " + id);
                System.out.println("NAME = " + name);
                System.out.println("AGE = " + age);
                System.out.println("ADDRESS = " + address);
                System.out.println("SALARY = " + salary);
                System.out.println();
            }
            rs.close();
            stmt.close();
            c.close();
        } catch (Exception e) {
            System.err.println(e.getClass().getName() + ": " + e.getMessage());
            System.exit(0);
        }
        System.out.println("Operation done successfully");

    }

    public void deleteJob() {
        Connection c = null;
        Statement stmt = null;
        try {
            Class.forName(DB_CLASS_FOR_NAME);
            c = DriverManager.getConnection(JOB_DB_URL, username, password);
            c.setAutoCommit(false);
            System.out.println("Opened database successfully");

            stmt = c.createStatement();
            String sql = "DELETE from JOBS where ID=2;";
            stmt.executeUpdate(sql);
            c.commit();

            ResultSet rs = stmt.executeQuery("SELECT * FROM JOBS;");
            while (rs.next()) {
                int id = rs.getInt("id");
                String name = rs.getString("name");
                int age = rs.getInt("age");
                String address = rs.getString("address");
                float salary = rs.getFloat("salary");
                System.out.println("ID = " + id);
                System.out.println("NAME = " + name);
                System.out.println("AGE = " + age);
                System.out.println("ADDRESS = " + address);
                System.out.println("SALARY = " + salary);
                System.out.println();
            }
            rs.close();
            stmt.close();
            c.close();
        } catch (Exception e) {
            System.err.println(e.getClass().getName() + ": " + e.getMessage());
            System.exit(0);
        }
        System.out.println("Operation done successfully");
    }
}


