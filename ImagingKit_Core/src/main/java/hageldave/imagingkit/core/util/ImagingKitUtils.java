package hageldave.imagingkit.core.util;

import hageldave.imagingkit.core.ImgBase;

public final class ImagingKitUtils {
	static{new ImagingKitUtils();}
	private ImagingKitUtils() {}


	public static final int clamp_0_255(int val){
		return Math.max(0, Math.min(val, 255));
	}

	public static double clamp_0_1(double val){
		return Math.max(0.0, Math.min(val, 1.0));
	}
	
	public static void requireAreaInImageBounds(final int xStart, final int yStart, final int width, final int height, ImgBase<?> img){
		if(		width <= 0 || height <= 0 ||
				xStart < 0 || yStart < 0 ||
				xStart+width > img.getWidth() || yStart+height > img.getHeight() )
		{
			throw new IllegalArgumentException(String.format(
							"provided area [%d,%d][%d,%d] is not within bounds of the image [%d,%d]",
							xStart,yStart,width,height, img.getWidth(), img.getHeight()));
		}
	}

}
