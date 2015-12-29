package hageldave.imagingkit.core;

public class Pixel {
	private final Img img;
	private int index;
	
	public Pixel(Img img, int index) {
		this.img = img;
		this.index = index;
	}
	
	public Pixel(Img img, int x, int y) {
		this(img, y*img.getWidth()+x);
	}
	
	public Img getImg() {
		return img;
	}
	
	public void setIndex(int index) {
		this.index = index;
	}
	
	public void setPosition(int x, int y) {
		this.index = y*img.getWidth()+x;
	}
	
	public int getIndex() {
		return index;
	}
	
	public int getX() {
		return index % img.getWidth();
	}
	
	public int getY() {
		return index / img.getWidth();
	}
	
	public void setValue(int pixelValue){
		this.img.getData()[index] = pixelValue;
	}
	
	public int getValue(){
		return this.img.getData()[index];
	}
	
	public int a(){
		return Img.a(getValue());
	}
	
	public int r(){
		return Img.r(getValue());
	}
	
	public int g(){
		return Img.g(getValue());
	}
	
	public int b(){
		return Img.b(getValue());
	}
	
	public void setARGB(int a, int r, int g, int b){
		setValue(Img.argb(a, r, g, b));
	}
	
	public void setRGB(int r, int g, int b){
		setValue(Img.rgb(r, g, b));
	}
}
