package compiler;

import java.util.ArrayList;


public class VirtualMachine {
	
	public ArrayList<Instruction> code;
	public int pc;

	public int sp;
	
	public VirtualMachine(ArrayList<Instruction> code){
		this.code = code;
		for(Instruction i: code)
			if(i!=null)
				System.out.println(i);
	}
}
