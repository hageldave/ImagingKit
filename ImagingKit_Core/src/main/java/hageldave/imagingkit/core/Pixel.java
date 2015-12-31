package hageldave.imagingkit.core;

/**
 * Pixel class for retrieving a value from an {@link Img}.
 * A Pixel object stores a position and can be used to get and set values of 
 * an Img. It is NOT the value and changing its position will not change the 
 * image, instead it will reference a different value of the image as the 
 * pixel object is a pointer to a value in the Imgs data array.
 * <p>
 * The Pixel class also provides a set of static methods for color decomposition
 * and recombination from color channels like {@link #argb(int, int, int, int)}
 * or {@link #a(int)}, {@link #r(int)}, {@link #g(int)}, {@link #b(int)}.
 * 
 * @author hageldave
 */
public class Pixel {
	/** Img this pixel belongs to */
	private final Img img;
	
	/** index of the value this pixel references */
	private int index;
	
	/**
	 * Creates a new Pixel object referencing the value
	 * of specified Img at specified index.
	 * <p>
	 * No bounds checks are performed for index.
	 * @param img
	 * @param index of the value in the images data array
	 * @see #Pixel(Img, int, int)
	 * @see Img#getPixel()
	 * @see Img#getPixel(int, int)
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
	 * @param img
	 * @param x
	 * @param y
	 * @see #Pixel(Img, int)
	 * @see Img#getPixel()
	 * @see Img#getPixel(int, int)
	 */
	public Pixel(Img img, int x, int y) {
		this(img, y*img.getWidth()+x);
	}
	
	/**
	 * @return the Img this Pixel belongs to.
	 */
	public Img getImg() {
		return img;
	}
	
	/**
	 * Sets the index of the Img value this Pixel references.
	 * No bounds checks are performed.
	 * @param index
	 * @see #setPosition(int, int)
	 * @see #getIndex()
	 */
	public void setIndex(int index) {
		this.index = index;
	}
	
	/**
	 * Sets the position of the Img value this Pixel references.
	 * No bounds checks are performed.
	 * @param x
	 * @param y
	 * @see #setIndex(int)
	 * @see #getX()
	 * @see #getY()
	 */
	public void setPosition(int x, int y) {
		this.index = y*img.getWidth()+x;
	}
	
	/**
	 * @return the index of the Img value this Pixel references.
	 */
	public int getIndex() {
		return index;
	}
	
	/**
	 * @return the x coordinate of the position in the Img this Pixel references.
	 * @see #getY()
	 * @see #getIndex()
	 * @see #setPosition(int, int)
	 */
	public int getX() {
		return index % img.getWidth();
	}
	
	/**
	 * @return the y coordinate of the position in the Img this Pixel references.
	 * @see #getX()
	 * @see #getIndex()
	 * @see #setPosition(int, int)
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
	 * @param pixelValue
	 * @throws ArrayIndexOutOfBoundsException if this Pixels index is not in 
	 * range of the Imgs data array.
	 * @see #setARGB(int, int, int, int)
	 * @see #setRGB(int, int, int)
	 * @see #getValue()
	 * @see Img#setValue(int, int, int)
	 */
	public void setValue(int pixelValue){
		this.img.getData()[index] = pixelValue;
	}
	
	/**
	 * Gets the value of the Img at the position currently referenced by
	 * this Pixel.
	 * <p>
	 * If the position of this pixel is not in bounds of the Img the value for 
	 * a different position may be returned or an ArrayIndexOutOfBoundsException 
	 * may be thrown.
	 * @return the value of the Img currently referenced by this Pixel.
	 * @throws ArrayIndexOutOfBoundsException if this Pixels index is not in 
	 * range of the Imgs data array.
	 * @see #a()
	 * @see #r()
	 * @see #g()
	 * @see #b()
	 * @see #setValue(int)
	 * @see Img#getValue(int, int)
	 */
	public int getValue(){
		return this.img.getData()[index];
	}
	
