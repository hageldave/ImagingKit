package hageldave.imagingkit.filter.filterimpl;

import java.util.function.Consumer;

import hageldave.imagingkit.core.Pixel;
import hageldave.imagingkit.filter.PerPixelFilter;
import hageldave.imagingkit.filter.settings.FilterSettings;
import hageldave.imagingkit.filter.settings.ReadOnlyFilterSettings;
import hageldave.imagingkit.filter.settings.SettingConstraint;

public class GrayScaleFilter extends PerPixelFilter {
	public static final String RED_WEIGHT_ID = "red";
	public static final String GREEN_WEIGHT_ID = "green";
	public static final String BLUE_WEIGHT_ID = "blue";
	
	private final int[] weights = new int[]{1,1,1};
	
	public GrayScaleFilter() {
		super(null, new FilterSettings(SettingConstraint
				.createNew(RED_WEIGHT_ID).constrainType(Integer.class)
				.appendNew(GREEN_WEIGHT_ID).constrainType(Integer.class)
				.appendNew(BLUE_WEIGHT_ID).constrainType(Integer.class)
				.buildAll()
				));
	}
	
	
	@Override
	protected Consumer<Pixel> initiallyGetPerPixelAction() {
		return (px) -> 
		{
			int grey = Pixel.getGrey(px.getValue(),weights[0], weights[1], weights[2]);
			px.setValue(Pixel.argb_fast(px.a(),grey, grey, grey));
		};
	}
	
	public void setWeights(int r, int g, int b){
		getSettings().set(RED_WEIGHT_ID, r);
		getSettings().set(GREEN_WEIGHT_ID, g);
		getSettings().set(BLUE_WEIGHT_ID, b);
	}


	@Override
	protected void readSettingsBeforeApply(ReadOnlyFilterSettings settings) {
		weights[0] = settings.getAs(RED_WEIGHT_ID, Integer.class, weights[0]);
		weights[1] = settings.getAs(GREEN_WEIGHT_ID, Integer.class, weights[1]);
		weights[2] = settings.getAs(BLUE_WEIGHT_ID, Integer.class, weights[2]);
	}
}
