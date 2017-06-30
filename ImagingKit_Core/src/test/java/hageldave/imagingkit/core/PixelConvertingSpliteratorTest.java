package hageldave.imagingkit.core;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.LinkedList;
import java.util.Spliterator;
import java.util.function.Consumer;
import java.util.stream.StreamSupport;

import org.junit.Test;

public class PixelConvertingSpliteratorTest {

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
				assertEquals("i="+i+" "+Integer.toHexString(img.getData()[i]), 0, Pixel.b(img.getData()[i]));
				assertEquals("i="+i+" "+Integer.toHexString(img.getData()[i]), 1+(i%0xfe), Pixel.r(img.getData()[i]));
				assertEquals("i="+i+" "+Integer.toHexString(img.getData()[i]), 0, Pixel.g(img.getData()[i]));
			}
		}
		
	}
	
}
