package gov.nasa.gsfc.seadas.writeimage;

import java.awt.Color;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.esa.beam.framework.datamodel.ColorPaletteDef;
import org.esa.beam.framework.datamodel.ColorPaletteDef.Point;

/**
 * Class containing helper methods for RGB image construction.
 *
 * TODO: a very basic implementation which works. Should be improved and unit tested.
 *
 * @author kutila
 */
public class RGBUtils
{

    /**
     * Load a default color palette definition.
     *
     * @param min
     * @param max
     * @param noData
     * @return
     */
    public static ColorPaletteDef buildColorPaletteDef(final double min, final double max)
    {
        System.out.println("Using default color palette definition");
        return new ColorPaletteDef(new ColorPaletteDef.Point[] { new ColorPaletteDef.Point(min, Color.BLACK),
                new ColorPaletteDef.Point(0.00, Color.DARK_GRAY), new ColorPaletteDef.Point(0.1, Color.WHITE),
                new ColorPaletteDef.Point(0.2, Color.YELLOW), new ColorPaletteDef.Point(0.3, Color.CYAN),
                new ColorPaletteDef.Point(0.4, Color.PINK), new ColorPaletteDef.Point(0.5, Color.ORANGE),
                new ColorPaletteDef.Point(1.0, Color.GREEN), new ColorPaletteDef.Point(2.0, Color.RED),
                new ColorPaletteDef.Point((min + max) / 2, Color.MAGENTA), new ColorPaletteDef.Point(max, Color.BLUE) });
    }

    /**
     * Load color palette definition from the specified look up table on file.
     *
     * @param min
     * @param max
     * @param noData
     * @param clutPath
     * @return
     */
    public static ColorPaletteDef buildColorPaletteDefFromFile(final double min, final double max, final double noData,
                                                               final String clutPath)
    {
        final List<Point> list = new ArrayList<Point>();

        // set NO Data color
        // list.add(new Point(noData, Color.GRAY));

        FileReader fr = null;
        BufferedReader br = null;
        try
        {
            fr = new FileReader(clutPath);
            br = new BufferedReader(fr);
            int i = 0;
            String line = br.readLine();
            while(line != null)
            {
                final String[] cv = line.trim().split("\\s+");
                if(cv.length != 3)
                {
                    throw new IOException("CLUT row should have 3 columns");
                }

                final double sample =
                        // i * max/256;
                        Math.pow(i * max / 256 + min, 10);
                final Color color = new Color(Integer.valueOf(cv[0]), Integer.valueOf(cv[1]), Integer.valueOf(cv[2]));
                list.add(new Point(sample, color));

                // System.out.println("(" + color.getRed() + " " + color.getGreen() + " " +
                // color.getBlue() + " ) "
                // + sample);

                i++;
                line = br.readLine();
            }

            final ColorPaletteDef cpd = new ColorPaletteDef(list.toArray(new Point[0]));

            return cpd;
        }
        catch(final IOException e)
        {
            System.out.println("Ignored exception");
        }
        finally
        {
            try
            {
                if(br != null)
                {
                    br.close();
                }
                if(fr != null)
                {
                    fr.close();
                }
            }
            catch(final Exception e)
            {
            }
        }

        return RGBUtils.buildColorPaletteDef(min, max);
    }

}