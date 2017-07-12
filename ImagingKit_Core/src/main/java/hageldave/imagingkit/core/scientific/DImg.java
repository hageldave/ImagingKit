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
import java.awt.Graphics2D;
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
import java.util.Iterator;
import java.util.Objects;
import java.util.Spliterator;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import hageldave.imagingkit.core.Img;
import hageldave.imagingkit.core.Pixel;
import hageldave.imagingkit.core.util.ParallelForEachExecutor;

/**
 * Image class with data stored in an int array.
 * <p>
 * In contrast to {@link BufferedImage} the Img class only offers
 * DPixel data to be stored as integer values simplifying data retrieval
 * and increasing performance due to less overhead and omitting color
 * model conversions. <br>
 * However the Img class can be easily used together with BufferedImages
 * offering convenience methods like {@link #Img(BufferedImage)},
 * {@link #toBufferedImage()} or {@link #createRemoteImg(BufferedImage)}.
 * <p>
 * Moreover the Img class targets lambda expressions introduced in Java 8
 * useful for per DPixel operations by implementing the {@link Iterable}
 * interface and providing
 * <ul>
 * <li> {@link #iterator()} </li>
 * <li> {@link #spliterator()} </li>
 * <li> {@link #forEach(Consumer)} </li>
 * <li> and {@link #forEachParallel(Consumer)}. </li>
 * </ul>
 * <p>
 * Since version 1.1 it is also possible to iterate over a specified area of
 * the Img using
 * <ul>
 * <li> {@link #iterator(int, int, int, int)} </li>
 * <li> {@link #spliterator(int, int, int, int)} </li>
 * <li> {@link #forEach(int, int, int, int, Consumer)} </li>
 * <li> and {@link #forEachParallel(int, int, int, int, Consumer)}. </li>
 * </ul>
 * <p>
 * Here is an example of a parallelized per DPixel operation:
 * <pre>
 * {@code
 * Img img = new Img(1024, 1024);
 * img.forEachParallel(px -> {
 *     double x = (px.getX()-512)/512.0;
 *     double y = (px.getY()-512)/512.0;
 *     double len = Math.max(Math.abs(x),Math.abs(y));
 *     double angle = (Math.atan2(x,y)+Math.PI)*(180/Math.PI);
 *
 *     double r = 255*Math.max(0,1-Math.abs((angle-120)/120.0));
 *     double g = 255*Math.max(0, 1-Math.abs((angle-240)/120.0));
 *     double b = 255*Math.max(0, angle <= 120 ?
 *          1-Math.abs((angle)/120.0):1-Math.abs((angle-360)/120.0));
 *
 *     px.setRGB((int)(r*(1-len)), (int)(g*(1-len)), (int)(b*(1-len)));
 * });
 * ImageSaver.saveImage(img.getRemoteBufferedImage(), "polar_colors.png");
 * }</pre>
 *
 * @author hageldave
 * @since 1.0
 */
public class DImg implements Iterable<DPixel> {

	/** boundary mode that will return 0 for out of bounds positions.
	 * @see #getValue(int, int, int)
	 * @since 1.0
	 */
	public static final int boundary_mode_zero = 0;

	/** boundary mode that will repeat the the edge of of an Img for out of
	 * bounds positions.
	 * @see #getValue(int, int, int)
	 * @since 1.0
	 */
	public static final int boundary_mode_repeat_edge = 1;

	/** boundary mode that will repeat the Img for out of bounds positions.
	 * @see #getValue(int, int, int)
	 * @since 1.0
	 */
	public static final int boundary_mode_repeat_image = 2;

	/** boundary mode that will mirror the Img for out of bounds positions
	 * @see #getValue(int, int, int)
	 * @since 1.0
	 */
	public static final int boundary_mode_mirror = 3;

	public static final int channel_r = 0;
	public static final int channel_g = 1;
	public static final int channel_b = 2;
	public static final int channel_a = 3;


	/** data array of this Img containing a value for each DPixel in row major order
	 * @since 1.0 */
	private final double[] dataR;
	private final double[] dataG;
	private final double[] dataB;
	private final double[] dataA;

	private final double[][] data;
	private final boolean hasAlpha;

	/** dimension of this Img
	 * @since 1.0 */
	private final Dimension dimension;

	/** minimum number of elements this Img's {@link Spliterator}s can be split to.
	 * Default value is 1024.
	 * @since 1.3
	 */
	private int spliteratorMinimumSplitSize = 1024;


	/**
	 * Creates a new DImg of specified dimensions.
	 * Values are initialized to 0.
	 * @param width of the DImg
	 * @param height of the DImg
	 * @since 1.0
	 */
	public DImg(int width, int height, boolean alpha){
		this(new Dimension(width, height), alpha);
	}

	/**
	 * Creates a new DImg of specified Dimension.
	 * Values are initilaized to 0.
	 * @param dimension extend of the DImg (width and height)
	 * @since 1.0
	 */
	public DImg(Dimension dimension, boolean alpha){
		this.dataR = new double[dimension.width*dimension.height];
		this.dataG = new double[dimension.width*dimension.height];
		this.dataB = new double[dimension.width*dimension.height];
		this.hasAlpha = alpha;
		this.dataA = alpha ? new double[dimension.width*dimension.height]:null;
		this.data = alpha ? new double[][]{dataR,dataG,dataB,dataA}:new double[][]{dataR,dataG,dataB};
		this.dimension = new Dimension(dimension);
	}

