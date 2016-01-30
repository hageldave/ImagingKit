package hageldave.imagingkit.filter.settings;

import hageldave.imagingkit.filter.util.GenericsHelper;

public class SettingConstraint {

	public final Class<?> typeConstraint;
	public final ValueConstraint valueConstraint;
	public final String settingID;
	
	public SettingConstraint(String settingID, ValueConstraint valueConstraint, Class<?> typeConstraint) {
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
	
}
