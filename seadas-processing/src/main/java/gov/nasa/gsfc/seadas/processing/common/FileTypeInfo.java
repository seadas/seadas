package gov.nasa.gsfc.seadas.processing.common;

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
        L1EXTRACT,
        L1,
        L1A,
        GEO,
        L1B,
        SDR,
        L1MAP,
        L1BRS,
        L2EXTRACT,
        L2,
        L2MAP,
        L2BRS,
        L2BIN,
        L3,
        L3BIN,
        L3SMI,
        UNKNOWN
    }


    private final static String[] L0_NAMES = {"Level 0", "L0"};
    private final static String[] L1EXTRACT_NAMES = null;
    private final static String[] L1_NAMES = {"Level 1", "L1"};
    private final static String[] L1A_NAMES = {"Level 1A", "L1A"};
    private final static String[] GEO_NAMES = {"GEO"};
    private final static String[] L1B_NAMES = {"Level 1B", "L1B"};
    private final static String[] SDR_NAMES = {"SDR"};
    private final static String[] L1MAP_NAMES = null;
    private final static String[] L1BRS_NAMES = {"Level 1 Browse Data"};
    private final static String[] L2EXTRACT_NAMES = null;
    private final static String[] L2_NAMES = {"Level 2", "L2"};
    private final static String[] L2MAP_NAMES = null;
    private final static String[] L2BRS_NAMES = {"Level 2 Browse Data"};
    private final static String[] L2BIN_NAMES = null;
    private final static String[] L3_NAMES = {"Level 3"};
    private final static String[] L3BIN_NAMES = {"Level 3 Binned"};
    private final static String[] L3SMI_NAMES = {"Level 3 SMI"};
    private final static String[] UNKNOWN_NAMES = {"UNKNOWN"};

    private final HashMap<Id, String[]> nameLookup = new HashMap<Id, String[]>();


    private Id id = null;


    public FileTypeInfo() {
        initNamesHashMap();
    }

    public FileTypeInfo(Id id) {
        this();
        this.id = id;
    }

    public void clear() {
        id = null;
    }

    private void initNamesHashMap() {
        nameLookup.put(Id.L0, L0_NAMES);
        nameLookup.put(Id.L1EXTRACT, L1EXTRACT_NAMES);
        nameLookup.put(Id.L1, L1_NAMES);
        nameLookup.put(Id.L1A, L1A_NAMES);
        nameLookup.put(Id.GEO, GEO_NAMES);
        nameLookup.put(Id.L1B, L1B_NAMES);
        nameLookup.put(Id.SDR, SDR_NAMES);
        nameLookup.put(Id.L1MAP, L1MAP_NAMES);
        nameLookup.put(Id.L1BRS, L1BRS_NAMES);
        nameLookup.put(Id.L2EXTRACT, L2EXTRACT_NAMES);
        nameLookup.put(Id.L2, L2_NAMES);
        nameLookup.put(Id.L2MAP, L2MAP_NAMES);
        nameLookup.put(Id.L2BRS, L2BRS_NAMES);
        nameLookup.put(Id.L2BIN, L2BIN_NAMES);
        nameLookup.put(Id.L3BIN, L3BIN_NAMES);
        nameLookup.put(Id.L3, L3_NAMES);
        nameLookup.put(Id.L3SMI, L3SMI_NAMES);
        nameLookup.put(Id.UNKNOWN, UNKNOWN_NAMES);
    }

    public Id getId() {
        return id;
    }

    public void setId(Id id) {
        this.id = id;
    }

    public boolean isId(Id id) {
        if (this.id == id) {
            return true;
        } else {
            return false;
        }
    }

    public void setName(String name) {
        clear();

        if (name == null) {
            return;
        }

        Iterator itr = nameLookup.keySet().iterator();

        while (itr.hasNext()) {
            Object key = itr.next();

            if (nameLookup.get(key) != null) {
                for (String typeLookup : nameLookup.get(key)) {
                    if (typeLookup.toLowerCase().equals(name.toLowerCase())) {
                        setId((Id) key);
                        return;
                    }
                }
            }
        }

        setId(Id.UNKNOWN);
        return;
    }


    public String getName() {
        if (id == null) {
            return null;
        }

        if (nameLookup.containsKey(id) && nameLookup.get(id) != null) {
            return nameLookup.get(id)[0];
        }

        return null;
    }


}
