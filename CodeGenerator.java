package compiler;

import java.util.ArrayList;
import java.util.Stack;

public class CodeGenerator 
{
    Scanner scanner; // This was my way of informing CG about Constant Values detected by Scanner, you can do whatever you like
    
    // Define any variables needed for code generation
    private SymbolTable symbolTable = new SymbolTable();
    private Stack<DSCP> ss = new Stack<DSCP>();
    private int depth = 0;
    private int foff = 0, goff = 0;
    private ArrayList<Instruction> code = new ArrayList<Instruction>();
    
    
    
    private DSCP intType;
    private DSCP charType;
    private DSCP booleanType;
    private DSCP floatType;
    private DSCP voidType;
    private DSCP stringType;
 
    public CodeGenerator(Scanner scanner)
    {
        this.scanner = scanner;
		
        intType = new DSCP();
        intType.level = -1;
        intType.idKind = IdKind.type;
		intType.CV = "int";

		charType = new DSCP();
		charType.level = -1;
        charType.idKind = IdKind.type;
		charType.CV = "char";

		booleanType = new DSCP();
		booleanType.level = -1;
        booleanType.idKind = IdKind.type;
		booleanType.CV = "boolean";

		floatType = new DSCP();
        floatType.level = -1;
		floatType.idKind = IdKind.type;
		floatType.CV = "float";
		
		voidType = new DSCP();
		voidType.level = -1;
        voidType.idKind = IdKind.type;
		voidType.CV = "void";
		
		stringType = new DSCP();
		stringType.level = -1;
        stringType.idKind = IdKind.type;
		stringType.CV = "string";


    }

