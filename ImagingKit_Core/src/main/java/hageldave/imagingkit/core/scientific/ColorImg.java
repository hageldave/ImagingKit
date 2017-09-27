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

package hageldave.imagingkit.core.scientific;

import java.awt.Dimension;
import java.awt.color.ColorSpace;
import java.awt.image.BandedSampleModel;
import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.awt.image.ComponentColorModel;
import java.awt.image.DataBuffer;
import java.awt.image.DataBufferDouble;
import java.awt.image.Raster;
import java.awt.image.SampleModel;
import java.awt.image.WritableRaster;
import java.util.Arrays;
import java.util.Objects;
import java.util.Spliterator;
import java.util.function.Consumer;
import java.util.function.DoubleToIntFunction;

import hageldave.imagingkit.core.Img;
import hageldave.imagingkit.core.ImgBase;
import hageldave.imagingkit.core.Pixel;
import hageldave.imagingkit.core.PixelBase;
import hageldave.imagingkit.core.util.ImageFrame;
import hageldave.imagingkit.core.util.ImagingKitUtils;

/**
 * The ColorImg class provides defines a 2D Image with 3 (4 with alpha) channels 
 * for RGB (ARGB) values.
 * <p>
 * In contrast to the standard {@link Img} class, ColorImg uses
 * double precision values for each channel of a pixel, whereas Img uses
 * 8bit discrete values per channel of a pixel. <br>
 * ColorImg is thus way more resource 'hungry' as Img 
 * (32byte (24byte without alpha) vs 4byte per pixel). 
 * This accuracy is meant for use in scientific image processing, e.g.
 * frequency analysis, medical imaging, vector field visualitzation or the like.
 * <p>
 * Its pixel class {@link ColorPixel} provides, appart from the methods 
 * defined in {@link PixelBase}, methods for vector operations treating the
 * RGB values of the pixel as 3D vector.
 * 
 * @author hageldave
 * @since 2.0
 */
public class ColorImg implements ImgBase<ColorPixel> {

	/** boundary mode that will return 0 for out of bounds positions.
	 * @see #getValue(int channel, int x, int y, int mode)
	 */
	public static final int boundary_mode_zero = Img.boundary_mode_zero;

	/** boundary mode that will repeat the edge of of an image for out of
	 * bounds positions.
	 * @see #getValue(int channel, int x, int y, int mode)
	 */
	public static final int boundary_mode_repeat_edge = Img.boundary_mode_repeat_edge;

	/** boundary mode that will repeat the image for out of bounds positions.
	 * @see #getValue(int channel, int x, int y, int mode)
	 */
	public static final int boundary_mode_repeat_image = Img.boundary_mode_repeat_image;

	/** boundary mode that will mirror the image for out of bounds positions
	 * @see #getValue(int channel, int x, int y, int mode)
	 */
	public static final int boundary_mode_mirror = Img.boundary_mode_mirror;

	/** red channel index */
	public static final int channel_r = 0;
	/** green channel index */
	public static final int channel_g = 1;
	/** blue channel index */
	public static final int channel_b = 2;
	/** alpha channel index */
	public static final int channel_a = 3;

	/* data arrays per channel */
	private final double[] dataR;
	private final double[] dataG;
	private final double[] dataB;
	private final double[] dataA;
	/* all data arrays */
	private final double[][] data;
	
	/* whether this image has an alpha channel */
	private final boolean hasAlpha;

	private final int width,height;

	/** minimum number of elements this image's {@link Spliterator}s can be split to.
	 * Default value is 1024.
	 * @since 1.3
	 */
	private int spliteratorMinimumSplitSize = 1024;


	/**
	 * Creates a new ColorImg of specified dimensions.
	 * Channel values are initialized to 0.
	 * @param width of the ColorImg
	 * @param height of the ColorImg
	 * @param alpha whether the created image has an alpha channel
	 */
	public ColorImg(int width, int height, boolean alpha){
		this.dataR = new double[width*height];
		this.dataG = new double[width*height];
		this.dataB = new double[width*height];
		this.hasAlpha = alpha;
		this.dataA = alpha ? new double[width*height]:null;
		this.data = alpha ? new double[][]{dataR,dataG,dataB,dataA}:new double[][]{dataR,dataG,dataB};
		this.width=width;
		this.height=height;
	}

	/**
	 * Creates a new ColorImg of specified Dimension.
	 * Channel values are initialized to 0.
	 * @param dimension extend of the ColorImg (width and height)
	 * @param alpha whether the created image has an alpha channel
	 */
	public ColorImg(Dimension dimension, boolean alpha){
		this(dimension.width, dimension.height, alpha);
	}

	/**
	 * Creates a new ColorImg of same dimensions as provided {@link Img}.
	 * Values are copied from argument image.
	 * 
	 * @param img the Img
	 * @param alpha whether the created image has an alpha channel
	 * @see #ColorImg(int, int, boolean)
	 * @see #ColorImg(Dimension, boolean)
	 * @see #ColorImg(Img, boolean)
	 * @see #ColorImg(BufferedImage)
	 * @see #ColorImg(int, int, double[], double[], double[], double[])
	 */
	public ColorImg(Img img, boolean alpha){
		this(img.getWidth(), img.getHeight(), alpha);
		for(int i=0; i<img.numValues();i++){
			int val = img.getData()[i];
			dataR[i] = Pixel.r_normalized(val);
			dataG[i] = Pixel.g_normalized(val);
			dataB[i] = Pixel.b_normalized(val);
			if(alpha){
				dataA[i] = Pixel.a_normalized(val);
			}
		}
	}

