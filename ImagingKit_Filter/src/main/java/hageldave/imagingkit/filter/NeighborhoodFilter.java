package hageldave.imagingkit.filter;

import java.util.Objects;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

import hageldave.imagingkit.core.Img;
import hageldave.imagingkit.core.Pixel;

/**
 * The {@link NeighborhoodFilter} interface provides a default imlementation of the
 * {@link ImgFilter#applyTo(Img, boolean, int, int, int, int)} method which 
 * applies a {@code Consumer<Pixel>} to the {@link Img}. Therefore classes that 
 * implement this interface have to provide such a {@link Consumer} via the 
 * {@link #consumer(Img)} method which is the action that will be executed on
 * every {@link Pixel}. The {@link #consumer(Img)} method will be passed a copy
 * of the Img that the returned {@code Consumer<Pixel>} will be applied to.
 * <br>e.g. {@code img.forEach(consumer(img.copy()));}<br>
 * This way a Consumer using the neighborhood of the pixel it operates on can
 * access these in the copy where they are unchanged.
 * <br>e.g. {@code Consumer<Pixel> c = px->px.setValue(copy.getValue(px.getX()-1,px.getY(),0x0));}
 * 
 * @author hageldave
 *
 */
public interface NeighborhoodFilter extends ImgFilter {
	
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
	public default ImgFilter followedBy(ImgFilter nextFilter) {
		if(nextFilter instanceof NeighborhoodFilter)
			return followedBy((NeighborhoodFilter)nextFilter);
		if(nextFilter instanceof PerPixelFilter)
			return followedBy((PerPixelFilter)nextFilter);
		else
			return ImgFilter.super.followedBy(nextFilter);
	}
	
	public default NeighborhoodFilter followedBy(NeighborhoodFilter nextFilter){
		Objects.requireNonNull(nextFilter);
		return new NeighborhoodFilter() {
			
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
				NeighborhoodFilter.this.applyTo(img, copy, parallelPreferred, x, y, width, height);
				System.arraycopy(img.getData(), 0, copy.getData(), 0, img.numValues());
				nextFilter.applyTo(img, copy, parallelPreferred, x, y, width, height);
			}
		};
	}
	
	public default NeighborhoodFilter followedBy(PerPixelFilter nextFilter){
		Objects.requireNonNull(nextFilter);
		return new NeighborhoodFilter() {
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
				NeighborhoodFilter.this.applyTo(img, copy, parallelPreferred, x, y, width, height);
				nextFilter.applyTo(img, parallelPreferred, x, y, width, height);
			}
		};
	}
	
	public static NeighborhoodFilter fromConsumer(BiConsumer<Pixel, Img> consumer){
		Objects.requireNonNull(consumer);
		return copy->{return px->consumer.accept(px, copy);};
	}
}
