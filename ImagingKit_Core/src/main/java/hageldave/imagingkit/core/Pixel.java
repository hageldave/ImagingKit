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

package hageldave.imagingkit.core;

import static hageldave.imagingkit.core.util.ImagingKitUtils.*;

/**
 * Pixel class for retrieving a value from an {@link Img}.
 * A Pixel object stores a position and can be used to get and set values of
 * an Img. It is NOT the value and changing its position will not change the
 * image, instead it will reference a different value of the image as the
 * pixel object is a pointer to a value in the Img's data array.
 * <p>
 * The Pixel class also provides a set of static methods for color decomposition
 * and recombination from color channels like {@link #argb(int, int, int, int)}
 * or {@link #a(int)}, {@link #r(int)}, {@link #g(int)}, {@link #b(int)}.
 *
 * @author hageldave
 * @since 1.0
 */
public class Pixel implements PixelBase {
	/** Img this pixel belongs to
	 * @since 1.0 */
	private final Img img;

	/** index of the value this pixel references
	 * @since 1.0 */
	private int index;

	/**
	 * Creates a new Pixel object referencing the value
	 * of specified Img at specified index.
	 * <p>
	 * No bounds checks are performed for index.
	 * @param img the Img this pixel corresponds to
	 * @param index of the value in the images data array
	 * @see #Pixel(Img, int, int)
	 * @see Img#getPixel()
	 * @see Img#getPixel(int, int)
	 * @since 1.0
	 */
	public Pixel(Img img, int index) {
		this.img = img;
		this.index = index;
	}

	/**
	 * Creates a new Pixel object referencing the value
	 * of specified Img at specified position.
	 * <p>
	 * No bounds checks are performed for x and y
	 * @param img the Img this pixel corresponds to
	 * @param x coordinate
	 * @param y coordinate
	 * @see #Pixel(Img, int)
	 * @see Img#getPixel()
	 * @see Img#getPixel(int, int)
	 * @since 1.0
	 */
	public Pixel(Img img, int x, int y) {
		this(img, y*img.getWidth()+x);
	}

	/**
	 * @return the Img this Pixel belongs to.
	 * @since 1.0
	 */
	public Img getSource() {
		return img;
	}

	/**
	 * Sets the index of the Img value this Pixel references.
	 * No bounds checks are performed.
	 * @param index corresponding to the position of the image's data array.
	 * @see #setPosition(int, int)
	 * @see #getIndex()
	 * @since 1.0
	 */
	public Pixel setIndex(int index) {
		this.index = index;
		return this;
	}

	/**
	 * Sets the position of the Img value this Pixel references.
	 * No bounds checks are performed.
	 * @param x coordinate
	 * @param y coordinate
	 * @see #setIndex(int)
	 * @see #getX()
	 * @see #getY()
	 * @since 1.0
	 */
	public Pixel setPosition(int x, int y) {
		this.index = y*img.getWidth()+x;
		return this;
	}

	/**
	 * @return the index of the Img value this Pixel references.
	 * @since 1.0
	 */
	public int getIndex() {
		return index;
	}

	/**
	 * @return the x coordinate of the position in the Img this Pixel references.
	 * @see #getY()
	 * @see #getIndex()
	 * @see #setPosition(int, int)
	 * @since 1.0
	 */
	public int getX() {
		return index % img.getWidth();
	}

	/**
	 * @return the y coordinate of the position in the Img this Pixel references.
	 * @see #getX()
	 * @see #getIndex()
	 * @see #setPosition(int, int)
	 * @since 1.0
	 */
	public int getY() {
		return index / img.getWidth();
	}

	/**
	 * Sets the value of the Img at the position currently referenced by
	 * this Pixel.
	 * <p>
	 * If the position of this pixel is not in bounds of the Img the value for
	 * a different position may be set or an ArrayIndexOutOfBoundsException
	 * may be thrown.
	 * @param pixelValue to be set e.g. 0xff0000ff for blue.
	 * @throws ArrayIndexOutOfBoundsException if this Pixel's index is not in
	 * range of the Img's data array.
	 * @see #setARGB(int, int, int, int)
	 * @see #setRGB(int, int, int)
	 * @see #getValue()
	 * @see Img#setValue(int, int, int)
	 * @since 1.0
	 */
	public Pixel setValue(int pixelValue){
		this.img.getData()[index] = pixelValue;
		return this;
	}

