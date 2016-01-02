package hageldave.imagingkit.filter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class FilterSettings {
	private final Map<String, Object> settings = new HashMap<>();
	private final Map<String, Class<?>> typeConstraints = new HashMap<>();
	
	public FilterSettings() {
		
	}
	
	public FilterSettings(Map<String, Class<?>> typeConstraints) {
		this.typeConstraints.putAll(typeConstraints);
	}
	
	public FilterSettings(Object[] typeConstraints) {
		if(typeConstraints.length % 2 != 0){
			throw new IllegalArgumentException(
					"Provided typeConstraints array is odd! An array with String Class pairs is excpected, "
					+ "e.g {\"setting1\", Integer.class, \"setting2\", Float.class}");
		}
		for(int i = 0; i < typeConstraints.length; i+=2){
			if(typeConstraints[i] instanceof String){
				String settingId = (String) typeConstraints[i];
				if(typeConstraints[i+1] instanceof Class){
					@SuppressWarnings("rawtypes")
					Class clazz = (Class) typeConstraints[i+1];
					if(this.typeConstraints.containsKey(settingId)){
						throw new IllegalArgumentException(String.format(
								"duplicate settingsID in type constraints array at %d", i));
					}
					this.typeConstraints.put(settingId, clazz);
				} else {
					throw new IllegalArgumentException(String.format(
							"Invalid value in type constraints array at index %d. Expected Class type.", i+1));
				}
			} else {
				throw new IllegalArgumentException(String.format(
						"Invalid value in type constraints array at index %d. Expected String type.", i));
			}
		}
	}
	
	public void set(String settingId, Object settingValue){
		if(settingId == null || settingValue == null){
			throw new NullPointerException("None of the arguments is allowed to be null");
		}
		Class<?> clazz = typeConstraints.getOrDefault(settingId, Object.class);
		if(clazz.isInstance(settingValue)){
			settings.put(settingId, settingValue);
		} else {
			throw new IllegalArgumentException(String.format(
					"Setting %s has type constraint %s! Provided value of type %s cannot be assigned to this setting.",
					settingId, clazz.getName(), settingValue.getClass().getName()));
		}
	}
	
	public Class<?> getTypeConstraint(String settingsId){
		return typeConstraints.get(settingsId);
	}
	
	public boolean containsSetting(String settingID){
		return settings.containsKey(settingID);
	}
	
	public Object get(String settingId){
		return get(settingId, null);
	}
	
	public Object get(String settingId, Object defaultValue){
		return settings.getOrDefault(settingId, defaultValue);
	}
	
	public <T> T getAs(String settingId, Class<T> clazz){
		return getAs(settingId, clazz, null);
	}
	
	/** returns value, default value if not present or null if value is not an instance of specified type */
	public <T> T getAs(String settingId, Class<T> clazz, T defaultValue){
		Object val = get(settingId, defaultValue);
		if(clazz.isInstance(val)){
			try{
				return clazz.cast(val);
			} catch(ClassCastException e){
				return null;
			}
		}
		return null;
	}
	
	public void clear(String settingId){
		this.settings.remove(settingId);
	}
	
	public void clearAll(){
		this.settings.clear();
	}
	
	public List<String> getSettingIds(){
		Set<String> keys = settings.keySet();
		return new ArrayList<>(keys);
	}
}
