package gov.nasa.gsfc.seadas.contour.ui;

import org.esa.beam.framework.datamodel.Band;

import javax.swing.event.SwingPropertyChangeSupport;
import java.util.ArrayList;

/**
 * Created by IntelliJ IDEA.
 * User: Aynur Abdurazik (aabduraz)
 * Date: 9/5/13
 * Time: 1:03 PM
 * To change this template use File | Settings | File Templates.
 */
public class ContourData {


    private ArrayList<ContourInterval> contourIntervals;
    Band band;
    int bandIndex;
    static final String CONTOUR_LINES_BASE_NAME = "contour_";
    String contourBaseName;
    private Double startValue;
    private Double endValue;
    private int numOfLevels;

    private final SwingPropertyChangeSupport propertyChangeSupport = new SwingPropertyChangeSupport(this);


    public ContourData() {
        contourIntervals = new ArrayList<ContourInterval>();
        contourBaseName = CONTOUR_LINES_BASE_NAME;
        startValue = 0.0;
        endValue = 0.0;
    }


    public void reset() {
        contourIntervals.clear();
    }

    /**
     * @param startValue     start value for the contour levels; i.e., first contour line falls on this level
     * @param endValue       end value for the contour lines.
     * @param numberOfLevels This parameter decides how many levels between start and end values
     * @param log            This parameter decides the distribution between two levels
     */
    public void createContourLevels(Double startValue, Double endValue, int numberOfLevels, boolean log) {
        Double sv;
        Double ev;
        Double interval;

        this.startValue = startValue;
        this.endValue = endValue;
        this.numOfLevels = numberOfLevels;

        contourIntervals.clear();
        /**
         * In case start value and end values are the same, there will be only one level. The log value is ignored.
         * If the numberofIntervals is one (1), the contour line will be based on the start value.
         */
        if (startValue == endValue || Math.abs(startValue - endValue) < 0.00001 || numberOfLevels == 1) {
            contourIntervals.add(new ContourInterval( contourBaseName, startValue));
            return;
        }

        /**
         * Normal case.
         */
        if (log) {
            sv = Math.log10(startValue);
            ev = Math.log10(endValue);
            interval = (ev - sv) / (numberOfLevels - 1);

            System.out.println("start value: " + sv + "   end value: " + ev + "   interval: " + interval);
            for (int i = 0; i < numberOfLevels; i++) {
                double contourValue = Math.pow(10, sv + interval * i);
                contourIntervals.add(new ContourInterval( contourBaseName, contourValue));
            }
        } else {
            interval = (endValue - startValue) / (numberOfLevels - 1);
            System.out.println("start value: " + startValue + "   end value: " + endValue + "   interval: " + interval);
            for (int i = 0; i < numberOfLevels; i++) {
                double contourValue = startValue + interval * i;
                contourIntervals.add(new ContourInterval( contourBaseName, contourValue));
            }
        }

        for (ContourInterval contourInverval : contourIntervals) {
            System.out.println("contourInterval: " + contourInverval.getContourLevelName());
        }
    }

//    public void addContourLevel(ArrayList<Double> levels) {
//        for (Double level : levels) {
//            contourIntervals.add(level);
//        }
//    }

    public void setBand(Band band) {
        this.band = band;
        contourBaseName = CONTOUR_LINES_BASE_NAME + band.getName() + "_";
    }

    public Band getBand() {
        return band;
    }

    public void setBandIndex(int bandIndex) {
        this.bandIndex = bandIndex;

    }

    public int getBandIndex() {
        return bandIndex;
    }

    public ArrayList<ContourInterval> getLevels() {
//        for (double level = 1; level < 10; level += 2) {
//            contourIntervals.add(level);
//        }
        return contourIntervals;
    }

    public ArrayList<ContourInterval> getContourIntervals() {
        return contourIntervals;
    }

    public void setContourIntervals(ArrayList<ContourInterval> contourIntervals) {
        this.contourIntervals = contourIntervals;
    }

    public Double getStartValue() {
        return startValue;
    }

    public void setStartValue(Double startValue) {
        this.startValue = startValue;
    }

    public Double getEndValue() {
        return endValue;
    }

    public void setEndValue(Double endValue) {
        this.endValue = endValue;
    }

    public int getNumOfLevels() {
        return numOfLevels;
    }

    public void setNumOfLevels(int numOfLevels) {
        this.numOfLevels = numOfLevels;
    }

    public ArrayList<ContourInterval> cloneContourIntervals() {
        ArrayList<ContourInterval> clone = new ArrayList<ContourInterval>(contourIntervals.size());
        for(ContourInterval interval: contourIntervals) {
            clone.add(interval.clone());
        }
        return clone;
    }
}