	/**
	 * Creates a new ColorImg of specified dimensions.
	 * Provided data arrays will be used as this images data.
	 * @param width of the ColorImg
	 * @param height of the ColorImg
	 * @param dataR array of red values (row major)
	 * @param dataG array of green values (row major)
	 * @param dataB array of blue values (row major)
	 * @param dataA array of alpha values (row major)(can be null when no alpha channel is desired)
	 * @throws IllegalArgumentException when the provided data arrays are not of the same length, 
	 * or if the number of pixels resulting from the specified dimension does not match the array length.
	 */
	public ColorImg(int width, int height, double[] dataR, double[] dataG, double[] dataB, double[] dataA){
		Objects.requireNonNull(dataR);
		Objects.requireNonNull(dataG);
		Objects.requireNonNull(dataB);
		hasAlpha = dataA != null;
		if(dataR.length != dataG.length || dataG.length != dataB.length || (hasAlpha && dataB.length != dataA.length)){
			throw new IllegalArgumentException(String.format("Provided data arrays are not of same size. R[%d] G[%d] B[%d]%s", dataR.length, dataG.length, dataB.length, hasAlpha ? " A["+dataA.length+"]":""));
		}
		if(width*height != dataR.length){
			throw new IllegalArgumentException(String.format("Provided Dimension (width=%d, height=$d) does not match number of provided Pixels %d", width, height, dataR.length));
		}
		this.width = width;
		this.height = height;
		this.dataR=dataR;
		this.dataG=dataG;
		this.dataB=dataB;
		this.dataA=dataA;
		this.data = hasAlpha ? new double[][]{dataR,dataG,dataB,dataA}:new double[][]{dataR,dataG,dataB};
	}


	/**
	 * Creates a new ColorImg from the specified {@link BufferedImage}.
	 * Therefore a ColorImage of equal dimension as the the argument image is created
	 * and the argument image is then painted on it.
	 * @param bimg BufferedImage from which a ColorImg is to be created.
	 */
	public ColorImg(BufferedImage bimg){
		this(bimg.getWidth(),bimg.getHeight(),bimg.getColorModel().hasAlpha());
		this.paint(g->g.drawImage(bimg, 0, 0, getWidth(), getHeight(), 0, 0, getWidth(), getHeight(), null));
	}

	/** @return true when this image has an alpha channel, else false */
	public boolean hasAlpha(){
		return hasAlpha;
	}

	@Override
	public int getWidth(){
		return this.width;
	}

	@Override
	public int getHeight(){
		return this.height;
	}

	@Override
	public int numValues(){
		return getWidth()*getHeight();
	}

	/**
	 * Returns the data arrays of this image in the following order:
	 * <pre>
	 * data[0]= redData
	 * data[1]= greenData
	 * data[2]= blueData
	 * (data[3]= alphaData)
	 * </pre>
	 * Depending on this image having an alpha channel or not, the returned array is
	 * of size 3 (no alpha) or 4 (with alpha).
	 * @return data arrays of this image
	 * 
	 * @see #getDataR()
	 * @see #getDataG()
	 * @see #getDataB()
	 * @see #getDataA()
	 * @see #getData()
	 */
	public double[][] getData() {
		return Arrays.copyOf(data, data.length);
	}

	/**
	 * @return the data array of the red channel (row major)
	 * @see #getDataR()
	 * @see #getDataG()
	 * @see #getDataB()
	 * @see #getDataA()
	 * @see #getData()
	 */
	public double[] getDataR() {
		return dataR;
	}

	/**
	 * @return the data array of the green channel (row major)
	 * @see #getDataR()
	 * @see #getDataG()
	 * @see #getDataB()
	 * @see #getDataA()
	 * @see #getData()
	 */
	public double[] getDataG() {
		return dataG;
	}

	/**
	 * @return the data array of the blue channel (row major)
	 * @see #getDataR()
	 * @see #getDataG()
	 * @see #getDataB()
	 * @see #getDataA()
	 * @see #getData()
	 */
	public double[] getDataB() {
		return dataB;
	}

	/**
	 * @return the data array of the alpha channel (row major). Null if this image has no alpha.
	 * @see #getDataR()
	 * @see #getDataG()
	 * @see #getDataB()
	 * @see #getDataA()
	 * @see #getData()
	 */
	public double[] getDataA() {
		return dataA;
	}
	
	/**
	 * Returns a ColorImage which uses the specified channel of this image, 
	 * for all its own channels. This, for example, comes in handy when only 
	 * a single channel of this image should be displayed with {@link ImageFrame}.<br>
	 * The image is created like this:<br>
	 * <pre>
	 * {@code
	 * double[] channelData = img.getData()[channel];
	 * ColorImg channelImg = new ColorImg(img.getWidth(), img.getHeight(), 
	 *    channelData, // red
	 *    channelData, // green
	 *    channelData, // blue
	 *    null);       // alpha
	 * }</pre>
	 * This means that the returned image has 3 redundant channels. Changes to
	 * that channel are reflected in the original image. Also setting a value
	 * for a specific channel of the channel image will result in the same value
	 * in all of its channels. <br>
	 * The assertions in the following code snippet are true for a channel image:<br>
	 * <pre>
	 * {@code
	 * ColorImg channelImg = img.getChannelImage(channel);
	 * assert(channelImg.getDataR() == channelImg.getDataG());
	 * assert(channelImg.getDataG() == channelImg.getDataB());
	 * }</pre>
	 * When using one of the setter methods for all channels of a pixel object of a
	 * channel image, (e.g. {@link ColorPixel#setRGB_fromDouble(double, double, double)})
	 * then the value specified for the blue channel will be used because it is set last.<br>
	 * The assertions in the following code snippet are true for a channel image:<br>
	 * <pre>
	 * {@code
	 * ColorImg channelImg = img.getChannelImage(channel);
	 * channelImg.getPixel(0,0).setRGB_fromDouble(1, 2, 3);
	 * assert(channelImg.getValue(channel_r, 0, 0) == 3);
	 * assert(channelImg.getValue(channel_g, 0, 0) == 3);
	 * assert(channelImg.getValue(channel_b, 0, 0) == 3);
	 * }</pre>
	 * 
	 * @param channel of this image that should be used by the returned channel image.
	 * One of {@link #channel_r},{@link #channel_g},{@link #channel_b},{@link #channel_a} (0,1,2,3).
	 * @return a ColorImg using the specified channel of this image for its r,g and b channel.
	 * @throws ArrayIndexOutOfBoundsException if the specified channel is not in [0,3] 
	 * or is 3 but the image has no alpha (check using {@link #hasAlpha()}).
	 */
	public ColorImg getChannelImage(int channel){
		double[] channelData = getData()[channel];
		return new ColorImg(getWidth(), getHeight(), channelData, channelData, channelData, null);
	}