	/**
	 * Creates a new DImg of same dimensions as provided BufferedImage.
	 * Values are copied from argument Image
	 * @param bimg the BufferedImage
	 * @see #createRemoteImg(BufferedImage)
	 * @since 1.0
	 */
	public DImg(Img img, boolean alpha){
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
	 * Creates a new DImg of specified dimensions.
	 * Provided data array will be used as this images data.
	 * @param width of the DImg
	 * @param height of the DImg
	 * @param data values (DPixels) that will be used as the content of this Img
	 * @throws IllegalArgumentException when the number of DPixels of this Img
	 * resulting from width*height does not match the number of provided data values.
	 * @throws NullPointerException if any of dataR, dataG or dataB is null.
	 * @since 1.5
	 */
	public DImg(int width, int height, double[] dataR, double[] dataG, double[] dataB, double[] dataA){
		this(new Dimension(width, height), dataR,dataG,dataB,dataA);
	}

	/**
	 * Creates a new DImg of specified dimensions.
	 * Provided data array will be used as this images data.
	 * @param dim extend of the image (width and height)
	 * @param data values (DPixels) that will be used as the content of this Img
	 * @throws IllegalArgumentException when the number of DPixels of this Img
	 * resulting from width*height does not match the number of provided data values.
	 * @throws NullPointerException if any of dataR, dataG or dataB is null.
	 * @since 1.0
	 */
	public DImg(Dimension dim, double[] dataR, double[] dataG, double[] dataB, double[] dataA){
		Objects.requireNonNull(dataR);
		Objects.requireNonNull(dataG);
		Objects.requireNonNull(dataB);
		hasAlpha = dataA != null;
		if(dataR.length != dataG.length || dataG.length != dataB.length || (hasAlpha && dataB.length != dataA.length)){
			throw new IllegalArgumentException(String.format("Provided data arrays are not of same size. R[%d] G[%d] B[%d]%s", dataR.length, dataG.length, dataB.length, hasAlpha ? " A["+dataA.length+"]":""));
		}
		if(dim.width*dim.height != dataR.length){
			throw new IllegalArgumentException(String.format("Provided Dimension %s does not match number of provided Pixels %d", dim, dataR.length));
		}
		this.dimension = new Dimension(dim);
		this.dataR=dataR;
		this.dataG=dataG;
		this.dataB=dataB;
		this.dataA=dataA;
		this.data = hasAlpha ? new double[][]{dataR,dataG,dataB,dataA}:new double[][]{dataR,dataG,dataB};
	}


	public DImg (BufferedImage bimg){
		this(bimg.getWidth(),bimg.getHeight(),bimg.getColorModel().hasAlpha());
		this.paint(g->g.drawImage(bimg, 0, 0, getWidth(), getHeight(), 0, 0, getWidth(), getHeight(), null));
	}

	/**
	 * @return dimension of this Img
	 * @since 1.0
	 */
	public Dimension getDimension() {
		return new Dimension(dimension);
	}

	public boolean hasAlpha(){
		return hasAlpha;
	}

	/**
	 * @return width of this Img
	 * @since 1.0
	 */
	public int getWidth(){
		return dimension.width;
	}

	/**
	 * @return height of this Img
	 * @since 1.0
	 */
	public int getHeight(){
		return dimension.height;
	}

	/**
	 * @return number of values (DPixels) of this Img
	 * @since 1.0
	 */
	public int numValues(){
		return getWidth()*getHeight();
	}

	/**
	 * @return data array of this Img
	 * @since 1.0
	 */
	public double[][] getData() {
		return data;
	}

	public double[] getDataR() {
		return dataR;
	}

	public double[] getDataG() {
		return dataG;
	}

	public double[] getDataB() {
		return dataB;
	}

	public double[] getDataA() {
		return dataA;
	}

	/**
	 * Returns the value of this Img at the specified position.
	 * No bounds checks will be performed, positions outside of this
	 * image's dimension can either result in a value for a different position
	 * or an ArrayIndexOutOfBoundsException.
	 * @param x coordinate
	 * @param y coordinate
	 * @return value for specified position
	 * @throws ArrayIndexOutOfBoundsException if resulting index from x and y
	 * is not within the data arrays bounds.
	 * @see #getValue(int, int, int)
	 * @see #getDPixel(int, int)
	 * @see #setValue(int, int, int)
	 * @since 1.0
	 */
	public double getValue(final int channel, final int x, final int y){
		return this.data[channel][y*dimension.width + x];
	}

	public double getValueR(final int x, final int y){
		return this.dataR[y*dimension.width + x];
	}

	public double getValueG(final int x, final int y){
		return this.dataG[y*dimension.width + x];
	}

	public double getValueB(final int x, final int y){
		return this.dataB[y*dimension.width + x];
	}

	public double getValueA(final int x, final int y){
		return this.dataA[y*dimension.width + x];
	}

	/**
	 * Returns the value of this Img at the specified position.
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
	 * @param x coordinate
	 * @param y coordinate
	 * @param boundaryMode one of the boundary modes e.g. boundary_mode_mirror
	 * @return value at specified position or a value depending on the
	 * boundary mode for out of bounds positions.
	 * @since 1.0
	 */
	public double getValue(final int channel, int x, int y, final int boundaryMode){
		if(x < 0 || y < 0 || x >= dimension.width || y >= dimension.height){
			switch (boundaryMode) {
			case boundary_mode_zero:
				return 0;
			case boundary_mode_repeat_edge:
				x = (x < 0 ? 0: (x >= dimension.width ? dimension.width-1:x));
				y = (y < 0 ? 0: (y >= dimension.height ? dimension.height-1:y));
				return getValue(channel, x, y);
			case boundary_mode_repeat_image:
				x = (dimension.width + (x % dimension.width)) % dimension.width;
				y = (dimension.height + (y % dimension.height)) % dimension.height;
				return getValue(channel, x,y);
			case boundary_mode_mirror:
				if(x < 0){ // mirror x to right side of image
					x = -x - 1;
				}
				if(y < 0 ){ // mirror y to bottom side of image
					y = -y - 1;
				}
				x = (x/dimension.width) % 2 == 0 ? (x%dimension.width) : (dimension.width-1)-(x%dimension.width);
				y = (y/dimension.height) % 2 == 0 ? (y%dimension.height) : (dimension.height-1)-(y%dimension.height);
				return getValue(channel, x, y);
			default:
				return boundaryMode; // boundary mode can be default color
			}
		} else {
			return getValue(channel, x, y);
		}
	}

	public double getValueR(int x, int y, final int boundaryMode){
		return getValue(channel_r, x, y, boundaryMode);
	}

	public double getValueG(int x, int y, final int boundaryMode){
		return getValue(channel_g, x, y, boundaryMode);
	}

	public double getValueB(int x, int y, final int boundaryMode){
		return getValue(channel_b, x, y, boundaryMode);
	}

	public double getValueA(int x, int y, final int boundaryMode){
		return getValue(channel_a, x, y, boundaryMode);
	}

	/**
	 * Returns a bilinearly interpolated ARGB value of the image for the
	 * specified normalized position (x and y within [0,1]). Position {0,0}
	 * denotes the image's origin (top left corner), position {1,1} denotes the
	 * opposite corner (DPixel at {width-1, height-1}).
	 * <p>
	 * An ArrayIndexOutOfBoundsException may be thrown for x and y greater than 1
	 * or less than 0.
	 * @param xNormalized coordinate within [0,1]
	 * @param yNormalized coordinate within [0,1]
	 * @throws ArrayIndexOutOfBoundsException when a resulting index is out of
	 * the data array's bounds, which can only happen for x and y values less
	 * than 0 or greater than 1.
	 * @return bilinearly interpolated ARGB value.
	 * @since 1.0
	 */
	public double interpolate(final int channel, final float xNormalized, final float yNormalized){
		float xF = xNormalized * (getWidth()-1);
		float yF = yNormalized * (getHeight()-1);
		int x = (int)xF;
		int y = (int)yF;
		double c00 = getValue(channel, x, 							y);
		double c01 = getValue(channel, x, 						   (y+1 < getHeight() ? y+1:y));
		double c10 = getValue(channel, (x+1 < getWidth() ? x+1:x), 	y);
		double c11 = getValue(channel, (x+1 < getWidth() ? x+1:x), (y+1 < getHeight() ? y+1:y));
		return interpolateBilinear(c00, c01, c10, c11, xF-x, yF-y);
	}

	public double interpolateR(final float xNormalized, final float yNormalized){
		float xF = xNormalized * (getWidth()-1);
		float yF = yNormalized * (getHeight()-1);
		int x = (int)xF;
		int y = (int)yF;
		double c00 = getValueR(x, 							y);
		double c01 = getValueR(x, 						   (y+1 < getHeight() ? y+1:y));
		double c10 = getValueR((x+1 < getWidth() ? x+1:x), 	y);
		double c11 = getValueR((x+1 < getWidth() ? x+1:x), (y+1 < getHeight() ? y+1:y));
		return interpolateBilinear(c00, c01, c10, c11, xF-x, yF-y);
	}

	public double interpolateG(final float xNormalized, final float yNormalized){
		float xF = xNormalized * (getWidth()-1);
		float yF = yNormalized * (getHeight()-1);
		int x = (int)xF;
		int y = (int)yF;
		double c00 = getValueG(x, 							y);
		double c01 = getValueG(x, 						   (y+1 < getHeight() ? y+1:y));
		double c10 = getValueG((x+1 < getWidth() ? x+1:x), 	y);
		double c11 = getValueG((x+1 < getWidth() ? x+1:x), (y+1 < getHeight() ? y+1:y));
		return interpolateBilinear(c00, c01, c10, c11, xF-x, yF-y);
	}

	public double interpolateB(final float xNormalized, final float yNormalized){
		float xF = xNormalized * (getWidth()-1);
		float yF = yNormalized * (getHeight()-1);
		int x = (int)xF;
		int y = (int)yF;
		double c00 = getValueB(x, 							y);
		double c01 = getValueB(x, 						   (y+1 < getHeight() ? y+1:y));
		double c10 = getValueB((x+1 < getWidth() ? x+1:x), 	y);
		double c11 = getValueB((x+1 < getWidth() ? x+1:x), (y+1 < getHeight() ? y+1:y));
		return interpolateBilinear(c00, c01, c10, c11, xF-x, yF-y);
	}

	public double interpolateA(final float xNormalized, final float yNormalized){
		float xF = xNormalized * (getWidth()-1);
		float yF = yNormalized * (getHeight()-1);
		int x = (int)xF;
		int y = (int)yF;
		double c00 = getValueA(x, 							y);
		double c01 = getValueA(x, 						   (y+1 < getHeight() ? y+1:y));
		double c10 = getValueA((x+1 < getWidth() ? x+1:x), 	y);
		double c11 = getValueA((x+1 < getWidth() ? x+1:x), (y+1 < getHeight() ? y+1:y));
		return interpolateBilinear(c00, c01, c10, c11, xF-x, yF-y);
	}


	private static double interpolateBilinear(final double c00, final double c01, final double c10, final double c11, final float mx, final float my){
		return (c00*mx+c10*(1.0-mx))* my + (c01*mx+c11*(1.0-mx))*(1.0-my);
	}

	/**
	 * Creates a new DPixel object for this DImg with position {0,0}.
	 * @return a DPixel object for this DImg.
	 * @since 1.0
	 */
	public DPixel getDPixel(){
		return new DPixel(this, 0);
	}

	/**
	 * Creates a new DPixel object for this DImg at specified position.
	 * No bounds checks are performed for x and y.
	 * <p>
	 * <b>Tip:</b><br>
	 * Do not use this method repeatedly while iterating the image.
	 * Use {@link DPixel#setPosition(int, int)} instead to avoid excessive
	 * allocation of DPixel objects.
	 * <p>
	 * You can also use <code>for(DPixel px: img){...}</code> syntax or the
	 * {@link #forEach(Consumer)} method to iterate this image.
	 * @param x coordinate
	 * @param y coordinate
	 * @return a DPixel object for this DImg at {x,y}.
	 * @see #getValue(int, int)
	 * @since 1.0
	 */
	public DPixel getDPixel(int x, int y){
		return new DPixel(this, x,y);
	}

	/**
	 * Copies specified area of this Img to the specified destination Img
	 * at specified destination coordinates. If destination Img is null a new
	 * Img with the areas size will be created and the destination coordinates
	 * will be ignored so that the Img will contain all the values of the area.
	 * <p>
	 * The specified area has to be within the bounds of this image or
	 * otherwise an IllegalArgumentException will be thrown. Only the
	 * intersecting part of the area and the destination image is copied which
	 * allows for an out of bounds destination area origin.
	 *
	 * @param x area origin in this image (x-coordinate)
	 * @param y area origin in this image (y-coordinate)
	 * @param w width of area
	 * @param h height of area
	 * @param dest destination Img
	 * @param destX area origin in destination Img (x-coordinate)
	 * @param destY area origin in destination Img (y-coordinate)
	 * @return the destination Img
	 * @throws IllegalArgumentException if the specified area is not within
	 * the bounds of this Img or if the size of the area is not positive.
	 * @since 1.0
	 */
	public DImg copyArea(int x, int y, int w, int h, DImg dest, int destX, int destY){
		if(w <= 0 || h <= 0){
			throw new IllegalArgumentException(String.format(
					"specified area size is not positive! specified size w,h = [%dx%d]",
					w,h));
		}
		if(x < 0 || y < 0 || x+w > getWidth() || y+h > getHeight()){
			throw new IllegalArgumentException(String.format(
					"specified area is not within image bounds! specified x,y = [%d,%d] w,h = [%dx%d], image dimensions are [%dx%d]",
					x,y,w,h,getWidth(),getHeight()));
		}
		if(dest == null){
			return copyArea(x, y, w, h, new DImg(w,h,this.hasAlpha()), 0, 0);
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
				if(this.hasAlpha() && dest.hasAlpha()) System.arraycopy(this.getDataA(), srcPos, dest.getDataA(), destPos, len);
				else
				if(dest.hasAlpha())                    Arrays.fill(dest.getDataA(), destPos, destPos+len, 1.0);
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
					if(this.hasAlpha() && dest.hasAlpha()) { System.arraycopy(
							this.getDataA(), srcPos,
							dest.getDataA(), destPos,
							len);
					} else if(dest.hasAlpha()) {
						Arrays.fill(dest.getDataA(), destPos, destPos+len, 1.0);
					}
				}
			}
		}
		return dest;
	}

	/**
	 * Sets value at the specified position.
	 * No bounds checks will be performed, positions outside of this
	 * images dimension can either result in a value for a different position
	 * or an ArrayIndexOutOfBoundsException.
	 * @param x coordinate
	 * @param y coordinate
	 * @param value to be set at specified position. e.g. 0xff0000ff for blue color
	 * @throws ArrayIndexOutOfBoundsException if resulting index from x and y
	 * is not within the data arrays bounds.
	 * @see #getValue(int, int)
	 * @since 1.0
	 */
	public void setValue(final int channel, final int x, final int y, final double value){
		this.data[channel][y*dimension.width + x] = value;
	}

	public void setValueR(final int x, final int y, final double value){
		this.dataR[y*dimension.width + x] = value;
	}

	public void setValueG(final int x, final int y, final double value){
		this.dataG[y*dimension.width + x] = value;
	}

	public void setValueB(final int x, final int y, final double value){
		this.dataB[y*dimension.width + x] = value;
	}

	public void setValueA(final int x, final int y, final double value){
		this.dataA[y*dimension.width + x] = value;
	}

	/**
	 * Fills the whole image with the specified value.
	 * @param value for filling image
	 * @since 1.0
	 */
	public void fill(final int channel, final double value){
		Arrays.fill(getData()[channel], value);
	}

	/**
	 * @return a deep copy of this Img.
	 * @since 1.0
	 */
	public DImg copy(){
		return new DImg(
				getDimension(),
				Arrays.copyOf(getDataR(), numValues()),
				Arrays.copyOf(getDataG(), numValues()),
				Arrays.copyOf(getDataB(), numValues()),
				hasAlpha() ? Arrays.copyOf(getDataA(), numValues()):null);
	}

	/**
	 * @return a BufferedImage of type INT_ARGB with this Img's data copied to it.
	 * @see #toBufferedImage(BufferedImage)
	 * @see #getRemoteBufferedImage()
	 * @since 1.0
	 */
	public BufferedImage toBufferedImage(){
		return toBufferedImage(TransferFunction.normalizedInput());
	}

	public BufferedImage toBufferedImage(TransferFunction transferFunc){
		return toImg(transferFunc).getRemoteBufferedImage();
	}

	/**
	 * Copies this Img's data to the specified {@link BufferedImage}.
	 * @param bimg the BufferedImage
	 * @return specified BufferedImage
	 * @throws ArrayIndexOutOfBoundsException if the provided BufferedImage
	 * has less values than this Img.
	 * @see #toBufferedImage()
	 * @see #getRemoteBufferedImage()
	 * @since 1.0
	 */
	public BufferedImage toBufferedImage(BufferedImage bimg){
		return toBufferedImage(bimg, TransferFunction.normalizedInput());
	}


	public BufferedImage toBufferedImage(BufferedImage bimg, TransferFunction transferFunc){
		return toImg(transferFunc).toBufferedImage(bimg);
	}

	public Img toImg(TransferFunction transferFunc){
		Img img = new Img(getDimension());
		if(hasAlpha()){
			img.forEach(px->px.setValue(transferFunc.toARGB(
					getDataA()[px.getIndex()],
					getDataR()[px.getIndex()],
					getDataG()[px.getIndex()],
					getDataB()[px.getIndex()])));
		} else {
			img.forEach(px->px.setValue(0xff000000 | transferFunc.toRGB(
					getDataR()[px.getIndex()],
					getDataG()[px.getIndex()],
					getDataB()[px.getIndex()])));
		}
		return null;
	}

	public Img toImg(){
		return toImg(TransferFunction.normalizedInput());
	}

	/**
	 * Creates a BufferedImage that shares the data of this Img. Changes in
	 * this Img are reflected in the created BufferedImage and vice versa.
	 * The created BufferedImage uses an ARGB DirectColorModel with an
	 * underlying DataBufferInt (similar to {@link BufferedImage#TYPE_INT_ARGB})
	 * @return BufferedImage sharing this Img's data.
	 * @see #createRemoteImg(BufferedImage)
	 * @see #toBufferedImage()
	 * @since 1.0
	 */
	public BufferedImage getRemoteBufferedImage(){
		SampleModel samplemodel = new BandedSampleModel(DataBuffer.TYPE_DOUBLE, getWidth(), getHeight(), hasAlpha() ? 4:3);
		DataBufferDouble databuffer = new DataBufferDouble(getData(), numValues());
		WritableRaster raster = Raster.createWritableRaster(samplemodel, databuffer, null);
		ColorModel colormodel = new ComponentColorModel(
				ColorSpace.getInstance(ColorSpace.CS_sRGB),
				hasAlpha(),
				false,
				ComponentColorModel.TRANSLUCENT,
				DataBuffer.TYPE_DOUBLE
		);
		BufferedImage bimg = new BufferedImage(colormodel, raster, false, null);
		return bimg;
	}

	/**
	 * Creates an Img sharing the specified BufferedImage's data. Changes in
	 * the BufferdImage are reflected in the created Img and vice versa.
	 * <p>
	 * Only BufferedImages with DataBuffer of {@link DataBuffer#TYPE_INT} can
	 * be used since the Img class uses an int[] to store its data. An
	 * IllegalArgumentException will be thrown if a BufferedImage with a
	 * different DataBufferType is provided.
	 * @param bimg BufferedImage with TYPE_INT DataBuffer.
	 * @return Img sharing the BufferedImages data.
	 * @throws IllegalArgumentException if a BufferedImage with a DataBufferType
	 * other than {@link DataBuffer#TYPE_INT} is provided.
	 * @see #getRemoteBufferedImage()
	 * @see #Img(BufferedImage)
	 * @since 1.0
	 */
