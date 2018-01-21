package compiler;

import java.util.ArrayList;

public class SymbolTable {
	
	public ArrayList<DSCP> SYMTB = new ArrayList<DSCP>();

	public void insert(DSCP dscp) {
		SYMTB.add(dscp);
	}

	public DSCP get(Entry entry) {
		for (DSCP i : SYMTB)
			if (entry.equals(i))
				return i;
		return null;
	}

	public boolean contains(Entry entry) {
		for (DSCP i : SYMTB) 
			if (entry.equals(i))
				return true;
		return false;
	}
	
	public void remove(Entry entry){
		DSCP toBeRemoved=null;
		for (DSCP i : SYMTB) 
			if (entry.equals(i))
				toBeRemoved = i;
		SYMTB.remove(toBeRemoved);
				
	}
	public void update(DSCP dscp){
		Entry temp = new Entry();
		temp.CV = dscp.CV;
		temp.level = dscp.level;
		remove(temp);
		insert(dscp);
	}
	
	public void flush(int level){
		ArrayList<DSCP> toBeRemoved= new ArrayList<DSCP>();
		for (DSCP i : SYMTB) 
			if (i.level==(level))
				toBeRemoved.add(i);
		for(DSCP i : toBeRemoved)
			SYMTB.remove(i);
	}
	
}
