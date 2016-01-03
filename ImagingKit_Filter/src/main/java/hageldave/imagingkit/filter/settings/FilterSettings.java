package hageldave.imagingkit.filter.settings;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class FilterSettings implements ReadOnlyFilterSettings {
	protected final HashMap<String, Object> settings = new HashMap<>();
	protected final HashMap<String, Class<?>> typeConstraints = new HashMap<>();
	
	protected LinkedList<FilterSettingsListener> listeners = new LinkedList<>();
	
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
	
	public void set(String settingId, Object settingValue, boolean notifyListeners){
		if(settingId == null || settingValue == null){
			throw new NullPointerException("None of the arguments is allowed to be null");
		}
		Class<?> clazz = typeConstraints.getOrDefault(settingId, Object.class);
		if(clazz.isInstance(settingValue)){
			Object previous = settings.put(settingId, settingValue);
			if(notifyListeners && !settingValue.equals(previous)){
				notifyListeners(settingId, settingValue, previous);
			}
		} else {
			throw new IllegalArgumentException(String.format(
					"Setting %s has type constraint %s! Provided value of type %s cannot be assigned to this setting.",
					settingId, clazz.getName(), settingValue.getClass().getName()));
		}
	}
	
	public void set(String settingId, Object settingValue){
		set(settingId, settingValue, true);
	}
	
	public void setAll(List<String> settingIds, List<Object> settingValues){
		if(settingIds.size() != settingValues.size()){
			throw new IllegalArgumentException(
					"provided unequal number of settingIds and settingValues");
		}
		// test type constraints first
		for(int i = 0; i < settingIds.size(); i++){
			String id = settingIds.get(i);
			Object value = settingValues.get(i);
			Class<?> clazz = getTypeConstraint(id);
			if(clazz != null && value != null && !clazz.isInstance(value)){
				throw new IllegalArgumentException(String.format(
						"Setting %s has type constraint %s! Provided value at index %d of type %s cannot be assigned to this setting.",
						id, clazz.getName(), i, value.getClass().getName()));
			}
		}
		// now its safe to set all values
		FilterSettings oldVals = copy(settingIds.toArray(new String[settingIds.size()]));
		List<String> changedValues = new ArrayList<>(settingIds.size());
		for(int i = 0; i < settingIds.size(); i++){
			String id = settingIds.get(i);
			Object newValue = settingValues.get(i);
			Object old = oldVals.get(id);
			if(newValue == null && old != null){
				clear(id, false);
				changedValues.add(id);
			} else if(newValue != null && !newValue.equals(old)){
				set(id, newValue, false);
				changedValues.add(id);
			} else {
				oldVals.clear(id);
			}
		}
		FilterSettings newVals = copy(changedValues.toArray(new String[changedValues.size()]));
		notifyListeners(changedValues, newVals, oldVals);
	}
	
	@Override
	public Class<?> getTypeConstraint(String settingsId){
		return typeConstraints.get(settingsId);
	}
	
	@Override
	public boolean containsSetting(String settingID){
		return settings.containsKey(settingID);
	}
	
	@Override
	public Object get(String settingId){
		return get(settingId, null);
	}
	
	@Override
	public Object get(String settingId, Object defaultValue){
		return settings.getOrDefault(settingId, defaultValue);
	}
	
	@Override
	public <T> T getAs(String settingId, Class<T> clazz){
		return getAs(settingId, clazz, null);
	}
	
	
	/** returns value, default value if not present or null if value is not an instance of specified type */
	@Override
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
	
	@Override
	public List<String> getSettingIds(){
		Set<String> keys = settings.keySet();
		return new ArrayList<>(keys);
	}
	
	public void clear(String settingId, boolean notifyListeners){
		Object previous = this.settings.remove(settingId);
		if(notifyListeners && previous != null){
			notifyListeners(settingId, null, previous);
		}
	}
	
	public void clear(String settingId){
		clear(settingId, true);
	}
	
	public void clearAll(){
		if(!settings.isEmpty()){
			FilterSettings cpy = copy();
			this.settings.clear();
			notifyListeners(cpy.getSettingIds(), cpy, new FilterSettings());
		}
	}
	
	public ReadOnlyFilterSettings getReadOnly(){
		return this;
	}
	
	public FilterSettings copy(){
		FilterSettings cpy = new FilterSettings(typeConstraints);
		cpy.settings.putAll(settings);
		return cpy;
	}
	
	public FilterSettings copy(String... settingIds){
		Map<String, Class<?>> constraints = new HashMap<>();
		Map<String, Object> settings = new HashMap<>();
		for(String id: settingIds){
			Class<?> clazz = getTypeConstraint(id);
			Object value = get(id);
			if(clazz != null){
				constraints.put(id, clazz);
			}
			if(value != null){
				settings.put(id, value);
			}
		}
		FilterSettings cpy = new FilterSettings(constraints);
		cpy.settings.putAll(settings);
		return cpy;
	}
	
	protected void notifyListeners(String settingId, Object newValue, Object oldValue){
		listeners.forEach((l)->l.settingChanged(this, settingId, newValue, oldValue));
	}
	
	protected void notifyListeners(List<String> settingIds, ReadOnlyFilterSettings newSettings, ReadOnlyFilterSettings oldSettings){
		listeners.forEach((l)->l.settingsChanged(this, settingIds, newSettings, oldSettings));
	}
	
	public void addListener(FilterSettingsListener l){
		if(l != null && !listeners.contains(l)) 
			listeners.add(l);
	}
	
	public void removeListener(FilterSettingsListener l){
		listeners.remove(l);
	}
}
