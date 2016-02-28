package hageldave.imagingkit.core;

import java.awt.image.BufferedImage;
import java.io.File;

import org.junit.After;
import org.junit.Test;
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
		
		Img img = new Img(100, 100);
		for(Pixel p: img){
			p.setValue(0xff000000 | p.getIndex());
		}
		
		File testFile = new File(testDir, "test.png");
		ImageSaver.saveImage(img.getRemoteBufferedImage(), testFile);
		
		Img loaded = Img.createRemoteImg(ImageLoader.loadImage(testFile, BufferedImage.TYPE_INT_ARGB));
		assertEquals(img.getDimension(), loaded.getDimension());
		
		for(Pixel p: loaded){
			assertEquals(0xff000000 | p.getIndex(), p.getValue());
		}
	}
	
	@After
	public void cleanup(){
		deleteTestDir();
	}
	
	
	public static void testException(Runnable codeThatThrows, Class<? extends Throwable> exClass){
		boolean wasThrown = true;
		try{
			codeThatThrows.run();
			wasThrown = false;
		} catch(Throwable t){
			if(!exClass.isInstance(t)){
				fail(String.format("Expected Exception %s but got %s", exClass, t.getClass()));
			}
		}
		if(!wasThrown){
			fail(String.format("Expected Exception %s but none was thrown",exClass));
		}
	}
}
