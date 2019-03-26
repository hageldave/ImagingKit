package hageldave.imagingkit.core.pixel;

/**
 * The Pixel1 interface models a Pixel which consists of a single value.
 * This does not necessarily mean that the implementing type actually consists
 * of a single value but instead this interface provides a single value view of
 * the pixel.
 * 
 * @author hageldave
 *
 * @param <SELF> the implementing type, needed to infer the least general type for chaining to prevent
 * loss of type information.
 */
public interface Pixel1<SELF> extends PixelBase<SELF> {
	
	/**
	 * Returns the 0th channel value of this pixel at its current position.
	 * @return 0th channel value.
	 * 
	 * @see #setValueCh0(double)
	 */
	public default double getValueCh0() {
		return getValue(0);
	}


	/**
	 * Sets the value of the 0th channel.
	 * @param v0 the value
	 * @return this pixel for chaining.
	 * 
	 * @see #getValueCh0()
	 * @see #setValue(double)
	 */
	public default SELF setValueCh0(double v0){
		setValue(0, v0);
		return self();
	}
	
	/**
	 * Identical to {@link #getValueCh0()}
	 * @return the value of this pixel.
	 */
	public default double getValue(){
		return getValueCh0();
	}
	
	/**
	 * Sets the value of this pixel. Identical to {@link #setValueCh0(double)}
	 * @param v the value
	 * @return this pixel for chaining.
	 */
	public default SELF setValue(double v){
		return setValueCh0(v);
	}

}
