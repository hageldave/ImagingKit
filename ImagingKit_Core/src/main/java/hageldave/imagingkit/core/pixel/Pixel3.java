package hageldave.imagingkit.core.pixel;

/**
 * The Pixel3 interface models a Pixel which consists of three values.
 * This does not necessarily mean that the implementing type actually consists
 * of three values but instead this interface provides a three value view of
 * the pixel.
 * 
 * @author hageldave
 *
 * @param <SELF> the implementing type, needed to infer the least general type for chaining to prevent
 * loss of type information.
 */
public interface Pixel3<SELF> extends PixelBase<SELF> {

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
	 * Returns the 2nd channel value of this pixel at its current position.
	 * @return 2nd channel value.
	 */
	public default double getValueCh2() {
		return getValue(2);
	}

	/**
	 * Sets the value of the 2nd channel.
	 * @param v2 the value
	 * @return this pixel for chaining.
	 */
	public default SELF setValueCh2(double v2){
		setValue(2, v2);
		return self();
	}
	
	/**
	 * Sets the values for channels 0, 1 and 2.
	 * @param v0 value for channel 0
	 * @param v1 value for channel 1
	 * @param v2 value for channel 2
	 * @return this pixel for chaining.
	 */
	public default SELF setValues(double v0, double v1, double v2){
		setValueCh0(v0);
		setValueCh1(v1);
		return setValueCh2(v2);
	}
	
}
