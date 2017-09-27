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

import java.util.Iterator;
import java.util.Spliterator;
import java.util.function.Consumer;
import java.util.function.Supplier;


/**
 * Class holding all of the {@link Iterator} and {@link Spliterator} classes
 * used in the {@link ImgBase} interface.
 * <p>
 * <b>NOTE ON ELEMENTS OF ITERATORS/SPLITERATORS</b><br>
 * <i>All of the iterators/spliterators in this class implement the following paradigm:</i><br>
 * As image implementations typically use buffers or arrays of a native datatype as a
 * representation of their pixel values, they are not collections of a pixel datatype.
 * The pixel datatypes used here are thus pointers into an image's array data structure.
 * <u>When iterating the pixel data of an image, a pixel object will be reused for each
 * image pixel.</u> This is done to avoid excessive allocation of pixel objects and keep
 * the garbage collector 'asleep' (as GC heavily impacts performance). This means that
 * the elements (pixel objects) returned by an iterator/spliterator are not distinct.
 * 
 * @author hageldave
 * @since 2.0
 */
public final class Iterators {

	private Iterators(){/*not to be instantiated*/}


	/**
	 * The standard {@link Iterator} class for images.
	 * @author hageldave
	 * @param <P> the pixel type of this iterator
	 */
	public static class ImgIterator<P extends PixelBase> implements Iterator<P> {
		
		private final P px;
		private final int numValues;
		private int index;

		/**
		 * Creates a new ImgIterator over the image of the specified pixel.
		 * The specified pixel will be reused on every invocation of {@link #next()}
		 * with incremented index.
		 * 
		 * @param px pixel that will be used to iterate its image
		 */
		public ImgIterator(P px) {
			this.px = px;
			this.numValues = px.getSource().numValues();
			this.index = -1;
		}

		@Override
		public P next() {
			px.setIndex(++index);
			return px;
		}

		@Override
		public boolean hasNext() {
			return index+1 < numValues;
		}

		@Override
		public void forEachRemaining(Consumer<? super P> action) {
			px.setIndex(++index);
			for(/*index*/; index < numValues; px.setIndex(++index)){
				action.accept(px);
			}
		}
	}


	/**
	 * The standard iterator class for iterating over an area of an image.
	 * @author hageldave
	 * @param <P> the pixel type of this iterator
	 */
	public static class ImgAreaIterator<P extends PixelBase> implements Iterator<P> {
		private final P px;
		private final int xStart;
		private final int yStart;
		private final int width;
		private final int height;
		private int x;
		private int y;

		/**
		 * Creates a new ImgAreaIterator for iterating the pixels in the specified area of an image.
		 * The iterated image is the source of the specified pixel.
		 * The specified pixel will be reused on every invocation of {@link #next()}
		 * with incremented index.
		 * 
		 * @param xStart the left boundary of the area (inclusive)
		 * @param yStart the top boundary of the area (inclusive)
		 * @param width of the area
		 * @param height of the area
		 * @param px the pixel used for iterating
		 */
		public ImgAreaIterator(final int xStart, final int yStart, final int width, final int height, P px) {
			this.px = px;
			this.xStart = xStart;
			this.yStart = yStart;
			this.width = width;
			this.height = height;
			this.x=0;
			this.y=0;
		}


