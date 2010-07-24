/**
 * A tool for calculating "Data-Ink Ratio" measurements for UI design
 * metrics analysis.  Based on published works by Dr. Edward Tufte.
 *
 * @author  <a href="mailto:ceteri@gmail.com">Paco Nathan</a>
 * @version $Revision$
 *
 * Given the nature of the academic work on which this tool based,
 * this source code should most probably be made open source by NESI.
 *
 * See: Edward R. Tufte, The Visual Display of Quantitative Data,
 * Graphics Press, 1983, pp. 91-105.
 * http://www.edwardtufte.com/tufte/books_vdqi
 */

public class
    DataInk
{
    private final static int debug = 0 ; // 1


    /**
     * analyze one image
     */

    public
	DataInk (String file_name)
    {
	// load the image from a file

    	java.awt.Image image = loadImageFile(file_name);

	// use a label as a component to display the image

	javax.swing.JLabel label =
	    new javax.swing.JLabel(new javax.swing.ImageIcon(image));

	// tally the pixel counts as a histogram into a table

	javax.swing.JTable table =
	    populateTable(getPixelCount(image, label));

	// setup the frame with tabbed pane

	javax.swing.JFrame frame = new javax.swing.JFrame();
	javax.swing.JTabbedPane tabbedPane = new javax.swing.JTabbedPane();

	tabbedPane.setTabLayoutPolicy(javax.swing.JTabbedPane.SCROLL_TAB_LAYOUT);
	frame.getContentPane().add(tabbedPane, java.awt.BorderLayout.CENTER);

	// setup the tabs: image display and analysis table

	addTab(tabbedPane, file_name, label, "Shows the image analyzed for data-ink ratio");
	addTab(tabbedPane, "Pixels", table, "Shows the histogram of pixel counts");

	// display the results

	frame.pack();
	frame.setVisible(true);
    }


    /**
     * load an image from the named file
     */

    protected java.awt.Image
	loadImageFile (String file_name)
    {
    	java.awt.Image image = null;

	try {
	    image = javax.imageio.ImageIO.read(new java.io.File(file_name));
	}
	catch (java.io.IOException e) {
	    System.err.println(e.getMessage());
	    System.exit(-1);
	}

	// return the results

	return image;
    }


    /**
     * build a hashtable for tallying the pixel count as a histogram
     */

    protected java.util.Hashtable
	getPixelCount (java.awt.Image image, javax.swing.JLabel label)
    {
	java.util.Hashtable hash = new java.util.Hashtable();

	// get the image dimensions

	int w = image.getWidth(label);
	int h = image.getHeight(label);

	if (debug > 0) {
	    System.out.println("dim:" + " w = " + w + " h = " + h);
	}

	// fetch the image pixelmap

	int[] pixel = new int[w * h];

	java.awt.image.PixelGrabber pg =
	    new java.awt.image.PixelGrabber(image, 0, 0, w, h, pixel, 0, w);

	try {
	    pg.grabPixels();
	}
	catch (InterruptedException e) {
	    System.err.println("interrupted waiting for pixel!");
	    System.exit(-1);
	}

	// error check the fetched image pixelmap

	if ((pg.getStatus() & java.awt.image.ImageObserver.ABORT) != 0) {
	    System.err.println("image fetch aborted or errored");
	    System.exit(-1);
	}

	// iterate through the XY grid

	for (int j = 0; j < h; j++) {
	    for (int i = 0; i < w; i++) {
		int pix = pixel[j * w + i];
		String pix_string = Integer.toHexString(pix).toUpperCase();

		int a = (pix & 0xff000000) >> 24;
		int r = (pix & 0x00ff0000) >> 16;
		int g = (pix & 0x0000ff00) >> 8;
		int b = (pix & 0x000000ff);

		int rgb = pix & 0x00ffffff;

		String rgb_string =
		    "0x" + Integer.toHexString(rgb | 0xff000000).toUpperCase().substring(2);

		if (debug > 0) {
		    System.out.println("(" + i + ", " + j + ") = "
				       + a + ", "
				       + r + ", "
				       + g + ", "
				       + b + ", "
				       + rgb + ", "
				       + rgb_string + ", "
				       + pix_string
				       );
		}

		// tally histogram data for the pix counts

		Integer pix_count = (Integer) hash.get(rgb_string);

		if (pix_count == null) {
		    hash.put(rgb_string, new Integer(1));
		}
		else {
		    hash.put(rgb_string, new Integer(pix_count.intValue() + 1));
		}
	    }
	}

	return hash;
    }


    /**
     * populate the histogram data into the table
     */

    protected javax.swing.JTable
	populateTable (java.util.Hashtable hash)
    {
	// setup a table

	javax.swing.JTable table =
	    new javax.swing.JTable(hash.size() + 1, 2);

	table.setFont(new java.awt.Font("courier", java.awt.Font.PLAIN, 12));

	// sort the RGB values, from "white" to "black"

	String[] rgb_string = new String[hash.size()];
	int i = 0;

	for (java.util.Enumeration e = hash.keys(); e.hasMoreElements(); i++) {
	    rgb_string[i] = (String) e.nextElement();
	}

	java.util.Arrays.sort(rgb_string);

	// populate the table

	for (i = 0; i < hash.size(); i++) {
	    Integer pix_count = (Integer) hash.get(rgb_string[i]);

	    if (debug > 0) {
		System.out.println(rgb_string[i] + "\t" + pix_count);
	    }

	    table.setValueAt(rgb_string[i], i, 0);
	    table.setValueAt(pix_count, i, 1);
	}

	// return the results

	return table;
    }


    /**
     * add a scrolled component as a tab in the frame
     */

    protected void
	addTab (javax.swing.JTabbedPane tabbedPane, String title, java.awt.Component widget, String tooltip)
    {
        javax.swing.JPanel panel = new javax.swing.JPanel(false);
	javax.swing.JScrollPane scroll_pane = new javax.swing.JScrollPane(widget);

        panel.setLayout(new java.awt.GridLayout(1, 1));
        panel.add(scroll_pane);

	tabbedPane.addTab(title, null, panel, tooltip);
    }


    /**
     * take the image file names from command line arguments
     */

    public static void
	main (String args[])
    {
	if (args.length < 1) {
	    System.err.println("usage: java DataInk <image1> <image2> ...");
	    System.exit(-1);
	}

	for (int i = 0; i < args.length; i++) {
	    String file_name = args[i];
	    DataInk data_ink = new DataInk(file_name);
	}
    }
}
