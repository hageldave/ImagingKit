package hageldave.imagingkit.filter.filterimpl;

import hageldave.imagingkit.core.Pixel;
import hageldave.imagingkit.filter.PerPixelFilter;
import hageldave.imagingkit.filter.settings.FilterSettings;
import hageldave.imagingkit.filter.settings.ReadOnlyFilterSettings;
import hageldave.imagingkit.filter.settings.SettingConstraint;
import hageldave.imagingkit.filter.settings.ValueRange;

public class GammaCorrectionFilter extends PerPixelFilter {
	
	public static final String GAMMA_ID = "gamma";
	private Double[] gamma = new Double[]{1.0};
	
	public GammaCorrectionFilter() {
		super((px,gamma)->
		{
			px.setValue(applyGamma(px.getValue(), (double)gamma[0]));
		}, new FilterSettings(new SettingConstraint[]{new SettingConstraint(
				GAMMA_ID, ValueRange.fromNumber(0.0, (double)1500, true, false, Double.class), Double.class)}));
	}

	@Override
	protected Object[] getConfiguration(ReadOnlyFilterSettings settings) {
		gamma[0] = settings.getAs(GAMMA_ID, Double.class, 1.0);
		return gamma;
	}
	
	public static int applyGamma(int argb, double gamma){
		double divByGamma = 1.0/gamma;
		int r = (int)(255*Math.pow(Pixel.r(argb)/255.0, divByGamma));
		int g = (int)(255*Math.pow(Pixel.g(argb)/255.0, divByGamma));
		int b = (int)(255*Math.pow(Pixel.b(argb)/255.0, divByGamma));
		return Pixel.argb_fast(Pixel.a(argb), r, g, b);
	}
	
	public void setGamma(double gamma){
		getSettings().set(GAMMA_ID, gamma);
	}

}
