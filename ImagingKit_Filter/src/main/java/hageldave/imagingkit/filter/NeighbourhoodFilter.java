package hageldave.imagingkit.filter;

import java.util.function.BiConsumer;
import java.util.function.Consumer;

import hageldave.imagingkit.core.Img;
import hageldave.imagingkit.core.Pixel;

public interface NeighbourhoodFilter extends Filter, BiConsumer<Pixel, Img> {
	
	@Override
	public void accept(Pixel px, Img copy);
	
	public default Consumer<Pixel> accept(Img copy)
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
				img.forEach(accept(copy));
			else
				img.forEach(x, y, width, height, accept(copy));
		} else {
			if(x == 0 && y == 0 && width == img.getWidth() && height == img.getHeight())
				img.forEachParallel(accept(copy));
			else
				img.forEachParallel(x, y, width, height, accept(copy));
		}
	}
	
	public default NeighbourhoodFilter followedBy(NeighbourhoodFilter nextFilter){
		return new NeighbourhoodFilter() {
			@Override
			public void accept(Pixel px, Img copy) {
				NeighbourhoodFilter.this.accept(px, copy);
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
				NeighbourhoodFilter.this.accept(px,copy);
				nextFilter.accept(px);
			}
			
			@Override
			public void applyTo(Img img, Img copy, boolean parallelPreferred, int x, int y, int width, int height) {
				NeighbourhoodFilter.this.applyTo(img, copy, parallelPreferred, x, y, width, height);
			}
		};
	}
	
	// TODO: use protected static applyTo(biconsumer,img,copy,parallel,...) method for following
}
