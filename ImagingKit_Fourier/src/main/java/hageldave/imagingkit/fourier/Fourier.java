package hageldave.imagingkit.fourier;

import java.awt.Dimension;
import java.util.Arrays;

import hageldave.ezfftw.dp.FFT;
import hageldave.ezfftw.dp.RowMajorArrayAccessor;
import hageldave.ezfftw.dp.samplers.ComplexValuedSampler;
import hageldave.imagingkit.core.scientific.ColorImg;

public class Fourier {

	public static ComplexImg[] transform(ColorImg img, int ...channels) {
		// sanity checks
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
		// make transforms
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
	
	public static ColorImg inverseTransform(ColorImg target, ComplexImg[] fourierPerChannel, int...channels) {
		if(channels.length < 1 || channels.length > 4){
			throw new IllegalArgumentException(
					"Need to at least specify 1 channel, can at most specify 4 (ARGB). Got " + channels.length);
		}
		for(int ch: channels){
			if( ch < 0 || ch > 3 ){
				throw new IllegalArgumentException(String.format("Channels can be 0,1,2,3. But one channel is %d", ch));
			}
		}
		if(fourierPerChannel.length != channels.length) {
			throw new IllegalArgumentException(String.format(
					"Number of provided fourier images does not match number of specified channels. %d images, %d channels"
					,fourierPerChannel.length, channels.length));
		}
		Dimension dim = fourierPerChannel[0].getDimension();
		for(ComplexImg f: fourierPerChannel){
			if(!f.getDimension().equals(dim)){
				throw new IllegalArgumentException("Dimensions of fourier images, are inconsistent. They have to be equal.");
			}
		}
		// if no target was specified create a new one
		if(target == null) {
			target = new ColorImg(dim, Arrays.asList(channels).contains(ColorImg.channel_a));
		}
		// continue sanity checks
		if(!target.getDimension().equals(dim)){
			throw new IllegalArgumentException(String.format(
					"The specified target image has wrong dimensions (%s). Fourier images have %s.", 
					target.getDimension(), dim));
		}
		if(Arrays.asList(channels).contains(ColorImg.channel_a) && !target.hasAlpha()){
			throw new IllegalArgumentException(
					"One of the specified channels is alpha, but the specified target image has no alpha.");
		}
		// now do the transforms
		for(int c = 0; c < channels.length; c++){
			int ch = channels[c];
			ComplexImg fourier = fourierPerChannel[c];
			if(fourier.getCurrentXshift() != 0 || fourier.getCurrentYshift() != 0){
				ComplexValuedSampler complexIn = getSamplerForShiftedComplexImg(fourier);
				RowMajorArrayAccessor realOut = new RowMajorArrayAccessor(target.getData()[ch], target.getWidth(), target.getHeight());
				FFT.ifft(complexIn, realOut, realOut.getDimensions());
			} else {
				FFT.ifft(	fourier.getDataReal(), 
							fourier.getDataImag(), 
							target.getData()[ch], 
							target.getWidth(), target.getHeight());
			}
			double scaling = 1.0/target.numValues();
			ArrayUtils.scaleArray(target.getData()[ch], scaling);
		}
		
		return target;
	}
	
	private static ComplexValuedSampler getSamplerForShiftedComplexImg(ComplexImg img){
		int x = img.getCurrentXshift();
		int y = img.getCurrentYshift();
		int w = img.getWidth();
		int h = img.getHeight();
		
		RowMajorArrayAccessor real = new RowMajorArrayAccessor(img.getDataReal(), w,h);
		RowMajorArrayAccessor imag = new RowMajorArrayAccessor(img.getDataImag(), w,h);
		ComplexValuedSampler complex = real.combineToComplexSampler(imag);
		return (imaginary, coords) -> complex.getValueAt(imaginary, (coords[0]+w-x)%w, (coords[1]+h-y)%h);
	}
	
	
}