	/**
	 * Returns the value of this image at the specified position for the specified channel.
	 * No bounds checks will be performed, positions outside of this
	 * image's dimension can either result in a value for a different position
	 * or an ArrayIndexOutOfBoundsException.
	 * @param x coordinate
	 * @param y coordinate
	 * @return value for specified position and channel
	 * @throws ArrayIndexOutOfBoundsException if resulting index from x and y
	 * is not within the data arrays bounds or if the specified channel is not in [0,3] 
	 * or is 3 but the image has no alpha (check using {@link #hasAlpha()}).
	 * 
	 * @see #getValue(int channel, int x, int y, int mode)
	 * @see #getValueR(int, int)
	 * @see #getValueG(int, int)
	 * @see #getValueB(int, int)
	 * @see #getValueA(int, int)
	 * @see #getPixel(int x, int y)
	 * @see #setValue(int channel, int x, int y, double val)
	 */
	public double getValue(final int channel, final int x, final int y){
		return this.data[channel][y*this.width + x];
	}

	/**
	 * Returns the red channel value of this image at the specified position.
	 * No bounds checks will be performed, positions outside of this
	 * image's dimension can either result in a value for a different position
	 * or an ArrayIndexOutOfBoundsException.
	 * @param x coordinate
	 * @param y coordinate
	 * @return red value for specified position
	 * @throws ArrayIndexOutOfBoundsException if resulting index from x and y
	 * is not within the data arrays bounds
	 * 
	 * @see #getValue(int channel , int x, int y)
	 * @see #getValueR(int x, int y, int mode)
	 */
	public double getValueR(final int x, final int y){
		return this.dataR[y*this.width + x];
	}

	/**
	 * Returns the green channel value of this image at the specified position.
	 * No bounds checks will be performed, positions outside of this
	 * image's dimension can either result in a value for a different position
	 * or an ArrayIndexOutOfBoundsException.
	 * @param x coordinate
	 * @param y coordinate
	 * @return green value for specified position
	 * @throws ArrayIndexOutOfBoundsException if resulting index from x and y
	 * is not within the data arrays bounds
	 * 
	 * @see #getValue(int channel , int x, int y)
	 * @see #getValueG(int x, int y, int mode)
	 */
	public double getValueG(final int x, final int y){
		return this.dataG[y*this.width + x];
	}

	/**
	 * Returns the blue channel value of this image at the specified position.
	 * No bounds checks will be performed, positions outside of this
	 * image's dimension can either result in a value for a different position
	 * or an ArrayIndexOutOfBoundsException.
	 * @param x coordinate
	 * @param y coordinate
	 * @return blue value for specified position
	 * @throws ArrayIndexOutOfBoundsException if resulting index from x and y
	 * is not within the data arrays bounds
	 * 
	 * @see #getValue(int channel , int x, int y)
	 * @see #getValueB(int x, int y, int mode)
	 */
	public double getValueB(final int x, final int y){
		return this.dataB[y*this.width + x];
	}

	/**
	 * Returns the alpha channel value of this image at the specified position.
	 * No bounds checks will be performed, positions outside of this
	 * image's dimension can either result in a value for a different position
	 * or an ArrayIndexOutOfBoundsException.
	 * @param x coordinate
	 * @param y coordinate
	 * @return alpha value for specified position
	 * @throws ArrayIndexOutOfBoundsException if resulting index from x and y
	 * is not within the data arrays bounds
	 * @throws NullPointerException if this image has no alpha channel (check using {@link #hasAlpha()})
	 * 
	 * @see #getValue(int channel , int x, int y)
	 * @see #getValueA(int x, int y, int mode)
	 */
	public double getValueA(final int x, final int y){
		return this.dataA[y*this.width + x];
	}

