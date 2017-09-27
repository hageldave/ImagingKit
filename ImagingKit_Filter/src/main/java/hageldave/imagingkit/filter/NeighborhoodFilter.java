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
 * of the Img that the returned {@code Consumer<Pixel>} will be applied to. e.g. 
 * <pre>{@code 
 * img.forEach(consumer(img.copy()));
 * }</pre>
 * This way a Consumer using the neighborhood of the pixel it operates on can
 * access these in the copy where they are unchanged. e.g. 
 * <pre>{@code 
 * Consumer<Pixel> shiftRight = px->{
 *    int x = px.getX()-1;
 *    px.setValue(copy.getValue(x,px.getY(),Img.boundary_mode_repeat_image));
 * };
 * }</pre>
 * 
 * 
 * @author hageldave
 *
 */
public interface NeighborhoodFilter extends ImgFilter {
	
	/**
	 * Returns the action to be performed on an individual pixel.
	 * A copy of the {@link Img} the pixel belongs to is passed as argument
	 * for accessing neighboring pixels in their unchanged state during
	 * the application of the returned consumer.
	 * 
	 * @param copy of the image the returned consumer is applied to
	 * @return the {@link Consumer} that will be applied to a {@link Pixel}
	 */
	/* >>>> TO BE IMPLEMENTED <<<< */
	public Consumer<Pixel> consumer(Img copy);

	
	@Override
	default void applyTo(Img img, boolean parallelPreferred, int x, int y, int width, int height, ProgressListener progress) 
		{applyTo(img, new Img(img.getDimension()), parallelPreferred, x,y,width,height, progress);}
	
	/**
	 * Applies this filter to the specified {@link Img} (first argument) within 
	 * the specified area (x, y, width, height). When parallelPreferred is true, 
	 * a multithreaded (parallel) implementation will be executed leveraging
	 * the {@link Img#forEachParallel(Consumer)} method.<br>
	 * The specified {@link Img} copy (second argument) will be used to provide
	 * the {@link #consumer(Img)} with an unchanging version of the image that
	 * it is applied to.
	 * 
	 * @param img the filter will be applied to
	 * @param copy Img of the same dimensions as the img argument (img content
	 * will be copied to it)
	 * @param parallelPreferred if true, the filter will be executed in parallel 
	 * if possible
	 * @param x starting x coordinate of the area the filter will be applied to
	 * @param y starting y coordinate of the area the filter will be applied to
	 * @param width of the area the filter will be applied to
	 * @param height of the area the filter will be applied to
	 */
	public default void applyTo(Img img, Img copy, boolean parallelPreferred, int x, int y, int width, int height, ProgressListener progress) {
		assert(img.getDimension().equals(copy.getDimension()));
		
		Consumer<Pixel> consumer = consumer(copy);
		
		if(progress != null){
			progress.pushPendingFilter(this, height);
			consumer = consumer.andThen(PerPixelFilter.getScanlineProgressNotificationConsumer(progress, this, height, x+height-1));
		}
		
		System.arraycopy(img.getData(), 0, copy.getData(), 0, img.numValues());
		
		if(parallelPreferred){
			if(x == 0 && y == 0 && width == img.getWidth() && height == img.getHeight())
				img.forEachParallel(consumer);
			else
				img.forEachParallel(x, y, width, height, consumer);
		} else {
			if(x == 0 && y == 0 && width == img.getWidth() && height == img.getHeight())
				img.forEach(consumer);
			else
				img.forEach(x, y, width, height, consumer);
		}
		
		if(progress != null) {
			progress.popFinishedFilter(this);
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
	 * this {@link NeighborhoodFilter} with another NeighborhoodFilter. The
	 * Result of this concatenation will be a NeighborhoodFilter that will
	 * apply this NeighborhoodFilter first and the specified NeighborhoodFilter
	 * subsequently.
	 * <p>
	 * Using this method has an advantage over applying two NeighborhoodFilters
	 * 'manually' (e.g. calling applyTo subsequently:
	 * {@code nbFilter1.applyTo(img); nbFilter2.applyTo(img);}).
	 * The advantage is that memory for the image copy is only allocated once
	 * and reused on each subsequent NeighborhoodFilter, where as multiple calls
	 * to applyTo() need to allocate a new copy each time.
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
			public void applyTo(Img img, Img copy, boolean parallelPreferred, int x, int y, int width, int height, ProgressListener progress) {
				if(progress != null) {
					progress.pushPendingFilter(this, 2);
				}
				NeighborhoodFilter.this.applyTo(img, copy, parallelPreferred, x, y, width, height, progress);
				if(progress != null) {
					progress.notifyFilterProgress(this, 2, 1);
				}
				System.arraycopy(img.getData(), 0, copy.getData(), 0, img.numValues());
				nextFilter.applyTo(img, copy, parallelPreferred, x, y, width, height, progress);
				if(progress != null) {
					progress.notifyFilterProgress(this, 2, 1);
					progress.popFinishedFilter(this);
				}
			}
		};
	}
	
	/**
	 * Specialized <tt>followedBy()</tt> implementation for concatenating
	 * this {@link NeighborhoodFilter} with a {@link PerPixelFilter}. The
	 * Result of this concatenation will be a NeighborhoodFilter that will
	 * apply this NeighborhoodFilter first and the specified PerPixelFilter
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
			public void applyTo(Img img, Img copy, boolean parallelPreferred, int x, int y, int width, int height, ProgressListener progress) {
				if(progress != null) {
					progress.pushPendingFilter(this, 2);
				}
				NeighborhoodFilter.this.applyTo(img, copy, parallelPreferred, x, y, width, height, progress);
				if(progress != null) {
					progress.notifyFilterProgress(this, 2, 1);
				}
				nextFilter.applyTo(img, parallelPreferred, x, y, width, height, progress);
				if(progress != null) {
					progress.notifyFilterProgress(this, 2, 1);
					progress.popFinishedFilter(this);
				}
			}
		};
	}
	
	/**
	 * Returns a {@link NeighborhoodFilter} that utilizes the specified 
	 * {@code BiConsumer<Pixel, Img>}. This is simply a convenience method
	 * for the following code <br> 
	 * {@code NeighborhoodFilter nbf = (copy) -> {return px->biconsumer.accept(px,copy);};}
	 * 
	 * @param pixelNeighborhoodAction to be performed on a Pixel by the returned filter.
	 * The Img argument of the {@link BiConsumer} will be a copy of Img the Pixel 
	 * belongs to, enabling the BiConsumer to access the unchanged neighboring pixels.
	 * @return a PerPixelFilter that applies the specified {@link BiConsumer} 
	 * to a {@link Pixel}
	 * @throws NullPointerException if pixelNeighborhoodAction is null
	 */
	public static NeighborhoodFilter fromConsumer(BiConsumer<Pixel, Img> pixelNeighborhoodAction){
		Objects.requireNonNull(pixelNeighborhoodAction);
		return copy->{return px->pixelNeighborhoodAction.accept(px, copy);};
	}
}
