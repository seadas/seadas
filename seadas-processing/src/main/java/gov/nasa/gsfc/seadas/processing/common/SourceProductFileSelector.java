/*
 * Copyright (C) 2010 Brockmann Consult GmbH (info@brockmann-consult.de)
 *
 * This program is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License as published by the Free
 * Software Foundation; either version 3 of the License, or (at your option)
 * any later version.
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for
 * more details.
 *
 * You should have received a copy of the GNU General Public License along
 * with this program; if not, see http://www.gnu.org/licenses/
 */

package gov.nasa.gsfc.seadas.processing.common;

import com.bc.ceres.swing.progress.ProgressMonitorSwingWorker;
import com.bc.ceres.swing.selection.SelectionChangeListener;
import com.bc.ceres.swing.selection.support.ComboBoxSelectionContext;
import static gov.nasa.gsfc.seadas.processing.common.FileSelector.PROPERTY_KEY_APP_LAST_OPEN_DIR;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.PopupMenuEvent;
import javax.swing.event.PopupMenuListener;
import javax.swing.filechooser.FileFilter;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Vector;
import java.util.logging.Logger;
import org.esa.snap.core.dataio.ProductIO;
import org.esa.snap.core.dataio.ProductIOPlugInManager;
import org.esa.snap.core.dataio.ProductReaderPlugIn;
import org.esa.snap.core.datamodel.Product;
import org.esa.snap.core.datamodel.ProductFilter;
import org.esa.snap.core.datamodel.ProductManager;
import org.esa.snap.core.util.SystemUtils;
import org.esa.snap.rcp.SnapApp;
import org.esa.snap.ui.AppContext;
import org.esa.snap.ui.SnapFileChooser;

/**
 * WARNING: This class belongs to a preliminary API and may change in future releases.
 * todo - add capability to select/add/remove multiple sources (nf - 27.11.2007)
 * todo - add capability to specify optional sources
 *
 * @author Ralf Quast
 * @author Don Shea
 * @author Aynur Abdurazik
 * @version $Revision$ $Date$
 */
public class SourceProductFileSelector {

    private AppContext appContext;
    private ProductFilter productFilter;
    private Product extraProduct;
    private File currentDirectory;
    private DefaultComboBoxModel productListModel;
    private JLabel productNameLabel;
    private JButton productFileChooserButton;
    private JComboBox productNameComboBox;
    private final ProductManager.Listener productManagerListener;
    private ComboBoxSelectionContext selectionContext;
    private RegexFileFilter regexFileFilter;
    private JTextField filterRegexField;
    private JLabel filterRegexLabel;
    //private JTextField ifileTextfield;
    private boolean selectMultipleIFiles;

    private Product sampleProductForMultiIfiles;


    public SourceProductFileSelector(AppContext appContext, String labelText) {
        this(appContext, labelText, false);
    }

