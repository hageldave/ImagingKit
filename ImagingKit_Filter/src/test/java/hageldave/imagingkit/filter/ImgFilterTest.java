package hageldave.imagingkit.filter;

import org.junit.Test;
import static org.junit.Assert.*;

import hageldave.imagingkit.core.Img;

public class ImgFilterTest {

	@Test
	public void testImgFilterFollowedBy(){
		ImgFilter rotateChannels = (img, parallel, x,y,w,h)->img.forEach(x, y, w, h, px->px.setRGB_preserveAlpha(px.g(),px.b(),px.r()));
		
		Img img = new Img(10, 10);
		img.fill(0xff112233);
		
		Img copy = img.copy();
		rotateChannels.applyTo(copy);
		for(int c: copy.getData())
			assertEquals(Integer.toHexString(c), 0xff223311, c);
		
		copy = img.copy();
		ImgFilter rotateTwice = rotateChannels.followedBy(rotateChannels);
		rotateTwice.applyTo(copy);
		for(int c: copy.getData())
			assertEquals(Integer.toHexString(c), 0xff331122, c);
		
		copy = img.copy();
		ImgFilter identity = rotateChannels.followedBy(rotateTwice);
		identity.applyTo(copy);
		for(int c: copy.getData())
			assertEquals(Integer.toHexString(c), 0xff112233, c);
	}
	
	@Test
	public void testPerPixelFilterFollowedBy(){
		PerPixelFilter rotateChannels = PerPixelFilter.fromPixelConsumer(px->px.setRGB_preserveAlpha(px.g(),px.b(),px.r()));
		
		Img img = new Img(10, 10);
		img.fill(0xff112233);
		
		Img copy = img.copy();
		rotateChannels.applyTo(copy);
		for(int c: copy.getData())
			assertEquals(Integer.toHexString(c), 0xff223311, c);
		
		copy = img.copy();
		PerPixelFilter rotateTwice = rotateChannels.followedBy(rotateChannels);
		rotateTwice.applyTo(copy);
		for(int c: copy.getData())
			assertEquals(Integer.toHexString(c), 0xff331122, c);
		
	}
	
	@Test
	public void testNeighborhoodFilterFollowedBy(){
		NeighborhoodFilter shiftRight = NeighborhoodFilter.fromConsumer((px,copy)->{
			px.setValue(copy.getValue(px.getX()-2, px.getY(), Img.boundary_mode_repeat_image));
		});
		NeighborhoodFilter shiftLeft = NeighborhoodFilter.fromConsumer((px,copy)->{
			px.setValue(copy.getValue(px.getX()+2, px.getY(), Img.boundary_mode_repeat_image));
		});
		
		Img img = new Img(10,10);
		img.forEach(px->{px.setValue(px.getIndex());});
		
		Img copy = img.copy();
		shiftRight.applyTo(copy);
		for(int x = 0; x < img.getWidth();x++)
		for(int y = 0; y < img.getWidth(); y++){
			assertEquals(img.getValue(x, y), copy.getValue(x+2, y, Img.boundary_mode_repeat_image));
		}
		
		copy = img.copy();
		NeighborhoodFilter identity = shiftRight.followedBy(shiftLeft);
		identity.applyTo(copy);
		for(int x = 0; x < img.getWidth();x++)
		for(int y = 0; y < img.getWidth(); y++){
			assertEquals(img.getValue(x, y), copy.getValue(x, y));
		}
	}
	
	@Test
	public void testMixedFollowedBy(){
		NeighborhoodFilter shiftRight = NeighborhoodFilter.fromConsumer((px,copy)->{
			px.setValue(copy.getValue(px.getX()-2, px.getY(), Img.boundary_mode_repeat_image));
		});
		NeighborhoodFilter shiftLeft = NeighborhoodFilter.fromConsumer((px,copy)->{
			px.setValue(copy.getValue(px.getX()+2, px.getY(), Img.boundary_mode_repeat_image));
		});
		PerPixelFilter rotateChannels = PerPixelFilter.fromPixelConsumer(px->px.setRGB_preserveAlpha(px.g(),px.b(),px.r()));
	}
	
}
