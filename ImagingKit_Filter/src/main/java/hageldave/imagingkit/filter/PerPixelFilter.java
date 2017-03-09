package hageldave.imagingkit.filter;

import java.util.Objects;
import java.util.function.Consumer;

import hageldave.imagingkit.core.Img;
import hageldave.imagingkit.core.Pixel;

/**
 * The {@link PerPixelFilter} interface provides a default imlementation of the
 * {@link ImgFilter#applyTo(Img, boolean, int, int, int, int)} method which 
 * applies a {@code Consumer<Pixel>} to the {@link Img}. Therefore classes that 
 * implement this interface have to provide such a {@link Consumer} via the 
 * {@link #consumer()} method which is the action that will be executed on
 * every {@link Pixel}.
 * 
 * @author hageldave
 *
 */
public interface PerPixelFilter extends ImgFilter {

	/**
	 * Returns the per pixel action that is defined by this filter.
	 * The {@code Consumer<Pixel>} will be used in the <tt>applyTo()</tt>
	 * methods.
	 * 
	 * @return the {@link Consumer} that will be applied to a {@link Pixel}
	 */
	/* >>>> TO BE IMPLEMENTED <<<< */
	public Consumer<Pixel> consumer();

	@Override
	public default void applyTo(Img img, boolean parallelPreferred, int x, int y, int width, int height) {
		if(parallelPreferred){
			if(x == 0 && y == 0 && width == img.getWidth() && height == img.getHeight())
				img.forEachParallel(consumer());
			else
				img.forEachParallel(x, y, width, height, consumer());
		} else {
			if(x == 0 && y == 0 && width == img.getWidth() && height == img.getHeight())
				img.forEach(consumer());
			else
				img.forEach(x, y, width, height, consumer());
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
	
	/**
	 * Specialized <tt>followedBy()</tt> implementation for concatenating
	 * this {@link PerPixelFilter} with another PerPixelFilter. The
	 * Returned {@link PerPixelFilter} will use a concatenated {@link Consumer}
	 * build from <br><tt>this.consumer().andThen(next.consumer())</tt>.
	 * 
	 * @param nextFilter to be applied after this filter
	 * @return a PerPixelFilter that will execute this filter and the specified 
	 * one subsequently
	 * @throws NullPointerException if nextFilter is null.
	 */
	public default PerPixelFilter followedBy(PerPixelFilter nextFilter) {
		Objects.requireNonNull(nextFilter);
		return () -> this.consumer().andThen(nextFilter.consumer())::accept;
	}
	
	/**
	 * Specialized <tt>followedBy()</tt> implementation for concatenating
	 * this {@link PerPixelFilter} with a {@link NeighborhoodFilter}. The
	 * Result of this concatenation will be a NeighborhoodFilter that will
	 * apply this PerPixelFilter first and the specified NeighborhoodFilter
	 * subsequently.
	 * <p><b>
	 * Note that calls to {@link NeighborhoodFilter#consumer(Img)} of the
	 * returned filter will throw an {@link UnsupportedOperationException} as
	 * its {@link NeighborhoodFilter#applyTo(Img, Img, boolean, int, int, int, int)}
	 * method no longer relies on that method.
	 * 
	 * @param nextFilter to be applied after this filter
	 * @return a NeighborhoodFilter that will execute this filter and the 
	 * specified one subsequently
	 * @throws NullPointerException if nextFilter is null.
	 */
	public default NeighborhoodFilter followedBy(NeighborhoodFilter nextFilter) {
		Objects.requireNonNull(nextFilter);
		return new NeighborhoodFilter() {
			@Override
			public Consumer<Pixel> consumer(Img copy) {
				throw new UnsupportedOperationException(
						"Calls to consumer(Img) are not supported by a chained "
						+ NeighborhoodFilter.class.getSimpleName() 
						+ " created from followedBy("
						+ NeighborhoodFilter.class.getSimpleName()
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
	
	/**
	 * Returns a {@link PerPixelFilter} that utilizes the specified 
	 * {@code Consumer<Pixel>}. This is simply a convenience method
	 * for the following code <br> 
	 * {@code PerPixelFilter ppf = () -> consumer::accept;}
	 * 
	 * @param perPixelAction to be performed on a Pixel by the returned filter
	 * @return a PerPixelFilter that applies the specified {@link Consumer} 
	 * to a {@link Pixel}
	 * @throws NullPointerException if perPixelAction is null
	 */
	public static PerPixelFilter fromPixelConsumer(Consumer<Pixel> perPixelAction) {
		Objects.requireNonNull(perPixelAction);
		return ()->perPixelAction::accept;
	}
	
}
