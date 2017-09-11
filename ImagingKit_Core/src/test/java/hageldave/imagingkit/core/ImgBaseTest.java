package hageldave.imagingkit.core;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

import java.awt.image.BufferedImage;

import org.junit.Test;

public class ImgBaseTest {
	
	@Test
	public void test() throws InterruptedException{
		TestImg img = new TestImg();
		assertEquals(img.getWidth()*img.getHeight(), img.numValues());
		JunitUtils.testException(()->{img.getRemoteBufferedImage();}, UnsupportedOperationException.class);
		assertFalse(img.supportsRemoteBufferedImage());
		assertEquals(1024, img.getSpliteratorMinimumSplitSize());
		img.forEach(px->px.setA_fromDouble(px.getIndex()%220));
		img.forEach(px->assertEquals((double)px.getIndex()%220, px.a_asDouble(), 0));
	}

	
	private static class TestImg implements ImgBase<TestPixel> {
		
		final double[][] values;
		
		public TestImg() {
			values = new double[1024][512];
		}
		

		@Override
		public int getWidth() {
			return 512;
		}

		@Override
		public int getHeight() {
			return 1024;
		}

		@Override
		public TestPixel getPixel() {
			return new TestPixel(this);
		}

		@Override
		public TestPixel getPixel(int x, int y) {
			return getPixel().setPosition(x, y);
		}

		@Override
		public BufferedImage toBufferedImage(BufferedImage bimg) {
			this.forEach(px->bimg.setRGB(px.x, px.y, Pixel.rgb_bounded((int)px.r_asDouble(),(int)px.g_asDouble(),(int)px.b_asDouble())));
			return bimg;
		}

		@Override
		public ImgBase<TestPixel> copy() {
			TestImg testImg = new TestImg();
			for(int i = 0; i < getHeight(); i++){
				System.arraycopy(values[i], 0, testImg.values[i], 0, getWidth());
			}
			return testImg;
		}
		
	}
	
	private static class TestPixel implements PixelBase {

		int x,y;
		TestImg source;
		
		public TestPixel(TestImg source) {
			this.source = source;
		}
		
		@Override
		public double a_asDouble() {
			return getSource().values[y][x];
		}

		@Override
		public double r_asDouble() {
			return a_asDouble();
		}

		@Override
		public double g_asDouble() {
			return a_asDouble();
		}

		@Override
		public double b_asDouble() {
			return a_asDouble();
		}

		@Override
		public PixelBase setA_fromDouble(double a) {
			getSource().values[y][x]=a;
			return this;
		}

		@Override
		public PixelBase setR_fromDouble(double r) {
			return setA_fromDouble(r);
		}

		@Override
		public PixelBase setG_fromDouble(double g) {
			return setA_fromDouble(g); 
		}

		@Override
		public PixelBase setB_fromDouble(double b) {
			return setA_fromDouble(b);
		}

		@Override
		public int getX() {
			return x;
		}

		@Override
		public int getY() {
			return y;
		}

		@Override
		public int getIndex() {
			return y * getSource().getWidth()+ x;
		}

		@Override
		public TestPixel setIndex(int index) {
			return setPosition(index % getSource().getWidth(), index/getSource().getWidth());
		}

		@Override
		public TestPixel setPosition(int x, int y) {
			this.x = x;
			this.y = y;
			return this;
		}

		@Override
		public TestImg getSource() {
			return source;
		}
		
	}
	
}
