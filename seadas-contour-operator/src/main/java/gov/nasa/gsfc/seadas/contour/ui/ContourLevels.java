package gov.nasa.gsfc.seadas.contour.ui;

import java.util.ArrayList;

/**
 * Created by IntelliJ IDEA.
 * User: Aynur Abdurazik (aabduraz)
 * Date: 1/28/14
 * Time: 4:17 PM
 * To change this template use File | Settings | File Templates.
 */
public class ContourLevels {
    ArrayList<Double> contourIntervals;

    ContourLevels() {
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

    public void addContourLevel(Double startValue, Double endValue, int numberOfLevels, boolean log) {
        Double sv;
        Double ev;
        Double interval;

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

    public ArrayList<Double> getLevels() {
//        for (double level = 1; level < 10; level += 2) {
//            contourIntervals.add(level);
//        }
        return contourIntervals;
    }

    public static void main(String[] args){
        ContourLevels cl = new ContourLevels();
        cl.addContourLevel(1.0, 10.0, 10, false);
        cl.addContourLevel(1.0, 10.0, 10, true);
        ArrayList<Double> result = cl.getLevels();
        result.toArray();
    }
}
