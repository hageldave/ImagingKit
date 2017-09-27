package hageldave.imagingkit.core;

import static org.junit.Assert.*;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.image.BufferedImage;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Spliterator;
import java.util.function.BiFunction;
import java.util.function.Consumer;

import org.junit.Test;

import hageldave.imagingkit.core.PixelConvertingSpliterator.PixelConverter;

public class ImgTest {

	static final double eps = 0.000001;
	
	@Test
	public void channelMethods_test(){
		int color = 0xffaa1244;
		assertEquals(0xff, Pixel.a(color));
		assertEquals(0xaa, Pixel.r(color));
		assertEquals(0x12, Pixel.g(color));
		assertEquals(0x44, Pixel.b(color));
		assertEquals(1.0, Pixel.a_normalized(color), 0);
		assertEquals(0xaa/255.0, Pixel.r_normalized(color), eps);
		assertEquals(0x12/255.0, Pixel.g_normalized(color), eps);
		assertEquals(0x44/255.0, Pixel.b_normalized(color), eps);

		assertEquals(0x01001234, Pixel.argb_fast(0x01, 0x00, 0x12, 0x34));
		assertEquals(0xff543210, Pixel.rgb_fast(0x54, 0x32, 0x10));
		assertEquals(0xff00ff54, Pixel.rgb_bounded(-12, 260, 0x54));
		assertEquals(0xff000000, Pixel.rgb_bounded(-2, -260, -16));
		assertEquals(0xff887799, Pixel.rgb_bounded(0x88, 0x77, 0x99));
		assertEquals(0xffffffff, Pixel.rgb_bounded(260, 256, 258));
		assertEquals(0x8800ff54, Pixel.argb_bounded(0x88, -12, 260, 0x54));
		assertEquals(0x00000000, Pixel.argb_bounded(-4 ,-2, -260, -16));
		assertEquals(0x66887799, Pixel.argb_bounded(0x66, 0x88, 0x77, 0x99));
		assertEquals(0xffffffff, Pixel.argb_bounded(1022, 260, 256, 258));
		assertEquals(0xffffffff, Pixel.rgb(0x15ff, 0xaff, 0x5cff));
		assertEquals(0x44770122, Pixel.argb(0x44, 0x177, 0x101, 0x222));
		assertEquals(0xffff00ff, Pixel.argb_fromNormalized(1, 1, 0, 1));
		assertEquals(0x0000ff00, Pixel.argb_fromNormalized(0, 0, 1, 0));
		assertEquals(0x66778899, Pixel.argb_fromNormalized(0x66/255.0, 0x77/255.0, 0x88/255.0, 0x99/255.0));
		assertEquals(0xff778899, Pixel.rgb_fromNormalized(0x77/255.0, 0x88/255.0, 0x99/255.0));
		assertEquals(0x44, Pixel.getGrey(0xffff44, 0, 0, 2));
		assertEquals(0x77, Pixel.getGrey(0xff7744, 0, 3, 0));
		assertEquals(0x99, Pixel.getGrey(0x99ff44, 4, 0, 0));
		assertEquals(128, Pixel.getGrey(Pixel.rgb(127, 128, 129), 1, 1, 1));
		assertEquals(100, Pixel.getGrey(Pixel.rgb(80, 80, 120), 1, 1, 2));
		assertEquals(0xaa, Pixel.getLuminance(0xffaaaaaa));

		Img img = new Img(11,11);
		Pixel p = new Pixel(img, 0);
		assertEquals(img, p.getSource());
		assertEquals(0, p.getValue());
		p.setR(0xff);
		assertEquals(0x00ff0000, p.getValue());
		p.setG(0xaa);
		assertEquals(0x00ffaa00, p.getValue());
		p.setB(0xcdef);
		assertEquals(0x00ffaaef, p.getValue());
		p.setA(p.r());
		assertEquals(0xffffaaef, p.getValue());

		p.setA(0x44);
		p.setRGB_preserveAlpha(0x88, 0x77, 0x66);
		assertEquals(0x44887766, p.getValue());
		p.setRGB_fromDouble_preserveAlpha(0, 0, 0);
		assertEquals(0x44000000, p.getValue());
		p.setRGB_fromDouble_preserveAlpha(0.4f, 0.7f, 0.3f);
		assertEquals(p.getValue(), Pixel.argb_fromNormalized(p.a_asDouble(), p.r_asDouble(), p.g_asDouble(), p.b_asDouble()));
		{
			int temp = p.getValue();
			p.setARGB_fromDouble(p.a_asDouble(), p.r_asDouble(), p.g_asDouble(), p.b_asDouble());
			assertEquals(temp, p.getValue());
		}
		assertEquals(0x44/255f, p.a_asDouble(), eps);
		assertEquals(0.4f, p.r_asDouble(), 0.01);
		assertEquals(0.7f, p.g_asDouble(), 0.01);
		assertEquals(0.3f, p.b_asDouble(), 0.01);
		p.setARGB(0x22, 0x11, 0x44, 0x33);
		assertEquals(0x22114433, p.getValue());
		p.setARGB_fromDouble(1, 0x33/255.0, 0x70/255.0, 0);
		assertEquals(0xff337000, p.getValue());
		p.setRGB(0x22, 0x33, 0x44);
		assertEquals(0xff223344, p.getValue());
		p.setRGB_fromDouble(0x88/255d, 0xee/255d, 0xcc/255f);
		assertEquals(0xff88eecc, p.getValue());
		p.setA(0x44);
		assertEquals(0x44, p.a());
		assertEquals(0x88, p.r());
		assertEquals(0xee, p.g());
		assertEquals(0xcc, p.b());


		assertEquals(0, p.getX());
		assertEquals(0, p.getY());
		assertEquals(0, p.getXnormalized(), 0);
		assertEquals(0, p.getYnormalized(), 0);
		p.setPosition(5, 0);
		assertEquals(5, p.getX());
		assertEquals(0, p.getY());
		assertEquals(0.5f, p.getXnormalized(), 0);
		assertEquals(0, p.getYnormalized(), 0);
		p.setPosition(5, 5);
		assertEquals(5, p.getX());
		assertEquals(5, p.getY());
		assertEquals(0.5f, p.getXnormalized(), 0);
		assertEquals(0.5f, p.getYnormalized(), 0);
		p.setPosition(10, 10);
		assertEquals(10, p.getX());
		assertEquals(10, p.getY());
		assertEquals(1, p.getXnormalized(), 0);
		assertEquals(1, p.getYnormalized(), 0);

		color = 0x88997744;
		p.setValue(color);
		assertEquals(color, p.getValue());
		assertEquals(Pixel.getGrey(color, 4, 2, 1), p.getGrey(4, 2, 1));
		assertEquals(Pixel.getLuminance(color), p.getLuminance());
		
		img.getPixel(0, 0).setA_fromDouble(1).setR_fromDouble(0).setG_fromDouble(1).setB_fromDouble(0);
		assertEquals(0xff00ff00, img.getPixel(0,0).getValue());
		img.getPixel(1, 1).setA_fromDouble(100).setR_fromDouble(-1).setG_fromDouble(2).setB_fromDouble(-0.1);
		assertEquals(0xff00ff00, img.getPixel(1,1).getValue());
	}

