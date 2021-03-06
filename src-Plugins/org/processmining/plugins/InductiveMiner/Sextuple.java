package org.processmining.plugins.InductiveMiner;

public class Sextuple<A,B,C,D,E,F> {
	private final A a;
	private final B b;
	private final C c;
	private final D d;
	private final E e;
	private final F f;
	
	public Sextuple(A a, B b, C c, D d, E e, F f) {
		this.a = a;
		this.b = b;
		this.c = c;
		this.d = d;
		this.e = e;
		this.f = f;
	}
	
	public static <A,B,C,D,E,F> Sextuple<A,B,C,D,E,F> of(A a, B b, C c, D d, E e, F f){
        return new Sextuple<A,B,C,D,E,F>(a,b,c,d,e,f);
    }
	
	public A getA() { return a; }
	public B getB() { return b; }
	public C getC() { return c; }
	public D getD() { return d; }
	public E getE() { return e; }
	public F getF() { return f; }
	
	@Override
	public int hashCode() { return a.hashCode() ^ b.hashCode() ^ c.hashCode() ^ d.hashCode() ^ e.hashCode(); }
	
	@SuppressWarnings("rawtypes")
	@Override
	  public boolean equals(Object o) {
	    if (o == null) return false;
	    if (!(o instanceof Sextuple)) return false;
	    Sextuple pairo = (Sextuple) o;
	    return this.a.equals(pairo.getA()) &&
	           this.b.equals(pairo.getB()) &&
	           this.c.equals(pairo.getC()) &&
	           this.d.equals(pairo.getD()) &&
	           this.e.equals(pairo.getE()) &&
	           this.f.equals(pairo.getF());
	  }
}
