package hageldave.imagingkit.core.img;

import hageldave.imagingkit.core.pixel.PixelBase;

public interface BilinearInterpolation<P extends PixelBase<P>> extends ImgBase<P> {

	/**
	 * Returns a bilinearly interpolated value of the image for the
	 * specified channel at the
	 * specified normalized position (x and y within [0,1]). Position {0,0}
	 * denotes the image's origin (top left corner), position {1,1} denotes the
	 * opposite corner (pixel at {width-1, height-1}).
	 * <p>
	 * An ArrayIndexOutOfBoundsException may be thrown for x and y greater than 1
	 * or less than 0.
	 * @param xNormalized coordinate within [0,1]
	 * @param yNormalized coordinate within [0,1]
	 * @return bilinearly interpolated value for specified channel.
	 * @throws ArrayIndexOutOfBoundsException when a resulting index is out of
	 * the data array's bounds, which can only happen for x and y values less
	 * than 0 or greater than 1 
	 * or if the specified channel is not in [0,3] or is 3 but the image has no alpha (check using {@link #hasAlpha()}).
	 */
	public default double interpolate(final int channel, final double xNormalized, final double yNormalized){
		double xF = xNormalized * (getWidth()-1);
		double yF = yNormalized * (getHeight()-1);
		int x = (int)xF;
		int y = (int)yF;
		double c00 = getValueAt(channel, x, 							y);
		double c01 = getValueAt(channel, x, 						   (y+1 < getHeight() ? y+1:y));
		double c10 = getValueAt(channel, (x+1 < getWidth() ? x+1:x), 	y);
		double c11 = getValueAt(channel, (x+1 < getWidth() ? x+1:x),   (y+1 < getHeight() ? y+1:y));
		return interpolateBilinear(c00, c01, c10, c11, xF-x, yF-y);
	}
	
	/* bilinear interpolation between values c00 c01 c10 c11 at position mx my (in [0,1]) */
	public static double interpolateBilinear(final double c00, final double c01, final double c10, final double c11, final double mx, final double my){
		return (c00*(1.0-mx)+c10*(mx))*(1.0-my) + (c01*(1.0-mx)+c11*(mx))*(my);
	}
	
}
