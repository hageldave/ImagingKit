package hageldave.imagingkit.filter;

import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;

import hageldave.imagingkit.core.Img;
import hageldave.imagingkit.core.TheImageObserver;
import hageldave.imagingkit.filter.util.MiscUtils;

public class Resize {
	
	/** Nearest Neighbour interpolation */
	public static final Object INTERPOLATION_NN = RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR;
	/** Bi-Linear interpolation */
	public static final Object INTERPOLATION_BL = RenderingHints.VALUE_INTERPOLATION_BILINEAR;
	/** Bi-Cubic interpolation */
	public static final Object INTERPOLATION_BC = RenderingHints.VALUE_INTERPOLATION_BICUBIC;

	public static BufferedImage resize(BufferedImage img, int width, int height, Object interpolationStrategy){
		if(!MiscUtils.isAnyOf(interpolationStrategy, INTERPOLATION_NN, INTERPOLATION_BL, INTERPOLATION_BC)){
			interpolationStrategy = INTERPOLATION_BL;
		}
		BufferedImage scaled = new BufferedImage(width, height, img.getType());
		Graphics2D g2d = scaled.createGraphics();
		g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, interpolationStrategy);
		g2d.drawImage(img, 0, 0, width, height, 0, 0, img.getWidth(), img.getHeight(), TheImageObserver.OBS_ALLBITS);
		g2d.dispose();
		return scaled;
	}
	
	public static BufferedImage resize(BufferedImage img, int width, int height){
		return resize(img, width, height, null);
	}
	
	public static Img resize(Img img, int width, int height, Object interpolationStrategy){
		return Img.createRemoteImg(resize(img.getRemoteBufferedImage(), width, height, interpolationStrategy));
	}
	
	public static Img resize(Img img, int width, int height){
		return resize(img, width, height, null);
	}
	
}