	/**
	 * Returns the value of this image at the specified position for the specified channel.
	 * Bounds checks will be performed and positions outside of this image's
	 * dimensions will be handled according to the specified boundary mode.
	 * <p>
	 * <b><u>Boundary Modes</u></b><br>
	 * {@link #boundary_mode_zero} <br>
	 * will return 0 for out of bounds positions.
	 * <br>
	 * -{@link #boundary_mode_repeat_edge} <br>
	 * will return the same value as the nearest edge value.
	 * <br>
	 * -{@link #boundary_mode_repeat_image} <br>
	 * will return a value of the image as if the if the image was repeated on
	 * all sides.
	 * <br>
	 * -{@link #boundary_mode_mirror} <br>
	 * will return a value of the image as if the image was mirrored on all
	 * sides.
	 * <br>
	 * -<u>other values for boundary mode </u><br>
	 * will be used as default color for out of bounds positions. It is safe
	 * to use opaque colors (0xff000000 - 0xffffffff) and transparent colors
	 * above 0x0000000f which will not collide with one of the boundary modes
	 * (number of boundary modes is limited to 16 for the future).
	 * @param channel one of {@link #channel_r},{@link #channel_g},{@link #channel_b},{@link #channel_a} (0,1,2,3)
	 * @param x coordinate
	 * @param y coordinate
	 * @param boundaryMode one of the boundary modes e.g. boundary_mode_mirror
	 * @return value at specified position or a value depending on the
	 * boundary mode for out of bounds positions.
	 * @throws ArrayIndexOutOfBoundsException if the specified channel is not in [0,3] 
	 * or is 3 but the image has no alpha (check using {@link #hasAlpha()}).
	 */
	public double getValue(final int channel, int x, int y, final int boundaryMode){
		if(x < 0 || y < 0 || x >= this.width || y >= this.height){
			switch (boundaryMode) {
			case boundary_mode_zero:
				return 0;
			case boundary_mode_repeat_edge:
				x = (x < 0 ? 0: (x >= this.width ? this.width-1:x));
				y = (y < 0 ? 0: (y >= this.height ? this.height-1:y));
				return getValue(channel, x, y);
			case boundary_mode_repeat_image:
				x = (this.width + (x % this.width)) % this.width;
				y = (this.height + (y % this.height)) % this.height;
				return getValue(channel, x,y);
			case boundary_mode_mirror:
				if(x < 0){ // mirror x to right side of image
					x = -x - 1;
				}
				if(y < 0 ){ // mirror y to bottom side of image
					y = -y - 1;
				}
				x = (x/this.width) % 2 == 0 ? (x%this.width) : (this.width-1)-(x%this.width);
				y = (y/this.height) % 2 == 0 ? (y%this.height) : (this.height-1)-(y%this.height);
				return getValue(channel, x, y);
			default:
				return boundaryMode; // boundary mode can be default color
			}
		} else {
			return getValue(channel, x, y);
		}
	}

	/**
	 * See {@link #getValue(int channel, int x, int y, int mode)} for details.
	 * This is a shortcut for {@code getValue(channel_r, x, y, boundaryMode)}.
	 * @param x coordinate
	 * @param y coordinate
	 * @param boundaryMode one of the boundary modes e.g. boundary_mode_mirror
	 * @return red value at specified position or a value depending on the
	 * boundary mode for out of bounds positions.
	 */
	public double getValueR(int x, int y, final int boundaryMode){
		return getValue(channel_r, x, y, boundaryMode);
	}

	/**
	 * See {@link #getValue(int channel, int x, int y, int mode)} for details.
	 * This is a shortcut for {@code getValue(channel_g, x, y, boundaryMode)}.
	 * @param x coordinate
	 * @param y coordinate
	 * @param boundaryMode one of the boundary modes e.g. boundary_mode_mirror
	 * @return green value at specified position or a value depending on the
	 * boundary mode for out of bounds positions.
	 */
	public double getValueG(int x, int y, final int boundaryMode){
		return getValue(channel_g, x, y, boundaryMode);
	}

	/**
	 * See {@link #getValue(int channel, int x, int y, int mode)} for details.
	 * This is a shortcut for {@code getValue(channel_b, x, y, boundaryMode)}.
	 * @param x coordinate
	 * @param y coordinate
	 * @param boundaryMode one of the boundary modes e.g. boundary_mode_mirror
	 * @return blue value at specified position or a value depending on the
	 * boundary mode for out of bounds positions.
	 */
	public double getValueB(int x, int y, final int boundaryMode){
		return getValue(channel_b, x, y, boundaryMode);
	}

	/**
	 * See {@link #getValue(int channel, int x, int y, int mode)} for details.
	 * This is a shortcut for {@code getValue(channel_a, x, y, boundaryMode)}.
	 * @param x coordinate
	 * @param y coordinate
	 * @param boundaryMode one of the boundary modes e.g. boundary_mode_mirror
	 * @return alpha value at specified position or a value depending on the
	 * boundary mode for out of bounds positions.
	 * @throws ArrayIndexOutOfBoundsException the image has no alpha (check using {@link #hasAlpha()}).
	 */
	public double getValueA(int x, int y, final int boundaryMode){
		return getValue(channel_a, x, y, boundaryMode);
	}
	
	/**
	 * Returns the index of the maximum value of the specified channel.
	 * @param channel one of {@link #channel_r},{@link #channel_g},{@link #channel_b},{@link #channel_a} (0,1,2,3)
	 * @return index of maximum value of specified channel
	 * @throws ArrayIndexOutOfBoundsException if the specified channel is not in [0,3] 
	 * or is 3 but the image has no alpha (check using {@link #hasAlpha()}).
	 * @see #getIndexOfMaxValue(int)
	 * @see #getIndexOfMinValue(int)
	 * @see #getMaxValue(int)
	 * @see #getMinValue(int)
	 */
	public int getIndexOfMaxValue(int channel){
		double[] values = getData()[channel];
		int index = 0;
		double val = values[index];
		for(int i = 1; i < numValues(); i++){
			if(values[i] > val){
				index = i;
				val = values[index];
			}
		}
		return index;
	}
	
	/**
	 * Returns the maximum value of the specified channel.
	 * @param channel one of {@link #channel_r},{@link #channel_g},{@link #channel_b},{@link #channel_a} (0,1,2,3)
	 * @return maximum value of the specified channel
	 * @throws ArrayIndexOutOfBoundsException if the specified channel is not in [0,3] 
	 * or is 3 but the image has no alpha (check using {@link #hasAlpha()}).
	 * @see #getIndexOfMaxValue(int)
	 * @see #getIndexOfMinValue(int)
	 * @see #getMaxValue(int)
	 * @see #getMinValue(int)
	 */
	public double getMaxValue(int channel){
		return getData()[channel][getIndexOfMaxValue(channel)];
	}
	
	/**
	 * Returns the index of the minimum value of the specified channel.
	 * @param channel one of {@link #channel_r},{@link #channel_g},{@link #channel_b},{@link #channel_a} (0,1,2,3)
	 * @return index of minimum value of specified channel
	 * @throws ArrayIndexOutOfBoundsException if the specified channel is not in [0,3] 
	 * or is 3 but the image has no alpha (check using {@link #hasAlpha()}).
	 * @see #getIndexOfMaxValue(int)
	 * @see #getIndexOfMinValue(int)
	 * @see #getMaxValue(int)
	 * @see #getMinValue(int)
	 */
	public int getIndexOfMinValue(int channel){
		double[] values = getData()[channel];
		int index = 0;
		double val = values[index];
		for(int i = 1; i < numValues(); i++){
			if(values[i] < val){
				index = i;
				val = values[index];
			}
		}
		return index;
	}
	
