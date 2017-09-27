package hageldave.imagingkit.filter.implementations;

import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Function;

import hageldave.imagingkit.core.Pixel;
import hageldave.imagingkit.filter.PerPixelFilter;
import hageldave.imagingkit.filter.util.MiscUtils;

public abstract class GenericColorChannelTransformation implements PerPixelFilter {

	protected boolean transformAlpha = false;
	
	@Override
	public Consumer<Pixel> consumer() {
		int[] lut = createLookUpTable();
		require1024(lut.length);
		if(transformAlpha)
			return px->{
				px.setValue(Pixel.argb_fast(
					lut[px.a()], 
					lut[px.r()+256], 
					lut[px.g()+512], 
					lut[px.b()+768]));
			};
		else 
			return px->{
				px.setValue(Pixel.argb_fast(
					px.a(),
					lut[px.r()+256], 
					lut[px.g()+512], 
					lut[px.b()+768]));
			};	
	}
	

	protected int[] createLookUpTable() {
		int[] lut = new int[1024];
		for(int i = 0; i < 256; i++){
			lut[i] = transformAlpha(i);
			lut[i+256] = transformRed(i);
			lut[i+512] = transformGreen(i);
			lut[i+768] = transformBlue(i);
		}
		return lut;
	}
	
	protected int transformAlpha(int a) {
		return a;
	}

	protected abstract int transformRed(int r);

	protected abstract int transformGreen(int g);

	protected abstract int transformBlue(int b);
	
	private static void require1024(int lutLength){
		if(lutLength != 1024)
			throw new RuntimeException("look up table returned by createLookUpTable() does not match the size constraint of 1024 values");
	}
	
	public static GenericColorChannelTransformation from8BitFunction(Function<Integer, Integer> fn8bit){
		Objects.requireNonNull(fn8bit);
		return new GenericColorChannelTransformation() {
			
			@Override
			protected int transformRed(int r) {
				return fn8bit.apply(r);
			}
			
			@Override
			protected int transformGreen(int g) {
				return fn8bit.apply(g);
			}
			
			@Override
			protected int transformBlue(int b) {
				return fn8bit.apply(b);
			}
			
			@Override
			protected int transformAlpha(int a) {
				return fn8bit.apply(a);
			}
		};
	}
	
	public static GenericColorChannelTransformation from8BitFunctions(
			Function<Integer, Integer> fn8bitRed, 
			Function<Integer, Integer> fn8bitGreen, 
			Function<Integer, Integer> fn8bitBlue, 
			Function<Integer, Integer> fn8bitAlpha)
	{
		
		if(MiscUtils.isAnyOf(null, fn8bitRed,fn8bitGreen,fn8bitBlue,fn8bitAlpha)){
			return from8BitFunctions(
					fn8bitRed   != null ?   fn8bitRed:Function.identity(),
					fn8bitGreen != null ? fn8bitGreen:Function.identity(),
					fn8bitBlue  != null ?  fn8bitBlue:Function.identity(),
					fn8bitAlpha != null ? fn8bitAlpha:Function.identity()
			);
		} else return new GenericColorChannelTransformation() {
			
			@Override
			protected int transformRed(int r) {
				return fn8bitRed.apply(r);
			}
			
			@Override
			protected int transformGreen(int g) {
				return fn8bitGreen.apply(g);
			}
			
			@Override
			protected int transformBlue(int b) {
				return fn8bitBlue.apply(b);
			}
			
			@Override
			protected int transformAlpha(int a) {
				return fn8bitAlpha.apply(a);
			}
		};
	}
	
	/**
	 * <p>
	 * Try this function for contrast stretching:<br>
	 * {@code v->(1+Math.cos(v*Math.PI+Math.PI))/2}
	 * 
	 * @param fn
	 * @return
	 */
	public static GenericColorChannelTransformation fromFunction(Function<Double, Double> fn){
		Objects.requireNonNull(fn);
		return fromFunctions(fn, fn, fn, fn);
	}
	
	public static GenericColorChannelTransformation fromFunctions(
			Function<Double, Double> fnRed, 
			Function<Double, Double> fnGreen, 
			Function<Double, Double> fnBlue, 
			Function<Double, Double> fnAlpha)
	{
		if(MiscUtils.isAnyOf(null, fnRed,fnGreen,fnBlue,fnAlpha)){
			return fromFunctions(
					fnRed   != null ?   fnRed:Function.identity(),
					fnGreen != null ? fnGreen:Function.identity(),
					fnBlue  != null ?  fnBlue:Function.identity(),
					fnAlpha != null ? fnAlpha:Function.identity()
			);
		} else return new GenericColorChannelTransformation() {
			
			@Override
			protected int transformRed(int r) {
				return (int)(255*fnRed.apply(r/255.0));
			}
			
			@Override
			protected int transformGreen(int g) {
				return (int)(255*fnGreen.apply(g/255.0));
			}
			
			@Override
			protected int transformBlue(int b) {
				return (int)(255*fnBlue.apply(b/255.0));
			}
			
			@Override
			protected int transformAlpha(int a) {
				return (int)(255*fnAlpha.apply(a/255.0));
			}
		};
	}
	
}
