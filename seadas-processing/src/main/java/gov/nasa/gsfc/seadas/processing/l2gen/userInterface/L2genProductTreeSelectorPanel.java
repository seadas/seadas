package gov.nasa.gsfc.seadas.processing.l2gen.userInterface;

import gov.nasa.gsfc.seadas.processing.core.L2genData;
import gov.nasa.gsfc.seadas.processing.l2gen.productData.L2genBaseInfo;
import gov.nasa.gsfc.seadas.processing.l2gen.productData.L2genProductCategoryInfo;
import gov.nasa.gsfc.seadas.processing.l2gen.productData.L2genWavelengthInfo;
import gov.nasa.gsfc.seadas.processing.common.GridBagConstraintsCustom;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.tree.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.Enumeration;
import java.util.EventObject;

/**
 * Created by IntelliJ IDEA.
 * User: knowles
 * Date: 5/11/12
 * Time: 2:28 PM
 * To change this template use File | Settings | File Templates.
 */
public class L2genProductTreeSelectorPanel extends JPanel {

    private L2genData l2genData;
    private DefaultMutableTreeNode rootNode;
    private JTree productJTree;


    L2genProductTreeSelectorPanel(L2genData l2genData) {

        this.l2genData = l2genData;

        initComponents();
        addComponents();
    }


    public void initComponents() {
        productJTree = createProductJTree();
    }


    public void addComponents() {
        setLayout(new GridBagLayout());
        setBorder(BorderFactory.createTitledBorder("Product Selector"));

        JScrollPane jScrollPane = new JScrollPane(productJTree);
        jScrollPane.setBorder(null);
        add(jScrollPane,
                new GridBagConstraintsCustom(0, 0, 1, 1, GridBagConstraints.NORTHWEST, GridBagConstraints.BOTH));
    }

    private L2genTristateCheckBox.State getCheckboxState(L2genBaseInfo.State state) {
        switch (state) {
            case SELECTED:
                return L2genTristateCheckBox.SELECTED;
            case PARTIAL:
                return L2genTristateCheckBox.PARTIAL;
            default:
                return L2genTristateCheckBox.NOT_SELECTED;
        }
    }

    private L2genBaseInfo.State getInfoState(L2genTristateCheckBox.State state) {
        if (state == L2genTristateCheckBox.SELECTED) {
            return L2genBaseInfo.State.SELECTED;
        }
        if (state == L2genTristateCheckBox.PARTIAL) {
            return L2genBaseInfo.State.PARTIAL;
        }
        return L2genBaseInfo.State.NOT_SELECTED;

    }


    class CheckBoxNodeRenderer implements TreeCellRenderer {
        private JPanel nodeRenderer = new JPanel();
        private JLabel label = new JLabel();
        private L2genTristateCheckBox check = new L2genTristateCheckBox();

        Color selectionBorderColor, selectionForeground, selectionBackground,
                textForeground, textBackground;

        protected L2genTristateCheckBox getJCheckBox() {
            return check;
        }

        public CheckBoxNodeRenderer() {
            Insets inset0 = new Insets(0, 0, 0, 0);
            check.setMargin(inset0);
            nodeRenderer.setLayout(new BorderLayout());
            nodeRenderer.add(check, BorderLayout.WEST);
            nodeRenderer.add(label, BorderLayout.CENTER);

            Font fontValue;
            fontValue = UIManager.getFont("Tree.font");
            if (fontValue != null) {
                check.setFont(fontValue);
                label.setFont(fontValue);
            }
            Boolean booleanValue = (Boolean) UIManager
                    .get("Tree.drawsFocusBorderAroundIcon");
            check.setFocusPainted((booleanValue != null)
                    && (booleanValue.booleanValue()));

            selectionBorderColor = UIManager.getColor("Tree.selectionBorderColor");
            selectionForeground = UIManager.getColor("Tree.selectionForeground");
            selectionBackground = UIManager.getColor("Tree.selectionBackground");
            textForeground = UIManager.getColor("Tree.textForeground");
            textBackground = UIManager.getColor("Tree.textBackground");
        }

