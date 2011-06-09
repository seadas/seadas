package gov.nasa.obpg.seadas.sandbox.l2gen;

import com.bc.ceres.core.ProgressMonitor;
import com.bc.ceres.swing.progress.ProgressMonitorSwingWorker;
import org.esa.beam.framework.dataio.ProductIO;
import org.esa.beam.framework.datamodel.Product;
import org.esa.beam.framework.ui.AppContext;
import org.esa.beam.framework.ui.command.CommandEvent;
import org.esa.beam.visat.VisatApp;
import org.esa.beam.visat.actions.AbstractVisatAction;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.concurrent.ExecutionException;

/**
 * A ...
 *
 * @author Norman Fomferra
 * @since SeaDAS 7.0
 */
public class CallL2genAction extends AbstractVisatAction {

    @Override
    public void actionPerformed(CommandEvent event) {
        final AppContext appContext = getAppContext();
        final Product selectedProduct = appContext.getSelectedProduct();
        if (selectedProduct == null) {
            return;
        }
        final File ifile = selectedProduct.getFileLocation();
        final File ofile = new File(selectedProduct.getFileLocation().getParentFile(), "l2gen-out");
        System.out.println("selectedProduct = " + ifile);

        ProgressMonitorSwingWorker swingWorker = new ProgressMonitorSwingWorker<File, Object>(appContext.getApplicationWindow(), "Invoking l2gen") {
            @Override
            protected File doInBackground(ProgressMonitor pm) throws Exception {

                final Process process = Runtime.getRuntime().exec("/disk01/home/nfomferra/Applications/OCSSW/run/bin/l2gen \"ifile=" + ifile + "\" \"ofile=" + ofile + "\"",
                                                                  new String[]{"OCDATAROOT=/home/nfomferra/Applications/OCSSW/run/data"});

                final Thread stdoutPrinter = new Thread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
                            try {
                                String line;
                                while ((line = reader.readLine()) != null) {
                                    System.out.println("l2gen: " + line);
                                }
                            } finally {
                                reader.close();
                            }
                        } catch (IOException e) {
                            // cannot be handled
                        }
                    }
                });
                stdoutPrinter.start();

                final Thread stderrPrinter = new Thread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getErrorStream()));
                            try {
                                String line;
                                while ((line = reader.readLine()) != null) {
                                    System.err.println("l2gen: " + line);
                                }
                            } finally {
                                reader.close();
                            }
                        } catch (IOException e) {
                            // cannot be handled
                        }
                    }
                });
                stderrPrinter.start();

                int exitCode = process.waitFor();
                if (exitCode != 0) {
                    throw new IOException("l2gen failed with exit code " + exitCode);
                }

                appContext.getProductManager().addProduct(ProductIO.readProduct(ofile));

                return ofile;
            }

            @Override
            protected void done() {
                try {
                    final File ofile = get();
                    VisatApp.getApp().showInfoDialog("l2gen", "l2gen done!\nOutput written to:\n" + ofile, null);
                } catch (InterruptedException e) {
                    //
                } catch (ExecutionException e) {
                    VisatApp.getApp().showErrorDialog("l2gen", e.getMessage());
                }
            }
        };

        swingWorker.run();
    }


}
