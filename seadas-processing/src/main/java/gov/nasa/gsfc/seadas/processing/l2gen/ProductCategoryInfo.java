package gov.nasa.gsfc.seadas.processing.l2gen;

import java.util.ArrayList;
import java.util.Collections;

/**
 * A ...
 *
 * @author Danny Knowles
 * @since SeaDAS 7.0
 */
public class ProductCategoryInfo extends BaseInfo {


    private String name = null;
    private boolean visible = false;
    private boolean defaultBucket = false;

    private ArrayList<String> productNames = new ArrayList<String>();
    ArrayList<ProductInfo> productInfos = new ArrayList<ProductInfo>();

    public ProductCategoryInfo(String name) {
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

    public ArrayList<ProductInfo> getProductInfos() {
        return productInfos;
    }


    public void addProductInfo(ProductInfo productInfo) {
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
