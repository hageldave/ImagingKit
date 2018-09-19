package hageldave.imagingkit.fourier;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;

import java.awt.Dimension;

import org.junit.Test;

import hageldave.imagingkit.core.scientific.ColorImg;

public class ComplexImgTest {

	@Test
	public void test() {
		ComplexImg img = new ComplexImg(new Dimension(100, 100));
		img.enableSynchronizePowerSpectrum(true);
		img.setComplex(1, 1, 2, 4);
		assertEquals(2, img.getValueR(1, 1), 0);
		assertEquals(4, img.getValueI(1, 1), 0);
		assertEquals(2*2+4*4, img.getValueP(1, 1), 0);
		img.setValueI(1, 1, 0);
		assertEquals(0, img.getValueI(1, 1, ColorImg.boundary_mode_zero), 0);
		assertEquals(2*2, img.getValueP(1, 1, ColorImg.boundary_mode_zero), 0);
		img.setValueR(1, 1, -2);
		assertEquals(2*2, img.getValueP(1, 1, ColorImg.boundary_mode_zero), 0);
		assertEquals(-2, img.getValueR(1, 1, ColorImg.boundary_mode_zero), 0);
		assertEquals(2*2, img.computePower(1, 1), 0);
		assertEquals(Math.PI, img.computePhase(1, 1), 0);
		img.getPowerSpectrumImg();
		img.getPhaseSpectrumImg();
		img.getPowerPhaseSpectrumImg();
		if(img.supportsRemoteBufferedImage())
			img.getRemoteBufferedImage();
		ComplexImg copy = img.copy();
		for(int y=0;y<img.getHeight();y++)
			for(int x=0;x<img.getWidth();x++)
				assertEquals(
						img.getValue(ComplexImg.CHANNEL_POWER, x, y), 
						copy.getValue(ComplexImg.CHANNEL_POWER, -(x+1), -(y+1), ColorImg.boundary_mode_mirror),
						0
				);
	}
	
}