        public Component getTreeCellRendererComponent(JTree tree, Object value,
                                                      boolean selected, boolean expanded, boolean leaf, int row,
                                                      boolean hasFocus) {

            String stringValue = null;
            L2genBaseInfo.State state = L2genBaseInfo.State.NOT_SELECTED;

            if ((value != null) && (value instanceof DefaultMutableTreeNode)) {
                Object userObject = ((DefaultMutableTreeNode) value).getUserObject();
                if (userObject instanceof L2genBaseInfo) {
                    L2genBaseInfo info = (L2genBaseInfo) userObject;
                    state = info.getState();
                    stringValue = info.getFullName();

                    tree.setToolTipText(info.getDescription());
                }
            }

            if (stringValue == null) {
                stringValue = tree.convertValueToText(value, selected, expanded, leaf, row, false);
            }

            label.setText(stringValue);
            check.setState(getCheckboxState(state));
            check.setEnabled(tree.isEnabled());

            if (selected) {
                label.setForeground(selectionForeground);
                check.setForeground(selectionForeground);
                nodeRenderer.setForeground(selectionForeground);
                label.setBackground(selectionBackground);
                check.setBackground(selectionBackground);
                nodeRenderer.setBackground(selectionBackground);
            } else {
                label.setForeground(textForeground);
                check.setForeground(textForeground);
                nodeRenderer.setForeground(textForeground);
                label.setBackground(textBackground);
                check.setBackground(textBackground);
                nodeRenderer.setBackground(textBackground);
            }

//            if (((DefaultMutableTreeNode) value).getParent() == null) {
//                check.setAutoTab(false);
//            }

            L2genBaseInfo baseInfo = (L2genBaseInfo) ((DefaultMutableTreeNode) value).getUserObject();

            if (baseInfo instanceof L2genProductCategoryInfo) {
                check.setVisible(false);
            } else {
                check.setVisible(true);
            }

            return nodeRenderer;
        }
    }

    class CheckBoxNodeEditor extends AbstractCellEditor implements TreeCellEditor {

        CheckBoxNodeRenderer renderer = new CheckBoxNodeRenderer();
        JTree tree;
        DefaultMutableTreeNode currentNode;