	/**
	 * Gets the value of the Img at the position currently referenced by
	 * this Pixel.
	 * <p>
	 * If the position of this pixel is not in bounds of the Img the value for
	 * a different position may be returned or an ArrayIndexOutOfBoundsException
	 * may be thrown.
	 * @return the value of the Img currently referenced by this Pixel.
	 * @throws ArrayIndexOutOfBoundsException if this Pixel's index is not in
	 * range of the Img's data array.
	 * @see #a()
	 * @see #r()
	 * @see #g()
	 * @see #b()
	 * @see #setValue(int)
	 * @see Img#getValue(int, int)
	 * @since 1.0
	 */
	public int getValue(){
		return this.img.getData()[index];
	}

	/**
	 * @return the alpha component of the value currently referenced by this
	 * Pixel. It is assumed that the value is an ARGB value with 8bits per
	 * color channel, so this will return a value in [0..255].
	 * @throws ArrayIndexOutOfBoundsException if this Pixel's index is not in
	 * range of the Img's data array.
	 * @see #r()
	 * @see #g()
	 * @see #b()
	 * @see #setRGB(int, int, int)
	 * @see #setARGB(int, int, int, int)
	 * @see #getValue()
	 * @since 1.0
	 */
	public int a(){
		return Pixel.a(getValue());
	}

	/**
	 * @return the red component of the value currently referenced by this
	 * Pixel. It is assumed that the value is an ARGB value with 8bits per
	 * color channel, so this will return a value in [0..255].
	 * @throws ArrayIndexOutOfBoundsException if this Pixel's index is not in
	 * range of the Img's data array.
	 * @see #a()
	 * @see #g()
	 * @see #b()
	 * @see #setRGB(int, int, int)
	 * @see #setARGB(int, int, int, int)
	 * @see #getValue()
	 * @since 1.0
	 */
	public int r(){
		return Pixel.r(getValue());
	}

	/**
	 * @return the green component of the value currently referenced by this
	 * Pixel. It is assumed that the value is an ARGB value with 8bits per
	 * color channel, so this will return a value in [0..255].
	 * @throws ArrayIndexOutOfBoundsException if this Pixel's index is not in
	 * range of the Img's data array.
	 * @see #a()
	 * @see #r()
	 * @see #b()
	 * @see #setRGB(int, int, int)
	 * @see #setARGB(int, int, int, int)
	 * @see #getValue()
	 * @since 1.0
	 */
	public int g(){
		return Pixel.g(getValue());
	}

	/**
	 * @return the blue component of the value currently referenced by this
	 * Pixel. It is assumed that the value is an ARGB value with 8bits per
	 * color channel, so this will return a value in [0..255].
	 * @throws ArrayIndexOutOfBoundsException if this Pixel's index is not in
	 * range of the Img's data array.
	 * @see #a()
	 * @see #r()
	 * @see #g()
	 * @see #setRGB(int, int, int)
	 * @see #setARGB(int, int, int, int)
	 * @see #getValue()
	 * @since 1.0
	 */
	public int b(){
		return Pixel.b(getValue());
	}

	/**
	 * @return the normalized alpha component of the value currently referenced by this
	 * Pixel. This will return a value in [0.0 .. 1.0].
	 * @throws ArrayIndexOutOfBoundsException if this Pixel's index is not in
	 * range of the Img's data array.
	 *
	 * @see #a()
	 * @see #r_asDouble()
	 * @see #g_asDouble()
	 * @see #b_asDouble()
	 * @since 1.2
	 */
	public double a_asDouble(){
		return Pixel.a_normalized(getValue());
	}

	/**
	 * @return the normalized red component of the value currently referenced by this
	 * Pixel. This will return a value in [0.0 .. 1.0].
	 * @throws ArrayIndexOutOfBoundsException if this Pixel's index is not in
	 * range of the Img's data array.
	 *
	 * @see #r()
	 * @see #a_asDouble()
	 * @see #g_asDouble()
	 * @see #b_asDouble()
	 * @since 1.2
	 */
	public double r_asDouble(){
		return Pixel.r_normalized(getValue());
	}

