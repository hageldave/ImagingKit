package hageldave.imagingkit.filter;

import static hageldave.imagingkit.core.Pixel.*;

import hageldave.imagingkit.core.Img;

public class Blend {
	
	public static interface BlendFunction {
		public int blend(int bottom, int top);
	}
	
	public static enum BlendMode {
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
		DODGE(		(a,b) -> Math.min((a<<8)/Math.max(0xff-b, 1),0xff) );
		
		public final BlendFunction blendFunction;
		
		private BlendMode(BlendFunction func) {
			this.blendFunction = func;
		}
	}

	public static int blend(int bottomRGBA, int topRGBA, float visibility, BlendFunction func){
		int temp = 0;
		float a = visibility*func.blend(temp=a(bottomRGBA), a(topRGBA)) + temp*(1-visibility);
		visibility = Math.max(visibility-(1-(a(topRGBA)/255.0f)), 0);
		float oneMinusVis = 1-visibility;
		float r = visibility*func.blend(temp=r(bottomRGBA), r(topRGBA)) + temp*oneMinusVis;
		float g = visibility*func.blend(temp=g(bottomRGBA), g(topRGBA)) + temp*oneMinusVis;
		float b = visibility*func.blend(temp=b(bottomRGBA), b(topRGBA)) + temp*oneMinusVis;
		return argb_fast((int)a, (int)r, (int)g, (int)b);
	}
	
	public static int blend(int bottomRGBA, int topRGBA, float visibility, BlendMode mode){
		return blend(bottomRGBA, topRGBA, visibility, mode.blendFunction);
	}
	
	public static void blend(Img bottom, Img top, float visibility, int xTop, int yTop, BlendFunction func){
		bottom.forEachParallel((px)->
		{
			int x = px.getX()-xTop;
			int y = px.getY()-yTop;
			
			if(x >= 0 && y >= 0 && x < top.getWidth() && y < top.getHeight()){
				px.setValue(blend(px.getValue(), top.getValue(x, y), visibility, func));
			}
		});
	}
	
	public static void blend(Img bottom, Img top, float visibility, int xTop, int yTop, BlendMode mode){
		blend(bottom, top, visibility, xTop, yTop, mode.blendFunction);
	}
	
	
}
