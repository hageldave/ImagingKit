package hageldave.imagingkit.core;

import static hageldave.imagingkit.core.Pixel.*;

import java.util.function.Consumer;

import hageldave.imagingkit.core.Img;

/**
 * This Enum class provides a variation of different blending functions to
 * blend a bottom and top value (pixels). It also provides methods to calculate
 * the blend of two colors with additional transparency specification 
 * (see {@link #blend(int, int, float, Blending)}), as well as ready to use 
 * Consumers for blending two {@link Img}s 
 * (see {@link #getBlendingWith(Img, float)}).
 * <p>
 * Here's a short example on how to blend two images using the Blending class:
 * <pre>
 * {@code
 * Img bottom, top;
 * ... initialize images ...
 * bottom.forEach(Blending.AVERAGE.getBlendingWith(top));
 * }</pre>
 * You can also specify the offset of the top image on the bottom image:
 * <pre>
 * {@code
 * Img bottom, top;
 * ... initialize images ...
 * int x = 3;  // horizontal offset
 * int y = -5; // vertical offset 
 * bottom.forEach(Blending.DIFFERENCE.getBlendingWith(top, x, y));
 * }</pre>
 * If you do not rely on the Img class or only need to blend two single colors
 * you may omit this level of abstraction and use the per color or 
 * channel methods:
 * <pre>
 * {@code
 * int bottomARGB = 0x8844FF11;
 * int topARGB =    0xFF118899;
 * float opacity = 0.7f;
 * int blendARGB = Blending.blend(bottomARGB, topARGB, opacity, Blending.DODGE);
 * 	
 * int channel1 = 0xBB;
 * int channel2 = 0x7F;
 * int channelBlend = Blending.SCREEN.blendFunction.blend(channel1, channel2);
 * }</pre>
 * 
 * 
 * @author hageldave
 * @since 1.3
 */
public enum Blending {
	
	NORMAL(		(a,b) -> b ),
	AVERAGE( 	(a,b) -> ((a+b)>>1) ),
	MULTIPLY( 	(a,b) -> ((a*b)>>8) ),
	SCREEN( 	(a,b) -> (0xff - ((0xff-a) * (0xff-b)>>8)) ),
	DARKEN( 	(a,b) -> Math.min(a, b) ),
	BRIGHTEN(	(a,b) -> Math.max(a, b) ),
	DIFFERENCE( (a,b) -> Math.abs(a-b) ),
	ADDITION(	(a,b) -> Math.min(a+b, 0xff) ),
	SUBTRACTION((a,b) -> Math.max(a+b-0xff, 0) ),
	REFLECT(	(a,b) -> Math.min(a*a / Math.max(0xff-b, 1), 0xff) ),
	OVERLAY(	(a,b) -> a < 128 ? (a*b)>>7 : 0xff-(((0xff-a)*(0xff-b))>>7) ),
	HARDLIGHT(	(a,b) -> b < 128 ? (a*b)>>7 : 0xff-(((0xff-a)*(0xff-b))>>7) ),
	SOFTLIGHT(	(a,b) -> {int c=(a*b)>>8; return c + (a*(0xff-(((0xff-a)*(0xff-b)) >> 8)-c) >> 8);} ),
	DODGE(		(a,b) -> Math.min((a<<8)/Math.max(0xff-b, 1),0xff) )
	;
		
	////// ATTRIBUTES / METHODS //////
	
	/** This blending's blend function */
	public final BlendFunction blendFunction;
	
	/** Enum Constructor */
	private Blending(BlendFunction func) {
		this.blendFunction = func;
	}

	/** ARGB blending with visibility and alpha value consideration */
	public Consumer<Pixel> getBlendingWith(Img topImg, int xTop, int yTop, float visibility){
		return blending(topImg, xTop, yTop, visibility, blendFunction);
	}

	/** RGB blending with opaque result (alpha is ignored) */
	public Consumer<Pixel> getBlendingWith(Img topImg, int xTop, int yTop){
		return blending(topImg, xTop, yTop, blendFunction);
	}

	public Consumer<Pixel> getBlendingWith(Img topImg, float visibility){
		return getBlendingWith(topImg,0,0, visibility);
	}

	public Consumer<Pixel> getBlendingWith(Img topImg){
		return getBlendingWith(topImg, 0, 0);
	}
		
	
	////// STATIC //////
	
	/** Interface providing the {@link #blend(int, int)} method */
	public static interface BlendFunction {
		/**
		 * Calculates the blended value of two 8bit values 
		 * (e.g. red, green or blue channel values).
		 * The resulting value is also 8bit.
		 * @param bottom value
		 * @param top value
		 * @return blended value
		 */
		public int blend(int bottom, int top);
	}
	
	public static int blend(int bottomRGB, int topRGB, BlendFunction func){
		return rgb_fast(func.blend(r(bottomRGB), r(topRGB)), 
						func.blend(g(bottomRGB), g(topRGB)),
						func.blend(b(bottomRGB), b(topRGB)));
	}
	
	public static int blend(int bottomRGB, int topRGB, Blending mode){
		return blend(bottomRGB, topRGB, mode.blendFunction);
	}

	public static int blend(int bottomARGB, int topARGB, float visibility, BlendFunction func){
		int temp = 0;
		
		float a = Math.min(visibility*(temp=a(topARGB)) + a(bottomARGB),0xff);
		
		visibility *= (temp/255.0f);
		float oneMinusVis = 1-visibility;
		
		float r = visibility*func.blend(temp=r(bottomARGB), r(topARGB)) + temp*oneMinusVis;
		float g = visibility*func.blend(temp=g(bottomARGB), g(topARGB)) + temp*oneMinusVis;
		float b = visibility*func.blend(temp=b(bottomARGB), b(topARGB)) + temp*oneMinusVis;
		
		return argb_fast((int)a, (int)r, (int)g, (int)b);
	}
	
	public static int blend(int bottomARGB, int topARGB, float visibility, Blending mode){
		return blend(bottomARGB, topARGB, visibility, mode.blendFunction);
	}
	
	public static Consumer<Pixel> blending(Img top, int xTop, int yTop, float visibility, BlendFunction func){
		return (px)->
		{
			int x = px.getX()-xTop;
			int y = px.getY()-yTop;
			
			if(x >= 0 && y >= 0 && x < top.getWidth() && y < top.getHeight()){
				px.setValue(blend(px.getValue(), top.getValue(x, y), visibility, func));
			}
		};
	}
	
	public static Consumer<Pixel> blending(Img top, int xTop, int yTop, BlendFunction func){
		return (px)->
		{
			int x = px.getX()-xTop;
			int y = px.getY()-yTop;
			
			if(x >= 0 && y >= 0 && x < top.getWidth() && y < top.getHeight()){
				px.setValue(blend(px.getValue(), top.getValue(x, y), func));
			}
		};
	}
	
}
