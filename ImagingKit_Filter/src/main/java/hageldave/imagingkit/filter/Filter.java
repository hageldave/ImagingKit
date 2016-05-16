package hageldave.imagingkit.filter;

import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import hageldave.imagingkit.core.Img;
import hageldave.imagingkit.filter.settings.FilterSettings;
import hageldave.imagingkit.filter.settings.FilterSettingsListener;
import hageldave.imagingkit.filter.settings.ReadOnlyFilterSettings;

public abstract class Filter implements FilterSettingsListener {
	
	private FilterSettings settings;
	private AtomicBoolean settingsChanged = new AtomicBoolean(true);
	
	public Filter(){
		this(null);
	}
	
	public Filter(FilterSettings settings) {
		if(settings == null){
			settings = new FilterSettings();
		}
		this.settings = settings;
		this.settings.addListener(this);
	}

	public void applyTo(final Img img){
		if(settingsChanged.getAndSet(false)){
			readSettingsBeforeApply(this.settings.copy().getReadOnly());
		}
		doApply(img);
	}
	
	protected abstract void readSettingsBeforeApply(ReadOnlyFilterSettings settings);
	
	protected abstract void doApply(final Img img);
	
	public FilterSettings getSettings() {
		return settings;
	}
	
	protected synchronized void replaceSettings(FilterSettings settings){
			this.settings.removeListener(this);
			this.settings = settings;
			this.settings.addListener(this);
	}
	
	@Override
	public void settingsChanged(ReadOnlyFilterSettings source, List<String> settingIds,
			ReadOnlyFilterSettings newSettings, ReadOnlyFilterSettings oldSettings) 
	{
		settingsChanged.set(true);
	}
	
	@Override
	public void settingChanged(ReadOnlyFilterSettings source, String settingId, Object newValue, Object oldValue) 
	{
		settingsChanged.set(true);
	}
}
