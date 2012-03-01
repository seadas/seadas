package gov.nasa.gsfc.seadas.sandbox.toolwindow;

import org.esa.beam.framework.ui.application.support.AbstractToolView;

import javax.swing.*;

/**
 * todo - Javadoc me!
 *
 * @author Danny Knowles
 */
public class AudioPlayerToolView extends AbstractToolView {

    public AudioPlayerToolView() {
    }

    @Override
    protected JComponent createControl() {
        AudioPlayer audioPlayer = new AudioPlayer();
        return audioPlayer.getContentPane();
    }


}
