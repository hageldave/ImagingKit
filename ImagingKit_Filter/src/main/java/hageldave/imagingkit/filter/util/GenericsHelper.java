package hageldave.imagingkit.filter.util;


public class GenericsHelper {

	/** same as class.isAssignableFrom(class) but with implicit cast check for native numbers */
	public static boolean isAssignableFrom(Class<?> assignedCls, Class<?> assigningCls){
		if(isNativeNumberType(assigningCls) && isNativeNumberType(assignedCls)){
			return isNumberImplicitlyCastableFrom(assignedCls, assigningCls);
		}
		return assignedCls.isAssignableFrom(assigningCls);
	}
	
	public static boolean isNativeNumberType(Class<?> type){
		return isFloatingPointNative(type) || isIntegralNative(type);
	}
	
	public static boolean isFloatingPointNative(Class<?> type){
		return MiscUtils.isAnyOf(type, Float.class, Double.class);
	}
	
	public static boolean isIntegralNative(Class<?> type){
		return MiscUtils.isAnyOf(type, Integer.class, Long.class, Short.class, Byte.class);
	}
	
	private static int getNativeOrder(Class<?> type){
		Class<?>[] order = new Class<?>[]{Byte.class, Short.class, Integer.class, Long.class, Float.class, Double.class};
		for(int i = 0; i < order.length; i++){
			if(type.equals(order[i])){
				return i;
			}
		}
		throw new IllegalArgumentException("only native number types permitted");
	}
	
	public static boolean isNumberImplicitlyCastableFrom(Class<?> assigned, Class<?> cast){
		if(isNativeNumberType(assigned) && isNativeNumberType(cast)){
			if(getNativeOrder(cast) <= getNativeOrder(assigned)){
				return true;
			}
			return false;
		}
		throw new IllegalArgumentException("only native number types permitted");
	}
	
	
	public static <T> T castToNativeNumber(Number num, Class<T> nativeNumberType){
		System.out.format("casting from %s to %s%n", num.getClass().getName(), nativeNumberType.getName());
		switch (getNativeOrder(nativeNumberType)) {
		case 0:
			return nativeNumberType.cast(num.byteValue());
		case 1:
			return nativeNumberType.cast(num.shortValue());
		case 2:
			return nativeNumberType.cast(num.intValue());
		case 3:
			return nativeNumberType.cast(num.longValue());
		case 4:
			return nativeNumberType.cast(num.floatValue());
		case 5:
			return nativeNumberType.cast(num.doubleValue());
		default:
			// will not happen because getNativeOrder throws when not one of the previous
			return null;
		}
	}
	
	/** same as class.cast(obj) but with explicit casting for native numbers e.g. int i = (int) 2.3f; */
	@SuppressWarnings("unchecked")
	public static <T> T cast(Object obj, Class<T> type){
		if(obj == null){
			return null;
		}
		if(obj.getClass() == type){
			return (T) obj;
		}
		if(isNativeNumberType(obj.getClass()) && isNativeNumberType(type)){
			return castToNativeNumber((Number)obj, type);
		}
		return type.cast(obj);
	}
	
}
