package hageldave.imagingkit.core.scientific;

import org.junit.Test;

import hageldave.imagingkit.core.Img;
import hageldave.imagingkit.core.util.ImageFrame;

import static org.junit.Assert.*;

import java.util.Arrays;

import static hageldave.imagingkit.core.JunitUtils.*;
import static hageldave.imagingkit.core.scientific.ColorImg.*;

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
		
	}
	
}
