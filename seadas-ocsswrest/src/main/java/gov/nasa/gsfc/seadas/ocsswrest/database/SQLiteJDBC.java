package gov.nasa.gsfc.seadas.ocsswrest.database;

import sun.misc.IOUtils;

import java.io.*;
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
    public static String SQL_INSERT_STRING = "INSERT INTO PROCESS_TABLE (JOB_ID,COMMAND_ARRAY, EXIT_VALUE, stdout, stderr) ";
    private static String username = "obpg";
    private static String password = "obpg";

    protected static final String STDOUT = "stdout";
    protected static final String STDERR = "stderr";


    private static String NEXT_LEVEL_NAME_FINDER_PROGRAM_NAME = "next_level_name.py";
    private static String NEXT_LEVEL_FILE_NAME_TOKEN = "Output Name:";

    public static final String FILE_TABLE_NAME = "FILE_TABLE";
    public static final String MISSION_TABLE_NAME = "MISSION_TABLE";
    public static final String PROCESS_TABLE_NAME = "PROCESS_TABLE";
    public static final String PROCESS_MONITOR_STDOUT_TABLE_NAME = "PROCESS_MONITOR_STDOUT_TABLE";
    public static final String PROCESS_MONITOR_STDERR_TABLE_NAME = "PROCESS_MONITOR_STDERR_TABLE";
    public static final String INPUT_FILES_LIST_TABLE_NAME = "INPUT_FILES_LIST_TABLE";

    public static final String JOB_ID_FIELD_NAME = "JOB_ID";
    public static final String IFILE_NAME_FIELD_NAME = "I_FILE_NAME";
    public static final String IFILE_TYPE_FIELD_NAME = "I_FILE_TYPE";
    public static final String OFILE_NAME_FIELD_NAME = "O_FILE_NAME";
    public static final String PROGRAM_NAME_FIELD_NAME = "PROGRAM_NAME";
    public static final String MISSION_NAME_FIELD_NAME = "MISSION_NAME";
    public static final String MISSION_DIR_FIELD_NAME = "MISSION_DIR";

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

    public enum NextLevelFileNameTableFields {
        JOB_ID_NAME("JOB_ID"),
        CLIENT_ID_NAME("CLIENT_ID_NAME"),
        WORKING_DIR_PATH("WORKING_DIR_PATH"),
        I_FILE_NAME("I_FILE_NAME"),
        O_FILE_NAME("O_FILE_NAME"),
        PROGRAM_NAME("PROGRAM_NAME"),
        OPTIONS_NAME("OPTIONS");

        String fieldName;

        NextLevelFileNameTableFields(String fieldName) {
            this.fieldName = fieldName;
        }

        public String getFieldName() {
            return fieldName;
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

    public SQLiteJDBC() {

    }

    public void addJob() {
        try {
            Connection db = DriverManager.getConnection(JOB_DB_URL);
            db.close();
        } catch (SQLException sqle) {

        }

    }

    public static void main(String args[]) {
//        Connection c = null;
//        Statement stmt = null;
//        try {
//            Class.forName(DB_CLASS_FOR_NAME);
//            c = DriverManager.getConnection(JOB_DB_URL);
//
//            stmt = c.createStatement();
//            String processor_table_sql = "CREATE TABLE IF NOT EXISTS PROCESS_TABLE " +
//                    "(JOB_ID INT PRIMARY KEY     NOT NULL, " +
//                    " COMMAND_ARRAY  CHAR(500), " +
//                    " EXIT_VALUE        CHAR(2), " +
//                    " stdout CHAR(500), " +
//                    " stderr(500) )";
//            String file_table_sql = "CREATE TABLE IF NOT EXISTS FILE_TABLE " +
//                    "(JOB_ID INT PRIMARY KEY     NOT NULL, " +
//                    " PROGRAM_NAME  CHAR(25)   NOT NULL, " +
//                    " I_FILE_NAME       CHAR(100)   NOT NULL, " +
//                    " I_FILE_TYPE      CHAR(50)    NOT NULL, " +
//                    " O_FILE_NAME      CHAR(100)    NOT NULL, " +
//                    " MISSION  CHAR(50), " +
//                    " STATUS        CHAR(50) )";
//            stmt.executeUpdate(processor_table_sql);
//            stmt.executeUpdate(file_table_sql);
//            stmt.close();
//            c.close();
//
//        } catch (Exception e) {
//            System.err.println(e.getClass().getName() + ": " + e.getMessage());
//            System.exit(0);
//        }
        createTables();
        System.out.println("Opened database successfully");
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
            String file_table_sql = "CREATE TABLE IF NOT EXISTS FILE_TABLE " +
                    "(JOB_ID CHAR(50) PRIMARY KEY     NOT NULL, " +
                    " CLIENT_ID_NAME       CHAR(100)  , " +
                    " WORKING_DIR_PATH       CHAR(100)  , " +
                    " PROGRAM_NAME  CHAR(25)   , " +
                    " I_FILE_NAME       CHAR(100)  , " +
                    "I_FILE_TYPE      CHAR(50) ,    " +
                    "O_FILE_NAME      CHAR(100) , " +
                    "MISSION_NAME  CHAR(50), " +
                    "MISSION_DIR  CHAR(50) )";

            //string for creating NEXT_LEVELE_FILE_NAME_TABLE
            String next_level_file_name_table_sql = "CREATE TABLE IF NOT EXISTS FILE_TABLE " +
                    "(JOB_ID CHAR(50) PRIMARY KEY     NOT NULL, " +
                    " CLIENT_ID_NAME       CHAR(100)  , " +
                    " WORKING_DIR_PATH       CHAR(100)  , " +
                    " PROGRAM_NAME  CHAR(25)   , " +
                    " I_FILE_NAME       CHAR(100)  , " +
                    "O_FILE_NAME      CHAR(100) , " +
                    "OPTIONS        CHAR(150) )";


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

            //execute create_table statements
            stmt.executeUpdate(processor_table_sql);
            stmt.executeUpdate(file_table_sql);
            stmt.executeUpdate(next_level_file_name_table_sql);
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
            preparedStatement.setString(3, "hmodisa");
            preparedStatement.executeUpdate();

            //7. insert modis terra mission info
            preparedStatement.setString(1, "MODIST");
            preparedStatement.setString(2, "MODIS Terra TERRA MODIST");
            preparedStatement.setString(3, "hmodist");
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
            preparedStatement.setString(1, "VIIRS");
            preparedStatement.setString(2, "VIIRS VIIRSN");
            preparedStatement.setString(3, "viirsn");
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

            stmt.close();
            preparedStatement.close();
            connection.close();

        } catch (Exception e) {
            System.err.println(e.getClass().getName() + ": " + e.getMessage());
            System.exit(0);
        }
        System.out.println("Tables created successfully");

    }

    public static void createProcessMonitorTables(String jobId) {

        //string for creating PROCESS_TABLE

        if (new File(PROCESS_STDOUT_DB_FILENAME).exists()) {
            new File(PROCESS_STDOUT_DB_FILENAME).delete();
        }

        if (new File(PROCESS_STDERR_DB_FILENAME).exists()) {
            new File(PROCESS_STDERR_DB_FILENAME).delete();
        }


        String stdoutTableName = PROCESS_MONITOR_STDOUT_TABLE_NAME + "_" + jobId;
        String stderrTableName = PROCESS_MONITOR_STDERR_TABLE_NAME + "_" + jobId;

        System.out.println("Creating table " + stdoutTableName + " ... ");


        String create_process_monitor_stdout_table_sql = "CREATE TABLE IF NOT EXISTS " + stdoutTableName  +
                "( LINE_ID INTEGER PRIMARY KEY   NOT NULL, "  + STDOUT + " CHAR(500) )";

        String create_process_monitor_stderr_table_sql = "CREATE TABLE IF NOT EXISTS " + stderrTableName +
                "( LINE_ID INTEGER PRIMARY KEY   NOT NULL, " + STDERR + " CHAR(500))";

        String drop_process_monitor_stdout_table_sql = "DROP TABLE " + stdoutTableName;
        String drop_process_monitor_stderr_table_sql = "DROP TABLE " + stderrTableName;

        Connection stdoutConnection = null;
        Connection stderrConnection = null;
        Statement stdoutStatement = null;
        Statement stderrStatement = null;

        try {
            Class.forName(DB_CLASS_FOR_NAME);
            stdoutConnection = DriverManager.getConnection(PROCESS_STDOUT_DB_URL, username, password);
            stdoutStatement = stdoutConnection.createStatement();

            stderrConnection = DriverManager.getConnection(PROCESS_STDERR_DB_URL, username, password);
            stderrStatement = stderrConnection.createStatement();

//            DatabaseMetaData md = stdoutConnection.getMetaData();
//
//            System.out.println( stdoutTableName + " ... "  + md.getDatabaseProductName());
//
//            ResultSet rs = md.getTables(null, null, stdoutTableName, null);
//
//            System.out.println( stdoutTableName + " ... "  + rs.next());
//
//
//            if (rs.next()) {
//                stdoutStatement.executeUpdate(drop_process_monitor_stdout_table_sql);
//                stdoutStatement.executeUpdate(drop_process_monitor_stderr_table_sql);
//
//            }
            stdoutStatement.executeUpdate(create_process_monitor_stdout_table_sql);
            stderrStatement.executeUpdate(create_process_monitor_stderr_table_sql);
            System.out.println("Table " + stdoutTableName + " created successfully");
            System.out.println("Table " + stderrTableName + " created successfully");
            //rs.close();
            stdoutStatement.close();
            stderrStatement.close();
            stdoutConnection.close();
            stderrConnection.close();
            
        } catch (Exception e) {
            System.err.println(e.getClass().getName() + ": " + e.getMessage());
            System.exit(0);
        }
    }

    public static void dropProcessMonitorTables(String jobId) {


        String stdoutTableName = PROCESS_MONITOR_STDOUT_TABLE_NAME + "_" + jobId;
        String stderrTableName = PROCESS_MONITOR_STDERR_TABLE_NAME + "_" + jobId;


        String drop_process_monitor_stdout_table_sql = "DROP TABLE " + stdoutTableName;
        String drop_process_monitor_stderr_table_sql = "DROP TABLE " + stderrTableName;

        Connection connection = null;
        Statement stmt = null;

        try {
            Class.forName(DB_CLASS_FOR_NAME);
            connection = DriverManager.getConnection(JOB_DB_URL, username, password);
            stmt = connection.createStatement();

            DatabaseMetaData md = connection.getMetaData();
            ResultSet rs = md.getTables(null, null, stdoutTableName, null);
            if (rs.next()) {
                stmt.executeUpdate(drop_process_monitor_stdout_table_sql);
                stmt.executeUpdate(drop_process_monitor_stderr_table_sql);

            }
            rs.close();
            stmt.close();
            connection.close();

        } catch (Exception e) {
            System.err.println(e.getClass().getName() + ": " + e.getMessage());
            System.exit(0);
        }
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
            System.out.println("Opened database successfully");

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
            c = DriverManager.getConnection(JOB_DB_URL);
            c.setAutoCommit(false);
            System.out.println("Opened database successfully");

            stmt = c.createStatement();
            ResultSet rs = stmt.executeQuery("SELECT * FROM FILE_TABLE;");
            while (rs.next()) {
                int id = rs.getInt("JOB_ID");
                String fileType = rs.getString("I_FILE_TYPE");
                String o_file_name = rs.getString("O_FILE_NAME");
                System.out.println("ID = " + id);
                System.out.println("NAME = " + fileType);
                System.out.println("AGE = " + o_file_name);
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

    public static void insertItem(String tableName, String itemName, String itemValue) {
        Connection connection = null;
        PreparedStatement preparedStatement = null;

        String commonInsertString = "INSERT INTO " + tableName + " (" + itemName + ") VALUES ( ? );";

        try {
            Class.forName(DB_CLASS_FOR_NAME);
            connection = DriverManager.getConnection(JOB_DB_URL);
            connection.setAutoCommit(false);
            System.out.println("Opened database successfully");
            System.out.println("Operating on table " + tableName);


            preparedStatement = connection.prepareStatement(commonInsertString);

            preparedStatement.setString(1, itemValue);
            System.out.println("sql string: " + commonInsertString + itemValue);

            // execute insert SQL stetement
            int exitCode = preparedStatement.executeUpdate();

            connection.commit();

            System.out.println(itemName + " is " + (exitCode == 1 ? "" : "not") + " inserted into " + tableName + " table!");
            preparedStatement.close();
            connection.close();

        } catch (Exception e) {
            System.err.println(" in inserting item : " + e.getClass().getName() + ": " + e.getMessage());
            //System.exit(0);
        }
        System.out.println("Inserted " + itemName + " successfully");
    }

    public static void insertItemInProcessMonitorTables(String dbNameURL, String tableName, String itemName, String itemValue) {
        Connection connection = null;
        PreparedStatement preparedStatement = null;

        String commonInsertString = "INSERT INTO " + tableName + " ( LINE_ID, " + itemName + ") VALUES ( ?, ? );";

        try {
            Class.forName(DB_CLASS_FOR_NAME);
            connection = DriverManager.getConnection(dbNameURL);
            connection.setAutoCommit(false);
            System.out.println("Opened database successfully");
            System.out.println("Operating on table " + tableName);


            preparedStatement = connection.prepareStatement(commonInsertString);

            preparedStatement.setString(1, itemValue);
            System.out.println("sql string: " + commonInsertString + itemValue);

            // execute insert SQL stetement
            int exitCode = preparedStatement.executeUpdate();

            connection.commit();

            System.out.println(itemName + " is " + (exitCode == 1 ? "" : "not") + " inserted into " + tableName + " table!");
            preparedStatement.close();
            connection.close();

        } catch (Exception e) {
            System.err.println(" in inserting item : " + e.getClass().getName() + ": " + e.getMessage());
            //System.exit(0);
        }
        System.out.println("Inserted " + itemName + " successfully");
    }

    public static String updateItem(String tableName, String jobID, String itemName, String itemValue) {
        Connection connection = null;
        PreparedStatement preparedStatement = null;

        String commonUpdateString = "UPDATE " + tableName + " SET " + itemName + " = ?  WHERE JOB_ID = ?";

        System.out.println(commonUpdateString);
        String retrievedItem = null;

        try {
            Class.forName(DB_CLASS_FOR_NAME);
            connection = DriverManager.getConnection(JOB_DB_URL);
            connection.setAutoCommit(false);
            System.out.println("Operating on table " + tableName);

            preparedStatement = connection.prepareStatement(commonUpdateString);

            preparedStatement.setString(1, itemValue);
            preparedStatement.setString(2, jobID);

            System.out.println("sql string: " + commonUpdateString);

            int exitCode = preparedStatement.executeUpdate();
            connection.commit();

            System.out.println(itemName + " is " + (exitCode == 1 ? "" : "not") + " updated on " + tableName + " table!");
            System.out.println(itemName + " = "  + itemValue);
            preparedStatement.close();
            connection.close();

        } catch (Exception e) {
            System.err.println(" in update item : " + e.getClass().getName() + ": " + e.getMessage());
            //System.exit(0);
        }
        System.out.println("Operation done successfully");

        return retrievedItem;
    }

    private static byte[] transformInputStreamToBytes(InputStream inputStream){
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();

        int nRead;
        byte[] data = new byte[16384];

        try {
            while ((nRead = inputStream.read(data, 0, data.length)) != -1) {
                buffer.write(data, 0, nRead);
            }
            buffer.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return buffer.toByteArray();
    }

    public static String updateInputStreamItem(String tableName, String jobID, String itemName, InputStream itemValue) {
        Connection connection = null;
        PreparedStatement preparedStatement = null;

        String commonUpdateString = "UPDATE " + tableName + " SET " + itemName + " = ?  WHERE JOB_ID = ?";

        System.out.println(commonUpdateString);
        String retrievedItem = null;

        try {
            Class.forName(DB_CLASS_FOR_NAME);
            connection = DriverManager.getConnection(JOB_DB_URL);
            connection.setAutoCommit(false);
            System.out.println("Operating on table " + tableName);

            preparedStatement = connection.prepareStatement(commonUpdateString);

            preparedStatement.setBytes(1, transformInputStreamToBytes(itemValue));
            preparedStatement.setString(2, jobID);

            System.out.println("sql string: " + commonUpdateString);

            int exitCode = preparedStatement.executeUpdate();
            connection.commit();

            System.out.println(itemName + " is " + (exitCode == 1 ? "" : "not") + " updated on " + tableName + " table!");
            preparedStatement.close();
            connection.close();

        } catch (Exception e) {
            System.err.println(" in update item : " + e.getClass().getName() + ": " + e.getMessage());
            //System.exit(0);
        }
        System.out.println("Operation done successfully");

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
            System.out.println("Operating on table " + INPUT_FILES_LIST_TABLE_NAME + "  jobID = " + jobId + "  searching for " + "fileName");

            preparedStatement = connection.prepareStatement(commonQueryString);

            //preparedStatement.setString(1, itemName);
            preparedStatement.setString(1, jobId);
            System.out.println("sql string: " + preparedStatement);

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
        System.out.println("Operation done successfully");

        return fileList;
    }

    public static void updateInputFilesList(String jobID, String newClientFileName) {
        Connection connection = null;
        PreparedStatement preparedStatement = null;

        String commonUpdateString = "INSERT INTO " + INPUT_FILES_LIST_TABLE_NAME + " ( JOB_ID, FILENAME )  VALUES ( ? , ? );";

        System.out.println(commonUpdateString);

        try {
            Class.forName(DB_CLASS_FOR_NAME);
            connection = DriverManager.getConnection(JOB_DB_URL);
            connection.setAutoCommit(false);
            System.out.println("Operating on table " + INPUT_FILES_LIST_TABLE_NAME);

            preparedStatement = connection.prepareStatement(commonUpdateString);

            preparedStatement.setString(1, jobID);
            preparedStatement.setString(2, newClientFileName);

            int exitCode = preparedStatement.executeUpdate();
            connection.commit();

            System.out.println(newClientFileName + " is " + (exitCode == 1 ? "" : "not") + " added in " + INPUT_FILES_LIST_TABLE_NAME + " table!");

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
        System.out.println("Operating on table " + tableName + "  jobID = " + searchKey + " searching for " + itemName);

        String commonQueryString = "SELECT * FROM " + tableName + " WHERE JOB_ID = ?";

        String retrievedItem = null;

        try {
            Class.forName(DB_CLASS_FOR_NAME);
            connection = DriverManager.getConnection(JOB_DB_URL);
            connection.setAutoCommit(false);
            System.out.println("Operating on table " + tableName + "  jobID = " + searchKey + " searching for " + itemName);

            preparedStatement = connection.prepareStatement(commonQueryString);

            //preparedStatement.setString(1, itemName);
            preparedStatement.setString(1, searchKey);
            System.out.println("sql string: " + preparedStatement);

            ResultSet rs = preparedStatement.executeQuery();

            retrievedItem = rs.getString(itemName);
            System.out.println("Retrieved item name : " + retrievedItem);
            rs.close();
            preparedStatement.close();
            connection.close();
        } catch (Exception e) {
            System.err.println(" in retrieve item : " + e.getClass().getName() + ": " + e.getMessage());
            //System.exit(0);
        }
        System.out.println("Operation done successfully");

        return retrievedItem;
    }

    public static String retrieveProcessMonitorLine(String tableName) {
        Connection connection = null;
        PreparedStatement preparedStatement = null;

        String commonQueryString = "SELECT * FROM " + tableName;

        String retrievedItem = null;

        try {
            Class.forName(DB_CLASS_FOR_NAME);
            connection = DriverManager.getConnection(JOB_DB_URL);
            connection.setAutoCommit(false);

            preparedStatement = connection.prepareStatement(commonQueryString);

            ResultSet rs = preparedStatement.executeQuery();

            if (rs.next()) {
                retrievedItem = rs.getString(1);
                rs.deleteRow();
            } else {
                retrievedItem = "process exited!";
            }

            System.out.println("Retrieved item name : " + retrievedItem);
            rs.close();
            preparedStatement.close();
            connection.close();
        } catch (Exception e) {
            System.err.println(" in retrieve item : " + e.getClass().getName() + ": " + e.getMessage());
            //System.exit(0);
        }

        return retrievedItem;
    }


    public static InputStream retrieveInputStreamItem(String tableName, String searchKey, String itemName) {
        Connection connection = null;
        PreparedStatement preparedStatement = null;
        System.out.println("Operating on table " + tableName + "  jobID = " + searchKey + " searching for " + itemName);

        String commonQueryString = "SELECT * FROM " + tableName + " WHERE JOB_ID = ?";

        InputStream retrievedItem = null;

        try {
            Class.forName(DB_CLASS_FOR_NAME);
            connection = DriverManager.getConnection(JOB_DB_URL);
            connection.setAutoCommit(false);
            System.out.println("Operating on table " + tableName + "  jobID = " + searchKey + " searching for " + itemName);

            preparedStatement = connection.prepareStatement(commonQueryString);

            //preparedStatement.setString(1, itemName);
            preparedStatement.setString(1, searchKey);
            System.out.println("sql string: " + preparedStatement.toString());

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
                System.out.println("Total retrieved item number : " + rs.getFetchSize());
                //rs.deleteRow();
            } else {
                retrievedItem = null;
            }

            //retrievedItem = rs.getBinaryStream(itemName);
            System.out.println("Retrieved item name : " + retrievedItem.toString());
            rs.close();
            preparedStatement.close();
            connection.close();
        } catch (Exception e) {
            System.err.println(" in retrieve input stream item : " );
            e.printStackTrace();
        }
        System.out.println("Operation done successfully");

        return retrievedItem;
    }

    public static boolean isJobIdExist(String jobId) {
        Connection connection = null;
        PreparedStatement preparedStatement = null;

        String queryString = "SELECT JOB_ID FROM FILE_TABLE WHERE JOB_ID = ?";
        String jobName = null;

        try {
            Class.forName(DB_CLASS_FOR_NAME);
            connection = DriverManager.getConnection(JOB_DB_URL);
            connection.setAutoCommit(false);
            System.out.println("Operating on job table ");

            preparedStatement = connection.prepareStatement(queryString);

            preparedStatement.setString(1, jobId);
            System.out.println("sql string: " + preparedStatement.toString());

            ResultSet rs = preparedStatement.executeQuery();

            if (rs.next()) {
                jobName = rs.getString("JOB_ID");
                System.out.println("JOB_ID : " + jobName);
            }
            rs.close();
            preparedStatement.close();
            connection.close();
        } catch (Exception e) {
            System.err.println(" in retrieve item : " + e.getClass().getName() + ": " + e.getMessage());
            //System.exit(0);
        }
        System.out.println("Operation done successfully");
        return (jobName != null);
    }

    public static void insertOFileName(String jobId, String ofileName) {

        Connection connection = null;
        PreparedStatement preparedStatement = null;

        String insertTableSQL = "INSERT INTO FILE_TABLE " + " (JOB_ID, O_FILE_NAME) VALUES (?, ?)";

        try {
            Class.forName(DB_CLASS_FOR_NAME);
            connection = DriverManager.getConnection(JOB_DB_URL);
            connection.setAutoCommit(false);
            System.out.println("Opened database successfully");


            preparedStatement = connection.prepareStatement(insertTableSQL);

            preparedStatement.setString(1, jobId);
            preparedStatement.setString(2, ofileName);

            // execute insert SQL stetement
            int exitCode = preparedStatement.executeUpdate();

            connection.commit();

            System.out.println("OfileName is " + (exitCode == 1 ? "" : "not") + " inserted into FILE_TABLE table!");
            preparedStatement.close();
            connection.close();
        } catch (SQLException e) {
            System.err.println(e.getClass().getName() + ": " + e.getMessage());
            System.exit(0);
        } catch (ClassNotFoundException cnfe) {
            System.err.println(cnfe.getClass().getName() + ": " + cnfe.getMessage());
            System.exit(0);
        }
        System.out.println("Operation done successfully");
    }

    public static String getProgramName(String jobId) {
        return retrieveItem(FILE_TABLE_NAME, jobId, FileTableFields.PROGRAM_NAME.getFieldName());
    }

    public static void updateOFileName(String jobId, String ofileName) {

        Connection connection = null;
        PreparedStatement preparedStatement = null;

        String updateTableSQL = "UPDATE FILE_TABLE set O_FILE_NAME = ? where JOB_ID = ?";

        try {
            Class.forName(DB_CLASS_FOR_NAME);
            connection = DriverManager.getConnection(JOB_DB_URL);
            connection.setAutoCommit(false);
            System.out.println("Opened database successfully");


            preparedStatement = connection.prepareStatement(updateTableSQL);

            preparedStatement.setString(1, ofileName);
            preparedStatement.setString(2, jobId);

            // execute update SQL stetement
            int exitCode = preparedStatement.executeUpdate();

            connection.commit();

            System.out.println("OfileName is " + (exitCode == 1 ? "" : "not") + " updated in FILE_TABLE table!");
            preparedStatement.close();
            connection.close();
        } catch (SQLException e) {
            System.err.println(e.getClass().getName() + ": " + e.getMessage());
            System.exit(0);
        } catch (ClassNotFoundException cnfe) {
            System.err.println(cnfe.getClass().getName() + ": " + cnfe.getMessage());
            System.exit(0);
        }
        System.out.println("Operation done successfully");
    }

    public static void insertFileType(String jobId, String fileType) {

        Connection connection = null;
        PreparedStatement preparedStatement = null;

        String insertTableSQL = "INSERT INTO FILE_TABLE " + " (JOB_ID, I_FILE_TYPE) VALUES (" + "?, ?)";

        try {
            Class.forName(DB_CLASS_FOR_NAME);
            connection = DriverManager.getConnection(JOB_DB_URL);
            connection.setAutoCommit(false);
            System.out.println("Opened database successfully");


            preparedStatement = connection.prepareStatement(insertTableSQL);

            preparedStatement.setString(1, jobId);
            preparedStatement.setString(2, fileType);

            // execute insert SQL stetement
            int exitCode = preparedStatement.executeUpdate();

            connection.commit();

            System.out.println("Ifile type is " + (exitCode == 1 ? "" : "not") + " inserted into FILE_TABLE table!");
            preparedStatement.close();
            connection.close();
        } catch (SQLException e) {
            System.err.println(e.getClass().getName() + ": " + e.getMessage());
            System.exit(0);
        } catch (ClassNotFoundException cnfe) {
            System.err.println(cnfe.getClass().getName() + ": " + cnfe.getMessage());
            System.exit(0);
        }
        System.out.println("Operation done successfully");
    }

    public static void updateFileType(String jobId, String fileType) {

        Connection connection = null;
        PreparedStatement preparedStatement = null;

        String updateTableSQL = "UPDATE FILE_TABLE set I_FILE_TYPE = ? where JOB_ID = ?";

        try {
            Class.forName(DB_CLASS_FOR_NAME);
            connection = DriverManager.getConnection(JOB_DB_URL);
            connection.setAutoCommit(false);
            System.out.println("Opened database successfully");


            preparedStatement = connection.prepareStatement(updateTableSQL);

            preparedStatement.setString(1, fileType);
            preparedStatement.setString(2, jobId);

            // execute update SQL stetement
            int exitCode = preparedStatement.executeUpdate();

            connection.commit();

            System.out.println("ifiletype is " + (exitCode == 1 ? "" : "not") + " updated in FILE_TABLE table!");
            preparedStatement.close();
            connection.close();
        } catch (SQLException e) {
            System.err.println(e.getClass().getName() + ": " + e.getMessage());
            System.exit(0);
        } catch (ClassNotFoundException cnfe) {
            System.err.println(cnfe.getClass().getName() + ": " + cnfe.getMessage());
            System.exit(0);
        }
        System.out.println("Operation done successfully");
    }

    public static void insertMissionName(String jobId, String missionName) {

        Connection connection = null;
        PreparedStatement preparedStatement = null;

        String missionDir = retrieveMissionDir(missionName);

        String insertMissionNameSQL = "INSERT INTO FILE_TABLE " + " (JOB_ID, MISSION_NAME) VALUES (" + "?, ?)";
        String updateMissionTableSQL = "UPDATE FILE_TABLE set MISSION_DIR = ? where JOB_ID = ?";

        try {
            Class.forName(DB_CLASS_FOR_NAME);
            connection = DriverManager.getConnection(JOB_DB_URL);
            connection.setAutoCommit(false);
            System.out.println("Opened database successfully");

            //insert mission_name
            preparedStatement = connection.prepareStatement(insertMissionNameSQL);
            preparedStatement.setString(1, jobId);
            preparedStatement.setString(2, missionName);
            int exitCode = preparedStatement.executeUpdate();

            //insert mission_dir
            preparedStatement = connection.prepareStatement(updateMissionTableSQL);
            preparedStatement.setString(1, missionDir);
            preparedStatement.setString(2, jobId);
            exitCode = preparedStatement.executeUpdate();

            connection.commit();

            System.out.println("mission_name and mission_dir are " + (exitCode == 1 ? "" : "not") + " inserted into FILE_TABLE table!");

            preparedStatement.close();
            connection.close();
        } catch (SQLException e) {
            System.err.println(e.getClass().getName() + ": " + e.getMessage());
            System.exit(0);
        } catch (ClassNotFoundException cnfe) {
            System.err.println(cnfe.getClass().getName() + ": " + cnfe.getMessage());
            System.exit(0);
        }
        System.out.println("Operation done successfully");
    }

    public static void updateMissionName(String jobId, String missionName) {

        Connection connection = null;
        PreparedStatement preparedStatement = null;

        String updateMissionNameSQL = "UPDATE FILE_TABLE set MISSION_NAME = ? where JOB_ID = ?";
        String updateMissionDirSQL = "UPDATE FILE_TABLE set MISSION_DIR = ? where JOB_ID = ?";

        try {
            Class.forName(DB_CLASS_FOR_NAME);
            connection = DriverManager.getConnection(JOB_DB_URL);
            connection.setAutoCommit(false);
            System.out.println("Opened database successfully");


            preparedStatement = connection.prepareStatement(updateMissionNameSQL);

            preparedStatement.setString(1, missionName);
            preparedStatement.setString(2, jobId);

            // execute update SQL stetement
            int exitCode = preparedStatement.executeUpdate();

            connection.commit();

            System.out.println("Mission Name is " + (exitCode == 1 ? "" : "not") + " updated in FILE_TABLE table!");
            preparedStatement.close();
            connection.close();
        } catch (SQLException e) {
            System.err.println(e.getClass().getName() + ": " + e.getMessage());
            System.exit(0);
        } catch (ClassNotFoundException cnfe) {
            System.err.println(cnfe.getClass().getName() + ": " + cnfe.getMessage());
            System.exit(0);
        }
        System.out.println("Operation done successfully");
    }


    public static void updateOFileNameb(String jobId, String ofileName) {

        Connection c = null;
        Statement stmt = null;
        try {
            Class.forName(DB_CLASS_FOR_NAME);
            c = DriverManager.getConnection(JOB_DB_URL, username, password);
            c.setAutoCommit(false);
            System.out.println("Opened database successfully");

            stmt = c.createStatement();
            String sql = "UPDATE FILE_TABLE set O_FILE_NAME = " + ofileName + " where JOB_ID=" + jobId;
            stmt.executeUpdate(sql);
            c.commit();

            ResultSet rs = stmt.executeQuery("SELECT * FROM FILE_TABLE;");
            while (rs.next()) {
                int id = rs.getInt("JOB_ID");
                String fileType = rs.getString("I_FILE_TYPE");
                String o_file_name = rs.getString("O_FILE_NAME");
                System.out.println("ID = " + id);
                System.out.println("NAME = " + fileType);
                System.out.println("AGE = " + o_file_name);
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

    public static String updateOFileName(String jobId, Process process) {
        int exitCode = process.exitValue();
        System.out.println("process exit code = " + exitCode);
        InputStream is;
        if (exitCode == 0) {
            is = process.getInputStream();
        } else {
            is = process.getErrorStream();
        }

        BufferedReader br;

        InputStreamReader isr = new InputStreamReader(is);
        br = new BufferedReader(isr);

        String oFileName = "output";

        try {

            if (exitCode == 0) {
                String line;
                while ((line = br.readLine()) != null) {
                    if (line.startsWith(NEXT_LEVEL_FILE_NAME_TOKEN)) {
                        oFileName = (line.substring(NEXT_LEVEL_FILE_NAME_TOKEN.length())).trim();
                    }
                }
            }

        } catch (IOException ioe) {

            System.out.println(ioe.getMessage());
        } catch (NullPointerException npe) {

            System.out.println(npe.getMessage());
        }

        System.out.println("computed ofile Name = " + oFileName);

        Connection c = null;
        Statement stmt = null;
        try {
            Class.forName(DB_CLASS_FOR_NAME);
            c = DriverManager.getConnection(JOB_DB_URL);
            c.setAutoCommit(false);
            System.out.println("Opened database successfully");

            stmt = c.createStatement();
            String sql = "UPDATE FILE_TABLE set O_FILE_NAME = " + oFileName + " where JOB_ID=" + jobId;
            stmt.executeUpdate(sql);
            c.commit();

            ResultSet rs = stmt.executeQuery("SELECT * FROM FILE_TABLE;");
            while (rs.next()) {
                int id = rs.getInt("JOB_ID");
                String fileType = rs.getString("I_FILE_TYPE");
                String o_file_name = rs.getString("O_FILE_NAME");
                System.out.println("ID = " + id);
                System.out.println("NAME = " + fileType);
                System.out.println("AGE = " + o_file_name);
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
        return "ok";
    }

}
