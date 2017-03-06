package hageldave.imagingkit.filter.implementations;

import java.util.function.Consumer;

import hageldave.imagingkit.core.Pixel;
import hageldave.imagingkit.filter.PerPixelFilter;

public class GrayScaleFilter implements PerPixelFilter {

	private int redWeight = 1;
	private int greenWeight = 1;
	private int blueWeight = 1;
	
	public int getRedWeight() {
		return redWeight;
	}
	
	public int getGreenWeight() {
		return greenWeight;
	}
	
	public int getBlueWeight() {
		return blueWeight;
	}
	
	public void setWeights(int r, int g, int b) {
		if(r+g+b == 0)
			throw new IllegalArgumentException(String.format("Cannot use weights that sum up to 0! (%d, %d, %d)",r,g,b));
		
		redWeight = r;
		greenWeight = g;
		blueWeight = b;
	}

	@Override
	public Consumer<Pixel> consumer() {
		int r=redWeight, g=greenWeight, b=blueWeight;
		return px->{
			int grey = px.getGrey(r, g, b);
			px.setRGB_preserveAlpha(grey, grey, grey);
		};
	}
	
}
