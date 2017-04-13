package hageldave.imagingkit.filter.implementations;

import java.util.function.Consumer;

import hageldave.imagingkit.core.Img;
import hageldave.imagingkit.core.Pixel;
import hageldave.imagingkit.filter.PerPixelFilter;
import hageldave.imagingkit.filter.util.Histogram;

public class HistogramEqualisation implements PerPixelFilter {

	@Override
	public Consumer<Pixel> consumer() {
		return new Consumer<Pixel>() {


			Histogram h = null;
			int[] r,g,b,a;

			@Override
			public void accept(Pixel p) {
				if(h == null){
					makeHistogram(p.getImg());
				}
				p.setARGB(a[p.a()], r[p.r()], g[p.g()], b[p.b()]);
			}

			private synchronized void makeHistogram(Img img){
				if(h == null){
					h = Histogram.getHistogram(img);
					r = h.getEqualizationLUT(Histogram.RED_HIST);
					g = h.getEqualizationLUT(Histogram.GREEN_HIST);
					b = h.getEqualizationLUT(Histogram.BLUE_HIST);
					a = h.getEqualizationLUT(Histogram.ALPHA_HIST);
				}
			}

		};
	}



}