	/**
	 * @return the alpha component of the value currently referenced by this
	 * Pixel. It is assumed that the value is an ARGB value with 8bits per
	 * color channel.
	 * @throws ArrayIndexOutOfBoundsException if this Pixels index is not in 
	 * range of the Imgs data array.
	 * @see #r()
	 * @see #g()
	 * @see #b()
	 * @see #setRGB(int, int, int)
	 * @see #setARGB(int, int, int, int)
	 * @see #getValue()
	 */
	public int a(){
		return Pixel.a(getValue());
	}
	
	/**
	 * @return the red component of the value currently referenced by this
	 * Pixel. It is assumed that the value is an ARGB value with 8bits per
	 * color channel.
	 * @throws ArrayIndexOutOfBoundsException if this Pixels index is not in 
	 * range of the Imgs data array.
	 * @see #a()
	 * @see #g()
	 * @see #b()
	 * @see #setRGB(int, int, int)
	 * @see #setARGB(int, int, int, int)
	 * @see #getValue()
	 */
	public int r(){
		return Pixel.r(getValue());
	}
	
	/**
	 * @return the green component of the value currently referenced by this
	 * Pixel. It is assumed that the value is an ARGB value with 8bits per
	 * color channel.
	 * @throws ArrayIndexOutOfBoundsException if this Pixels index is not in 
	 * range of the Imgs data array.
	 * @see #a()
	 * @see #r()
	 * @see #b()
	 * @see #setRGB(int, int, int)
	 * @see #setARGB(int, int, int, int)
	 * @see #getValue()
	 */
	public int g(){
		return Pixel.g(getValue());
	}
	
	/**
	 * @return the blue component of the value currently referenced by this
	 * Pixel. It is assumed that the value is an ARGB value with 8bits per
	 * color channel.
	 * @throws ArrayIndexOutOfBoundsException if this Pixels index is not in 
	 * range of the Imgs data array.
	 * @see #a()
	 * @see #r()
	 * @see #g()
	 * @see #setRGB(int, int, int)
	 * @see #setARGB(int, int, int, int)
	 * @see #getValue()
	 */
	public int b(){
		return Pixel.b(getValue());
	}
	
	/**
	 * Sets an ARGB value at the position currently referenced by this Pixel.
	 * Each channel value is assumed to be 8bit and otherwise truncated.
	 * @param a alpha
	 * @param r red
	 * @param g green
	 * @param b blue
	 * @throws ArrayIndexOutOfBoundsException if this Pixels index is not in 
	 * range of the Imgs data array.
	 * @see #setRGB(int, int, int)
	 * @see #argb(int, int, int, int)
	 * @see #argb_bounded(int, int, int, int)
	 * @see #argb_fast(int, int, int, int)
	 * @see #setValue(int)
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
	 * @throws ArrayIndexOutOfBoundsException if this Pixels index is not in 
	 * range of the Imgs data array.
	 * @see #setARGB(int, int, int, int)
	 * @see #argb(int, int, int, int)
	 * @see #argb_bounded(int, int, int, int)
	 * @see #argb_fast(int, int, int, int)
	 * @see #setValue(int)
	 */
	public void setRGB(int r, int g, int b){
		setValue(Pixel.rgb(r, g, b));
	}
	

	/* * * * * * * * * */
	// STATIC  METHODS //
	/* * * * * * * * * */
	