// TODO: figure out if this is even possible
//	public static DImg createRemoteImg(BufferedImage bimg){
//		int type = bimg.getRaster().getDataBuffer().getDataType();
//		if(type != DataBuffer.TYPE_INT){
//			throw new IllegalArgumentException(
//					String.format("cannot create Img as remote of provided BufferedImage!%n"
//							+ "Need BufferedImage with DataBuffer of type TYPE_INT (%d). Provided type: %d",
//							DataBuffer.TYPE_INT, type));
//		}
//		DImg img = new DImg(
//				new Dimension(bimg.getWidth(),bimg.getHeight()),
//				((DataBufferInt)bimg.getRaster().getDataBuffer()).getData()
//			);
//		return img;
//	}


	@Override
	public Iterator<DPixel> iterator() {
		Iterator<DPixel> pxIter = new Iterator<DPixel>() {
			DPixel px = new DPixel(DImg.this, -1);

			@Override
			public DPixel next() {
				px.setIndex(px.getIndex()+1);
				return px;
			}

			@Override
			public boolean hasNext() {
				return px.getIndex()+1 < numValues();
			}

			@Override
			public void forEachRemaining(Consumer<? super DPixel> action) {
				px.setIndex(px.getIndex()+1);
				for(int i = px.getIndex(); i < DImg.this.numValues(); px.setIndex(++i)){
					action.accept(px);
				}
			}
		};
		return pxIter;
	}

	/**
	 * Returns an iterator for the specified area of the image.
	 * @param xStart left boundary of the area (inclusive)
	 * @param yStart upper boundary of the area (inclusive)
	 * @param width of the area
	 * @param height of the area
	 * @return iterator for iterating over the DPixels in the specified area.
	 * @throws IllegalArgumentException if provided area is not within this
	 * Img's bounds.
	 * @since 1.1
	 */
	public Iterator<DPixel> iterator(final int xStart, final int yStart, final int width, final int height) {
		if(		width <= 0 || height <= 0 ||
				xStart < 0 || yStart < 0 ||
				xStart+width > getWidth() || yStart+height > getHeight() )
		{
			throw new IllegalArgumentException(String.format(
							"provided area [%d,%d][%d,%d] is not within bounds of the image [%d,%d]",
							xStart,yStart,width,height, getWidth(), getHeight()));
		}
		return new Iterator<DPixel>() {
			DPixel px = new DPixel(DImg.this, -1);
			int x = 0;
			int y = 0;
			@Override
			public DPixel next() {
				px.setPosition(x+xStart, y+yStart);
				x++;
				if(x >= width){
					x=0;
					y++;
				}
				return px;
			}

			@Override
			public boolean hasNext() {
				return y < height;
			}

			@Override
			public void forEachRemaining(Consumer<? super DPixel> action) {
				int xEnd = xStart+width;
				int yEnd = yStart+height;
				int x = this.x+xStart;
				for(int y=this.y+yStart; y < yEnd; y++){
					for(; x < xEnd; x++){
						px.setPosition(x, y);
						action.accept(px);
					}
					x = xStart;
				}
			}
		};
	}

	@Override
	public Spliterator<DPixel> spliterator() {
		return new Img.ImgSpliterator<DPixel>(0, numValues()-1, spliteratorMinimumSplitSize, (index)->new DPixel(this, index));
	}

	/**
	 * Creates a {@link Spliterator} that guarantees that each split will
	 * at least cover an entire row of the Img. It also guarantes that each
	 * row will be iterated starting at the least index of that row
	 * (e.g.starts at index 0 then continues with index 1, then 2, until
	 * the end of the row, then continuing with the next row).
	 * This Spliterator iterates in row-major order.
	 * @return Spliterator that splits at beginning of rows.
	 * @see #colSpliterator()
	 * @see #spliterator()
	 * @see #stream(Spliterator, boolean)
	 * @since 1.3
	 */
	public Spliterator<DPixel> rowSpliterator() {
		return new Img.RowSpliterator<DPixel>(0, getWidth(), 0, getHeight()-1, (x,y)->new DPixel(this, x, y));
	}

	/**
	 * Creates a {@link Spliterator} that guarantees that each split will
	 * at least cover an entire column of the Img. It also guarantes that each
	 * column will be iterated starting at the least index of that column
	 * (e.g.starts at index 0 then continues with index 1, then 2, until
	 * the end of the column, then continuing with the next column).
	 * This Spliterator iterates in column-major order.
	 * @return Spliterator that splits at beginning of columns.
	 * @see #rowSpliterator()
	 * @see #spliterator()
	 * @see #stream(Spliterator, boolean)
	 * @since 1.3
	 */
	public Spliterator<DPixel> colSpliterator() {
		return new Img.ColSpliterator<DPixel>(0, getWidth()-1, 0, getHeight(), (x,y)->new DPixel(this, x, y));
	}

	/**
	 * Creates a {@link Spliterator} over the DPixels within the specified area.
	 * @param xStart left boundary of the area (inclusive)
	 * @param yStart upper boundary of the area (inclusive)
	 * @param width of the area
	 * @param height of the area
	 * @return spliterator for the specified area.
	 * @throws IllegalArgumentException if provided area is not within this
	 * Img's bounds.
	 * @since 1.1
	 */
	public Spliterator<DPixel> spliterator(final int xStart, final int yStart, final int width, final int height) {
		if(		width <= 0 || height <= 0 ||
				xStart < 0 || yStart < 0 ||
				xStart+width > getWidth() || yStart+height > getHeight() )
		{
			throw new IllegalArgumentException(String.format(
							"provided area [%d,%d][%d,%d] is not within bounds of the image [%d,%d]",
							xStart,yStart,width,height, getWidth(), getHeight()));
		}
		return new Img.ImgAreaSpliterator<DPixel>(xStart,yStart,width,height, spliteratorMinimumSplitSize, (x,y)->new DPixel(this, x, y));
	}

	/**
	 * Returns the minimum number of elements in a split of a {@link Spliterator}
	 * of this Img. Spliterators will only split if they contain more elements than
	 * specified by this value. Default is 1024.
	 * @return minimum number of elements of a Spliterator to allow for splitting.
	 * @since 1.3
	 */
	public int getSpliteratorMinimumSplitSize() {
		return spliteratorMinimumSplitSize;
	}

	/**
	 * Sets the minimum number of elements in a split of a {@link Spliterator}
	 * of this Img. Spliterators will only split if they contain more elements than
	 * specified by this value. Default is 1024.
	 * <p>
	 * It is advised that this number is
	 * chosen carefully and with respect to the Img's size and application of the
	 * spliterator, as it can decrease performance of the parallelized methods
	 * {@link #forEachParallel(Consumer)}, {@link #forEachParallel(int, int, int, int, Consumer)}
	 * or {@link #parallelStream()} and {@link #parallelStream(int, int, int, int)}.
	 * Low values cause a Spliterator to be split more often which will consume more
	 * memory compared to higher values. Special applications on small Imgs using
	 * sophisticated consumers or stream operations may justify the use of low split sizes.
	 * @param size number of elements
	 * @since 1.3
	 */
	public void setSpliteratorMinimumSplitSize(int size) {
		if(size < 1){
			throw new IllegalArgumentException(
					String.format("Minimum split size has to be above zero, specified:%d", size));
		}
		this.spliteratorMinimumSplitSize = size;
	}

	/**
	 * {@link #forEach(Consumer)} method but with multithreaded execution.
	 * This Img's {@link #spliterator()} is used to parallelize the workload.
	 * As the threaded execution comes with a certain overhead it is only
	 * suitable for more sophisticated consumer actions and large Images (1MP+)
	 * @param action to be performed on each DPixel
	 * @see #forEach(Consumer action)
	 * @since 1.0
	 */
	public void forEachParallel(final Consumer<? super DPixel> action) {
		ParallelForEachExecutor<DPixel> exec = new ParallelForEachExecutor<>(null, spliterator(), action);
		exec.invoke();
	}

	/**
	 * Applies the specified action to every DPixel in the specified area of
	 * this image during a multithreaded execution.
	 * This Img's {@link #spliterator(int,int,int,int)} is used to parallelize the workload.
	 * As the threaded execution comes with a certain overhead it is only
	 * suitable for more sophisticated consumer actions and large Images (1MP+)
	 * @param xStart left boundary of the area (inclusive)
	 * @param yStart upper boundary of the area (inclusive)
	 * @param width of the area
	 * @param height of the area
	 * @param action to be performed on each DPixel
	 * @throws IllegalArgumentException if provided area is not within this
	 * Img's bounds.
	 * @see #forEach(int x, int y, int w, int h, Consumer action)
	 * @since 1.1
	 */
	public void forEachParallel(final int xStart, final int yStart, final int width, final int height, final Consumer<? super DPixel> action) {
		ParallelForEachExecutor<DPixel> exec = new ParallelForEachExecutor<>(null, spliterator(xStart, yStart, width, height), action);
		exec.invoke();
	}

	/**
	 * @see #forEachParallel(Consumer action)
	 * @since 1.0
	 */
	@Override
	public void forEach(final Consumer<? super DPixel> action) {
		DPixel p = getDPixel();
		for(int i = 0; i < numValues(); p.setIndex(++i)){
			action.accept(p);
		}
	}

	/**
	 * Applies the specified action to every DPixel in the specified area of this image.
	 * @param xStart left boundary of the area (inclusive)
	 * @param yStart upper boundary of the area (inclusive)
	 * @param width of the area
	 * @param height of the area
	 * @param action to be performed on each DPixel
	 * @throws IllegalArgumentException if provided area is not within this
	 * Img's bounds.
	 * @see #forEachParallel(int x, int y, int w, int h, Consumer action)
	 * @since 1.1
	 */
	public void forEach(final int xStart, final int yStart, final int width, final int height, final Consumer<? super DPixel> action) {
		DPixel p = getDPixel();
		int yEnd = yStart+height;
		int xEnd = xStart+width;
		for(int y = yStart; y < yEnd; y++){
			for(int x = xStart; x < xEnd; x++){
				p.setPosition(x, y);
				action.accept(p);
			}
		}
	}

	/** default implementation of {@link Iterable#forEach(Consumer)} <br>
	 * only for performance test purposes as it is slower than the
	 * {@link DImg#forEach(Consumer)} implementation
	 * @since 1.0
	 */
	void forEach_defaultimpl(final Consumer<? super DPixel> action) {
		Iterable.super.forEach(action);
	}

	/**
	 * Returns a DPixel {@link Stream} of this Img.
	 * This Img's {@link #spliterator()} is used to create the Stream.
	 * @return DPixel Stream of this Img.
	 * @see #parallelStream()
	 * @see #stream(int x, int y, int w, int h)
	 * @since 1.2
	 */
	public Stream<DPixel> stream() {
		return DImg.stream(spliterator(), false);
	}

	/**
	 * Returns a parallel DPixel {@link Stream} of this Img.
	 * This Img's {@link #spliterator()} is used to create the Stream.
	 * @return parallel DPixel Stream of this Img.
	 * @see #stream()
	 * @see #parallelStream(int x, int y, int w, int h)
	 * @since 1.2
	 */
	public Stream<DPixel> parallelStream() {
		return DImg.stream(spliterator(), true);
	}

	/**
	 * Returns a {@code Stream<DPixel>} for the specified {@code Spliterator<DPixel>}.
	 * This is just a wrapper method arround {@link StreamSupport#stream(Spliterator, boolean)}
	 * mainly used as syntactic sugar for the use with the non default Spliterators
	 * ({@link #rowSpliterator()} and {@link #colSpliterator()}. When the default spliterator
	 * of the Img is sufficient the non-static {@link #stream()} can be used.
	 * <p>
	 * For example (horizontal edge detection in parallel using forward difference):
	 * <pre>
	 * {@code
	 * Img myImg = ...;
	 * Img.stream(myImg.rowSpliterator(), true).forEach( px -> {
	 *     int next = px.getImg().getValue(px.getX()+1, px.getY(), Img.boundary_mode_repeat_edge);
	 *     int forwardDiff = Math.abs( DPixel.getLuminance(next) - px.getLuminance() );
	 *     px.setRGB(forwardDiff, forwardDiff, forwardDiff);
	 * });
	 * }
	 * </pre>
	 * @param spliterator Spliterator of Img to be streamed
	 * @param parallel whether parallel or sequential stream is returned
	 * @return a new sequential or parallel DPixel stream.
	 *
	 * @see #stream()
	 * @see #parallelStream()
	 * @since 1.3
	 */
	public static Stream<DPixel> stream(Spliterator<DPixel> spliterator, boolean parallel){
		return StreamSupport.stream(spliterator, parallel);
	}

	/**
	 * Returns a DPixel {@link Stream} for the specified area of this Img.<br>
	 * This Img's {@link #spliterator(int,int,int,int)} is used to create
	 * the Stream.
	 * @param xStart left boundary of the area (inclusive)
	 * @param yStart upper boundary of the area (inclusive)
	 * @param width of the area
	 * @param height of the area
	 * @return DPixel Stream for specified area.
	 * @throws IllegalArgumentException if provided area is not within this
	 * Img's bounds.
	 * @see #parallelStream(int x, int y, int w, int h)
	 * @see #stream()
	 * @since 1.2
	 */
	public Stream<DPixel> stream(final int xStart, final int yStart, final int width, final int height){
		return StreamSupport.stream(spliterator(xStart, yStart, width, height), false);
	}


	/**
	 * Returns a parallel DPixel {@link Stream} for the specified area of this Img.<br>
	 * This Img's {@link #spliterator(int,int,int,int)} is used to create
	 * the Stream.
	 * @param xStart left boundary of the area (inclusive)
	 * @param yStart upper boundary of the area (inclusive)
	 * @param width of the area
	 * @param height of the area
	 * @return parallel DPixel Stream for specified area.
	 * @throws IllegalArgumentException if provided area is not within this
	 * Img's bounds.
	 * @see #stream(int x, int y, int w, int h)
	 * @see #parallelStream()
	 * @since 1.2
	 */
	public Stream<DPixel> parallelStream(final int xStart, final int yStart, final int width, final int height){
		return StreamSupport.stream(spliterator(xStart, yStart, width, height), true);
	}

	/**
	 * Creates a {@link Graphics2D}, which can be used to draw into this Img.
	 * @return Graphics2D object to draw into this image.
	 * @see #paint(Consumer)
	 * @since 1.3
	 */
	public Graphics2D createGraphics(){
		return getRemoteBufferedImage().createGraphics();
	}

	/**
	 * Uses the specified paintInstructions to draw into this Img.
	 * This method will pass a {@link Graphics2D} object of this Img to the
	 * specified {@link Consumer}. The {@link Consumer#accept(Object)} method
	 * can then draw into this Image. When the accept method return, the
	 * Graphics2D object is disposed.
	 * <p>
	 * For example (using lambda expression for Consumers accept method):
	 * <pre>
	 * {@code
	 * Img img = new Img(100, 100);
	 * img.paint( g2d -> { g2d.drawLine(0, 0, 100, 100); } );
	 * }
	 * </pre>
	 * @param paintInstructions to be executed on a this Graphics2D object
	 * of this Img.
	 * @see #createGraphics()
	 * @since 1.3
	 */
	public void paint(Consumer<Graphics2D> paintInstructions){
		Graphics2D g2d = createGraphics();
		paintInstructions.accept(g2d);
		g2d.dispose();
	}


