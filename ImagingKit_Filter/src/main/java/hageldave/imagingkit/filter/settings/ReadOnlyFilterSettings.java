package hageldave.imagingkit.filter.settings;

import java.util.List;

public interface ReadOnlyFilterSettings {
	public Class<?> getTypeConstraint(String settingsId);
	public boolean isTypeConstrained(String settingId);
	public ValueConstraint getValueConstraint(String settingId);
	public boolean isValueConstrained(String settingId);
	public boolean containsSetting(String settingID);
	public Object get(String settingId);
	public Object get(String settingId, Object defaultValue);
	public <T> T getAs(String settingId, Class<T> clazz);
	public <T> T getAs(String settingId, Class<T> clazz, T defaultValue);
	public List<String> getSettingIds();
}