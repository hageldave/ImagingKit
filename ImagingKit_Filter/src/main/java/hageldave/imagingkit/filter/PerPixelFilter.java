package hageldave.imagingkit.filter;

import java.util.Objects;
import java.util.function.Consumer;

import hageldave.imagingkit.core.Img;
import hageldave.imagingkit.core.Pixel;

public interface PerPixelFilter extends Filter {

	
	public Consumer<Pixel> consumer();

	@Override
	public default void applyTo(Img img, boolean parallelPreferred, int x, int y, int width, int height) {
		if(parallelPreferred){
			if(x == 0 && y == 0 && width == img.getWidth() && height == img.getHeight())
				img.forEach(consumer());
			else
				img.forEach(x, y, width, height, consumer());
		} else {
			if(x == 0 && y == 0 && width == img.getWidth() && height == img.getHeight())
				img.forEachParallel(consumer());
			else
				img.forEachParallel(x, y, width, height, consumer());
		}
	}
	
	@Override
	public default Filter followedBy(Filter nextFilter) {
		if(nextFilter instanceof NeighbourhoodFilter)
			return followedBy((NeighbourhoodFilter)nextFilter);
		if(nextFilter instanceof PerPixelFilter)
			return followedBy((PerPixelFilter)nextFilter);
		else
			return Filter.super.followedBy(nextFilter);
	}
	
	public default PerPixelFilter followedBy(PerPixelFilter nextFilter) {
		Objects.requireNonNull(nextFilter);
		return () -> this.consumer().andThen(nextFilter.consumer())::accept;
	}
	
	public default NeighbourhoodFilter followedBy(NeighbourhoodFilter nextFilter) {
		Objects.requireNonNull(nextFilter);
		return new NeighbourhoodFilter() {
			@Override
			public Consumer<Pixel> consumer(Img copy) {
				throw new UnsupportedOperationException(
						"Calls to consumer(Img) are not supported by a chained "
						+ NeighbourhoodFilter.class.getSimpleName() 
						+ " created from followedBy("
						+ NeighbourhoodFilter.class.getSimpleName()
						+")");
			}
			
			@Override
			public void applyTo(Img img, Img copy, boolean parallelPreferred, int x, int y, int width, int height) {
				PerPixelFilter.this.applyTo(img, parallelPreferred, x, y, width, height);
				System.arraycopy(img.getData(), 0, copy.getData(), 0, img.numValues());
				nextFilter.applyTo(img, copy, parallelPreferred, x, y, width, height);
			}
		};
	}
	
	
	public static PerPixelFilter fromPixelConsumer(Consumer<Pixel> perPixelAction) {
		Objects.requireNonNull(perPixelAction);
		return ()->perPixelAction::accept;
	}
	
}
