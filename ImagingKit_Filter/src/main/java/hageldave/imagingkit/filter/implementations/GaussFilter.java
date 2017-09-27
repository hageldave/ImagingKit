package hageldave.imagingkit.filter.implementations;

public class GaussFilter extends ConvolutionFilter {

	double std = 1;
	
	public GaussFilter() {
		calcAndSetKernel();
	}
	
	public GaussFilter setVariance(double variance) {
		setStd(Math.sqrt(variance));
		return this;
	}
	
	public double getVariance() {
		return std*std;
	}
	
	public GaussFilter setStd(double std) {
		this.std = std;
		calcAndSetKernel();
		return this;
	}
	
	public double getStd() {
		return std;
	}
	
	public int calcKernelSize() {
		// 3times standard deviation to both sides 
		// will cover over 99% of gaussian bell
		// 2times will cover about 95%
		return 1+(int)Math.round(getStd()*3)*2;
	}
	
	static double calcGaussAt(double a, double b, int x){
		return a*Math.exp(-((x*x)/(2*b*b)));
	}
	
	float[] calcConvolutionKernel() {
		int kernSize = calcKernelSize();
		double[] conv1D = new double[kernSize];
		float[] kernel = new float[kernSize*kernSize];
		
		int center = kernSize/2;
		double a = 1/(getStd()*Math.sqrt(Math.PI*2));
		double b = getStd();
		
		for(int i = 0; i < kernSize; i++){
			conv1D[i] = calcGaussAt(a, b, i-center);
		}
		
		for(int i = 0; i < kernSize; i++){
			for(int j = 0; j < kernSize; j++){
				kernel[i*kernSize+j] = (float)(conv1D[i]*conv1D[j]);
			}
		}
		
		return kernel;
	}
	
	void calcAndSetKernel() {
		int kernSize = calcKernelSize();
		this.setConvolutionKernel(kernSize, kernSize, calcConvolutionKernel());
		this.normalizeConvolutionKernel();
	}
	
}
