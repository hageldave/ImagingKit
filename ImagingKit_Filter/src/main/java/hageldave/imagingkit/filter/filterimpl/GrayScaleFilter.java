package hageldave.imagingkit.filter.filterimpl;

import hageldave.imagingkit.core.Pixel;
import hageldave.imagingkit.filter.PerPixelFilter;
import hageldave.imagingkit.filter.settings.FilterSettings;
import hageldave.imagingkit.filter.settings.ReadOnlyFilterSettings;
import hageldave.imagingkit.filter.settings.SettingConstraint;

public class GrayScaleFilter extends PerPixelFilter {
	public static final String RED_WEIGHT_ID = "red";
	public static final String GREEN_WEIGHT_ID = "green";
	public static final String BLUE_WEIGHT_ID = "blue";
	
	private final Integer[] weights = new Integer[]{1,1,1};
	
	public GrayScaleFilter() {
		super((px, weights) -> 
		{
			int grey = Pixel.getGrey(px.getValue(),(int)weights[0], (int)weights[1], (int)weights[2]);
			px.setValue(Pixel.argb_fast(px.a(),grey, grey, grey));
		}, 
			new FilterSettings(createSettingConstraints()));
	}

	@Override
	protected Object[] getConfiguration(ReadOnlyFilterSettings settings) {
		weights[0] = settings.getAs(RED_WEIGHT_ID, Integer.class, weights[0]);
		weights[1] = settings.getAs(GREEN_WEIGHT_ID, Integer.class, weights[1]);
		weights[2] = settings.getAs(BLUE_WEIGHT_ID, Integer.class, weights[2]);
		
		if(weights[0] == 0 && weights[1] == 0 && weights[2] == 0)
			weights[0] = weights[1] = weights[2] = 1;
		return weights;
	}
	
	private static SettingConstraint[] createSettingConstraints(){
		SettingConstraint[] constraints = new SettingConstraint[3];
		constraints[0] = SettingConstraint.pureTypeConstraint(RED_WEIGHT_ID, Integer.class);
		constraints[1] = SettingConstraint.pureTypeConstraint(GREEN_WEIGHT_ID, Integer.class);
		constraints[2] = SettingConstraint.pureTypeConstraint(BLUE_WEIGHT_ID, Integer.class);
		return constraints;
	}
	
	public void setWeights(int r, int g, int b){
		getSettings().set(RED_WEIGHT_ID, r);
		getSettings().set(GREEN_WEIGHT_ID, g);
		getSettings().set(BLUE_WEIGHT_ID, b);
	}
}