        public CheckBoxNodeEditor(JTree tree) {
            this.tree = tree;

            // add a listener fo the check box
            ItemListener itemListener = new ItemListener() {
                public void itemStateChanged(ItemEvent itemEvent) {
                    L2genTristateCheckBox.State state = renderer.getJCheckBox().getState();

                    if (stopCellEditing()) {
                        fireEditingStopped();
                    }
                }
            };
            //renderer.getJCheckBox().addItemListener(itemListener);
            renderer.getJCheckBox().addChangeListener(new ChangeListener() {
                @Override
                public void stateChanged(ChangeEvent e) {
                    if (stopCellEditing()) {
                        fireEditingStopped();
                    }
                }
            });
            renderer.getJCheckBox().addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                }
            });


        }

        public Object getCellEditorValue() {

            L2genTristateCheckBox.State state = renderer.getJCheckBox().getState();

            setNodeState(currentNode, getInfoState(state));

            return currentNode.getUserObject();
        }

        public boolean isCellEditable(EventObject event) {

            return true;

        }

        public Component getTreeCellEditorComponent(JTree tree, Object value,
                                                    boolean selected, boolean expanded, boolean leaf, int row) {

            if (value instanceof DefaultMutableTreeNode) {
                currentNode = (DefaultMutableTreeNode) value;
            }

            Component editor = renderer.getTreeCellRendererComponent(tree, value,
                    true, expanded, leaf, row, true);

            return editor;
        }
    }


    private TreeNode createTree() {

        DefaultMutableTreeNode productCategory, product, oldAlgorithm, algorithm = null, wavelength;

        oldAlgorithm = new DefaultMutableTreeNode();
        L2genBaseInfo oldAInfo = null;

        rootNode = new DefaultMutableTreeNode(new L2genBaseInfo());

        for (L2genProductCategoryInfo productCategoryInfo : l2genData.getProductCategoryInfos()) {
            if (productCategoryInfo.isVisible() && productCategoryInfo.hasChildren()) {
                productCategory = new DefaultMutableTreeNode(productCategoryInfo);
                rootNode.add(productCategory);
                for (L2genBaseInfo pInfo : productCategoryInfo.getChildren()) {
                    product = new DefaultMutableTreeNode(pInfo);
                    for (L2genBaseInfo aInfo : pInfo.getChildren()) {
                        algorithm = new DefaultMutableTreeNode(aInfo);

                        if (algorithm.toString().equals(oldAlgorithm.toString())) {
                            if (oldAInfo.hasChildren()) {
                                if (aInfo.hasChildren()) {
                                    algorithm = oldAlgorithm;
                                } else {
                                    oldAlgorithm.add(algorithm);
                                }
                            } else {
                                if (aInfo.hasChildren()) {
                                    product.remove(oldAlgorithm);
                                    algorithm.add(oldAlgorithm);
                                    product.add(algorithm);
                                }
                            }
                        } else {
                            product.add(algorithm);
                        }

                        for (L2genBaseInfo wInfo : aInfo.getChildren()) {
                            wavelength = new DefaultMutableTreeNode(wInfo);
                            algorithm.add(wavelength);
                        }

                        oldAInfo = aInfo;
                        oldAlgorithm = algorithm;
                    }
                    if (pInfo.getChildren().size() == 1) {
                        productCategory.add(algorithm);
                    } else {
                        productCategory.add(product);
                    }
                }
            }
        }

        return rootNode;
    }


    public void checkTreeState(DefaultMutableTreeNode node) {

        l2genData.disableEvent(L2genData.L2PROD);
        L2genBaseInfo info = (L2genBaseInfo) node.getUserObject();
        L2genBaseInfo.State newState = info.getState();

        if (node.getChildCount() > 0) {
            Enumeration<DefaultMutableTreeNode> enumeration = node.children();
            DefaultMutableTreeNode kid;
            boolean selectedFound = false;
            boolean notSelectedFound = false;
            while (enumeration.hasMoreElements()) {
                kid = enumeration.nextElement();
                checkTreeState(kid);

                L2genBaseInfo childInfo = (L2genBaseInfo) kid.getUserObject();

                switch (childInfo.getState()) {
                    case SELECTED:
                        selectedFound = true;
                        break;
                    case PARTIAL:
                        selectedFound = true;
                        notSelectedFound = true;
                        break;
                    case NOT_SELECTED:
                        notSelectedFound = true;
                        break;
                }
            }

            if (selectedFound && !notSelectedFound) {
                newState = L2genBaseInfo.State.SELECTED;
            } else if (!selectedFound && notSelectedFound) {
                newState = L2genBaseInfo.State.NOT_SELECTED;
            } else if (selectedFound && notSelectedFound) {
                newState = L2genBaseInfo.State.PARTIAL;
            }

        } else {
            if (newState == L2genBaseInfo.State.PARTIAL) {
                newState = L2genBaseInfo.State.SELECTED;
                debug("in checkAlgorithmState converted newState to " + newState);
            }
        }

        if (newState != info.getState()) {
            l2genData.setSelectedInfo(info, newState);

        }

        l2genData.enableEvent(L2genData.L2PROD);
    }


    public void setNodeState(DefaultMutableTreeNode node, L2genBaseInfo.State state) {

        debug("setNodeState called with state = " + state);

        if (node == null) {
            return;
        }

        L2genBaseInfo info = (L2genBaseInfo) node.getUserObject();

        if (state == info.getState()) {
            return;
        }

        l2genData.disableEvent(L2genData.L2PROD);

        if (node.getChildCount() > 0) {
            l2genData.setSelectedInfo(info, state);

            Enumeration<DefaultMutableTreeNode> enumeration = node.children();
            DefaultMutableTreeNode childNode;

            L2genBaseInfo.State newState = state;

            while (enumeration.hasMoreElements()) {
                childNode = enumeration.nextElement();

                L2genBaseInfo childInfo = (L2genBaseInfo) childNode.getUserObject();

                if (childInfo instanceof L2genWavelengthInfo) {
                    if (state == L2genBaseInfo.State.PARTIAL) {
                        if (l2genData.compareWavelengthLimiter((L2genWavelengthInfo) childInfo)) {
                            newState = L2genBaseInfo.State.SELECTED;
                        } else {
                            newState = L2genBaseInfo.State.NOT_SELECTED;
                        }
                    }
                }

                setNodeState(childNode, newState);
            }

            DefaultMutableTreeNode ancestorNode;
            DefaultMutableTreeNode targetNode = node;
            ancestorNode = (DefaultMutableTreeNode) node.getParent();

            while (ancestorNode.getParent() != null) {
                targetNode = ancestorNode;
                ancestorNode = (DefaultMutableTreeNode) ancestorNode.getParent();
            }

            checkTreeState(targetNode);

        } else {
            if (state == L2genBaseInfo.State.PARTIAL) {
                l2genData.setSelectedInfo(info, L2genBaseInfo.State.SELECTED);
            } else {
                l2genData.setSelectedInfo(info, state);
            }
        }

        l2genData.enableEvent(L2genData.L2PROD);
    }


    private void updateProductTreePanel() {

        TreeNode rootNode = createTree();
        productJTree.setModel(new DefaultTreeModel(rootNode, false));

    }


    private JTree createProductJTree() {

        TreeNode rootNode = createTree();
        productJTree = new JTree(rootNode);
        productJTree.setCellRenderer(new CheckBoxNodeRenderer());
        productJTree.setCellEditor(new CheckBoxNodeEditor(productJTree));
        productJTree.setEditable(true);
        productJTree.setShowsRootHandles(true);
        productJTree.setRootVisible(false);


        l2genData.addPropertyChangeListener(L2genData.IFILE, new PropertyChangeListener() {
            @Override
            public void propertyChange(PropertyChangeEvent evt) {
                updateProductTreePanel();
            }
        });

        l2genData.addPropertyChangeListener(L2genData.L2PROD, new PropertyChangeListener() {
            @Override
            public void propertyChange(PropertyChangeEvent evt) {
                productChangedHandler();
            }
        });

        return productJTree;
    }


    private void productChangedHandler() {

        productJTree.treeDidChange();
        checkTreeState(rootNode);
    }

    private void debug(String message) {

    }
}
