package hageldave.imagingkit.core;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;

import org.junit.After;
import org.junit.Test;

import hageldave.imagingkit.core.ImageLoader.ImageLoaderException;
import hageldave.imagingkit.core.ImageSaver.ImageSaverException;

import static org.junit.Assert.*;

public class IOTest {

	static File testDir = new File("testdir");
	
	static void deleteDir(File dir){
		if(dir.exists()){
			if(dir.isDirectory()){
				for(File f: dir.listFiles((file)->{return file.isDirectory();})){
					deleteDir(f);
				}
				for(File f: dir.listFiles()){
					f.delete();
				}
				dir.delete();
			}
		}
	}
	
	static void deleteTestDir(){
		deleteDir(testDir);
	}
	
	@Test
	public void iotest(){
		testDir.mkdir();
		assertTrue(testDir.isDirectory());
		
		{// save File load File
			Img img = getTestImg(100,100);
			File testFile = new File(testDir, "test1.png");
			ImageSaver.saveImage(img.getRemoteBufferedImage(), testFile);
			Img loaded = Img.createRemoteImg(ImageLoader.loadImage(testFile, BufferedImage.TYPE_INT_ARGB));
			
			assertEquals(img.getDimension(), loaded.getDimension());
			for(Pixel p: loaded){
				assertEquals(0xff000000 | p.getIndex(), p.getValue());
			}
		}
		
		{// save String load String
			Img img = getTestImg(100,100);
			String filepath = testDir.getPath()+"/test2.png";
			ImageSaver.saveImage(img.getRemoteBufferedImage(), filepath);
			Img loaded = Img.createRemoteImg(ImageLoader.loadImage(filepath, BufferedImage.TYPE_INT_ARGB));
			
			assertEquals(img.getDimension(), loaded.getDimension());
			for(Pixel p: loaded){
				assertEquals(0xff000000 | p.getIndex(), p.getValue());
			}
		}
		
		{// save String format load String
			// need very low resolution so all colors have a unique entry in the gif palette
			Img img = getTestImg(15,15);
			String filepath = testDir.getPath()+"/test3.gif";
			String format = "gif";
			ImageSaver.saveImage(img.getRemoteBufferedImage(), filepath, format);
			Img loaded = Img.createRemoteImg(ImageLoader.loadImage(filepath, BufferedImage.TYPE_INT_ARGB));
			
			assertEquals(img.getDimension(), loaded.getDimension());
			for(Pixel p: loaded){
				assertEquals(p.toString(), img.getValue(p.getX(), p.getY()), p.getValue());
			}
		}
		
		{// save Stream load Stream
			Img img = new Img(100, 100);
			// need uniform grey so jpeg compression will not alter the color
			img.fill(0xff888888);
			ByteArrayOutputStream out = new ByteArrayOutputStream();
			ImageSaver.saveImage(img.getRemoteBufferedImage(), out, "jpg");
			BufferedImage bimg = ImageLoader.loadImage(
					new ByteArrayInputStream(out.toByteArray()), BufferedImage.TYPE_INT_ARGB);
			Img loaded = Img.createRemoteImg(bimg);
			
			assertEquals(img.getDimension(), loaded.getDimension());
			for(Pixel p: loaded){
				assertEquals(p.toString(), img.getValue(p.getX(), p.getY()), p.getValue());
			}
		}
		
		{// test exceptions
			Img img = new Img(100,100);
			
			// saving
			JunitUtils.testException(()->
			{
				String filepath = testDir.getPath()+"/namewithoutdot";
				ImageSaver.saveImage(img.getRemoteBufferedImage(), filepath);
			}, ImageSaverException.class);
			JunitUtils.testException(()->
			{
				ImageSaver.saveImage(img.getRemoteBufferedImage(), testDir);
			}, ImageSaverException.class);
			JunitUtils.testException(()->
			{
				ImageSaver.saveImage(img.getRemoteBufferedImage(), testDir.getPath());
			}, ImageSaverException.class);
			JunitUtils.testException(()->
			{
				ImageSaver.saveImage(img.getRemoteBufferedImage(), testDir, "png");
			}, ImageSaverException.class);
			JunitUtils.testException(()->
			{
				ImageSaver.saveImage(img.getRemoteBufferedImage(), testDir.getPath(), "png");
			}, ImageSaverException.class);
			JunitUtils.testException(()->
			{
				ImageSaver.saveImage(img.getRemoteBufferedImage(), testDir.getPath()+"/test6.png", "java");
			}, ImageSaverException.class);
			
			// loading
			JunitUtils.testException(()->
			{
				ImageLoader.loadImage(testDir);
			}, ImageLoaderException.class);
			JunitUtils.testException(()->
			{
				ImageLoader.loadImage(testDir.getPath());
			}, ImageLoaderException.class);
			JunitUtils.testException(()->
			{
				ImageLoader.loadImage(testDir.getPath()+"/thefilethatdoesnotexist.png");
			}, ImageLoaderException.class);
			JunitUtils.testException(()->
			{
				File notImageFile = new File(testDir, "mytext.java");
				try {
					assertTrue(notImageFile.createNewFile());
				} catch (Exception e) {
					fail();
				}
				ImageLoader.loadImage(notImageFile);
			}, ImageLoaderException.class);
			JunitUtils.testException(()->
			{
				ByteArrayInputStream instream_nonsense = new ByteArrayInputStream(new byte[]{0,0,0,0,0});
				ImageLoader.loadImage(instream_nonsense);
			}, ImageLoaderException.class);
		}
		
		{// for coverage (methods only delegate to java api)
			assertNotNull(ImageSaver.getSaveableImageFileFormats());
			assertNotNull(ImageLoader.getLoadableImageFileFormats());
		}
	}
	
	static Img getTestImg(int w, int h) {
		Img img = new Img(w, h);
		for(Pixel p: img){
			p.setValue(0xff000000 | p.getIndex());
		}
		return img;
	}
	
	@After
	public void cleanup(){
		deleteTestDir();
	}
}
