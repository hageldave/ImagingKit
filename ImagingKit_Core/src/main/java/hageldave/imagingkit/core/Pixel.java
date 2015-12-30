package hageldave.imagingkit.core;

public class Pixel {
	private final Img img;
	private int index;
	
	public Pixel(Img img, int index) {
		this.img = img;
		this.index = index;
	}
	
	public Pixel(Img img, int x, int y) {
		this(img, y*img.getWidth()+x);
	}
	
	public Img getImg() {
		return img;
	}
	
	public void setIndex(int index) {
		this.index = index;
	}
	
	public void setPosition(int x, int y) {
		this.index = y*img.getWidth()+x;
	}
	
	public int getIndex() {
		return index;
	}
	
	public int getX() {
		return index % img.getWidth();
	}
	
	public int getY() {
		return index / img.getWidth();
	}
	
	public void setValue(int pixelValue){
		this.img.getData()[index] = pixelValue;
	}
	
	public int getValue(){
		return this.img.getData()[index];
	}
	
	public int a(){
		return Pixel.a(getValue());
	}
	
	public int r(){
		return Pixel.r(getValue());
	}
	
	public int g(){
		return Pixel.g(getValue());
	}
	
	public int b(){
		return Pixel.b(getValue());
	}
	
	public void setARGB(int a, int r, int g, int b){
		setValue(Pixel.argb(a, r, g, b));
	}
	
	public void setRGB(int r, int g, int b){
		setValue(Pixel.rgb(r, g, b));
	}
	

	/* * * * * * * * * */
	// STATIC  METHODS //
	/* * * * * * * * * */
	
	public static final int getLuminance(final int color){
		return getGrey(color, 2126, 7152, 722);
	}

	public static final int getGrey(final int color, final int redWeight, final int greenWeight, final int blueWeight){
		return (r(color)*redWeight + g(color)*greenWeight + b(color)*blueWeight)/(redWeight+blueWeight+greenWeight);
	}

	public static final int rgb_bounded(final int r, final int g, final int b){
		return argb_bounded(0xff, r, g, b);
	}

	public static final int rgb(final int r, final int g, final int b){
		return argb(0xff, r, g, b);
	}

	public static final int rgb_fast(final int r, final int g, final int b){
		return argb_fast(0xff, r, g, b);
	}

	public static final int argb_bounded(final int a, final int r, final int g, final int b){
		return argb_fast(
				a > 255 ? 255: a < 0 ? 0:a, 
				r > 255 ? 255: r < 0 ? 0:r, 
				g > 255 ? 255: g < 0 ? 0:g,
				b > 255 ? 255: b < 0 ? 0:b);
	}

	public static final int argb(final int a, final int r, final int g, final int b){
		return argb_fast(a & 0xff, r & 0xff, g & 0xff, b & 0xff);
	}

	public static final int argb_fast(final int a, final int r, final int g, final int b){
		return (a<<24)|(r<<16)|(g<<8)|b;
	}

	public static final int b(final int color){
		return (color) & 0xff;
	}

	public static final int g(final int color){
		return (color >> 8) & 0xff;
	}

	public static final int r(final int color){
		return (color >> 16) & 0xff;
	}

	public static final int a(final int color){
		return (color >> 24) & 0xff;
	}

	public static final int combineCh(int bitsPerChannel, int ... channels){
		int result = 0;
		int startBit = 0;
		for(int i = channels.length-1; i >= 0; i--){
			result |= channels[i] << startBit;
			startBit += bitsPerChannel;
		}
		return result;
	}

	public static final int ch(final int color, final int startBit, final int numBits){
		return (color >> startBit) & ((1 << numBits)-1);
	}
}