	/**
	 * Returns the minimum value of the specified channel.
	 * @param channel one of {@link #channel_r},{@link #channel_g},{@link #channel_b},{@link #channel_a} (0,1,2,3)
	 * @return minimum value of the specified channel
	 * @throws ArrayIndexOutOfBoundsException if the specified channel is not in [0,3] 
	 * or is 3 but the image has no alpha (check using {@link #hasAlpha()}).
	 * @see #getIndexOfMaxValue(int)
	 * @see #getIndexOfMinValue(int)
	 * @see #getMaxValue(int)
	 * @see #getMinValue(int)
	 */
	public double getMinValue(int channel){
		return getData()[channel][getIndexOfMinValue(channel)];
	}
	
	/**
	 * Clamps all values of the specified channel to unit range [0,1].
	 * Values less than 0 are set to zero, values greater than 1 are set to 1.
	 * @param channel one of {@link #channel_r},{@link #channel_g},{@link #channel_b},{@link #channel_a} (0,1,2,3)
	 * @return this for chaining
	 * @throws ArrayIndexOutOfBoundsException if the specified channel is not in [0,3] 
	 * or is 3 but the image has no alpha (check using {@link #hasAlpha()}).
	 * 
	 * @see #clampAllChannelsToUnitRange()
	 * @see #scaleChannelToUnitRange(int)
	 */
	public ColorImg clampChannelToUnitRange(int channel){
		double[] channelData = getData()[channel];
		for(int i=0; i<channelData.length; i++){
			channelData[i] = ImagingKitUtils.clamp_0_1(channelData[i]);
		}
		return this;
	}
	
	/**
	 * Clamps all values of all channels (including alpha if present) to unit range [0,1].
	 * Values less than 0 are set to zero, values greater than 1 are set to 1.
	 * @return this for chaining
	 * 
	 * @see #clampChannelToUnitRange(int)
	 */
	public ColorImg clampAllChannelsToUnitRange(){
		clampChannelToUnitRange(channel_r);
		clampChannelToUnitRange(channel_g);
		clampChannelToUnitRange(channel_b);
		if(hasAlpha) clampChannelToUnitRange(channel_a);
		return this;
	}
	
	/**
	 * Scales all values of the specified channel to unit range [0,1].
	 * This means that the values are shifted and scaled (proportionally) to fit in unit range.
	 * It is a 1-dimensional affine transform from the current value range [min,max] to [0,1].
	 * If all values are the same (min=max), the channel is set to 0.
	 * @param channel one of {@link #channel_r},{@link #channel_g},{@link #channel_b},{@link #channel_a} (0,1,2,3)
	 * @return this for chaining
	 * @throws ArrayIndexOutOfBoundsException if the specified channel is not in [0,3] 
	 * or is 3 but the image has no alpha (check using {@link #hasAlpha()}).
	 * 
	 * @see #scaleRGBToUnitRange()
	 * @see #clampChannelToUnitRange(int)
	 */
	public ColorImg scaleChannelToUnitRange(int channel) {
		double min=getMinValue(channel), max=getMaxValue(channel);
		double range = max-min;
		if(range != 0){
			double[] channelData = getData()[channel];
			for(int i=0; i<channelData.length; i++){
				channelData[i] = (channelData[i]-min)/range;
			}
		} else {
			fill(channel, 0);
		}
		return this;
	}
	
	/**
	 * Scales all values of the R,G and B channel to unit range [0,1].
	 * This means that the values are shifted and scaled (proportionally) to fit in unit range.
	 * It is a 1-dimensional affine transform from the current value range [min,max] to [0,1].
	 * If all values are the same (min=max), the channels are set to 0.
	 * <br><b>The global minimum and maximum of RGB are considered, channels are not treated seperately.</b>
	 * This is NOT equal to {@code scaleChannelToUnitRange(channel_r).scaleChannelToUnitRange(channel_g).scaleChannelToUnitRange(channel_b);}
	 * @return this for chaining
	 * 
	 * @see #scaleChannelToUnitRange(int)
	 */
	public ColorImg scaleRGBToUnitRange(){
		double min=Math.min(getMinValue(channel_r), Math.min(getMinValue(channel_g), getMinValue(channel_b)));
		double max=Math.max(getMaxValue(channel_r), Math.max(getMaxValue(channel_g), getMaxValue(channel_b)));
		if(min != max){
			forEach(px->px.convertRange(min,max, 0,1));
		} else {
			fill(channel_r, 0);
			fill(channel_g, 0);
			fill(channel_b, 0);
		}
		return this;
	}

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
	public double interpolate(final int channel, final double xNormalized, final double yNormalized){
		double xF = xNormalized * (getWidth()-1);
		double yF = yNormalized * (getHeight()-1);
		int x = (int)xF;
		int y = (int)yF;
		double c00 = getValue(channel, x, 							y);
		double c01 = getValue(channel, x, 						   (y+1 < getHeight() ? y+1:y));
		double c10 = getValue(channel, (x+1 < getWidth() ? x+1:x), 	y);
		double c11 = getValue(channel, (x+1 < getWidth() ? x+1:x), (y+1 < getHeight() ? y+1:y));
		return interpolateBilinear(c00, c01, c10, c11, xF-x, yF-y);
	}

