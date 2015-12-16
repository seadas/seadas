package gov.nasa.gsfc.seadas.contour.data;

import java.awt.*;
import java.text.DecimalFormat;

/**
 * Created by IntelliJ IDEA.
 * User: Aynur Abdurazik (aabduraz)
 * Date: 5/27/14
 * Time: 6:17 PM
 * To change this template use File | Settings | File Templates.
 */
public class ContourInterval {
    String contourLevelName;
    Double contourLevelValue;
    String contourLineStyleValue;
    private Color lineColor;
    private double dashLength;
    private double spaceLength;
    private String filterName;

    private boolean initial;
    //DecimalFormat decimalFormatBig = new DecimalFormat("##.###");
    DecimalFormat decimalFormatSmall = new DecimalFormat("##.#######");

    private double ptsToPixelsMultiplier;

    public ContourInterval(String contourBaseName, Double contourLevelValue, String filterName, double ptsToPixelsMultiplier, boolean userDefinedContourName) {
        this.contourLevelValue = new Double(decimalFormatSmall.format(contourLevelValue));
//        if (contourLevelValue > 1) {
//            this.contourLevelValue = new Double(decimalFormatBig.format(contourLevelValue));
//        }  else {
//            this.contourLevelValue = new Double(decimalFormatSmall.format(contourLevelValue));
//        }
        if (userDefinedContourName) {
             contourLevelName = contourBaseName;
        }else {
            contourLevelName = contourBaseName + this.contourLevelValue + "_" + filterName;
        }
        lineColor = Color.BLACK;
        contourLineStyleValue = "1.0, 0";
        dashLength = 1.0;
        spaceLength = 0;
        this.filterName = filterName;
        this.ptsToPixelsMultiplier = ptsToPixelsMultiplier;
    }

    public ContourInterval() {
    }

    public void setContourLevelName(String contourLevelName) {
        this.contourLevelName = contourLevelName;
    }

    public String getContourLevelName() {
        return contourLevelName;
    }

    public void setContourLevelValue(Double contourLevelValue) {
        this.contourLevelValue = new Double(decimalFormatSmall.format(contourLevelValue));
//        if (contourLevelValue > 1) {
//            this.contourLevelValue = new Double(decimalFormatBig.format(contourLevelValue));
//        }  else {
//            this.contourLevelValue = new Double(decimalFormatSmall.format(contourLevelValue));
//        }
    }

    public double getContourLevelValue() {
        return contourLevelValue;
    }

    public String getContourLineStyleValue() {
        return contourLineStyleValue;
    }

    public void setContourLineStyleValue(String contourLineStyleValue) {
        this.contourLineStyleValue = contourLineStyleValue;
    }



    public Color getLineColor() {
        return lineColor;
    }

    public void setLineColor(Color lineColor) {
        this.lineColor = lineColor;
    }

    @Override
    public ContourInterval clone(){
        ContourInterval contourInterval = new ContourInterval();
        contourInterval.setPtsToPixelsMultiplier(this.getPtsToPixelsMultiplier());
        contourInterval.setLineColor(new Color(this.getLineColor().getRed(),
                                               this.getLineColor().getGreen(),
                                               this.getLineColor().getBlue(),
                                               this.getLineColor().getAlpha()));
        contourInterval.setContourLevelName(this.getContourLevelName());
        contourInterval.setContourLevelValue(this.getContourLevelValue());
        contourInterval.setContourLineStyleValue(this.getContourLineStyleValue());
        contourInterval.setDashLength(this.getDashLength());
        contourInterval.setSpaceLength(this.getSpaceLength());
        return contourInterval;

    }

    public boolean isInitial() {
        return initial;
    }

    public void setInitial(boolean initial) {
        this.initial = initial;
    }

    public double getDashLength() {
        return dashLength;
    }

    public void setDashLength(double dashLength) {
        this.dashLength = dashLength;
        setContourLineStyleValue(new Double(this.dashLength * ptsToPixelsMultiplier).toString() + ", " + new Double(this.spaceLength * ptsToPixelsMultiplier).toString());
    }

    public double getSpaceLength() {
        return spaceLength;
    }

    public void setSpaceLength(double spaceLength) {
        this.spaceLength = spaceLength;
        setContourLineStyleValue(new Double(this.dashLength * ptsToPixelsMultiplier).toString() + ", " + new Double(this.spaceLength * ptsToPixelsMultiplier).toString());
    }

    public String getFilterName() {
        return filterName;
    }

    public void setFilterName(String filterName) {
        this.filterName = filterName;
    }

    public double getPtsToPixelsMultiplier() {
        return ptsToPixelsMultiplier;
    }

    public void setPtsToPixelsMultiplier(double ptsToPixelsMultiplier) {
        this.ptsToPixelsMultiplier = ptsToPixelsMultiplier;
    }
}