    public void Generate(String sem) throws Exception
    {
    	System.out.println(sem); // Just for debug
    	
            if (sem.equals("NoSem")) {
            	return;
            } else if (sem.equals("inDCL")) {
            	Entry entry = new Entry();
            	entry.CV = scanner.getCV();
            	entry.level = -1;
            	if(symbolTable.contains(entry))
            		ss.push(symbolTable.get(entry));
            	else {
            		boolean found = false;
            		for(int i = scanner.level; i >= 0; i--) {
            			entry = new Entry();
                    	entry.CV = scanner.getCV();
                    	entry.level = i;
                    	if(symbolTable.contains(entry)) {
                    		ss.push(symbolTable.get(entry));
                    		found = true;
                    		break;
                    	}
            		}	
            		//-------> not defined type!
            		if(!found) 
            			throw new Exception("An error occured on token " + scanner.getCV() );
            	}
            	scanner.inDCL = true;
            } else if (sem.equals("@decrease_depth")) {
            	scanner.level--;
            } else if (sem.equals("flip")) {
            	DSCP temp = ss.pop();
            	temp.address = foff;
            	temp.setSize();
            	foff = foff + temp.size;
            	symbolTable.update(temp);
            	DSCP type = ss.pop();
            	DSCP mother = ss.pop();
            	mother.fld.add(temp);
            	ss.push(mother);
            	ss.push(type);
            	symbolTable.update(mother);
            	scanner.inDCL = false;
            } else if (sem.equals("not_complete_flip")) {
            	DSCP temp = ss.pop();
            	temp.address = foff;
            	temp.setSize();
            	foff = foff + temp.size;
            	symbolTable.update(temp);
            	DSCP mother = ss.pop();
            	mother.fld.add(temp);
            	ss.push(mother);
            	symbolTable.update(mother);
            	scanner.inDCL = false;
            } else if (sem.equals("s_flip")) {
            	//----> complete this!
            	scanner.inDCL = false;
            } else if (sem.equals("inSDL")) {
            	//----> complete this!
            	scanner.inDCL = false;
            } else if (sem.equals("inMTD")) {
            	scanner.level++;
            	DSCP temp = ss.pop();
            	//----> damn it! :( not yet complete
            	scanner.inMTD = true;
            } else if (sem.equals("m_flip")) {
            	//----> complete this!
            	scanner.inMTD = false;
            } else if (sem.equals("MDSCP")) {
            	Entry entry = new Entry();
            	entry.CV = scanner.getCV();
            	entry.level = scanner.level;
            	if(symbolTable.contains(entry)) 
            		//-------> declaration twice
            		throw new Exception("An error occured on token " + scanner.getCV() );
            	if( ss.isEmpty() || ss.peek().idKind != IdKind.type )
            		//-------> probably this wont happen, cause we've pushed some type in ss right before this
            		throw new Exception("An error occured on token " + scanner.getCV() );
            	DSCP temp = new DSCP();
            	DSCP type = ss.peek();
            	temp.level = entry.level;
            	//------> occasionally it will need to be updated
            	temp.idKind = IdKind.variable;
            	temp.CV = scanner.getCV();
            	temp.type = type;
            	symbolTable.insert(temp);
            	ss.push(temp);
            } else if(sem.equals("@or")) {
            	generateTwoOprCode("||");
            } else if(sem.equals("@and")) {
            	generateTwoOprCode("&&");
            } else if(sem.equals("@equal")) {
            	generateTwoOprCode("==");
            } else if(sem.equals("@not_equal")) {
            	generateTwoOprCode("!=");
            } else if(sem.equals("@smaller")) {
            	generateTwoOprCode("<");
            } else if(sem.equals("@smaller_equal")) {
            	generateTwoOprCode("<=");
            } else if(sem.equals("@bigger")) {
            	generateTwoOprCode(">");
            } else if(sem.equals("@bigger_equal")) {
            	generateTwoOprCode(">=");
            } else if(sem.equals("@add")) {
            	generateTwoOprCode("+");
            } else if(sem.equals("@sub")) {
            	generateTwoOprCode("-");
            } else if(sem.equals("@div")) {
            	generateTwoOprCode("/");
            } else if(sem.equals("@mod")) {
            	generateTwoOprCode("%");
            } else if(sem.equals("@mult")) {
            	generateTwoOprCode("*");
            } else if(sem.equals("@not")) {
            	generateOneOprCode("!");
            } else if(sem.equals("@unary")) {
            	generateOneOprCode("u-");
            } else if (sem.equals("@push_float")) {
            	DSCP stp = MDSCP(scanner.getCV(),IdKind.literal,floatType);
            	ss.push(stp);
    		} else if (sem.equals("@push_int")) {
            	DSCP stp = MDSCP(scanner.getCV(),IdKind.literal,intType);
            	ss.push(stp);
    		} else if (sem.equals("@push_bool")) {
            	DSCP stp = MDSCP(scanner.getCV(),IdKind.literal,booleanType);
            	ss.push(stp);
    		} else if (sem.equals("@push_string")) {
            	DSCP stp = MDSCP(scanner.getCV(),IdKind.literal,stringType);
            	ss.push(stp);
    		} else if (sem.equals("@push_void")) {
            	DSCP stp = MDSCP(scanner.getCV(),IdKind.literal,voidType);
            	ss.push(stp);
    		} else if (sem.equals("@push_char")) {
            	DSCP stp = MDSCP(scanner.getCV(),IdKind.literal,charType);
            	ss.push(stp);
    		} else if (sem.equals("@push_variable")) {
            	DSCP stp = null;
            	for(int i=depth; i>=0; i--) {
            		Entry temp = new Entry();
            		temp.CV = scanner.getCV();
            		temp.level = i;
            		if(symbolTable.contains(temp)) {
            			stp = symbolTable.get(temp);
            			break;
            		}	
            	}
            	if( stp != null )
            		ss.push(stp);
            	else
            		throw new Exception("An error occured on token " + scanner.getCV() );
    		} else if (sem.equals("@ub")) {
    			DSCP temp = ss.pop();
    			temp.idKind = IdKind.array;
    			temp.ub.add(Integer.parseInt(scanner.getCV()));
    			symbolTable.update(temp);
    			ss.push(temp);
    		} 
    }
    