	/**
	 * See {@link #interpolate(int, double, double)} for details.
	 * This is a shorthand for {@code interpolate(channel_r, xNormalized, yNormalized)}.
	 * @param xNormalized
	 * @param yNormalized
	 * @return bilinearly interpolated red value
	 * @throws ArrayIndexOutOfBoundsException when a resulting index is out of
	 * the data array's bounds, which can only happen for x and y values less
	 * than 0 or greater than 1.
	 */
	public double interpolateR(final double xNormalized, final double yNormalized){
		return interpolate(channel_r, xNormalized, yNormalized);
	}

	/**
	 * See {@link #interpolate(int, double, double)} for details.
	 * This is a shorthand for {@code interpolate(channel_g, xNormalized, yNormalized)}.
	 * @param xNormalized
	 * @param yNormalized
	 * @return bilinearly interpolated green value
	 * @throws ArrayIndexOutOfBoundsException when a resulting index is out of
	 * the data array's bounds, which can only happen for x and y values less
	 * than 0 or greater than 1.
	 */
	public double interpolateG(final double xNormalized, final double yNormalized){
		return interpolate(channel_g, xNormalized, yNormalized);
	}

	/**
	 * See {@link #interpolate(int, double, double)} for details.
	 * This is a shorthand for {@code interpolate(channel_b, xNormalized, yNormalized)}.
	 * @param xNormalized
	 * @param yNormalized
	 * @return bilinearly interpolated blue value
	 * @throws ArrayIndexOutOfBoundsException when a resulting index is out of
	 * the data array's bounds, which can only happen for x and y values less
	 * than 0 or greater than 1.
	 */
	public double interpolateB(final double xNormalized, final double yNormalized){
		return interpolate(channel_b, xNormalized, yNormalized);
	}

	/**
	 * See {@link #interpolate(int, double, double)} for details.
	 * This is a shorthand for {@code interpolate(channel_a, xNormalized, yNormalized)}.
	 * @param xNormalized
	 * @param yNormalized
	 * @return bilinearly interpolated alpha value
	 * @throws ArrayIndexOutOfBoundsException when a resulting index is out of
	 * the data array's bounds, which can only happen for x and y values less
	 * than 0 or greater than 1,
	 * or if the image has no alpha channel (check using {@link #hasAlpha()}).
	 */
	public double interpolateA(final double xNormalized, final double yNormalized){
		return interpolate(channel_a, xNormalized, yNormalized);
	}

	/* bilinear interpolation between values c00 c01 c10 c11 at position mx my (in [0,1]) */
	private static double interpolateBilinear(final double c00, final double c01, final double c10, final double c11, final double mx, final double my){
		return (c00*(1.0-mx)+c10*(mx))*(1.0-my) + (c01*(1.0-mx)+c11*(mx))*(my);
	}

	@Override
	public ColorPixel getPixel(){
		return new ColorPixel(this, 0);
	}

	@Override
	public ColorPixel getPixel(int x, int y){
		return new ColorPixel(this, x,y);
	}

	/**
	 * Copies specified area of this image to the specified destination image
	 * at specified destination coordinates. If destination image is null a new
	 * ColorImage with the areas size will be created and the destination coordinates
	 * will be ignored so that the image will contain all the values of the area.
	 * <p>
	 * The specified area has to be within the bounds of this image or
	 * otherwise an IllegalArgumentException will be thrown. Only the
	 * intersecting part of the area and the destination image is copied which
	 * allows for an out of bounds destination area origin.
	 * <p>
	 * If this image has no alpha channel but the destination image has one, the destination's
	 * alpha is left unchanged.
	 *
	 * @param x area origin in this image (x-coordinate)
	 * @param y area origin in this image (y-coordinate)
	 * @param w width of area
	 * @param h height of area
	 * @param dest destination image
	 * @param destX area origin in destination image (x-coordinate)
	 * @param destY area origin in destination image (y-coordinate)
	 * @return the destination image, or newly created image if destination was null.
	 * @throws IllegalArgumentException if the specified area is not within
	 * the bounds of this image or if the size of the area is not positive.
	 */
	public ColorImg copyArea(int x, int y, int w, int h, ColorImg dest, int destX, int destY){
		ImagingKitUtils.requireAreaInImageBounds(x, y, w, h, this);
		if(dest == null){
			return copyArea(x, y, w, h, new ColorImg(w,h,this.hasAlpha()), 0, 0);
		}
		if(x==0 && destX==0 && w==dest.getWidth() && w==this.getWidth()){
			if(destY < 0){
				/* negative destination y
				 * need to shrink area by overlap and translate area origin */
				y -= destY;
				h += destY;
				destY = 0;
			}
			// limit area height to not exceed targets bounds
			h = Math.min(h, dest.getHeight()-destY);
			if(h > 0){
				int srcPos = y*w, destPos = destY*w, len=w*h;
				System.arraycopy(this.getDataR(), srcPos, dest.getDataR(), destPos, len);
				System.arraycopy(this.getDataG(), srcPos, dest.getDataG(), destPos, len);
				System.arraycopy(this.getDataB(), srcPos, dest.getDataB(), destPos, len);
				if(this.hasAlpha() && dest.hasAlpha()) 
					System.arraycopy(this.getDataA(), srcPos, dest.getDataA(), destPos, len);
			}
		} else {
			if(destX < 0){
				/* negative destination x
				 * need to shrink area by overlap and translate area origin */
				x -= destX;
				w += destX;
				destX = 0;
			}
			if(destY < 0){
				/* negative destination y
				 * need to shrink area by overlap and translate area origin */
				y -= destY;
				h += destY;
				destY = 0;
			}
			// limit area to not exceed targets bounds
			w = Math.min(w, dest.getWidth()-destX);
			h = Math.min(h, dest.getHeight()-destY);
			if(w > 0 && h > 0){
				for(int i = 0; i < h; i++){
					int srcPos = (y+i)*getWidth()+x;
					int destPos = (destY+i)*dest.getWidth()+destX;
					int len = w;
					System.arraycopy(
							this.getDataR(), srcPos,
							dest.getDataR(), destPos,
							len);
					System.arraycopy(
							this.getDataG(), srcPos,
							dest.getDataG(), destPos,
							len);
					System.arraycopy(
							this.getDataB(), srcPos,
							dest.getDataB(), destPos,
							len);
					if(this.hasAlpha() && dest.hasAlpha()) { 
						System.arraycopy(
							this.getDataA(), srcPos,
							dest.getDataA(), destPos,
							len);
					}
				}
			}
		}
		return dest;
	}

