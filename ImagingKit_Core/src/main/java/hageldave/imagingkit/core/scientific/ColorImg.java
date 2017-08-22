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
import java.util.function.Function;

import hageldave.imagingkit.core.Img;
import hageldave.imagingkit.core.ImgBase;
import hageldave.imagingkit.core.Pixel;

/**
 * Image class with data stored in an int array.
 * <p>
 * In contrast to {@link BufferedImage} the Img class only offers
 * ColorPixel data to be stored as integer values simplifying data retrieval
 * and increasing performance due to less overhead and omitting color
 * model conversions. <br>
 * However the Img class can be easily used together with BufferedImages
 * offering convenience methods like {@link #Img(BufferedImage)},
 * {@link #toBufferedImage()} or {@link #createRemoteImg(BufferedImage)}.
 * <p>
 * Moreover the Img class targets lambda expressions introduced in Java 8
 * useful for per ColorPixel operations by implementing the {@link Iterable}
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
 * Here is an example of a parallelized per ColorPixel operation:
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
public class ColorImg implements ImgBase<ColorPixel> {

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


	/** data array of this Img containing a value for each ColorPixel in row major order
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
	 * Creates a new ColorImg of specified dimensions.
	 * Values are initialized to 0.
	 * @param width of the ColorImg
	 * @param height of the ColorImg
	 * @since 1.0
	 */
	public ColorImg(int width, int height, boolean alpha){
		this(new Dimension(width, height), alpha);
	}

	/**
	 * Creates a new ColorImg of specified Dimension.
	 * Values are initilaized to 0.
	 * @param dimension extend of the ColorImg (width and height)
	 * @since 1.0
	 */
	public ColorImg(Dimension dimension, boolean alpha){
		this.dataR = new double[dimension.width*dimension.height];
		this.dataG = new double[dimension.width*dimension.height];
		this.dataB = new double[dimension.width*dimension.height];
		this.hasAlpha = alpha;
		this.dataA = alpha ? new double[dimension.width*dimension.height]:null;
		this.data = alpha ? new double[][]{dataR,dataG,dataB,dataA}:new double[][]{dataR,dataG,dataB};
		this.dimension = new Dimension(dimension);
	}

	/**
	 * Creates a new ColorImg of same dimensions as provided BufferedImage.
	 * Values are copied from argument Image
	 * @param bimg the BufferedImage
	 * @see #createRemoteImg(BufferedImage)
	 * @since 1.0
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
	 * Provided data array will be used as this images data.
	 * @param width of the ColorImg
	 * @param height of the ColorImg
	 * @param data values (ColorPixels) that will be used as the content of this Img
	 * @throws IllegalArgumentException when the number of ColorPixels of this Img
	 * resulting from width*height does not match the number of provided data values.
	 * @throws NullPointerException if any of dataR, dataG or dataB is null.
	 * @since 1.5
	 */
	public ColorImg(int width, int height, double[] dataR, double[] dataG, double[] dataB, double[] dataA){
		this(new Dimension(width, height), dataR,dataG,dataB,dataA);
	}

	/**
	 * Creates a new ColorImg of specified dimensions.
	 * Provided data array will be used as this images data.
	 * @param dim extend of the image (width and height)
	 * @param data values (ColorPixels) that will be used as the content of this Img
	 * @throws IllegalArgumentException when the number of ColorPixels of this Img
	 * resulting from width*height does not match the number of provided data values.
	 * @throws NullPointerException if any of dataR, dataG or dataB is null.
	 * @since 1.0
	 */
	public ColorImg(Dimension dim, double[] dataR, double[] dataG, double[] dataB, double[] dataA){
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


	public ColorImg (BufferedImage bimg){
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
	 * @return number of values (ColorPixels) of this Img
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
	 * @see #getColorPixel(int, int)
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
	 * opposite corner (ColorPixel at {width-1, height-1}).
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
	 * Creates a new ColorPixel object for this ColorImg with position {0,0}.
	 * @return a ColorPixel object for this ColorImg.
	 * @since 1.0
	 */
	public ColorPixel getPixel(){
		return new ColorPixel(this, 0);
	}

	/**
	 * Creates a new ColorPixel object for this ColorImg at specified position.
	 * No bounds checks are performed for x and y.
	 * <p>
	 * <b>Tip:</b><br>
	 * Do not use this method repeatedly while iterating the image.
	 * Use {@link ColorPixel#setPosition(int, int)} instead to avoid excessive
	 * allocation of ColorPixel objects.
	 * <p>
	 * You can also use <code>for(ColorPixel px: img){...}</code> syntax or the
	 * {@link #forEach(Consumer)} method to iterate this image.
	 * @param x coordinate
	 * @param y coordinate
	 * @return a ColorPixel object for this ColorImg at {x,y}.
	 * @see #getValue(int, int)
	 * @since 1.0
	 */
	public ColorPixel getPixel(int x, int y){
		return new ColorPixel(this, x,y);
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
	public ColorImg copyArea(int x, int y, int w, int h, ColorImg dest, int destX, int destY){
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
	public ColorImg copy(){
		return new ColorImg(
				getDimension(),
				Arrays.copyOf(getDataR(), numValues()),
				Arrays.copyOf(getDataG(), numValues()),
				Arrays.copyOf(getDataB(), numValues()),
				hasAlpha() ? Arrays.copyOf(getDataA(), numValues()):null);
	}

	public BufferedImage toBufferedImage(TransferFunction transferFunc){
		return toImg(transferFunc).getRemoteBufferedImage();
	}

	@Override
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

	@Override
	public int getSpliteratorMinimumSplitSize() {
		return this.spliteratorMinimumSplitSize;
	}

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
