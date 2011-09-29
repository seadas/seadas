package gov.nasa.obpg.seadas.sandbox.l2gen;

import org.esa.beam.framework.datamodel.Product;

import java.util.ArrayList;
import java.util.Comparator;

/**
 * A ...
 *
 * @author Danny Knowles
 * @since SeaDAS 7.0
 */
public class ProductInfo implements Comparable<ProductInfo> {

    private String name = "";
    private ArrayList<AlgorithmInfo> algorithmInfoArrayList = new ArrayList<AlgorithmInfo>();

    // some new ones
    private boolean isPartiallySelected = false;
    private boolean isSelected = false;




    public static final Comparator<ProductInfo> CASE_SENSITIVE_ORDER
            = new CaseSensitiveComparator();

    public static final Comparator<ProductInfo> CASE_INSENSITIVE_ORDER
            = new CaseInsensitiveComparator();

    private static class CaseSensitiveComparator
            implements Comparator<ProductInfo> {

        public int compare(ProductInfo s1, ProductInfo s2) {
            return s1.name.compareTo(s2.name);
        }
    }

    private static class CaseInsensitiveComparator
            implements Comparator<ProductInfo> {

        public int compare(ProductInfo s1, ProductInfo s2) {
            return s1.name.compareToIgnoreCase(s2.name);
        }
    }




    public ProductInfo(String name) {
        this.setName(name);
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public ArrayList<AlgorithmInfo> getAlgorithmInfoArrayList() {
        return algorithmInfoArrayList;
    }


    public void addAlgorithmInfo(AlgorithmInfo algorithmInfo) {
        algorithmInfoArrayList.add(algorithmInfo);
    }

    public void dump() {
        System.out.println(name);

        for (AlgorithmInfo algorithmInfo : algorithmInfoArrayList) {
            algorithmInfo.dump();
        }
    }

    public String toString() {
        StringBuilder stringBuilder = new StringBuilder(name);


        for (AlgorithmInfo algorithmInfo : algorithmInfoArrayList) {
            stringBuilder.append("\n  ");
            stringBuilder.append(algorithmInfo.toString());
        }

        return stringBuilder.toString();
    }

    public int compareTo(ProductInfo p) {
        return name.compareToIgnoreCase(p.name);
    }


}