	@Test
	public void misc_test(){
		JunitUtils.testException(()->{new Img(1, 2, new int[]{0});}, IllegalArgumentException.class);
		JunitUtils.testException(()->{new Img(10,10).setSpliteratorMinimumSplitSize(0);}, IllegalArgumentException.class);
		assertTrue(new Img(1,1).supportsRemoteBufferedImage());
		assertEquals(100, new Img(new Dimension(100, 200)).getWidth());
		assertEquals(200, new Img(new Dimension(100, 200)).getHeight());
		assertFalse(new Img(1, 1).getPixel().toString().isEmpty());
	}

	@Test
	public void boundaryModes_test(){
		Img img = new Img(4, 4, new int[]
				{
						0,1,2,3,
						4,5,6,7,
						8,9,9,9,
						9,9,9,9
				});
		for(int mode: new int[]{Img.boundary_mode_zero, Img.boundary_mode_mirror, Img.boundary_mode_repeat_edge, Img.boundary_mode_repeat_image}){
			// test corners
			assertEquals(0, img.getValue(0, 0, mode));
			assertEquals(3, img.getValue(3, 0, mode));
			assertEquals(9, img.getValue(0, 3, mode));
			assertEquals(9, img.getValue(3, 3, mode));
		}
		assertEquals(0, img.getValue(-1, 0, Img.boundary_mode_zero));
		assertEquals(0, img.getValue(4, 0, Img.boundary_mode_zero));
		assertEquals(0, img.getValue(0, -1, Img.boundary_mode_zero));
		assertEquals(0, img.getValue(0, 4, Img.boundary_mode_zero));

		assertEquals(0xff112233, img.getValue(-1, 0, 0xff112233));
		assertEquals(0xff112233, img.getValue(4, 0,  0xff112233));
		assertEquals(0xff112233, img.getValue(0, -1, 0xff112233));
		assertEquals(0xff112233, img.getValue(0, 4,  0xff112233));

		assertEquals(0, img.getValue(-2, 0, Img.boundary_mode_repeat_edge));
		assertEquals(3, img.getValue(3, -2, Img.boundary_mode_repeat_edge));
		assertEquals(9, img.getValue(-10, 10, Img.boundary_mode_repeat_edge));
		assertEquals(3, img.getValue(10, -10, Img.boundary_mode_repeat_edge));

		for(int y = 0; y < 4; y++)
		for(int x = 0; x < 4; x++){
			assertEquals(img.getValue(x, y), img.getValue(x+4, y, Img.boundary_mode_repeat_image));
			assertEquals(img.getValue(x, y), img.getValue(x, y+4, Img.boundary_mode_repeat_image));
			assertEquals(img.getValue(x, y), img.getValue(x-4, y, Img.boundary_mode_repeat_image));
			assertEquals(img.getValue(x, y), img.getValue(x, y-4, Img.boundary_mode_repeat_image));
			assertEquals(img.getValue(x, y), img.getValue(x+8, y+8, Img.boundary_mode_repeat_image));
			assertEquals(img.getValue(x, y), img.getValue(x-8, y-8, Img.boundary_mode_repeat_image));
		}

		for(int y = 0; y < 4; y++)
		for(int x = 0; x < 4; x++){
			assertEquals(img.getValue(x, y), img.getValue(x+8, y+8, Img.boundary_mode_mirror));
			assertEquals(img.getValue(x, y), img.getValue(7-x, 7-y, Img.boundary_mode_mirror));
			assertEquals(img.getValue(x, y), img.getValue(-8+x, -8+y, Img.boundary_mode_mirror));
			assertEquals(img.getValue(x, y), img.getValue(-1-x, -1-y, Img.boundary_mode_mirror));
		}

	}

