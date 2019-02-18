package gov.nasa.gsfc.seadas.ocsswrest.database;

import java.io.File;
import java.io.InputStream;
import java.sql.*;
import java.util.ArrayList;

/**
 * Created by IntelliJ IDEA.
 * User: Aynur Abdurazik (aabduraz)
 * Date: 2/5/15
 * Time: 5:30 PM
 * To change this template use File | Settings | File Templates.
 */

public class SQLiteJDBC {

    public static String JOB_DB_FILENAME = "ocssw.db";
    public static String PROCESS_STDOUT_DB_FILENAME = "process_stdout.db";
    public static String PROCESS_STDERR_DB_FILENAME = "process_stderr.db";
    public static String JOB_DB_URL = "jdbc:sqlite:" + JOB_DB_FILENAME;
    public static String PROCESS_STDOUT_DB_URL = "jdbc:sqlite:" + PROCESS_STDOUT_DB_FILENAME;
    public static String PROCESS_STDERR_DB_URL = "jdbc:sqlite:" + PROCESS_STDERR_DB_FILENAME;
    public static String DB_CLASS_FOR_NAME = "org.sqlite.JDBC";
    private static String username = "obpg";
    private static String password = "obpg";

    public static final String FILE_TABLE_NAME = "FILE_TABLE";
    public static final String MISSION_TABLE_NAME = "MISSION_TABLE";
    public static final String PROCESS_TABLE_NAME = "PROCESS_TABLE";
    public static final String LONLAT_TABLE_NAME = "LONLAT_2_PIXEL_TABLE";
    public static final String PROCESS_MONITOR_STDOUT_TABLE_NAME = "PROCESS_MONITOR_STDOUT_TABLE";
    public static final String PROCESS_MONITOR_STDERR_TABLE_NAME = "PROCESS_MONITOR_STDERR_TABLE";
    public static final String INPUT_FILES_LIST_TABLE_NAME = "INPUT_FILES_LIST_TABLE";

    public static final String JOB_ID_FIELD_NAME = "JOB_ID";
    public static final String IFILE_NAME_FIELD_NAME = "I_FILE_NAME";
    public static final String IFILE_TYPE_FIELD_NAME = "I_FILE_TYPE";
    public static final String OFILE_NAME_FIELD_NAME = "O_FILE_NAME";

    public static final String PROCESS_STATUS_NONEXIST = "-100";
    public static final String PROCESS_STATUS_STARTED = "-1";
    public static final String PROCESS_STATUS_COMPLETED = "0";
    public static final String PROCESS_STATUS_FAILED = "1";

    public enum FileTableFields {
        JOB_ID_NAME("JOB_ID"),
        CLIENT_ID_NAME("CLIENT_ID_NAME"),
        WORKING_DIR_PATH("WORKING_DIR_PATH"),
        I_FILE_NAME("I_FILE_NAME"),
        I_FILE_TYPE("I_FILE_TYPE"),
        O_FILE_NAME("O_FILE_NAME"),
        PROGRAM_NAME("PROGRAM_NAME"),
        MISSION_NAME("MISSION_NAME"),
        MISSION_DIR("MISSION_DIR");

        String fieldName;

        FileTableFields(String fieldName) {
            this.fieldName = fieldName;
        }

        public String getFieldName() {
            return fieldName;
        }
    }

    public enum ProcessStatusFlag{
        NONEXIST(PROCESS_STATUS_NONEXIST),
        STARTED(PROCESS_STATUS_STARTED),
        COMPLETED(PROCESS_STATUS_COMPLETED),
        FAILED(PROCESS_STATUS_FAILED);

        String value;

        ProcessStatusFlag(String value) {
            this.value = value;
        }

        public String getValue(){
            return value;
        }
    }

    public enum ProcessTableFields {
        JOB_ID_NAME("JOB_ID"),
        COMMAND_ARRAY_NAME("COMMAND_ARRAY"),
        STATUS("STATUS"),
        EXIT_VALUE_NAME("EXIT_VALUE"),
        STD_OUT_NAME("stdout"),
        STD_ERR_NAME("stderr"),
        INPUTSTREAM("INPUT_STREAM"),
        ERRORSTREAM("ERROR_STREAM");

        String fieldName;

        ProcessTableFields(String fieldName) {
            this.fieldName = fieldName;
        }
        public String getFieldName() {
            return fieldName;
        }
    }

