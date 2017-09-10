/*
 * Copyright 2017 David Haegele
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to
 * deal in the Software without restriction, including without limitation the
 * rights to use, copy, modify, merge, publish, distribute, sublicense, and/or
 * sell copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING
 * FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS
 * IN THE SOFTWARE.
 */

package hageldave.imagingkit.core.scientific;

import hageldave.imagingkit.core.PixelBase;

/**
 * Pixel class for retrieving a value from an {@link ColorImg}.
 * A pixel object stores a position and can be used to get and set values of
 * an ColorImg. It is NOT the value and changing its position will not change the
 * image, instead it will reference a different value of the image as the
 * pixel object is a pointer to a value in the ColorImg's data array.
 * <p>
 * The Pixel class also provides a set of vector calculations using the RGB channels
 * as a 3-dimensional vector.
 *
 * @author hageldave
 * @since 2.0
 */
public class ColorPixel implements PixelBase {

	/** red channel index */
	public static final int R = ColorImg.channel_r;
	/** green channel index */
	public static final int G = ColorImg.channel_g;
	/** blue channel index */
	public static final int B = ColorImg.channel_b;
	/** alpha channel index */
	public static final int A = ColorImg.channel_a;

	/** ColorImg this pixel belongs to */
	private final ColorImg img;

	/** index of the value this pixel references */
	private int index;

	/**
	 * Creates a new Pixel object referencing the value
	 * of specified ColorImg at specified index.
	 * <p>
	 * No bounds checks are performed for index.
	 * @param img the ColorImg this pixel corresponds to
	 * @param index of the value in the images data array
	 * @see #ColorPixel(ColorImg, int, int)
	 * @see ColorImg#getPixel()
	 * @see ColorImg#getPixel(int, int)
	 */
	public ColorPixel(ColorImg img, int index) {
		this.img = img;
		this.index = index;
	}

	/**
	 * Creates a new Pixel object referencing the value
	 * of specified ColorImg at specified position.
	 * <p>
	 * No bounds checks are performed for x and y
	 * @param img the ColorImg this pixel corresponds to
	 * @param x coordinate
	 * @param y coordinate
	 * @see #ColorPixel(ColorImg, int)
	 * @see ColorImg#getPixel()
	 * @see ColorImg#getPixel(int, int)
	 */
	public ColorPixel(ColorImg img, int x, int y) {
		this(img, y*img.getWidth()+x);
	}

	@Override
	public ColorImg getSource() {
		return img;
	}

	@Override
	public ColorPixel setIndex(int index) {
		this.index = index;
		return this;
	}

	@Override
	public ColorPixel setPosition(int x, int y) {
		this.index = y*img.getWidth()+x;
		return this;
	}

	@Override
	public int getIndex() {
		return index;
	}

	@Override
	public int getX() {
		return index % img.getWidth();
	}

	@Override
	public int getY() {
		return index / img.getWidth();
	}

	/**
	 * Sets the value of the ColorImg at the position currently referenced by
	 * this Pixel for the specified channel.
	 * 
	 * @param channel one of {@link #R},{@link #G},{@link #B},{@link #A} (0,1,2,3)
	 * @param value to be set e.g. 0xff0000ff for blue.
	 * @throws ArrayIndexOutOfBoundsException if this Pixel's index is not in
	 * range of the ColorImg's data array, or if the specified channel is not in [0,3],
	 * or if the specified channel is alpha but the image has no alpha (you may check 
	 * this with {@code getSource().hasAlpha()})
	 * 
	 * @see #setARGB_fromDouble(double, double, double, double)
	 * @see #setRGB_fromDouble(double, double, double)
	 * @see #getValue(int channel)
	 * @see ColorImg#setValue(int channel, int x, int y, double value)
	 */
	public ColorPixel setValue(int channel, double value){
		this.img.getData()[channel][index] = value;
		return this;
	}

	/**
	 * Gets the value of the ColorImg at the position currently referenced by
	 * this Pixel.
	 * 
	 * @param channel one of {@link #R},{@link #G},{@link #B},{@link #A} (0,1,2,3)
	 * @return the value of the ColorImg currently referenced by this Pixel.
	 * @throws ArrayIndexOutOfBoundsException if this Pixel's index is not in
	 * range of the ColorImg's data array, or if the specified channel is not in [0,3],
	 * or if the specified channel is alpha (3) but the image has no alpha (you may check 
	 * this with {@code getSource().hasAlpha()})
	 * 
	 * @see #a_asDouble()
	 * @see #r_asDouble()
	 * @see #g_asDouble()
	 * @see #b_asDouble()
	 * @see #setValue(int channel, double value)
	 * @see ColorImg#getValue(int channel, int x, int y)
	 */
	public double getValue(int channel){
		return this.img.getData()[channel][index];
	}

