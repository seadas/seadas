package gov.nasa.gsfc.seadas.contour.data;

import org.esa.snap.core.datamodel.Band;

import javax.swing.event.SwingPropertyChangeSupport;
import java.awt.*;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;

/**
 * Created by IntelliJ IDEA.
 * User: Aynur Abdurazik (aabduraz)
 * Date: 9/5/13
 * Time: 1:03 PM
 * To change this template use File | Settings | File Templates.
 */
public class ContourData {


    static final String NEW_BAND_SELECTED_PROPERTY = "newBandSelected";
    static final String CONTOUR_LINES_BASE_NAME = "contour_";
    static final String NEW_FILTER_SELECTED_PROPERTY = "newFilterSelected";
    static final String DATA_CHANGED_PROPERTY = "dataChanged";


    private ArrayList<ContourInterval> contourIntervals;
    Band band;
    int bandIndex;
    String contourBaseName;
    private Double startValue;
    private Double endValue;
    private int numOfLevels;
    private boolean log;
    private boolean filtered;
    private boolean contourCustomized;
    private String filterName;
    private String oldFilterName;
    private double ptsToPixelsMultiplier;
    private boolean deleted;
    private boolean contourInitialized;
    private boolean contourValuesChanged;

    private final SwingPropertyChangeSupport propertyChangeSupport = new SwingPropertyChangeSupport(this);


    public ContourData() {
        this(null, null, null, 1);
    }

    public ContourData(Band band, String unfiltereBandName, String filterName, double ptsToPixelsMultiplier) {
        contourIntervals = new ArrayList<ContourInterval>();
        contourBaseName = CONTOUR_LINES_BASE_NAME;
        if (band != null) {
            contourBaseName = contourBaseName + unfiltereBandName + "_";
        }
        startValue = Double.MIN_VALUE;
        endValue = Double.MAX_VALUE;
        this.band = band;
        log = false;
        filtered = true;
        contourCustomized = false;
        contourInitialized = true;
        contourValuesChanged = false;
        deleted = false;
        this.filterName = filterName;
        this.ptsToPixelsMultiplier = ptsToPixelsMultiplier;
        propertyChangeSupport.addPropertyChangeListener(NEW_FILTER_SELECTED_PROPERTY, getFilterButtonPropertyListener());
        propertyChangeSupport.addPropertyChangeListener(NEW_FILTER_SELECTED_PROPERTY, getFilterButtonPropertyListener());
    }

    private PropertyChangeListener getFilterButtonPropertyListener() {
        return new PropertyChangeListener() {
            @Override
            public void propertyChange(PropertyChangeEvent propertyChangeEvent) {
                updateContourNamesForNewFilter(oldFilterName, filterName);
            }
        };
    }


    public void reset() {
        contourIntervals.clear();
    }

    public void createContourLevels() {
        createContourLevels(startValue, endValue, numOfLevels, log);
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
        ArrayList<Color> colors = new ArrayList<Color>();
        ArrayList<String> names = new ArrayList<>();
        this.startValue = startValue;
        this.endValue = endValue;
        this.numOfLevels = numberOfLevels;

        if (contourCustomized) {
            colors = getColors();
            names = getNames();

        }


        contourIntervals.clear();
        /**
         * In case start value and end values are the same, there will be only one level. The log value is ignored.
         * If the numberofIntervals is one (1), the contour line will be based on the start value.
         */
        if (startValue == endValue || Math.abs(startValue - endValue) < 0.00001 || numberOfLevels == 1) {
            contourIntervals.add(new ContourInterval(contourBaseName, startValue, filterName, ptsToPixelsMultiplier, false));

            //Only update names if the user edited the contour interval values
            if (contourCustomized) {
                updateColors(colors);
                updateNames(names);
            }
            return;
        }

        /**
         * Normal case.
         */
        if (log) {
            if (startValue == 0) startValue = Double.MIN_VALUE;
            if (endValue == 0) endValue = Double.MIN_VALUE;
            sv = Math.log10(startValue);
            ev = Math.log10(endValue);
            interval = (ev - sv) / (numberOfLevels - 1);

            System.out.println("start value: " + sv + "   end value: " + ev + "   interval: " + interval);
            for (int i = 0; i < numberOfLevels; i++) {
                double contourValue = Math.pow(10, sv + interval * i);
                contourIntervals.add(new ContourInterval(contourBaseName, contourValue, filterName, ptsToPixelsMultiplier, false));
            }
        } else {
            interval = (endValue - startValue) / (numberOfLevels - 1);
            System.out.println("start value: " + startValue + "   end value: " + endValue + "   interval: " + interval);
            for (int i = 0; i < numberOfLevels; i++) {
                double contourValue = startValue + interval * i;
                contourIntervals.add(new ContourInterval(contourBaseName, contourValue, filterName, ptsToPixelsMultiplier, false));
            }
        }

        if (contourCustomized) {
            updateColors(colors);
            updateNames(names);
        }
    }

