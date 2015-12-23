package hageldave.imagingkit.core;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.awt.image.ImageObserver;


public class BufferedImageFactory {
	
	private static final ImageObserver obs = new TheImageObserver();

	public static BufferedImage getINT_ARGB(Image img){
		return get(img, BufferedImage.TYPE_INT_ARGB);
	}
	
	public static BufferedImage get(Image img, int color_model){
		BufferedImage bimg = new BufferedImage(img.getWidth(obs), img.getHeight(obs), color_model);
		Graphics2D gr2D = bimg.createGraphics();
		gr2D.drawImage(img, 0, 0, obs);
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
