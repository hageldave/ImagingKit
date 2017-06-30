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
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.awt.image.DataBuffer;
import java.awt.image.DataBufferInt;
import java.awt.image.DirectColorModel;
import java.awt.image.Raster;
import java.awt.image.WritableRaster;
import java.util.Arrays;
import java.util.Iterator;
import java.util.Spliterator;
import java.util.concurrent.CountedCompleter;
import java.util.function.Consumer;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import hageldave.imagingkit.core.PixelConvertingSpliterator.PixelConverter;

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
public class Img implements Iterable<Pixel> {

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


	/** data array of this Img containing a value for each pixel in row major order
	 * @since 1.0 */
	private final int[] data;

	/** dimension of this Img
	 * @since 1.0 */
	private final Dimension dimension;

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
		this(new Dimension(width, height));
	}

	/**
	 * Creates a new Img of specified Dimension.
	 * Values are initilaized to 0.
	 * @param dimension extend of the Img (width and height)
	 * @since 1.0
	 */
	public Img(Dimension dimension){
		this.data = new int[dimension.width*dimension.height];
		this.dimension = new Dimension(dimension);
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
		this.dimension = new Dimension(dim);
		this.data = data;
	}

	/**
	 * @return dimension of this Img
	 * @since 1.0
	 */
	public Dimension getDimension() {
		return new Dimension(dimension);
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
		return this.data[y*dimension.width + x];
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
		if(x < 0 || y < 0 || x >= dimension.width || y >= dimension.height){
			switch (boundaryMode) {
			case boundary_mode_zero:
				return 0;
			case boundary_mode_repeat_edge:
				x = (x < 0 ? 0: (x >= dimension.width ? dimension.width-1:x));
				y = (y < 0 ? 0: (y >= dimension.height ? dimension.height-1:y));
				return getValue(x, y);
			case boundary_mode_repeat_image:
				x = (dimension.width + (x % dimension.width)) % dimension.width;
				y = (dimension.height + (y % dimension.height)) % dimension.height;
				return getValue(x,y);
			case boundary_mode_mirror:
				if(x < 0){ // mirror x to right side of image
					x = -x - 1;
				}
				if(y < 0 ){ // mirror y to bottom side of image
					y = -y - 1;
				}
				x = (x/dimension.width) % 2 == 0 ? (x%dimension.width) : (dimension.width-1)-(x%dimension.width);
				y = (y/dimension.height) % 2 == 0 ? (y%dimension.height) : (dimension.height-1)-(y%dimension.height);
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
		return (int) ((channel2 * m) + (channel1 * (1f-m)));
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
		this.data[y*dimension.width + x] = value;
	}

	/**
	 * Fills the whole image with the specified value.
	 * @param value for filling image
	 * @since 1.0
	 */
	public void fill(final int value){
		Arrays.fill(getData(), value);
	}

	/**
	 * @return a deep copy of this Img.
	 * @since 1.0
	 */
	public Img copy(){
		return new Img(getDimension(), Arrays.copyOf(getData(), getData().length));
	}

	/**
	 * @return a BufferedImage of type INT_ARGB with this Img's data copied to it.
	 * @see #toBufferedImage(BufferedImage)
	 * @see #getRemoteBufferedImage()
	 * @since 1.0
	 */
	public BufferedImage toBufferedImage(){
		BufferedImage bimg = BufferedImageFactory.getINT_ARGB(getDimension());
		return toBufferedImage(bimg);
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


	@Override
	public Iterator<Pixel> iterator() {
		Iterator<Pixel> pxIter = new Iterator<Pixel>() {
			Pixel px = new Pixel(Img.this, -1);

			@Override
			public Pixel next() {
				px.setIndex(px.getIndex()+1);
				return px;
			}

			@Override
			public boolean hasNext() {
				return px.getIndex()+1 < numValues();
			}

			@Override
			public void forEachRemaining(Consumer<? super Pixel> action) {
				px.setIndex(px.getIndex()+1);
				for(int i = px.getIndex(); i < Img.this.numValues(); px.setIndex(++i)){
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
	 * @return iterator for iterating over the pixels in the specified area.
	 * @throws IllegalArgumentException if provided area is not within this
	 * Img's bounds.
	 * @since 1.1
	 */
	public Iterator<Pixel> iterator(final int xStart, final int yStart, final int width, final int height) {
		if(		width <= 0 || height <= 0 ||
				xStart < 0 || yStart < 0 ||
				xStart+width > getWidth() || yStart+height > getHeight() )
		{
			throw new IllegalArgumentException(String.format(
							"provided area [%d,%d][%d,%d] is not within bounds of the image [%d,%d]",
							xStart,yStart,width,height, getWidth(), getHeight()));
		}
		return new Iterator<Pixel>() {
			Pixel px = new Pixel(Img.this, -1);
			int x = 0;
			int y = 0;
			@Override
			public Pixel next() {
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
			public void forEachRemaining(Consumer<? super Pixel> action) {
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
	public Spliterator<Pixel> spliterator() {
		return new ImgSpliterator(0, numValues()-1, spliteratorMinimumSplitSize);
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
	public Spliterator<Pixel> rowSpliterator() {
		return new RowSpliterator(0, getWidth(), 0, getHeight()-1, this);
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
	public Spliterator<Pixel> colSpliterator() {
		return new ColSpliterator(0, getWidth()-1, 0, getHeight(), this);
	}

	/**
	 * Creates a {@link Spliterator} over the pixels within the specified area.
	 * @param xStart left boundary of the area (inclusive)
	 * @param yStart upper boundary of the area (inclusive)
	 * @param width of the area
	 * @param height of the area
	 * @return spliterator for the specified area.
	 * @throws IllegalArgumentException if provided area is not within this
	 * Img's bounds.
	 * @since 1.1
	 */
	public Spliterator<Pixel> spliterator(final int xStart, final int yStart, final int width, final int height) {
		if(		width <= 0 || height <= 0 ||
				xStart < 0 || yStart < 0 ||
				xStart+width > getWidth() || yStart+height > getHeight() )
		{
			throw new IllegalArgumentException(String.format(
							"provided area [%d,%d][%d,%d] is not within bounds of the image [%d,%d]",
							xStart,yStart,width,height, getWidth(), getHeight()));
		}
		return new ImgAreaSpliterator(xStart,yStart,width,height, spliteratorMinimumSplitSize);
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
	 * @param action to be performed on each pixel
	 * @see #forEach(Consumer action)
	 * @since 1.0
	 */
	public void forEachParallel(final Consumer<? super Pixel> action) {
		ParallelForEachExecutor<Pixel> exec = new ParallelForEachExecutor<>(null, spliterator(), action);
		exec.invoke();
	}
	
	public <T> void forEachParallel(final PixelConverter<T> converter, final Consumer<? super T> action) {
		Spliterator<T> spliterator = new PixelConvertingSpliterator<T>(
				spliterator(), 
				converter);
 		ParallelForEachExecutor<T> exec = new ParallelForEachExecutor<>(null, spliterator, action);
		exec.invoke();
	}

	/**
	 * Applies the specified action to every pixel in the specified area of
	 * this image during a multithreaded execution.
	 * This Img's {@link #spliterator(int,int,int,int)} is used to parallelize the workload.
	 * As the threaded execution comes with a certain overhead it is only
	 * suitable for more sophisticated consumer actions and large Images (1MP+)
	 * @param xStart left boundary of the area (inclusive)
	 * @param yStart upper boundary of the area (inclusive)
	 * @param width of the area
	 * @param height of the area
	 * @param action to be performed on each pixel
	 * @throws IllegalArgumentException if provided area is not within this
	 * Img's bounds.
	 * @see #forEach(int x, int y, int w, int h, Consumer action)
	 * @since 1.1
	 */
	public void forEachParallel(final int xStart, final int yStart, final int width, final int height, final Consumer<? super Pixel> action) {
		ParallelForEachExecutor<Pixel> exec = new ParallelForEachExecutor<>(null, spliterator(xStart, yStart, width, height), action);
		exec.invoke();
	}
	
	public <T> void forEachParallel(final PixelConverter<T> converter, final int xStart, final int yStart, final int width, final int height, final Consumer<? super T> action) {
		Spliterator<T> spliterator = new PixelConvertingSpliterator<T>(
				spliterator(xStart, yStart, width, height), 
				converter);
		ParallelForEachExecutor<T> exec = new ParallelForEachExecutor<>(null, spliterator, action);
		exec.invoke();
	}

	/**
	 * @see #forEachParallel(Consumer action)
	 * @since 1.0
	 */
	@Override
	public void forEach(final Consumer<? super Pixel> action) {
		Pixel p = getPixel();
		for(int i = 0; i < numValues(); p.setIndex(++i)){
			action.accept(p);
		}
	}
	
	public <T> void forEach(final PixelConverter<T> converter, final Consumer<? super T> action) {
		Pixel px = getPixel();
		T element = converter.allocateElement();
		for(int i = 0; i < numValues(); px.setIndex(++i)){
			converter.convertPixelToElement(px, element);
			action.accept(element);
			converter.convertElementToPixel(element, px);
		}
	}

	/**
	 * Applies the specified action to every pixel in the specified area of this image.
	 * @param xStart left boundary of the area (inclusive)
	 * @param yStart upper boundary of the area (inclusive)
	 * @param width of the area
	 * @param height of the area
	 * @param action to be performed on each pixel
	 * @throws IllegalArgumentException if provided area is not within this
	 * Img's bounds.
	 * @see #forEachParallel(int x, int y, int w, int h, Consumer action)
	 * @since 1.1
	 */
	public void forEach(final int xStart, final int yStart, final int width, final int height, final Consumer<? super Pixel> action) {
		Pixel p = getPixel();
		int yEnd = yStart+height;
		int xEnd = xStart+width;
		for(int y = yStart; y < yEnd; y++){
			for(int x = xStart; x < xEnd; x++){
				p.setPosition(x, y);
				action.accept(p);
			}
		}
	}
	
	public <T> void forEach(final PixelConverter<T> converter, final int xStart, final int yStart, final int width, final int height, final Consumer<? super T> action) {
		Pixel p = getPixel();
		T element = converter.allocateElement();
		int yEnd = yStart+height;
		int xEnd = xStart+width;
		for(int y = yStart; y < yEnd; y++){
			for(int x = xStart; x < xEnd; x++){
				p.setPosition(x, y);
				converter.convertPixelToElement(p, element);
				action.accept(element);
				converter.convertElementToPixel(element, p);
			}
		}
	}

	/** default implementation of {@link Iterable#forEach(Consumer)} <br>
	 * only for performance test purposes as it is slower than the
	 * {@link Img#forEach(Consumer)} implementation
	 * @since 1.0
	 */
	void forEach_defaultimpl(final Consumer<? super Pixel> action) {
		Iterable.super.forEach(action);
	}

	/**
	 * Returns a Pixel {@link Stream} of this Img.
	 * This Img's {@link #spliterator()} is used to create the Stream.
	 * @return Pixel Stream of this Img.
	 * @see #parallelStream()
	 * @see #stream(int x, int y, int w, int h)
	 * @since 1.2
	 */
	public Stream<Pixel> stream() {
		return stream(false);
	}
	
	public Stream<Pixel> stream(boolean parallel) {
		return Img.stream(spliterator(), parallel);
	}
	
	public <T> Stream<T> stream(PixelConverter<T> converter, boolean parallel) {
		Spliterator<T> spliterator = new PixelConvertingSpliterator<T>(spliterator(), converter);
		return StreamSupport.stream(spliterator, parallel);
	}

	/**
	 * Returns a {@code Stream<Pixel>} for the specified {@code Spliterator<Pixel>}.
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
	 *     int forwardDiff = Math.abs( Pixel.getLuminance(next) - px.getLuminance() );
	 *     px.setRGB(forwardDiff, forwardDiff, forwardDiff);
	 * });
	 * }
	 * </pre>
	 * @param spliterator Spliterator of Img to be streamed
	 * @param parallel whether parallel or sequential stream is returned
	 * @return a new sequential or parallel pixel stream.
	 *
	 * @see #stream()
	 * @see #parallelStream()
	 * @since 1.3
	 */
	public static Stream<Pixel> stream(Spliterator<Pixel> spliterator, boolean parallel){
		return StreamSupport.stream(spliterator, parallel);
	}

	/**
	 * Returns a Pixel {@link Stream} for the specified area of this Img.<br>
	 * This Img's {@link #spliterator(int,int,int,int)} is used to create
	 * the Stream.
	 * @param xStart left boundary of the area (inclusive)
	 * @param yStart upper boundary of the area (inclusive)
	 * @param width of the area
	 * @param height of the area
	 * @return Pixel Stream for specified area.
	 * @throws IllegalArgumentException if provided area is not within this
	 * Img's bounds.
	 * @see #parallelStream(int x, int y, int w, int h)
	 * @see #stream()
	 * @since 1.2
	 */
	public Stream<Pixel> stream(final int xStart, final int yStart, final int width, final int height){
		return stream(false, xStart, yStart, width, height);
	}
	
	public Stream<Pixel> stream(boolean parallel, final int xStart, final int yStart, final int width, final int height){
		return StreamSupport.stream(spliterator(xStart, yStart, width, height), parallel);
	}
	
	public <T> Stream<T> stream(final PixelConverter<T> converter, boolean parallel, final int xStart, final int yStart, final int width, final int height){
		Spliterator<T> spliterator = new PixelConvertingSpliterator<>(
				spliterator(xStart, yStart, width, height), 
				converter);
		return StreamSupport.stream(spliterator, parallel);
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


	/**
	 * Spliterator class for Img bound to a specific area
	 * @author hageldave
	 * @since 1.1
	 */
	private final class ImgAreaSpliterator implements Spliterator<Pixel> {

		final Pixel px;
		/* start x coord and end x coord of a row */
		final int startX, endXexcl;
		/* current coords of this spliterator */
		int x,y;
		/* final coords of this spliterator */
		int finalXexcl, finalYincl;

		final int minimumSplitSize;

		/**
		 * Constructs a new ImgAreaSpliterator for the specified area
		 * @param xStart left boundary of the area (inclusive)
		 * @param yStart upper boundary of the area (inclusive)
		 * @param width of the area
		 * @param height of the area
		 * @param minSplitSize the minimum number of elements in a split
		 * @since 1.1
		 */
		private ImgAreaSpliterator(int xStart, int yStart, int width, int height, int minSplitSize){
			this(xStart, xStart+width, xStart, yStart, xStart+width, yStart+height-1, minSplitSize);
		}

		private ImgAreaSpliterator(int xStart, int endXexcl, int x, int y, int finalXexcl, int finalYincl, int minSplitSize){
			this.startX = xStart;
			this.endXexcl = endXexcl;
			this.x = x;
			this.y = y;
			this.finalXexcl = finalXexcl;
			this.finalYincl = finalYincl;
			this.px = Img.this.getPixel(x, y);
			this.minimumSplitSize = minSplitSize;
		}


		@Override
		public boolean tryAdvance(final Consumer<? super Pixel> action) {
			if(y > finalYincl || (y == finalYincl && x >= finalXexcl)){
				return false;
			} else {
				action.accept(px);
				if(x+1 >= endXexcl){
					x = startX;
					y++;
				} else {
					x++;
				}
				px.setPosition(x, y);
				return true;
			}
		}

		@Override
		public void forEachRemaining(final Consumer<? super Pixel> action) {
			if(this.y == finalYincl){
				for(int x = this.x; x < finalXexcl; x++){
					px.setPosition(x, finalYincl);
					action.accept(px);
				}
			} else {
				// end current row
				for(int x = this.x; x < endXexcl; x++){
					px.setPosition(x, this.y);
					action.accept(px);
				}
				// do next rows right before final row
				for(int y = this.y+1; y < this.finalYincl; y++){
					for(int x = startX; x < endXexcl; x++ ){
						px.setPosition(x, y);
						action.accept(px);
					}
				}
				// do final row
				for(int x = startX; x < finalXexcl; x++){
					px.setPosition(x, finalYincl);
					action.accept(px);
				}
			}
		}

		@Override
		public Spliterator<Pixel> trySplit() {
			int width = (this.endXexcl-this.startX);
			int idx = this.x - this.startX;
			int finalIdx_excl = (this.finalYincl-this.y)*width + (this.finalXexcl-startX);
			int midIdx_excl = idx + (finalIdx_excl-idx)/2;
			if(midIdx_excl > idx+minimumSplitSize){
//				int midIdx_excl = idx + (finalIdx_excl-idx)/2;

				int newFinalX_excl = startX + (midIdx_excl%width);
				int newFinalY_incl = this.y + midIdx_excl/width;
				ImgAreaSpliterator split = new ImgAreaSpliterator(
						startX,         // start of a row
						endXexcl,       // end of a row
						newFinalX_excl, // x coord of new spliterator
						newFinalY_incl, // y coord of new spliterator
						finalXexcl,     // final x coord of new spliterator
						finalYincl,    // final y coord of new spliterator
						minimumSplitSize);

				// shorten this spliterator because new one takes care of the rear part
				this.finalXexcl = newFinalX_excl;
				this.finalYincl = newFinalY_incl;

				return split;
			} else {
				return null;
			}
		}

		@Override
		public long estimateSize() {
			int idx = this.x - this.startX;
			int finalIdx_excl = (this.finalYincl-this.y)*(this.endXexcl-this.startX) + (this.finalXexcl-startX);
			return finalIdx_excl-idx;
		}

		@Override
		public int characteristics() {
			return NONNULL | SIZED | CONCURRENT | SUBSIZED | IMMUTABLE;
		}

	}

	/**
	 * Spliterator class for Img
	 * @author hageldave
	 * @since 1.0
	 */
	private final class ImgSpliterator implements Spliterator<Pixel> {

		final Pixel px;
		int endIndex;
		final int minimumSplitSize;

		/**
		 * Constructs a new ImgSpliterator for the specified index range
		 * @param startIndex first index of the range (inclusive)
		 * @param endIndex last index of the range (inclusive)
		 * @param minSplitSize minimum split size for this spliterator (minimum number of elements in a split)
		 * @since 1.0
		 */
		private ImgSpliterator(int startIndex, int endIndex, int minSplitSize) {
			px = new Pixel(Img.this, startIndex);
			this.endIndex = endIndex;
			this.minimumSplitSize = minSplitSize;
		}

		private void setEndIndex(int endIndex) {
			this.endIndex = endIndex;
		}

		@Override
		public boolean tryAdvance(final Consumer<? super Pixel> action) {
			if(px.getIndex() <= endIndex){
				int index = px.getIndex();
				action.accept(px);
				px.setIndex(index+1);
				return true;
			} else {
				return false;
			}
		}

		@Override
		public void forEachRemaining(final Consumer<? super Pixel> action) {
			int idx = px.getIndex();
			for(;idx <= endIndex; px.setIndex(++idx)){
				action.accept(px);
			}
		}

		@Override
		public Spliterator<Pixel> trySplit() {
			int currentIdx = Math.min(px.getIndex(), endIndex);
			int midIdx = currentIdx + (endIndex-currentIdx)/2;
			if(midIdx > currentIdx+minimumSplitSize){
				ImgSpliterator split = new ImgSpliterator(midIdx, endIndex, minimumSplitSize);
				setEndIndex(midIdx-1);
				return split;
			} else {
				return null;
			}
		}

		@Override
		public long estimateSize() {
			int currentIndex = px.getIndex();
			int lastIndexPlusOne = endIndex+1;
			return lastIndexPlusOne-currentIndex;
		}

		@Override
		public int characteristics() {
			return NONNULL | SIZED | CONCURRENT | SUBSIZED | IMMUTABLE;
		}

	}

	/**
	 * Special Spliterator which guarantees that each split will cover at least
	 * an entire row of the image.
	 * @author hageldave
	 * @since 1.3
	 */
	private static final class RowSpliterator implements Spliterator<Pixel> {

		int startX;
		int endXinclusive;
		int x;
		int y;
		int endYinclusive;
		Pixel px;

		public RowSpliterator(int startX, int width, int startY, int endYincl, Img img) {
			this.startX = startX;
			this.x = startX;
			this.endXinclusive = startX+width-1;
			this.y = startY;
			this.endYinclusive = endYincl;
			this.px = img.getPixel(x, y);
		}


		@Override
		public boolean tryAdvance(Consumer<? super Pixel> action) {
			if(x <= endXinclusive){
				px.setPosition(x, y);
				x++;
			} else if(y < endYinclusive) {
				y++;
				x=startX;
				px.setPosition(x, y);
				x++;
			} else {
				return false;
			}
			action.accept(px);
			return true;
		}

		@Override
		public void forEachRemaining(Consumer<? super Pixel> action) {
			int x_ = x;
			for(int y_ = y; y_ <= endYinclusive; y_++){
				for(;x_ <= endXinclusive; x_++){
					px.setPosition(x_, y_);
					action.accept(px);
				}
				x_=startX;
			}
		}

		@Override
		public Spliterator<Pixel> trySplit() {
			if(this.y < endYinclusive){
				int newY = y + 1 + (endYinclusive-y)/2;
				RowSpliterator split = new RowSpliterator(startX, endXinclusive-startX+1, newY, endYinclusive, px.getImg());
				this.endYinclusive = newY-1;
				return split;
			} else return null;
		}

		@Override
		public long estimateSize() {
			return (endYinclusive-y)*(endXinclusive+1-startX)+endXinclusive+1-x;
		}

		@Override
		public int characteristics() {
			return NONNULL | SIZED | CONCURRENT | SUBSIZED | IMMUTABLE;
		}

	}


	/**
	 * Special Spliterator which guarantees that each split will cover at least
	 * an entire column of the image.
	 * @author hageldave
	 * @since 1.3
	 */
	private static final class ColSpliterator implements Spliterator<Pixel> {

		int startY;
		int endXinclusive;
		int x;
		int y;
		int endYinclusive;
		Pixel px;

		public ColSpliterator(int startX, int endXincl, int startY, int height, Img img) {
			this.startY = startY;
			this.y = startY;
			this.endYinclusive = startY+height-1;
			this.x = startX;
			this.endXinclusive = endXincl;
			this.px = img.getPixel(x, y);
		}


		@Override
		public boolean tryAdvance(Consumer<? super Pixel> action) {
			if(y <= endYinclusive){
				px.setPosition(x, y);
				y++;
			} else if(x < endXinclusive) {
				x++;
				y=startY;
				px.setPosition(x, y);
				y++;
			} else {
				return false;
			}
			action.accept(px);
			return true;
		}

		@Override
		public void forEachRemaining(Consumer<? super Pixel> action) {
			int y_ = y;
			for(int x_ = x; x_ <= endXinclusive; x_++){
				for(;y_ <= endYinclusive; y_++){
					px.setPosition(x_, y_);
					action.accept(px);
				}
				y_=startY;
			}
		}

		@Override
		public Spliterator<Pixel> trySplit() {
			if(this.x < endXinclusive){
				int newX = x + 1 + (endXinclusive-x)/2;
				ColSpliterator split = new ColSpliterator(newX, endXinclusive, startY, endYinclusive-startY+1, px.getImg());
				this.endXinclusive = newX-1;
				return split;
			} else return null;
		}

		@Override
		public long estimateSize() {
			return (endXinclusive-x)*(endYinclusive+1-startY)+endYinclusive+1-y;
		}

		@Override
		public int characteristics() {
			return NONNULL | SIZED | CONCURRENT | SUBSIZED | IMMUTABLE;
		}

	}

	/**
	 * CountedCompleter class for multithreaded execution of a Consumer on a
	 * Pixel Spliterator. Used to realise multithreaded forEach loop.
	 * @author hageldave
	 * @see Img#forEachParallel(Consumer)
	 * @since 1.0
	 */
	final static class ParallelForEachExecutor<T> extends CountedCompleter<Void> {
		private static final long serialVersionUID = 1L;

		final Spliterator<T> spliterator;
		final Consumer<? super T> action;

		ParallelForEachExecutor(
				ParallelForEachExecutor<T> parent,
				Spliterator<T> spliterator,
				Consumer<? super T> action)
		{
			super(parent);
			this.spliterator = spliterator;
			this.action = action;
		}

		@Override
		public void compute() {
			Spliterator<T> sub;
			while ((sub = spliterator.trySplit()) != null) {
				addToPendingCount(1);
				new ParallelForEachExecutor<T>(this, sub, action).fork();
			}
			spliterator.forEachRemaining(action);
			propagateCompletion();
		}
	}


}
