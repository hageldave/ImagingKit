/*
 * Copyright 2017 David Haegele
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to
 * deal in the Software without restriction, including without limitation the
 * rights to use, copy, modify, merge, publish, distribute, sublicense, and/or
 * sell copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING
 * FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS
 * IN THE SOFTWARE.
 */

package hageldave.imagingkit.core.pixel;

import java.util.Locale;

import hageldave.imagingkit.core.img.ImgBase;

/**
 * Basic interface for Pixel classes of {@link ImgBase} implementations.
 * <br>
 * An implementation of PixelBase stores a position and works like a pointer into the
 * data of an image.
 * An instance of PixelBase (or any implementation of it) is NOT the pixel value of an image at a
 * specific location and changing its position will not change that image.
 * Instead an instance is used to iterate the pixel values of an image. Such a pixel value can be 
 * retrieved and manipulated as long as the instance points to it.
 * 
 * @author hageldave
 * @since 2.0
 */
public interface PixelBase<SELF> {
	
	public int numChannels();
	
	public double getValue(int ch);
	
	public SELF setValue(int ch, double v);

	/**
	 * @return the x coordinate of this pixel's current position
	 * 
	 * @see #getY()
	 * @see #getXnormalized()
	 * @see #setPosition(int, int)
	 * @see #getIndex()
	 * @see #setIndex(int)
	 */
	public int getX();

	/**
	 * @return the y coordinate of this pixel's current position
	 * 
	 * @see #getX()
	 * @see #getYnormalized()
	 * @see #setPosition(int, int)
	 * @see #getIndex()
	 * @see #setIndex(int)
	 */
	public int getY();

	/**
	 * Returns the normalized x coordinate of this Pixel.
	 * This will return 0 for Pixels at the left boundary and 1 for Pixels
	 * at the right boundary of the Img.<br>
	 * <em>For Img's that are only 1 Pixel wide, <u>NaN</u> is returned.</em>
	 * @return normalized x coordinate within [0..1]
	 * 
	 * @see #getX()
	 * @see #getYnormalized()
	 * @see #setPosition(int, int)
	 * @see #getIndex()
	 * @see #setIndex(int)
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
	 * 
	 * @see #getY()
	 * @see #getXnormalized()
	 * @see #setPosition(int, int)
	 * @see #getIndex()
	 * @see #setIndex(int)
	 */
	public default double getYnormalized(){
		return getY()*1.0/(getSource().getHeight()-1.0);
	}

	/**
	 * Returns this pixel's index. 
	 * The index relates to the pixels's position in the following way:<br>
	 * <pre>
	 * index = y * width + x
	 *     y = index / width
	 *     x = index % width
	 * </pre>
	 * @return index of this pixel
	 * 
	 * @see #setIndex(int)
	 * @see #getX()
	 * @see #getY()
	 */
	public int getIndex();

	/**
	 * Sets the index of this pixel and thus its position.
	 * The index relates to the pixels's position in the following way:<br>
	 * <pre>
	 * index = y * width + x
	 *     y = index / width
	 *     x = index % width
	 * </pre>
	 * Index values outside the range [0, number of pixels] are not allowed
	 * but not necessarily enforced either. Accessing image data with an index/position
	 * outside of the underlying image's boundaries may cause an Exception.
	 * @param index to set the pixel to
	 * @return this pixel for chaining
	 * 
	 * @see #getIndex()
	 * @see #setPosition(int, int)
	 */
	public SELF setIndex(int index);

	/**
	 * Sets the position of this pixel to the specified coordinates.
	 * Positions outside of the underlying images boundaries are not allowed 
	 * but not necessarily enforced either. Accessing image data with a position
	 * outside of the boundaries may cause an Exception.
	 * @param x coordinate (0 left most, width-1 right most)
	 * @param y coordinate (0 top most, height-1 bottom most)
	 * @return this pixel for chaining
	 * 
	 * @see #getX()
	 * @see #getY()
	 * @see #setIndex(int)
	 */
	public SELF setPosition(int x, int y);

	/**
	 * Returns the underlying image of this pixel (the image referenced by this pixel)
	 * @return the image this pixel belongs to
	 * 
	 * @see ImgBase
	 */
	public ImgBase<?> getSource();

	/**
	 * Returns a String representation of this pixel at its current position.
	 * The returned String has the following pattern:<br>
	 * <em>Pixelclass</em>[a:<em>alpha</em> r:<em>red</em> g:<em>green</em> b:<em>blue</em>]@(<em>x</em>,<em>y</em>)
	 * @return string representation of this pixel at its current position
	 */
	public default String asString(){
		StringBuilder sb = new StringBuilder(32);
		sb.append(getClass().getSimpleName());
		sb.append("@(");
		sb.append(getX());
		sb.append(',');
		sb.append(getY());
		sb.append(")-[");
		int numChannels = numChannels();
		for(int ch=0; ch<numChannels; ch++){
			sb.append(String.format(Locale.US, "%.3f", getValue(ch)));
			if(ch < numChannels-1)
				sb.append(',');
		}
		return sb.toString();
	}

}