	@Test
	public void pixelRetrieval_test(){
		Img img = new Img(4, 3, new int[]
				{
						0,1,2,3,
						4,5,6,7,
						8,9,9,5
				});

		assertEquals(img.getData().length, img.getWidth()*img.getHeight());
		assertEquals(img.getData().length, img.numValues());

		int i = 0;
		for(int y = 0; y < img.getHeight(); y++)
		for(int x = 0; x < img.getWidth(); x++){
			assertEquals(img.getData()[i], img.getValue(x, y));
			i++;
		}

		// test interpolation
		img = new Img(2,2, new int[]
				{
						0,4,
						8,16
				});
		assertEquals(2, img.interpolateARGB(0.5f, 0));
		assertEquals(4, img.interpolateARGB(0, 0.5f));
		assertEquals(12, img.interpolateARGB(0.5f, 1));
		assertEquals(10, img.interpolateARGB(1, 0.5f));
		assertEquals(7, img.interpolateARGB(0.5f, 0.5f));

		img = new Img(5,3, new int[]
				{
					0,1,2,3,4,
					2,3,4,5,6,
					4,5,6,7,8
				});

		assertEquals(img.getValue(0, 0), img.interpolateARGB(0, 0));
		assertEquals(img.getValue(img.getWidth()-1, img.getHeight()-1), img.interpolateARGB(1, 1));
		assertEquals(img.getValue(0, img.getHeight()-1), img.interpolateARGB(0, 1));
		assertEquals(img.getValue(img.getWidth()-1, 0), img.interpolateARGB(1, 0));
		assertEquals(img.getValue(2, 1), img.interpolateARGB(0.5f, 0.5f));

		// test copypixels
		Img img2 = new Img(2,2);
		img.copyArea(0, 0, 2, 2, img2, 0, 0);
		assertEquals(img.getValue(0, 0), img2.getValue(0, 0));
		assertEquals(img.getValue(1, 1), img2.getValue(1, 1));
		img.copyArea(1, 1, 2, 2, img2, 0, 0);
		assertEquals(img.getValue(1, 1), img2.getValue(0, 0));
		assertEquals(img.getValue(2, 2), img2.getValue(1, 1));
		img.copyArea(4, 2, 1, 1, img2, 1, 0);
		assertEquals(img.getValue(4, 2), img2.getValue(1, 0));
	}

