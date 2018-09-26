package hageldave.imagingkit.fourier.examples;

import hageldave.imagingkit.core.scientific.ColorImg;
import hageldave.imagingkit.core.util.ImageFrame;
import hageldave.imagingkit.fourier.ComplexImg;
import hageldave.imagingkit.fourier.Fourier;

public class CodeSnippets {

	public static void filtering() {
		ColorImg img = new ColorImg(128,128,false);
		img.paint(g2d->g2d.fillRect(64-16, 64-8, 32, 16));
		ImageFrame.display(img.getRemoteBufferedImage()).setTitle("original");
		ComplexImg fourier = Fourier.transform(img, ColorImg.channel_r);
		fourier.shiftCornerToCenter();
		ImageFrame.display(fourier.getPowerSpectrumImg().toImg()).setTitle("fft");
		fourier.forEach(px->{
			int xfreq = px.getXFrequency();
			int yfreq = px.getYFrequency();
			double freqRadius = Math.sqrt(xfreq*xfreq+yfreq*yfreq);
			double gaussian = Math.exp(-freqRadius/(0.05*128));
			px.mult(gaussian, 0);
		});
		ImageFrame.display(fourier.getPowerSpectrumImg().toImg()).setTitle("filtered fft");
		ColorImg restored = Fourier.inverseTransform(null, fourier, ColorImg.channel_r);
		ColorImg redChannel = restored.getChannelImage(ColorImg.channel_r);
		ImageFrame.display(redChannel.toImg()).setTitle("filterd original");
	}
	
}
