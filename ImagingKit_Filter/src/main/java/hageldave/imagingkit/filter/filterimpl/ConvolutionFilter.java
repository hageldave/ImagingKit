package hageldave.imagingkit.filter.filterimpl;

import hageldave.imagingkit.core.Img;
import hageldave.imagingkit.core.Pixel;
import hageldave.imagingkit.filter.Filter;
import hageldave.imagingkit.filter.settings.ReadOnlyFilterSettings;

public class ConvolutionFilter extends Filter {

	public static final String boundaryModeID = "boundary mode";
	public static final String convolveAlphaID = "convolve alpha";
	float[] convolutionKernel;
	int kernelWidth;
	int kernelHeight;
	boolean convolveAlpha = true;
	int boundaryMode = Img.boundary_mode_mirror;
	

	@Override
	protected void readSettingsBeforeApply(ReadOnlyFilterSettings settings) {
		this.convolveAlpha = settings.getAs(convolveAlphaID, Boolean.class, true);
		this.boundaryMode = settings.getAs(boundaryModeID, Integer.class, Img.boundary_mode_mirror);
	}

	@Override
	public void doApply(Img img) {
		Img cpy = img.copy();
		if(convolveAlpha){
		img.forEachParallel((px)->
		{
			int x = px.getX(); int y = px.getY();
			float a, r, g, b; 
			a = r = g = b = 0;
			for(int ky = getKernelY0(); ky < getKernelY0()+kernelHeight; ky++){
				for(int kx = getKernelX0(); kx < getKernelX0()+kernelWidth; kx++){
					int value = cpy.getValue(x+kx, y+ky, boundaryMode);
					a += Pixel.a(value)*getKernelValue(kx, ky);
					r += Pixel.r(value)*getKernelValue(kx, ky);
					g += Pixel.g(value)*getKernelValue(kx, ky);
					b += Pixel.b(value)*getKernelValue(kx, ky);
				}
			}
			px.setValue(Pixel.argb_bounded((int)a, (int)r, (int)g, (int)b));
		});
		} else {
			img.forEachParallel((px)->
			{
				int x = px.getX(); int y = px.getY();
				float r, g, b; 
				r = g = b = 0;
				for(int ky = getKernelY0(); ky < getKernelY0()+kernelHeight; ky++){
					for(int kx = getKernelX0(); kx < getKernelX0()+kernelWidth; kx++){
						int value = cpy.getValue(x+kx, y+ky, boundaryMode);
						r += Pixel.r(value)*getKernelValue(kx, ky);
						g += Pixel.g(value)*getKernelValue(kx, ky);
						b += Pixel.b(value)*getKernelValue(kx, ky);
					}
				}
				px.setValue(Pixel.argb_bounded(px.a(), (int)r, (int)g, (int)b));
			});
		}
	}
	
	int getKernelX0(){
		return -kernelWidth/2;
	}
	
	int getKernelY0(){
		return -kernelHeight/2;
	}
	
	float getKernelValue(int kx, int ky){
		return convolutionKernel[(ky-getKernelY0())*kernelWidth+(kx-getKernelX0())];
	}
	
	public void setConvolutionKernel(int kWidth, int kHeight, float[] kernel){
		this.convolutionKernel = kernel;
		this.kernelWidth = kWidth;
		this.kernelHeight = kHeight;
	}
	
	public void normalizeConvolutionKernel(){
		float sum = 0;
		for(int y = 0; y < kernelHeight; y++)
			for(int x = 0; x < kernelWidth; x++)
				sum += convolutionKernel[y*kernelWidth+x];
		if(sum != 0){
			for(int y = 0; y < kernelHeight; y++)
				for(int x = 0; x < kernelWidth; x++){
					convolutionKernel[y*kernelWidth+x] /= sum;
				}
		}
	}
	
	
}
