package hageldave.imagingkit.filter.settings;

public interface ValueConstraint<T> {

	public boolean isValuePermitted(T val);
	
	public default void throwIfValueNotPermitted(T val) throws IllegalArgumentException {
		if(!isValuePermitted(val))
			throw new IllegalArgumentException(String.format("Provided value %s does not meet value constraint!", val));
	}
}