	/**
	 * @return the normalized green component of the value currently referenced by this
	 * Pixel. This will return a value in [0.0 .. 1.0].
	 * @throws ArrayIndexOutOfBoundsException if this Pixel's index is not in
	 * range of the Img's data array.
	 *
	 * @see #g()
	 * @see #a_asDouble()
	 * @see #r_asDouble()
	 * @see #b_asDouble()
	 * @since 1.2
	 */
	public double g_asDouble(){
		return Pixel.g_normalized(getValue());
	}

	/**
	 * @return the normalized blue component of the value currently referenced by this
	 * Pixel. This will return a value in [0.0 .. 1.0].
	 * @throws ArrayIndexOutOfBoundsException if this Pixel's index is not in
	 * range of the Img's data array.
	 *
	 * @see #b()
	 * @see #a_asDouble()
	 * @see #r_asDouble()
	 * @see #g_asDouble()
	 * @since 1.2
	 */
	public double b_asDouble(){
		return Pixel.b_normalized(getValue());
	}

	/**
	 * Sets an ARGB value at the position currently referenced by this Pixel.
	 * Each channel value is assumed to be 8bit and otherwise truncated.
	 * @param a alpha
	 * @param r red
	 * @param g green
	 * @param b blue
	 * @throws ArrayIndexOutOfBoundsException if this Pixel's index is not in
	 * range of the Img's data array.
	 * @see #setRGB(int, int, int)
	 * @see #setRGB_preserveAlpha(int, int, int)
	 * @see #argb(int, int, int, int)
	 * @see #argb_bounded(int, int, int, int)
	 * @see #argb_fast(int, int, int, int)
	 * @see #setValue(int)
	 * @since 1.0
	 */
	public void setARGB(int a, int r, int g, int b){
		setValue(Pixel.argb(a, r, g, b));
	}

	/**
	 * Sets an opaque RGB value at the position currently referenced by this Pixel.
	 * Each channel value is assumed to be 8bit and otherwise truncated.
	 * @param r red
	 * @param g green
	 * @param b blue
	 * @throws ArrayIndexOutOfBoundsException if this Pixel's index is not in
	 * range of the Img's data array.
	 * @see #setARGB(int, int, int, int)
	 * @see #setRGB_preserveAlpha(int, int, int)
	 * @see #argb(int, int, int, int)
	 * @see #argb_bounded(int, int, int, int)
	 * @see #argb_fast(int, int, int, int)
	 * @see #setValue(int)
	 * @since 1.0
	 */
	public void setRGB(int r, int g, int b){
		setValue(Pixel.rgb(r, g, b));
	}

	/**
	 * Sets an RGB value at the position currently referenced by this Pixel.
	 * The present alpha value will not be altered by this operation.
	 * Each channel value is assumed to be 8bit and otherwise truncated.
	 * @param r red
	 * @param g green
	 * @param b blue
	 * @throws ArrayIndexOutOfBoundsException if this Pixel's index is not in
	 * range of the Img's data array.
	 * @see #setRGB_fromDouble_preserveAlpha(double, double, double)
	 * @since 1.2
	 */
	public void setRGB_preserveAlpha(int r, int g, int b){
		setValue((getValue() & 0xff000000 ) | Pixel.argb(0, r, g, b));
	}

	/**
	 * Sets an ARGB value at the position currently referenced by this Pixel. <br>
	 * Each channel value is assumed to be within [0.0 .. 1.0]. Channel values
	 * outside these bounds will be clamped to them.
	 * @param a normalized alpha
	 * @param r normalized red
	 * @param g normalized green
	 * @param b normalized blue
	 * @throws ArrayIndexOutOfBoundsException if this Pixel's index is not in
	 * range of the Img's data array.
	 *
	 * @see #setRGB_fromDouble(double, double, double)
	 * @see #setRGB_fromDouble_preserveAlpha(double, double, double)
	 * @see #setARGB(int, int, int, int)
	 * @see #a_asDouble()
	 * @see #r_asDouble()
	 * @see #g_asDouble()
	 * @see #b_asDouble()
	 * @since 1.2
	 */
	public Pixel setARGB_fromDouble(double a, double r, double g, double b){
		return setValue(Pixel.argb_fromNormalized(a, r, g, b));
	}

