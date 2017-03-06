package hageldave.imagingkit.filter.implementations;

import java.util.function.Consumer;

import hageldave.imagingkit.core.Img;
import hageldave.imagingkit.core.Pixel;
import hageldave.imagingkit.filter.NeighborhoodFilter;

public class ConvolutionFilter implements NeighborhoodFilter {
	
	float[] convolutionKernel;
	int kernelWidth;
	int kernelHeight;
	boolean convolveAlpha = true;
	int boundaryMode = Img.boundary_mode_mirror;

	
	@Override
	public Consumer<Pixel> consumer(Img copy) {
		int kernelWidth=this.kernelWidth, kernelHeight=this.kernelHeight;
		int kernelX0 = -kernelWidth/2, kernelY0 = -kernelHeight/2;
		float[] convolutionKernel = this.convolutionKernel;
		
		if(convolveAlpha)
			return px->convolveWithAlpha(px, copy, kernelX0, kernelY0, kernelWidth, kernelHeight, convolutionKernel);
		else
			return px->convolveWithoutAlpha(px, copy, kernelX0, kernelY0, kernelWidth, kernelHeight, convolutionKernel);
	}
	
	
	private void convolveWithAlpha(
			final Pixel px, 
			final Img copy, 
			final int kernelX0, 
			final int kernelY0, 
			final int kernelWidth, 
			final int kernelHeight, 
			final float[] convolutionKernel)
	{
		int x = px.getX(), y = px.getY();
		float a, r, g, b; 
		a = r = g = b = 0;
		for(int ky = kernelY0; ky < kernelY0+kernelHeight; ky++){
			for(int kx = kernelX0; kx < kernelX0+kernelWidth; kx++){
				int value = copy.getValue(x+kx, y+ky, boundaryMode);
				float k = getKernelValue(kx, ky, kernelX0, kernelY0, kernelWidth, convolutionKernel);
				a += Pixel.a(value)*k;
				r += Pixel.r(value)*k;
				g += Pixel.g(value)*k;
				b += Pixel.b(value)*k;
			}
		}
		px.setValue(Pixel.argb_bounded((int)a, (int)r, (int)g, (int)b));
	}
	
	private void convolveWithoutAlpha(
			final Pixel px, 
			final Img copy, 
			final int kernelX0, 
			final int kernelY0, 
			final int kernelWidth, 
			final int kernelHeight, 
			final float[] convolutionKernel)
	{
		int x = px.getX(), y = px.getY();
		float r, g, b; 
		r = g = b = 0;
		for(int ky = kernelY0; ky < kernelY0+kernelHeight; ky++){
			for(int kx = kernelX0; kx < kernelX0+kernelWidth; kx++){
				int value = copy.getValue(x+kx, y+ky, boundaryMode);
				float k = getKernelValue(kx, ky, kernelX0, kernelY0, kernelWidth, convolutionKernel);
				r += Pixel.r(value)*k;
				g += Pixel.g(value)*k;
				b += Pixel.b(value)*k;
			}
		}
		px.setValue(Pixel.argb_bounded(px.a(), (int)r, (int)g, (int)b));
	}

	static float getKernelValue(int kx, int ky, int kx0, int ky0, int kWidth, float[] convolutionKernel){
		return convolutionKernel[(ky-ky0)*kWidth+(kx-kx0)];
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

	public String kernelToString(){
		StringBuilder sb = new StringBuilder();
		for(int y = 0; y < kernelHeight; y++){
			sb.append("[");
			for(int x = 0; x < kernelWidth; x++){
				float k = convolutionKernel[y*kernelWidth+x];
				sb.append(String.format(" %.3f ", k));
			}
			sb.append("]");
			if(y < kernelHeight-1)
				sb.append("\n");
		}
		
		return sb.toString();
	}

}
