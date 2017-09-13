package hageldave.imagingkit.core.scientific;

import static hageldave.imagingkit.core.JunitUtils.testException;
import static hageldave.imagingkit.core.scientific.ColorImg.boundary_mode_mirror;
import static hageldave.imagingkit.core.scientific.ColorImg.boundary_mode_repeat_edge;
import static hageldave.imagingkit.core.scientific.ColorImg.boundary_mode_repeat_image;
import static hageldave.imagingkit.core.scientific.ColorImg.boundary_mode_zero;
import static hageldave.imagingkit.core.scientific.ColorImg.channel_a;
import static hageldave.imagingkit.core.scientific.ColorImg.channel_b;
import static hageldave.imagingkit.core.scientific.ColorImg.channel_g;
import static hageldave.imagingkit.core.scientific.ColorImg.channel_r;
import static org.junit.Assert.*;

import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Spliterator;

import org.junit.Test;

import hageldave.imagingkit.core.Img;
import hageldave.imagingkit.core.scientific.ColorImg.TransferFunction;

public class ColorImgTest {

	@Test
	public void testExceptions(){
		testException(()->{
			new ColorImg(2, 2, new double[4], new double[4], new double[4], new double[3]);
		}, IllegalArgumentException.class);
		testException(()->{
			new ColorImg(2, 2, new double[4], new double[4], new double[3], new double[4]);
		}, IllegalArgumentException.class);
		testException(()->{
			new ColorImg(2, 2, new double[4], new double[3], new double[4], new double[4]);
		}, IllegalArgumentException.class);
		testException(()->{
			new ColorImg(2, 2, new double[3], new double[4], new double[4], new double[4]);
		}, IllegalArgumentException.class);
		testException(()->{
			new ColorImg(2, 2, new double[3], new double[4], new double[4], null);
		}, IllegalArgumentException.class);
		testException(()->{
			new ColorImg(2, 3, new double[4], new double[4], new double[4], new double[4]);
		}, IllegalArgumentException.class);
		testException(()->{
			new ColorImg(2, 3, new double[4], new double[4], new double[4], null);
		}, IllegalArgumentException.class);
		
		// exceptions due to no alpha
		ColorImg img = new ColorImg(3, 3, false);
		testException(()->
		{
			img.getValue(channel_a, 0, 0);
		}, ArrayIndexOutOfBoundsException.class);
		testException(()->
		{
			img.setValue(channel_a, 0, 0, 2.5);
		}, ArrayIndexOutOfBoundsException.class);
		testException(()->
		{
			img.getValueA(0, 0);
		}, NullPointerException.class);
		testException(()->
		{
			img.setValueA(0, 0, 2.5);
		}, NullPointerException.class);
		testException(()->
		{
			img.getValue(channel_a, 0, 0, boundary_mode_mirror);
		}, ArrayIndexOutOfBoundsException.class);
		testException(()->
		{
			img.getValueA(0, 0, boundary_mode_mirror);
		}, ArrayIndexOutOfBoundsException.class);
		testException(()->
		{
			img.interpolate(channel_a, 0.5, 0.5);
		}, ArrayIndexOutOfBoundsException.class);
		testException(()->
		{
			img.fill(channel_a, 0);
		}, ArrayIndexOutOfBoundsException.class);
		testException(()->
		{
			img.getPixel().getValue(channel_a);
		}, ArrayIndexOutOfBoundsException.class);
		testException(()->
		{
			img.getPixel().setValue(channel_a, 2.5);
		}, ArrayIndexOutOfBoundsException.class);
		// things that are not supposed to throw even though no alpha
		img.getPixel().a_asDouble();
		img.getPixel().setA_fromDouble(0);
		
		// other
		testException(()->
		{
			img.setSpliteratorMinimumSplitSize(0);
		}, IllegalArgumentException.class);
		testException(()->
		{
			img.getPixel().transform(new double[3][2]);
		}, ArrayIndexOutOfBoundsException.class);
	}
	
