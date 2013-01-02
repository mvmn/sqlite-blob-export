package x.mvmn.sqliteblobexporter.gui.util;

import java.awt.Dimension;
import java.awt.Toolkit;
import java.awt.Window;


/**
 * @author Mykola Makhin
 *
 */
public class SwingUtil {

	public static void moveWindowToScreenCenter(Window window) {
		window.setLocationRelativeTo(null);
	}
	
	public static void resizeWindowToBestFit(Window window, int marginPercent) {
		Toolkit toolkit = Toolkit.getDefaultToolkit();
		Dimension screenDimension = toolkit.getScreenSize();
		Dimension windowNewDimension = screenDimension;
		if(marginPercent>0 && marginPercent<100) {
			int width = screenDimension.width;
			int height = screenDimension.height;
			width = width - ((width*marginPercent)/100);
			height = height - ((height*marginPercent)/100);
			windowNewDimension = new Dimension(width, height);
		}
		
		window.setSize(windowNewDimension);
	}
}