	/**
	 * @param color RGB(24bit) or ARGB(32bit) value 
	 * @return 8bit luminance value of given RGB value. Using weights r=0.2126 g=0.7152
	 * b=0.0722
	 * @see #getGrey(int, int, int, int)
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
	 * @param color RGB(24bit) or ARGB(32bit) value 
	 * @param redWeight
	 * @param greenWeight
	 * @param blueWeight
	 * @return weighted grey value (8bit) of RGB color value.
	 * @throws ArithmeticException divide by zero if the weights sum up to 0.
	 * @see #getLuminance(int)
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
	 * @see {@link #a(int)}, {@link #r(int)}, {@link #g(int)}, {@link #b(int)}
	 */
	public static final int rgb_bounded(final int r, final int g, final int b){
		return argb_bounded(0xff, r, g, b);
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
	 * @see {@link #a(int)}, {@link #r(int)}, {@link #g(int)}, {@link #b(int)}
	 */
	public static final int rgb(final int r, final int g, final int b){
		return argb(0xff, r, g, b);
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
	 * @see {@link #a(int)}, {@link #r(int)}, {@link #g(int)}, {@link #b(int)}
	 */
	public static final int rgb_fast(final int r, final int g, final int b){
		return argb_fast(0xff, r, g, b);
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
	 * @see {@link #a(int)}, {@link #r(int)}, {@link #g(int)}, {@link #b(int)}
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
	 * @see {@link #a(int)}, {@link #r(int)}, {@link #g(int)}, {@link #b(int)}
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
	 * @see {@link #a(int)}, {@link #r(int)}, {@link #g(int)}, {@link #b(int)}
	 */
	public static final int argb_fast(final int a, final int r, final int g, final int b){
		return (a<<24)|(r<<16)|(g<<8)|b;
	}

	/**
	 * @param color ARGB(32bit) or RGB(24bit) value
	 * @return blue component(8bit) of specified color.
	 * @see #a(int)
	 * @see #r(int)
	 * @see #g(int)
	 * @see #argb(int, int, int, int)
	 * @see #rgb(int, int, int)
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
	 */
	public static final int r(final int color){
		return (color >> 16) & 0xff;
	}

	/**
	 * @param color ARGB(32bit) or RGB(24bit) value
	 * @return alpha component(8bit) of specified color.
	 * @see #r(int)
	 * @see #g(int)
	 * @see #b(int)
	 * @see #argb(int, int, int, int)
	 * @see #rgb(int, int, int)
	 */
	public static final int a(final int color){
		return (color >> 24) & 0xff;
	}

	/**
	 * Generalized channel packing method similar to {@link #argb(int, int, int, int)}
	 * but for arbitrary channel sizes and number of channels. 
	 * This method calculates the bitwise OR concatenation of all channels with
	 * the last channel occupying the least significant bits of the result and 
	 * former channels the following bits so that there wont be any collisions. 
	 * Each channel is assumed to be in the specified number of bits. <br>
	 * E.g. ARGB would be realised like this: <code>combineCh(8,a,r,g,b)</code>
	 * or 30bit YCbCr could be realized like this: <code>combineCh(10,y,cb,cr)</code>
	 * <p>
	 * From a performance point of view this method is not optimal. A custom
	 * method tailored to the specific packing task will certainly be superior.
	 * 
	 * @param bitsPerChannel
	 * @param channels
	 * @return packed channel values
	 * @see #ch(int, int, int)
	 */
	public static final int combineCh(int bitsPerChannel, int ... channels){
		int result = 0;
		int startBit = 0;
		for(int i = channels.length-1; i >= 0; i--){
			result |= channels[i] << startBit;
			startBit += bitsPerChannel;
		}
		return result;
	}

	/**
	 * Extracts a channel value of arbitrary bitsize and bit position
	 * from an integer color value. This method bit shifts the requested
	 * channel area to the least significant bits and truncates the resulting
	 * value to match the number of bits of the channel area. <br>
	 * E.g. blue from ARGB would be realised like this: <code>ch(argb, 0, 8)</code>
	 * red would be: <code>ch(argb, 16, 8)</code>
	 * <p>
	 * From a performance point of view this method is not optimal. A custom
	 * method tailored to the specific extraction task will probably be superior.
	 * 
	 * @param color
	 * @param startBit
	 * @param numBits
	 * @return channel value
	 * @see #combineCh(int, int...) combineCh(int, int...)
	 */
	public static final int ch(final int color, final int startBit, final int numBits){
		return (color >> startBit) & ((1 << numBits)-1);
	}
}