	/**
	 * Sets value at the specified position for the specified channel.
	 * No bounds checks will be performed, positions outside of this
	 * images dimension can either result in a value for a different position
	 * or an ArrayIndexOutOfBoundsException.
	 * 
	 * @param channel the set value corresponds to
	 * @param x coordinate
	 * @param y coordinate
	 * @param value to be set at specified position. e.g. 0xff0000ff for blue color
	 * @throws ArrayIndexOutOfBoundsException if resulting index from x and y
	 * is not within the data arrays bounds 
	 * or if the specified channel is not in [0,3] 
	 * or is 3 but the image has no alpha (check using {@link #hasAlpha()}).
	 * @see #getValue(int channel, int x, int y)
	 */
	public void setValue(final int channel, final int x, final int y, final double value){
		this.data[channel][y*this.width + x] = value;
	}

	/**
	 * Sets the red value at the specified position.
	 * No bounds checks will be performed, positions outside of this
	 * images dimension can either result in a value for a different position
	 * or an ArrayIndexOutOfBoundsException.
	 * 
	 * @param x coordinate
	 * @param y coordinate
	 * @param value to be set
	 * @throws ArrayIndexOutOfBoundsException if resulting index from x and y
	 * is not within the data arrays bounds 
	 */
	public void setValueR(final int x, final int y, final double value){
		this.dataR[y*this.width + x] = value;
	}

	/**
	 * Sets the green value at the specified position.
	 * No bounds checks will be performed, positions outside of this
	 * images dimension can either result in a value for a different position
	 * or an ArrayIndexOutOfBoundsException.
	 * 
	 * @param x coordinate
	 * @param y coordinate
	 * @param value to be set
	 * @throws ArrayIndexOutOfBoundsException if resulting index from x and y
	 * is not within the data arrays bounds 
	 */
	public void setValueG(final int x, final int y, final double value){
		this.dataG[y*this.width + x] = value;
	}

	/**
	 * Sets the blue value at the specified position.
	 * No bounds checks will be performed, positions outside of this
	 * images dimension can either result in a value for a different position
	 * or an ArrayIndexOutOfBoundsException.
	 * 
	 * @param x coordinate
	 * @param y coordinate
	 * @param value to be set
	 * @throws ArrayIndexOutOfBoundsException if resulting index from x and y
	 * is not within the data arrays bounds 
	 */
	public void setValueB(final int x, final int y, final double value){
		this.dataB[y*this.width + x] = value;
	}

	/**
	 * Sets the alpha value at the specified position.
	 * No bounds checks will be performed, positions outside of this
	 * images dimension can either result in a value for a different position
	 * or an ArrayIndexOutOfBoundsException.
	 * 
	 * @param x coordinate
	 * @param y coordinate
	 * @param value to be set
	 * @throws ArrayIndexOutOfBoundsException if resulting index from x and y
	 * is not within the data arrays bounds
	 * @throws NullPointerException if this image has no alpha channel (check using {@link #hasAlpha()})
	 */
	public void setValueA(final int x, final int y, final double value){
		this.dataA[y*this.width + x] = value;
	}

	/**
	 * Fills the specified channel with the specified value.
	 * @param channel to be filled
	 * @param value for filling channel
	 * @return this for chaining
	 * @throws ArrayIndexOutOfBoundsException if the specified channel is not in [0,3] 
	 * or is 3 but the image has no alpha (check using {@link #hasAlpha()}).
	 */
	public ColorImg fill(final int channel, final double value){
		Arrays.fill(getData()[channel], value);
		return this;
	}

	@Override
	public ColorImg copy(){
		return new ColorImg(
				getWidth(),
				getHeight(),
				Arrays.copyOf(getDataR(), numValues()),
				Arrays.copyOf(getDataG(), numValues()),
				Arrays.copyOf(getDataB(), numValues()),
				hasAlpha() ? Arrays.copyOf(getDataA(), numValues()):null);
	}

	/**
	 * Creates a {@link BufferedImage} of type {@link BufferedImage#TYPE_INT_ARGB} 
	 * from this ColorImg using the specified {@link TransferFunction} to map the channel values 
	 * of this image to the 8bits per channel ARGB of the BufferedImage.
	 * @param transferFunc to transform a pixel value to the required 8bit per channel ARGB value
	 * @return a BufferedImage
	 */
	public BufferedImage toBufferedImage(TransferFunction transferFunc){
		return toImg(transferFunc).getRemoteBufferedImage();
	}

	/**
	 * {@inheritDoc}
	 * <p>
	 * It is assumed that all channel values are in range of [0.0, 1.0] and are otherwise
	 * clamped to that range.
	 */
	@Override
	public BufferedImage toBufferedImage(BufferedImage bimg){
		return toBufferedImage(bimg, TransferFunction.normalizedInput());
	}

	/**
	 * Copies this image's data to the specified {@link BufferedImage}.
	 * This method will preserve the {@link Raster} of the specified
	 * BufferedImage and will only modify the contents of it.
	 * <p>
	 * The specified {@link TransferFunction} is used to map this
	 * image's channel values to 8bit per channel ARGB values.
	 * 
	 * @param bimg the BufferedImage
	 * @param transferFunc to transform a pixel value to the required 8bit per channel ARGB value
	 * @return the specified BufferedImage
	 * @throws IllegalArgumentException if the provided BufferedImage
	 * has a different dimension as this image.
	 */
	public BufferedImage toBufferedImage(BufferedImage bimg, TransferFunction transferFunc){
		return toImg(transferFunc).toBufferedImage(bimg);
	}

