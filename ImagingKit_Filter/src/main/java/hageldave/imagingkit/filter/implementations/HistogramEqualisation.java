package hageldave.imagingkit.filter.implementations;

import java.util.function.Consumer;

import hageldave.imagingkit.core.ColorSpaceTransformation;
import hageldave.imagingkit.core.Img;
import hageldave.imagingkit.core.Pixel;
import hageldave.imagingkit.filter.ImgFilter;
import hageldave.imagingkit.filter.PerPixelFilter;
import hageldave.imagingkit.filter.ProgressListener;
import hageldave.imagingkit.filter.util.Histogram;

public class HistogramEqualisation implements ImgFilter {

	@Override
	public void applyTo(Img img, boolean parallelPreferred, int x, int y, int width, int height, ProgressListener progress) {
		
		Consumer<Pixel> progressNotifier = px->{};
		if(progress != null){
			progress.pushPendingFilter(this, height);
			progressNotifier = PerPixelFilter.getScanlineProgressNotificationConsumer(progress, this, height*2, x+height-1);
		}
		if(parallelPreferred){
			if(x == 0 && y == 0 && width == img.getWidth() && height == img.getHeight()){
				img.forEachParallel(ColorSpaceTransformation.RGB_2_HSV
						.andThen(progressNotifier));
				img.forEachParallel(equalization(Histogram.getHistogram(img).getEqualizationLUT(Histogram.BLUE_HIST))
						.andThen(ColorSpaceTransformation.HSV_2_RGB)
						.andThen(progressNotifier));
			} else {
				img.forEachParallel(x, y, width, height, ColorSpaceTransformation.RGB_2_HSV
						.andThen(progressNotifier));
				img.forEachParallel(x, y, width, height, equalization(Histogram.getHistogram(img).getEqualizationLUT(Histogram.BLUE_HIST))
						.andThen(ColorSpaceTransformation.HSV_2_RGB)
						.andThen(progressNotifier));
			}
		} else {
			if(x == 0 && y == 0 && width == img.getWidth() && height == img.getHeight()){
				img.forEach(ColorSpaceTransformation.RGB_2_HSV
						.andThen(progressNotifier));
				img.forEach(equalization(Histogram.getHistogram(img).getEqualizationLUT(Histogram.BLUE_HIST))
						.andThen(ColorSpaceTransformation.HSV_2_RGB)
						.andThen(progressNotifier));
				
			} else {
				img.forEach(x, y, width, height, ColorSpaceTransformation.RGB_2_HSV
						.andThen(progressNotifier));
				img.forEach(x, y, width, height, equalization(Histogram.getHistogram(img).getEqualizationLUT(Histogram.BLUE_HIST))
						.andThen(ColorSpaceTransformation.HSV_2_RGB)
						.andThen(progressNotifier));
			}
		}
		if(progress != null){
			progress.popFinishedFilter(this);
		}
		
		
	}
	
	private Consumer<Pixel> equalization(int[] lut){
		return px->px.setB(lut[px.b()]);
	}
	
	

}
