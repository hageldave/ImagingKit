package hageldave.imagingkit.core;

import java.awt.Dimension;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferInt;
import java.util.Arrays;


public class Img {

	public static final int boundary_mode_zero = 0;
	public static final int boundary_mode_repeat_edge = -1;
	public static final int boundary_mode_repeat_image = -2;
	public static final int boundary_mode_mirror = -3;
	
	
	private int[] data;
	private Dimension dimension;
	
	public Img(int width, int height){
		this(new Dimension(width, height));
	}
	
	public Img(Dimension dimension){
		this.data = new int[dimension.width*dimension.height];
		this.dimension = dimension;
	}
	
	public Img(BufferedImage img){
		int type = img.getType();
		if(type != BufferedImage.TYPE_INT_ARGB || type != BufferedImage.TYPE_INT_RGB){
			throw new IllegalArgumentException("cannot use BufferedImage with type other than INT_ARGB or INT_RGB");
		}
		this.dimension = new Dimension(img.getWidth(),img.getHeight());
		this.data = ((DataBufferInt)img.getRaster().getDataBuffer()).getData();
	}
	
	public Img(int width, int height, int[] data){
		this(new Dimension(width, height), data);
	}
	
	public Img(Dimension dim, int[] data){
		if(dim.width*dim.height != data.length){
			throw new IllegalArgumentException(String.format("Provided Dimension %s does not match number of provided pixels %d", dim, data.length));
		}
		this.dimension = dim;
		this.data = data;
	}
	
	public Dimension getDimension() {
		return dimension;
	}
	
	public int getWidth(){
		return dimension.width;
	}
	
	public int getHeight(){
		return dimension.height;
	}
	
	public int numPixels(){
		return getWidth()*getHeight();
	}
	
	public int[] getData() {
		return data;
	}
	
	public int getPixel(int x, int y){
		return this.data[y*dimension.width + x];
	}
	
	public int getPixel(int x, int y, final int boundaryMode){
		if(x < 0 || y < 0 || x >= dimension.width || y >= dimension.height){
			switch (boundaryMode) {
			case boundary_mode_zero:
				return 0;
			case boundary_mode_repeat_edge:
				x = (x < 0 ? 0: (x >= dimension.width ? dimension.width-1:x));
				y = (y < 0 ? 0: (y >= dimension.height ? dimension.height-1:y));
				return getPixel(x, y);
			case boundary_mode_repeat_image:
				x = (dimension.width + (x % dimension.width)) % dimension.width;
				y = (dimension.height + (y % dimension.height)) % dimension.height;
				return getPixel(x,y);
			case boundary_mode_mirror:
				if(x < 0){ // mirror x to right side of image
					x = -x - 1; 
				}
				if(y < 0 ){ // mirror y to bottom side of image
					y = -y - 1;
				}
				x = (x/dimension.width) % 2 == 0 ? (x%dimension.width) : (dimension.width-1)-(x%dimension.width);
				y = (y/dimension.height) % 2 == 0 ? (y%dimension.height) : (dimension.height-1)-(y%dimension.height);
				return getPixel(x, y);
			default:
				return boundaryMode; // boundary mode can be default color
			}
		} else { 
			return getPixel(x, y);
		}
	}
	
	public static void main(String[] args) {
		Img img = new Img(4,1, new int[]{1,0,0,4});
		for(int i = -4; i < 16; i++){
			System.out.println(img.getPixel(i, 0, boundary_mode_mirror));
		}
	}
	
	public int interpolatePixel(float xNormalized, float yNormalized){
		float xF = xNormalized * getWidth();
		float yF = yNormalized * getHeight();
		int x = (int)xF;
		int y = (int)yF;
		int c00 = getPixel(x, 							y);
		int c01 = getPixel(x, 						   (y+1 < getHeight() ? y+1:y));
		int c10 = getPixel((x+1 < getWidth() ? x+1:x), 	y);
		int c11 = getPixel((x+1 < getWidth() ? x+1:x), (y+1 < getHeight() ? y+1:y));
		return interpolateColors(c00, c01, c10, c11, xF-x, yF-y);
	}
	
	private static int interpolateColors(int c00, int c01, int c10, int c11, float mx, float my){
		return rgba_fast/*_bounded*/(
				blend( blend(a(c00), a(c01), mx), blend(a(c10), a(c11), mx), my),
				blend( blend(r(c00), r(c01), mx), blend(r(c10), r(c11), mx), my),
				blend( blend(g(c00), g(c01), mx), blend(g(c10), g(c11), mx), my),
				blend( blend(b(c00), b(c01), mx), blend(b(c10), b(c11), mx), my) );
	}
	
	private static int blend(int channel1, int channel2, float m){
		return (int) ((channel2 * m) + (channel1 * (1f-m)));
	}
	
	public void setPixel(int x, int y, int px){
		this.data[y*dimension.width + x] = px;
	}
	
	public Img copy(){
		return new Img(getDimension(), Arrays.copyOf(getData(), getData().length));
	}
	
	public BufferedImage toBufferedImage(){
		BufferedImage img = BufferedImageFactory.getINT_ARGB(getDimension());
		return toBufferedImage(img);
	}
	
	public BufferedImage toBufferedImage(BufferedImage img){
		img.setRGB(0, 0, getWidth(), getHeight(), getData(), 0, getWidth());
		return img;
	}
	
	public static int ch(int color, int startBit, int numBits){
		return (color >> startBit) & ((1 << numBits)-1);
	}
	
	public static int combineCh(int bitsPerChannel, int ... channels){
		int result = 0;
		int startBit = 0;
		for(int i = channels.length-1; i >= 0; i--){
			result |= channels[i] << startBit;
			startBit += bitsPerChannel;
		}
		return result;
	}
	
	public static int a(int color){
		return (color >> 24) & 0xff;
	}
	
	public static int r(int color){
		return (color >> 16) & 0xff;
	}
	
	public static int g(int color){
		return (color >> 8) & 0xff;
	}
	
	public static int b(int color){
		return (color) & 0xff;
	}
	
	public static int rgba_fast(int a, int r, int g, int b){
		return (a<<24)|(r<<16)|(g<<8)|b;
	}
	
	public static int rgba(int a, int r, int g, int b){
		return rgba_fast(a & 0xff, r & 0xff, g & 0xff, b & 0xff);
	}
	
	public static int rgba_bounded(int a, int r, int g, int b){
		return rgba_fast(
				a > 255 ? 255: a < 0 ? 0:a, 
				r > 255 ? 255: r < 0 ? 0:r, 
				g > 255 ? 255: g < 0 ? 0:g,
				b > 255 ? 255: b < 0 ? 0:b);
	}
	
	public static int rgb_fast(int r, int g, int b){
		return rgba_fast(0xff, r, g, b);
	}
	
	public static int rgb(int r, int g, int b){
		return rgba(0xff, r, g, b);
	}
	
	public static int rgb_bounded(int r, int g, int b){
		return rgba_bounded(0xff, r, g, b);
	}
	
}
