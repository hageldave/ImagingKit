package hageldave.imagingkit.filter.util;

public class MiscUtils {

	public static boolean isAnyOf(Object object, Object ... objects){
		for(Object obj: objects){
			if(object == obj || (object != null && object.equals(obj))){
				return true;
			}
		}
		return false;
	}

}
