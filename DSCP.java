package compiler;

import java.util.LinkedList;

public class DSCP {
	
	public IdKind idKind;
	public String CV;
	public DSCP type;
	public AddressingMode addmd;
	
	public int address;
	public int level;
	
	public int activationRecord;
	
	//----> this is zero for all :|
	//	public LinkedList<Integer> lb;
	public LinkedList<Integer> ub; 
	public LinkedList<DSCP> arg; 
	public LinkedList<DSCP> fld; 
	
	public int size;
	
	public DSCP() {
		CV = "";
		type = null;
		addmd = null;
		address = 0;
		level = 0;
		activationRecord = 0;
		ub = new LinkedList<Integer>();
		arg = new LinkedList<DSCP>();
		fld = new LinkedList<DSCP>();
		size = 0;
	}
	
	//----> needs to be checked :|
	public void setSize() {
		
		int size = 0;
		for(Integer i : ub) {
			size = size*i;
		}
		
		int temp = 0;
		for(DSCP i : fld) {
			temp += i.size;
		}
		if(!fld.isEmpty())
			size = size*temp;
		
		int factor = type.size;
		if( type.CV.equals("string") )
			factor = CV.length()*1;
		else if (type.CV.equals("boolean") )
			factor = 1;
		else if (type.CV.equals("int") )
			factor = 4;
		else if (type.CV.equals("float") )
			factor = 4;
		else if (type.CV.equals("char") )
			factor = 1;
		this.size = size*factor;
	}

	public boolean equals(Object obj) {
		DSCP temp = (DSCP) obj;
		return ( (temp.CV.equals(this.CV)) && (temp.level == this.level) );
	}
	
	
}
