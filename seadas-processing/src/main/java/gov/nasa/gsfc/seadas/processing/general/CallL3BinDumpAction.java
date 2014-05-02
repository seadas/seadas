package gov.nasa.gsfc.seadas.processing.general;

import gov.nasa.gsfc.seadas.processing.core.ProcessorModel;
import gov.nasa.gsfc.seadas.processing.utilities.SheetCell;
import gov.nasa.gsfc.seadas.processing.utilities.SpreadSheet;

import javax.swing.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.StringTokenizer;

/**
 * Created by IntelliJ IDEA.
 * User: Aynur Abdurazik (aabduraz)
 * Date: 4/29/14
 * Time: 1:22 PM
 * To change this template use File | Settings | File Templates.
 */
public class CallL3BinDumpAction extends CallCloProgramAction {

    @Override
    void displayOutput(ProcessorModel processorModel) {
        String output = processorModel.getExecutionLogMessage();
        StringTokenizer st = new StringTokenizer(output, "\n");
        int numRows = st.countTokens();
        String line = st.nextToken();
        StringTokenizer stLine = new StringTokenizer(line, " ");
        String prodName = null;
        boolean skipFirstLine = false;

        if (stLine.countTokens() < 14) {
            prodName = stLine.nextToken();
            numRows--;
            skipFirstLine = true;
        }
        if (!skipFirstLine) {
            st = new StringTokenizer(output, "\n");
        }
        SheetCell[][] cells = new SheetCell[numRows][14];
        int i = 0;
        while (st.hasMoreElements()) {
            line = st.nextToken();
            stLine = new StringTokenizer(line, " ");
            int j = 0;
            while (stLine.hasMoreElements()) {
                cells[i][j] = new SheetCell(i, j, stLine.nextToken(), null);
                j++;
            }
            i++;
        }

        if (skipFirstLine) {
            cells[0][10] = new SheetCell(0, 10, prodName + " " + cells[0][10].getValue(), null);
            cells[0][11] = new SheetCell(0, 11, prodName + " " + cells[0][11].getValue(), null);
        }

        SpreadSheet sp = new SpreadSheet(cells);

        JFrame frame = new JFrame("l3bindump Output");

        /*
       * Allows the user to exit the application
       * from the window manager's dressing.
       */
        frame.addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                System.exit(0);
            }
        });

        frame.getContentPane().add(sp.getScrollPane());
        frame.pack();
        frame.setVisible(true);

    }
}