    public enum LonLatTableFields{
        SLINE_FIELD_NAME("sline"),
        ELINE_FIELD_NAME("eline"),
        SPIXL_FIELD_NAME("spixl"),
        EPIXL_FIELD_NAME("epixl");

        String value;

        LonLatTableFields(String value) {
            this.value = value;
        }

        public String getValue(){
            return value;
        }
    }

    public SQLiteJDBC() {

    }

    public static void main(String args[]) {
        createTables();
        //System.out.println("Opened database successfully");
    }

    public static void createTables() {

        if (new File(JOB_DB_FILENAME).exists()) {
            new File(JOB_DB_FILENAME).delete();
        }
        Connection connection = null;
        Statement stmt = null;
        PreparedStatement preparedStatement = null;

        try {
            Class.forName(DB_CLASS_FOR_NAME);
            connection = DriverManager.getConnection(JOB_DB_URL, username, password);

            stmt = connection.createStatement();

            //string for creating PROCESS_TABLE
            String processor_table_sql = "CREATE TABLE IF NOT EXISTS PROCESS_TABLE " +
                    "(JOB_ID CHAR(50) PRIMARY KEY     NOT NULL, " +
                    " COMMAND_ARRAY  CHAR(500), " +
                    " EXIT_VALUE        CHAR(2), " +
                    " STATUS        CHAR(50), " +
                    " stdout CHAR(500), " +
                    " stderr CHAR(500), " +
                    " INPUT_STREAM BLOB , " +
                    " OUTPUT_STREAM BLOB )";

            //string for creating FILE_TABLE
            String file_table_sql = "CREATE TABLE IF NOT EXISTS FILE_TABLE ( " +
                    "JOB_ID CHAR(50) PRIMARY KEY     NOT NULL, " +
                    " CLIENT_ID_NAME       CHAR(100)  , " +
                    " WORKING_DIR_PATH       CHAR(100)  , " +
                    " PROGRAM_NAME  CHAR(25)   , " +
                    " I_FILE_NAME       CHAR(100)  , " +
                    "I_FILE_TYPE      CHAR(50) ,    " +
                    "O_FILE_NAME      CHAR(100) , " +
                    "MISSION_NAME  CHAR(50), " +
                    "MISSION_DIR  CHAR(50) " +
                    ")";

            //string for creating NEXT_LEVELE_FILE_NAME_TABLE
            String file_info_table_sql = "CREATE TABLE IF NOT EXISTS FILE_TABLE " +
                    "(JOB_ID CHAR(50)    NOT NULL, " +
                    " CLIENT_ID_NAME       CHAR(100)  , " +
                    " WORKING_DIR_PATH       CHAR(100)  , " +
                    " PROGRAM_NAME  CHAR(25)   , " +
                    " I_FILE_NAME       CHAR(100) NOT NULL , " +
                    " I_FILE_TYPE      CHAR(50) ,    " +
                    " O_FILE_NAME      CHAR(100) ,  " +
                    " MISSION_NAME  CHAR(50),  " +
                    " PRIMARY KEY(JOB_ID, I_FILE_NAME)"  +
                    " );";


            //string for creating LONLAT_2_PIXEL_TABLE
            String lonlat_2_pixel_table_sql = "CREATE TABLE IF NOT EXISTS LONLAT_2_PIXEL_TABLE " +
                    "(JOB_ID CHAR(50) PRIMARY KEY     NOT NULL, " +
                    "SPIXL      CHAR(50) ,    " +
                    "EPIXL     CHAR(100) , " +
                    "SLINE  CHAR(50), " +
                    "ELINE       CHAR(50), " +
                    "PIX_SUB     CHAR(100), " +
                    "SC_SUB  CHAR(50), " +
                    "PRODLIST       CHAR(50) )";

            String input_files_list_table_sql = "CREATE TABLE IF NOT EXISTS INPUT_FILES_LIST_TABLE " +
                    "(FILE_ID INTEGER   NOT NULL , " +
                    "JOB_ID CHAR(50)   NOT NULL, " +
                    "FILENAME       STRING," +
                    "PRIMARY KEY (FILE_ID)," +
                    "FOREIGN KEY (JOB_ID) REFERENCES FILE_TABLE(JOB_ID)" +
                    " );";
//
//            String lonlat_table_sql = "CREATE TABLE IF NOT EXISTS LONLAT2PIXEL_TABLE " +
//                    "(LONLAT_ID INTEGER   NOT NULL , " +
//                    "JOB_ID CHAR(50)   NOT NULL, " +
//                    "FILENAME       STRING," +
//                    "PRIMARY KEY (FILE_ID)," +
//                    "FOREIGN KEY (JOB_ID) REFERENCES FILE_TABLE(JOB_ID)" +
//                    " );";


            //execute create_table statements
            stmt.executeUpdate(processor_table_sql);
            stmt.executeUpdate(file_table_sql);
            stmt.executeUpdate(file_info_table_sql);
            stmt.executeUpdate(lonlat_2_pixel_table_sql);
            stmt.executeUpdate(input_files_list_table_sql);


            //string for creating MISSION_TABLE
            String mission_table_sql = "CREATE TABLE IF NOT EXISTS MISSION_TABLE " +
                    "(MISSION_ID CHAR(50) PRIMARY KEY     NOT NULL, " +
                    " MISSION_NAMES           CHAR(50)    NOT NULL, " +
                    " MISSION_DIR            CHAR(50)     NOT NULL)";


            stmt.executeUpdate(mission_table_sql);

            String insertMissionTableSQL = "INSERT INTO MISSION_TABLE " + " (MISSION_ID, MISSION_NAMES, MISSION_DIR) VALUES (" + "?, ?, ?)";

            preparedStatement = connection.prepareStatement(insertMissionTableSQL);

            //1. insert aquarius mission info
            preparedStatement.setString(1, "AQUARIUS");
            preparedStatement.setString(2, "AQUARIUS");
            preparedStatement.setString(3, "aquarius");
            preparedStatement.executeUpdate();

            //2. insert czcs mission info
            preparedStatement.setString(1, "CZCS");
            preparedStatement.setString(2, "CZCS");
            preparedStatement.setString(3, "czcs");
            preparedStatement.executeUpdate();

            //3. insert hico mission info
            preparedStatement.setString(1, "HICO");
            preparedStatement.setString(2, "HICO");
            preparedStatement.setString(3, "hico");
            preparedStatement.executeUpdate();

            //4. insert goci mission info
            preparedStatement.setString(1, "GOCI");
            preparedStatement.setString(2, "GOCI");
            preparedStatement.setString(3, "goci");
            preparedStatement.executeUpdate();

            //5. insert meris mission info
            preparedStatement.setString(1, "MERIS");
            preparedStatement.setString(2, "MERIS");
            preparedStatement.setString(3, "meris");
            preparedStatement.executeUpdate();

            //6. insert modis aqua mission info
            preparedStatement.setString(1, "MODISA");
            preparedStatement.setString(2, "MODIS Aqua AQUA MODISA");
            preparedStatement.setString(3, "modis/aqua");
            preparedStatement.executeUpdate();

            //7. insert modis terra mission info
            preparedStatement.setString(1, "MODIST");
            preparedStatement.setString(2, "MODIS Terra TERRA MODIST");
            preparedStatement.setString(3, "modis/terra");
            preparedStatement.executeUpdate();

            //8. insert mos mission info
            preparedStatement.setString(1, "MOS");
            preparedStatement.setString(2, "MOS");
            preparedStatement.setString(3, "mos");
            preparedStatement.executeUpdate();

            //9. insert msi mission info
            preparedStatement.setString(1, "MSI");
            preparedStatement.setString(2, "MSI");
            preparedStatement.setString(3, "msi");
            preparedStatement.executeUpdate();

            //10. insert octs mission info
            preparedStatement.setString(1, "OCTS");
            preparedStatement.setString(2, "OCTS");
            preparedStatement.setString(3, "octs");
            preparedStatement.executeUpdate();

            //11. insert osmi mission info
            preparedStatement.setString(1, "OSMI");
            preparedStatement.setString(2, "OSMI");
            preparedStatement.setString(3, "osmi");
            preparedStatement.executeUpdate();

            //12. insert seawifs mission info
            preparedStatement.setString(1, "SEAWIFS");
            preparedStatement.setString(2, "SEAWIFS SeaWiFS");
            preparedStatement.setString(3, "seawifs");
            preparedStatement.executeUpdate();

            //13. insert viirs mission info
            preparedStatement.setString(1, "VIIRSN");
            preparedStatement.setString(2, "VIIRS VIIRSN");
            preparedStatement.setString(3, "viirs/npp");
            preparedStatement.executeUpdate();

            //14. insert ocm1 mission info
            preparedStatement.setString(1, "OCM1");
            preparedStatement.setString(2, "OCM1");
            preparedStatement.setString(3, "ocm1");
            preparedStatement.executeUpdate();

            //15. insert ocm2 mission info
            preparedStatement.setString(1, "OCM2");
            preparedStatement.setString(2, "OCM2");
            preparedStatement.setString(3, "ocm2");
            preparedStatement.executeUpdate();

            //16. insert oli mission info
            preparedStatement.setString(1, "OLI");
            preparedStatement.setString(2, "OLI");
            preparedStatement.setString(3, "oli");
            preparedStatement.executeUpdate();

            //16. insert olci mission info
            preparedStatement.setString(1, "OLCI");
            preparedStatement.setString(2, "OLCI");
            preparedStatement.setString(3, "olci");
            preparedStatement.executeUpdate();

            //17. insert viirs mission info
            preparedStatement.setString(1, "VIIRSJ1");
            preparedStatement.setString(2, "VIIRSJ1");
            preparedStatement.setString(3, "viirs/j1");
            preparedStatement.executeUpdate();


            stmt.close();
            preparedStatement.close();
            connection.close();

        } catch (Exception e) {
            System.err.println(e.getClass().getName() + ": " + e.getMessage());
            System.exit(0);
        }
        //System.out.println("Tables created successfully");

    }

