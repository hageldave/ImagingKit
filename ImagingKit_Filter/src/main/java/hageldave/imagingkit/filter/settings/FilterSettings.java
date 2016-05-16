package hageldave.imagingkit.filter.settings;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import hageldave.imagingkit.filter.util.GenericsHelper;

public class FilterSettings implements ReadOnlyFilterSettings {
	protected final HashMap<String, Object> settings = new HashMap<>();
	protected final HashMap<String, SettingConstraint> constraints = new HashMap<>();
	protected final Set<String> allowedSettingIds = new HashSet<>();
	
	protected LinkedList<FilterSettingsListener> listeners = new LinkedList<>();
	
	public FilterSettings() {
		// NOOP
	}

	
	public FilterSettings(SettingConstraint[] constraints) {
		this(Arrays.asList(constraints));
	}
	
	public FilterSettings(Collection<SettingConstraint> constraints) {
		for(SettingConstraint c: constraints){
			this.constraints.put(c.settingID, c);
			this.allowedSettingIds.add(c.settingID);
		}
	}
	
	public synchronized void set(String settingId, Object settingValue, boolean notifyListeners){
		if(settingId == null || settingValue == null){
			throw new NullPointerException("None of the arguments is allowed to be null");
		}
		if(!allowedSettingIds.contains(settingId)){
			return;
		}
		
		SettingConstraint c = getConstraint(settingId);
		if(c == null || c.isValuePermitted_ThrowIfNot(settingValue)){
			Object previous = settings.put(settingId, settingValue);
			if(notifyListeners && !settingValue.equals(previous)){
				notifyListeners(settingId, settingValue, previous);
			}
		}
	}
	
	public synchronized void set(String settingId, Object settingValue){
		set(settingId, settingValue, true);
	}
	
	public synchronized void setAll(List<String> settingIds, List<Object> settingValues){
		if(settingIds.size() != settingValues.size()){
			throw new IllegalArgumentException(
					"provided unequal number of settingIds and settingValues");
		}
		// test type constraints first
		for(int i = 0; i < settingIds.size(); i++){
			String id = settingIds.get(i);
			Object value = settingValues.get(i);
			SettingConstraint c = getConstraint(id);
			if(value != null && c != null){
				c.isValuePermitted_ThrowIfNot(value);
			}
		}
		// now its safe to set all values
		FilterSettings oldVals = copy(settingIds.toArray(new String[settingIds.size()]));
		List<String> changedValues = new ArrayList<>(settingIds.size());
		for(int i = 0; i < settingIds.size(); i++){
			String id = settingIds.get(i);
			if(!allowedSettingIds.contains(id)){
				continue;
			}
			Object newValue = settingValues.get(i);
			Object old = oldVals.get(id);
			if(newValue == null && old != null){
				this.clear(id, false);
				changedValues.add(id);
			} else if(newValue != null && !newValue.equals(old)){
				this.set(id, newValue, false);
				changedValues.add(id);
			} else {
				oldVals.clear(id);
			}
		}
		FilterSettings newVals = copy(changedValues.toArray(new String[changedValues.size()]));
		notifyListeners(changedValues, newVals, oldVals);
	}
	
	public SettingConstraint getConstraint(String settingId){
		return this.constraints.get(settingId);
	}
	
	@Override
	public Class<?> getTypeConstraint(String settingId){
		SettingConstraint c = constraints.get(settingId);
		return c == null ? Object.class: c.typeConstraint == null ? Object.class:c.typeConstraint;
	}
	
	@Override
	public boolean isTypeConstrained(String settingId){
		return getTypeConstraint(settingId) != Object.class;
	}
	
	@Override
	public ValueConstraint getValueConstraint(String settingId){
		SettingConstraint c = constraints.get(settingId);
		return c == null ? ValueConstraint.ALLOWALLBUTNULL: c.valueConstraint == null ? ValueConstraint.ALLOWALLBUTNULL:c.valueConstraint;
	}
	
	@Override
	public boolean isValueConstrained(String settingId){
		return getValueConstraint(settingId) != ValueConstraint.ALLOWALLBUTNULL;
	}
	
	
	@Override
	public synchronized boolean containsSetting(String settingID){
		return settings.containsKey(settingID);
	}
	
	@Override
	public synchronized Object get(String settingId){
		return get(settingId, null);
	}
	
	@Override
	public synchronized Object get(String settingId, Object defaultValue){
		return settings.getOrDefault(settingId, defaultValue);
	}
	
	@Override
	public synchronized <T> T getAs(String settingId, Class<T> clazz){
		return getAs(settingId, clazz, null);
	}
	
	
	/** returns value when present or default value if not present or if value cannot be cast to specified type */
	@Override
	public synchronized <T> T getAs(String settingId, Class<T> clazz, T defaultValue){
		Object val = get(settingId, defaultValue);
		try{
			return GenericsHelper.cast(val, clazz);
		} catch(ClassCastException e){
			return defaultValue;
		}
	}
	
	@Override
	public synchronized List<String> getSettingIds(){
		Set<String> keys = settings.keySet();
		return new ArrayList<>(keys);
	}
	
	public synchronized void clear(String settingId, boolean notifyListeners){
		Object previous = this.settings.remove(settingId);
		if(notifyListeners && previous != null){
			notifyListeners(settingId, null, previous);
		}
	}
	
	public synchronized void clear(String settingId){
		clear(settingId, true);
	}
	
	public synchronized void clearAll(){
		if(!settings.isEmpty()){
			FilterSettings cpy = copy();
			this.settings.clear();
			notifyListeners(cpy.getSettingIds(), cpy, new FilterSettings());
		}
	}
	
	public ReadOnlyFilterSettings getReadOnly(){
		return this;
	}
	
	public synchronized FilterSettings copy(){
		FilterSettings cpy = new FilterSettings(constraints.values());
		cpy.settings.putAll(settings);
		return cpy;
	}
	
	public synchronized FilterSettings copy(String... settingIds){
		Map<String, SettingConstraint> constraints = new HashMap<>();
		Map<String, Object> settings = new HashMap<>();
		for(String id: settingIds){
			SettingConstraint constraint = constraints.get(id);
			Object value = get(id);
			if(constraint != null){
				constraints.put(id, constraint);
			}
			if(value != null){
				settings.put(id, value);
			}
		}
		FilterSettings cpy = new FilterSettings();
		cpy.settings.putAll(settings);
		cpy.constraints.putAll(constraints);
		return cpy;
	}
	
	protected void notifyListeners(String settingId, Object newValue, Object oldValue){
		synchronized (listeners) {
			listeners.forEach((l)->l.settingChanged(this, settingId, newValue, oldValue));
		}	
	}
	
	protected void notifyListeners(List<String> settingIds, ReadOnlyFilterSettings newSettings, ReadOnlyFilterSettings oldSettings){
		synchronized (listeners) {
			listeners.forEach((l)->l.settingsChanged(this, settingIds, newSettings, oldSettings));
		}
	}
	
	public void addListener(FilterSettingsListener l){
		synchronized (listeners) {
			if(l != null && !listeners.contains(l)) 
				listeners.add(l);
		}
	}
	
	public void removeListener(FilterSettingsListener l){
		synchronized (listeners) {
			listeners.remove(l);
		}
	}
	
	public List<FilterSettingsListener> getListeners(){
		synchronized (listeners) {
			return new ArrayList<>(listeners);
		}
	}
}
