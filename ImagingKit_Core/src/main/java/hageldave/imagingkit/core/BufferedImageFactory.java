package hageldave.imagingkit.core;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.image.BufferedImage;

/**
 * Class providing convenience methods for converting Images to BufferedImages.
 * @author hageldave
 */
public class BufferedImageFactory {

	/**
	 * shortcut for get(img, BufferedImage.TYPE_INT_ARGB).
	 * @param img
	 * @return a BufferedImage copy of the provided Image
	 */
	public static BufferedImage getINT_ARGB(Image img){
		return get(img, BufferedImage.TYPE_INT_ARGB);
	}
	
	/**
	 * Creates a new BufferedImage of the specified imgType and same size as 
	 * the provided image and draws the provided Image onto the new BufferedImage.
	 * @param img to be copied to BufferedImage
	 * @param imgType of the BufferedImage. See 
	 * {@link BufferedImage#BufferedImage(int, int, int)} for details on the
	 * available imgTypes.
	 * @return a BufferedImage copy of the provided Image
	 */
	public static BufferedImage get(Image img, int imgType){
		BufferedImage bimg = new BufferedImage(img.getWidth(TheImageObserver.OBS_WIDTHHEIGHT), img.getHeight(TheImageObserver.OBS_WIDTHHEIGHT), imgType);
		Graphics2D gr2D = bimg.createGraphics();
		gr2D.drawImage(img, 0, 0, TheImageObserver.OBS_ALLBITS);
		gr2D.dispose();
		
		return bimg;
	}
	
	/**
	 * Instancing method for BufferedImage of type {@link BufferedImage#TYPE_INT_ARGB}
	 * @param d dimension of the created BufferedImage
	 * @return a new BufferedImage of specified dimension and type TYPE_INT_ARGB
	 */
	public static BufferedImage getINT_ARGB(Dimension d){
		return getINT_ARGB(d.width, d.height);
	}
	
	/**
	 * Instancing method for BufferedImage of type {@link BufferedImage#TYPE_INT_ARGB}
	 * @param width of the created BufferedImage
	 * @param height of the created BufferedImage
	 * @return a new BufferedImage of specified dimension and type TYPE_INT_ARGB
	 */
	public static BufferedImage getINT_ARGB(int width, int height){
		return new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
	}
}
