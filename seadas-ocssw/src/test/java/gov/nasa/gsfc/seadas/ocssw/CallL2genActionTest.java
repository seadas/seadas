package gov.nasa.gsfc.seadas.ocssw;

import org.junit.Test;

import java.util.regex.Matcher;

import static org.junit.Assert.*;

/**
 * A test that deals with userInterface's stdout.
 *
 * @author Norman Fomferra
 * @since SeaDAS 7.0
 */
public class CallL2genActionTest {

    @Test
    public void testProgressPatternIdentifiesProgressLines() throws Exception {

        System.out.println("REGEX = " + CallL2genAction.PROCESSING_SCAN_PATTERN.pattern());

        Matcher matcher;

        matcher = CallL2genAction.PROCESSING_SCAN_PATTERN.matcher("Processing scan #     0 (1 of 4417) after      1 seconds");
        assertTrue(matcher.find());
        assertEquals(2, matcher.groupCount());
        assertEquals("1", matcher.group(1));
        assertEquals("4417", matcher.group(2));

        matcher = CallL2genAction.PROCESSING_SCAN_PATTERN.matcher("Processing scan #  1200 (1201 of 4417) after     66 seconds");
        assertTrue(matcher.find());
        assertEquals(2, matcher.groupCount());
        assertEquals("1201", matcher.group(1));
        assertEquals("4417", matcher.group(2));
    }

    @Test
    public void testProgressPatternIdentifiesNonProgressLines() throws Exception {
        Matcher matcher;

        matcher = CallL2genAction.PROCESSING_SCAN_PATTERN.matcher("Number of input pixels per scan is 4481");
        assertFalse(matcher.find());

        matcher = CallL2genAction.PROCESSING_SCAN_PATTERN.matcher("Number of Diffuse Transmittance Wavelengths    15");
        assertFalse(matcher.find());
    }
}
