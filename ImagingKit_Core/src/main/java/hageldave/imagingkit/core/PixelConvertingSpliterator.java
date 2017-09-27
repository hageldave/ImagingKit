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

import static hageldave.imagingkit.core.util.ImagingKitUtils.clamp_0_1;

import java.util.Objects;
import java.util.Spliterator;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Supplier;
import java.util.stream.Stream;

/**
 * The PixelConvertingSpliterator enables iterating an {@link Img} with a
 * different datatype than {@link Pixel} ({@link Img#spliterator()}). This
 * can come in handy if a {@link Consumer} would be easier to write when not
 * restricted to the Pixel datatype.
 * <p>
 * For example, a specific operation should be applied to each pixel of the Img
 * which is specified as a function that takes a 3-dimensional vector as
 * argument with red, green and blue component e.g.
 * <br><tt>applyOperation(Vector3D vec){...}}</tt><br>
 * To obtain a spliterator of the Img that will accept a {@code Consumer<Vector3D>}
 * a PixelConvertingSpliterator can be created from an ordinary {@code Spliterator<Pixel>}
 * providing the following functions:<br>
 * <ul><li>
 * <u>element allocator:</u> a function to create (allocate) an object of the
 * desired datatype (Vector3D in this example)
 * </li><li>
 * <u>from Pixel converter:</u> a function to convert a Pixel object to an object
 * of the desired datatype e.g. <br>{@code void convertPixel(Pixel px, Vector3D vec)}
 * </li><li>
 * <u>to Pixel converter:</u> a function to convert an object of the desired
 * datatype to a Pixel object e.g. <br>{@code void convertVector(Vector3D vec, Pixel px)}
 * </li></ul><br>
 * A {@code PixelConvertingSpliterator<Vector3D>} would then be created like this:
 * <pre>
 * {@code
 * Img img=loadImgFromSomewhere();
 * Spliterator<Vector3D> split = new PixelConvertingSpliterator<>(
 *       img.spliterator(),
 *       ()->{return new Vector3D();},
 *       MyConverter::convertPixel,
 *       MyConverter::convertVector
 *       );
 * StreamSupport.stream(split, true)
 *       .forEach(VectorOperations::applyOperation);
 * }</pre>
 * <p>
 * The reason for not simply doing an 'on the fly' conversion inside a {@code Spliterator<Pixel>}
 * or using the {@link Stream#map(java.util.function.Function)} function is,
 * that these methods are prone to excessive object allocation (allocating a
 * new object for every pixel). When using the PixelConvertingSpliterator there
 * is only one object allocation per split, and the object will be reused for
 * each pixel within that split.
 *
 * @author hageldave
 * @param <P> the pixel type of the underlying Spliterator
 * @param <T> the type of elements returned by the PixelConvertingSpliterator
 * @since 1.4
 */
public class PixelConvertingSpliterator<P extends PixelBase, T> implements Spliterator<T> {

	/** {@code Spliterator<Pixel>} acting as delegate of this spliterator
	 * @since 1.4 */
	protected final Spliterator<? extends P> delegate;

	/** the element of this spliterator (reused on each pixel of the delegate)
	 * @since 1.4 */
	protected final T element;


	protected PixelConverter<P,T> converter;


	/**
	 * Constructs a new PixelConvertingSpliterator.
	 *
	 * @param delegate the {@code Spliterator<Pixel>} this spliterator delegates to.
	 * @param elementAllocator method for allocating an object of this spliterator's
	 * element type.
	 * @param fromPixelConverter method for setting up an element of this spliterator
	 * according to its underlying pixel.
	 * @param toPixelConverter method for adopting an underlying pixel value
	 * according to an element of this spliterator.
	 *
	 * @since 1.4
	 */
	public PixelConvertingSpliterator(
			Spliterator<? extends P> delegate,
			Supplier<T> elementAllocator,
			BiConsumer<P, T> fromPixelConverter,
			BiConsumer<T, P> toPixelConverter)
	{
		this(delegate, PixelConverter.fromFunctions(
				elementAllocator,
				fromPixelConverter,
				toPixelConverter)
			);
	}

	public PixelConvertingSpliterator(Spliterator<? extends P> delegate, PixelConverter<P,T> converter) {
		this.converter = converter;
		this.delegate=delegate;
		this.element = converter.allocateElement();
	}

	@Override
	public boolean tryAdvance(Consumer<? super T> action) {
		return delegate.tryAdvance( px -> {
			converter.convertPixelToElement(px, element);
			action.accept(element);
			converter.convertElementToPixel(element, px);
		});
	}

	@Override
	public void forEachRemaining(Consumer<? super T> action) {
		delegate.forEachRemaining(px -> {
			converter.convertPixelToElement(px, element);
			action.accept(element);
			converter.convertElementToPixel(element, px);
		});
	}

	@Override
	public Spliterator<T> trySplit() {
		Spliterator<? extends P> del = delegate.trySplit();
		return del == null ? null:new PixelConvertingSpliterator<P,T>(del,converter);
	}

	@Override
	public long estimateSize() {
		return delegate.estimateSize();
	}

