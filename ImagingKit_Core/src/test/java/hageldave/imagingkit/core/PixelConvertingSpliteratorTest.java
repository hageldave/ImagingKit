package hageldave.imagingkit.core;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertTrue;

import java.util.LinkedList;
import java.util.Spliterator;
import java.util.function.Consumer;
import java.util.stream.StreamSupport;

import org.junit.Test;

import hageldave.imagingkit.core.PixelConvertingSpliterator.PixelConverter;
import hageldave.imagingkit.core.scientific.ColorImg;

public class PixelConvertingSpliteratorTest {

	static final double eps = 0.000001;

	@Test
	public void testAll(){
		Img img = new Img(1000,1000);
		img.forEach(px->px.setB(1+(px.getIndex()%0xfe)));
		assertEquals(1, img.getData()[0]);

		Consumer<double[]> rotateChannels = (px) -> {double t=px[0]; px[0]=px[1]; px[1]=px[2]; px[2]=t;};

		{
			Spliterator<Pixel> delegate = img.spliterator();
			Spliterator<double[]> split = PixelConvertingSpliterator.getDoubletArrayElementSpliterator(delegate);

			assertTrue(split.hasCharacteristics(delegate.characteristics()));
			assertEquals(img.numValues(), split.getExactSizeIfKnown());
			LinkedList<Spliterator<double[]>> all = new LinkedList<>();
			all.add(split);
			int idx = 0;
			while(idx < all.size()){
				Spliterator<double[]> sp = all.get(idx);
				Spliterator<double[]> child = sp.trySplit();
				if(child != null){
					all.add(child);
				} else {
					idx++;
				}
			}
			for(Spliterator<double[]> iter: all){
				iter.tryAdvance(rotateChannels);
				iter.forEachRemaining(rotateChannels);
			}
			for(int i = 0; i < img.numValues(); i++){
				// g channel will contain special value
				assertEquals("i="+i+" "+Integer.toHexString(img.getData()[i]), 0, Pixel.b(img.getData()[i]));
				assertEquals("i="+i+" "+Integer.toHexString(img.getData()[i]), 1+(i%0xfe), Pixel.g(img.getData()[i]));
				assertEquals("i="+i+" "+Integer.toHexString(img.getData()[i]), 0, Pixel.r(img.getData()[i]));
			}
		}

		{
			img.setSpliteratorMinimumSplitSize(77);
			Spliterator<double[]> split = PixelConvertingSpliterator.getDoubletArrayElementSpliterator(img.colSpliterator());
			StreamSupport.stream(split, true).forEach(rotateChannels);

			for(int i = 0; i < img.numValues(); i++){
				// now r channel will contain special value
				assertEquals("i="+i+" "+Integer.toHexString(img.getData()[i]), 0, Pixel.b(img.getData()[i]));
				assertEquals("i="+i+" "+Integer.toHexString(img.getData()[i]), 1+(i%0xfe), Pixel.r(img.getData()[i]));
				assertEquals("i="+i+" "+Integer.toHexString(img.getData()[i]), 0, Pixel.g(img.getData()[i]));
			}
		}


		PixelConverter<PixelBase, double[]> converter = PixelConverter.fromFunctions(
				()->new double[3],
				(px,a)->{a[0]=px.r_asDouble();a[1]=px.g_asDouble();a[2]=px.b_asDouble();},
				(a,px)->{px.setRGB_fromDouble_preserveAlpha(a[0], a[1], a[2]);});


		{
			img.forEach(converter, false, rotateChannels);
			for(int i = 0; i < img.numValues(); i++){
				// b now
				assertEquals("i="+i+" "+Integer.toHexString(img.getData()[i]), 0, Pixel.r(img.getData()[i]));
				assertEquals("i="+i+" "+Integer.toHexString(img.getData()[i]), 1+(i%0xfe), Pixel.b(img.getData()[i]));
				assertEquals("i="+i+" "+Integer.toHexString(img.getData()[i]), 0, Pixel.g(img.getData()[i]));
			}
			img.forEach(converter, true, rotateChannels);
			for(int i = 0; i < img.numValues(); i++){
				// g again
				assertEquals("i="+i+" "+Integer.toHexString(img.getData()[i]), 0, Pixel.r(img.getData()[i]));
				assertEquals("i="+i+" "+Integer.toHexString(img.getData()[i]), 1+(i%0xfe), Pixel.g(img.getData()[i]));
				assertEquals("i="+i+" "+Integer.toHexString(img.getData()[i]), 0, Pixel.b(img.getData()[i]));
			}
			img.forEach(converter, false, 0, 0, img.getWidth(), 1, rotateChannels);
			for(int i = img.getWidth(); i < img.numValues(); i++){
				// g for all except first row
				assertEquals("i="+i+" "+Integer.toHexString(img.getData()[i]), 0, Pixel.r(img.getData()[i]));
				assertEquals("i="+i+" "+Integer.toHexString(img.getData()[i]), 1+(i%0xfe), Pixel.g(img.getData()[i]));
				assertEquals("i="+i+" "+Integer.toHexString(img.getData()[i]), 0, Pixel.b(img.getData()[i]));
			}
			for(int i = 0; i < img.getWidth(); i++){
				// r for first row
				assertEquals("i="+i+" "+Integer.toHexString(img.getData()[i]), 0, Pixel.g(img.getData()[i]));
				assertEquals("i="+i+" "+Integer.toHexString(img.getData()[i]), 1+(i%0xfe), Pixel.r(img.getData()[i]));
				assertEquals("i="+i+" "+Integer.toHexString(img.getData()[i]), 0, Pixel.b(img.getData()[i]));
			}
			img.forEach(converter, true, 0, 0, img.getWidth(), 1, rotateChannels);
			for(int i = img.getWidth(); i < img.numValues(); i++){
				// g for all except first row
				assertEquals("i="+i+" "+Integer.toHexString(img.getData()[i]), 0, Pixel.r(img.getData()[i]));
				assertEquals("i="+i+" "+Integer.toHexString(img.getData()[i]), 1+(i%0xfe), Pixel.g(img.getData()[i]));
				assertEquals("i="+i+" "+Integer.toHexString(img.getData()[i]), 0, Pixel.b(img.getData()[i]));
			}
			for(int i = 0; i < img.getWidth(); i++){
				// b for first row
				assertEquals("i="+i+" "+Integer.toHexString(img.getData()[i]), 0, Pixel.g(img.getData()[i]));
				assertEquals("i="+i+" "+Integer.toHexString(img.getData()[i]), 1+(i%0xfe), Pixel.b(img.getData()[i]));
				assertEquals("i="+i+" "+Integer.toHexString(img.getData()[i]), 0, Pixel.r(img.getData()[i]));
			}
		}

		{
			double sumb1 = img.stream().mapToDouble(px->px.b_asDouble()).sum();
			double sumb2 = img.stream(converter, false).mapToDouble(a->a[2]).sum();
			assertNotEquals(0, sumb1, eps);
			assertEquals(sumb1, sumb2, eps);
			double sumg1 = img.stream().mapToDouble(px->px.g_asDouble()).sum();
			double sumg2 = img.stream(converter, true).mapToDouble(a->a[1]).sum();
			assertNotEquals(0, sumg1, eps);
			assertEquals(sumg1, sumg2, eps);
			double sumw1 = img.stream(0, 0, img.getWidth(), 1).mapToDouble(px->px.r_asDouble()+px.g_asDouble()+px.b_asDouble()).sum();
			double sumw2 = img.stream(converter, false, 0, 0, img.getWidth(), 1).mapToDouble(a->a[0]+a[1]+a[2]).sum();
			assertNotEquals(0, sumw1, eps);
			assertEquals(sumw1, sumw2, eps);
		}

		{	// this is rather a test for correct generic definition
			Consumer<int[]> rotateChannelsInt = (px) -> {int t=px[0]; px[0]=px[1]; px[1]=px[2]; px[2]=t;};
			// test if its possible to infer correct types <Pixel,int[]> instead of <PixelBase,int[]> for PixelConverter
			// conclusion: yes this compiles so its all good
			img.forEach(PixelConverter.fromFunctions(
						()->new int[3],
						(px,a)->{a[0]=px.r();a[1]=px.g();a[2]=px.b();},
						(a,px)->px.setRGB_preserveAlpha(a[0], a[1], a[2])),
					true, 0, 0, img.getWidth(), 1, rotateChannelsInt);
			for(int i = img.getWidth(); i < img.numValues(); i++){
				// g for all except first row
				assertEquals("i="+i+" "+Integer.toHexString(img.getData()[i]), 0, Pixel.r(img.getData()[i]));
				assertEquals("i="+i+" "+Integer.toHexString(img.getData()[i]), 1+(i%0xfe), Pixel.g(img.getData()[i]));
				assertEquals("i="+i+" "+Integer.toHexString(img.getData()[i]), 0, Pixel.b(img.getData()[i]));
			}
			for(int i = 0; i < img.getWidth(); i++){
				// g for first row
				assertEquals("i="+i+" "+Integer.toHexString(img.getData()[i]), 0, Pixel.r(img.getData()[i]));
				assertEquals("i="+i+" "+Integer.toHexString(img.getData()[i]), 1+(i%0xfe), Pixel.g(img.getData()[i]));
				assertEquals("i="+i+" "+Integer.toHexString(img.getData()[i]), 0, Pixel.b(img.getData()[i]));
			}
		}

		{
			ColorImg dimg = new ColorImg(40, 40, true);
			dimg.setValueR(1, 2, -3);
			dimg.setValueB(22, 33, -1);
			long numNegativeSums = dimg.stream(
					PixelConverter.fromFunctions(
							()->new float[1],
							(px,f)->f[0]=(float)(px.r_asDouble()+px.g_asDouble()+px.b_asDouble()),
							null),
					true)
			.filter(f->f[0] < 0)
			.count();
			assertEquals(2, numNegativeSums);
		}

	}

}
