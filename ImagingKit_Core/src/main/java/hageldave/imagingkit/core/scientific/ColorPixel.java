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
 * A Pixel object stores a position and can be used to get and set values of
 * an ColorImg. It is NOT the value and changing its position will not change the
 * image, instead it will reference a different value of the image as the
 * pixel object is a pointer to a value in the ColorImg's data array.
 * <p>
 * The Pixel class also provides a set of static methods for color decomposition
 * and recombination from color channels like {@link #argb(int, int, int, int)}
 * or {@link #a(int)}, {@link #r(int)}, {@link #g(int)}, {@link #b(int)}.
 *
 * @author hageldave
 * @since 1.0
 */
public class ColorPixel implements PixelBase {

	public static final int R = ColorImg.channel_r;
	public static final int G = ColorImg.channel_g;
	public static final int B = ColorImg.channel_b;
	public static final int A = ColorImg.channel_a;

	/** ColorImg this pixel belongs to
	 * @since 1.0 */
	private final ColorImg img;

	/** index of the value this pixel references
	 * @since 1.0 */
	private int index;

	/**
	 * Creates a new Pixel object referencing the value
	 * of specified ColorImg at specified index.
	 * <p>
	 * No bounds checks are performed for index.
	 * @param ColorImg the ColorImg this pixel corresponds to
	 * @param index of the value in the images data array
	 * @see #Pixel(ColorImg, int, int)
	 * @see ColorImg#getPixel()
	 * @see ColorImg#getPixel(int, int)
	 * @since 1.0
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
	 * @param ColorImg the ColorImg this pixel corresponds to
	 * @param x coordinate
	 * @param y coordinate
	 * @see #Pixel(ColorImg, int)
	 * @see ColorImg#getPixel()
	 * @see ColorImg#getPixel(int, int)
	 * @since 1.0
	 */
	public ColorPixel(ColorImg img, int x, int y) {
		this(img, y*img.getWidth()+x);
	}

	/**
	 * @return the ColorImg this Pixel belongs to.
	 * @since 1.0
	 */
	public ColorImg getSource() {
		return img;
	}

	/**
	 * Sets the index of the ColorImg value this Pixel references.
	 * No bounds checks are performed.
	 * @param index corresponding to the position of the image's data array.
	 * @see #setPosition(int, int)
	 * @see #getIndex()
	 * @since 1.0
	 */
	public ColorPixel setIndex(int index) {
		this.index = index;
		return this;
	}

	/**
	 * Sets the position of the ColorImg value this Pixel references.
	 * No bounds checks are performed.
	 * @param x coordinate
	 * @param y coordinate
	 * @see #setIndex(int)
	 * @see #getX()
	 * @see #getY()
	 * @since 1.0
	 */
	public ColorPixel setPosition(int x, int y) {
		this.index = y*img.getWidth()+x;
		return this;
	}

	/**
	 * @return the index of the ColorImg value this Pixel references.
	 * @since 1.0
	 */
	public int getIndex() {
		return index;
	}

	/**
	 * @return the x coordinate of the position in the ColorImg this Pixel references.
	 * @see #getY()
	 * @see #getIndex()
	 * @see #setPosition(int, int)
	 * @since 1.0
	 */
	public int getX() {
		return index % img.getWidth();
	}

	/**
	 * @return the y coordinate of the position in the ColorImg this Pixel references.
	 * @see #getX()
	 * @see #getIndex()
	 * @see #setPosition(int, int)
	 * @since 1.0
	 */
	public int getY() {
		return index / img.getWidth();
	}

	/**
	 * Sets the value of the ColorImg at the position currently referenced by
	 * this Pixel.
	 * <p>
	 * If the position of this pixel is not in bounds of the ColorImg the value for
	 * a different position may be set or an ArrayIndexOutOfBoundsException
	 * may be thrown.
	 * @param pixelValue to be set e.g. 0xff0000ff for blue.
	 * @throws ArrayIndexOutOfBoundsException if this Pixel's index is not in
	 * range of the ColorImg's data array.
	 * @see #setARGB(int, int, int, int)
	 * @see #setRGB(int, int, int)
	 * @see #getValue()
	 * @see ColorImg#setValue(int, int, int)
	 * @since 1.0
	 */
	public ColorPixel setValue(int channel, double value){
		this.img.getData()[channel][index] = value;
		return this;
	}

	/**
	 * Gets the value of the ColorImg at the position currently referenced by
	 * this Pixel.
	 * <p>
	 * If the position of this pixel is not in bounds of the ColorImg the value for
	 * a different position may be returned or an ArrayIndexOutOfBoundsException
	 * may be thrown.
	 * @return the value of the ColorImg currently referenced by this Pixel.
	 * @throws ArrayIndexOutOfBoundsException if this Pixel's index is not in
	 * range of the ColorImg's data array.
	 * @see #a()
	 * @see #r()
	 * @see #g()
	 * @see #b()
	 * @see #setValue(int)
	 * @see ColorImg#getValue(int, int)
	 * @since 1.0
	 */
	public double getValue(int channel){
		return this.img.getData()[channel][index];
	}

	/**
	 * @return the alpha component of the value currently referenced by this
	 * Pixel. It is assumed that the value is an ARGB value with 8bits per
	 * color channel, so this will return a value in [0..255].
	 * @throws ArrayIndexOutOfBoundsException if this Pixel's index is not in
	 * range of the ColorImg's data array.
	 * @see #r()
	 * @see #g()
	 * @see #b()
	 * @see #setRGB(int, int, int)
	 * @see #setARGB(int, int, int, int)
	 * @see #getValue()
	 * @since 1.0
	 */
	public double a_asDouble(){
		return img.hasAlpha() ? this.img.getDataA()[index]:1;
	}

	/**
	 * @return the red component of the value currently referenced by this
	 * Pixel. It is assumed that the value is an ARGB value with 8bits per
	 * color channel, so this will return a value in [0..255].
	 * @throws ArrayIndexOutOfBoundsException if this Pixel's index is not in
	 * range of the ColorImg's data array.
	 * @see #a()
	 * @see #g()
	 * @see #b()
	 * @see #setRGB(int, int, int)
	 * @see #setARGB(int, int, int, int)
	 * @see #getValue()
	 * @since 1.0
	 */
	public double r_asDouble(){
		return this.img.getDataR()[index];
	}

	/**
	 * @return the green component of the value currently referenced by this
	 * Pixel. It is assumed that the value is an ARGB value with 8bits per
	 * color channel, so this will return a value in [0..255].
	 * @throws ArrayIndexOutOfBoundsException if this Pixel's index is not in
	 * range of the ColorImg's data array.
	 * @see #a()
	 * @see #r()
	 * @see #b()
	 * @see #setRGB(int, int, int)
	 * @see #setARGB(int, int, int, int)
	 * @see #getValue()
	 * @since 1.0
	 */
	public double g_asDouble(){
		return this.img.getDataG()[index];
	}

	/**
	 * @return the blue component of the value currently referenced by this
	 * Pixel. It is assumed that the value is an ARGB value with 8bits per
	 * color channel, so this will return a value in [0..255].
	 * @throws ArrayIndexOutOfBoundsException if this Pixel's index is not in
	 * range of the ColorImg's data array.
	 * @see #a()
	 * @see #r()
	 * @see #g()
	 * @see #setRGB(int, int, int)
	 * @see #setARGB(int, int, int, int)
	 * @see #getValue()
	 * @since 1.0
	 */
	public double b_asDouble(){
		return this.img.getDataB()[index];
	}

	/**
	 * Sets alpha channel value of this Pixel. Value will be truncated to
	 * 8bits (e.g. 0x12ff will truncate to 0xff).
	 * @param a alpha value in range [0..255]
	 * @throws ArrayIndexOutOfBoundsException if this Pixel's index is not in
	 * range of the ColorImg's data array.
	 * @see #setARGB(int a, int r, int g, int b)
	 * @since 1.2
	 */
	public ColorPixel setA_fromDouble(double a){
		if(img.hasAlpha())
			this.img.getDataA()[index] = a;
		return this;
	}

	/**
	 * Sets red channel value of this Pixel. Value will be truncated to
	 * 8bits (e.g. 0x12ff will truncate to 0xff).
	 * @param r red value in range [0..255]
	 * @throws ArrayIndexOutOfBoundsException if this Pixel's index is not in
	 * range of the ColorImg's data array.
	 * @see #setARGB(int a, int r, int g, int b)
	 * @see #setRGB(int r, int g, int b)
	 * @since 1.2
	 */
	public ColorPixel setR_fromDouble(double r){
		this.img.getDataR()[index] = r;
		return this;
	}

	/**
	 * Sets green channel value of this Pixel. Value will be truncated to
	 * 8bits (e.g. 0x12ff will truncate to 0xff).
	 * @param g green value in range [0..255]
	 * @throws ArrayIndexOutOfBoundsException if this Pixel's index is not in
	 * range of the ColorImg's data array.
	 * @see #setARGB(int a, int r, int g, int b)
	 * @see #setRGB(int r, int g, int b)
	 * @since 1.2
	 */
	public ColorPixel setG_fromDouble(double g){
		this.img.getDataG()[index] = g;
		return this;
	}

	/**
	 * Sets blue channel value of this Pixel. Value will be truncated to
	 * 8bits (e.g. 0x12ff will truncate to 0xff).
	 * @param b blue value in range [0..255]
	 * @throws ArrayIndexOutOfBoundsException if this Pixel's index is not in
	 * range of the ColorImg's data array.
	 * @see #setARGB(int a, int r, int g, int b)
	 * @see #setRGB(int r, int g, int b)
	 * @since 1.2
	 */
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
	public PixelBase setRGB_fromDouble_preserveAlpha(double r, double g, double b) {
		PixelBase.super.setRGB_fromDouble_preserveAlpha(r, g, b);
		return this;
	}

	/**
	 * @return 8bit luminance value of this pixel. <br>
	 * Using weights r=0.2126 g=0.7152 b=0.0722
	 * @throws ArrayIndexOutOfBoundsException if this Pixel's index is not in
	 * range of the ColorImg's data array.
	 * @see #getGrey(int, int, int)
	 * @see #getLuminance(int)
	 * @since 1.2
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
	 * @throws ArithmeticException divide by zero if the weights sum up to 0.
	 * @throws ArrayIndexOutOfBoundsException if this Pixel's index is not in
	 * range of the ColorImg's data array.
	 * @see #getLuminance()
	 * @see #getGrey(int, int, int, int)
	 * @since 1.2
	 */
	public double getGrey(final double redWeight, final double greenWeight, final double blueWeight){
		return ColorPixel.getGrey(r_asDouble(),g_asDouble(),b_asDouble(), redWeight, greenWeight, blueWeight);
	}

	@Override
	public String toString() {
		return asString();
	}

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

	public ColorPixel scaleToRange(double lowerLimit, double upperLimit){
		return convertRange(minValue(), maxValue(), lowerLimit, upperLimit);
	}

	public ColorPixel scaleToUnitRange(){
		return scaleToRange(0, 1);
	}

	public ColorPixel scale(double factor){
		setRGB_fromDouble_preserveAlpha(r_asDouble()*factor, g_asDouble()*factor, b_asDouble()*factor);
		return this;
	}

	public ColorPixel add(double r, double g, double b){
		setRGB_fromDouble_preserveAlpha(r+r_asDouble(), g+g_asDouble(), b+b_asDouble());
		return this;
	}

	public ColorPixel subtract(double r, double g, double b){
		return add(-r,-g,-b);
	}

	public ColorPixel cross(double r, double g, double b){
		setRGB_fromDouble_preserveAlpha(
				(g_asDouble()*b)-(g*b_asDouble()),
				(b_asDouble()*r)-(b*r_asDouble()),
				(r_asDouble()*g)-(r*g_asDouble()));
		return this;
	}

	public ColorPixel cross_(double r, double g, double b){
		setRGB_fromDouble_preserveAlpha(
				(g*b_asDouble())-(g_asDouble()*b),
				(b*r_asDouble())-(b_asDouble()*r),
				(r*g_asDouble())-(r_asDouble()*g));
		return this;
	}

	public double dot(double r, double g, double b){
		return getGrey(r, g, b);
	}

	public ColorPixel transform(
			double m00, double m01, double m02,
			double m10, double m11, double m12,
			double m20, double m21, double m22)
	{
		setRGB_fromDouble_preserveAlpha(
				dot(m00,m01,m02),
				dot(m10,m11,m12),
				dot(m20,m21,m22));
		return this;
	}

	public ColorPixel transform(double[][] m3x3){
		transform(
				m3x3[0][0],m3x3[0][1],m3x3[0][2],
				m3x3[1][0],m3x3[1][1],m3x3[1][2],
				m3x3[2][0],m3x3[2][1],m3x3[2][2]);
		return this;
	}



	public double getLenSquared(){
		return r_asDouble()*r_asDouble() + g_asDouble()*g_asDouble() + b_asDouble()*b_asDouble();
	}

	public double getLen(){
		return Math.sqrt(getLenSquared());
	}

	public ColorPixel normalize(){
		double len = getLen();
		if(len == 0.0)
			return this;
		double divByLen = 1/len;
		return setR_fromDouble(r_asDouble()*divByLen).setG_fromDouble(g_asDouble()*divByLen).setB_fromDouble(b_asDouble()*divByLen);
	}

	public int minChannel() {
		int c = 0;
		if(getValue(c) > getValue(1)) c=1;
		if(getValue(c) > getValue(2)) c=2;
		return c;
	}

	public int maxChannel() {
		int c = 0;
		if(getValue(c) < getValue(1)) c=1;
		if(getValue(c) < getValue(2)) c=2;
		return c;
	}

	public double minValue() {
		return getValue(minChannel());
	}

	public double maxValue() {
		return getValue(maxChannel());
	}


	/* * * * * * * * * */
	// STATIC  METHODS //
	/* * * * * * * * * */

	/**
	 * @param color RGB(24bit) or ARGB(32bit) value
	 * @return 8bit luminance value of given RGB value. <br>
	 * Using weights r=0.2126 g=0.7152 b=0.0722
	 * @see #getGrey(int, int, int, int)
	 * @since 1.0
	 */
	public static final double getLuminance(final double r, final double g, final double b){
		return getGrey(r,g,b, 0.2126, 0.7152, 0.0722);
	}

	/**
	 * Calculates a grey value from an RGB or ARGB value using specified
	 * weights for each R,G and B channel.
	 * <p>
	 * Weights are integer values so normalized weights need to be converted
	 * beforehand. E.g. normalized weights (0.33, 0.62, 0.05) would be have to
	 * be converted to integer weights (33, 62, 5).
	 * <p>
	 * When using weights with same signs, the value is within [0..255]. When
	 * weights have mixed signs the resulting value is unbounded.
	 * @param color RGB(24bit) or ARGB(32bit) value
	 * @param redWeight weight for red channel
	 * @param greenWeight weight for green channel
	 * @param blueWeight weight for blue channel
	 * @return weighted grey value (8bit) of RGB color value for non-negative weights.
	 * @throws ArithmeticException divide by zero if the weights sum up to 0.
	 * @see #getLuminance(int)
	 * @since 1.0
	 */
	public static final double getGrey(final double r, final double g, final double b, final double redWeight, final double greenWeight, final double blueWeight){
		return r*redWeight+g*greenWeight+b*blueWeight;
	}

}
