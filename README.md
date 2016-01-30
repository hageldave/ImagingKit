# ImagingKit
A Java library for imaging tasks that integrates well with the commonly used java.awt.image environment (especially well with TYPE_INT BufferedImages). Its goal is to make image processing more convenient and to ease performance optimization. The library is intended for images using integer typed values like 24bit RGB or 32bit ARGB.

So far the *ImagingKit-Core* artifact of the library is available through the maven central repository:
```
<dependency>
    <groupId>com.github.hageldave.imagingkit</groupId>
    <artifactId>imagingkit-core</artifactId>
    <version>1.0</version>
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
