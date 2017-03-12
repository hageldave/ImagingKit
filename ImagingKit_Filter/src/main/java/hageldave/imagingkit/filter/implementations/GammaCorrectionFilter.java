package hageldave.imagingkit.filter.implementations;

public class GammaCorrectionFilter extends GenericColorChannelTransformation {
	
	private double gamma = 1.0;
	
	public void setGamma(double gamma){
		if(gamma == 0.0)
			throw new IllegalArgumentException("Cannot set gamma to 0!");
		
		this.gamma = gamma;
	}
	
	public double getGamma() {
		return gamma;
	}


	@Override
	protected int transformRed(int r) {
		return correctGamma(r, 1.0/gamma);
	}

	@Override
	protected int transformGreen(int g) {
		return correctGamma(g, 1.0/gamma);
	}

	@Override
	protected int transformBlue(int b) {
		return correctGamma(b, 1.0/gamma);
	}
	
	@Override
	protected int transformAlpha(int a) {
		return correctGamma(a, 1.0/gamma);
	}
	
	private static int correctGamma(int value, double divByGamma){
		return (int)(255*Math.pow(value/255.0, divByGamma));
	}


}
