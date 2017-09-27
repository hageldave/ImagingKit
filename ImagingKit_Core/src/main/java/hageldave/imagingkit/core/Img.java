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

package hageldave.imagingkit.core;

import java.awt.Dimension;
import java.awt.image.BufferedImage;
import java.awt.image.DataBuffer;
import java.awt.image.DataBufferInt;
import java.awt.image.DirectColorModel;
import java.awt.image.Raster;
import java.awt.image.WritableRaster;
import java.util.Arrays;
import java.util.Spliterator;
import java.util.function.Consumer;

import hageldave.imagingkit.core.util.ImagingKitUtils;

/**
 * Image class with data stored in an int array.
 * <p>
 * In contrast to {@link BufferedImage} the Img class only offers
 * pixel data to be stored as integer values simplifying data retrieval
 * and increasing performance due to less overhead and omitting color
 * model conversions. <br>
 * However the Img class can be easily used together with BufferedImages
 * offering convenience methods like {@link #Img(BufferedImage)},
 * {@link #toBufferedImage()} or {@link #createRemoteImg(BufferedImage)}.
 * <p>
 * Moreover the Img class targets lambda expressions introduced in Java 8
 * useful for per pixel operations by implementing the {@link Iterable}
 * interface and providing
 * <ul>
 * <li> {@link #iterator()} </li>
 * <li> {@link #spliterator()} </li>
 * <li> {@link #forEach(Consumer action)} </li>
 * <li> and {@link #forEach(boolean parallel, Consumer action)}. </li>
 * </ul>
 * <p>
 * Since version 1.1 it is also possible to iterate over a specified area of
 * the Img using
 * <ul>
 * <li> {@link #iterator(int x, int y, int w, int h)} </li>
 * <li> {@link #spliterator(int x, int y, int w, int h)} </li>
 * <li> {@link #forEach(int x, int y, int w, int h, Consumer action)} </li>
 * <li> and {@link #forEach(boolean parallel, int x, int y, int w, int h, Consumer action)}. </li>
 * </ul>
 * <p>
 * Here is an example of a parallelized per pixel operation:
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
public class Img implements ImgBase<Pixel> {

	/** boundary mode that will return 0 for out of bounds positions.
	 * @see #getValue(int, int, int)
	 * @since 1.0
	 */
	public static final int boundary_mode_zero = 0;

	/** boundary mode that will repeat the edge of of an Img for out of
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


	/** data array of this Img containing a value for each pixel in row major order
	 * @since 1.0 */
	private final int[] data;

	/** width and height of this image */
	private final int width,height;

	/** minimum number of elements this Img's {@link Spliterator}s can be split to.
	 * Default value is 1024.
	 * @since 1.3
	 */
	private int spliteratorMinimumSplitSize = 1024;


	/**
	 * Creates a new Img of specified dimensions.
	 * Values are initialized to 0.
	 * @param width of the Img
	 * @param height of the Img
	 * @since 1.0
	 */
	public Img(int width, int height){
		this.data = new int[width*height];
		this.width = width;
		this.height = height;
	}

	/**
	 * Creates a new Img of specified Dimension.
	 * Values are initilaized to 0.
	 * @param dimension extend of the Img (width and height)
	 * @since 1.0
	 */
	public Img(Dimension dimension){
		this(dimension.width,dimension.height);
	}

	/**
	 * Creates a new Img of same dimensions as provided BufferedImage.
	 * Values are copied from argument Image
	 * @param bimg the BufferedImage
	 * @see #createRemoteImg(BufferedImage)
	 * @since 1.0
	 */
	public Img(BufferedImage bimg){
		this(bimg.getWidth(), bimg.getHeight());
		bimg.getRGB(0, 0, this.getWidth(), this.getHeight(), this.getData(), 0, this.getWidth());
	}

	/**
	 * Creates a new Img of specified dimensions.
	 * Provided data array will be used as this images data.
	 * @param width of the Img
	 * @param height of the Img
	 * @param data values (pixels) that will be used as the content of this Img
	 * @throws IllegalArgumentException when the number of pixels of this Img
	 * resulting from width*height does not match the number of provided data values.
	 * @since 1.0
	 */
	public Img(int width, int height, int[] data){
		this(new Dimension(width, height), data);
	}

	/**
	 * Creates a new Img of specified dimensions.
	 * Provided data array will be used as this images data.
	 * @param dim extend of the image (width and height)
	 * @param data values (pixels) that will be used as the content of this Img
	 * @throws IllegalArgumentException when the number of pixels of this Img
	 * resulting from width*height does not match the number of provided data values.
	 * @since 1.0
	 */
	public Img(Dimension dim, int[] data){
		if(dim.width*dim.height != data.length){
			throw new IllegalArgumentException(String.format("Provided Dimension %s does not match number of provided pixels %d", dim, data.length));
		}
		this.width = dim.width;
		this.height = dim.height;
		this.data = data;
	}

	/**
	 * @return width of this Img
	 * @since 1.0
	 */
	public int getWidth(){
		return this.width;
	}

	/**
	 * @return height of this Img
	 * @since 1.0
	 */
	public int getHeight(){
		return this.height;
	}

	/**
	 * @return number of values (pixels) of this Img
	 * @since 1.0
	 */
	public int numValues(){
		return getWidth()*getHeight();
	}

	/**
	 * @return data array of this Img
	 * @since 1.0
	 */
	public int[] getData() {
		return data;
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
	 * @see #getPixel(int, int)
	 * @see #setValue(int, int, int)
	 * @since 1.0
	 */
	public int getValue(final int x, final int y){
		return this.data[y*this.width + x];
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
	public int getValue(int x, int y, final int boundaryMode){
		if(x < 0 || y < 0 || x >= this.width || y >= this.height){
			switch (boundaryMode) {
			case boundary_mode_zero:
				return 0;
			case boundary_mode_repeat_edge:
				x = (x < 0 ? 0: (x >= this.width ? this.width-1:x));
				y = (y < 0 ? 0: (y >= this.height ? this.height-1:y));
				return getValue(x, y);
			case boundary_mode_repeat_image:
				x = (this.width + (x % this.width)) % this.width;
				y = (this.height + (y % this.height)) % this.height;
				return getValue(x,y);
			case boundary_mode_mirror:
				if(x < 0){ // mirror x to right side of image
					x = -x - 1;
				}
				if(y < 0 ){ // mirror y to bottom side of image
					y = -y - 1;
				}
				x = (x/this.width) % 2 == 0 ? (x%this.width) : (this.width-1)-(x%this.width);
				y = (y/this.height) % 2 == 0 ? (y%this.height) : (this.height-1)-(y%this.height);
				return getValue(x, y);
			default:
				return boundaryMode; // boundary mode can be default color
			}
		} else {
			return getValue(x, y);
		}
	}

	/**
	 * Returns a bilinearly interpolated ARGB value of the image for the
	 * specified normalized position (x and y within [0,1]). Position {0,0}
	 * denotes the image's origin (top left corner), position {1,1} denotes the
	 * opposite corner (pixel at {width-1, height-1}).
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
	public int interpolateARGB(final double xNormalized, final double yNormalized){
		double xF = xNormalized * (getWidth()-1);
		double yF = yNormalized * (getHeight()-1);
		int x = (int)xF;
		int y = (int)yF;
		int c00 = getValue(x, 							y);
		int c01 = getValue(x, 						   (y+1 < getHeight() ? y+1:y));
		int c10 = getValue((x+1 < getWidth() ? x+1:x), 	y);
		int c11 = getValue((x+1 < getWidth() ? x+1:x), (y+1 < getHeight() ? y+1:y));
		return interpolateColors(c00, c01, c10, c11, xF-x, yF-y);
	}

	private static int interpolateColors(final int c00, final int c01, final int c10, final int c11, final double mx, final double my){
		return Pixel.argb_fast/*_bounded*/(
				blend( blend(Pixel.a(c00), Pixel.a(c10), mx), blend(Pixel.a(c01), Pixel.a(c11), mx), my),
				blend( blend(Pixel.r(c00), Pixel.r(c10), mx), blend(Pixel.r(c01), Pixel.r(c11), mx), my),
				blend( blend(Pixel.g(c00), Pixel.g(c10), mx), blend(Pixel.g(c01), Pixel.g(c11), mx), my),
				blend( blend(Pixel.b(c00), Pixel.b(c10), mx), blend(Pixel.b(c01), Pixel.b(c11), mx), my) );
	}

	private static int blend(final int channel1, final int channel2, final double m){
		return (int) ((channel2 * m) + (channel1 * (1.0-m)));
	}

	/**
	 * Creates a new Pixel object for this Img with position {0,0}.
	 * @return a Pixel object for this Img.
	 * @since 1.0
	 */
	public Pixel getPixel(){
		return new Pixel(this, 0);
	}

	/**
	 * Creates a new Pixel object for this Img at specified position.
	 * No bounds checks are performed for x and y.
	 * <p>
	 * <b>Tip:</b><br>
	 * Do not use this method repeatedly while iterating the image.
	 * Use {@link Pixel#setPosition(int, int)} instead to avoid excessive
	 * allocation of Pixel objects.
	 * <p>
	 * You can also use <code>for(Pixel px: img){...}</code> syntax or the
	 * {@link #forEach(Consumer)} method to iterate this image.
	 * @param x coordinate
	 * @param y coordinate
	 * @return a Pixel object for this Img at {x,y}.
	 * @see #getValue(int, int)
	 * @since 1.0
	 */
	public Pixel getPixel(int x, int y){
		return new Pixel(this, x,y);
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
	public Img copyArea(int x, int y, int w, int h, Img dest, int destX, int destY){
		ImagingKitUtils.requireAreaInImageBounds(x, y, w, h, this);
		if(dest == null){
			return copyArea(x, y, w, h, new Img(w,h), 0, 0);
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
				System.arraycopy(this.getData(), y*w, dest.getData(), destY*w, w*h);
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
					System.arraycopy(
							this.getData(), (y+i)*getWidth()+x,
							dest.getData(), (destY+i)*dest.getWidth()+destX,
							w);
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
	public void setValue(final int x, final int y, final int value){
		this.data[y*this.width + x] = value;
	}

	/**
	 * Fills the whole image with the specified value.
	 * @param value for filling image
	 * @return this for chaining
	 * @since 1.0
	 */
	public Img fill(final int value){
		Arrays.fill(getData(), value);
		return this;
	}

	/**
	 * @return a deep copy of this Img.
	 * @since 1.0
	 */
	@Override
	public Img copy(){
		return new Img(getDimension(), Arrays.copyOf(getData(), getData().length));
	}

	@Override
	public BufferedImage toBufferedImage(BufferedImage bimg){
		if(bimg.getWidth() != this.getWidth() || bimg.getHeight() != this.getHeight()){
			throw new IllegalArgumentException(String.format(
					"Specified BufferedImage has a different dimension as this image. BufferedImage dimension: [%dx%d], this: [%dx%d]", 
					bimg.getWidth(),bimg.getHeight(), this.getWidth(),this.getHeight()));
		}
		bimg.setRGB(0, 0, getWidth(), getHeight(), getData(), 0, getWidth());
		return bimg;
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
	@Override
	public BufferedImage getRemoteBufferedImage(){
		DirectColorModel cm = new DirectColorModel(32,
				0x00ff0000,       // Red
                0x0000ff00,       // Green
                0x000000ff,       // Blue
                0xff000000        // Alpha
                );
		DataBufferInt buffer = new DataBufferInt(getData(), numValues());
		WritableRaster raster = Raster.createPackedRaster(buffer, getWidth(), getHeight(), getWidth(), cm.getMasks(), null);
		BufferedImage bimg = new BufferedImage(cm, raster, false, null);
		return bimg;
	}

	@Override
	public boolean supportsRemoteBufferedImage() {
		return true;
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
	public static Img createRemoteImg(BufferedImage bimg){
		int type = bimg.getRaster().getDataBuffer().getDataType();
		if(type != DataBuffer.TYPE_INT){
			throw new IllegalArgumentException(
					String.format("cannot create Img as remote of provided BufferedImage!%n"
							+ "Need BufferedImage with DataBuffer of type TYPE_INT (%d). Provided type: %d",
							DataBuffer.TYPE_INT, type));
		}
		Img img = new Img(
				new Dimension(bimg.getWidth(),bimg.getHeight()),
				((DataBufferInt)bimg.getRaster().getDataBuffer()).getData()
			);
		return img;
	}


	/**
	 * Returns the minimum number of elements in a split of a {@link Spliterator}
	 * of this Img. Spliterators will only split if they contain more elements than
	 * specified by this value. Default is 1024.
	 * @return minimum number of elements of a Spliterator to allow for splitting.
	 * @since 1.3
	 */
	@Override
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
	 * @since 1.3
	 */
	public void setSpliteratorMinimumSplitSize(int size) {
		if(size < 1){
			throw new IllegalArgumentException(
					String.format("Minimum split size has to be above zero, specified:%d", size));
		}
		this.spliteratorMinimumSplitSize = size;
	}


}
