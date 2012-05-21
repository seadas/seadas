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

    private final HashMap<Id, String[]> names = new HashMap<Id, String[]>();


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
        names.put(Id.L0, L0_NAMES);
        names.put(Id.L1A, L1A_NAMES);
        names.put(Id.L1B, L1B_NAMES);
        names.put(Id.L2, L2_NAMES);
        names.put(Id.GEO, GEO_NAMES);
        names.put(Id.UNKNOWN, UNKNOWN_NAMES);
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

        Iterator itr = names.keySet().iterator();

        while (itr.hasNext()) {
            Object key = itr.next();

            for (String typeLookup : names.get(key)) {
                if (typeLookup.toLowerCase().equals(name.toLowerCase())) {
                    setId((Id) key);
                    return;
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

        if (names.containsKey(id)) {
            return names.get(id)[0];
        } else {
            return names.get(Id.UNKNOWN)[0];
        }
    }


}