	@Test
	public void copyArea_test(){
		// testing copy with overlapping area

		Img source = new Img(5, 10);
		Img target = new Img(10, 5);
		source.fill(1);
		for(int value: source.getData()){
			assertEquals(1, value);
		}
		for(int value: target.getData()){
			assertEquals(0, value);
		}

		source.copyArea(0, 0, 5, 10, target, 0, 0);
		for(int y = 0; y < target.getHeight(); y++)
		for(int x = 0; x < target.getWidth(); x++){
			if(x < 5){
				assertEquals(1, target.getValue(x, y));
			} else {
				assertEquals(0, target.getValue(x, y));
			}
		}

		target.fill(0);
		source.copyArea(0, 0, 5, 10, target, -1, 0);
		for(int y = 0; y < target.getHeight(); y++)
		for(int x = 0; x < target.getWidth(); x++){
			if(x < 4){
				assertEquals(1, target.getValue(x, y));
			} else {
				assertEquals(0, target.getValue(x, y));
			}
		}

		target.fill(0);
		source.copyArea(0, 0, 5, 10, target, 0, -6);
		for(int y = 0; y < target.getHeight(); y++)
		for(int x = 0; x < target.getWidth(); x++){
			if(x < 5 && y < 4){
				assertEquals(1, target.getValue(x, y));
			} else {
				assertEquals(0, target.getValue(x, y));
			}
		}

		target.fill(0);
		source.copyArea(0, 0, 5, 10, target, 1, 0);
		for(int y = 0; y < target.getHeight(); y++)
		for(int x = 0; x < target.getWidth(); x++){
			if(x > 0 && x < 6){
				assertEquals(1, target.getValue(x, y));
			} else {
				assertEquals(0, target.getValue(x, y));
			}
		}

		target.fill(0);
		source.copyArea(0, 0, 5, 10, target, 0, 1);
		for(int y = 0; y < target.getHeight(); y++)
		for(int x = 0; x < target.getWidth(); x++){
			if(x < 5 && y > 0){
				assertEquals(1, target.getValue(x, y));
			} else {
				assertEquals(0, target.getValue(x, y));
			}
		}

		target.fill(0);
		source.copyArea(0, 0, 5, 10, target, -5, 0);
		for(int color: target.getData()){
			assertEquals(0, color);
		}

		target.fill(0);
		source.copyArea(0, 0, 5, 10, target, 0, -10);
		for(int color: target.getData()){
			assertEquals(0, color);
		}

		// same width images
		target = new Img(5, 9);

		source.copyArea(0, 0, 5, 10, target, 0, 0);
		for(int value: target.getData()){
			assertEquals(1, value);
		}

		target.fill(0);
		source.copyArea(0, 1, 5, 8, target, 0, 0);
		for(int y = 0; y < target.getHeight(); y++)
		for(int x = 0; x < target.getWidth(); x++){
			if(y < 8){
				assertEquals(1, target.getValue(x, y));
			} else {
				assertEquals(0, target.getValue(x, y));
			}
		}

		target.fill(0);
		source.copyArea(0, 1, 5, 9, target, 0, 1);
		for(int y = 0; y < target.getHeight(); y++)
		for(int x = 0; x < target.getWidth(); x++){
			if(y > 0){
				assertEquals(1, target.getValue(x, y));
			} else {
				assertEquals(0, target.getValue(x, y));
			}
		}

		target.fill(0);
		source.copyArea(0, 1, 5, 8, target, 0, -1);
		for(int y = 0; y < target.getHeight(); y++)
		for(int x = 0; x < target.getWidth(); x++){
			if(y < 7){
				assertEquals(1, target.getValue(x, y));
			} else {
				assertEquals(0, target.getValue(x, y));
			}
		}

		target.fill(0);
		source.copyArea(0, 0, 5, 9, target, 0, -10);
		for(int color: target.getData()){
			assertEquals(0, color);
		}

		// copy to new source
		Img result = source.copyArea(0, 0, 5, 4, null, 0, 0);
		assertEquals(5, result.getWidth());
		assertEquals(4, result.getHeight());
		for(int y = 0; y < result.getHeight(); y++)
		for(int x = 0; x < result.getWidth(); x++){
			assertEquals(source.getValue(x, y), result.getValue(x, y));
		}

		result = source.copy();
		for(int i = 0; i < source.numValues(); i++){
			assertEquals(source.getData()[i], result.getData()[i]);
		}

		// exceptions
		JunitUtils.testException(()->{source.copyArea(0,0,0,1,null,0,0);}, IllegalArgumentException.class);
		JunitUtils.testException(()->{source.copyArea(0,0,1,0,null,0,0);}, IllegalArgumentException.class);
		JunitUtils.testException(()->{source.copyArea(-1,0,2,2,null,0,0);}, IllegalArgumentException.class);
		JunitUtils.testException(()->{source.copyArea(0,-1,2,2,null,0,0);}, IllegalArgumentException.class);
		JunitUtils.testException(()->{source.copyArea(0,0,20,2,null,0,0);}, IllegalArgumentException.class);
		JunitUtils.testException(()->{source.copyArea(0,0,2,20,null,0,0);}, IllegalArgumentException.class);
		JunitUtils.testException(()->{source.copyArea(0,0,-1,1,null,0,0);}, IllegalArgumentException.class);
	}

