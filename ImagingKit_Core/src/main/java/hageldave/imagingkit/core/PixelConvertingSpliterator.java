package hageldave.imagingkit.core;

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
 * @param <T> the type of elements returned by this Spliterator
 * @since 1.4
 */
public class PixelConvertingSpliterator<T> implements Spliterator<T> {
	
	/** {@code Spliterator<Pixel>} acting as delegate of this spliterator 
	 * @since 1.4 */
	protected final Spliterator<Pixel> delegate;
	
	/** the element of this spliterator (reused on each pixel of the delegate) 
	 * @since 1.4 */
	protected final T element;
	
	/** method for allocating an element of this spliterator 
	 * @since 1.4 */
	protected final Supplier<T> elementAllocator;
	
	/** method for converting a pixel to an element of this spliterator.
	 * <br><b>NOT ALLOCATION, ONLY SETUP OF THE ELEMENT</b> 
	 * @since 1.4 */
	protected final BiConsumer<Pixel, T> fromPixelConverter;
	
	/** method for converting an element of this spliterator to a pixel 
	 * <br><b>NOT ALLOCATION, ONLY SETTING THE PIXEL VALUE ACCORDINGLY</b> 
	 * @since 1.4 */
	protected final BiConsumer<T, Pixel> toPixelConverter;
	
	
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
			Spliterator<Pixel> delegate, 
			Supplier<T> elementAllocator, 
			BiConsumer<Pixel, T> fromPixelConverter, 
			BiConsumer<T, Pixel> toPixelConverter) 
	{
		this.delegate=delegate;
		this.element = elementAllocator.get();
		this.elementAllocator = elementAllocator;
		this.fromPixelConverter = fromPixelConverter;
		this.toPixelConverter = toPixelConverter;
	}

	@Override
	public boolean tryAdvance(Consumer<? super T> action) {
		return delegate.tryAdvance( px -> {
			fromPixelConverter.accept(px, element);
			action.accept(element);
			toPixelConverter.accept(element, px);
		});
	}
	
	@Override
	public void forEachRemaining(Consumer<? super T> action) {
		delegate.forEachRemaining(px -> {
			fromPixelConverter.accept(px, element);
			action.accept(element);
			toPixelConverter.accept(element, px);
		});
	}

	@Override
	public Spliterator<T> trySplit() {
		Spliterator<Pixel> del = delegate.trySplit();
		return del == null ? null:new PixelConvertingSpliterator<T>(del, elementAllocator, fromPixelConverter, toPixelConverter);
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
	 * Example implementation of a {@code PixelConvertingSpliterator<float[]>}.
	 * <p>
	 * The elements of the returned spliterator will be {@code float[]} of length 3
	 * with normalized red, green and blue channels on index 0, 1 and 2.
	 * <p>
	 * <b>Code:</b>
	 * <pre>
	 * {@code
	 * Supplier<float[]> arrayAllocator = () -> {
	 *    return new float[3];
	 * };
	 * BiConsumer<Pixel, float[]> convertToArray = (px, array) -> {
	 *    array[0]=px.r_normalized();
	 *    array[1]=px.g_normalized();
	 *    array[2]=px.b_normalized();
	 * };
	 * BiConsumer<float[], Pixel> convertToPixel = (array, px) -> {
	 *    px.setRGB_fromNormalized_preserveAlpha(
	 *       // clamp values between zero and one
	 *       Math.min(1, Math.max(0, array[0])), 
	 *       Math.min(1, Math.max(0, array[0])), 
	 *       Math.min(1, Math.max(0, array[0])));
	 * };
	 * PixelConvertingSpliterator<float[]> arraySpliterator = new PixelConvertingSpliterator<>(
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
	public static PixelConvertingSpliterator<float[]> getFloatArrayElementSpliterator(Spliterator<Pixel> pixelSpliterator){
		Supplier<float[]> arrayAllocator = () -> {
			return new float[3];
		};
		BiConsumer<Pixel, float[]> convertToArray = (px, array) -> {
			array[0]=px.r_normalized();
			array[1]=px.g_normalized();
			array[2]=px.b_normalized();
		};
		BiConsumer<float[], Pixel> convertToPixel = (array, px) -> {
			px.setRGB_fromNormalized_preserveAlpha(
					// clamp values between zero and one
					Math.min(1, Math.max(0, array[0])), 
					Math.min(1, Math.max(0, array[0])), 
					Math.min(1, Math.max(0, array[0])));
		};
		PixelConvertingSpliterator<float[]> arraySpliterator = new PixelConvertingSpliterator<>(
				pixelSpliterator, 
				arrayAllocator, 
				convertToArray, 
				convertToPixel);
		return arraySpliterator;
	}
	
}
