package gov.nasa.gsfc.seadas.processing.l2gen.productData;

import java.util.ArrayList;

/**
 * A ...
 *
 * @author Danny Knowles
 * @since SeaDAS 7.0
 */
public class L2genProductCategoryInfo extends L2genBaseInfo {


    private String name = null;
    private boolean visible = false;
    private boolean defaultBucket = false;

    private ArrayList<String> productNames = new ArrayList<String>();
    ArrayList<L2genProductInfo> productInfos = new ArrayList<L2genProductInfo>();

    public L2genProductCategoryInfo(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public boolean isVisible() {
        return visible;
    }

    public void setVisible(boolean visible) {
        this.visible = visible;
    }

    public boolean isDefaultBucket() {
        return defaultBucket;
    }

    public void setDefaultBucket(boolean defaultBucket) {
        this.defaultBucket = defaultBucket;
    }

    public ArrayList<L2genProductInfo> getProductInfos() {
        return productInfos;
    }


    public void addProductInfo(L2genProductInfo productInfo) {
        productInfos.add(productInfo);
    }

    public void clearProductInfos() {
        productInfos.clear();
    }

    public void addProductName(String name) {
        productNames.add(name);
    }


    public ArrayList<String> getProductNames() {
        return productNames;
    }
}