    public static String retrieveMissionDir(String missionName) {
        Connection connection = null;
        PreparedStatement preparedStatement = null;
        Statement statement = null;
        String commonQueryString = "SELECT * FROM MISSION_TABLE WHERE MISSION_ID = ?";

        String missionDir = null;

        try {
            Class.forName(DB_CLASS_FOR_NAME);
            connection = DriverManager.getConnection(JOB_DB_URL);
            connection.setAutoCommit(false);
            //System.out.println("Opened database successfully");

            statement = connection.createStatement();
            ResultSet rs = statement.executeQuery("SELECT * FROM MISSION_TABLE;");
            while (rs.next()) {
                String missionNames = rs.getString("MISSION_NAMES");
                System.out.println("MISSION NAMES = " + missionNames);
                if (missionNames.contains(missionName)) {
                    missionDir = rs.getString("MISSION_DIR");
                    System.out.println("MISSION DIR = " + missionDir);
                    break;
                }
            }
            rs.close();
            statement.close();
            connection.close();
        } catch (Exception e) {
            System.err.println(e.getClass().getName() + ": " + e.getMessage());
            System.exit(0);
        }

        return missionDir;
    }

    public static void insertItem(String tableName, String itemName, String itemValue) {
        Connection connection = null;
        PreparedStatement preparedStatement = null;

        String commonInsertString = "INSERT INTO " + tableName + " (" + itemName + ") VALUES ( ? );";

        try {
            Class.forName(DB_CLASS_FOR_NAME);
            connection = DriverManager.getConnection(JOB_DB_URL);
            connection.setAutoCommit(false);
            preparedStatement = connection.prepareStatement(commonInsertString);
            preparedStatement.setString(1, itemValue);
            // execute insert SQL stetement
            int exitCode = preparedStatement.executeUpdate();

            connection.commit();

            //System.out.println(itemName + " is " + (exitCode == 1 ? "" : "not") + " inserted into " + tableName + " table!");
            preparedStatement.close();
            connection.close();

        } catch (Exception e) {
            System.err.println(" in inserting item : " + e.getClass().getName() + ": " + e.getMessage());
            //System.exit(0);
        }
        //System.out.println("Inserted " + itemName + " successfully");
    }

