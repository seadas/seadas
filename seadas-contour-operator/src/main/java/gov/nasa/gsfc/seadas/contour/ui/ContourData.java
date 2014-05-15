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


    ArrayList<Double> contourIntervals;
    Band band;

    private final SwingPropertyChangeSupport propertyChangeSupport = new SwingPropertyChangeSupport(this);


    public ContourData() {
        contourIntervals = new ArrayList<Double>();

    }


    public void setLevels() {
        for (double level = 1; level < 10; level += 2) {
            contourIntervals.add(level);
        }
    }

    public void reset(){
        contourIntervals.clear();
    }

    public void addContourLevel(Double level) {
        contourIntervals.add(level);
    }

    /**
     *
     * @param startValue  start value for the contour levels; i.e., first contour line falls on this level
     * @param endValue   end value for the contour lines.
     * @param numberOfLevels  This parameter decides how many levels between start and end values
     * @param log    This parameter decides the distribution between two levels
     */
    public void createContourLevels(Double startValue, Double endValue, int numberOfLevels, boolean log) {
        Double sv;
        Double ev;
        Double interval;

        /**
         * In case start value and end values are the same, there will be only one level. The log value is ignored.
         */
        if (startValue == endValue || Math.abs(startValue-endValue) < 0.00001 )  {
            contourIntervals.add(startValue);
            return;
        }

        /**
         * Normal case.
         */
        if (log) {
            sv = Math.log10(startValue);
            ev = Math.log10(endValue);
            interval = (ev - sv) / (numberOfLevels - 1);
            for (int i = 0; i < numberOfLevels; i++) {
                contourIntervals.add(Math.pow(10, interval * i));
            }
        }  else {
            interval = (endValue - startValue) / (numberOfLevels - 1);
            for (int i = 0; i < numberOfLevels; i++) {
                contourIntervals.add(startValue + interval * i );
            }
        }
    }

    public void addContourLevel(ArrayList<Double> levels) {
        for (Double level:levels) {
            contourIntervals.add(level);
        }
    }

    public void setBand(Band band){
        this.band = band;

    }

    public Band getBand(){
        return band;
    }


    public ArrayList<Double> getLevels() {
//        for (double level = 1; level < 10; level += 2) {
//            contourIntervals.add(level);
//        }
        return contourIntervals;
    }

}



