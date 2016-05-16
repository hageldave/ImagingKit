package hageldave.imagingkit.filter.filterimpl;

import java.util.function.Consumer;

import hageldave.imagingkit.core.Pixel;
import hageldave.imagingkit.filter.PerPixelFilter;
import hageldave.imagingkit.filter.settings.FilterSettings;
import hageldave.imagingkit.filter.settings.ReadOnlyFilterSettings;
import hageldave.imagingkit.filter.settings.SettingConstraint;
import hageldave.imagingkit.filter.settings.ValueRange;

public class GammaCorrectionFilter extends PerPixelFilter {
	
	public static final String GAMMA_ID = "gamma";
	private double gamma = 1.0;
	
	public GammaCorrectionFilter() {
		super(null, new FilterSettings(SettingConstraint
				.createNew(GAMMA_ID)
				.constrainValue(ValueRange.fromNumber(0.0, 1500.0, true, false, Double.class))
				.constrainType(Double.class)
				.buildAll()));
	}
	
	@Override
	protected Consumer<Pixel> initiallyGetPerPixelAction() {
		return (px)->
		{
			px.setValue(applyGamma(px.getValue(), gamma));
		};
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

	@Override
	protected void readSettingsBeforeApply(ReadOnlyFilterSettings settings) {
		this.gamma = settings.getAs(GAMMA_ID, Double.class, this.gamma);
	}


}