    private void updateContourIntervals() {
        ArrayList<Color> colors = getColors();
        ArrayList<String> names = getNames();
        updateColors(colors);
        updateNames(names);

    }

    private void updateColors(ArrayList<Color> colors) {
        if (colors.size() > 0) {
            int i = 0;
            for (ContourInterval contourInverval : contourIntervals) {
                if (colors.size() > i) {
                    contourInverval.setLineColor(colors.get(i++));
                }
            }
        }
    }

    private void updateNames(ArrayList<String> names) {
        if (names.size() > 0) {
            int i = 0;
            for (ContourInterval contourInverval : contourIntervals) {
                if (names.size() > i) {
                    contourInverval.setContourLevelName(names.get(i++));
                }
            }
        }
    }

    public void updateContourNamesForNewFilter(String oldFilterName, String newFilterName) {
        for (ContourInterval contourInverval : contourIntervals) {
            String newName = contourInverval.getContourLevelName().replace(oldFilterName, newFilterName);
            contourInverval.setContourLevelName(newName);
        }
    }


    public void setBand(Band band) {
        String oldBandName = this.band.getName();
        this.band = band;
        //contourBaseName = CONTOUR_LINES_BASE_NAME + band.getName() + "_";
        propertyChangeSupport.firePropertyChange(NEW_BAND_SELECTED_PROPERTY, oldBandName, band.getName());
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

    public void setStartEndValues(Double startValue, Double endValue) {
        this.startValue = startValue;
        this.endValue = endValue;
        propertyChangeSupport.firePropertyChange(DATA_CHANGED_PROPERTY, startValue, endValue);
    }

    public int getNumOfLevels() {
        return numOfLevels;
    }

    public void setNumOfLevels(int numOfLevels) {
        this.numOfLevels = numOfLevels;
    }

    public ArrayList<Color> getColors() {
        ArrayList<Color> colors = new ArrayList<Color>();
        for (ContourInterval interval : contourIntervals) {
            colors.add(interval.getLineColor());
        }
        return colors;
    }

    public ArrayList<String> getNames() {
        ArrayList<String> names = new ArrayList<String>();
        for (ContourInterval interval : contourIntervals) {
            names.add(interval.getContourLevelName());
        }
        return names;
    }

    public ArrayList<ContourInterval> cloneContourIntervals() {
        ArrayList<ContourInterval> clone = new ArrayList<ContourInterval>(contourIntervals.size());
        for (ContourInterval interval : contourIntervals) {
            clone.add(interval.clone());
        }
        return clone;
    }

    public void addPropertyChangeListener(String name, PropertyChangeListener listener) {
        propertyChangeSupport.addPropertyChangeListener(name, listener);
    }

    public void removePropertyChangeListener(String name, PropertyChangeListener listener) {
        propertyChangeSupport.removePropertyChangeListener(name, listener);
    }

    public SwingPropertyChangeSupport getPropertyChangeSupport() {
        return propertyChangeSupport;
    }

    public void appendPropertyChangeSupport(SwingPropertyChangeSupport propertyChangeSupport) {
        PropertyChangeListener[] pr = propertyChangeSupport.getPropertyChangeListeners();
        for (int i = 0; i < pr.length; i++) {
            this.propertyChangeSupport.addPropertyChangeListener(pr[i]);
        }
    }

    public void clearPropertyChangeSupport() {
        PropertyChangeListener[] pr = propertyChangeSupport.getPropertyChangeListeners();
        for (int i = 0; i < pr.length; i++) {
            this.propertyChangeSupport.removePropertyChangeListener(pr[i]);
        }

    }

    public boolean isLog() {
        return log;
    }

    public void setLog(boolean log) {
        this.log = log;
    }

    public boolean isFiltered() {
        return filtered;
    }

    public void setFiltered(boolean filtered) {
        this.filtered = filtered;
    }

    public boolean isContourCustomized() {
        return contourCustomized;
    }

    public void setContourCustomized(boolean contourCustomized) {
        this.contourCustomized = contourCustomized;
    }

    public String getFilterName() {
        return filterName;
    }

    public void setFilterName(String filterName) {
        oldFilterName = this.filterName;
        this.filterName = filterName;
        updateContourNamesForNewFilter(oldFilterName, filterName);
    }

    public boolean isDeleted() {
        return deleted;
    }

    public void setDeleted(boolean deleted) {
        this.deleted = deleted;
    }

    public boolean isContourValuesChanged() {
        return contourValuesChanged;
    }

    public void setContourValuesChanged(boolean contourValuesChanged) {
        this.contourValuesChanged = contourValuesChanged;
    }
}



