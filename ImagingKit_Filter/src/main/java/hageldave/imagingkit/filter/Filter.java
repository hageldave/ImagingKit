package hageldave.imagingkit.filter;

import hageldave.imagingkit.core.Img;

public interface Filter {
	
	public default void applyTo(Img img)
		{applyTo(img, false);}

	public default void applyTo(Img img, boolean parallelPreferred)
		{applyTo(img, parallelPreferred, 0, 0, img.getWidth(), img.getHeight());}
	
	public default void applyTo(Img img, int x, int y, int width, int height)
		{applyTo(img, false, 0, 0, img.getWidth(), img.getHeight());}
	
	public void applyTo(Img img, boolean parallelPreferred, int x, int y, int width, int height);
	
	
}