    public static String updateItem(String tableName, String jobID, String itemName, String itemValue) {
        Connection connection = null;
        PreparedStatement preparedStatement = null;
        String commonUpdateString = "UPDATE " + tableName + " SET " + itemName + " = ?  WHERE JOB_ID = ?";
        String retrievedItem = null;

        try {
            Class.forName(DB_CLASS_FOR_NAME);
            connection = DriverManager.getConnection(JOB_DB_URL);
            connection.setAutoCommit(false);
            //System.out.println("Operating on table " + tableName);

            preparedStatement = connection.prepareStatement(commonUpdateString);

            preparedStatement.setString(1, itemValue);
            preparedStatement.setString(2, jobID);
            int exitCode = preparedStatement.executeUpdate();
            connection.commit();

            //System.out.println(itemName + " is " + (exitCode == 1 ? "" : "not") + " updated on " + tableName + " table!");
            //System.out.println(itemName + " = "  + itemValue);
            preparedStatement.close();
            connection.close();

        } catch (Exception e) {
            System.err.println(" in update item : " + e.getClass().getName() + ": " + e.getMessage());
            //System.exit(0);
        }
        //System.out.println("Operation done successfully");

        return retrievedItem;
    }

    public static void insertItemWithDoubleKey(String tableName, String key1, String value1, String key2, String value2) {
        Connection connection = null;
        PreparedStatement preparedStatement = null;

        String commonInsertString = "INSERT INTO " + tableName + " (" + key1 + "," + key2 + ") VALUES ( ?, ? );";

        try {
            Class.forName(DB_CLASS_FOR_NAME);
            connection = DriverManager.getConnection(JOB_DB_URL);
            connection.setAutoCommit(false);
            preparedStatement = connection.prepareStatement(commonInsertString);
            preparedStatement.setString(1, value1);
            preparedStatement.setString(2, value2);
            // execute insert SQL stetement
            int exitCode = preparedStatement.executeUpdate();

            connection.commit();

            //System.out.println(key1 + " is " + (exitCode == 1 ? "" : "not") + " inserted into " + tableName + " table!");
            preparedStatement.close();
            connection.close();

        } catch (Exception e) {
            System.err.println(" in inserting item : " + e.getClass().getName() + ": " + e.getMessage());
            //System.exit(0);
        }
        //System.out.println("Inserted " + key1 + "and " + key2 + " successfully");
    }


