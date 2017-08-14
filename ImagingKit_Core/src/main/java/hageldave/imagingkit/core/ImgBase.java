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
import java.util.Iterator;
import java.util.Spliterator;
import java.util.function.Consumer;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import hageldave.imagingkit.core.PixelConvertingSpliterator.PixelConverter;
import hageldave.imagingkit.core.util.BufferedImageFactory;
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
 * @param <P>
 *
 * @author hageldave
 * @since 2.0
 */
public interface ImgBase<P extends PixelBase> extends Iterable<P> {

	public Dimension getDimension();

	public default int getWidth(){return getDimension().width;}

	public default int getHeight(){return getDimension().height;}

	public default int numValues(){return getWidth()*getHeight();}

	/**
	 * Creates a new Pixel object for this Img with position {0,0}.
	 * @return a Pixel object for this Img.
	 * @since 1.0
	 */
	public P getPixel();

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
	public P getPixel(int x, int y);

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
	public BufferedImage toBufferedImage(BufferedImage bimg);

	/**
	 * @return a BufferedImage of type INT_ARGB with this Img's data copied to it.
	 * @see #toBufferedImage(BufferedImage)
	 * @see #getRemoteBufferedImage()
	 * @since 1.0
	 */
	public default BufferedImage toBufferedImage(){
		BufferedImage bimg = BufferedImageFactory.getINT_ARGB(getDimension());
		return toBufferedImage(bimg);
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
	public default BufferedImage getRemoteBufferedImage(){
		throw new UnsupportedOperationException();
	}


	public default boolean supportsRemoteBufferedImage(){
		return false;
	}

	/**
	 * Creates a {@link Graphics2D}, which can be used to draw into this Img.
	 * @return Graphics2D object to draw into this image.
	 * @see #paint(Consumer)
	 * @since 1.3
	 */
	public default Graphics2D createGraphics(){
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
	public default void paint(Consumer<Graphics2D> paintInstructions){
		Graphics2D g2d = createGraphics();
		paintInstructions.accept(g2d);
		g2d.dispose();
	}

	@Override
	public default Iterator<P> iterator() {
		return new Iterators.ImgIterator<P>(numValues(), getPixel()) ;
	}

	/**
	 * Returns the minimum number of elements in a split of a {@link Spliterator}
	 * of this Img. Spliterators will only split if they contain more elements than
	 * specified by this value. Default is 1024.
	 * @return minimum number of elements of a Spliterator to allow for splitting.
	 * @since 1.3
	 */
	public default int getSpliteratorMinimumSplitSize(){return 1024;}

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
	public default Iterator<P> iterator(final int xStart, final int yStart, final int width, final int height) {
		if(		width <= 0 || height <= 0 ||
				xStart < 0 || yStart < 0 ||
				xStart+width > getWidth() || yStart+height > getHeight() )
		{
			throw new IllegalArgumentException(String.format(
							"provided area [%d,%d][%d,%d] is not within bounds of the image [%d,%d]",
							xStart,yStart,width,height, getWidth(), getHeight()));
		}
		return new Iterators.ImgAreaIterator<P>(xStart, yStart, width, height, getPixel());
	}

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
	 * @return Spliterator that splits at beginning of rows.
	 * @see #colSpliterator()
	 * @see #spliterator()
	 * @see #stream(Spliterator, boolean)
	 * @since 1.3
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
	 * @return Spliterator that splits at beginning of columns.
	 * @see #rowSpliterator()
	 * @see #spliterator()
	 * @see #stream(Spliterator, boolean)
	 * @since 1.3
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
	 * Img's bounds.
	 * @since 1.1
	 */
	public default Spliterator<P> spliterator(final int xStart, final int yStart, final int width, final int height) {
		if(		width <= 0 || height <= 0 ||
				xStart < 0 || yStart < 0 ||
				xStart+width > getWidth() || yStart+height > getHeight() )
		{
			throw new IllegalArgumentException(String.format(
							"provided area [%d,%d][%d,%d] is not within bounds of the image [%d,%d]",
							xStart,yStart,width,height, getWidth(), getHeight()));
		}
		return new Iterators.ImgAreaSpliterator<P>(xStart,yStart,width,height, getSpliteratorMinimumSplitSize(), this::getPixel);
	}


	/** default implementation of {@link Iterable#forEach(Consumer)} <br>
	 * only for performance test purposes as it is slower than the
	 * {@link Img#forEach(Consumer)} implementation
	 * @since 1.0
	 */
	default void forEach_defaultimpl(final Consumer<? super P> action) {
		Iterable.super.forEach(action);
	}

	/**
	 * @see #forEachParallel(Consumer action)
	 * @since 1.0
	 */
	@Override
	public default void forEach(final Consumer<? super P> action) {
		forEach(false, action);
	}

	public default void forEach(boolean parallel, final Consumer<? super P> action) {
		if(parallel){
			ParallelForEachExecutor<P> exec = new ParallelForEachExecutor<>(null, spliterator(), action);
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
	 * Img's bounds.
	 * @see #forEachParallel(int x, int y, int w, int h, Consumer action)
	 * @since 1.1
	 */
	public default void forEach(final int xStart, final int yStart, final int width, final int height, final Consumer<? super P> action) {
		forEach(false, xStart, yStart, width, height, action);
	}

	public default void forEach(boolean parallel, final int xStart, final int yStart, final int width, final int height, final Consumer<? super P> action) {
		if(parallel){
			ParallelForEachExecutor<P> exec = new ParallelForEachExecutor<>(null, spliterator(xStart, yStart, width, height), action);
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

	public default <T> void forEach(final PixelConverter<? super P,T> converter, boolean parallel, final Consumer<? super T> action) {
		if(parallel){
			Spliterator<T> spliterator = new PixelConvertingSpliterator<>(
					spliterator(),
					converter);
	 		ParallelForEachExecutor<T> exec = new ParallelForEachExecutor<>(null, spliterator, action);
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

	public default <T> void forEach(final PixelConverter<? super P, T> converter, boolean parallel, final int xStart, final int yStart, final int width, final int height, final Consumer<? super T> action) {
		if(parallel){
			Spliterator<T> spliterator = new PixelConvertingSpliterator<>(
					spliterator(xStart, yStart, width, height),
					converter);
			ParallelForEachExecutor<T> exec = new ParallelForEachExecutor<>(null, spliterator, action);
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

	public default <T> void forEach(final PixelManipulator<T> manipulator) {
		forEach(false, manipulator);
	}

	public default <T> void forEach(final boolean parallel, final PixelManipulator<T> manipulator) {
		forEach(manipulator.getConverter(), parallel, manipulator.getAction());
	}

	public default <T> void forEach(final int xStart, final int yStart, final int width, final int height, final PixelManipulator<T> manipulator) {
		forEach(false, xStart, yStart, width, height, manipulator);
	}

	public default <T> void forEach(final boolean parallel, final int xStart, final int yStart, final int width, final int height, final PixelManipulator<T> manipulator) {
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
	public static <Px extends PixelBase> Stream<Px> stream(Spliterator<Px> spliterator, boolean parallel){
		return StreamSupport.stream(spliterator, parallel);
	}

	/**
	 * Returns a Pixel {@link Stream} of this Img.
	 * This Img's {@link #spliterator()} is used to create the Stream.
	 * @return Pixel Stream of this Img.
	 * @see #parallelStream()
	 * @see #stream(int x, int y, int w, int h)
	 * @since 1.2
	 */
	public default Stream<P> stream() {
		return stream(false);
	}

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
	 * @see #parallelStream(int x, int y, int w, int h)
	 * @see #stream()
	 * @since 1.2
	 */
	public default Stream<P> stream(final int xStart, final int yStart, final int width, final int height){
		return stream(false, xStart, yStart, width, height);
	}

	public default Stream<P> stream(boolean parallel, final int xStart, final int yStart, final int width, final int height){
		return StreamSupport.stream(spliterator(xStart, yStart, width, height), parallel);
	}

	public default <T> Stream<T> stream(PixelConverter<? super P, T> converter, boolean parallel) {
		Spliterator<T> spliterator = new PixelConvertingSpliterator<>(spliterator(), converter);
		return StreamSupport.stream(spliterator, parallel);
	}

	public default <T> Stream<T> stream(final PixelConverter<? super P, T> converter, boolean parallel, final int xStart, final int yStart, final int width, final int height){
		Spliterator<T> spliterator = new PixelConvertingSpliterator<>(
				spliterator(xStart, yStart, width, height),
				converter);
		return StreamSupport.stream(spliterator, parallel);
	}

}
