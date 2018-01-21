package compiler;

public class Entry{
	
	public String CV;
	public int level;
	
	public boolean equals(Object obj) {
		Entry temp = (Entry)obj;
		return ( (temp.CV.equals(CV)) && (temp.level == level) );
	}
}
