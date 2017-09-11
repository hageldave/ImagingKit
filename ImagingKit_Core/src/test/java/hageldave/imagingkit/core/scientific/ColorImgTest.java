package hageldave.imagingkit.core.scientific;

import static hageldave.imagingkit.core.JunitUtils.testException;
import static hageldave.imagingkit.core.scientific.ColorImg.*;
import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;

import org.junit.Test;

import hageldave.imagingkit.core.Img;

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
			
			assertEquals(1,  img.getValueR( 0,-2, boundary_mode_repeat_edge), 0);
			assertEquals(20, img.getValueG( 3, 0, boundary_mode_repeat_edge), 0);
			assertEquals(400,img.getValueB( 1, 4, boundary_mode_repeat_edge), 0);
			assertEquals(-3, img.getValueA(-2, 1, boundary_mode_repeat_edge), 0);
			
			
		}
		
	}
	
}
