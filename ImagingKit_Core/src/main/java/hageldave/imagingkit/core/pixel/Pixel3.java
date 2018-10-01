package hageldave.imagingkit.core.pixel;

public interface Pixel3<SELF> extends PixelBase<SELF> {

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
	 * Returns the green value of this pixel at its current position.
	 * @return green value of this pixel with 0.0 as no green contribution and 1.0 as full green contribution.
	 * May exceed [0,1] range depending on implementation.
	 * 
	 * @see #setValueCh1(double)
	 * @see #a_asDouble
	 * @see #r_asDouble
	 * @see #b_asDouble
	 */
	public double getValueCh1();

	/**
	 * Returns the blue value of this pixel at its current position.
	 * @return blue value of this pixel with 0.0 as no blue contribution and 1.0 as full blue contribution.
	 * May exceed [0,1] range depending on implementation.
	 * 
	 * @see #setValueCh2(double)
	 * @see #a_asDouble
	 * @see #r_asDouble
	 * @see #g_asDouble
	 */
	public double getValueCh2();

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

	/**
	 * Sets the green value of this pixel at its current position with 0.0 as no green contribution and 1.0 as full green contribution. 
	 * @param g the green value. May exceed [0,1] range depending on implementation.
	 * @return this pixel for chaining.
	 * 
	 * @see #getValueCh1()
	 * @see #setValueCh3(double)
	 * @see #setValueCh0(double)
	 * @see #setValueCh2(double)
	 * @see #setValues(double, double, double, double)
	 */
	public SELF setValueCh1(double g);

	/**
	 * Sets the blue value of this pixel at its current position with 0.0 as no blue contribution and 1.0 as full blue contribution. 
	 * @param b the blue value. May exceed [0,1] range depending on implementation.
	 * @return this pixel for chaining.
	 * 
	 * @see #getValueCh2()
	 * @see #setValueCh3(double)
	 * @see #setValueCh0(double)
	 * @see #setValueCh1(double)
	 * @see #setValues(double, double, double, double)
	 */
	public SELF setValueCh2(double b);
	
	public default SELF setValues(double v0, double v1, double v2){
		setValueCh0(v0);
		setValueCh1(v1);
		return setValueCh2(v2);
	}
}