	@Override
	public int characteristics() {
		return delegate.characteristics();
	}

	/**
	 * Example implementation of a {@code PixelConvertingSpliterator<double[]>}.
	 * <p>
	 * The elements of the returned spliterator will be {@code double[]} of length 3
	 * with normalized red, green and blue channels on index 0, 1 and 2.
	 * <p>
	 * <b>Code:</b>
	 * <pre>
	 * {@code
	 * Supplier<double[]> arrayAllocator = () -> {
	 *    return new double[3];
	 * };
	 * BiConsumer<Pixel, double[]> convertToArray = (px, array) -> {
	 *    array[0]=px.r_normalized();
	 *    array[1]=px.g_normalized();
	 *    array[2]=px.b_normalized();
	 * };
	 * BiConsumer<double[], Pixel> convertToPixel = (array, px) -> {
	 *    px.setRGB_fromNormalized_preserveAlpha(
	 *       // clamp values between zero and one
	 *       Math.min(1, Math.max(0, array[0])),
	 *       Math.min(1, Math.max(0, array[1])),
	 *       Math.min(1, Math.max(0, array[2])));
	 * };
	 * PixelConvertingSpliterator<double[]> arraySpliterator = new PixelConvertingSpliterator<>(
	 *    pixelSpliterator,
	 *    arrayAllocator,
	 *    convertToArray,
	 *    convertToPixel);
	 * }
	 * </pre>
	 * @param pixelSpliterator the {@code Spliterator<Pixel>} to which the
	 * returned spliterator delegates to.
	 * @return a spliterator with float[] elements consisting of normalized RGB channels.
	 *
	 * @since 1.4
	 */
	public static PixelConvertingSpliterator<PixelBase, double[]> getDoubletArrayElementSpliterator(
			Spliterator<? extends PixelBase> pixelSpliterator){
		PixelConvertingSpliterator<PixelBase, double[]> arraySpliterator = new PixelConvertingSpliterator<>(
				pixelSpliterator, getDoubleArrayConverter());
		return arraySpliterator;
	}

	/**
	 * @return Exemplary PixelConverter that converts to double[].
	 */
	public static PixelConverter<PixelBase, double[]> getDoubleArrayConverter(){
		return new PixelConverter<PixelBase, double[]>() {

			@Override
			public void convertPixelToElement(PixelBase px, double[] array) {
				array[0]=px.r_asDouble();
				array[1]=px.g_asDouble();
				array[2]=px.b_asDouble();
			}

			@Override
			public void convertElementToPixel(double[] array, PixelBase px) {
				px.setRGB_fromDouble_preserveAlpha(
						// clamp values between zero and one
						clamp_0_1(array[0]),
						clamp_0_1(array[1]),
						clamp_0_1(array[2]));
			}

			@Override
			public double[] allocateElement() {
				return new double[3];
			}
		};
	}



	public static interface PixelConverter<P extends PixelBase, T> {
		/**
		 * Allocates a new element for the PixelConvertingSpliterator
		 * (will be called once per split)
		 * @return element, probably in uninitialized state
		 */
		public T allocateElement();
		
		/**
		 * converts the specified pixel to the specified element
		 * (initiliazation of previously allocated element).
		 * <br><b>NOT ALLOCATION, ONLY SETUP OF THE ELEMENT</b>
		 * @param px to used for setting up the element
		 * @param element to be set up
		 */
		public void convertPixelToElement(P px, T element);
		
		/**
		 * converts the specified element back to the specified pixel
		 * (set pixel value according to element).
		 * <br><b>NOT ALLOCATION, ONLY SETTING THE PIXEL VALUE ACCORDINGLY</b>
		 * @param element to be used for setting the pixel value
		 * @param px to be set
		 */
		public void convertElementToPixel(T element, P px);

		/**
		 * Creates a new PixelConverter from the specified functions.
		 * @param allocator a supplier that allocates a new object of the element type (see {@link #allocateElement()})
		 * @param pixelToElement a consumer that sets the contents of the element according to a pixel (see {@link #convertPixelToElement(PixelBase, Object)})
		 * @param elementToPixel a consumer that sets the content of a pixel according to the element (see {@link #convertElementToPixel(Object, PixelBase)})
		 * @return a new PixelConverter
		 */
		public static <P extends PixelBase, T> PixelConverter<P,T> fromFunctions(
				Supplier<T> allocator,
				BiConsumer<P,T> pixelToElement,
				BiConsumer<T,P> elementToPixel)
		{
			Objects.requireNonNull(allocator);
			BiConsumer<P,T> px_2_el = pixelToElement==null ? (px,e)->{}:pixelToElement;
			BiConsumer<T,P> el_2_px = elementToPixel==null ? (e,px)->{}:elementToPixel;

			return new PixelConverter<P,T>(){
				@Override
				public T allocateElement() {return allocator.get();}
				@Override
				public void convertPixelToElement(P px, T element) {px_2_el.accept(px, element);}
				@Override
				public void convertElementToPixel(T element, P px) {el_2_px.accept(element, px);}

			};
		}
	}

}
