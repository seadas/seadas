package gov.nasa.gsfc.seadas.processing.utilities;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * This class implements a popup-menu
 * used to customize cell appearance.
 *
 * @version 1.0 July-2002
 * @author  Thierry Manfé
 */
class CellMenu extends JPopupMenu implements ActionListener {

  /**
   * Set this field to true and recompile
   * to get debug traces
   */
  public static final boolean DEBUG = false;

  static private final String _FOREGROUND = "Foreground";
  static private final String _BACKGROUND = "Background";
  static private final String _FONT       = "Font";
  static private final String _EDITABLE   = "Editable";

  //private SheetCell   _targetCells[];
  private Object      _targetCells[];
  private JWindow     _colorWindow;
  private SpreadSheet _sp;

  CellMenu(SpreadSheet parent) {

    _sp = parent;

    setDefaultLightWeightPopupEnabled(true);

    JMenuItem item = new JMenuItem(_FOREGROUND);
    item.addActionListener(this);
    add(item);
    item = new JMenuItem(_BACKGROUND);
    item.addActionListener(this);
    add(item);
    pack();
  }

  void setTargetCells( Object c[]) { _targetCells = c; }

  public void actionPerformed(ActionEvent ev) {

    if (DEBUG) System.out.println("Size of selection: "+_targetCells.length);

    if (ev.getActionCommand().equals(_FOREGROUND)) {
      setVisible(false);
      if (_colorWindow==null) new JWindow();
      Color col = JColorChooser.showDialog(_colorWindow,"Foreground Color",null);
      for (int ii=0; ii<_targetCells.length; ii++) {
	SheetCell sc = (SheetCell)_targetCells[ii];
	sc.foreground = col;
      }
      _sp.repaint();
    } else if (ev.getActionCommand().equals(_BACKGROUND)) {
      setVisible(false);
      if (_colorWindow==null) new JWindow();
      Color col = JColorChooser.showDialog(_colorWindow,"Background Color",null);
      for (int ii=0; ii<_targetCells.length; ii++) {
	SheetCell sc = (SheetCell)_targetCells[ii];
	sc.background = col;
      }
      _sp.repaint();
    }

  }

}