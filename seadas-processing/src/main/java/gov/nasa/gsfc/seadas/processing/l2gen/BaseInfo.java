package gov.nasa.gsfc.seadas.processing.l2gen;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: dshea
 * Date: 1/3/12
 * Time: 8:30 AM
 * To change this template use File | Settings | File Templates.
 */
public class BaseInfo implements Comparable<BaseInfo> {


    public enum State {
        NOT_SELECTED, PARTIAL, SELECTED
    }

    public static final Comparator<ProductInfo> CASE_SENSITIVE_ORDER
            = new CaseSensitiveComparator();

    public static final Comparator<ProductInfo> CASE_INSENSITIVE_ORDER
            = new CaseInsensitiveComparator();


    private static class CaseSensitiveComparator
            implements Comparator<ProductInfo> {

        public int compare(ProductInfo s1, ProductInfo s2) {
            return s1.getFullName().compareTo(s2.getFullName());
        }
    }

    private static class CaseInsensitiveComparator
            implements Comparator<ProductInfo> {

        public int compare(ProductInfo s1, ProductInfo s2) {
            return s1.getFullName().compareToIgnoreCase(s2.getFullName());
        }
    }


    private String name;
    private State state = State.NOT_SELECTED;
    private String description = null;
    private BaseInfo parent = null;
    private ArrayList<BaseInfo> children = new ArrayList<BaseInfo>();

    public BaseInfo() {
        this("", null);
    }

    public BaseInfo(String name) {
        this(name, null);
    }

    public BaseInfo(String name, BaseInfo parent) {
        this.name = name;
        this.parent = parent;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public String getFullName() {
        return getName();
    }

    public void setSelected(boolean selected) {
        if (selected) {
            setState(State.SELECTED);
        } else {
            setState(State.NOT_SELECTED);
        }
    }

    public boolean isSelected() {
        if (state == State.SELECTED || state == State.PARTIAL) {
            return true;
        } else {
            return false;
        }
    }

    public void setState(State state) {
        this.state = state;
    }

    public void nextState() {
        if (state == State.NOT_SELECTED) {
            setState(State.PARTIAL);
        } else if (state == State.PARTIAL) {
            setState(State.SELECTED);
        } else {
            setState(State.NOT_SELECTED);
        }
    }

    public State getState() {
        return state;
    }


    public void setParent(BaseInfo parent) {
        this.parent = parent;
    }

    public BaseInfo getParent() {
        return parent;
    }

    public boolean hasChildren() {
        return !children.isEmpty();
    }

    public List<BaseInfo> getChildren() {
        return children;
    }

    public void clearChildren() {
        children.clear();
    }

    public void addChild(BaseInfo child) {
        children.add(child);
    }

    public int compareTo(BaseInfo info) {
        return getName().compareToIgnoreCase(info.getName());
    }

    public void dump() {
        System.out.println(getName());

        for (BaseInfo info : getChildren()) {
            info.dump();
        }
    }


    public void sortChildren() {
        Collections.sort(children);
    }

//
//    public boolean isWavelengthDependent() {
//
//        boolean result = false;
//
//        for(BaseInfo info : getChildren()) {
//            if(info.isWavelengthDependent()) {
//                return true;
//            }
//        }
//        return false;
//    }
//
//    public boolean isWavelengthIndependent() {
//        for(BaseInfo info : getChildren()) {
//            if(info.isWavelengthIndependent()) {
//                return true;
//            }
//        }
//        return false;
//    }
    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String toString() {
        return getFullName();
    }

}
