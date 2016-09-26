package hageldave.imagingkit.core;

import static hageldave.imagingkit.core.Pixel.*;

import java.util.function.Consumer;

import hageldave.imagingkit.core.Img;

public enum BlendMode {
	
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
	
	public final BlendFunction blendFunction;
	
	private BlendMode(BlendFunction func) {
		this.blendFunction = func;
	}

	/** RGBA blending with visibility and alpha value consideration */
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
	
	public static interface BlendFunction {
		public int blend(int bottom, int top);
	}
	
	public static int blend(int bottomRGB, int topRGB, BlendFunction func){
		return rgb_fast(func.blend(r(bottomRGB), r(topRGB)), 
						func.blend(g(bottomRGB), g(topRGB)),
						func.blend(b(bottomRGB), b(topRGB)));
	}
	
	public static int blend(int bottomRGB, int topRGB, BlendMode mode){
		return blend(bottomRGB, topRGB, mode.blendFunction);
	}

	public static int blend(int bottomRGBA, int topRGBA, float visibility, BlendFunction func){
		int temp = 0;
		
		float a = Math.min(visibility*(temp=a(topRGBA)) + a(bottomRGBA),0xff);
		
		visibility *= (temp/255.0f);
		float oneMinusVis = 1-visibility;
		
		float r = visibility*func.blend(temp=r(bottomRGBA), r(topRGBA)) + temp*oneMinusVis;
		float g = visibility*func.blend(temp=g(bottomRGBA), g(topRGBA)) + temp*oneMinusVis;
		float b = visibility*func.blend(temp=b(bottomRGBA), b(topRGBA)) + temp*oneMinusVis;
		
		return argb_fast((int)a, (int)r, (int)g, (int)b);
	}
	
	public static int blend(int bottomRGBA, int topRGBA, float visibility, BlendMode mode){
		return blend(bottomRGBA, topRGBA, visibility, mode.blendFunction);
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
