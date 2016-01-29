package hageldave.imagingkit.filter;

import java.awt.image.BufferedImage;

import hageldave.imagingkit.core.Img;

public class Crop {

	public static Img crop(Img img, int x, int y, int width, int height){
		return img.copyArea(x, y, width, height, null, 0, 0);
	}
	
	public static BufferedImage crop(BufferedImage bufferedimg, int x, int y, int width, int height){
		Img img = null;
		try {
			img = Img.createRemoteImg(bufferedimg);
		} catch(IllegalArgumentException e){
			img = new Img(bufferedimg);
		}
		return crop(img, x, y, width, height).getRemoteBufferedImage();
	}
	
}
