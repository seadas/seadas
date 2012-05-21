package gov.nasa.gsfc.seadas.processing.l2gen;

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

    public static enum Id {
        L0,
        L1A,
        L1B,
        L2,
        GEO,
        UNKNOWN
    }

    private final static String[] L0_NAMES = {"Level 0", "L0"};
    private final static String[] L1A_NAMES = {"Level 1A", "L1A"};
    private final static String[] L1B_NAMES = {"Level 1B", "L1B"};
    private final static String[] L2_NAMES = {"Level 2", "L2"};
    private final static String[] GEO_NAMES = {"GEO"};
    private final static String[] UNKNOWN_NAMES = {"UNKNOWN"};

    private final HashMap<Id, String[]> typesLookup = new HashMap<Id, String[]>();


    private Id type = null;


    public FileTypeInfo() {
        initLevelHashMap();
    }

    public FileTypeInfo(Id type) {
        this();
        this.type = type;
    }

    public void clear() {
        type = null;
    }

    private void initLevelHashMap() {
        typesLookup.put(Id.L0, L0_NAMES);
        typesLookup.put(Id.L1A, L1A_NAMES);
        typesLookup.put(Id.L1B, L1B_NAMES);
        typesLookup.put(Id.L2, L2_NAMES);
        typesLookup.put(Id.GEO, GEO_NAMES);
        typesLookup.put(Id.UNKNOWN, UNKNOWN_NAMES);
    }

    public Id getType() {
        return type;
    }

    public void setType(Id type) {
        this.type = type;
    }

    public boolean isType(Id type) {
        if (this.type == type) {
            return true;
        } else {
            return false;
        }
    }

    public void setType(String type) {
        clear();

        if (type == null) {
            return;
        }

        Iterator itr = typesLookup.keySet().iterator();

        while (itr.hasNext()) {
            Object key = itr.next();

            for (String typeLookup : typesLookup.get(key)) {
                if (typeLookup.toLowerCase().equals(type.toLowerCase())) {
                    setType((Id) key);
                    return;
                }
            }
        }

        setType(Id.UNKNOWN);
        return;
    }


    public String getTypeString() {
        if (type == null) {
            return null;
        }

        if (typesLookup.containsKey(type)) {
            return typesLookup.get(type)[0];
        } else {
            return typesLookup.get(Id.UNKNOWN)[0];
        }
    }
}
