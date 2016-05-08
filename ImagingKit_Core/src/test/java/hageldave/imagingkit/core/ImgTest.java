package hageldave.imagingkit.core;

import org.junit.Test;
import static org.junit.Assert.*;

import java.awt.image.BufferedImage;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Spliterator;

public class ImgTest {

	@Test
	public void channelMethods_test(){
		int color = 0xffaa1244;
		assertEquals(0xff, Pixel.a(color));
		assertEquals(0xaa, Pixel.r(color));
		assertEquals(0x12, Pixel.g(color));
		assertEquals(0x44, Pixel.b(color));
		assertEquals(0xa124, Pixel.ch(color, 4, 16));
		assertEquals(0x44, Pixel.ch(color, 0, 8));
		assertEquals(1.0, Pixel.a_normalized(color), 0);
		assertEquals(0xaa/255.0f, Pixel.r_normalized(color), 0);
		assertEquals(0x12/255.0f, Pixel.g_normalized(color), 0);
		assertEquals(0x44/255.0f, Pixel.b_normalized(color), 0);
		
		assertEquals(0x01001234, Pixel.argb_fast(0x01, 0x00, 0x12, 0x34));
		assertEquals(0xff543210, Pixel.rgb_fast(0x54, 0x32, 0x10));
		assertEquals(0xff00ff54, Pixel.rgb_bounded(-12, 260, 0x54));
		assertEquals(0xffffffff, Pixel.rgb(0x15ff, 0xaff, 0x5cff));
		assertEquals(0b10101110, Pixel.combineCh(2, 0b10, 0b10, 0b11, 0b10));
		assertEquals(0xffff00ff, Pixel.argb_fromNormalized(1, 1, 0, 1));
		assertEquals(0x0000ff00, Pixel.argb_fromNormalized(0, 0, 1, 0));
		assertEquals(0x66778899, Pixel.argb_fromNormalized(0x66/255.0f, 0x77/255.0f, 0x88/255.0f, 0x99/255.0f));
		assertEquals(0xff778899, Pixel.rgb_fromNormalized(0x77/255.0f, 0x88/255.0f, 0x99/255.0f));
		
		Pixel p = new Pixel(new Img(1,1), 0);
		assertEquals(0, p.getValue());
		p.setR(0xff);
		assertEquals(0x00ff0000, p.getValue());
		p.setG(0xaa);
		assertEquals(0x00ffaa00, p.getValue());
		p.setB(0xcdef);
		assertEquals(0x00ffaaef, p.getValue());
		p.setA(p.r());
		assertEquals(0xffffaaef, p.getValue());
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
		
	}
	
	@Test
	public void iterable_test(){
		// iterator
		{
			Img img = new Img(16,9);
			int idx = 0;
			for(Pixel p: img){
				p.setValue(p.getValue()+idx);
				idx++;
			}
			assertEquals(img.numValues(), idx);
			for(int i = 0; i < img.numValues(); i++){
				assertEquals(i, img.getData()[i]);
			}
		}
		
		{
			Img img = new Img(16,9);
			img.iterator().forEachRemaining((px)->{px.setValue(px.getValue()+px.getIndex());});
			for(int i = 0; i < img.numValues(); i++){
				assertEquals(i, img.getData()[i]);
			}
		}
		
		{
			Img img = new Img(16,9);
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
			Img img = new Img(16,9);
			img.forEach((px)->{px.setValue(px.getIndex());});
			for(int i = 0; i < img.numValues(); i++){
				assertEquals(i, img.getData()[i]);
			}
		}
		
		// spliterator
		{
			Img img = new Img(2000, 400);
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
				iter.tryAdvance((px) -> {px.setValue(px.getValue()+px.getIndex());});
				iter.forEachRemaining((px) -> {px.setValue(px.getValue()+px.getIndex());});
			}
			for(int i = 0; i < img.numValues(); i++){
				assertEquals(i, img.getData()[i]);
			}
		}
		
		// area spliterator
		{
			Img img = new Img(2000, 400);
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
		
		// parallel foreach
		{
			Img img = new Img(3000, 2000);
			img.forEachParallel( (px)->{px.setValue(px.getIndex());} );
			for(int i = 0; i < img.numValues(); i++){
				assertEquals(i, img.getData()[i]);
			}
		}
		
		// foreach area
		{
			Img img = new Img(2000,400);
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
			Img img = new Img(3000,2000);
			img.forEachParallel(40, 80, 1000, 500, (px)->{px.setValue(px.getValue()+px.getIndex());});
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
				img.forEachParallel(0, 0, 3000,2001, (px)->{});
			}, IllegalArgumentException.class);
		}
		
	}
	
	
}
