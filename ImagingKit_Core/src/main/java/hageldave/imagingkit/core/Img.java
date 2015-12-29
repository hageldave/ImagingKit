package hageldave.imagingkit.core;

import java.awt.Dimension;
import java.awt.image.BufferedImage;
import java.awt.image.DataBuffer;
import java.awt.image.DataBufferInt;
import java.awt.image.DirectColorModel;
import java.awt.image.Raster;
import java.awt.image.WritableRaster;
import java.util.Arrays;
import java.util.Iterator;
import java.util.Spliterator;
import java.util.function.Consumer;


public class Img implements Iterable<Pixel> {

	public static final int boundary_mode_zero = 0;
	public static final int boundary_mode_repeat_edge = -1;
	public static final int boundary_mode_repeat_image = -2;
	public static final int boundary_mode_mirror = -3;
	
	
	private final int[] data;
	private final Dimension dimension;
	
	public Img(int width, int height){
		this(new Dimension(width, height));
	}
	
	public Img(Dimension dimension){
		this.data = new int[dimension.width*dimension.height];
		this.dimension = dimension;
	}
	
	public Img(BufferedImage bimg){
		this(bimg.getWidth(), bimg.getHeight());
		bimg.getRGB(0, 0, this.getWidth(), this.getHeight(), this.getData(), 0, this.getWidth());
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
	
	public int numValues(){
		return getWidth()*getHeight();
	}
	
	public int[] getData() {
		return data;
	}
	
	public int getValue(final int x, final int y){
		return this.data[y*dimension.width + x];
	}
	
	public int getValue(int x, int y, final int boundaryMode){
		if(x < 0 || y < 0 || x >= dimension.width || y >= dimension.height){
			switch (boundaryMode) {
			case boundary_mode_zero:
				return 0;
			case boundary_mode_repeat_edge:
				x = (x < 0 ? 0: (x >= dimension.width ? dimension.width-1:x));
				y = (y < 0 ? 0: (y >= dimension.height ? dimension.height-1:y));
				return getValue(x, y);
			case boundary_mode_repeat_image:
				x = (dimension.width + (x % dimension.width)) % dimension.width;
				y = (dimension.height + (y % dimension.height)) % dimension.height;
				return getValue(x,y);
			case boundary_mode_mirror:
				if(x < 0){ // mirror x to right side of image
					x = -x - 1; 
				}
				if(y < 0 ){ // mirror y to bottom side of image
					y = -y - 1;
				}
				x = (x/dimension.width) % 2 == 0 ? (x%dimension.width) : (dimension.width-1)-(x%dimension.width);
				y = (y/dimension.height) % 2 == 0 ? (y%dimension.height) : (dimension.height-1)-(y%dimension.height);
				return getValue(x, y);
			default:
				return boundaryMode; // boundary mode can be default color
			}
		} else { 
			return getValue(x, y);
		}
	}
	
	public int interpolateValue(final float xNormalized, final float yNormalized){
		float xF = xNormalized * (getWidth()-1);
		float yF = yNormalized * (getHeight()-1);
		int x = (int)xF;
		int y = (int)yF;
		int c00 = getValue(x, 							y);
		int c01 = getValue(x, 						   (y+1 < getHeight() ? y+1:y));
		int c10 = getValue((x+1 < getWidth() ? x+1:x), 	y);
		int c11 = getValue((x+1 < getWidth() ? x+1:x), (y+1 < getHeight() ? y+1:y));
		return interpolateColors(c00, c01, c10, c11, xF-x, yF-y);
	}
	
	private static int interpolateColors(final int c00, final int c01, final int c10, final int c11, final float mx, final float my){
		return argb_fast/*_bounded*/(
				blend( blend(a(c00), a(c01), mx), blend(a(c10), a(c11), mx), my),
				blend( blend(r(c00), r(c01), mx), blend(r(c10), r(c11), mx), my),
				blend( blend(g(c00), g(c01), mx), blend(g(c10), g(c11), mx), my),
				blend( blend(b(c00), b(c01), mx), blend(b(c10), b(c11), mx), my) );
	}
	
	private static int blend(final int channel1, final int channel2, final float m){
		return (int) ((channel2 * m) + (channel1 * (1f-m)));
	}
	
	public Pixel getPixel(){
		return new Pixel(this, 0);
	}
	
	public Pixel getPixel(int x, int y){
		return new Pixel(this, x,y);
	}
	
	public Img copyArea(int x, int y, int w, int h, Img dest, int destX, int destY){
		if(dest == null){
			dest = new Img(w,h);
		}
		if(x==0 && destX==0 && w==dest.getWidth() && w==this.getWidth()){
			System.arraycopy(this.getData(), y*w, dest.getData(), destY*w, w*h);
		} else {
			for(int i = 0; i < h; i++){
				System.arraycopy(this.getData(), (y+i)*getWidth()+x, dest.getData(), (destY+i)*dest.getWidth()+destX, w);
			}
		}
		return dest;
	}
	
	public void setValue(final int x, final int y, final int px){
		this.data[y*dimension.width + x] = px;
	}
	
	public void fill(final int value){
		Arrays.fill(getData(), value);
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
	
	public BufferedImage getRemoteBufferedImage(){
		DirectColorModel cm = new DirectColorModel(32,
				0x00ff0000,       // Red
                0x0000ff00,       // Green
                0x000000ff,       // Blue
                0xff000000        // Alpha
                );
		DataBufferInt buffer = new DataBufferInt(getData(), numValues());
		WritableRaster raster = Raster.createPackedRaster(buffer, getWidth(), getHeight(), getWidth(), cm.getMasks(), null);
		BufferedImage bimg = new BufferedImage(cm, raster, false, null);
		return bimg;
	}
	
	public static Img createRemoteImg(BufferedImage bimg){
		int type = bimg.getRaster().getDataBuffer().getDataType();
		if(type != DataBuffer.TYPE_INT){
			throw new IllegalArgumentException(
					String.format("cannot create Img as remote of provided BufferedImage!%n"
							+ "Need BufferedImage with DataBuffer of type TYPE_INT (%d). Provided type: %d", 
							DataBuffer.TYPE_INT, type));
		}
		Img img = new Img(
				new Dimension(bimg.getWidth(),bimg.getHeight()), 
				((DataBufferInt)bimg.getRaster().getDataBuffer()).getData()
			);
		return img;
	}
	
	
	@Override
	public Iterator<Pixel> iterator() {
		Iterator<Pixel> pxIter = new Iterator<Pixel>() {
			Pixel px = new Pixel(Img.this, -1);
			
			@Override
			public Pixel next() {
				px.setIndex(px.getIndex()+1);
				return px;
			}
			
			@Override
			public boolean hasNext() {
				return px.getIndex()+1 < numValues();
			}
		};
		return pxIter;
	}
	
	public Spliterator<Pixel> spliterator() {
		return new ImgSpliterator(0, numValues()-1);
	}
	
	
	public static final int ch(final int color, final int startBit, final int numBits){
		return (color >> startBit) & ((1 << numBits)-1);
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
	
	public static final int a(final int color){
		return (color >> 24) & 0xff;
	}
	
	public static final int r(final int color){
		return (color >> 16) & 0xff;
	}
	
	public static final int g(final int color){
		return (color >> 8) & 0xff;
	}
	
	public static final int b(final int color){
		return (color) & 0xff;
	}
	
	public static final int argb_fast(final int a, final int r, final int g, final int b){
		return (a<<24)|(r<<16)|(g<<8)|b;
	}
	
	public static final int argb(final int a, final int r, final int g, final int b){
		return argb_fast(a & 0xff, r & 0xff, g & 0xff, b & 0xff);
	}
	
	public static final int argb_bounded(final int a, final int r, final int g, final int b){
		return argb_fast(
				a > 255 ? 255: a < 0 ? 0:a, 
				r > 255 ? 255: r < 0 ? 0:r, 
				g > 255 ? 255: g < 0 ? 0:g,
				b > 255 ? 255: b < 0 ? 0:b);
	}
	
	public static final int rgb_fast(final int r, final int g, final int b){
		return argb_fast(0xff, r, g, b);
	}
	
	public static final int rgb(final int r, final int g, final int b){
		return argb(0xff, r, g, b);
	}
	
	public static final int rgb_bounded(final int r, final int g, final int b){
		return argb_bounded(0xff, r, g, b);
	}
	
	public static final int getGrey(final int color, final int redWeight, final int greenWeight, final int blueWeight){
		return (r(color)*redWeight + g(color)*greenWeight + b(color)*blueWeight)/(redWeight+blueWeight+greenWeight);
	}
	
	public static final int getLuminance(final int color){
		return getGrey(color, 2126, 7152, 722);
	}
	
	
	
	private class ImgSpliterator implements  Spliterator<Pixel> {
		
		final Pixel px;
		int endIndex;
		
		public ImgSpliterator(int startIndex, int endIndex) {
			px = new Pixel(Img.this, startIndex);
			this.endIndex = endIndex;
		}
		
		public void setEndIndex(int endIndex) {
			this.endIndex = endIndex;
		}

		@Override
		public boolean tryAdvance(Consumer<? super Pixel> action) {
			if(px.getIndex() <= endIndex){
				int index = px.getIndex();
				action.accept(px);
				px.setIndex(index+1);
				return true;
			} else {
				return false;
			}
		}
		
		@Override
		public void forEachRemaining(Consumer<? super Pixel> action) {
			int idx = px.getIndex();
			for(;idx <= endIndex; px.setIndex(++idx)){
				action.accept(px);
			}
		}

		@Override
		public Spliterator<Pixel> trySplit() {
			int currentIdx = Math.min(px.getIndex(), endIndex);
			int midIdx = currentIdx + (endIndex-currentIdx)/2;
			if(midIdx > currentIdx+1024){
				ImgSpliterator split = new ImgSpliterator(midIdx, endIndex);
				setEndIndex(midIdx-1);
				return split;
			} else {
				return null;
			}
		}

		@Override
		public long estimateSize() {
			int currentIndex = px.getIndex();
			int lastIndexPlusOne = endIndex+1;
			return lastIndexPlusOne-currentIndex;
		}

		@Override
		public int characteristics() {
			return NONNULL | SIZED | CONCURRENT | SUBSIZED;
		}
		
	}
}
