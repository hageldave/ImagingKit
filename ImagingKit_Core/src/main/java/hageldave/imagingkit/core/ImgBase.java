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
import java.awt.image.ColorModel;
import java.awt.image.Raster;
import java.util.Iterator;
import java.util.Spliterator;
import java.util.function.Consumer;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import hageldave.imagingkit.core.PixelConvertingSpliterator.PixelConverter;
import hageldave.imagingkit.core.util.BufferedImageFactory;
import hageldave.imagingkit.core.util.ImagingKitUtils;
import hageldave.imagingkit.core.util.ParallelForEachExecutor;

/**
 * Base interface for imagingkit's Img classes.
 * <p>
 * This interface defines the most basic methods like getting the dimensions
 * of an image and converting an image to {@link BufferedImage}.
 * <p>
 * Appart from that it defines and implements all the {@link Iterable} functionality
 * which is based on {@link PixelBase}. The Iterable Functionality also comprises
 * {@link Spliterator}s as well as the {@link #forEach(Consumer)} and {@link #stream()}
 * functionality.
 * <p>
 * The {@link Graphics2D} related functionality like {@link #createGraphics()}
 * and {@link #paint(Consumer)} is by default based on {@link #getRemoteBufferedImage()}.
 * If it is possible to create a remote BufferedImage from the implemented
 * data structure, the method should be overridden to enable the mentioned funtionality.
 *
 * @param <P> the pixel type of the image
 *
 * @author hageldave
 * @since 2.0
 */
public interface ImgBase<P extends PixelBase> extends Iterable<P> {

	/**
	 * @return the dimension (width,height) of this image
	 * 
	 * @see #getWidth()
	 * @see #getHeight()
	 * @see #numValues()
	 */
	public default Dimension getDimension(){ return new Dimension(getWidth(),getHeight());}

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
	 * @return the number of pixels of this image
	 * 
	 * @see #getWidth()
	 * @see #getHeight()
	 * @see #getDimension()
	 */
	public default int numValues(){return getWidth()*getHeight();}

	/**
	 * Creates a new pixel object (instance of {@link PixelBase}) for this Img 
	 * with initial position (0,0) i.e. top left corner.
	 * <p>
	 * <b>Tip:</b><br>
	 * Do not use this method repeatedly while iterating the image.
	 * Use {@link PixelBase#setPosition(int, int)} instead to avoid excessive
	 * allocation of pixel objects.
	 * <br>
	 * You can also use <code>for(PixelBase px: img){...}</code> syntax or the
	 * {@link #forEach(Consumer)} method to iterate this image.
	 * 
	 * @return a pixel object for this image.
	 * 
	 * @see #getPixel(int, int)
	 */
	public P getPixel();

	/**
	 * Creates a new Pixel object for this Img at specified position.
	 * (0,0) is the top left corner, (width-1,height-1) is the bottom right corner.
	 * <p>
	 * <b>Tip:</b><br>
	 * Do not use this method repeatedly while iterating the image.
	 * Use {@link PixelBase#setPosition(int, int)} instead to avoid excessive
	 * allocation of pixel objects.
	 * <br>
	 * You can also use <code>for(PixelBase px: img){...}</code> syntax or the
	 * {@link #forEach(Consumer)} method to iterate this image.
	 * 
	 * @param x coordinate
	 * @param y coordinate
	 * @return a Pixel object for this Img at {x,y}.
	 * 
	 * @see #getPixel()
	 */
	public P getPixel(int x, int y);

	/**
	 * Copies this image's data to the specified {@link BufferedImage}.
	 * This method will preserve the {@link Raster} of the specified
	 * BufferedImage and will only modify the contents of it.
	 * 
	 * @param bimg the BufferedImage
	 * @return the specified BufferedImage
	 * @throws IllegalArgumentException if the provided BufferedImage
	 * has a different dimension as this image.
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
	 * Creates a {@link BufferedImage} that shares the data of this image. Changes in
	 * this image are reflected in the created BufferedImage and vice versa.
	 * The {@link ColorModel} and {@link Raster} of the resulting BufferedImage
	 * are implementation dependent.
	 * <p>
	 * This operation may not be supported by an implementation of {@link ImgBase}
	 * and will then throw an {@link UnsupportedOperationException}. Use
	 * {@link #supportsRemoteBufferedImage()} to check if this operation is
	 * supported.
	 * 
	 * @return BufferedImage sharing this Img's data.
	 * @throws UnsupportedOperationException if this implementation of {@link ImgBase}
	 * does not support this method.
	 * 
	 * @see #supportsRemoteBufferedImage()
	 * @see #toBufferedImage(BufferedImage)
	 * @see #toBufferedImage()
	 */
	public default BufferedImage getRemoteBufferedImage(){
		throw new UnsupportedOperationException("This method is not supported. You can check beforehand using supportsRemoteBufferedImage()");
	}


