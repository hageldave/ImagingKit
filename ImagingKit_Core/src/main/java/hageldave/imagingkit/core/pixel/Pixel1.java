package hageldave.imagingkit.core.pixel;

public interface Pixel1<SELF> extends PixelBase<SELF> {
	
	/**
	 * Returns the red value of this pixel at its current position.
	 * @return red value of this pixel with 0.0 as no red contribution and 1.0 as full red contribution.
	 * May exceed [0,1] range depending on implementation.
	 * 
	 * @see #setValueCh0(double)
	 * @see #a_asDouble
	 * @see #g_asDouble
	 * @see #b_asDouble
	 */
	public double getValueCh0();


	/**
	 * Sets the red value of this pixel at its current position with 0.0 as no red contribution and 1.0 as full red contribution. 
	 * @param r the red value. May exceed [0,1] range depending on implementation.
	 * @return this pixel for chaining.
	 * 
	 * @see #getValueCh0()
	 * @see #setValueCh3(double)
	 * @see #setValueCh1(double)
	 * @see #setValueCh2(double)
	 * @see #setValues(double, double, double, double)
	 */
	public SELF setValueCh0(double r);
	
	public default double getValue(){
		return getValueCh0();
	}
	
	public default SELF setValue(double v){
		return setValueCh0(v);
	}

}
