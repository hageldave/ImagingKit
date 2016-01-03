package hageldave.imagingkit.filter.settings;

import java.util.List;

public interface FilterSettingsListener {

	public void settingChanged(ReadOnlyFilterSettings source, String settingId, Object newValue, Object oldValue);
	
	public void settingsChanged(ReadOnlyFilterSettings source, List<String> settingIds, ReadOnlyFilterSettings newSettings, ReadOnlyFilterSettings oldSettings);

}
