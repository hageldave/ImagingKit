package hageldave.imagingkit.fourier;

import hageldave.ezfftw.dp.FFT;
import hageldave.imagingkit.core.scientific.ColorImg;

public class Fourier {

	public static ComplexImg[] transform(ColorImg img, int ...channels) {
		if(channels.length < 1 || channels.length > 4){
			throw new IllegalArgumentException(
					"Need to at least specify 1 channel, can at most specify 4 (ARGB). Got " + channels.length);
		}
		for(int ch: channels){
			if( ch < 0 || ch > 3 || (ch > 2 && !img.hasAlpha()) ){
				throw new IllegalArgumentException(String.format(
						"Channels can be 0,1,2 (also 3 if image has alpha). But one channel is %d and image %s alpha",
						ch, img.hasAlpha() ? "has":"does not have"));
			}
		}
		ComplexImg[] fourierPerChannel = new ComplexImg[channels.length];
		for(int i = 0; i < channels.length; i++){
			int ch = channels[i];
			ComplexImg transformed = new ComplexImg(img.getDimension());
			FFT.fft(
					img.getData()[ch], // input
					transformed.getDataReal(), // real out
					transformed.getDataImag(), // imaginary out
					img.getWidth(), img.getHeight()); // dimensions
			fourierPerChannel[i] = transformed;
		}
		return fourierPerChannel;
	}
	
}