	@Override
	public double a_asDouble(){
		return img.hasAlpha() ? this.img.getDataA()[index]:1;
	}

	@Override
	public double r_asDouble(){
		return this.img.getDataR()[index];
	}
	
	@Override
	public double g_asDouble(){
		return this.img.getDataG()[index];
	}

	@Override
	public double b_asDouble(){
		return this.img.getDataB()[index];
	}

	@Override
	public ColorPixel setA_fromDouble(double a){
		if(img.hasAlpha())
			this.img.getDataA()[index] = a;
		return this;
	}

	@Override
	public ColorPixel setR_fromDouble(double r){
		this.img.getDataR()[index] = r;
		return this;
	}

	@Override
	public ColorPixel setG_fromDouble(double g){
		this.img.getDataG()[index] = g;
		return this;
	}

	@Override
	public ColorPixel setB_fromDouble(double b){
		this.img.getDataB()[index] = b;
		return this;
	}
	
	@Override
	public ColorPixel setARGB_fromDouble(double a, double r, double g, double b) {
		PixelBase.super.setARGB_fromDouble(a, r, g, b);
		return this;
	}
	
	@Override
	public ColorPixel setRGB_fromDouble(double r, double g, double b) {
		PixelBase.super.setRGB_fromDouble(r, g, b);
		return this;
	}
	
	@Override
	public ColorPixel setRGB_fromDouble_preserveAlpha(double r, double g, double b) {
		PixelBase.super.setRGB_fromDouble_preserveAlpha(r, g, b);
		return this;
	}

	/**
	 * @return luminance of this pixel. <br>
	 * Using weights r=0.2126 g=0.7152 b=0.0722
	 * @throws ArrayIndexOutOfBoundsException if this Pixel's index is not in
	 * range of the ColorImg's data array.
	 * @see #getGrey(double rW, double gW, double bW)
	 * @see #getLuminance(double r, double g, double b)
	 */
	public double getLuminance(){
		return ColorPixel.getLuminance(r_asDouble(),g_asDouble(),b_asDouble());
	}

	/**
	 * Calculates the grey value of this pixel using specified weights.
	 * @param redWeight weight for red channel
	 * @param greenWeight weight for green channel
	 * @param blueWeight weight for blue channel
	 * @return grey value of pixel for specified weights
	 * @throws ArrayIndexOutOfBoundsException if this Pixel's index is not in
	 * range of the ColorImg's data array.
	 * @see #getLuminance()
	 * @see #getGrey(double r, double g, double b, double rW, double gW, double bW)
	 */
	public double getGrey(final double redWeight, final double greenWeight, final double blueWeight){
		return ColorPixel.getGrey(r_asDouble(),g_asDouble(),b_asDouble(), redWeight, greenWeight, blueWeight);
	}

	@Override
	public String toString() {
		return asString();
	}

	/**
	 * Converts the pixels RGB channel values from one value range to another. 
	 * Alpha is preserved.
	 * <p>
	 * Suppose we know the pixels value range is currently from -10 to 10, and we want to
	 * change that value range to 0.0 to 1.0, then the call would look like this:<br>
	 * {@code convertRange(-10,10, 0,1)}.<br>
	 * A channel value of -10 would then be 0, a channel value of 0 would then be 0.5, 
	 * a channel value 20 would then be 1.5 (even though it is out of range).
	 * 
	 * @param lowerLimitNow the lower limit of the currently assumed value range
	 * @param upperLimitNow the upper limit of the currently assumed value range
	 * @param lowerLimitAfter the lower limit of the desired value range
	 * @param upperLimitAfter the upper limit of the desired value range
	 * @return this pixel for chaining.
	 * 
	 * @see #scale(double)
	 */
	public ColorPixel convertRange(double lowerLimitNow, double upperLimitNow, double lowerLimitAfter, double upperLimitAfter){
		//		double currentRange = upperLimitNow-lowerLimitNow;
		//		double newRange = upperLimitAfter-lowerLimitAfter;
		//		double scaling = newRange/currentRange;
		double scaling = (upperLimitAfter-lowerLimitAfter)/(upperLimitNow-lowerLimitNow);
		setRGB_fromDouble_preserveAlpha(
				lowerLimitAfter+(r_asDouble()-lowerLimitNow)*scaling,
				lowerLimitAfter+(g_asDouble()-lowerLimitNow)*scaling,
				lowerLimitAfter+(b_asDouble()-lowerLimitNow)*scaling);
		return this;
	}

