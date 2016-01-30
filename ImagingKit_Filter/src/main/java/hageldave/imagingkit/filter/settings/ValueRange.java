package hageldave.imagingkit.filter.settings;

import java.util.Comparator;

import hageldave.imagingkit.filter.util.GenericsHelper;

public class ValueRange<T> implements ValueConstraint {

	public T min;
	public T max;
	public final Comparator<T> comparator;
	public boolean minExclusive;
	public boolean maxExclusive;
	private final Class<T> valueType;
	
	public static <V extends Comparable<V>> ValueRange<V> fromComparable(V min, V max, boolean minExclusive, boolean maxExclusive, Class<V> valueType){
		return new ValueRange<V>((a,b)->a.compareTo(b), min, max, minExclusive, maxExclusive, valueType);
	}
	
	public static <V extends Comparable<V>> ValueRange<V> fromComparable(V min, V max, Class<V> valueType){
		return fromComparable(min, max, false, false, valueType);
	}
	
	public static <V extends Number> ValueRange<V> fromNumber(V min, V max, boolean minExclusive, boolean maxExclusive, Class<V> valueType){
		return new ValueRange<V>((a,b)->{return (int)Math.signum(a.doubleValue()-b.doubleValue());}, min, max, minExclusive, maxExclusive, valueType);
	}
	
	public static <V extends Number> ValueRange<V> fromNumber(V min, V max, Class<V> valueType){
		return fromNumber(min, max, false, false, valueType);
	}
	
	public ValueRange(Comparator<T> comparator, T min, T max, boolean minExclusive, boolean maxExclusive, Class<T> valueType) {
		this.comparator = comparator;
		if(comparator.compare(max, min) < 0){
			this.min = max;
			this.max = min;
		} else {
			this.min = min;
			this.max = max;
		}
		this.minExclusive = minExclusive;
		this.maxExclusive = maxExclusive;
		this.valueType = valueType;
	}
	
	public ValueRange(Comparator<T> comparator, T min, T max, Class<T> valueType){
		this(comparator, min, max, false, false, valueType);
	}
	
	public boolean isLowerMin(T val){
		return comparator.compare(val, min) < 0;
	}
	
	public boolean isLowerOrEqualMin(T val){
		return comparator.compare(val, min) <= 0;
	}
	
	public boolean isHigherMax(T val){
		return comparator.compare(val, max) > 0;
	}
	
	public boolean isHigherOrEqualMax(T val){
		return comparator.compare(val, max) >= 0;
	}
	
	private boolean _lower(T val){
		return minExclusive ? isLowerOrEqualMin(val):isLowerMin(val);
	}
	
	private boolean _higher(T val){
		return maxExclusive ? isHigherOrEqualMax(val):isHigherMax(val);
	}
	
	public boolean isInRange(T val){
		return !_higher(val) && !_lower(val);
	}
	
	public Class<T> getValueType(){
		return this.valueType;
	}
	
	@Override
	public boolean isValuePermitted(Object val) {
		if(GenericsHelper.isAssignableFrom(valueType, val.getClass())){
			return isInRange(GenericsHelper.cast(val, valueType));
		}
		return false;
	}
	
	@Override
	public void throwIfValueNotPermitted(Object val) throws IllegalArgumentException {
		if(!isValuePermitted(val))
			throw new IllegalArgumentException(String.format("provided value %s is not in range of %s%s,%s%s", val, minExclusive ? "]":"[", min, max, maxExclusive ? "[":"]"));
	}
}