	/**
	 * Copies this image's data to a new {@link Img}.
	 * The specified {@link TransferFunction} is used to map this
	 * image's channel values to 8bit per channel ARGB values.
	 * 
	 * @param transferFunc to transform a pixel value to the required 8bit per channel ARGB value
	 * @return an Img with this image's data copied to it
	 */
	public Img toImg(TransferFunction transferFunc){
		Img img = new Img(getDimension());
		if(hasAlpha()){
			img.forEach(px->px.setValue(transferFunc.toARGB(
					getDataA()[px.getIndex()],
					getDataR()[px.getIndex()],
					getDataG()[px.getIndex()],
					getDataB()[px.getIndex()])));
		} else {
			img.forEach(px->px.setValue(transferFunc.toRGB(
					getDataR()[px.getIndex()],
					getDataG()[px.getIndex()],
					getDataB()[px.getIndex()])));
		}
		return img;
	}

	/**
	 * Copies this image's data to a new {@link Img}.
	 * It is assumed that all channel values are in range of [0.0, 1.0] and are otherwise
	 * clamped to that range.
	 * 
	 * @return an Img with this image's data copied to it
	 */
	public Img toImg(){
		return toImg(TransferFunction.normalizedInput());
	}

	@Override
	public BufferedImage getRemoteBufferedImage(){
		SampleModel samplemodel = new BandedSampleModel(DataBuffer.TYPE_DOUBLE, getWidth(), getHeight(), hasAlpha() ? 4:3);
		DataBufferDouble databuffer = new DataBufferDouble(getData(), numValues());
		WritableRaster raster = Raster.createWritableRaster(samplemodel, databuffer, null);
		ColorModel colormodel = new ComponentColorModel(
				ColorSpace.getInstance(ColorSpace.CS_sRGB),
				hasAlpha(),
				false,
				hasAlpha() ? ComponentColorModel.TRANSLUCENT:ComponentColorModel.OPAQUE,
				DataBuffer.TYPE_DOUBLE
		);
		BufferedImage bimg = new BufferedImage(colormodel, raster, false, null);
		return bimg;
	}

	@Override
	public boolean supportsRemoteBufferedImage() {
		return true;
	}

	/**
	 * Sets the minimum number of elements in a split of a {@link Spliterator}
	 * of this image. Spliterators will only split if they contain more elements than
	 * specified by this value. Default is 1024.
	 * <p>
	 * It is advised that this number is
	 * chosen carefully and with respect to the image's size and application of the
	 * spliterator, as it can decrease performance of the parallelized methods<br>
	 * {@link #forEach(boolean parallel, Consumer action)},<br>
	 * {@link #forEach(boolean parallel, int x, int y, int w, int h, Consumer action)} or<br>
	 * {@link #stream(boolean parallel)} etc.<br>
	 * Small values cause a Spliterator to be split more often which will consume more
	 * memory compared to higher values. Special applications on small Imgs using
	 * sophisticated consumers or stream operations may justify the use of small split sizes.
	 * High values cause a Spliterator to be split less often which may cause the work items
	 * to be badly apportioned among the threads and lower throughput.
	 *  
	 * @param size the minimum number of elements a split covers
	 * @throws IllegalArgumentException if specified size is less than 1
	 */
	public void setSpliteratorMinimumSplitSize(int size) {
		if(size < 1){
			throw new IllegalArgumentException(
					String.format("Minimum split size has to be above zero, specified:%d", size));
		}
		this.spliteratorMinimumSplitSize = size;
	}

	@Override
	public int getSpliteratorMinimumSplitSize() {
		return this.spliteratorMinimumSplitSize;
	}

	/**
	 * A TransferFunction defines the method {@link #toARGB(double, double, double, double)}
	 * which maps 4 double precision values (channels) to an 8bit per channel int (ARGB) value.
	 * @author hageldave
	 * @since 2.0
	 */
	public static interface TransferFunction {

		/**
		 * Transforms the specified channel values to an 8bit per channel ARGB value packed integer value.
		 * @param a alpha value
		 * @param r red value
		 * @param g green value
		 * @param b blue value
		 * @return integer packed 8bit per channel ARGB value
		 */
		public int toARGB(double a, double r, double g, double b);

		/**
		 * Transforms the specified channel values to an 8bit per channel ARGB value packed integer value.
		 * This returns an opaque color (alpha=255).
		 * @param r red value
		 * @param g green value
		 * @param b blue value
		 * @return integer packed 8bit per channel ARGB value
		 */
		public default int toRGB(double r, double g, double b){
			return 0xff000000 | toARGB(0, r, g, b);
		}

		/**
		 * Returns a {@link TransferFunction} that uses the specified function for each channel.
		 * The specified function has to guarantee a mapping to the value range of [0,255] (8bit)
		 * in order for the TransferFunction to produce well formed ARGB values.
		 * @param fn function to be applied to each channel
		 * @return a TransferFunction using the specified function on each channel
		 */
		static TransferFunction fromFunction(DoubleToIntFunction fn){
			return (a,r,g,b) -> Pixel.argb_fast(
					fn.applyAsInt(a),
					fn.applyAsInt(r),
					fn.applyAsInt(g),
					fn.applyAsInt(b));
		}

		/**
		 * Returns a TransferFunction that assumes values in range of [0.0, 1.0] which are
		 * linearly mapped to [0, 255]. Values outside the assumed range are clamped to it.
		 * @return the TransferFunction that linearly maps [0.0, 1.0] to [0,255].
		 */
		static TransferFunction normalizedInput(){
			return (a,r,g,b) -> Pixel.argb_fromNormalized(a, r, g, b);
		}

	}

}
