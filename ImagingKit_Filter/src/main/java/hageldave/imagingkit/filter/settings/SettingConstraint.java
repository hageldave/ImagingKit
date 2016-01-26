package hageldave.imagingkit.filter.settings;

import hageldave.imagingkit.filter.util.GenericsHelper;

public class SettingConstraint {

	public final Class<?> typeConstraint;
	public final ValueConstraint valueConstraint;
	public final String settingID;
	
	public <T> SettingConstraint(String settingID, ValueConstraint valueConstraint, Class<? extends T> typeConstraint) {
		this.settingID = settingID;
		this.typeConstraint = typeConstraint;
		this.valueConstraint = valueConstraint;
	}
	
	public boolean isValuePermitted(Object value){
		return isTypeAllowed(value.getClass()) && (valueConstraint == null || valueConstraint.isValuePermitted(value));
	}
	
	public boolean isTypeAllowed(Class<?> type){
		if(typeConstraint != null){
			return GenericsHelper.isAssignableFrom(typeConstraint, type);
		} else {
			return true;
		}
	}
	
}
