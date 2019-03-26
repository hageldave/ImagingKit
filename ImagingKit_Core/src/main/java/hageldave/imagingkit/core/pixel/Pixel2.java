package hageldave.imagingkit.core.pixel;

/**
 * The Pixel2 interface models a Pixel which consists of two values.
 * This does not necessarily mean that the implementing type actually consists
 * of two values but instead this interface provides a two value view of
 * the pixel.
 * 
 * @author hageldave
 *
 * @param <SELF> the implementing type, needed to infer the least general type for chaining to prevent
 * loss of type information.
 */
public interface Pixel2<SELF> extends PixelBase<SELF> {

	/**
	 * Returns the 0th channel value of this pixel at its current position.
	 * @return 0th channel value.
	 */
	public default double getValueCh0() {
		return getValue(0);
	}
	
	/**
	 * Sets the value of the 0th channel.
	 * @param v0 the value
	 * @return this pixel for chaining.
	 */
	public default SELF setValueCh0(double v0){
		setValue(0, v0);
		return self();
	}

	/**
	 * Returns the 1st channel value of this pixel at its current position.
	 * @return 1st channel value.
	 */
	public default double getValueCh1() {
		return getValue(1);
	}

	/**
	 * Sets the value of the 1st channel.
	 * @param v1 the value
	 * @return this pixel for chaining.
	 */
	public default SELF setValueCh1(double v1){
		setValue(1, v1);
		return self();
	}
	
	/**
	 * Sets the values for channels 0 and 1.
	 * @param v0 value for channel 0
	 * @param v1 value for channel 1
	 * @return this pixel for chaining.
	 */
	public default SELF setValues(double v0, double v1){
		setValueCh0(v0);
		return setValueCh1(v1);
	}
	
}
