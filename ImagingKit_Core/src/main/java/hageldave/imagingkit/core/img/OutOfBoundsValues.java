package hageldave.imagingkit.core.img;

import hageldave.imagingkit.core.pixel.PixelBase;

public interface OutOfBoundsValues<P extends PixelBase<P>> extends ImgBase<P> {

	public static interface OutOfBoundsFunction {
		public double getValueForOutOfBoundsCoordinate(int x, int y, int w, int h, int ch, ImgBase<?> img);
	}
	
	public static OutOfBoundsFunction fixedOutOfBoundsValue(double v){
		return (x,y,w,h,ch,img)->v;
	}
	
	public static enum BoundaryMode implements OutOfBoundsFunction {
		ZERO(fixedOutOfBoundsValue(0)),
		REPEAT_EDGE((x,y,w,h,ch,img)->{
			x = (x < 0 ? 0: (x >= w ? w-1:x));
			y = (y < 0 ? 0: (y >= h ? h-1:y));
			return img.getValueAt(ch, x,y);
		}),
		REPEAT_IMAGE((x,y,w,h,ch,img)->{
			x = (w + (x % w)) % w;
			y = (h + (y % h)) % h;
			return img.getValueAt(ch, x,y);
		}),
		MIRROR((x,y,w,h,ch,img)->{
			if(x < 0){ // mirror x to right side of image
				x = -x - 1;
			}
			if(y < 0 ){ // mirror y to bottom side of image
				y = -y - 1;
			}
			x = (x/w) % 2 == 0 ? (x%w) : (w-1)-(x%w);
			y = (y/h) % 2 == 0 ? (y%h) : (h-1)-(y%h);
			return img.getValueAt(ch, x,y);
		}),
		;
		
		final OutOfBoundsFunction fn;
		
		private BoundaryMode(OutOfBoundsFunction fn){
			this.fn=fn;
		}
		
		@Override
		public double getValueForOutOfBoundsCoordinate(int x, int y, int w, int h, int ch, ImgBase<?> img) {
			return fn.getValueForOutOfBoundsCoordinate(x, y, w, h, ch, img);
		}
	}
	
	/**
	 * Returns the value of this image at the specified position for the specified channel.
	 * Bounds checks will be performed and positions outside of this image's
	 * dimensions will be handled according to the specified boundary mode.
	 * <p>
	 * <b><u>Boundary Modes</u></b><br>
	 * {@link #boundary_mode_zero} <br>
	 * will return 0 for out of bounds positions.
	 * <br>
	 * -{@link #boundary_mode_repeat_edge} <br>
	 * will return the same value as the nearest edge value.
	 * <br>
	 * -{@link #boundary_mode_repeat_image} <br>
	 * will return a value of the image as if the if the image was repeated on
	 * all sides.
	 * <br>
	 * -{@link #boundary_mode_mirror} <br>
	 * will return a value of the image as if the image was mirrored on all
	 * sides.
	 * <br>
	 * -<u>other values for boundary mode </u><br>
	 * will be used as default color for out of bounds positions. It is safe
	 * to use opaque colors (0xff000000 - 0xffffffff) and transparent colors
	 * above 0x0000000f which will not collide with one of the boundary modes
	 * (number of boundary modes is limited to 16 for the future).
	 * @param channel one of {@link #channel_r},{@link #channel_g},{@link #channel_b},{@link #channel_a} (0,1,2,3)
	 * @param x coordinate
	 * @param y coordinate
	 * @param boundaryMode one of the boundary modes e.g. boundary_mode_mirror
	 * @return value at specified position or a value depending on the
	 * boundary mode for out of bounds positions.
	 * @throws ArrayIndexOutOfBoundsException if the specified channel is not in [0,3] 
	 * or is 3 but the image has no alpha (check using {@link #hasAlpha()}).
	 */
	public default double getValueAt(final int channel, int x, int y, final OutOfBoundsFunction boundarymode){
		final int w = getWidth();
		final int h = getHeight();
		if(x < 0 || y < 0 || x >= w || y >= h){
			return boundarymode.getValueForOutOfBoundsCoordinate(x, y, w, h, channel, this);
		} else {
			return getValueAt(channel, x, y);
		}
	}
	
}
