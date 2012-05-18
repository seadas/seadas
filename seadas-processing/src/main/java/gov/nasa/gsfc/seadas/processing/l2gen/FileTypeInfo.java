package gov.nasa.gsfc.seadas.processing.l2gen;

import com.jidesoft.icons.JideIconsFactory;

import java.util.HashMap;
import java.util.Iterator;

/**
 * Created by IntelliJ IDEA.
 * User: knowles
 * Date: 5/17/12
 * Time: 2:51 PM
 * To change this template use File | Settings | File Templates.
 */
public class FileTypeInfo {

    public static enum Type {
        L0,
        L1A,
        L1B,
        L2,
        GEO,
        UNKNOWN
    }

    private static String L0 = "Level 0";
    private static String L1A = "Level 1A";
    private static String L1B = "Level 1B";
    private static String L2 = "Level 2";
    private static String GEO = "GEO";

    private Type type = Type.UNKNOWN;
    private HashMap<Type, String> levelHashMap = new HashMap<Type, String>();


    public FileTypeInfo() {
        this(Type.UNKNOWN);
    }

    public FileTypeInfo(Type type) {
        this.type = type;
        initLevelHashMap();
    }

    public void clear() {
        type = Type.UNKNOWN;
    }

    private void initLevelHashMap() {
        levelHashMap.put(Type.L0, L0);
        levelHashMap.put(Type.L1A, L1A);
        levelHashMap.put(Type.L1B, L1B);
        levelHashMap.put(Type.L2, L2);
        levelHashMap.put(Type.GEO, GEO);
        levelHashMap.put(Type.UNKNOWN, "");
    }

    public Type getType() {
        return type;
    }

    public void setType(Type type) {
        this.type = type;
    }

    public boolean isType(Type type) {
        if (this.type == type) {
            return true;
        } else {
            return false;
        }
    }

    public void setType(String levelString) {
        if (levelString == null) {
            setType(Type.UNKNOWN);
            return;
        }

        Iterator itr = levelHashMap.keySet().iterator();

        while (itr.hasNext()) {
            Object key = itr.next();
            if (levelHashMap.get(key).toLowerCase().equals(levelString.toLowerCase())) {
                setType((Type) key);
                return;
            }
        }

        setType(Type.UNKNOWN);
        return;
    }


    public String getTypeString(Type type) {
        if (type == null) {
            return levelHashMap.get(Type.UNKNOWN);
        }

        if (levelHashMap.containsKey(type)) {
            return levelHashMap.get(type);
        } else {
            return levelHashMap.get(Type.UNKNOWN);
        }
    }
}
