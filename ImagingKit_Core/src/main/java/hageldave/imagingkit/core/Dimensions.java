package hageldave.imagingkit.core;

import java.awt.Dimension;

public interface Dimensions {

	
	/**
	 * @return the width of this image (number of pixels in horizontal direction)
	 * 
	 * @see #getHeight()
	 * @see #getDimension()
	 * @see #numValues()
	 */
	public int getWidth();

	/**
	 * @return the height of this image (number of pixels in vertical direction)
	 * 
	 * @see #getWidth()
	 * @see #getDimension()
	 * @see #numValues()
	 */
	public int getHeight();

	/**
	 * @return the dimension (width,height) of this image
	 * 
	 * @see #getWidth()
	 * @see #getHeight()
	 * @see #numValues()
	 */
	public default Dimension getDimension(){ return new Dimension(getWidth(),getHeight());}

	/**
	 * @return the number of pixels of this image
	 * 
	 * @see #getWidth()
	 * @see #getHeight()
	 * @see #getDimension()
	 */
	public default int numValues(){return getWidth()*getHeight();}
	
	/**
	 * Returns the aspect ratio of the dimensions (width/height).
	 * <p>
	 * A value less than 1 means higher than wide (portrait), 
	 * 1 mean square shaped, 
	 * greater than 1 means wider than high (landscape)
	 * 
	 * @return aspect ratio
	 */
	public default double aspectRatio(){return getWidth()*1.0/getHeight();}
	
	
}