	@Test
	public void testNonInterfaceMethods(){
		{// constructors
			Img theimg = new Img(3, 3, new int[]{
					0,1,2,
					3,4,5,
					6,7,8});
			theimg.forEach(px->px.setA(255));
			ColorImg img1 = new ColorImg(theimg, true);
			ColorImg img2 = new ColorImg(theimg.getRemoteBufferedImage());
			assertEquals(theimg.getPixel(2,2).b_asDouble(), img1.getPixel(2, 2).b_asDouble(), 0);
			assertArrayEquals(img1.getDataB(), img2.getDataB(), 0);
			ColorImg img3 = new ColorImg(img2.getDimension(), true);
			img2.copyArea(0, 0, 3, 3, img3, 0, 0);
			assertArrayEquals(img2.getDataB(), img3.getDataB(), 0);
			ColorImg img4 = new ColorImg(3,3, img3.getDataB(),img3.getDataG(),img3.getDataR(),null);
			assertArrayEquals(img2.getDataB(), img4.getDataR(), 0);
			ColorImg img5 = new ColorImg(3,3, img3.getDataA(),img3.getDataG(),img3.getDataR(),img3.getDataB());
			assertArrayEquals(img2.getDataB(), img5.getDataA(), 0);
		}
		
		{// getValue methods
			double[] rBuffer = new double[]{1,2,
											3,4};
			
			double[] gBuffer = new double[]{10,20,
											30,40};
			
			double[] bBuffer = new double[]{100,200,
											300,400};
			
			double[] aBuffer = new double[]{-1,-2,
											-3,-4};
			
			ColorImg img = new ColorImg(2, 2, rBuffer, gBuffer, bBuffer, aBuffer);
			assertEquals(img.getValue(channel_r, 0, 0), rBuffer[0], 0);
			assertEquals(img.getValue(channel_g, 1, 0), gBuffer[1], 0);
			assertEquals(img.getValue(channel_b, 0, 1), bBuffer[2], 0);
			assertEquals(img.getValue(channel_a, 1, 1), aBuffer[3], 0);
			
			int i = 0;
			for(int y = 0; y < img.getHeight(); y++){
				for(int x = 0; x < img.getWidth(); x++){
					assertEquals(rBuffer[i], img.getValueR(x, y), 0);
					assertEquals(gBuffer[i], img.getValueG(x, y), 0);
					assertEquals(bBuffer[i], img.getValueB(x, y), 0);
					assertEquals(aBuffer[i], img.getValueA(x, y), 0);
					i++;
				}
			}
			
			assertEquals(4,  img.getValueR(-2,-2, boundary_mode_mirror), 0);
			assertEquals(40, img.getValueG(-2, 1, boundary_mode_mirror), 0);
			assertEquals(400,img.getValueB( 1,-2, boundary_mode_mirror), 0);
			assertEquals(-4, img.getValueA( 1, 1, boundary_mode_mirror), 0);
			assertEquals(1,  img.getValueR( 4, 4, boundary_mode_mirror), 0);
			assertEquals(1,  img.getValueR(-5,-5, boundary_mode_mirror), 0);
			assertEquals(1,  img.getValueR( 3, 3, boundary_mode_mirror), 0);
			
			assertEquals(1,  img.getValueR( 0,-2, boundary_mode_repeat_edge), 0);
			assertEquals(20, img.getValueG( 3, 0, boundary_mode_repeat_edge), 0);
			assertEquals(400,img.getValueB( 1, 4, boundary_mode_repeat_edge), 0);
			assertEquals(-3, img.getValueA(-2, 1, boundary_mode_repeat_edge), 0);
			
			assertEquals(1,   img.getValueR(2, 0, boundary_mode_repeat_image), 0);
			assertEquals(20,  img.getValueG(1, 2, boundary_mode_repeat_image), 0);
			assertEquals(400, img.getValueB(-1, 1,boundary_mode_repeat_image), 0);
			assertEquals(-3,  img.getValueA(0, 3, boundary_mode_repeat_image), 0);
			assertEquals(1,   img.getValueR(4, 2, boundary_mode_repeat_image), 0);
			assertEquals(20,  img.getValueG(5, 4, boundary_mode_repeat_image), 0);
			assertEquals(400, img.getValueB(-3, 3,boundary_mode_repeat_image), 0);
			assertEquals(-3,  img.getValueA(2, 5, boundary_mode_repeat_image), 0);
			
			assertEquals(0,   img.getValueR(2, 0, boundary_mode_zero), 0);
			assertEquals(0,  img.getValueG(1, 2,  boundary_mode_zero), 0);
			assertEquals(0, img.getValueB(-1, 1,  boundary_mode_zero), 0);
			assertEquals(0,  img.getValueA(0, 3,  boundary_mode_zero), 0);
			assertEquals(0,   img.getValueR(4, 2, boundary_mode_zero), 0);
			assertEquals(0,  img.getValueG(5, 4,  boundary_mode_zero), 0);
			assertEquals(0xff000000, img.getValueB(-3, 3,  0xff000000), 0);
			assertEquals(0xff2f5647,  img.getValueA(2, 5,  0xff2f5647), 0);
			
			
			
			rBuffer = new double[]{
					0,1,1,
					1,2,2,
					2,1,2,
			};
			img = new ColorImg(3, 3, rBuffer, Arrays.copyOf(rBuffer,9), Arrays.copyOf(rBuffer,9), Arrays.copyOf(rBuffer,9));
			// exact positions
			assertEquals(0, img.interpolateR(0, 0), 0);
			assertEquals(2, img.interpolateG(1, 1), 0);
			assertEquals(2, img.interpolateB(0, 1), 0);
			assertEquals(1, img.interpolateA(1, 0), 0);
			
			assertEquals(1, img.interpolateR(.5, 0), 0);
			assertEquals(1, img.interpolateG(.5, 1), 0);
			assertEquals(1, img.interpolateB(0, .5), 0);
			assertEquals(2, img.interpolateA(1, .5), 0);
			
			assertEquals(2, img.interpolateR(.5, .5), 0);
			
			// actual interpolation
			assertEquals(0.5, img.interpolateR(.25,  0), 0);
			assertEquals(1,   img.interpolateG(.75,  0), 0);
			assertEquals(1.5, img.interpolateB(.25,  1), 0);
			assertEquals(1.5, img.interpolateA(.75,  1), 0);
			assertEquals(1.75,img.interpolateR(.75, .75), 0);
			assertEquals(1,   img.interpolateR(.25, .25), 0);
			
			
			
			// copy
			ColorImg img1 = new ColorImg(100,100, true);
			ColorImg img2 = new ColorImg(20,20, true);
			
			img1.fill(channel_r, 1);
			img1.fill(channel_g, -2);
			img1.fill(channel_a, 33);
			for(i = 0; i < img1.numValues(); i++){
				assertEquals(1,  img1.getDataR()[i],0);
				assertEquals(-2, img1.getDataG()[i],0);
				assertEquals(0,  img1.getDataB()[i],0);
				assertEquals(33, img1.getDataA()[i],0);
			}
			
			ColorImg img3 = img1.copyArea(0, 0, 100, 100, null, 0, 0);
			assertArrayEquals(img1.getDataR(), img3.getDataR(), 0);
			assertArrayEquals(img1.getDataG(), img3.getDataG(), 0);
			assertArrayEquals(img1.getDataB(), img3.getDataB(), 0);
			assertArrayEquals(img1.getDataA(), img3.getDataA(), 0);
			
			img1.copyArea(0, 0, 100, 100, img2, -3, -2);
			for(i = 0; i < img2.numValues(); i++){
				assertEquals(1,  img2.getDataR()[i],0);
				assertEquals(-2, img2.getDataG()[i],0);
				assertEquals(0,  img2.getDataB()[i],0);
				assertEquals(33, img2.getDataA()[i],0);
			}
			
			img1.fill(channel_r, 5);
			img1.copyArea(0, 0, 100, 100, img3, -2, -4);
			for(int y = 0; y < 100; y++){
				for(int x = 0; x < 100; x++){
					assertEquals(-2, img3.getValue(channel_g, x, y),0);
					assertEquals(0,  img3.getValue(channel_b, x, y),0);
					assertEquals(33, img3.getValue(channel_a, x, y),0);
					if(x < 100-2 && y < 100-4){
						assertEquals(5, img3.getValue(channel_r, x, y),0);
					} else {
						assertEquals(1, img3.getValue(channel_r, x, y),0);
					}
				}
			}
			
			img1.fill(channel_r, 6);
			img1.copyArea(0, 0, 100, 100, img3, 0, -3);
			for(int y = 0; y < 100; y++){
				for(int x = 0; x < 100; x++){
					assertEquals(-2, img3.getValue(channel_g, x, y),0);
					assertEquals(0,  img3.getValue(channel_b, x, y),0);
					assertEquals(33, img3.getValue(channel_a, x, y),0);
					if(y < 100-3){
						assertEquals(6, img3.getValue(channel_r, x, y),0);
					} else {
						assertEquals(1, img3.getValue(channel_r, x, y),0);
					}
				}
			}
			
			img3 = img1.copy();
			assertFalse(img1.getDataA() == img3.getDataA());
			assertFalse(img1.getDataR() == img3.getDataR());
			assertFalse(img1.getDataG() == img3.getDataG());
			assertFalse(img1.getDataB() == img3.getDataB());
			assertArrayEquals(img1.getDataR(), img3.getDataR(), 0);
			assertArrayEquals(img1.getDataG(), img3.getDataG(), 0);
			assertArrayEquals(img1.getDataB(), img3.getDataB(), 0);
			assertArrayEquals(img1.getDataA(), img3.getDataA(), 0);
			
			img1.setValue(channel_r, 1, 0, 44);
			assertEquals(44, img1.getValue(channel_r, 1, 0),0);
			img1.fill(channel_a, 0);
			img1.fill(channel_r, 0);
			img1.fill(channel_g, 0);
			img1.fill(channel_b, 0);
			for(int x=0,y=0; y<100; x++,y++){
				img1.setValueR(x, y, 78);
				img1.setValueG(x, y, 79);
				img1.setValueB(x, y, 80);
				img1.setValueA(x, y, 81);
			}
			for(int y = 0; y < 100; y++){
				for(int x = 0; x < 100; x++){
					if(x==y){
						assertEquals(81, img1.getValue(channel_a, x, y),0);
						assertEquals(78, img1.getValue(channel_r, x, y),0);
						assertEquals(79, img1.getValue(channel_g, x, y),0);
						assertEquals(80, img1.getValue(channel_b, x, y),0);
					} else{
						assertEquals(0, img1.getValue(channel_a, x, y),0);
						assertEquals(0, img1.getValue(channel_r, x, y),0);
						assertEquals(0, img1.getValue(channel_g, x, y),0);
						assertEquals(0, img1.getValue(channel_b, x, y),0);
					}
				}
			}
			
			
			
			// toBufferedImage
			Img baseImage = new Img(16, 16);
			baseImage.forEach(px->px.setRGB(px.getIndex(), px.getIndex(), px.getIndex()));
			img = new ColorImg(baseImage, false);
			BufferedImage bufferedImage = img.toBufferedImage();
			Img targetImage = new Img(bufferedImage);
			assertArrayEquals(baseImage.getData(), targetImage.getData());
			assertArrayEquals(baseImage.getData(), img.toImg().getData());
			
			baseImage.forEach(px->px.setA(px.getIndex()));
			ColorImg img4 = new ColorImg(baseImage.getDimension(),true);
			baseImage.forEach(px->{
				img4.getDataA()[px.getIndex()]=px.a();
				img4.getDataR()[px.getIndex()]=px.r();
				img4.getDataG()[px.getIndex()]=px.g();
				img4.getDataB()[px.getIndex()]=px.b();
			});
			
			bufferedImage = img4.toBufferedImage(TransferFunction.fromFunction(v->(int)v));
			assertArrayEquals(baseImage.getData(), Img.createRemoteImg(bufferedImage).getData());
			
			
			img = new ColorImg(17,16, true);
			img.setSpliteratorMinimumSplitSize(11);
			assertEquals(11, img.getSpliteratorMinimumSplitSize());
			int minsplitSize = 4;
			img.setSpliteratorMinimumSplitSize(minsplitSize);
			assertEquals(minsplitSize, img.getSpliteratorMinimumSplitSize());
			
			Spliterator<ColorPixel> spliterator = img.spliterator();
			ArrayList<Spliterator<ColorPixel>> splits = new ArrayList<>();
			splits.add(spliterator);
			for(i = 0; i < splits.size(); i++){
				Spliterator<ColorPixel> split;
				while((split=splits.get(i).trySplit())!=null){
					splits.add(split);
				}
			}
			for(Spliterator<ColorPixel> split: splits){
				assertTrue(split.estimateSize() >= minsplitSize);
			}
			assertEquals(img.numValues(), splits.stream().mapToLong(Spliterator::estimateSize).sum());
			
			assertTrue(img.supportsRemoteBufferedImage());
		}
		
		
		
	}
	
}