//	/**
//	 * Spliterator class for Img bound to a specific area
//	 * @author hageldave
//	 * @since 1.1
//	 */
//	private final class DImgAreaSpliterator implements Spliterator<DPixel> {
//
//		final DPixel px;
//		/* start x coord and end x coord of a row */
//		final int startX, endXexcl;
//		/* current coords of this spliterator */
//		int x,y;
//		/* final coords of this spliterator */
//		int finalXexcl, finalYincl;
//
//		final int minimumSplitSize;
//
//		/**
//		 * Constructs a new ImgAreaSpliterator for the specified area
//		 * @param xStart left boundary of the area (inclusive)
//		 * @param yStart upper boundary of the area (inclusive)
//		 * @param width of the area
//		 * @param height of the area
//		 * @param minSplitSize the minimum number of elements in a split
//		 * @since 1.1
//		 */
//		private DImgAreaSpliterator(int xStart, int yStart, int width, int height, int minSplitSize){
//			this(xStart, xStart+width, xStart, yStart, xStart+width, yStart+height-1, minSplitSize);
//		}
//
//		private DImgAreaSpliterator(int xStart, int endXexcl, int x, int y, int finalXexcl, int finalYincl, int minSplitSize){
//			this.startX = xStart;
//			this.endXexcl = endXexcl;
//			this.x = x;
//			this.y = y;
//			this.finalXexcl = finalXexcl;
//			this.finalYincl = finalYincl;
//			this.px = DImg.this.getDPixel(x, y);
//			this.minimumSplitSize = minSplitSize;
//		}
//
//
//		@Override
//		public boolean tryAdvance(final Consumer<? super DPixel> action) {
//			if(y > finalYincl || (y == finalYincl && x >= finalXexcl)){
//				return false;
//			} else {
//				action.accept(px);
//				if(x+1 >= endXexcl){
//					x = startX;
//					y++;
//				} else {
//					x++;
//				}
//				px.setPosition(x, y);
//				return true;
//			}
//		}
//
//		@Override
//		public void forEachRemaining(final Consumer<? super DPixel> action) {
//			if(this.y == finalYincl){
//				for(int x = this.x; x < finalXexcl; x++){
//					px.setPosition(x, finalYincl);
//					action.accept(px);
//				}
//			} else {
//				// end current row
//				for(int x = this.x; x < endXexcl; x++){
//					px.setPosition(x, this.y);
//					action.accept(px);
//				}
//				// do next rows right before final row
//				for(int y = this.y+1; y < this.finalYincl; y++){
//					for(int x = startX; x < endXexcl; x++ ){
//						px.setPosition(x, y);
//						action.accept(px);
//					}
//				}
//				// do final row
//				for(int x = startX; x < finalXexcl; x++){
//					px.setPosition(x, finalYincl);
//					action.accept(px);
//				}
//			}
//		}
//
//		@Override
//		public Spliterator<DPixel> trySplit() {
//			int width = (this.endXexcl-this.startX);
//			int idx = this.x - this.startX;
//			int finalIdx_excl = (this.finalYincl-this.y)*width + (this.finalXexcl-startX);
//			int midIdx_excl = idx + (finalIdx_excl-idx)/2;
//			if(midIdx_excl > idx+minimumSplitSize){
////				int midIdx_excl = idx + (finalIdx_excl-idx)/2;
//
//				int newFinalX_excl = startX + (midIdx_excl%width);
//				int newFinalY_incl = this.y + midIdx_excl/width;
//				DImgAreaSpliterator split = new DImgAreaSpliterator(
//						startX,         // start of a row
//						endXexcl,       // end of a row
//						newFinalX_excl, // x coord of new spliterator
//						newFinalY_incl, // y coord of new spliterator
//						finalXexcl,     // final x coord of new spliterator
//						finalYincl,    // final y coord of new spliterator
//						minimumSplitSize);
//
//				// shorten this spliterator because new one takes care of the rear part
//				this.finalXexcl = newFinalX_excl;
//				this.finalYincl = newFinalY_incl;
//
//				return split;
//			} else {
//				return null;
//			}
//		}
//
//		@Override
//		public long estimateSize() {
//			int idx = this.x - this.startX;
//			int finalIdx_excl = (this.finalYincl-this.y)*(this.endXexcl-this.startX) + (this.finalXexcl-startX);
//			return finalIdx_excl-idx;
//		}
//
//		@Override
//		public int characteristics() {
//			return NONNULL | SIZED | CONCURRENT | SUBSIZED | IMMUTABLE;
//		}
//
//	}




	public static interface TransferFunction {

		public int toARGB(double a, double r, double g, double b);

		public default int toRGB(double r, double g, double b){
			return 0xff000000 | toARGB(0, r, g, b);
		}

		static TransferFunction fromFunction(Function<Double, Integer> fn){
			return (a,r,g,b) -> Pixel.argb_fast(
					fn.apply(a),
					fn.apply(r),
					fn.apply(g),
					fn.apply(b));
		}

		static TransferFunction normalizedInput(){
			return (a,r,g,b) -> Pixel.argb_fromNormalized((float)a, (float)r, (float)g, (float)b);
		}

	}






}