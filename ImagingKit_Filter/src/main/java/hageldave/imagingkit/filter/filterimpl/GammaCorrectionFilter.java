package hageldave.imagingkit.filter.filterimpl;

import java.util.function.Consumer;

import hageldave.imagingkit.core.Pixel;
import hageldave.imagingkit.filter.PerPixelFilter;

public class GammaCorrectionFilter implements PerPixelFilter {
	
	private double gamma = 1.0;
	
	
	@Override
	public Consumer<Pixel> consumer() {
		double divByGamma = 1.0/gamma;
		return px->px.setValue(applyGamma(px.getValue(), divByGamma));
	}
	
	public static int applyGamma(int argb, double divByGamma){
		int r = (int)(255*Math.pow(Pixel.r(argb)/255.0, divByGamma));
		int g = (int)(255*Math.pow(Pixel.g(argb)/255.0, divByGamma));
		int b = (int)(255*Math.pow(Pixel.b(argb)/255.0, divByGamma));
		return Pixel.argb_fast(Pixel.a(argb), r, g, b);
	}
	
	public void setGamma(double gamma){
		if(gamma == 0.0)
			throw new IllegalArgumentException("Cannot set gamma to 0!");
		
		this.gamma = gamma;
	}
	
	public double getGamma() {
		return gamma;
	}


}
