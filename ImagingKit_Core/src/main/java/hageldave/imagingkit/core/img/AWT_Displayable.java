package hageldave.imagingkit.core.img;

import java.awt.image.BufferedImage;
import java.awt.image.Raster;

import hageldave.imagingkit.core.Dimensions;
import hageldave.imagingkit.core.util.BufferedImageFactory;

public interface AWT_Displayable extends Dimensions {
	
	/**
	 * Copies this image's data to the specified {@link BufferedImage}.
	 * This method will preserve the {@link Raster} of the specified
	 * BufferedImage and will only modify the contents of it.
	 * 
	 * @param bimg the BufferedImage
	 * @return the specified BufferedImage
	 * @throws IllegalArgumentException if the provided BufferedImage
	 * has a different dimension as this image.
	 * <br><i>Implementation Notice:</i> {@link #requireEqualDimensions(Dimensions, BufferedImage)} can be used to check.
	 * 
	 * @see #toBufferedImage()
	 * @see #getRemoteBufferedImage()
	 */
	public BufferedImage toBufferedImage(BufferedImage bimg);

	/**
	 * @return a BufferedImage of type INT_ARGB with this Img's data copied to it.
	 * 
	 * @see #toBufferedImage(BufferedImage)
	 * @see #getRemoteBufferedImage()
	 */
	public default BufferedImage toBufferedImage(){
		BufferedImage bimg = BufferedImageFactory.getINT_ARGB(getDimension());
		return toBufferedImage(bimg);
	}
	
	/**
	 * Throws an {@link IllegalArgumentException} if dimensions don't match.
	 * @param ownDims reference dimensions
	 * @param bimg image of which dimensions are checked
	 */
	public static void requireEqualDimensions(Dimensions ownDims, BufferedImage bimg){
		if(ownDims.getWidth() != bimg.getWidth() || ownDims.getHeight() != bimg.getHeight())
			throw new IllegalArgumentException(
					String.format(
							"Specified BufferedImage does not match dimensions: required (%dx%d) but has (%dx%d)",
							ownDims.getWidth(),ownDims.getHeight(),
							bimg.getWidth(),bimg.getHeight()));
	}
	
}
