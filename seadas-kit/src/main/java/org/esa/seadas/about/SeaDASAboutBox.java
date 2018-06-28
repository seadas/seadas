/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.esa.seadas.about;

import org.esa.snap.rcp.about.AboutBox;
import org.esa.snap.rcp.util.BrowserUtils;
import org.openide.modules.ModuleInfo;
import org.openide.modules.Modules;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.net.URI;
import java.net.URISyntaxException;

/**
 * @author Norman
 */
@AboutBox(displayName = "SeaDASBX", position = 30)
public class SeaDASAboutBox extends JPanel {

    private final static String releaseNotesHTTP = "https://github.com/senbox-org/s3tbx/blob/master/ReleaseNotes.md";

    public SeaDASAboutBox() {
        super(new BorderLayout(4, 4));
        setBorder(new EmptyBorder(4, 4, 4, 4));
        ImageIcon aboutImage = new ImageIcon(SeaDASAboutBox.class.getResource("about_seadas.jpg"));
        JLabel iconLabel = new JLabel(aboutImage);
        add(iconLabel, BorderLayout.CENTER);
        add(createVersionPanel(), BorderLayout.SOUTH);
    }

    private JPanel createVersionPanel() {
        final JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.LINE_AXIS));
        final ModuleInfo moduleInfo = Modules.getDefault().ownerOf(SeaDASAboutBox.class);
        panel.add(new JLabel("<html><b>SeaDAS Toolbox (SeaDASBX) version " + moduleInfo.getImplementationVersion() + "</b>",
                SwingConstants.RIGHT));
        final URI releaseNotesURI = getReleaseNotesURI();
        if (releaseNotesURI != null) {
            final JLabel releaseNoteLabel = new JLabel("<html><a href=\"" + releaseNotesURI.toString() + "\">Release Notes</a>",
                    SwingConstants.RIGHT);
            releaseNoteLabel.setCursor(new Cursor(Cursor.HAND_CURSOR));
            releaseNoteLabel.addMouseListener(new BrowserUtils.URLClickAdaptor(releaseNotesHTTP));
            panel.add(releaseNoteLabel);
        }
        return panel;
    }

    private URI getReleaseNotesURI() {
        try {
            return new URI(releaseNotesHTTP);
        } catch (URISyntaxException e) {
            return null;
        }
    }
}