	/**
	 * Scales the RGB vector by the specified factor. Alpha is preserved.
	 * @param factor to scale the RGB channels with
	 * @return this pixel for chaining
	 * 
	 * @see #scale(double)
	 * @see #normalize()
	 * @see #getLen()
	 * @see #getLenSquared()
	 * @see #add(double, double, double)
	 * @see #subtract(double, double, double)
	 * @see #cross(double, double, double)
	 * @see #cross_(double, double, double)
	 * @see #dot(double, double, double)
	 * @see #transform(double[][] mat)
	 */
	public ColorPixel scale(double factor){
		return setRGB_fromDouble_preserveAlpha(r_asDouble()*factor, g_asDouble()*factor, b_asDouble()*factor);
	}

	/**
	 * Adds the specified RGB channel values to this pixels RGB channels (vector addition).
	 * Alpha is preserved.
	 * @param r to be added to this r
	 * @param g to be added to this g
	 * @param b to be added to this b
	 * @return this pixel for chaining
	 */
	public ColorPixel add(double r, double g, double b){
		return setRGB_fromDouble_preserveAlpha(r+r_asDouble(), g+g_asDouble(), b+b_asDouble());
	}

	/**
	 * Subtractes the specified RGB channel values from this pixels RGB channels (vector subtraction).
	 * Alpha is preserved.
	 * @param r to be subtracted from this r
	 * @param g to be subtracted from this g
	 * @param b to be subtracted from this b
	 * @return this pixel for chaining
	 */
	public ColorPixel subtract(double r, double g, double b){
		return add(-r,-g,-b);
	}

	/**
	 * Sets this RGB vector to the result of the cross product of this RGB vector with
	 * the specified vector. Alpha is preserved.
	 * See {@link #cross_(double, double, double)} for cross product with swapped arguments.<br>
	 * Pseudo code:
	 * <pre>
	 * a = this
	 * b = specified
	 * this = cross(a,b)
	 * ---------------------
	 * cross(a,b) :=
	 *    c0 = a1*b2 - a2*b1
	 *    c1 = a2*b0 - a0*b2
	 *    c2 = a0*b1 - a1*b0
	 *    return c
	 * </pre>
	 * 
	 * @param r of the specified vector
	 * @param g of the specified vector
	 * @param b of the specified vector
	 * @return this pixel for chaining
	 */
	public ColorPixel cross(double r, double g, double b){
		return setRGB_fromDouble_preserveAlpha(
				(g_asDouble()*b)-(g*b_asDouble()),
				(b_asDouble()*r)-(b*r_asDouble()),
				(r_asDouble()*g)-(r*g_asDouble()));
	}

	/**
	 * Sets this RGB vector to the result of the cross product of the specified vector 
	 * with this RGB vector. Alpha is preserved. 
	 * This is the same calculation as {@link #cross(double, double, double)} but with
	 * swapped arguments to the cross product operator.
	 * <br>
	 * Pseudo code:
	 * <pre>
	 * a = specified
	 * b = this
	 * this = cross(a,b)
	 * ---------------------
	 * cross(a,b) :=
	 *    c0 = a1*b2 - a2*b1
	 *    c1 = a2*b0 - a0*b2
	 *    c2 = a0*b1 - a1*b0
	 *    return c
	 * </pre>
	 * 
	 * @param r of the specified vector
	 * @param g of the specified vector
	 * @param b of the specified vector
	 * @return this pixel for chaining
	 */
	public ColorPixel cross_(double r, double g, double b){
		return setRGB_fromDouble_preserveAlpha(
				(g*b_asDouble())-(g_asDouble()*b),
				(b*r_asDouble())-(b_asDouble()*r),
				(r*g_asDouble())-(r_asDouble()*g));
	}

	/**
	 * Calculates the dot product of this RGB vector and the specified vector.<br>
	 * {@code dot = a0*b0 + a1*b1 + a2*b2}.
	 * 
	 * @param r of the specified vector
	 * @param g of the specified vector
	 * @param b of the specified vector
	 * @return the dot product
	 */
	public double dot(double r, double g, double b){
		return getGrey(r, g, b);
	}

	/**
	 * Applies the specified transformation matrix (3x3)
	 * to this RGB vector. Alpha is preserved. <br>
	 * {@code this = m * this}
	 * 
	 * @param m00 first row first col
	 * @param m01 first row second col
	 * @param m02 first row third col
	 * @param m10 second row first col
	 * @param m11 second row second col
	 * @param m12 second row third col
	 * @param m20 third row first col
	 * @param m21 third row second col
	 * @param m22 third row third col
	 * @return this pixel for chaining
	 */
	public ColorPixel transform(
			double m00, double m01, double m02,
			double m10, double m11, double m12,
			double m20, double m21, double m22)
	{
		return setRGB_fromDouble_preserveAlpha(
				dot(m00,m01,m02),
				dot(m10,m11,m12),
				dot(m20,m21,m22));
	}