	/**
	 * Returns true when this implementation of {@link ImgBase} supports the 
	 * {@link #getRemoteBufferedImage()} method. This by default also indicates
	 * the support for the following methods:
	 * <ul>
	 * <li>{@link #createGraphics()}</li>
	 * <li>{@link #paint(Consumer)}</li>
	 * </ul>
	 * 
	 * @return true when supported, false otherwise.
	 */
	public default boolean supportsRemoteBufferedImage(){
		return false;
	}

	/**
	 * Creates a {@link Graphics2D}, which can be used to draw into this image.
	 * <br>
	 * This operation may not be supported by an implementation of {@link ImgBase}
	 * and will then throw an {@link UnsupportedOperationException}. Use
	 * {@link #supportsRemoteBufferedImage()} to check if this operation is
	 * supported.
	 * 
	 * @return Graphics2D object to draw into this image.
	 * @throws UnsupportedOperationException if this implementation of {@link ImgBase}
	 * does not support this method.
	 * 
	 * @see #supportsRemoteBufferedImage()
	 * @see #paint(Consumer)
	 */
	public default Graphics2D createGraphics(){
		return getRemoteBufferedImage().createGraphics();
	}

	/**
	 * Uses the specified paintInstructions to draw into this image.
	 * This method will pass a {@link Graphics2D} object of this image to the
	 * specified {@link Consumer}. The {@link Consumer#accept(Object)} method
	 * can then draw into this image. When the accept method returns, the
	 * Graphics2D object is disposed.
	 * <p>
	 * This operation may not be supported by an implementation of {@link ImgBase}
	 * and will then throw an {@link UnsupportedOperationException}. Use
	 * {@link #supportsRemoteBufferedImage()} to check if this operation is
	 * supported.
	 * <p>
	 * Example (using lambda expression for Consumers accept method):
	 * <pre>
	 * {@code
	 * Img img = new Img(100, 100);
	 * img.paint( g2d -> { g2d.drawLine(0, 0, 100, 100); } );
	 * }
	 * </pre>
	 * 
	 * @param paintInstructions to be executed on a Graphics2D object of this image
	 * of this Img.
	 * @throws UnsupportedOperationException if this implementation of {@link ImgBase}
	 * does not support this method.
	 * 
	 * @see #createGraphics()
	 * @since 1.3
	 */
	public default void paint(Consumer<Graphics2D> paintInstructions){
		Graphics2D g2d = createGraphics();
		paintInstructions.accept(g2d);
		g2d.dispose();
	}

	/**
	 * Returns an iterator over the pixels of this image. The iterator will
	 * always return the same object on next() but with different index 
	 * (thus referencing different values in the image).
	 * 
	 * @return an iterator over the pixels of this image.
	 * 
	 * @see #iterator(int, int, int, int)
	 * @see #spliterator()
	 * @see #spliterator(int, int, int, int)
	 */
	@Override
	public default Iterator<P> iterator() {
		return new Iterators.ImgIterator<P>(getPixel());
	}

	/**
	 * Returns the minimum number of elements in a split of a {@link Spliterator}
	 * of this Img. Spliterators will only split if they contain more elements than
	 * specified by this value. Default is 1024.
	 * 
	 * @return minimum number of elements of a Spliterator to allow for splitting.
	 */
	public default int getSpliteratorMinimumSplitSize(){return 1024;}

	/**
	 * Returns an {@link Iterator} for the specified area of the image. The Iterator will
	 * always return the same pixel object on next() but with different index 
	 * (thus referencing different values in the image).
	 * 
	 * @param xStart left boundary of the area (inclusive)
	 * @param yStart upper boundary of the area (inclusive)
	 * @param width of the area
	 * @param height of the area
	 * @return iterator for iterating over the pixels in the specified area.
	 * @throws IllegalArgumentException if provided area is not within this
	 * image's bounds, or if the area is not positive (width or height &le; 0). 
	 * 
	 * @see #iterator()
	 * @see #spliterator()
	 * @see #spliterator(int, int, int, int)
	 */
	public default Iterator<P> iterator(final int xStart, final int yStart, final int width, final int height) {
		ImagingKitUtils.requireAreaInImageBounds(xStart, yStart, width, height, this);
		return new Iterators.ImgAreaIterator<P>(xStart, yStart, width, height, getPixel());
	}

