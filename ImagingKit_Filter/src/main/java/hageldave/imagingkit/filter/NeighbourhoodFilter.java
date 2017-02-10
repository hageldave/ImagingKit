package hageldave.imagingkit.filter;

import java.util.Objects;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

import hageldave.imagingkit.core.Img;
import hageldave.imagingkit.core.Pixel;

public interface NeighbourhoodFilter extends Filter {
	
	public Consumer<Pixel> consumer(Img copy);

	
	@Override
	default void applyTo(Img img, boolean parallelPreferred, int x, int y, int width, int height) 
		{applyTo(img, new Img(img.getDimension()), parallelPreferred, x,y,width,height);}
	
	public default void applyTo(Img img, Img copy, boolean parallelPreferred, int x, int y, int width, int height) {
		assert(img.getDimension().equals(copy.getDimension()));
		
		System.arraycopy(img.getData(), 0, copy.getData(), 0, img.numValues());
		
		if(parallelPreferred){
			if(x == 0 && y == 0 && width == img.getWidth() && height == img.getHeight())
				img.forEach(consumer(copy));
			else
				img.forEach(x, y, width, height, consumer(copy));
		} else {
			if(x == 0 && y == 0 && width == img.getWidth() && height == img.getHeight())
				img.forEachParallel(consumer(copy));
			else
				img.forEachParallel(x, y, width, height, consumer(copy));
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
	
	public default NeighbourhoodFilter followedBy(NeighbourhoodFilter nextFilter){
		Objects.requireNonNull(nextFilter);
		return new NeighbourhoodFilter() {
			
			@Override
			public Consumer<Pixel> consumer(Img copy){
				throw new UnsupportedOperationException(
						"Calls to consumer(Img) are not supported by a chained "
						+ getClass().getSimpleName() 
						+ " created from followedBy("
						+ getClass().getSimpleName()
						+")");
			}
			
			@Override
			public void applyTo(Img img, Img copy, boolean parallelPreferred, int x, int y, int width, int height) {
				NeighbourhoodFilter.this.applyTo(img, copy, parallelPreferred, x, y, width, height);
				System.arraycopy(img.getData(), 0, copy.getData(), 0, img.numValues());
				nextFilter.applyTo(img, copy, parallelPreferred, x, y, width, height);
			}
		};
	}
	
	public default NeighbourhoodFilter followedBy(PerPixelFilter nextFilter){
		Objects.requireNonNull(nextFilter);
		return new NeighbourhoodFilter() {
			@Override
			public Consumer<Pixel> consumer(Img copy){
				throw new UnsupportedOperationException(
						"Calls to consumer(Img) are not supported by a chained "
						+ getClass().getSimpleName() 
						+ " created from followedBy("
						+ PerPixelFilter.class.getSimpleName()
						+")");
			}
			
			@Override
			public void applyTo(Img img, Img copy, boolean parallelPreferred, int x, int y, int width, int height) {
				NeighbourhoodFilter.this.applyTo(img, copy, parallelPreferred, x, y, width, height);
				nextFilter.applyTo(img, parallelPreferred, x, y, width, height);
			}
		};
	}
	
	public static NeighbourhoodFilter fromConsumer(BiConsumer<Pixel, Img> consumer){
		Objects.requireNonNull(consumer);
		return copy->{return px->consumer.accept(px, copy);};
	}
}
