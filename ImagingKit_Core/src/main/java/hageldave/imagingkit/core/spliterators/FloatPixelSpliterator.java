package hageldave.imagingkit.core.spliterators;

import java.util.Spliterator;

import hageldave.imagingkit.core.Img;
import hageldave.imagingkit.core.Pixel;

public class FloatPixelSpliterator extends PixelConvertingSpliterator<FloatPixelSpliterator.FloatPixel>{

	public static class FloatPixel {
		/** normalized alpha within [0..1] */
		public float a;
		/** normalized red within [0..1]   */
		public float r;
		/** normalized green within [0..1] */
		public float g;
		/** normalized blue within [0..1]  */
		public float b;
		
		private Pixel pixel;
		
		private void set(Pixel px){
			a = px.a_normalized();
			r = px.r_normalized();
			g = px.g_normalized();
			b = px.b_normalized();
			pixel = px;
		}

		/**
		 * Delegates to {@link Pixel#getImg()}
		 * @return the Img this Pixel belongs to.
		 */
		public Img getImg() {
			return pixel.getImg();
		}

		/**
		 * Delegates to {@link Pixel#getIndex()}
		 * @return the index of the Img value this Pixel references.
		 */
		public int getIndex() {
			return pixel.getIndex();
		}

		/**
		 * Delegates to {@link Pixel#getX()}
		 * @return the x coordinate of the position in the Img this Pixel references.
		 */
		public int getX() {
			return pixel.getX();
		}

		/**
		 * Delegates to {@link Pixel#getY()}
		 * @return the y coordinate of the position in the Img this Pixel references.
		 */
		public int getY() {
			return pixel.getY();
		}

		/**
		 * Delegates to {@link Pixel#getXnormalized()}
		 * @return normalized x coordinate within [0..1]
		 */
		public float getXnormalized() {
			return pixel.getXnormalized();
		}

		/**
		 * Delegates to {@link Pixel#getYnormalized()}
		 * @return normalized y coordinate within [0..1]
		 */
		public float getYnormalized() {
			return pixel.getYnormalized();
		}
		
		
	}
	
	public FloatPixelSpliterator(Spliterator<Pixel> delegate) {
		super(	delegate, 
				()->{return new FloatPixel();}, 
				(px,fpx)->{fpx.set(px);}, 
				(fpx,px)->{px.setARGB_fromNormalized(fpx.a, fpx.r, fpx.g, fpx.b);});
	}
	
	
	public FloatPixelSpliterator(Img img) {
		this(img.spliterator());
	}
	
	
}