	/**
	 * Sets an opaque RGB value at the position currently referenced by this Pixel. <br>
	 * Each channel value is assumed to be within [0.0 .. 1.0]. Channel values
	 * outside these bounds will be clamped to them.
	 * @param r normalized red
	 * @param g normalized green
	 * @param b normalized blue
	 * @throws ArrayIndexOutOfBoundsException if this Pixel's index is not in
	 * range of the Img's data array.
	 *
	 * @see #setARGB_fromDouble(double, double, double, double)
	 * @see #setRGB_fromDouble_preserveAlpha(double, double, double)
	 * @see #setRGB(int, int, int)
	 * @see #a_asDouble()
	 * @see #r_asDouble()
	 * @see #g_asDouble()
	 * @see #b_asDouble()
	 * @since 1.2
	 */
	public Pixel setRGB_fromDouble(double r, double g, double b){
		return setValue(Pixel.rgb_fromNormalized(r, g, b));
	}

	/**
	 * Sets an RGB value at the position currently referenced by this Pixel.
	 * The present alpha value will not be altered by this operation. <br>
	 * Each channel value is assumed to be within [0.0 .. 1.0]. Channel values
	 * outside these bounds will be clamped to them.
	 * @param r normalized red
	 * @param g normalized green
	 * @param b normalized blue
	 * @throws ArrayIndexOutOfBoundsException if this Pixel's index is not in
	 * range of the Img's data array.
	 *
	 * @see #setRGB_preserveAlpha(int, int, int)
	 * @since 1.2
	 */
	public Pixel setRGB_fromDouble_preserveAlpha(double r, double g, double b){
		return setValue((getValue() & 0xff000000) | (0x00ffffff & Pixel.rgb_fromNormalized(r, g, b)));
	}

	/**
	 * Sets alpha channel value of this Pixel. Value will be truncated to
	 * 8bits (e.g. 0x12ff will truncate to 0xff).
	 * @param a alpha value in range [0..255]
	 * @throws ArrayIndexOutOfBoundsException if this Pixel's index is not in
	 * range of the Img's data array.
	 * @see #setARGB(int a, int r, int g, int b)
	 * @since 1.2
	 */
	public Pixel setA(int a){
		return setValue((getValue() & 0x00ffffff) | ((a<<24) & 0xff000000));
	}

	/**
	 * Sets red channel value of this Pixel. Value will be truncated to
	 * 8bits (e.g. 0x12ff will truncate to 0xff).
	 * @param r red value in range [0..255]
	 * @throws ArrayIndexOutOfBoundsException if this Pixel's index is not in
	 * range of the Img's data array.
	 * @see #setARGB(int a, int r, int g, int b)
	 * @see #setRGB(int r, int g, int b)
	 * @since 1.2
	 */
	public Pixel setR(int r){
		return setValue((getValue() & 0xff00ffff) | ((r<<16) & 0x00ff0000));
	}

	/**
	 * Sets green channel value of this Pixel. Value will be truncated to
	 * 8bits (e.g. 0x12ff will truncate to 0xff).
	 * @param g green value in range [0..255]
	 * @throws ArrayIndexOutOfBoundsException if this Pixel's index is not in
	 * range of the Img's data array.
	 * @see #setARGB(int a, int r, int g, int b)
	 * @see #setRGB(int r, int g, int b)
	 * @since 1.2
	 */
	public Pixel setG(int g){
		return setValue((getValue() & 0xffff00ff) | ((g<<8) & 0x0000ff00));
	}

	/**
	 * Sets blue channel value of this Pixel. Value will be truncated to
	 * 8bits (e.g. 0x12ff will truncate to 0xff).
	 * @param b blue value in range [0..255]
	 * @throws ArrayIndexOutOfBoundsException if this Pixel's index is not in
	 * range of the Img's data array.
	 * @see #setARGB(int a, int r, int g, int b)
	 * @see #setRGB(int r, int g, int b)
	 * @since 1.2
	 */
	public Pixel setB(int b){
		return setValue((getValue() & 0xffffff00) | ((b) & 0x000000ff));
	}

