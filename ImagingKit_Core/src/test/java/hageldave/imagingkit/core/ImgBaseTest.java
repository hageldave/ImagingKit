package hageldave.imagingkit.core;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

import java.awt.image.BufferedImage;

import org.junit.Test;

public class ImgBaseTest {
	
	@Test
	public void test() throws Exception {
		TestImg img = new TestImg();
		assertEquals(img.getWidth()*img.getHeight(), img.numValues());
		JunitUtils.testException(()->{img.getRemoteBufferedImage();}, UnsupportedOperationException.class);
		assertFalse(img.supportsRemoteBufferedImage());
		assertEquals(1024, img.getSpliteratorMinimumSplitSize());
		img.forEach(px->px.setRGB_fromDouble(px.getIndex()%220,px.getIndex()%221,px.getIndex()%222).setA_fromDouble(255));
		img.forEach(px->assertEquals((double)px.getIndex()%220, px.r_asDouble(), 0));
	}

	
	private static class TestImg implements ImgBase<TestPixel> {
		
		final double[][][] values;
		
		public TestImg() {
			values = new double[1024][512][4];
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
			this.forEach(px->bimg.setRGB(px.x, px.y, Pixel.argb_bounded((int)px.a_asDouble(),(int)px.r_asDouble(),(int)px.g_asDouble(),(int)px.b_asDouble())));
			return bimg;
		}

		@Override
		public ImgBase<TestPixel> copy() {
			TestImg testImg = new TestImg();
			for(int r = 0; r < getHeight(); r++){
				for(int c = 0; c < getWidth(); c++)
					System.arraycopy(values[r][c], 0, testImg.values[r][c], 0, 4);
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
		
		private double channelAsDouble(int c){
			return getSource().values[y][x][c];
		}
		
		private TestPixel setChannel(int c, double v){
			getSource().values[y][x][c]=v;
			return this;
		}
		
		@Override
		public double a_asDouble() {
			return channelAsDouble(0);
		}

		@Override
		public double r_asDouble() {
			return channelAsDouble(1);
		}

		@Override
		public double g_asDouble() {
			return channelAsDouble(2);
		}

		@Override
		public double b_asDouble() {
			return channelAsDouble(3);
		}

		@Override
		public PixelBase setA_fromDouble(double a) {
			return setChannel(0, a);
		}

		@Override
		public PixelBase setR_fromDouble(double r) {
			return setChannel(1, r);
		}

		@Override
		public PixelBase setG_fromDouble(double g) {
			return setChannel(2, g); 
		}

		@Override
		public PixelBase setB_fromDouble(double b) {
			return setChannel(3, b);
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
