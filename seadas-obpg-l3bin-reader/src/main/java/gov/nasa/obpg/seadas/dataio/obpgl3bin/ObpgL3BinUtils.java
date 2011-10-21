package gov.nasa.obpg.seadas.dataio.obpgl3bin;

import gov.nasa.obpg.seadas.dataio.obpg.ObpgUtils;
import org.esa.beam.framework.datamodel.Band;
import org.esa.beam.framework.datamodel.Product;
import org.esa.beam.framework.datamodel.ProductData;
import org.esa.beam.framework.datamodel.VirtualBand;
import ucar.nc2.Structure;
import ucar.nc2.Variable;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ObpgL3BinUtils {

    private ObpgUtils obpgUtils = new ObpgUtils();

    public Map<Band, Variable> addBands(Product product, Variable idxVariable, List<Variable> l3ProdVars) {

        final Structure binListStruc = (Structure) idxVariable;

        final Map<Band, Variable> bandToVariableMap = new HashMap<Band, Variable>();


        bandToVariableMap.put(addBand(product,"bin_num",ProductData.TYPE_UINT32), binListStruc.select("bin_num").findVariable("bin_num"));
        bandToVariableMap.put(addBand(product,"weights",ProductData.TYPE_FLOAT32), binListStruc.select("weights").findVariable("weights"));
        bandToVariableMap.put(addBand(product,"nobs",ProductData.TYPE_UINT16),binListStruc.select("nobs").findVariable("nobs"));
        bandToVariableMap.put(addBand(product,"nscenes",ProductData.TYPE_UINT16), binListStruc.select("nscenes").findVariable("nscenes"));

        for (Variable l3Var: l3ProdVars){
            String varName = l3Var.getShortName();
            final int dataType = ProductData.TYPE_FLOAT32;


            if (!varName.contains("Bin") && (!varName.equalsIgnoreCase("SEAGrid")) &&
                    (!varName.equalsIgnoreCase("Input Files"))) {
                final Structure binStruc = (Structure) l3Var;
                List<String> vnames = binStruc.getVariableNames();
                for (String bandvar : vnames){
                    bandToVariableMap.put(addBand(product,bandvar,dataType), binStruc.select(bandvar).findVariable(bandvar));
                }
                // Add virtual band for product mean
                StringBuilder prodname = new StringBuilder(varName);
                prodname.append("_mean");

                String calcmean = ComputeBinMeans(varName);
                Band varmean = new VirtualBand(prodname.toString(), ProductData.TYPE_FLOAT32,product.getSceneRasterWidth(),
                        product.getSceneRasterHeight(),calcmean);
                product.addBand(varmean);

                // Add virtual band for product stdev
                int underscore = prodname.indexOf("_mean");
                prodname.delete(underscore, underscore + 5);
                prodname.append("_stdev");

                String calcstdev = ComputeBinVariances(varName);

                Band varstdev = new VirtualBand(prodname.toString(), ProductData.TYPE_FLOAT32,product.getSceneRasterWidth(),
                        product.getSceneRasterHeight(),calcstdev);
                product.addBand(varstdev);
            }
        }
        return bandToVariableMap;
    }

    private Band addBand(Product product, String varName, int productType) {
        Band band = new Band(varName, productType, product.getSceneRasterWidth(),
                product.getSceneRasterHeight());
        band.setScalingOffset(0.0);
        band.setScalingFactor(1.0);
        band.setLog10Scaled(false);
        product.addBand(band);
        return band;
    }


    private String ComputeBinMeans(String prodname){
        StringBuilder bin_mean = new StringBuilder(prodname);
        bin_mean.append("_");
        bin_mean.append("sum / weights");
        return bin_mean.toString();
    }

    private String ComputeBinVariances(String prodname){
        StringBuilder bin_stdev = new StringBuilder("(((");
        bin_stdev.append(prodname);
        bin_stdev.append("_sum_sq/weights) - (");
        bin_stdev.append(prodname);
        bin_stdev.append("_mean * ");
        bin_stdev.append(prodname);
        bin_stdev.append("_mean) * (weights * weights)) / ((weights * weights) - nscenes))");
        return bin_stdev.toString();
    }

}