    public SourceProductFileSelector(AppContext appContext, String labelText, boolean selectMultipleIFiles) {
        this.selectMultipleIFiles = selectMultipleIFiles;
        this.appContext = appContext;

        //setIfileTextfield(new JTextField(""));

        productListModel = new DefaultComboBoxModel();

        productNameLabel = new JLabel(labelText);
        productFileChooserButton = new JButton(new ProductFileChooserAction());

        productNameComboBox = new JComboBox(productListModel);
        productNameComboBox.setPrototypeDisplayValue("[1] 123456789 123456789 123456789 123456789 123456789");
        productNameComboBox.setRenderer(new ProductListCellRenderer());
        productNameComboBox.setPreferredSize(productNameComboBox.getPreferredSize());
        //   productNameComboBox.setMaximumSize(productNameComboBox.getPreferredSize());
        productNameComboBox.setMinimumSize(productNameComboBox.getPreferredSize());
        productNameComboBox.addPopupMenuListener(new ProductPopupMenuListener());
        productNameComboBox.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                final Product product = (Product) productNameComboBox.getSelectedItem();
                if (product != null) {
                    if (product.getFileLocation() != null) {
                        productNameComboBox.setToolTipText(product.getFileLocation().getPath());
                    } else {
                        productNameComboBox.setToolTipText(product.getDisplayName());
                    }
                } else {
                    productNameComboBox.setToolTipText("Select an input file.");
                }
            }
        });

        productFilter = new AllProductFilter();
        selectionContext = new ComboBoxSelectionContext(productNameComboBox);

        productManagerListener = new ProductManager.Listener() {
            @Override
            public void productAdded(ProductManager.Event event) {
                addProduct(event.getProduct());
            }

            @Override
            public void productRemoved(ProductManager.Event event) {
                Product product = event.getProduct();
                if (productListModel.getSelectedItem() == product) {
                    productListModel.setSelectedItem(null);
                }
                productListModel.removeElement(product);
            }
        };
        regexFileFilter = new RegexFileFilter();
        sampleProductForMultiIfiles = null;

    }

    public void setEnabled(boolean enabled) {
        productNameComboBox.setEnabled(enabled);
        productNameLabel.setEnabled(enabled);
        productFileChooserButton.setEnabled(enabled);
        filterRegexField.setEnabled(enabled);
        filterRegexLabel.setEnabled(enabled);
        //ifileTextfield.setEnabled(enabled);
    }

    public SourceProductFileSelector(AppContext appContext) {
        this(appContext, "Name:");
    }

    /**
     * @return the product filter, default is a filter which accepts all products
     */
    public ProductFilter getProductFilter() {
        return productFilter;
    }

    /**
     * @param productFilter the product filter
     */
    public void setProductFilter(ProductFilter productFilter) {
        this.productFilter = productFilter;
    }

    public synchronized void initProducts() {
        productListModel.removeAllElements();
        for (Product product : appContext.getProductManager().getProducts()) {
            addProduct(product);
        }
        final Product selectedProduct = appContext.getSelectedProduct();
        if (selectedProduct != null && productFilter.accept(selectedProduct) && regexFileFilter.accept(selectedProduct.getFileLocation())) {
            //ifileTextfield.setText(selectedProduct.getName());
            setSelectedProduct(selectedProduct);
        }
        appContext.getProductManager().addListener(productManagerListener);
    }

    public int getProductCount() {
        return productListModel.getSize();
    }

    public void setSelectedIndex(int index) {
        productListModel.setSelectedItem(productListModel.getElementAt(index));
    }

    public Product getSelectedProduct() {
        return (Product) productListModel.getSelectedItem();
    }

    public Product getSelectedProductSample() {
        if (sampleProductForMultiIfiles == null) {
            return (Product) productListModel.getSelectedItem();
        } else {
            return sampleProductForMultiIfiles;
        }
    }

    public Product getSelectedProductsSample() {
        return sampleProductForMultiIfiles;
    }

    public void setCurrentDirectory(File directory) {
        if (directory != null && directory.isDirectory()) {
            currentDirectory = directory;
        }
    }

    public File getCurrentDirectory() {
        return currentDirectory;
    }

    public void setSelectedProduct(Product product) {
        if (product == null) {
            productListModel.setSelectedItem(null);
            return;
        }
        if (productListModelContains(product) && regexFileFilter.accept(product.getFileLocation())) {
            productListModel.setSelectedItem(product);
        } else {
            if (productFilter.accept(product) && regexFileFilter.accept(product.getFileLocation())) {
                productListModel.addElement(product);
                productListModel.setSelectedItem(product);
                if (extraProduct != null) {
                    productListModel.removeElement(extraProduct);
                    extraProduct.dispose();
                }

                extraProduct = product;
            }
        }
        productNameComboBox.revalidate();
        productNameComboBox.repaint();
    }

    public void setSelectedFile(File file) {
        if (file != null && file.canRead()) {
            //Product product = new Product(file.getName(), "DummyType", 10, 10);
            Product product = null;
            try {
                product = ProductIO.readProduct(file);
            } catch (IOException ioe) {

            }
            if (product == null) {
                product = new Product(file.getName(), "DummyType", 10, 10);
            }
            product.setFileLocation(file);
            setSelectedProduct(product);
        }

        if (file == null) {
            setSelectedProduct(null);
        }
        productNameComboBox.validate();
        productNameComboBox.repaint();
    }

    public synchronized void releaseProducts() {
        appContext.getProductManager().removeListener(productManagerListener);
        if (extraProduct != null && getSelectedProduct() != extraProduct) {
            extraProduct.dispose();
        }
        extraProduct = null;
        productListModel.removeAllElements();
    }

    public void addSelectionChangeListener(SelectionChangeListener listener) {
        selectionContext.addSelectionChangeListener(listener);
    }

    private void addProduct(Product product) {
        if (productFilter.accept(product) && regexFileFilter.accept(product.getFileLocation())) {
            productListModel.addElement(product);
        }
    }

    // UI Components

    /////////////////////////////////////

    public JComboBox getProductNameComboBox() {
        return productNameComboBox;
    }

    public JLabel getProductNameLabel() {
        return productNameLabel;
    }

    public void setProductNameLabel(JLabel jLabel) {
        this.productNameLabel = jLabel;
    }

    public JButton getProductFileChooserButton() {
        return productFileChooserButton;
    }

    private boolean productListModelContains(Product product) {
        for (int i = 0; i < productListModel.getSize(); i++) {
            if (productListModel.getElementAt(i).equals(product)) {
                return true;
            }
        }
        return false;
    }


    public JPanel createDefaultPanel() {
        return createDefaultPanel(true);
    }


    public JPanel createDefaultPanel(boolean includeLabel) {
        JPanel mainPanel = new JPanel(new GridBagLayout());

        if (includeLabel) {
            addLabelToMainPanel(mainPanel);
        }

        mainPanel.add(getProductNameComboBox(),
                new GridBagConstraintsCustom(1, 0, 1, 0, GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, 2));
        mainPanel.add(getProductFileChooserButton(),
                new GridBagConstraintsCustom(2, 0, 0, 0, GridBagConstraints.WEST, GridBagConstraints.NONE, 2));
        return mainPanel;
    }

    private void addLabelToMainPanel(JPanel jPanel) {
        jPanel.add(getProductNameLabel(),
                new GridBagConstraintsCustom(0, 0, 0, 0, GridBagConstraints.WEST, GridBagConstraints.NONE, 2));
    }

    private JPanel createFilterPane() {

        filterRegexField = new JTextField("123456789 ");
        filterRegexField.setPreferredSize(filterRegexField.getPreferredSize());
        filterRegexField.setMinimumSize(filterRegexField.getPreferredSize());
        filterRegexField.setMaximumSize(filterRegexField.getPreferredSize());
        filterRegexField.setText("");
        filterRegexField.setName("filterRegexField");

        filterRegexLabel = new JLabel("Filter:");
        filterRegexLabel.setPreferredSize(filterRegexLabel.getPreferredSize());
        filterRegexLabel.setMinimumSize(filterRegexLabel.getPreferredSize());
        filterRegexLabel.setMaximumSize(filterRegexLabel.getPreferredSize());
        filterRegexLabel.setToolTipText("Filter the chooser by regular expression");


        JPanel mainPanel = new JPanel(new GridBagLayout());
        mainPanel.setLayout(new FlowLayout());
        mainPanel.add(filterRegexLabel);
        mainPanel.add(filterRegexField);
        return mainPanel;

    }

    private File[] files;

    public File[] getSelectedMultiFiles() {
        return files;
    }

    private class ProductFileChooserAction extends AbstractAction {

        private String APPROVE_BUTTON_TEXT = "Select";
        private JFileChooser fileChooser;

        private ProductFileChooserAction() {
            super("...");
            fileChooser = new SnapFileChooser();
            JPanel filterPanel = createFilterPane();
            JPanel filePanel = (JPanel) fileChooser.getComponent(1);
            filePanel.add(filterPanel, BorderLayout.CENTER, 0);

            final Vector<RegexFileFilter> regexFilters = new Vector<RegexFileFilter>();

            final JTextField filterRegexField = (JTextField) filterPanel.getComponent(1);

            filterRegexField.getDocument().addDocumentListener(new DocumentListener() {
                @Override
                public void insertUpdate(DocumentEvent documentEvent) {
                    updateFileFilter();
                }

                @Override
                public void removeUpdate(DocumentEvent documentEvent) {
                    updateFileFilter();
                }

                @Override
                public void changedUpdate(DocumentEvent documentEvent) {
                    updateFileFilter();
                }
            });

            fileChooser.setMultiSelectionEnabled(selectMultipleIFiles);
            fileChooser.setDialogTitle("Select Input File");
            final Iterator<ProductReaderPlugIn> iterator = ProductIOPlugInManager.getInstance().getAllReaderPlugIns();
            while (iterator.hasNext()) {
                // todo - (mp, 2008/04/22)check if product file filter is applicable
                fileChooser.addChoosableFileFilter(iterator.next().getProductFileFilter());
            }

            // todo - (mp, 2008/04/22)check if product file filter is applicable
            fileChooser.setAcceptAllFileFilterUsed(false);
            fileChooser.setFileFilter(fileChooser.getAcceptAllFileFilter());
            fileChooser.setFileHidingEnabled(true);
        }

        private void updateFileFilter() {
            regexFileFilter = new RegexFileFilter(filterRegexField.getText());
            //removePreviousFilters(regexFilters);
            fileChooser.resetChoosableFileFilters();
            fileChooser.addChoosableFileFilter(regexFileFilter);
            fileChooser.getUI().rescanCurrentDirectory(fileChooser);

            //regexFilters.add(regexFileFilter);
            Logger.getLogger(appContext.getApplicationWindow().getName()).warning(regexFileFilter.getDescription());
        }

        @Override
        public void actionPerformed(ActionEvent event) {
            final Window window = SwingUtilities.getWindowAncestor((JComponent) event.getSource());

            String homeDirPath = SystemUtils.getUserHomeDir().getPath();
            String openDir = appContext.getPreferences().getPropertyString(PROPERTY_KEY_APP_LAST_OPEN_DIR,
                    homeDirPath);
            currentDirectory = new File(openDir);
            fileChooser.setCurrentDirectory(currentDirectory);

            if (fileChooser.showDialog(window, APPROVE_BUTTON_TEXT) == JFileChooser.APPROVE_OPTION) {

                currentDirectory = fileChooser.getCurrentDirectory();
                appContext.getPreferences().setPropertyString(PROPERTY_KEY_APP_LAST_OPEN_DIR,
                        currentDirectory.getAbsolutePath());

                if (selectMultipleIFiles && fileChooser.getSelectedFiles().length > 1) {
                    handleMultipFileSelection(window);
                    return;
                }

                final File file = fileChooser.getSelectedFile();


                // Added a progress monitor because loading the new product takes to long

                //final Product[] productTmp = {null};

                SnapApp snapApp = SnapApp.getDefault();
                ProgressMonitorSwingWorker pmSwingWorker = new ProgressMonitorSwingWorker(snapApp.getMainFrame(),
                        "SeaDAS Product/File Loader") {

                    @Override
                    protected Void doInBackground(com.bc.ceres.core.ProgressMonitor pm) throws Exception {

                        pm.beginTask("Loading file '" + file + "' as a new SeaDAS product ", 2);

                        try {
                            pm.worked(1);
                            Product product = null;
                            try {
                                if (file.canRead()) {
                                    product = new Product(file.getName(), "DummyType", 10, 10);
                                    product.setFileLocation(file);
                                } else {
                                    throw new IOException(MessageFormat.format("File ''{0}'' could not be read.", file.getPath()));
                                }
                                if (productFilter.accept(product) && regexFileFilter.accept(file)) {
                                    setSelectedProduct(product);
                                } else {
                                    final String message = String.format("Product [%s] is not a valid source.",
                                            product.getFileLocation().getCanonicalPath());
                                    handleError(window, message);
                                    Logger.getLogger(appContext.getApplicationWindow().getName()).warning(" product is hidden: " + new Boolean(product.getFileLocation().isHidden()).toString());
                                    product.dispose();
                                }
                            } catch (Exception e) {
                                if (product != null) {
                                    product.dispose();
                                }
                                handleError(window, e.getMessage());
                                e.printStackTrace();
                            }
                        } catch (Exception e) {
                            pm.done();
                        } finally {
                            pm.done();
                        }
                        return null;
                    }
                };

                pmSwingWorker.executeWithBlocking();
            }
        }

        private void handleMultipFileSelection(Window window) {
            File[] tmpFiles = fileChooser.getSelectedFiles();
            ArrayList<File> tmpArrayList = new ArrayList<File>();
            boolean firstTime = true;
            for (File file : tmpFiles) {
                try {
                    Product product = ProductIO.readProduct(file);
                    if (product == null) {
                        if (file.canRead()) {
                            product = new Product(file.getName(), "DummyType", 10, 10);
                            product.setFileLocation(file);
                            product.setDescription(file.getAbsolutePath());
                        } else {
                            throw new IOException(MessageFormat.format("File ''{0}'' could not be read.", file.getPath()));
                        }
                    } else {
                        if (firstTime) {
                            firstTime = true;
                            sampleProductForMultiIfiles = product;
                        }
                    }

                    if (productFilter.accept(product) && regexFileFilter.accept(file)) {
                        tmpArrayList.add(file);
                    } else {
                        final String message = String.format("Product [%s] is not a valid source.",
                                product.getFileLocation().getCanonicalPath());
                        handleError(window, message);
                        Logger.getLogger(appContext.getApplicationWindow().getName()).warning(" product is hidden: " + new Boolean(product.getFileLocation().isHidden()).toString());
                        product.dispose();
                    }
                } catch (IOException e) {

                    handleError(window, e.getMessage());
                } catch (Exception e) {
                    if (sampleProductForMultiIfiles != null) {
                        sampleProductForMultiIfiles.dispose();
                    }
                    handleError(window, e.getMessage());
                    e.printStackTrace();
                }
            }
            setSelectedMultiFileList(tmpArrayList);
        }

        public void setSelectedMultiFileList(ArrayList<File> tmpArrayList) {
            files = new File[tmpArrayList.size()];
            tmpArrayList.toArray(files);

            File fileListFile = new File(currentDirectory, "_inputFiles.lst");

            StringBuilder fileNames = new StringBuilder();
            for (File file : files) {
                fileNames.append(file.getAbsolutePath() + "\n");
            }
            FileWriter fileWriter = null;
            try {

                fileWriter = new FileWriter(fileListFile);
                fileWriter.write(fileNames.toString());
                fileWriter.close();
            } catch (IOException ioe) {
            }
            Product product = new Product(fileListFile.getName(), "DummyType", 10, 10);
            product.setFileLocation(fileListFile);
            product.setDescription(fileNames.toString());
            setSelectedProduct(product);
            currentDirectory = fileChooser.getCurrentDirectory();
            appContext.getPreferences().setPropertyString(PROPERTY_KEY_APP_LAST_OPEN_DIR,
                    currentDirectory.getAbsolutePath());
        }

        private void handleError(final Component component, final String message) {
            SwingUtilities.invokeLater(new Runnable() {
                public void run() {
                    //@Override
                    //JOptionPane.showMessageDialog(component, message, "Error", JOptionPane.ERROR_MESSAGE);
                    //JOptionPane.showMessageDialog(<Window>component, message, "test", JOptionPane.ERROR_MESSAGE);
                }
            });
        }


    }


    private static class ProductListCellRenderer extends DefaultListCellRenderer {

        @Override
        public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected,
                                                      boolean cellHasFocus) {
            final Component cellRendererComponent =
                    super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);

            if (cellRendererComponent instanceof JLabel && value instanceof Product) {
                final JLabel label = (JLabel) cellRendererComponent;
                final Product product = (Product) value;
                label.setText(product.getDisplayName());
                label.setToolTipText(product.getDescription());
            }

            return cellRendererComponent;
        }
    }

    /**
     * To let the popup menu be wider than the closed combobox.
     * Adapted an idea from http://bugs.sun.com/bugdatabase/view_bug.do?bug_id=6257236
     */
    private static class ProductPopupMenuListener implements PopupMenuListener {

        @Override
        public void popupMenuWillBecomeVisible(PopupMenuEvent e) {
            JComboBox box = (JComboBox) e.getSource();
            Object comp = box.getUI().getAccessibleChild(box, 0);
            if (!(comp instanceof JPopupMenu)) {
                return;
            }
            JComponent scrollPane = (JComponent) ((JPopupMenu) comp)
                    .getComponent(0);

            Dimension size = new Dimension();
            size.width = scrollPane.getPreferredSize().width;
            final int boxItemCount = box.getModel().getSize();
            for (int i = 0; i < boxItemCount; i++) {
                Product product = (Product) box.getModel().getElementAt(i);
                final JLabel label = new JLabel();
                label.setText(product.getDisplayName());
                size.width = Math.max(label.getPreferredSize().width, size.width);
            }
            size.height = scrollPane.getPreferredSize().height;
            scrollPane.setPreferredSize(size);
            scrollPane.setMaximumSize(size);
        }

        @Override
        public void popupMenuCanceled(PopupMenuEvent e) {
        }

        @Override
        public void popupMenuWillBecomeInvisible(PopupMenuEvent e) {
        }
    }

    private static class AllProductFilter implements ProductFilter {

        @Override
        public boolean accept(Product product) {
            return true;
        }
    }

    private class RegexFileFilter extends FileFilter {

        private String regex;

        public RegexFileFilter() {
            this(null);
        }

        public RegexFileFilter(String regex) throws IllegalStateException {
            Logger.getLogger(appContext.getApplicationWindow().getName()).info("regular expression: " + regex);

            if (regex == null) {
                return;
            }

            //Replace wildcards with regular expression.
            if (regex.indexOf("*") != -1) {
                regex = regex.replaceAll("\\*", ".*");
            }
            if (regex.trim().length() == 0) {

                //throw new IllegalStateException();
                return;
            }

            this.regex = ".*" + regex + ".*";

        }

        /* (non-Javadoc)
        * @see java.io.FileFilter#accept(java.io.File)
        */
        public boolean accept(File pathname) {

            if (regex == null) {
                return true;
            }
            Logger.getLogger(appContext.getApplicationWindow().getName()).info("regex: " + (pathname.isFile() && pathname.getName().matches(this.regex)));
            return (pathname.isFile() && pathname.getName().matches(this.regex));
        }

        public String getDescription() {
            return "Files matching regular expression: '" + regex + "'";
        }

        public void ensureFileIsVisible(JFileChooser fc, File f) {
            ensureFileIsVisible(fc, f);
            //ensureFileIsVisible(f, true);
        }
    }
}