	/**
	 * Returns a {@link Spliterator} over the pixels of this image. Within each
	 * split of the Spliterator a unique pixel object will be used on tryAdvance()
	 * but with changing index (thus referencing different values in the image).
	 * 
	 * @return a Spliterator over the pixels of this image
	 * 
	 * @see #spliterator(int, int, int, int)
	 * @see #rowSpliterator()
	 * @see #colSpliterator()
	 * @see #iterator()
	 * @see #iterator(int, int, int, int)
	 */
	@Override
	public default Spliterator<P> spliterator() {
		return new Iterators.ImgSpliterator<P>(0, numValues()-1, getSpliteratorMinimumSplitSize(),this::getPixel);
	}

	/**
	 * Creates a {@link Spliterator} that guarantees that each split will
	 * at least cover an entire row of the Img. It also guarantes that each
	 * row will be iterated starting at the least index of that row
	 * (e.g.starts at index 0 then continues with index 1, then 2, until
	 * the end of the row, then continuing with the next row).
	 * This Spliterator iterates in row-major order.
	 * 
	 * @return Spliterator that splits at beginning of rows.
	 * 
	 * @see #colSpliterator()
	 * @see #spliterator()
	 * @see #stream(Spliterator, boolean)
	 * @see #stream(PixelConverter, boolean)
	 */
	public default Spliterator<P> rowSpliterator() {
		return new Iterators.RowSpliterator<P>(0, getWidth(), 0, getHeight()-1, this::getPixel);
	}

	/**
	 * Creates a {@link Spliterator} that guarantees that each split will
	 * at least cover an entire column of the Img. It also guarantes that each
	 * column will be iterated starting at the least index of that column
	 * (e.g.starts at index 0 then continues with index 1, then 2, until
	 * the end of the column, then continuing with the next column).
	 * This Spliterator iterates in column-major order.
	 * 
	 * @return Spliterator that splits at beginning of columns.
	 * 
	 * @see #rowSpliterator()
	 * @see #spliterator()
	 * @see #stream(Spliterator, boolean)
	 * @see #stream(PixelConverter, boolean)
	 */
	public default Spliterator<P> colSpliterator() {
		return new Iterators.ColSpliterator<P>(0, getWidth()-1, 0, getHeight(), this::getPixel);
	}

	/**
	 * Creates a {@link Spliterator} over the pixels within the specified area.
	 * @param xStart left boundary of the area (inclusive)
	 * @param yStart upper boundary of the area (inclusive)
	 * @param width of the area
	 * @param height of the area
	 * @return spliterator for the specified area.
	 * @throws IllegalArgumentException if provided area is not within this
	 * image's bounds, or if the area is not positive (width or height &le; 0). 
	 * 
	 * @see #spliterator()
	 * @see #colSpliterator()
	 * @see #rowSpliterator()
	 * @see #stream(Spliterator, boolean)
	 * @see #stream(PixelConverter, boolean)
	 */
	public default Spliterator<P> spliterator(final int xStart, final int yStart, final int width, final int height) {
		ImagingKitUtils.requireAreaInImageBounds(xStart, yStart, width, height, this);
		return new Iterators.ImgAreaSpliterator<P>(xStart,yStart,width,height, getSpliteratorMinimumSplitSize(), this::getPixel);
	}


	/** 
	 * Default implementation of {@link Iterable#forEach(Consumer)} <br>
	 * only for performance test purposes as it is slower than the
	 * {@link Img#forEach(Consumer)} implementation.
	 * @param action to be performed
	 */
	default void forEach_defaultimpl(final Consumer<? super P> action) {
		Iterable.super.forEach(action);
	}

