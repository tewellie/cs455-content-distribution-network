package cdn;

public class Pair<T1, T2> {

	public T1 A;
	public T2 B;
	
	Pair (T1 a, T2 b){
		A = a;
		B= b;
	}
	
	
	public boolean equals(Pair<T1, T2> other){
		if(A.equals(other.A)&&B.equals(other.B))return true;
		else if(A.equals(other.B) && B.equals(other.A)) return true;
		else return false;
	}
	
	public String toString(){
		return A.toString() + " " + B.toString();
	}
	
	public boolean contains(T1 item){
		if(A.equals(item) || B.equals(item)) return true;
		else return false;
	}
	
	
}