	/**
	 * Applies the specified transformation matrix (3x3)
	 * to this RGB vector. Alpha is preserved. <br>
	 * {@code this = m * this}.<br>
	 * The specified matrix is in row major format (m[row][col]),
	 * and has to be at least of size 3x3 (can be larger, but only first three
	 * rows and columns are used).
	 * @param m3x3 the transformation matrix (row major)
	 * @return this pixel for chaining
	 * @throws ArrayIndexOutOfBoundsException if the specified matrix is not at least 3x3 in size.
	 */
	public ColorPixel transform(double[][] m3x3){
		return transform(
				m3x3[0][0],m3x3[0][1],m3x3[0][2],
				m3x3[1][0],m3x3[1][1],m3x3[1][2],
				m3x3[2][0],m3x3[2][1],m3x3[2][2]);
	}


	/**
	 * Returns the squared length of this RGB vector. You can also get the actual length with
	 * {@link #getLen()} which is more costly due to the square root operation.
	 * @return the squared length of this RGB vector.
	 */
	public double getLenSquared(){
		return r_asDouble()*r_asDouble() + g_asDouble()*g_asDouble() + b_asDouble()*b_asDouble();
	}

	/**
	 * Returns the length of this RGB vector. You can also use {@link #getLenSquared()} which is 
	 * less costly as no square root operation is required.
	 * @return the length of this RGB vector.
	 */
	public double getLen(){
		return Math.sqrt(getLenSquared());
	}

	/**
	 * Normalizes this RGB vector to unit length. Alpha is preserved.
	 * If this RGB vectors length is 0, then it will stay unchanged.
	 * @return this pixel for chaining
	 */
	public ColorPixel normalize(){
		double len = getLen();
		if(len == 0.0)
			return this;
		double divByLen = 1.0/len;
		return scale(divByLen);
	}

	/**
	 * Returns the channel index with minimum value.
	 * Alpha is not considered.
	 * @return 0 or 1 or 2.
	 */
	public int minChannel() {
		int c = 0;
		if(getValue(c) > getValue(1)) c=1;
		if(getValue(c) > getValue(2)) c=2;
		return c;
	}

	/**
	 * Returns the channel index with maximum value.
	 * Alpha is not considered.
	 * @return 0 or 1 or 2.
	 */
	public int maxChannel() {
		int c = 0;
		if(getValue(c) < getValue(1)) c=1;
		if(getValue(c) < getValue(2)) c=2;
		return c;
	}

	/**
	 * Returns the minimum channel value. Alpha is not considered.
	 * @return minimum channel value of RGB of this pixel
	 */
	public double minValue() {
		return getValue(minChannel());
	}

	/**
	 * Returns the maximum channel value. Alpha is not considered.
	 * @return maximum channel value of RGB of this pixel
	 */
	public double maxValue() {
		return getValue(maxChannel());
	}


	/* * * * * * * * * */
	// STATIC  METHODS //
	/* * * * * * * * * */

	/**
	 * Calculates the luminance of the specified RGB color
	 * using weights r=0.2126 g=0.7152 b=0.0722.
	 * @param r red value of the color
	 * @param g green value of the color
	 * @param b blue value of the color
	 * @return the luminance value
	 */
	public static final double getLuminance(final double r, final double g, final double b){
		return getGrey(r,g,b, 0.2126, 0.7152, 0.0722);
	}

	/**
	 * Calculates a grey value from the specified RGB color using the specified
	 * weights for each R,G and B channel.
	 * <p>
	 * It is advised to use non-negative weights that sum up to 1.0 to get a
	 * grey value within the same value range as the specified color channels.
	 * <pre>
	 * grey = r*redWeight + g*greenWeight + b*blueWeight
	 * </pre>
	 * 
	 * @param r red value of the color
	 * @param g green value of the color
	 * @param b blue value of the color
	 * @param redWeight weight for red channel
	 * @param greenWeight weight for green channel
	 * @param blueWeight weight for blue channel
	 * @return weighted grey value of the specified RGB color.
	 */
	public static final double getGrey(final double r, final double g, final double b, final double redWeight, final double greenWeight, final double blueWeight){
		return r*redWeight+g*greenWeight+b*blueWeight;
	}

}
