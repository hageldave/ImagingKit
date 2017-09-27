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
	
	static final double eps = 0.00000000001;

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
		testException(()->
		{
			img.getMinValue(channel_a);
		}, ArrayIndexOutOfBoundsException.class);
		testException(()->
		{
			img.getMaxValue(channel_a);
		}, ArrayIndexOutOfBoundsException.class);
		testException(()->
		{
			img.getChannelImage(channel_a);
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
			
			
		}
		
		{
			// copy
			ColorImg img1 = new ColorImg(100,100, true);
			ColorImg img2 = new ColorImg(20,20, true);
			
			img1.fill(channel_r, 1);
			img1.fill(channel_g, -2);
			img1.fill(channel_a, 33);
			for(int i = 0; i < img1.numValues(); i++){
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
			for(int i = 0; i < img2.numValues(); i++){
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
		}
		
		
		{	
			// toBufferedImage
			Img baseImage = new Img(16, 16);
			baseImage.forEach(px->px.setRGB(px.getIndex(), px.getIndex(), px.getIndex()));
			ColorImg img = new ColorImg(baseImage, false);
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
			for(int i = 0; i < splits.size(); i++){
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
		
		
		{
			ColorImg img = new ColorImg(10,10,false);
			for(int i=0; i < img.numValues(); i++){
				img.getDataR()[i] = i;
			}
			assertEquals(0, img.getMinValue(channel_r), 0);
			assertEquals(99, img.getMaxValue(channel_r), 0);
			img.getDataR()[22] = -1;
			img.getDataR()[23] = 100;
			assertEquals(22, img.getIndexOfMinValue(channel_r));
			assertEquals(23, img.getIndexOfMaxValue(channel_r));
		}
		
		{
			ColorImg img = new ColorImg(10,10,true);
			for(int i=0; i < img.numValues(); i++){
				for(int c=0; c<4; c++){
					img.getData()[c][i] = i+c;
				}
			}
			for(int c=0; c<4; c++){
				ColorImg channelImage = img.getChannelImage(c);
				assertEquals(img.getData()[c], channelImage.getDataR());
				assertEquals(img.getData()[c], channelImage.getDataG());
				assertEquals(img.getData()[c], channelImage.getDataB());
				
				channelImage.getPixel(0, 0).setRGB_fromDouble(-1, -2, -3);
				assertEquals(img.getValue(c, 0, 0), -3, 0);
			}
		}
		
		{
			ColorImg img = new ColorImg(10,10,true);
			
			for(int i=0; i < img.numValues(); i++){
				for(int c=0; c<4; c++){
					img.getData()[c][i] = i+c;
				}
			}
			img.clampAllChannelsToUnitRange();
			for(int i=0; i < img.numValues(); i++){
				for(int c=0; c<4; c++){
					assertTrue(img.getData()[c][i] >= 0);
					assertTrue(img.getData()[c][i] <= 1);
				}
			}
			
			for(int i=0; i < img.numValues(); i++){
				for(int c=0; c<4; c++){
					img.getData()[c][i] = i+c;
				}
			}
			img.scaleChannelToUnitRange(channel_r);
			for(int i=0; i < img.numValues(); i++){
				assertTrue(img.getDataR()[i] >= 0);
				assertTrue(img.getDataR()[i] <= 1);
			}
			assertEquals(3/99.0, img.getDataR()[3], 0);
			
			for(int i=0; i < img.numValues(); i++){
				for(int c=0; c<4; c++){
					img.getData()[c][i] = i+c;
				}
			}
			img.scaleRGBToUnitRange();
			for(int i=0; i < img.numValues(); i++){
				for(int c=0; c<3; c++){
					assertTrue(img.getData()[c][i] >= 0);
					assertTrue(img.getData()[c][i] <= 1);
				}
			}
			assertEquals(3/101.0, img.getDataR()[3], 0);
			
			img.fill(channel_r, -1);
			img.scaleChannelToUnitRange(channel_r);
			for(int i=0; i < img.numValues(); i++){
				assertEquals(0, img.getDataR()[i], 0);
			}
			
			img.fill(channel_r, 3).fill(channel_g, 3).fill(channel_b, 3);
			img.scaleRGBToUnitRange();
			for(int i=0; i < img.numValues(); i++){
				for(int c=0; c<3; c++){
					assertEquals(0, img.getData()[c][i], 0);
				}
			}
			
		}
	}
	
	@Test
	public void testPixelIteration(){
		ColorImg img = new ColorImg(10, 10, true);
		assertEquals(img, img.getPixel().getSource());
		for(int i = 0; i < img.numValues(); i++){
			for(int c=0;c<4;c++){
				assertEquals(0, img.getData()[c][i],0);
			}
		}
		img.forEach(px->px.setA_fromDouble(px.getIndex()));
		for(int i = 0; i < img.numValues(); i++){
			assertEquals(i, img.getDataA()[i],0);
			assertEquals(0, img.getDataR()[i],0);
			assertEquals(0, img.getDataG()[i],0);
			assertEquals(0, img.getDataB()[i],0);
		}
		img.forEach(px->px.setARGB_fromDouble(3, 4, 6, 7));
		for(int i = 0; i < img.numValues(); i++){
			assertEquals(3, img.getDataA()[i],0);
			assertEquals(4, img.getDataR()[i],0);
			assertEquals(6, img.getDataG()[i],0);
			assertEquals(7, img.getDataB()[i],0);
		}
		img.forEach(px->px.setValue(channel_r, 0));
		for(int i = 0; i < img.numValues(); i++){
			assertEquals(3, img.getDataA()[i],0);
			assertEquals(0, img.getDataR()[i],0);
			assertEquals(6, img.getDataG()[i],0);
			assertEquals(7, img.getDataB()[i],0);
		}
		img.forEach(px->px.setRGB_fromDouble(2,2,2));
		for(int i = 0; i < img.numValues(); i++){
			assertEquals(1, img.getDataA()[i],0);
			assertEquals(2, img.getDataR()[i],0);
			assertEquals(2, img.getDataG()[i],0);
			assertEquals(2, img.getDataB()[i],0);
		}
		img.forEach(px->px.setA_fromDouble(77).setRGB_fromDouble_preserveAlpha(0, 0, 0));
		for(int i = 0; i < img.numValues(); i++){
			assertEquals(77, img.getDataA()[i],0);
			assertEquals(0, img.getDataR()[i],0);
			assertEquals(0, img.getDataG()[i],0);
			assertEquals(0, img.getDataB()[i],0);
		}
		img.stream().filter(px->px.getX() == 0).filter(px->px.getY()==0).forEach(px->px.setA_fromDouble(1).setR_fromDouble(1).setG_fromDouble(1).setB_fromDouble(1));
		for(int i = 0; i < img.numValues(); i++){
			if(i == 0){
				assertEquals(1, img.getDataA()[i],0);
				assertEquals(1, img.getDataR()[i],0);
				assertEquals(1, img.getDataG()[i],0);
				assertEquals(1, img.getDataB()[i],0);
			} else {
				assertEquals(77, img.getDataA()[i],0);
				assertEquals(0, img.getDataR()[i],0);
				assertEquals(0, img.getDataG()[i],0);
				assertEquals(0, img.getDataB()[i],0);
			}
		}
		img.forEach(px->{int i = px.getIndex();px.setARGB_fromDouble(i,i+1, i+2, i+3);});
		img.forEach(px->{
			int i = px.getIndex();
			assertEquals(i, px.a_asDouble(),0);
			assertEquals(i+1, px.r_asDouble(),0);
			assertEquals(i+2, px.g_asDouble(),0);
			assertEquals(i+3, px.b_asDouble(),0);
			assertEquals(i, px.getValue(channel_a),0);
			assertEquals(i+1, px.getValue(channel_r),0);
			assertEquals(i+2, px.getValue(channel_g),0);
			assertEquals(i+3, px.getValue(channel_b),0);
		});
		
		img.getPixel().setPosition(0, 0).setRGB_fromDouble_preserveAlpha(4, 2, 1);
		assertEquals(4, img.getPixel().setPosition(0, 0).getGrey(1, 0, 0), 0);
		assertEquals(7, img.getPixel().setPosition(0, 0).getGrey(1, 1, 1), 0);
		assertNotEquals(img.getPixel().setPosition(0, 0).getLuminance(), img.getPixel().setPosition(1, 0).getLuminance(), 0);
		assertFalse(img.getPixel().toString().isEmpty());
		
		img.getPixel().setARGB_fromDouble(0, 1, 2, 3);
		img.getPixel().convertRange(1,3, 0,1);
		assertEquals(0, img.getPixel().a_asDouble(),0);
		assertEquals(0, img.getPixel().r_asDouble(),0);
		assertEquals(0.5, img.getPixel().g_asDouble(),0);
		assertEquals(1, img.getPixel().b_asDouble(),0);
		
		img.getPixel().scale(2);
		assertEquals(0, img.getPixel().a_asDouble(),0);
		assertEquals(0, img.getPixel().r_asDouble(),0);
		assertEquals(1, img.getPixel().g_asDouble(),0);
		assertEquals(2, img.getPixel().b_asDouble(),0);
		
		img.getPixel().add(1, 1, 1);
		assertEquals(0, img.getPixel().a_asDouble(),0);
		assertEquals(1, img.getPixel().r_asDouble(),0);
		assertEquals(2, img.getPixel().g_asDouble(),0);
		assertEquals(3, img.getPixel().b_asDouble(),0);
		
		img.getPixel().subtract(1, 2, 3);
		assertEquals(0, img.getPixel().a_asDouble(),0);
		assertEquals(0, img.getPixel().r_asDouble(),0);
		assertEquals(0, img.getPixel().g_asDouble(),0);
		assertEquals(0, img.getPixel().b_asDouble(),0);
		
		img.getPixel().setARGB_fromDouble(1, 1, 0, 0);
		img.getPixel().cross(0, 1, 0);
		assertEquals(1, img.getPixel().a_asDouble(),0);
		assertEquals(0, img.getPixel().r_asDouble(),0);
		assertEquals(0, img.getPixel().g_asDouble(),0);
		assertEquals(1, img.getPixel().b_asDouble(),0);
		
		img.getPixel().setARGB_fromDouble(1, 1, 0, 0);
		img.getPixel().cross_(0, 1, 0);
		assertEquals(1, img.getPixel().a_asDouble(),0);
		assertEquals(0, img.getPixel().r_asDouble(),0);
		assertEquals(0, img.getPixel().g_asDouble(),0);
		assertEquals(-1, img.getPixel().b_asDouble(),0);
		assertEquals(Math.cos(Math.PI/4)*Math.sqrt(2), img.getPixel().dot(0, 1, -1), eps);
		
		double sin90 = Math.sin(Math.PI/2);
		double cos90 = Math.cos(Math.PI/2);
		double[][] rotG90 = new double[][]{
			{ cos90, 0, sin90},
			{     0, 1,     0},
			{-sin90, 0, cos90},
		};
		img.getPixel().transform(rotG90);
		assertEquals(1, img.getPixel().a_asDouble(),0);
		assertEquals(-1, img.getPixel().r_asDouble(),eps);
		assertEquals(0, img.getPixel().g_asDouble(),eps);
		assertEquals(0, img.getPixel().b_asDouble(),eps);
		
		assertEquals(1, img.getPixel().getLen(), eps);
		
		img.getPixel().setRGB_fromDouble(1, 1, 0);
		assertEquals(Math.sqrt(2), img.getPixel().getLen(), eps);
		
		img.getPixel().normalize();
		assertEquals(1, img.getPixel().getLen(), eps);
		assertEquals(0, img.getPixel().b_asDouble(), 0);
		
		img.getPixel().scale(0);
		assertEquals(0, img.getPixel().r_asDouble(),0);
		assertEquals(0, img.getPixel().g_asDouble(),0);
		assertEquals(0, img.getPixel().b_asDouble(),0);
		assertEquals(0, img.getPixel().getLen(), 0);
		
		img.getPixel().normalize();
		assertEquals(0, img.getPixel().r_asDouble(),0);
		assertEquals(0, img.getPixel().g_asDouble(),0);
		assertEquals(0, img.getPixel().b_asDouble(),0);
		assertEquals(0, img.getPixel().getLen(), 0);
		
		img.getPixel().setRGB_fromDouble(2, 1, 3);
		assertEquals(1, img.getPixel().minValue(),0);
		assertEquals(3, img.getPixel().maxValue(),0);
		assertEquals(channel_g, img.getPixel().minChannel());
		assertEquals(channel_b, img.getPixel().maxChannel());
		
		img.getPixel().setRGB_fromDouble(1, 2, 3);
		assertEquals(channel_r, img.getPixel().minChannel());
		assertEquals(channel_b, img.getPixel().maxChannel());
		img.getPixel().setRGB_fromDouble(3, 2, 1);
		assertEquals(channel_b, img.getPixel().minChannel());
		assertEquals(channel_r, img.getPixel().maxChannel());
	}
	
}