    public static String updateItemWithDoubleKey(String tableName, String key1, String keyValue1, String key2, String keyValue2, String itemName, String itemValue) {
        Connection connection = null;
        PreparedStatement preparedStatement = null;
        String commonUpdateString = "UPDATE " + tableName + " SET " + itemName + " = ?  WHERE " + key1 + " = ? AND " + key2 + "=?";
        String retrievedItem = null;

        try {
            Class.forName(DB_CLASS_FOR_NAME);
            connection = DriverManager.getConnection(JOB_DB_URL);
            connection.setAutoCommit(false);
            //System.out.println("Operating on table " + tableName);

            preparedStatement = connection.prepareStatement(commonUpdateString);

            preparedStatement.setString(1, itemValue);
            preparedStatement.setString(2, keyValue1);
            preparedStatement.setString(3, keyValue2);
            int exitCode = preparedStatement.executeUpdate();
            connection.commit();

            //System.out.println(itemName + " is " + (exitCode == 1 ? "" : "not") + " updated on " + tableName + " table!");
            //System.out.println(itemName + " = "  + itemValue);
            preparedStatement.close();
            connection.close();

        } catch (Exception e) {
            System.err.println(" in update item : " + e.getClass().getName() + ": " + e.getMessage());
            //System.exit(0);
        }
        //System.out.println("Operation done successfully");

        return retrievedItem;
    }

    public static ArrayList getInputFilesList(String jobId) {
        Connection connection = null;
        PreparedStatement preparedStatement = null;
        String commonQueryString = "SELECT * FROM " + INPUT_FILES_LIST_TABLE_NAME + " WHERE JOB_ID = ?";

        ArrayList fileList = new ArrayList();

        try {
            Class.forName(DB_CLASS_FOR_NAME);
            connection = DriverManager.getConnection(JOB_DB_URL);
            connection.setAutoCommit(false);
            //System.out.println("Operating on table " + INPUT_FILES_LIST_TABLE_NAME + "  jobID = " + jobId + "  searching for " + "fileName");

            preparedStatement = connection.prepareStatement(commonQueryString);

            //preparedStatement.setString(1, itemName);
            preparedStatement.setString(1, jobId);
            //System.out.println("sql string: " + preparedStatement);

            ResultSet rs = preparedStatement.executeQuery();


            while (rs.next()) {
                fileList.add(rs.getString("FILENAME"));
            }
            rs.close();
            preparedStatement.close();
            connection.close();
        } catch (Exception e) {
            System.err.println(" in retrieve item : " + e.getClass().getName() + ": " + e.getMessage());
            //System.exit(0);
        }
        //System.out.println("Operation done successfully");

        return fileList;
    }

