package gov.nasa.gsfc.seadas.ocsswrest.utilities;

import gov.nasa.gsfc.seadas.ocsswrest.database.SQLiteJDBC;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by IntelliJ IDEA.
 * User: Aynur Abdurazik (aabduraz)
 * Date: 6/24/13
 * Time: 2:03 PM
 * To change this template use File | Settings | File Templates.
 */
public class ProcessRunner {
    public static Process execute(String[] cmdArray) {
        ProcessBuilder processBuilder = new ProcessBuilder(cmdArray);
        Map<String, String> env = processBuilder.environment();
        HashMap environment = new HashMap();

        environment.put("OCSSWROOT", OCSSWServerModel.OCSSW_INSTALL_DIR);

        env.putAll(environment);

        processBuilder.directory(new File(ServerSideFileUtilities.FILE_UPLOAD_PATH));
        Process process = null;
        try {
            process = processBuilder.start();
        } catch (IOException ioe) {

        }
        return process;
    }

    public static Process executeInstaller(String[] cmdArray) {


        executeTest();

        ProcessBuilder processBuilder = new ProcessBuilder(cmdArray);
//        Map<String, String> env = processBuilder.environment();
//        HashMap environment = new HashMap();
//
//        environment.put("OCSSWROOT", OCSSWServerModel.OCSSW_INSTALL_DIR);
//
//        env.putAll(environment);

        processBuilder.directory(new File(OCSSWServerModel.OCSSW_INSTALL_DIR));
        Process process = null;
        try {
            System.out.println("starting execution 1 ...");
            process = processBuilder.start();
            System.out.println("starting execution 2 ...");
            process.wait();
            System.out.println("starting execution 3 ...");
        } catch (IOException ioe) {
            System.out.println("installer execution exception!");
            System.out.println(ioe.getMessage());
        } catch (InterruptedException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
            System.out.println(e.getMessage());
        }
        System.out.println("completed installer execution!");

        return process;
    }

    public static Process executeCmdArray(String[] cmdArray) {


        executeTest();

        ProcessBuilder processBuilder = new ProcessBuilder(cmdArray);
        Process process = null;
        try {
            System.out.println("starting execution 1 ...");
            process = processBuilder.start();
        } catch (IOException ioe) {
            System.out.println("installer execution exception!");
            System.out.println(ioe.getMessage());
        }

        return process;
    }

    public static Process executeTest(){

        ArrayList<String> cmdList = new ArrayList<String>();

        cmdList.add("mkdir");
        cmdList.add("/home/aabduraz/Public/test");

        System.out.println("starting test execution  ...");

        String[] cmdArray = new String[3];
        cmdArray[0] = "ls";
        cmdArray[1] = ">";
        cmdArray[2] = "test";

        ProcessBuilder processBuilder = new ProcessBuilder(cmdArray);

        processBuilder.directory(new File("/home/aabduraz"));

        Process process = null;
        try {
            process = processBuilder.start();
            System.out.println("test executed!" );
            processBuilder = new ProcessBuilder(cmdList);
            process = processBuilder.start();
            System.out.println("test2 executed!");
        } catch (IOException ioe) {
            System.out.println("Exception in execution!");
            System.out.println(ioe.getMessage());
        }
        //int i = process.exitValue();
        System.out.println("completed " ) ; //System.out.println(process.exitValue());
        return process;
    }
}
