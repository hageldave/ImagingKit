# ImagingKit

[![Maven Central](https://maven-badges.herokuapp.com/maven-central/com.github.hageldave.imagingkit/imagingkit-core/badge.svg)](http://search.maven.org/#artifactdetails|com.github.hageldave.imagingkit|imagingkit-core|1.1|jar)

A Java library for imaging tasks that integrates well with the commonly used java.awt.image environment (especially well with TYPE_INT BufferedImages). Its goal is to make image processing more convenient and to ease performance optimization. The library is intended for images using integer typed values like 24bit RGB or 32bit ARGB.

So far the *ImagingKit-Core* artifact of the library is available through the maven central repository:
```
<dependency>
    <groupId>com.github.hageldave.imagingkit</groupId>
    <artifactId>imagingkit-core</artifactId>
    <version>1.1</version>
</dependency>
```
--
### Code Examples
Convert an image to grayscale:
```java
BufferedImage buffimg = ImageLoader.loadImage("myimage_colored.png", BufferedImage.TYPE_INT_ARGB);
Img img = Img.createRemoteImg(buffimg);
img.forEachParallel((pixel) -> {
	int gray = (pixel.r() + pixel.g() + pixel.b())/3;
	pixel.setARGB(pixel.a(), gray, gray, gray);
});
ImageSaver.saveImage(buffimg,"myimage_grayscale.png");
```
Fancy polar color thing:
```java
Img img = new Img(1024, 1024);
img.forEach(px -> {
	double x = (px.getX()-512)/512.0;
	double y = (px.getY()-512)/512.0;
	double len = Math.max(Math.abs(x),Math.abs(y));
	double angle = (Math.atan2(x,y)+Math.PI)*(180/Math.PI);
	
	double r = 255*Math.max(0,1-Math.abs((angle-120)/120.0));
	double g = 255*Math.max(0, 1-Math.abs((angle-240)/120.0));
	double b = 255*Math.max(0, angle <= 120 ? 
			1-Math.abs((angle)/120.0):1-Math.abs((angle-360)/120.0));
	
	px.setRGB((int)(r*(1-len)), (int)(g*(1-len)), (int)(b*(1-len)));
});
ImageSaver.saveImage(img.getRemoteBufferedImage(), "polar_colors.png");
