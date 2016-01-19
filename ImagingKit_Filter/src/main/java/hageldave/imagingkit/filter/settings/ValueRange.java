package hageldave.imagingkit.filter.settings;

import java.util.Comparator;

public class ValueRange<T> implements ValueConstraint<T> {

	public T min;
	public T max;
	public final Comparator<T> comparator;
	public boolean minExclusive;
	public boolean maxExclusive;
	
	public static <V extends Comparable<V>> ValueRange<V> fromComparable(V min, V max, boolean minExclusive, boolean maxExclusive){
		return new ValueRange<V>((a,b)->a.compareTo(b), min, max, minExclusive, maxExclusive);
	}
	
	public static <V extends Comparable<V>> ValueRange<V> fromComparable(V min, V max){
		return fromComparable(min, max, false, false);
	}
	
	public static <V extends Number> ValueRange<V> fromNumber(V min, V max, boolean minExclusive, boolean maxExclusive){
		return new ValueRange<V>((a,b)->{return (int)Math.signum(a.doubleValue()-b.doubleValue());}, min, max, minExclusive, maxExclusive);
	}
	
	public static <V extends Number> ValueRange<V> fromNumber(V min, V max){
		return fromNumber(min, max, false, false);
	}
	
	public ValueRange(Comparator<T> comparator, T min, T max, boolean minExclusive, boolean maxExclusive) {
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
	}
	
	public ValueRange(Comparator<T> comparator, T min, T max){
		this(comparator, min, max, false, false);
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
	
	@Override
	public boolean isValuePermitted(T val) {
		return isInRange(val);
	}
}
