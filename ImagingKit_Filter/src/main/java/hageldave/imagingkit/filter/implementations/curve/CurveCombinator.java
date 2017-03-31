package hageldave.imagingkit.filter.implementations.curve;

public interface CurveCombinator {
	public double interpolateWithCurves(double v, Curve curve1, Curve curve2);
	
	public default Curve combine(Curve curve1, Curve curve2){
		return v->interpolateWithCurves(v, curve1, curve2);
	}
	
	public default CurveCombinator swappedCurves(){
		return (v, c1, c2) -> interpolateWithCurves(v, c2, c1);
	}
	
	public static CurveCombinator addition(){return (v,c1,c2)->c1.interpolate(v)+c2.interpolate(v);}
	public static CurveCombinator subtraction(){return (v,c1,c2)->c1.interpolate(v)-c2.interpolate(v);}
	public static CurveCombinator multiplication(){return (v,c1,c2)->c1.interpolate(v)*c2.interpolate(v);}
	public static CurveCombinator division(){return (v,c1,c2)->c1.interpolate(v)/c2.interpolate(v);}
	public static CurveCombinator composition(){return (v,c1,c2)->c2.interpolate(c1.interpolate(v));}
}