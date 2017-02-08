package hageldave.imagingkit.filter;

import java.util.function.Consumer;

import hageldave.imagingkit.core.Img;
import hageldave.imagingkit.core.Pixel;

public interface NeighbourhoodFilter extends Filter {
	
	public void accept(Pixel px, Img copy);
	
	public default Consumer<Pixel> consumer(Img copy)
		{return px->this.accept(px, copy);}

	
	@Override
	default void applyTo(Img img, boolean parallelPreferred, int x, int y, int width, int height) 
		{applyTo(img, new Img(img.getDimension()), parallelPreferred, x,y,width,height);}
	
	public default void applyTo(Img img, Img copy, boolean parallelPreferred, int x, int y, int width, int height) {
		System.out.println("NeighbourhoodFilter.applyTo()");
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
	
	public default NeighbourhoodFilter followedBy(NeighbourhoodFilter nextFilter){
		return new NeighbourhoodFilter() {
			
			@Override
			public void accept(Pixel px, Img copy) {
				throw new UnsupportedOperationException(
						"Calls to accept(Pixel,Img) are not supported by a chained "
						+ getClass().getSimpleName() 
						+ " created from followedBy("
						+ getClass().getSimpleName()
						+")");
			}
			
			@Override
			public void applyTo(Img img, Img copy, boolean parallelPreferred, int x, int y, int width, int height) {
				System.out.println("NeighbourhoodFilter.followedBy(...).new NeighbourhoodFilter() {...}.applyTo()");
				NeighbourhoodFilter.this.applyTo(img, copy, parallelPreferred, x, y, width, height);
				System.arraycopy(img.getData(), 0, copy.getData(), 0, img.numValues());
				nextFilter.applyTo(img, copy, parallelPreferred, x, y, width, height);
			}
		};
	}
	
	public default NeighbourhoodFilter followedBy(PerPixelFilter nextFilter){
		return new NeighbourhoodFilter() {
			
			@Override
			public void accept(Pixel px, Img copy) {
				throw new UnsupportedOperationException(
						"Calls to accept(Pixel,Img) are not supported by a chained "
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
}
