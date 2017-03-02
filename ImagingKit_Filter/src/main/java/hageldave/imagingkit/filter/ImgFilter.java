package hageldave.imagingkit.filter;

import java.util.Objects;

import hageldave.imagingkit.core.Img;

public interface ImgFilter {
	
	public default void applyTo(Img img)
		{applyTo(img, false);}

	public default void applyTo(Img img, boolean parallelPreferred)
		{applyTo(img, parallelPreferred, 0, 0, img.getWidth(), img.getHeight());}
	
	public default void applyTo(Img img, int x, int y, int width, int height)
		{applyTo(img, false, 0, 0, img.getWidth(), img.getHeight());}
	
	public void applyTo(Img img, boolean parallelPreferred, int x, int y, int width, int height);
	
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