	@Test
	public void buffimg_test(){
		BufferedImage bimg = new BufferedImage(4, 4, BufferedImage.TYPE_INT_ARGB);
		bimg.setRGB(0, 0, 1, 1, new int[]
				{
					0,1,2,3,
					4,5,6,7,
					8,9,0,1,
					2,3,4,5
				}, 0, 4);

		{
			// test same pixels
			Img img = new Img(bimg);
			for(int y = 0; y < 4; y++)
			for(int x = 0; x < 4; x++){
			assertEquals(bimg.getRGB(x, y), img.getValue(x, y));
			}
		}

		{
			// test remoteness
			Img img = new Img(bimg);
			Img img2 = Img.createRemoteImg(bimg);
			for(int y = 0; y < 4; y++)
			for(int x = 0; x < 4; x++){
				img2.setValue(x, y, -2000-x-y);
				assertNotEquals(bimg.getRGB(x, y), img.getValue(x, y));
				assertEquals(bimg.getRGB(x, y), img2.getValue(x, y));
			}
		}

		{
			// test remoteness in both directions
			Img img = Img.createRemoteImg(bimg);
			BufferedImage r_bimg = img.getRemoteBufferedImage();
			for(int y = 0; y < 4; y++)
			for(int x = 0; x < 4; x++){
				assertEquals(bimg.getRGB(x, y), r_bimg.getRGB(x, y));
				bimg.setRGB(x, y, x+y+144);
				assertEquals(bimg.getRGB(x, y), r_bimg.getRGB(x, y));
				r_bimg.setRGB(x, y, x+y+166);
				assertEquals(bimg.getRGB(x, y), r_bimg.getRGB(x, y));
			}
		}

		{
			// test toBufferedImage
			Img img = new Img(3, 3, new int[]
					{ 1,2,3,
					  4,5,6,
					  7,8,9  });
			BufferedImage bimg2 = img.toBufferedImage();
			for(int i = 0; i < img.numValues(); i++){
				assertEquals(img.getValue(i%3, i/3), bimg2.getRGB(i%3, i/3));
				img.setValue(i%3, i/3, 2000-i);
				assertNotEquals(img.getValue(i%3, i/3), bimg2.getRGB(i%3, i/3));
			}
		}

		{
			// test exceptions
			BufferedImage bimg2 = new BufferedImage(1,1,BufferedImage.TYPE_BYTE_BINARY);
			JunitUtils.testException(()->{Img.createRemoteImg(bimg2);}, IllegalArgumentException.class);
			JunitUtils.testException(()->{new Img(3,3).toBufferedImage(new BufferedImage(4, 3, BufferedImage.TYPE_INT_ARGB));}, IllegalArgumentException.class);
		}

	}

	@Test
	public void iterable_test(){
		BiFunction<Integer, Integer, Img> stdAlloc = (w,h)->{return new Img(w,h);};
		iterable_test(stdAlloc);

		BiFunction<Integer, Integer, Img> rowsplitAlloc = (w,h)->{return new Img(w,h){
			@Override
			public Spliterator<Pixel> spliterator() {
				return this.rowSpliterator();
			}
		};};
		iterable_test(rowsplitAlloc);

		BiFunction<Integer, Integer, Img> colsplitAlloc = (w,h)->{return new Img(w,h){
			@Override
			public Spliterator<Pixel> spliterator() {
				return this.colSpliterator();
			}
		};};
		iterable_test(colsplitAlloc);
	}

