package hageldave.imagingkit.filter;

import java.util.Objects;

import hageldave.imagingkit.core.Img;

/**
 * The base interface for image filters.
 * It provides the <tt>applyTo</tt> methods where 
 * {@link #applyTo(Img, boolean, int, int, int, int)} has to be implemented,
 * while the others have default implementations.<br>
 * It also provides a default implementation of the chaining method
 * {@link #followedBy(ImgFilter)}.
 * 
 * @author hageldave
 */
public interface ImgFilter {
	
	/**
	 * Applies this filter to the whole specified {@link Img}.
	 * The filter will prefer executing a sequential implementation (not parallel).
	 * 
	 * @param img the filter will be applied to
	 */
	public default void applyTo(Img img)
		{applyTo(img, false);}

	/**
	 * Applies this filter to the whole specified {@link Img}.
	 * When parallelPreferred is true, a multithreaded (parallel) 
	 * implementation will be executed if possible.<br>
	 * Filters implementing this interface do not guarantee to provide a
	 * parallel implementation.
	 * 
	 * @param img the filter will be applied to
	 * @param parallelPreferred if true, the filter will be executed in parallel 
	 * if possible
	 * 
	 * @see #applyTo(Img, boolean, int, int, int, int)
	 * @see #applyTo(Img, int, int, int, int)
	 * @see #applyTo(Img)
	 */
	public default void applyTo(Img img, boolean parallelPreferred)
		{applyTo(img, parallelPreferred, 0, 0, img.getWidth(), img.getHeight());}
	
	/**
	 * Applies this filter to the specified {@link Img} within the specified
	 * area (x, y, width, height). The filter will prefer executing a sequential 
	 * implementation (not parallel).
	 * 
	 * @param img the filter will be applied to
	 * @param x starting x coordinate of the area the filter will be applied to
	 * @param y starting y coordinate of the area the filter will be applied to
	 * @param width of the area the filter will be applied to
	 * @param height of the area the filter will be applied to
	 * 
	 * @see #applyTo(Img, boolean, int, int, int, int)
	 * @see #applyTo(Img, boolean)
	 * @see #applyTo(Img)
	 */
	public default void applyTo(Img img, int x, int y, int width, int height)
		{applyTo(img, false, x, y, width, height);}
	
	/**
	 * Applies this filter to the specified {@link Img} within the specified
	 * area (x, y, width, height). When parallelPreferred is true, a 
	 * multithreaded (parallel) implementation will be executed if possible.<br>
	 * Filters implementing this interface do not guarantee to provide a
	 * parallel implementation.
	 * 
	 * @param img the filter will be applied to
	 * @param parallelPreferred if true, the filter will be executed in parallel 
	 * if possible
	 * @param x starting x coordinate of the area the filter will be applied to
	 * @param y starting y coordinate of the area the filter will be applied to
	 * @param width of the area the filter will be applied to
	 * @param height of the area the filter will be applied to
	 * 
	 * @see #applyTo(Img, int, int, int, int)
	 * @see #applyTo(Img, boolean)
	 * @see #applyTo(Img)
	 */
	/* >>>> TO BE IMPLEMENTED <<<< */
	public void applyTo(Img img, boolean parallelPreferred, int x, int y, int width, int height);
	
	/**
	 * Returns a combined ImageFilter that will apply the specified filter 
	 * right after this one.
	 * 
	 * @param nextFilter to be applied after this filter
	 * @return an ImageFilter that will execute this filter and the specified 
	 * one subsequently
	 * @throws NullPointerException if nextFilter is null.
	 */
	public default ImgFilter followedBy(ImgFilter nextFilter){
		Objects.requireNonNull(nextFilter);
		return new ImgFilter() {
			@Override
			public void applyTo(Img img, boolean parallelPreferred, int x, int y, int width, int height) {
				ImgFilter.this.applyTo(img, parallelPreferred, x, y, width, height);
				nextFilter.applyTo(img, parallelPreferred, x, y, width, height);
			}
		};
	}
	
	
}