    private void generateTwoOprCode(String operator) {
    	DSCP opr1 = ss.pop();
    	DSCP opr2 = ss.pop();
    	DSCP opr3 = null;
    	boolean typeMatch;
    	typeMatch = checkTypeTwoOpr(opr1,opr2,operator);
    	if( typeMatch ) {
    		opr3 = getTemp(opr1.type,operator,opr1.level);
    		Instruction inst = new Instruction();
    		inst.opr1 = opr1;
    		inst.opr2 = opr2;
    		inst.opr3 = opr3;
    		inst.opcode = operator;
    		code.add(inst);
    	}
    		
    }
    
    private void generateOneOprCode(String operator) {
    	DSCP opr1 = ss.pop();
    	DSCP opr2 = null;
    	boolean typeMatch;
    	typeMatch = checkTypeOneOpr(opr1,opr2,operator);
    	if( typeMatch ) {
    		opr2 = getTemp(opr1.type,operator,opr1.level);
    		Instruction inst = new Instruction();
    		inst.opr1 = opr1;
    		inst.opr2 = opr2;
    		inst.opr3 = null;
    		inst.opcode = operator;
    		code.add(inst);
    	}
    		
    }
    
    int tpnum = 0;
    
	private DSCP getTemp(DSCP type, String operator, int level) {
		DSCP temp = new DSCP();
		temp.idKind = IdKind.temp;
		temp.addmd = AddressingMode.ld;
		if( operator.equals("&&") || operator.equals("||") || operator.equals(">") || operator.equals("<") || operator.equals(">=") || operator.equals("<=") ) 
			temp.type = booleanType;
		else
			temp.type = type.type;
		temp.CV = "#t" + tpnum;
		temp.level = level;
		symbolTable.SYMTB.add(temp);
		ss.add(temp);
		tpnum++;
		return temp;
	}

	private boolean checkTypeTwoOpr(DSCP opr1, DSCP opr2, String operator) {
		if( opr1.type.equals(opr2.type) )
			return false;
		else {
			if( ( operator.equals("&&") || operator.equals("||") ) && opr1.type.equals(booleanType) )
				return true;
			if( ( operator.equals("+") || operator.equals("-") || operator.equals("*") || operator.equals("/") || operator.equals(">") || operator.equals("<") || operator.equals("%") || operator.equals(">=") || operator.equals("<=") )  && ( opr1.type.equals(intType) ) || opr1.type.equals(floatType) )
				return true;
			}
		return false;
	}
	
	private boolean checkTypeOneOpr(DSCP opr1, DSCP opr2, String operator) {
		if( operator.equals("!") && opr1.type.equals(booleanType) )
			return true;
		if(  operator.equals("u-") && ( opr1.type.equals(intType) ) || opr1.type.equals(floatType) )
			return true;
		if( operator.equals(":=") && ( opr1.type.equals(opr2.type) ) )
			return true;	
		return false;
	}

	private DSCP MDSCP(String CV, IdKind idKind, DSCP type) {
		DSCP temp = new DSCP();
		temp.CV = CV;
		temp.idKind = idKind;
		temp.type = type;
    	return temp;
    }
    
	public void FinishCode() // You may need this
    {

    }

    public void WriteOutput(String outputName)
    {
    	for (Instruction inst : code) {
			System.out.print(inst.opcode + " ");
			System.out.print((inst.opr1.addmd ) + ("_")
					+ (inst.opr1.type.toString()) + ("_")
					+ (inst.opr1.address) + " ");
			if (inst.opr2 != null) {
				System.out.print((inst.opr2.addmd ) + ("_")
						+ (inst.opr2.type.toString()) + ("_")
						+ (inst.opr2.address) + " ");
			}
			if (inst.opr3 != null) {
				System.out.print((inst.opr3.addmd) + ("_")
						+ (inst.opr3.type.toString()) + ("_")
						+ (inst.opr3.address) + " ");
			}
			
		}
    }
}
