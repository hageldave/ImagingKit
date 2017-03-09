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
	default void applyTo(Img img, boolean parallelPreferred, int x, int y, int width, int height) 
		{applyTo(img, new Img(img.getDimension()), parallelPreferred, x,y,width,height);}
	
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
	public default void applyTo(Img img, Img copy, boolean parallelPreferred, int x, int y, int width, int height) {
		assert(img.getDimension().equals(copy.getDimension()));
		
		System.arraycopy(img.getData(), 0, copy.getData(), 0, img.numValues());
		
		if(parallelPreferred){
			if(x == 0 && y == 0 && width == img.getWidth() && height == img.getHeight())
				img.forEachParallel(consumer(copy));
			else
				img.forEachParallel(x, y, width, height, consumer(copy));
		} else {
			if(x == 0 && y == 0 && width == img.getWidth() && height == img.getHeight())
				img.forEach(consumer(copy));
			else
				img.forEach(x, y, width, height, consumer(copy));
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
