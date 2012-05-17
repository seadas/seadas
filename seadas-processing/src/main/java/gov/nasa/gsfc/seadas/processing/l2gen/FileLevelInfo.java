package gov.nasa.gsfc.seadas.processing.l2gen;

import java.util.HashMap;

/**
 * Created by IntelliJ IDEA.
 * User: knowles
 * Date: 5/17/12
 * Time: 2:51 PM
 * To change this template use File | Settings | File Templates.
 */
public class FileLevelInfo {

    public static enum Level {
        L0,
        L1A,
        L1B,
        L2,
        NULL
    }

    private static String LEVEL_0 = "0";
    private static String LEVEL_1A = "1A";
    private static String LEVEL_1B = "1B";
    private static String LEVEL_2 = "2";



    private static HashMap<Level, String> initLevelHashMap() {
        HashMap<Level, String> levelHashMap;
        levelHashMap = new HashMap();
        levelHashMap.put(Level.L0, LEVEL_0);
        levelHashMap.put(Level.L1A, LEVEL_1A);
        levelHashMap.put(Level.L1B, LEVEL_1B);
        levelHashMap.put(Level.L2, LEVEL_2);
        levelHashMap.put(Level.NULL, "");
        return levelHashMap;
    }


    public static Level getLevel(String level) {
        if (level == null) {
            return Level.NULL;
        }


        if (level.toUpperCase().equals(LEVEL_0.toUpperCase())) {
            return Level.L0;
        } else if (level.toUpperCase().equals(LEVEL_1A.toUpperCase())) {
            return Level.L1A;
        } else if (level.toUpperCase().equals(LEVEL_1B.toUpperCase())) {
            return Level.L1B;
        } else if (level.toUpperCase().equals(LEVEL_2.toUpperCase())) {
            return Level.L2;
        } else {
            return Level.NULL;
        }
    }


    public String getLevelString(Level level) {
        if (level == null) {
            return null;
        }

        HashMap<Level, String> levelHashMap =  initLevelHashMap();

        if (levelHashMap.containsKey(level)) {
            return levelHashMap.get(level);
        } else {
            return null;
        }
    }
}