	@Override
	public PixelBase setA_fromDouble(double a) {
		return setA(clamp_0_255((int)Math.round(a*0xff)));
	}

	@Override
	public PixelBase setR_fromDouble(double r) {
		return setR(clamp_0_255((int)Math.round(r*0xff)));
	}

	@Override
	public PixelBase setG_fromDouble(double g) {
		return setG(clamp_0_255((int)Math.round(g*0xff)));
	}

	@Override
	public PixelBase setB_fromDouble(double b) {
		return setB(clamp_0_255((int)Math.round(b*0xff)));
	}

	/**
	 * @return 8bit luminance value of this pixel. <br>
	 * Using weights r=0.2126 g=0.7152 b=0.0722
	 * @throws ArrayIndexOutOfBoundsException if this Pixel's index is not in
	 * range of the Img's data array.
	 * @see #getGrey(int, int, int)
	 * @see #getLuminance(int)
	 * @since 1.2
	 */
	public int getLuminance(){
		return Pixel.getLuminance(getValue());
	}

	/**
	 * Calculates the grey value of this pixel using specified weights.
	 * @param redWeight weight for red channel
	 * @param greenWeight weight for green channel
	 * @param blueWeight weight for blue channel
	 * @return grey value of pixel for specified weights
	 * @throws ArithmeticException divide by zero if the weights sum up to 0.
	 * @throws ArrayIndexOutOfBoundsException if this Pixel's index is not in
	 * range of the Img's data array.
	 * @see #getLuminance()
	 * @see #getGrey(int, int, int, int)
	 * @since 1.2
	 */
	public int getGrey(final int redWeight, final int greenWeight, final int blueWeight){
		return Pixel.getGrey(getValue(), redWeight, greenWeight, blueWeight);
	}

