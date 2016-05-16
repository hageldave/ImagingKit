package hageldave.imagingkit.filter.settings;

import java.util.LinkedList;
import java.util.List;

import hageldave.imagingkit.filter.util.GenericsHelper;

public class SettingConstraint {

	public final Class<?> typeConstraint;
	public final ValueConstraint valueConstraint;
	public final String settingID;
	
	public SettingConstraint(String settingID, ValueConstraint valueConstraint, Class<?> typeConstraint) {
		if(settingID == null){
			throw new IllegalArgumentException("Cannot use null as settingID");
		}
		this.settingID = settingID;
		this.typeConstraint = typeConstraint;
		this.valueConstraint = valueConstraint;
	}
	
	public static SettingConstraint pureTypeConstraint(String settingID, Class<?> typeConstraint){
		return new SettingConstraint(settingID, null, typeConstraint);
	}
	
	public static SettingConstraint pureValueConstraint(String settingID, ValueConstraint valueConstraint){
		return new SettingConstraint(settingID, valueConstraint, null);
	}
	
	public static Builder createNew(String settingID) {
		return new Builder(settingID);
	}
	
	public boolean isValuePermitted(Object value){
		return isTypeAllowed(value.getClass()) && (valueConstraint == null || valueConstraint.isValuePermitted(value));
	}
	
	public boolean isValuePermitted_ThrowIfNot(Object value){
		if(isValuePermitted(value)){
			return true;
		} else {
			throwIfTypeNotPermitted(value.getClass());
			throwIfValueNotPermitted(value);
			throw new RuntimeException("value not permitted");
		}
	}
	
	public void throwIfValueNotPermitted(Object value) {
		if(valueConstraint != null){
			valueConstraint.throwIfValueNotPermitted(value);
		}
	}
	
	public void throwIfTypeNotPermitted(Class<?> type) {
		if(!isTypeAllowed(type)){
			throw new IllegalArgumentException(String.format(
					"Setting %s has type constraint %s! Values of type %s cannot be assigned to this setting.",
					settingID, typeConstraint.getName(), type.getName()));
		}
	}
	
	public boolean isTypeAllowed(Class<?> type){
		if(typeConstraint != null){
			return GenericsHelper.isAssignableFrom(typeConstraint, type);
		} else {
			return true;
		}
	}
	
	
	public static class Builder {
		String currentId = null;
		ValueConstraint currentValueConstraint = null;
		Class<?> currentTypeConstraint = Object.class;
		
		List<SettingConstraint> constraints = new LinkedList<>();
		
		public Builder(String settingID) {
			this.currentId = settingID;
		}
		
		public SettingConstraint build() {
			return new SettingConstraint(currentId, currentValueConstraint, currentTypeConstraint);
		}
		
		public Builder constrainValue(ValueConstraint constraint) {
			currentValueConstraint = constraint;
			return this;
		}
		
		
		public Builder constrainType(Class<?> type) {
			currentTypeConstraint = type;
			return this;
		}
		
		public Builder appendNew(String settingID) {
			constraints.add(this.build());
			// reset
			this.currentTypeConstraint = null;
			this.currentValueConstraint = null;
			// new ID
			this.currentId = settingID;
			return this;
		}
		
		public List<SettingConstraint> buildAll() {
			appendNew("");
			return this.constraints;
		}
		
		
	}
	
}
