package hageldave.imagingkit.core;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.image.BufferedImage;

public class BufferedImageFactory {

	public static BufferedImage getINT_ARGB(Image img){
		return get(img, BufferedImage.TYPE_INT_ARGB);
	}
	
	public static BufferedImage get(Image img, int imgType){
		BufferedImage bimg = new BufferedImage(img.getWidth(TheImageObserver.OBS_WIDTHHEIGHT), img.getHeight(TheImageObserver.OBS_WIDTHHEIGHT), imgType);
		Graphics2D gr2D = bimg.createGraphics();
		gr2D.drawImage(img, 0, 0, TheImageObserver.OBS_ALLBITS);
		gr2D.dispose();
		
		return bimg;
	}
	
	public static BufferedImage getINT_ARGB(Dimension d){
		return getINT_ARGB(d.width, d.height);
	}
	
	public static BufferedImage getINT_ARGB(int width, int height){
		return new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
	}
}
