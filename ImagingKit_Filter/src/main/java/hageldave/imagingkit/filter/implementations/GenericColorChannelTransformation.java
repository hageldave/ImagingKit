package hageldave.imagingkit.filter.implementations;

import java.util.function.Consumer;
import java.util.function.Function;

import hageldave.imagingkit.core.Pixel;
import hageldave.imagingkit.filter.PerPixelFilter;

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
	
	/**
	 * <p>
	 * Try this function for contrast stretching:<br>
	 * {@code v->(1+Math.cos(v*Math.PI+Math.PI))/2}
	 * 
	 * @param fn
	 * @return
	 */
	public static GenericColorChannelTransformation fromFunction(Function<Double, Double> fn){
		return new GenericColorChannelTransformation() {
			
			@Override
			protected int transformRed(int r) {
				return (int)(255*fn.apply(r/255.0));
			}
			
			@Override
			protected int transformGreen(int g) {
				return (int)(255*fn.apply(g/255.0));
			}
			
			@Override
			protected int transformBlue(int b) {
				return (int)(255*fn.apply(b/255.0));
			}
			
			@Override
			protected int transformAlpha(int a) {
				return (int)(255*fn.apply(a/255.0));
			}
		};
	}
	
}
