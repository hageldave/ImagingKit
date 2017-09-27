package hageldave.imagingkit.filter.implementations;


public class DOGFilter extends ConvolutionFilter {
	
	double std1=1;
	double std2=2;
	
	public DOGFilter() {
		calcAndSetKernel();
	}
	
	
	public double getStd1() {
		return std1;
	}
	
	public double getStd2() {
		return std2;
	}
	
	public void setStds(double std1, double std2){
		this.std1 = Math.min(std1, std2);
		this.std2 = Math.max(std1, std2);
		calcAndSetKernel();
	}
	
	public int calcKernelSize() {
		return 1+(int)Math.ceil(getStd2())*6;
	}
	
	float[] calcConvolutionKernel() {
		int kernSize = calcKernelSize();
		double[] conv1D = new double[kernSize];
		float[] kernel = new float[kernSize*kernSize];
		
		int center = kernSize/2;
		
		double a1 = 1/(getStd1()*Math.sqrt(Math.PI*2));
		double b1 = getStd1();
		
		double a2 = 1/(getStd2()*Math.sqrt(Math.PI*2));
		double b2 = getStd2();
		
		
		for(int i = 0; i < kernSize; i++){
			conv1D[i] = GaussFilter.calcGaussAt(a1, b1, i-center)-GaussFilter.calcGaussAt(a2, b2, i-center);
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