	@Override
	public String toString() {
		return asString();
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
	public static final int getLuminance(final int color){
		return getGrey(color, 2126, 7152, 722);
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
	public static final int getGrey(final int color, final int redWeight, final int greenWeight, final int blueWeight){
		return (r(color)*redWeight + g(color)*greenWeight + b(color)*blueWeight)/(redWeight+blueWeight+greenWeight);
	}

	/**
	 * Packs 8bit RGB color components into a single 32bit ARGB integer value
	 * with alpha=255 (opaque).
	 * Components are clamped to [0,255].
	 * @param r red
	 * @param g green
	 * @param b blue
	 * @return packed ARGB value
	 *
	 * @see #argb(int, int, int, int)
	 * @see #argb_bounded(int, int, int, int)
	 * @see #argb_fast(int, int, int, int)
	 * @see #rgb(int, int, int)
	 * @see #rgb_fast(int, int, int)
	 * @see #a(int)
	 * @see #r(int)
	 * @see #g(int)
	 * @see #b(int)
	 * @since 1.0
	 */
	public static final int rgb_bounded(final int r, final int g, final int b){
		return rgb_fast(
				r > 255 ? 255: r < 0 ? 0:r,
				g > 255 ? 255: g < 0 ? 0:g,
				b > 255 ? 255: b < 0 ? 0:b);
	}

	/**
	 * Packs 8bit RGB color components into a single 32bit ARGB integer value
	 * with alpha=255 (opaque).
	 * Components larger than 8bit get truncated to 8bit.
	 * @param r red
	 * @param g green
	 * @param b blue
	 * @return packed ARGB value
	 *
	 * @see #argb(int, int, int, int)
	 * @see #argb_bounded(int, int, int, int)
	 * @see #argb_fast(int, int, int, int)
	 * @see #rgb_bounded(int, int, int)
	 * @see #rgb_fast(int, int, int)
	 * @see #a(int)
	 * @see #r(int)
	 * @see #g(int)
	 * @see #b(int)
	 * @since 1.0
	 */
	public static final int rgb(final int r, final int g, final int b){
		return rgb_fast(r & 0xff, g & 0xff, b & 0xff);
	}

	/**
	 * Packs 8bit RGB color components into a single 32bit ARGB integer value
	 * with alpha=255 (opaque).
	 * Components larger than 8bit are NOT truncated and will result in a
	 * broken, malformed value.
	 * @param r red
	 * @param g green
	 * @param b blue
	 * @return packed ARGB value
	 *
	 * @see #argb(int, int, int, int)
	 * @see #argb_bounded(int, int, int, int)
	 * @see #argb_fast(int, int, int, int)
	 * @see #rgb_bounded(int, int, int)
	 * @see #rgb(int, int, int)
	 * @see #a(int)
	 * @see #r(int)
	 * @see #g(int)
	 * @see #b(int)
	 * @since 1.0
	 */
	public static final int rgb_fast(final int r, final int g, final int b){
		return 0xff000000|(r<<16)|(g<<8)|b;
	}

	/**
	 * Packs normalized ARGB color components (values in [0.0 .. 1.0]) into a
	 * single 32bit integer value with alpha=255 (opaque).
	 * Component values less than 0 or greater than 1 clamped to fit the range.
	 * @param r red
	 * @param g green
	 * @param b blue
	 * @return packed ARGB value
	 *
	 * @see #argb_fromNormalized(double, double, double, double)
	 * @see #rgb(int, int, int)
	 * @see #a_normalized(int)
	 * @see #r_normalized(int)
	 * @see #g_normalized(int)
	 * @see #b_normalized(int)
	 * @since 1.2
	 */
	public static final int rgb_fromNormalized(final double r, final double g, final double b){
		return rgb_bounded((int)Math.round(r*0xff), (int)Math.round(g*0xff), (int)Math.round(b*0xff));
	}

	/**
	 * Packs 8bit ARGB color components into a single 32bit integer value.
	 * Components are clamped to [0,255].
	 * @param a alpha
	 * @param r red
	 * @param g green
	 * @param b blue
	 * @return packed ARGB value
	 *
	 * @see #argb(int, int, int, int)
	 * @see #argb_fast(int, int, int, int)
	 * @see #rgb_bounded(int, int, int)
	 * @see #rgb(int, int, int)
	 * @see #rgb_fast(int, int, int)
	 * @see #a(int)
	 * @see #r(int)
	 * @see #g(int)
	 * @see #b(int)
	 * @since 1.0
	 */
	public static final int argb_bounded(final int a, final int r, final int g, final int b){
		return argb_fast(
				a > 255 ? 255: a < 0 ? 0:a,
				r > 255 ? 255: r < 0 ? 0:r,
				g > 255 ? 255: g < 0 ? 0:g,
				b > 255 ? 255: b < 0 ? 0:b);
	}

	/**
	 * Packs 8bit ARGB color components into a single 32bit integer value.
	 * Components larger than 8bit get truncated to 8bit.
	 * @param a alpha
	 * @param r red
	 * @param g green
	 * @param b blue
	 * @return packed ARGB value
	 *
	 * @see #argb_bounded(int, int, int, int)
	 * @see #argb_fast(int, int, int, int)
	 * @see #rgb_bounded(int, int, int)
	 * @see #rgb(int, int, int)
	 * @see #rgb_fast(int, int, int)
	 * @see #a(int)
	 * @see #r(int)
	 * @see #g(int)
	 * @see #b(int)
	 * @since 1.0
	 */
	public static final int argb(final int a, final int r, final int g, final int b){
		return argb_fast(a & 0xff, r & 0xff, g & 0xff, b & 0xff);
	}

	/**
	 * Packs 8bit ARGB color components into a single 32bit integer value.
	 * Components larger than 8bit are NOT truncated and will result in a
	 * broken, malformed value.
	 * @param a alpha
	 * @param r red
	 * @param g green
	 * @param b blue
	 * @return packed ARGB value
	 *
	 * @see #argb(int, int, int, int)
	 * @see #argb_bounded(int, int, int, int)
	 * @see #rgb_bounded(int, int, int)
	 * @see #rgb(int, int, int)
	 * @see #rgb_fast(int, int, int)
	 * @see #a(int)
	 * @see #r(int)
	 * @see #g(int)
	 * @see #b(int)
	 * @since 1.0
	 */
	public static final int argb_fast(final int a, final int r, final int g, final int b){
		return (a<<24)|(r<<16)|(g<<8)|b;
	}

	/**
	 * Packs normalized ARGB color components (values in [0.0 .. 1.0]) into a
	 * single 32bit integer value.
	 * Component values less than 0 or greater than 1 are clamped to fit the range.
	 * @param a alpha
	 * @param r red
	 * @param g green
	 * @param b blue
	 * @return packed ARGB value
	 *
	 * @see #rgb_fromNormalized(double, double, double)
	 * @see #argb(int, int, int, int)
	 * @see #a_normalized(int)
	 * @see #r_normalized(int)
	 * @see #g_normalized(int)
	 * @see #b_normalized(int)
	 * @since 1.2
	 */
	public static final int argb_fromNormalized(final double a, final double r, final double g, final double b){
		return argb_bounded((int)Math.round(a*0xff), (int)Math.round(r*0xff), (int)Math.round(g*0xff), (int)Math.round(b*0xff));
	}

	/**
	 * @param color ARGB(32bit) or RGB(24bit) value
	 * @return blue component(8bit) of specified color.
	 * @see #a(int)
	 * @see #r(int)
	 * @see #g(int)
	 * @see #argb(int, int, int, int)
	 * @see #rgb(int, int, int)
	 * @since 1.0
	 */
	public static final int b(final int color){
		return (color) & 0xff;
	}

	/**
	 * @param color ARGB(32bit) or RGB(24bit) value
	 * @return green component(8bit) of specified color.
	 * @see #a(int)
	 * @see #r(int)
	 * @see #b(int)
	 * @see #argb(int, int, int, int)
	 * @see #rgb(int, int, int)
	 * @since 1.0
	 */
	public static final int g(final int color){
		return (color >> 8) & 0xff;
	}

	/**
	 * @param color ARGB(32bit) or RGB(24bit) value
	 * @return red component(8bit) of specified color.
	 * @see #a(int)
	 * @see #g(int)
	 * @see #b(int)
	 * @see #argb(int, int, int, int)
	 * @see #rgb(int, int, int)
	 * @since 1.0
	 */
	public static final int r(final int color){
		return (color >> 16) & 0xff;
	}

	/**
	 * @param color ARGB(32bit) value
	 * @return alpha component(8bit) of specified color.
	 * @see #r(int)
	 * @see #g(int)
	 * @see #b(int)
	 * @see #argb(int, int, int, int)
	 * @see #rgb(int, int, int)
	 * @since 1.0
	 */
	public static final int a(final int color){
		return (color >> 24) & 0xff;
	}

	/**
	 * @param color ARGB(32bit) or RGB(24bit) value
	 * @return normalized blue component of specified color <br>
	 * (value in [0.0 .. 1.0]).
	 * @see #b()
	 * @see #r_normalized(int)
	 * @see #g_normalized(int)
	 * @see #a_normalized(int)
	 * @since 1.2
	 */
	public static final double b_normalized(final int color){
		return b(color)/255.0;
	}

	/**
	 * @param color ARGB(32bit) or RGB(24bit) value
	 * @return normalized green component of specified color <br>
	 * (value in [0.0 .. 1.0]).
	 * @see #g()
	 * @see #r_normalized(int)
	 * @see #b_normalized(int)
	 * @see #a_normalized(int)
	 * @since 1.2
	 */
	public static final double g_normalized(final int color){
		return g(color)/255.0;
	}

	/**
	 * @param color ARGB(32bit) or RGB(24bit) value
	 * @return normalized red component of specified color <br>
	 * (value in [0.0 .. 1.0]).
	 * @see #r()
	 * @see #b_normalized(int)
	 * @see #g_normalized(int)
	 * @see #a_normalized(int)
	 * @since 1.2
	 */
	public static final double r_normalized(final int color){
		return r(color)/255.0;
	}

	/**
	 * @param color ARGB(32bit) value
	 * @return normalized alpha component of specified color <br>
	 * (value in [0.0 .. 1.0]).
	 * @see #a()
	 * @see #r_normalized(int)
	 * @see #g_normalized(int)
	 * @see #a_normalized(int)
	 * @since 1.2
	 */
	public static final double a_normalized(final int color){
		return a(color)/255.0;
	}

}
