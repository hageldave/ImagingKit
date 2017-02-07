package hageldave.imagingkit.core.spliterators;

import java.util.Spliterator;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Supplier;

import hageldave.imagingkit.core.Pixel;

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