	/**
	 * Performs the specified action on each of the pixels of this image.
	 * @param action to be performed
	 * 
	 * @see #forEach(PixelManipulator)
	 * @see #forEach(boolean, Consumer)
	 * @see #forEach(boolean, PixelManipulator)
	 * @see #forEach(PixelConverter, boolean, Consumer)
	 * @see #forEach(int, int, int, int, Consumer)
	 * @see #forEach(int, int, int, int, PixelManipulator)
	 * @see #forEach(boolean, int, int, int, int, Consumer)
	 * @see #forEach(boolean, int, int, int, int, PixelManipulator)
	 * @see #forEach(PixelConverter, boolean, int, int, int, int, Consumer)
	 * @see #stream()
	 * @since 1.0
	 */
	@Override
	public default void forEach(final Consumer<? super P> action) {
		forEach(false, action);
	}

	/**
	 * Performs the specified action on each of the pixels of this image.
	 * @param parallel whether to be performed in parallel
	 * @param action to be performed
	 * 
	 * @see #forEach(Consumer)
	 * @see #forEach(PixelManipulator)
	 * @see #forEach(boolean, Consumer)
	 * @see #forEach(boolean, PixelManipulator)
	 * @see #forEach(PixelConverter, boolean, Consumer)
	 * @see #forEach(int, int, int, int, Consumer)
	 * @see #forEach(int, int, int, int, PixelManipulator)
	 * @see #forEach(boolean, int, int, int, int, Consumer)
	 * @see #forEach(boolean, int, int, int, int, PixelManipulator)
	 * @see #forEach(PixelConverter, boolean, int, int, int, int, Consumer)
	 * @see #stream()
	 */
	public default void forEach(boolean parallel, final Consumer<? super P> action) {
		if(parallel){
			ParallelForEachExecutor<P> exec = new ParallelForEachExecutor<>(spliterator(), action);
			exec.invoke();
		} else {
			P p = getPixel();
			for(int i = 0; i < numValues(); p.setIndex(++i)){
				action.accept(p);
			}
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
	 * images's bounds.
	 * 
	 * @see #forEach(Consumer)
	 * @see #forEach(PixelManipulator)
	 * @see #forEach(boolean, Consumer)
	 * @see #forEach(boolean, PixelManipulator)
	 * @see #forEach(PixelConverter, boolean, Consumer)
	 * @see #forEach(int, int, int, int, Consumer)
	 * @see #forEach(int, int, int, int, PixelManipulator)
	 * @see #forEach(boolean, int, int, int, int, Consumer)
	 * @see #forEach(boolean, int, int, int, int, PixelManipulator)
	 * @see #forEach(PixelConverter, boolean, int, int, int, int, Consumer)
	 * @see #stream()
	 */
	public default void forEach(final int xStart, final int yStart, final int width, final int height, final Consumer<? super P> action) {
		forEach(false, xStart, yStart, width, height, action);
	}

	/**
	 * Applies the specified action to every pixel in the specified area of this image.
	 * @param parallel whether to be performed in parallel
	 * @param xStart left boundary of the area (inclusive)
	 * @param yStart upper boundary of the area (inclusive)
	 * @param width of the area
	 * @param height of the area
	 * @param action to be performed on each pixel
	 * @throws IllegalArgumentException if provided area is not within this
	 * images's bounds, or if the area is not positive (width or height &le; 0). 
	 * 
	 * @see #forEach(Consumer)
	 * @see #forEach(PixelManipulator)
	 * @see #forEach(boolean, Consumer)
	 * @see #forEach(boolean, PixelManipulator)
	 * @see #forEach(PixelConverter, boolean, Consumer)
	 * @see #forEach(int, int, int, int, Consumer)
	 * @see #forEach(int, int, int, int, PixelManipulator)
	 * @see #forEach(boolean, int, int, int, int, Consumer)
	 * @see #forEach(boolean, int, int, int, int, PixelManipulator)
	 * @see #forEach(PixelConverter, boolean, int, int, int, int, Consumer)
	 * @see #stream()
	 */
	public default void forEach(boolean parallel, final int xStart, final int yStart, final int width, final int height, final Consumer<? super P> action) {
		ImagingKitUtils.requireAreaInImageBounds(xStart, yStart, width, height, this);
		if(parallel){
			ParallelForEachExecutor<P> exec = new ParallelForEachExecutor<>(spliterator(xStart, yStart, width, height), action);
			exec.invoke();
		} else {
			P p = getPixel();
			int yEnd = yStart+height;
			int xEnd = xStart+width;
			for(int y = yStart; y < yEnd; y++){
				for(int x = xStart; x < xEnd; x++){
					p.setPosition(x, y);
					action.accept(p);
				}
			}
		}
	}

	/**
	 * Applies the specified action to every pixel of this image.
	 * Prior to applying the action, each time the pixel is converted using the specified
	 * converter. The action is then performed on an instance of the element type of the converter
	 * (which is also the type accepted by the action).
	 * Finally the modified instance is then converted back to the pixel.
	 * 
	 * @param converter that converts the pixel to the type accepted by the action
	 * @param parallel whether to be performed in parallel
	 * @param action to be performed on each pixel
	 * 
	 * @param <T> converter's element type
	 * 
	 * @see #forEach(Consumer)
	 * @see #forEach(PixelManipulator)
	 * @see #forEach(boolean, Consumer)
	 * @see #forEach(boolean, PixelManipulator)
	 * @see #forEach(PixelConverter, boolean, Consumer)
	 * @see #forEach(int, int, int, int, Consumer)
	 * @see #forEach(int, int, int, int, PixelManipulator)
	 * @see #forEach(boolean, int, int, int, int, Consumer)
	 * @see #forEach(boolean, int, int, int, int, PixelManipulator)
	 * @see #forEach(PixelConverter, boolean, int, int, int, int, Consumer)
	 * @see #stream()
	 */
	public default <T> void forEach(final PixelConverter<? super P,T> converter, boolean parallel, final Consumer<? super T> action) {
		if(parallel){
			Spliterator<T> spliterator = new PixelConvertingSpliterator<>(
					spliterator(),
					converter);
	 		ParallelForEachExecutor<T> exec = new ParallelForEachExecutor<>(spliterator, action);
			exec.invoke();
		} else {
			P px = getPixel();
			T element = converter.allocateElement();
			for(int i = 0; i < numValues(); px.setIndex(++i)){
				converter.convertPixelToElement(px, element);
				action.accept(element);
				converter.convertElementToPixel(element, px);
			}
		}
	}


	/**
	 * Applies the specified action to every pixel in the specified area of this image.
	 * Prior to applying the action, each time the pixel is converted using the specified
	 * converter. The action is then performed on an instance of the element type of the converter
	 * (which is also the type accepted by the action).
	 * Finally the modified instance is then converted back to the pixel.
	 * 
	 * @param converter that converts the pixel to the type accepted by the action
	 * @param parallel whether to be performed in parallel
	 * @param xStart left boundary of the area (inclusive)
	 * @param yStart upper boundary of the area (inclusive)
	 * @param width of the area
	 * @param height of the area
	 * @param action to be performed on each pixel
	 * 
	 * @param <T> converter's element type
	 * 
	 * @throws IllegalArgumentException if provided area is not within this
	 * images's bounds, or if the area is not positive (width or height &le; 0). 
	 * 
	 * @see #forEach(Consumer)
	 * @see #forEach(PixelManipulator)
	 * @see #forEach(boolean, Consumer)
	 * @see #forEach(boolean, PixelManipulator)
	 * @see #forEach(PixelConverter, boolean, Consumer)
	 * @see #forEach(int, int, int, int, Consumer)
	 * @see #forEach(int, int, int, int, PixelManipulator)
	 * @see #forEach(boolean, int, int, int, int, Consumer)
	 * @see #forEach(boolean, int, int, int, int, PixelManipulator)
	 * @see #forEach(PixelConverter, boolean, int, int, int, int, Consumer)
	 * @see #stream()
	 */
	public default <T> void forEach(final PixelConverter<? super P, T> converter, boolean parallel, final int xStart, final int yStart, final int width, final int height, final Consumer<? super T> action) {
		ImagingKitUtils.requireAreaInImageBounds(xStart, yStart, width, height, this);
		if(parallel){
			Spliterator<T> spliterator = new PixelConvertingSpliterator<>(
					spliterator(xStart, yStart, width, height),
					converter);
			ParallelForEachExecutor<T> exec = new ParallelForEachExecutor<>(spliterator, action);
			exec.invoke();
		} else {
			P p = getPixel();
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
	}

	/**
	 * Applies the specified manipulator to every pixel of this image.
	 * @param manipulator that will be applied
	 * @param <T> manipulator's element type
	 * 
	 * @see #forEach(Consumer)
	 * @see #forEach(PixelManipulator)
	 * @see #forEach(boolean, Consumer)
	 * @see #forEach(boolean, PixelManipulator)
	 * @see #forEach(PixelConverter, boolean, Consumer)
	 * @see #forEach(int, int, int, int, Consumer)
	 * @see #forEach(int, int, int, int, PixelManipulator)
	 * @see #forEach(boolean, int, int, int, int, Consumer)
	 * @see #forEach(boolean, int, int, int, int, PixelManipulator)
	 * @see #forEach(PixelConverter, boolean, int, int, int, int, Consumer)
	 * @see #stream()
	 */
	public default <T> void forEach(final PixelManipulator<? super P,T> manipulator) {
		forEach(false, manipulator);
	}

	/**
	 * Applies the specified manipulator to every pixel of this image.
	 * @param parallel whether to be performed in parallel
	 * @param manipulator that will be applied
	 * @param <T> manipulator's element type
	 * 
	 * @see #forEach(Consumer)
	 * @see #forEach(PixelManipulator)
	 * @see #forEach(boolean, Consumer)
	 * @see #forEach(boolean, PixelManipulator)
	 * @see #forEach(PixelConverter, boolean, Consumer)
	 * @see #forEach(int, int, int, int, Consumer)
	 * @see #forEach(int, int, int, int, PixelManipulator)
	 * @see #forEach(boolean, int, int, int, int, Consumer)
	 * @see #forEach(boolean, int, int, int, int, PixelManipulator)
	 * @see #forEach(PixelConverter, boolean, int, int, int, int, Consumer)
	 * @see #stream()
	 */
	public default <T> void forEach(final boolean parallel, final PixelManipulator<? super P,T> manipulator) {
		forEach(manipulator.getConverter(), parallel, manipulator.getAction());
	}

	/**
	 * Applies the specified manipulator to every pixel in the specified area of this image.
	 * @param xStart left boundary of the area (inclusive)
	 * @param yStart upper boundary of the area (inclusive)
	 * @param width of the area
	 * @param height of the area
	 * @param manipulator that will be applied
	 * 
	 * @param <T> manipulator's element type
	 * 
	 * @throws IllegalArgumentException if provided area is not within this
	 * images's bounds.
	 * 
	 * @see #forEach(Consumer)
	 * @see #forEach(PixelManipulator)
	 * @see #forEach(boolean, Consumer)
	 * @see #forEach(boolean, PixelManipulator)
	 * @see #forEach(PixelConverter, boolean, Consumer)
	 * @see #forEach(int, int, int, int, Consumer)
	 * @see #forEach(int, int, int, int, PixelManipulator)
	 * @see #forEach(boolean, int, int, int, int, Consumer)
	 * @see #forEach(boolean, int, int, int, int, PixelManipulator)
	 * @see #forEach(PixelConverter, boolean, int, int, int, int, Consumer)
	 * @see #stream()
	 */
	public default <T> void forEach(final int xStart, final int yStart, final int width, final int height, final PixelManipulator<? super P,T> manipulator) {
		forEach(false, xStart, yStart, width, height, manipulator);
	}

	/**
	 * Applies the specified manipulator to every pixel in the specified area of this image.
	 * @param parallel whether to be performed in parallel
	 * @param xStart left boundary of the area (inclusive)
	 * @param yStart upper boundary of the area (inclusive)
	 * @param width of the area
	 * @param height of the area
	 * @param manipulator that will be applied
	 * 
	 * @param <T> manipulator's element type
	 * 
	 * @throws IllegalArgumentException if provided area is not within this
	 * images's bounds.
	 * 
	 * @see #forEach(Consumer)
	 * @see #forEach(PixelManipulator)
	 * @see #forEach(boolean, Consumer)
	 * @see #forEach(boolean, PixelManipulator)
	 * @see #forEach(PixelConverter, boolean, Consumer)
	 * @see #forEach(int, int, int, int, Consumer)
	 * @see #forEach(int, int, int, int, PixelManipulator)
	 * @see #forEach(boolean, int, int, int, int, Consumer)
	 * @see #forEach(boolean, int, int, int, int, PixelManipulator)
	 * @see #forEach(PixelConverter, boolean, int, int, int, int, Consumer)
	 * @see #stream()
	 */
	public default <T> void forEach(final boolean parallel, final int xStart, final int yStart, final int width, final int height, final PixelManipulator<? super P,T> manipulator) {
		forEach(manipulator.getConverter(), parallel, xStart, yStart, width, height, manipulator.getAction());
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
	 * ImgBase.stream(myImg.rowSpliterator(), true).forEach( px -> {
	 *     int next = px.getImg().getValue(px.getX()+1, px.getY(), Img.boundary_mode_repeat_edge);
	 *     int forwardDiff = Math.abs( Pixel.getLuminance(next) - px.getLuminance() );
	 *     px.setRGB(forwardDiff, forwardDiff, forwardDiff);
	 * });
	 * }
	 * </pre>
	 * @param spliterator Spliterator of Img to be streamed
	 * @param parallel whether parallel or sequential stream is returned
	 * 
	 * @param <Px> pixel type of the stream
	 * @return a new sequential or parallel pixel stream.
	 *
	 * @see #stream()
	 */
	public static <Px extends PixelBase> Stream<Px> stream(Spliterator<Px> spliterator, boolean parallel){
		return StreamSupport.stream(spliterator, parallel);
	}

	/**
	 * Returns a sequential {@link Stream} of pixels of this image.
	 * This Img's {@link #spliterator()} is used to create the Stream.
	 * <p>
	 * <b>The elements of this stream are not distinct!</b><br>
	 * This is due to a {@link PixelBase} object being a pointer into
	 * the data of the image and not a pixel value itself. While streaming
	 * the index of the pixel object is changed for each actual pixel of the image.
	 * <br>
	 * Thus, a Set created by the expression {@code img.stream().collect(Collectors.toSet())}
	 * will only contain a single element.
	 * 
	 * @return pixel Stream of this image.
	 * 
	 * @see #stream()
	 * @see #stream(boolean parallel)
	 * @see #stream(int x, int y, int w, int h)
	 * @see #stream(boolean parallel, int x, int y, int w, int h)
	 * @see #stream(PixelConverter c, boolean parallel)
	 * @see #stream(PixelConverter c, boolean parallel, int x, int y, int w, int h)
	 * @see #stream(Spliterator spliterator, boolean parallel)
	 * @see #forEach(Consumer action)
	 */
	public default Stream<P> stream() {
		return stream(false);
	}

	/**
	 * Returns a {@link Stream} of pixels of this image.
	 * Depending on the specified argument, the stream will be parallel or sequential.
	 * This Img's {@link #spliterator()} is used to create the Stream.
	 * <p>
	 * <b>The elements of this stream are not distinct!</b><br>
	 * This is due to a {@link PixelBase} object being a pointer into
	 * the data of the image and not a pixel value itself. While streaming
	 * the index of the pixel object is changed for each actual pixel of the image.
	 * <br>
	 * Thus, a Set created by the expression {@code img.stream().collect(Collectors.toSet())}
	 * will only contain a single element.
	 * 
	 * @param parallel whether the stream is parallel (true) or sequential (false)
	 * @return pixel Stream of this image.
	 * 
	 * @see #stream()
	 * @see #stream(boolean parallel)
	 * @see #stream(int x, int y, int w, int h)
	 * @see #stream(boolean parallel, int x, int y, int w, int h)
	 * @see #stream(PixelConverter c, boolean parallel)
	 * @see #stream(PixelConverter c, boolean parallel, int x, int y, int w, int h)
	 * @see #stream(Spliterator spliterator, boolean parallel)
	 * @see #forEach(Consumer action)
	 */
	public default Stream<P> stream(boolean parallel) {
		return ImgBase.stream(spliterator(), parallel);
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
	 * 
	 * @see #stream()
	 * @see #stream(boolean parallel)
	 * @see #stream(int x, int y, int w, int h)
	 * @see #stream(boolean parallel, int x, int y, int w, int h)
	 * @see #stream(PixelConverter c, boolean parallel)
	 * @see #stream(PixelConverter c, boolean parallel, int x, int y, int w, int h)
	 * @see #stream(Spliterator spliterator, boolean parallel)
	 * @see #forEach(Consumer action)
	 */
	public default Stream<P> stream(final int xStart, final int yStart, final int width, final int height){
		return stream(false, xStart, yStart, width, height);
	}

	/**
	 * Returns a Pixel {@link Stream} for the specified area of this Img.<br>
	 * This Img's {@link #spliterator(int,int,int,int)} is used to create
	 * the Stream.
	 * 
	 * @param parallel whether the stream is parallel (true) or sequential (false)
	 * @param xStart left boundary of the area (inclusive)
	 * @param yStart upper boundary of the area (inclusive)
	 * @param width of the area
	 * @param height of the area
	 * @return Pixel Stream for specified area.
	 * @throws IllegalArgumentException if provided area is not within this
	 * image's bounds.
	 * 
	 * @see #stream()
	 * @see #stream(boolean parallel)
	 * @see #stream(int x, int y, int w, int h)
	 * @see #stream(boolean parallel, int x, int y, int w, int h)
	 * @see #stream(PixelConverter c, boolean parallel)
	 * @see #stream(PixelConverter c, boolean parallel, int x, int y, int w, int h)
	 * @see #stream(Spliterator spliterator, boolean parallel)
	 * @see #forEach(Consumer action)
	 */
	public default Stream<P> stream(boolean parallel, final int xStart, final int yStart, final int width, final int height){
		return StreamSupport.stream(spliterator(xStart, yStart, width, height), parallel);
	}

	/**
	 * Returns a {@link Stream} of the specified {@link PixelConverter}'s element type 
	 * over the pixels of this image. Each pixel will be converted to the element type
	 * before being processed, and the element will be back converted to the pixel after
	 * processing. The conversion and back conversion is handled by the specified converter.
	 * 
	 * @param converter that determines the element type of the stream and handles conversion/back conversion
	 * @param parallel whether the stream is parallel (true) or sequential (false)
	 * 
	 * @param <T> converter's element type
	 * @return a Stream over the pixels of this image in the representation given by the specified converter.
	 * 
	 * @see #stream()
	 * @see #stream(boolean parallel)
	 * @see #stream(int x, int y, int w, int h)
	 * @see #stream(boolean parallel, int x, int y, int w, int h)
	 * @see #stream(PixelConverter c, boolean parallel)
	 * @see #stream(PixelConverter c, boolean parallel, int x, int y, int w, int h)
	 * @see #stream(Spliterator spliterator, boolean parallel)
	 * @see #forEach(boolean, PixelManipulator)
	 * @see #forEach(PixelConverter, boolean, Consumer)
	 */
	public default <T> Stream<T> stream(PixelConverter<? super P, T> converter, boolean parallel) {
		Spliterator<T> spliterator = new PixelConvertingSpliterator<>(spliterator(), converter);
		return StreamSupport.stream(spliterator, parallel);
	}

	/**
	 * Returns a {@link Stream} of the specified {@link PixelConverter}'s element type 
	 * over the pixels of this image within the specified area. Each pixel will be converted to the element type
	 * before being processed, and the element will be back converted to the pixel after
	 * processing. The conversion and back conversion is handled by the specified converter.
	 * 
	 * @param converter that determines the element type of the stream and handles conversion/back conversion
	 * @param parallel whether the stream is parallel (true) or sequential (false)
	 * @param xStart left boundary of the area (inclusive)
	 * @param yStart upper boundary of the area (inclusive)
	 * @param width of the area
	 * @param height of the area
	 * 
	 * @param <T> converter's element type
	 * 
	 * @return a Stream over the pixels in the specified area of this image 
	 * in the representation given by the specified converter.
	 * @throws IllegalArgumentException if provided area is not within this image's bounds.
	 * 
	 * @see #stream()
	 * @see #stream(boolean parallel)
	 * @see #stream(int x, int y, int w, int h)
	 * @see #stream(boolean parallel, int x, int y, int w, int h)
	 * @see #stream(PixelConverter c, boolean parallel)
	 * @see #stream(PixelConverter c, boolean parallel, int x, int y, int w, int h)
	 * @see #stream(Spliterator spliterator, boolean parallel)
	 * @see #forEach(boolean parallel, int x, int y, int w, int h, PixelManipulator m)
	 * @see #forEach(PixelConverter c, boolean parallel, int x, int y, int w, int h, Consumer action)
	 */
	public default <T> Stream<T> stream(final PixelConverter<? super P, T> converter, boolean parallel, final int xStart, final int yStart, final int width, final int height){
		Spliterator<T> spliterator = new PixelConvertingSpliterator<>(
				spliterator(xStart, yStart, width, height),
				converter);
		return StreamSupport.stream(spliterator, parallel);
	}
	
	/**
	 * Returns a deep copy of this image. 
	 * 'Deep' means that changes made to this image are NOT reflected in the copy.
	 * @return a deep copy.
	 */
	public ImgBase<P> copy();
	
}