    public static void updateInputFilesList(String jobID, String newClientFileName) {
        Connection connection = null;
        PreparedStatement preparedStatement = null;

        String commonUpdateString = "INSERT INTO " + INPUT_FILES_LIST_TABLE_NAME + " ( JOB_ID, FILENAME )  VALUES ( ? , ? );";

        //System.out.println(commonUpdateString);

        try {
            Class.forName(DB_CLASS_FOR_NAME);
            connection = DriverManager.getConnection(JOB_DB_URL);
            connection.setAutoCommit(false);
            //System.out.println("Operating on table " + INPUT_FILES_LIST_TABLE_NAME);

            preparedStatement = connection.prepareStatement(commonUpdateString);

            preparedStatement.setString(1, jobID);
            preparedStatement.setString(2, newClientFileName);

            int exitCode = preparedStatement.executeUpdate();
            connection.commit();

            //System.out.println(newClientFileName + " is " + (exitCode == 1 ? "" : "not") + " added in " + INPUT_FILES_LIST_TABLE_NAME + " table!");

            preparedStatement.close();
            connection.close();

        } catch (Exception e) {
            System.err.println(" in update item : " + e.getClass().getName() + ": " + e.getMessage());
            //System.exit(0);
        }
    }

    public static String retrieveItem(String tableName, String searchKey, String itemName) {
        Connection connection = null;
        PreparedStatement preparedStatement = null;

        String commonQueryString = "SELECT * FROM " + tableName + " WHERE JOB_ID = ?";

        String retrievedItem = null;

        try {
            Class.forName(DB_CLASS_FOR_NAME);
            connection = DriverManager.getConnection(JOB_DB_URL);
            connection.setAutoCommit(false);
            //System.out.println("Operating on table " + tableName + "  jobID = " + searchKey + " searching for " + itemName);

            preparedStatement = connection.prepareStatement(commonQueryString);
            preparedStatement.setString(1, searchKey);
            ResultSet rs = preparedStatement.executeQuery();
            retrievedItem = rs.getString(itemName);
            //System.out.println("Retrieved item name : " + retrievedItem);
            rs.close();
            preparedStatement.close();
            connection.close();
        } catch (Exception e) {
            System.err.println(" in retrieve item : " + e.getClass().getName() + ": " + e.getMessage());
            //System.exit(0);
        }
        //System.out.println("Operation done successfully");

        return retrievedItem;
    }

    public static InputStream retrieveInputStreamItem(String tableName, String searchKey, String itemName) {
        Connection connection = null;
        PreparedStatement preparedStatement = null;
        String commonQueryString = "SELECT * FROM " + tableName + " WHERE JOB_ID = ?";
        InputStream retrievedItem = null;

        try {
            Class.forName(DB_CLASS_FOR_NAME);
            connection = DriverManager.getConnection(JOB_DB_URL);
            connection.setAutoCommit(false);
            //System.out.println("Operating on table " + tableName + "  jobID = " + searchKey + " searching for " + itemName);

            preparedStatement = connection.prepareStatement(commonQueryString);
            preparedStatement.setString(1, searchKey);
            ResultSet rs = preparedStatement.executeQuery();

            int size= 0;
            if (rs != null)
            {
                rs.beforeFirst();
                rs.last();
                size = rs.getRow();
            }

            if (rs.next()) {
                retrievedItem = rs.getBinaryStream(size);
                //System.out.println("Total retrieved item number : " + rs.getFetchSize());
                //rs.deleteRow();
            } else {
                retrievedItem = null;
            }

            //retrievedItem = rs.getBinaryStream(itemName);
            //System.out.println("Retrieved item name : " + retrievedItem.toString());
            rs.close();
            preparedStatement.close();
            connection.close();
        } catch (Exception e) {
            System.err.println(" in retrieve input stream item : " );
            e.printStackTrace();
        }
        //System.out.println("Operation done successfully");

        return retrievedItem;
    }

     public static String getProgramName(String jobId) {
        return retrieveItem(FILE_TABLE_NAME, jobId, FileTableFields.PROGRAM_NAME.getFieldName());
    }
}
