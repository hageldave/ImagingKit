package hageldave.imagingkit.core.util;

import hageldave.imagingkit.core.ImgBase;

/**
 * Utility methods used throughout ImagingKit.
 * @author hageldave
 */
public final class ImagingKitUtils {

	// not to be instantiated
	private ImagingKitUtils() {}


	/**
	 * Clamps a value to the range [0,255].
	 * Returns 0 for values less than 0, 255 for values greater than 255, 
	 * and the value it self when in range.
	 * @param val value to be clamped
	 * @return value clamped to [0,255]
	 */
	public static final int clamp_0_255(int val){
		return Math.max(0, Math.min(val, 255));
	}

	/**
	 * Clamps a value to the range [0.0, 1.0].
	 * Returns 0.0 for values less than 0, 1.0 for values greater than 1.0, 
	 * and the value it self when in range.
	 * @param val value to be clamped
	 * @return value clamped to [0.0, 1.0]
	 */
	public static double clamp_0_1(double val){
		return Math.max(0.0, Math.min(val, 1.0));
	}
	
	/**
	 * Throws an {@link IllegalArgumentException} when the specified area 
	 * is not within the bounds of the specified image, or if the area
	 * is not positive.
	 * This is used for parameter evaluation.
	 * 
	 * @param xStart left boundary of the area
	 * @param yStart top boundary of the area
	 * @param width of the area
	 * @param height of the area
	 * @param img the area has to fit in.
	 * @throws IllegalArgumentException when area not in image bounds or not area not positive
	 */
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