		@Override
		public P next() {
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
		public void forEachRemaining(Consumer<? super P> action) {
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
	}
	
	/**
	 * The standard {@link Spliterator} class for images.
	 * @author hageldave
	 */
	public static final class ImgSpliterator<P extends PixelBase> implements Spliterator<P> {
	
		private final Supplier<P> pixelSupplier;
		private final P px;
		private int endIndex;
		private final int minimumSplitSize;
	
		/**
		 * Constructs a new ImgSpliterator for the specified index range
		 * @param startIndex first index of the range (inclusive)
		 * @param endIndex last index of the range (inclusive)
		 * @param minSplitSize minimum split size for this spliterator (minimum number of elements in a split)
		 * @param pixelSupplier a function that allocates a new pixel
		 * that points to the index given by the function argument
		 * @since 1.0
		 */
		public ImgSpliterator(int startIndex, int endIndex, int minSplitSize, Supplier<P> pixelSupplier) {
			this.pixelSupplier = pixelSupplier;
			this.px = pixelSupplier.get();
			this.px.setIndex(startIndex);
			this.endIndex = endIndex;
			this.minimumSplitSize = minSplitSize;
		}
	
		private void setEndIndex(int endIndex) {
			this.endIndex = endIndex;
		}
	
		@Override
		public boolean tryAdvance(final Consumer<? super P> action) {
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
		public void forEachRemaining(final Consumer<? super P> action) {
			int idx = px.getIndex();
			for(;idx <= endIndex; px.setIndex(++idx)){
				action.accept(px);
			}
		}
	
		@Override
		public Spliterator<P> trySplit() {
			int currentIdx = Math.min(px.getIndex(), endIndex);
			int range = endIndex+1-currentIdx;
			if(range/2 >= minimumSplitSize){
				int mid = currentIdx+range/2;
				ImgSpliterator<P> split = new ImgSpliterator<>(mid, endIndex, minimumSplitSize, pixelSupplier);
				setEndIndex(mid-1);
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
	 * Spliterator class for images bound to a specific area
	 * @author hageldave
	 */
	public static final class ImgAreaSpliterator<P extends PixelBase> implements Spliterator<P> {

		private final Supplier<P> pixelSupplier;
		private final P px;
		/* start x coord and end x coord of a row */
		private final int startX, endXexcl;
		/* current coords of this spliterator */
		private int x,y;
		/* final coords of this spliterator */
		private int finalXexcl, finalYincl;

		private final int minimumSplitSize;

		/**
		 * Constructs a new ImgAreaSpliterator for the specified area
		 * @param xStart left boundary of the area (inclusive)
		 * @param yStart upper boundary of the area (inclusive)
		 * @param width of the area
		 * @param height of the area
		 * @param minSplitSize the minimum number of elements in a split
		 * @param pixelSupplier a function that allocates a new pixel
		 * @since 1.1
		 */
		public ImgAreaSpliterator(
				int xStart,
				int yStart,
				int width,
				int height,
				int minSplitSize,
				Supplier<P> pixelSupplier
		){
			this(xStart, xStart+width, xStart, yStart, xStart+width, yStart+height-1, minSplitSize, pixelSupplier);
		}

		private ImgAreaSpliterator(
				int xStart,
				int endXexcl,
				int x,
				int y,
				int finalXexcl,
				int finalYincl,
				int minSplitSize,
				Supplier<P> pixelSupplier
		){
			this.startX = xStart;
			this.endXexcl = endXexcl;
			this.x = x;
			this.y = y;
			this.finalXexcl = finalXexcl;
			this.finalYincl = finalYincl;
			this.pixelSupplier = pixelSupplier;
			this.px = pixelSupplier.get();
			this.px.setPosition(x, y);
			this.minimumSplitSize = minSplitSize;
		}


		@Override
		public boolean tryAdvance(final Consumer<? super P> action) {
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
		public void forEachRemaining(final Consumer<? super P> action) {
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
		public Spliterator<P> trySplit() {
			int width = (this.endXexcl-this.startX);
			int idx = this.x - this.startX;
			int finalIdx_excl = (this.finalYincl-this.y)*width + (this.finalXexcl-startX);
			int midIdx_excl = idx + (finalIdx_excl-idx)/2;
			if(midIdx_excl > idx+minimumSplitSize){
				int newFinalX_excl = startX + (midIdx_excl%width);
				int newFinalY_incl = this.y + midIdx_excl/width;
				ImgAreaSpliterator<P> split = new ImgAreaSpliterator<>(
						startX,         // start of a row
						endXexcl,       // end of a row
						newFinalX_excl, // x coord of new spliterator
						newFinalY_incl, // y coord of new spliterator
						finalXexcl,     // final x coord of new spliterator
						finalYincl,    // final y coord of new spliterator
						minimumSplitSize,
						pixelSupplier);

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
	 * Special Spliterator for images which guarantees that each split will cover at least
	 * an entire row of the image.
	 * @author hageldave
	 */
	public static final class RowSpliterator<P extends PixelBase> implements Spliterator<P> {

		private final int startX;
		private final int endXinclusive;
		private int x;
		private int y;
		private int endYinclusive;
		private final Supplier<P> pixelSupplier;
		private final P px;

		/**
		 * Creates a new RowSpliterator for iterating the pixels in the specified area.
		 * Each split is guaranteed to cover at least 1 entire row of the area.
		 * @param startX left boundary of the area (inclusive)
		 * @param width width of the area
		 * @param startY top boundary of the area (inclusive)
		 * @param endYincl bottom boundary of the area (inclusive)
		 * @param pixelSupplier a function that allocates a new pixel
		 */
		public RowSpliterator(int startX, int width, int startY, int endYincl, Supplier<P> pixelSupplier) {
			this.startX = startX;
			this.x = startX;
			this.endXinclusive = startX+width-1;
			this.y = startY;
			this.endYinclusive = endYincl;
			this.pixelSupplier = pixelSupplier;
			this.px = pixelSupplier.get();
			this.px.setPosition(x,y);
		}


		@Override
		public boolean tryAdvance(Consumer<? super P> action) {
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
		public void forEachRemaining(Consumer<? super P> action) {
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
		public Spliterator<P> trySplit() {
			if(this.y < endYinclusive){
				int newY = y + 1 + (endYinclusive-y)/2;
				RowSpliterator<P> split = new RowSpliterator<>(startX, endXinclusive-startX+1, newY, endYinclusive, pixelSupplier);
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
	 */
	public static final class ColSpliterator<P extends PixelBase> implements Spliterator<P> {

		private final int startY;
		private int endXinclusive;
		private int x;
		private int y;
		private final int endYinclusive;
		private final Supplier<P> pixelSupplier;
		private final P px;

		/**
		 * Creates a new RowSpliterator for iterating the pixels in the specified area.
		 * Each split is guaranteed to cover at least 1 entire row of the area.
		 * @param startX left boundary of the area (inclusive)
		 * @param endXincl right boundary of the area (inclusive)
		 * @param startY top boundary of the area (inclusive)
		 * @param height of the area
		 * @param pixelSupplier a function that allocates a new pixel
		 */
		public ColSpliterator(int startX, int endXincl, int startY, int height, Supplier<P> pixelSupplier) {
			this.startY = startY;
			this.y = startY;
			this.endYinclusive = startY+height-1;
			this.x = startX;
			this.endXinclusive = endXincl;
			this.pixelSupplier = pixelSupplier;
			this.px = pixelSupplier.get();
			this.px.setPosition(x, y);
		}


		@Override
		public boolean tryAdvance(Consumer<? super P> action) {
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
		public void forEachRemaining(Consumer<? super P> action) {
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
		public Spliterator<P> trySplit() {
			if(this.x < endXinclusive){
				int newX = x + 1 + (endXinclusive-x)/2;
				ColSpliterator<P> split = new ColSpliterator<>(newX, endXinclusive, startY, endYinclusive-startY+1, pixelSupplier);
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

}
