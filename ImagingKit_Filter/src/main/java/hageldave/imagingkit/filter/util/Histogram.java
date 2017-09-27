package hageldave.imagingkit.filter.util;

import hageldave.imagingkit.core.Img;
import hageldave.imagingkit.core.Pixel;

public class Histogram {

	public static final int RED_HIST = 256*0;
	public static final int GREEN_HIST = 256*1;
	public static final int BLUE_HIST = 256*2;
	public static final int ALPHA_HIST = 256*3;
	public static final int LUMINANCE_HIST = 256*4;

	int[] histograms = new int[256*5];
	int totalNumOfPixels = 0;

	public void calculateHistogram(Img img){
		totalNumOfPixels = img.numValues();
		for(int i = 0; i < img.numValues(); i++){
			int val = img.getData()[i];
			histograms[RED_HIST+Pixel.r(val)]++;
			histograms[GREEN_HIST+Pixel.g(val)]++;
			histograms[BLUE_HIST+Pixel.b(val)]++;
			histograms[ALPHA_HIST+Pixel.a(val)]++;
			histograms[LUMINANCE_HIST+Pixel.getLuminance(val)]++;
		}
	}

	public int getValueFrequency(int value, int histogram){
		if(validValueAndHistogramArgs(value, histogram))
		{
			return getValueFrequency_(value, histogram);
		} else {
			return -1;
		}
	}

	public double getRelativeValueFrequency(int value, int histogram){
		if(validValueAndHistogramArgs(value, histogram))
		{
			return getValueFrequency_(value, histogram)*1.0/totalNumOfPixels;
		} else {
			return -1;
		}
	}

	private int getValueFrequency_(int value, int histogram) {
		return histograms[histogram+value];
	}
	
	public int getMaximumFrequencyValue(int histogram) {
		if(MiscUtils.isAnyOf(histogram, RED_HIST,GREEN_HIST,BLUE_HIST,ALPHA_HIST,LUMINANCE_HIST)){
			int max = 0;
			int val = 0;
			for(int i = 0; i < 256; i++){
				if(getValueFrequency_(i, histogram) > max){
					max = getValueFrequency_(i, histogram);
					val = i;
				}
			}
			return val;
		}
		return -1;
	}

	private static boolean validValueAndHistogramArgs(int value, int histogram){
		return 	0 <= value
				&& value < 256
				&& MiscUtils.isAnyOf(histogram, RED_HIST,GREEN_HIST,BLUE_HIST,ALPHA_HIST,LUMINANCE_HIST);
	}

	public int[] getEqualizationLUT(int histogram) {
		double sum = 0;
		int[] lut = new int[256];
		for(int i = 0; i < 256; i++){
			sum += getRelativeValueFrequency(i, histogram);
			lut[i] = (int) (sum*255);
		}
		return lut;
	}

	public static Histogram getHistogram(Img img){
		Histogram h = new Histogram();
		h.calculateHistogram(img);
		return h;
	}

}
