package hageldave.imagingkit.core;

public interface PixelBase {

	/** 
	 * @return alpha value of this pixel with 0.0 as fully transparent and 1.0 as fully opaque. 
	 * May exceed [0,1] range depending on implementation.
	 */
	public double a_asDouble();

	/** 
	 * @return red value of this pixel with 0.0 as no red contribution and 1.0 as full red contribution.
	 * May exceed [0,1] range depending on implementation.
	 */
	public double r_asDouble();

	/** 
	 * @return green value of this pixel with 0.0 as no green contribution and 1.0 as full green contribution.
	 * May exceed [0,1] range depending on implementation.
	 */
	public double g_asDouble();

	/** 
	 * @return blue value of this pixel with 0.0 as no blue contribution and 1.0 as full blue contribution.
	 * May exceed [0,1] range depending on implementation.
	 */
	public double b_asDouble();

	/**
	 * Sets the alpha, red, green and blue value of this pixel.
	 * Assumed minimum and maximum value for each channel is 0.0 and 1.0 
	 * but values may exceed [0,1] range depending on implementation.
	 * @param a alpha value (0=transparent, 1=opaque)
	 * @param r red value
	 * @param g green value
	 * @param b blue value
	 */
	public void setARGB_fromDouble(double a, double r, double g, double b);

	/**
	 * Sets the red, green and blue value of this pixel. The alpha value is set to 1.0 (opaque).
	 * Assumed minimum and maximum value for each channel is 0.0 and 1.0 
	 * but values may exceed [0,1] range depending on implementation.
	 * @param r red value
	 * @param g green value
	 * @param b blue value
	 */
	public void setRGB_fromDouble(double r, double g, double b);

	/**
	 * Sets the red, green and blue value of this pixel. The alpha value is preserved.
	 * Assumed minimum and maximum value for each channel is 0.0 and 1.0 
	 * but values may exceed [0,1] range depending on implementation.
	 * @param r red value
	 * @param g green value
	 * @param b blue value
	 */
	public void setRGB_fromDouble_preserveAlpha(double r, double g, double b);

	public int getX();

	public int getY();

	/**
	 * Returns the normalized x coordinate of this Pixel.
	 * This will return 0 for Pixels at the left boundary and 1 for Pixels
	 * at the right boundary of the Img.<br>
	 * <em>For Img's that are only 1 Pixel wide, <u>NaN</u> is returned.</em>
	 * @return normalized x coordinate within [0..1]
	 */
	public default double getXnormalized(){
		return getX()*1.0/(getSource().getWidth()-1.0);
	}

	/**
	 * Returns the normalized y coordinate of this Pixel.
	 * This will return 0 for Pixels at the upper boundary and 1 for Pixels
	 * at the lower boundary of the Img.<br>
	 * <em>For Img's that are only 1 Pixel high, <u>NaN</u> is returned.</em>
	 * @return normalized y coordinate within [0..1]
	 * @since 1.2
	 */
	public default double getYnormalized(){
		return getY()*1.0/(getSource().getHeight()-1.0);
	}
	
	public int getIndex();
	
	public void setIndex(int index);
	
	public void setPosition(int x, int y);
	
	public ImgBase<?> getSource();

}
