package hageldave.imagingkit.filter;

import hageldave.imagingkit.core.Img;

public abstract class Filter {
	
	private FilterSettings settings;
	
	public Filter(){
		this(null);
	}
	
	public Filter(FilterSettings settings) {
		if(settings == null){
			settings = new FilterSettings();
		}
		this.settings = settings;
	}

	public abstract void applyTo(final Img img);
	
	protected FilterSettings getSettings() {
		return settings;
	}
	
	protected void replaceSettings(FilterSettings settings){
		this.settings = settings;
	}
}
