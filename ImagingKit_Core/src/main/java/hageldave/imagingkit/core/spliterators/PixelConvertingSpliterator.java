package hageldave.imagingkit.core.spliterators;

import java.util.Spliterator;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Supplier;

import hageldave.imagingkit.core.Img;
import hageldave.imagingkit.core.Pixel;

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
 * which has to be provided with the following functions:<br>
 * <li>
 * <u>element initializer:</u> a function to create (allocate) an object of the
 * desired datatype (Vector3D in this example)
 * </li><li>
 * <u>from Pixel converter:</u> a function to convert a Pixel object to an object
 * of the desired datatype e.g. <br>{@code void convertPixel(Pixel px, Vector3D vec)}
 * </li><li>
 * <u>to Pixel converter:</u> a function to convert an object of the desired 
 * datatype to a Pixel object e.g. <br>{@code void convertVector(Vector3D vec, Pixel px)}
 * </li><br>
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
 * 
 * @author hageldave
 *
 * @param <T>
 */
public class PixelConvertingSpliterator<T> implements Spliterator<T> {
	
	protected final Spliterator<Pixel> delegate;
	protected final T element;
	protected final Supplier<T> elementInitializer;
	protected final BiConsumer<Pixel, T> fromPixelConverter;
	protected final BiConsumer<T, Pixel> toPixelConverter;
	
	public PixelConvertingSpliterator(
			Spliterator<Pixel> delegate, 
			Supplier<T> elementInitializer, 
			BiConsumer<Pixel, T> fromPixelConverter, 
			BiConsumer<T, Pixel> toPixelConverter) 
	{
		this.delegate=delegate;
		this.element = elementInitializer.get();
		this.elementInitializer = elementInitializer;
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
		return del == null ? null:new PixelConvertingSpliterator<T>(del, elementInitializer, fromPixelConverter, toPixelConverter);
	}

	@Override
	public long estimateSize() {
		return delegate.estimateSize();
	}

	@Override
	public int characteristics() {
		return delegate.characteristics();
	}
	
}