	private void iterable_test(BiFunction<Integer, Integer, Img> imgAlloc){
		// iterator
		{
			Img img = imgAlloc.apply(16,9);
			int idx = 0;
			for(Pixel p: img){
				p.setValue(p.getValue()+idx);
				idx++;
			}
			assertEquals(img.numValues(), idx);
			for(int i = 0; i < img.numValues(); i++){
				assertEquals(i, img.getData()[i]);
			}

			img.forEach_defaultimpl(px->{
				assertEquals(px.getIndex(), px.getValue());
			});
		}

		{
			Img img = imgAlloc.apply(16,9);
			img.iterator().forEachRemaining((px)->{px.setValue(px.getValue()+px.getIndex());});
			for(int i = 0; i < img.numValues(); i++){
				assertEquals(i, img.getData()[i]);
			}
		}

		{
			Img img = imgAlloc.apply(16,9);
			Iterator<Pixel> it = img.iterator();
			while(it.hasNext()){
				Pixel px = it.next();
				px.setValue(px.getValue()+px.getIndex());
			}
			for(int i = 0; i < img.numValues(); i++){
				assertEquals(i, img.getData()[i]);
			}
		}

		{
			Img img = imgAlloc.apply(16,9);
			img.forEach((px)->{px.setValue(px.getIndex());});
			for(int i = 0; i < img.numValues(); i++){
				assertEquals(i, img.getData()[i]);
			}
		}

		// area iterator
		{
			Img img = imgAlloc.apply(16, 9);
			Iterator<Pixel> iter = img.iterator(2, 3, 10, 5);
			while(iter.hasNext()){
				Pixel px = iter.next();
				px.setValue(1+px.getX()+px.getY());
			}
			for(int y = 0; y < 9; y++){
				for(int x = 0; x < 16; x++){
					if(x < 2 || x >=10+2 || y < 3 || y >= 5+3){
						assertEquals(0, img.getValue(x, y));
					} else {
						assertEquals(1+x+y, img.getValue(x, y));
					}
				}
			}
			img.fill(0);
			iter = img.iterator(2, 3, 10, 5);
			for(int i = 0; i < 10; i++){
				Pixel px = iter.next();
				px.setValue(1+px.getX()+px.getY());
			}
			iter.forEachRemaining(px->{px.setValue(1+px.getX()+px.getY());});
			for(int y = 0; y < 9; y++){
				for(int x = 0; x < 16; x++){
					if(x < 2 || x >=10+2 || y < 3 || y >= 5+3){
						assertEquals(0, img.getValue(x, y));
					} else {
						assertEquals(1+x+y, img.getValue(x, y));
					}
				}
			}

			JunitUtils.testException(()->{img.iterator(2,3,10,8);}, IllegalArgumentException.class);
		}

		// spliterator
		{
			Img img = imgAlloc.apply(2000, 400);
			Spliterator<Pixel> split = img.spliterator();
			assertTrue(split.hasCharacteristics(Spliterator.SUBSIZED));
			assertEquals(img.numValues(), split.getExactSizeIfKnown());
			LinkedList<Spliterator<Pixel>> all = new LinkedList<>();
			all.add(split);
			int idx = 0;
			while(idx < all.size()){
				Spliterator<Pixel> sp = all.get(idx);
				Spliterator<Pixel> child = sp.trySplit();
				if(child != null){
					all.add(child);
				} else {
					idx++;
				}
			}
			for(Spliterator<Pixel> iter: all){
				iter.tryAdvance((px) -> {px.setValue(px.getValue()+px.getIndex());});
				iter.forEachRemaining((px) -> {px.setValue(px.getValue()+px.getIndex());});
			}
			for(int i = 0; i < img.numValues(); i++){
				assertEquals(i, img.getData()[i]);
			}
		}
		{
			Img img = imgAlloc.apply(200, 400);
			Spliterator<Pixel> split = img.spliterator();
			LinkedList<Spliterator<Pixel>> all = new LinkedList<>();
			all.add(split);
			int idx = 0;
			while(idx < all.size()){
				Spliterator<Pixel> sp = all.get(idx);
				Spliterator<Pixel> child = sp.trySplit();
				if(child != null){
					all.add(child);
				} else {
					idx++;
				}
			}
			for(Spliterator<Pixel> iter: all){
				while(iter.tryAdvance((px) -> {px.setValue(px.getValue()+px.getIndex());}));
			}
			for(int i = 0; i < img.numValues(); i++){
				assertEquals(i, img.getData()[i]);
			}
		}

		{
			Img img = imgAlloc.apply(100, 100);
			Spliterator<Pixel> split = img.spliterator();
			while(split.tryAdvance(px->px.setValue(px.getIndex())));
			for(int i = 0; i < img.numValues(); i++){
				assertEquals(i, img.getData()[i]);
			}
		}

		// area spliterator
		{
			Img img = imgAlloc.apply(2000, 400);
			Spliterator<Pixel> split = img.spliterator(40,10,500,300);
			assertTrue(split.hasCharacteristics(Spliterator.SUBSIZED));
			assertEquals(500*300, split.getExactSizeIfKnown());
			LinkedList<Spliterator<Pixel>> all = new LinkedList<>();
			all.add(split);
			int idx = 0;
			while(idx < all.size()){
				Spliterator<Pixel> sp = all.get(idx);
				Spliterator<Pixel> child = sp.trySplit();
				if(child != null){
					all.add(child);
				} else {
					idx++;
				}
			}
			for(Spliterator<Pixel> iter: all){
				iter.tryAdvance((px) -> {px.setValue(px.getValue()+px.getIndex());});
				iter.forEachRemaining((px) -> {px.setValue(px.getValue()+px.getIndex());});
			}
			for(Pixel px: img){
				if(px.getX() < 40 || px.getX() >= 40+500 || px.getY() < 10 || px.getY() >= 10+300){
					assertEquals(0, px.getValue());
				} else {
					assertEquals(px.getIndex(), px.getValue());
				}
			}
		}
		{
			Img img = imgAlloc.apply(1023, 1023);
			Spliterator<Pixel> split = img.spliterator(40,10,500,300);
			LinkedList<Spliterator<Pixel>> all = new LinkedList<>();
			all.add(split);
			int idx = 0;
			while(idx < all.size()){
				Spliterator<Pixel> sp = all.get(idx);
				Spliterator<Pixel> child = sp.trySplit();
				if(child != null){
					all.add(child);
				} else {
					idx++;
				}
			}
			for(Spliterator<Pixel> iter: all){
				while(iter.tryAdvance((px) -> {
					px.setValue(px.getValue()+px.getIndex());
				}));
			}
			for(Pixel px: img){
				if(px.getX() < 40 || px.getX() >= 40+500 || px.getY() < 10 || px.getY() >= 10+300){
					assertEquals(0, px.getValue());
				} else {
					assertEquals(px.getIndex(), px.getValue());
				}
			}
		}
		{
			Img img = imgAlloc.apply(64,64);
			if(img.getSpliteratorMinimumSplitSize() != 24)
				img.setSpliteratorMinimumSplitSize(24);
			Spliterator<Pixel> split = img.spliterator(2,2,50,50);
			LinkedList<Spliterator<Pixel>> all = new LinkedList<>();
			all.add(split);
			int idx = 0;
			while(idx < all.size()){
				Spliterator<Pixel> sp = all.get(idx);
				Spliterator<Pixel> child = sp.trySplit();
				if(child != null){
					all.add(child);
				} else {
					idx++;
				}
			}
			for(Spliterator<Pixel> iter: all){
				Consumer<Pixel> consumer = (px) -> {
					px.setValue(px.getValue()+px.getIndex());
				};
				iter.tryAdvance(consumer);
				iter.forEachRemaining(consumer);
			}
			for(Pixel px: img){
				if(px.getX() < 2 || px.getX() >= 2+50 || px.getY() < 2 || px.getY() >= 2+50){
					assertEquals(0, px.getValue());
				} else {
					assertEquals(px.getIndex(), px.getValue());
				}
			}

		}

		// parallel foreach
		{
			Img img = imgAlloc.apply(3000, 2000);
			img.forEach(true, (px)->{px.setValue(px.getIndex());} );
			for(int i = 0; i < img.numValues(); i++){
				assertEquals(i, img.getData()[i]);
			}
		}

		// foreach area
		{
			Img img = imgAlloc.apply(2000,400);
			img.forEach(40, 80, 500, 100, (px)->{px.setValue(px.getValue()+px.getIndex());});
			for(int i = 0; i < img.numValues(); i++){
				int x = i % img.getWidth(); x-=40;
				int y = i / img.getWidth(); y-=80;
				if(x >= 0 && x < 500 && y >= 0 && y < 100){
					assertEquals(i, img.getData()[i]);
				} else {
					assertEquals(0, img.getData()[i]);
				}
			}
		}

		// parallel foreach area
		{
			Img img = imgAlloc.apply(3000,2000);
			img.forEach(true, 40, 80, 1000, 500, (px)->{px.setValue(px.getValue()+px.getIndex());});
			for(int i = 0; i < img.numValues(); i++){
				int x = i % img.getWidth(); x-=40;
				int y = i / img.getWidth(); y-=80;
				if(x >= 0 && x < 1000 && y >= 0 && y < 500){
					assertEquals(i, img.getData()[i]);
				} else {
					assertEquals(0, img.getData()[i]);
				}
			}

			JunitUtils.testException(()->{
				img.forEach(true, 0, 0, 3000,2001, (px)->{});
			}, IllegalArgumentException.class);
		}

		// streams
		{
			Img img = imgAlloc.apply(3000, 2000);
			img.stream().filter(px->{return px.getX() % 2 == 0;}).forEach(px->{px.setValue(1);});
			for(int y = 0; y < 2000; y++)
			for(int x = 0; x < 3000; x++){
				assertEquals((x+1)%2, img.getValue(x, y));
			}
			img.fill(0);
			img.stream(true).filter(px->{return px.getX() % 2 == 0;}).forEach(px->{px.setValue(1);});
			for(int y = 0; y < 2000; y++)
			for(int x = 0; x < 3000; x++){
				assertEquals((x+1)%2, img.getValue(x, y));
			}
			img.fill(0);
			img.stream(100,200,1000,1000).filter(px->{return px.getX() % 2 == 0;}).forEach(px->{px.setValue(1);});
			for(int y = 0; y < 2000; y++)
			for(int x = 0; x < 3000; x++){
				if(x < 100 || y < 200 || x >=100+1000 || y >= 200+1000){
					assertEquals(0, img.getValue(x, y));
				} else {
					assertEquals((x+1)%2, img.getValue(x, y));
				}
			}
			img.fill(0);
			img.stream(true, 100,200,1000,1000).filter(px->{return px.getX() % 2 == 0;}).forEach(px->{px.setValue(1);});
			for(int y = 0; y < 2000; y++)
			for(int x = 0; x < 3000; x++){
				if(x < 100 || y < 200 || x >=100+1000 || y >= 200+1000){
					assertEquals(0, img.getValue(x, y));
				} else {
					assertEquals((x+1)%2, img.getValue(x, y));
				}
			}

		}
		
		
		// forEach/stream with manipulator/converter
		{
			PixelManipulator<PixelBase,Color[]> colorManip = new PixelManipulator<PixelBase,Color[]>() {
				@Override
				public PixelConverter<PixelBase, Color[]> getConverter() {
					return PixelConverter.fromFunctions(
							()->new Color[1], 
							(px,c)->{
								c[0] = new Color(
										(float)px.r_asDouble(), 
										(float)px.g_asDouble(), 
										(float)px.b_asDouble(), 
										(float)px.a_asDouble());
							},
							(c,px)->{
								px.setARGB_fromDouble(
										c[0].getAlpha()/255.0, 
										c[0].getRed()/255.0, 
										c[0].getGreen()/255.0, 
										c[0].getBlue()/255.0);
							});
				}

				@Override
				public Consumer<Color[]> getAction() {
					return c->c[0] = c[0].brighter();
				}
			};

			// for each
			Img img = imgAlloc.apply(512, 512);
			img.forEach(true, px->assertTrue(px.getLuminance()==0));
			// apply manip to bottom half
			img.forEach(0, 256, 512, 256, colorManip);
			img.forEach(true, 0,   0, 512, 256, px->assertTrue(px.getLuminance()==0));
			img.forEach(true, 0, 256, 512, 256, px->assertTrue(px.getLuminance()>0));
			// apply to upper half
			img.forEach(true, 0, 0, 512, 256, colorManip);
			double avg1 = img.stream(true).mapToInt(Pixel::getLuminance).average().getAsDouble();
			// average luminance has to be the same as luminance of a single pixel
			assertTrue(avg1 == img.getPixel().getLuminance());
			img.forEach(true, colorManip);
			double avg2 = img.stream(true).mapToInt(Pixel::getLuminance).average().getAsDouble();
			assertTrue(avg2 > avg1);
			img.forEach(colorManip);
			double avg3 = img.stream(true).mapToInt(Pixel::getLuminance).average().getAsDouble();
			assertTrue(avg3 > avg2);
			
			
			
			// stream
			img = imgAlloc.apply(512, 512);
			img.fill(0xffffffff);
			img.forEach(px->assertTrue(px.getLuminance()==255));
			img.stream(colorManip.getConverter(), true).forEach(c->c[0]=c[0].darker());
			img.forEach(px->assertTrue(px.getLuminance() <255));
			img.stream(colorManip.getConverter(), true, 0, 256, 512, 256).forEach(c->c[0]=Color.white);
			img.forEach(0,   0, 512, 256, px->assertTrue(px.getLuminance() <255));
			img.forEach(0, 256, 512, 256, px->assertTrue(px.getLuminance()==255));
		}

	}

	@Test
	public void graphics_test(){
		Img img = new Img(400, 400);
		img.paint(g->{
			g.setColor(new Color(0xffff0000));
			g.drawLine(0, 0, 400, 400);
		});
		img.forEach(px->{
			if(px.getX() == px.getY()){
				assertEquals(0xffff0000, px.getValue());
			} else {
				assertEquals(0, px.getValue());
			}
		});
	}


}
