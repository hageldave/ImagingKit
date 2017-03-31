package hageldave.imagingkit.filter.implementations.curve;

import java.awt.Color;
import java.awt.geom.Point2D;
import java.util.Objects;

import hageldave.imagingkit.core.Img;
import hageldave.imagingkit.core.util.ImageFrame;
import hageldave.imagingkit.filter.implementations.GenericColorChannelTransformation;
import hageldave.imagingkit.filter.util.MiscUtils;

public class ColorCurves extends GenericColorChannelTransformation {
	private static final Curve IDENTITY_CURVE = Curve.identity();

	
	private Curve curveR = IDENTITY_CURVE;
	private Curve curveG = IDENTITY_CURVE;
	private Curve curveB = IDENTITY_CURVE;
	private Curve curveA = IDENTITY_CURVE;
	private Curve curveRGB = IDENTITY_CURVE;
	private CurveCombinator curveCombination = CurveCombinator.addition();
	
	
	public ColorCurves() {
		this(Curve.identity());
	}
	
	public ColorCurves(Curve curve) {
		setCurve(curve);
	}

	@Override
	protected int transformAlpha(int a) {
		return (int) MiscUtils.clamp(0,255*curveA.interpolate(a/255.0),1);
	}
	
	@Override
	protected int transformRed(int r) {
		return (int) (255*applyCurvesClamped(r/255.0, curveRGB, curveR, curveCombination));
	}

	@Override
	protected int transformGreen(int g) {
		return (int) (255*applyCurvesClamped(g/255.0, curveRGB, curveG, curveCombination));
	}

	@Override
	protected int transformBlue(int b) {
		return (int) (255*applyCurvesClamped(b/255.0, curveRGB, curveB, curveCombination));
	}
	
	private static double applyCurvesClamped(double v, Curve curveRGB, Curve channelCurve, CurveCombinator combination){
		return MiscUtils.clamp(0, combination.interpolateWithCurves(v, curveRGB, channelCurve), 1);
	}
	
	public void setCurve(Curve curve) {
		Objects.requireNonNull(curve);
		this.curveRGB = curve;
	}
	
	public Curve getCurve() {
		return curveRGB;
	}
	
	public CubicHermiteSpline setSpline(Point2D[] controlPoints){
		return (CubicHermiteSpline) (this.curveRGB = new CubicHermiteSpline(controlPoints));
	}

	public static void drawCurve(Point2D[] curvep) {
		CubicHermiteSpline curve = new CubicHermiteSpline(curvep);
		Img img = new Img(1024, 1024);
		img.fill(0xff000000);
		for(int x = 0; x < img.getWidth(); x++){
			double v = x*1.0/img.getWidth();
			int y = (int)(img.getHeight()*curve.interpolate(v));
			img.setValue(x, y, 0xff00ff00);
		}
		img.paint(g->{
			g.setColor(Color.WHITE);
			for(Point2D p : curvep){
				int x = (int) (p.getX()*img.getWidth());
				int y = (int) (p.getY()*img.getHeight());
				g.drawOval(x-2, y-2, 4, 4);
			}
		});
		ImageFrame.display(img);
	}
	
}
