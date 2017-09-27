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
	
	public static double clamp(double lowerBound, double value, double upperBound){
		return Math.max(lowerBound, Math.min(value, upperBound));
	}

	public static int clamp(int lowerBound, int value, int upperBound){
		return Math.max(lowerBound, Math.min(value, upperBound));
	}
	
}
