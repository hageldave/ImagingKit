package hageldave.imagingkit.core.util;

public class Pair<E1,E2> {
	public E1 e1;
	public E2 e2;
	public Pair() {this(null,null);}
	public Pair(E1 e1, E2 e2) {
		this.e1 = e1;
		this.e2 = e2;
	}
	@Override
	public boolean equals(Object obj) {
		if(obj instanceof Pair){
			Pair<?,?> other = (Pair<?,?>) obj;
			boolean firstIdentical = e1==null ? other.e1==null : e1==other.e1 || e1.equals(other.e1);
			boolean secondIdentical= e2==null ? other.e2==null : e2==other.e2 || e2.equals(other.e2);
			return firstIdentical && secondIdentical;
		}
		return false;
	}
	@Override
	public int hashCode() {
		int result = 1;
		result = 31 * result + (e1 == null ? 0 : e1.hashCode());
		result = 31 * result + (e2 == null ? 0 : e2.hashCode());
		return result;
	}
}
