package hageldave.imagingkit.filter;

import java.util.function.Consumer;

import hageldave.imagingkit.core.Img;
import hageldave.imagingkit.core.Pixel;

public interface PerPixelFilter extends Filter {

	
	public void accept(Pixel px);
	
	public default Consumer<Pixel> consumer()
		{return px->this.accept(px);}

	@Override
	public default void applyTo(Img img, boolean parallelPreferred, int x, int y, int width, int height) {
		System.out.println("PerPixelFilter.applyTo()");
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
	
	public default PerPixelFilter followedBy(PerPixelFilter nextFilter) {
		return px -> {PerPixelFilter.this.accept(px); nextFilter.accept(px);};
	}
	
	public default NeighbourhoodFilter followedBy(NeighbourhoodFilter nextFilter) {
		return new NeighbourhoodFilter() {
			@Override
			public void accept(Pixel px, Img copy) {
				throw new UnsupportedOperationException(
						"Calls to accept(Pixel,Img) are not supported by a chained "
						+ NeighbourhoodFilter.class.getSimpleName() 
						+ " created from followedBy("
						+ NeighbourhoodFilter.class.getSimpleName()
						+")");
			}
			
			@Override
			public void applyTo(Img img, Img copy, boolean parallelPreferred, int x, int y, int width, int height) {
				System.out.println("PerPixelFilter.followedBy(...).new NeighbourhoodFilter() {...}.applyTo()");
				PerPixelFilter.this.applyTo(img, parallelPreferred, x, y, width, height);
				System.arraycopy(img.getData(), 0, copy.getData(), 0, img.numValues());
				nextFilter.applyTo(img, copy, parallelPreferred, x, y, width, height);
			}
		};
	}
	
	
	public static PerPixelFilter fromPixelConsumer(Consumer<Pixel> perPixelAction) {
		return px -> perPixelAction.accept(px);
	}
	
}
