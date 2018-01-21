package compiler;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;


/*
 * Scanner class code,
 * 
 * author: Siavash Nazari 
 * 
 * p.s. I know the code is dirty and awful, I'm gonna fix this.   
 * 
 */

public class Scanner {

	private BufferedReader in;

	public HashMap<String , DSCP> SYMTB;
	public boolean inDCL = false;
	public boolean inMTD = false;
	public int level = 0;
	
	int lineNumber;
	private String currentLine;
	private String token;
	private String CV;

	private HashSet<String> keywords;
	private HashSet<Character> arithmaticOperators;
	private	HashSet<Character> relativeOperators;
	private HashSet<Character> logicalOperations;
	private HashSet<Character> punctuationMarks;
	private HashSet<Character> digits;
	private HashSet<Character> hexadecimals;
	
	private boolean scanComplete = false;


	public Scanner(String filename) throws Exception {

		File f = new File(filename);
		if (!f.exists())
			throw new Exception ("File does not exist: " + f);
		if (!f.isFile())
			throw new Exception("Should not be a directory: " + f);
		if (!f.canRead())
			throw new Exception("Can not read input file: " + f);

		
		currentLine = new String("");
		CV = new String("");
		lineNumber = 1;

		try {
			in = new BufferedReader(new FileReader(new File(filename)));
		} catch(IOException e) {
			throw new RuntimeException(e);
		}

		keywords = new HashSet<String>(); 
		{
			keywords.add("boolean");
			keywords.add("break");
			keywords.add("continue");
			keywords.add("else");
			keywords.add("float");
			keywords.add("for");
			keywords.add("if");
			keywords.add("int");
			keywords.add("readfloat");
			keywords.add("readint");
			keywords.add("return");
			keywords.add("string");
			keywords.add("struct");
			keywords.add("void");
			keywords.add("writetext");
			keywords.add("writefloat");
			keywords.add("writeint");
		}

		arithmaticOperators = new HashSet<Character>();
		{
			arithmaticOperators.add('-');
			arithmaticOperators.add('*');
			arithmaticOperators.add('/');
			arithmaticOperators.add('%');
			arithmaticOperators.add('+');
		}

		relativeOperators = new HashSet<Character>();
		{
			relativeOperators.add('=');
			relativeOperators.add('!');
			relativeOperators.add('<');
			relativeOperators.add('>');
		}

		logicalOperations = new HashSet<Character>();
		{
			logicalOperations.add('&');
			logicalOperations.add('|');
		}

		punctuationMarks = new HashSet<Character>();
		{
			punctuationMarks.add('(');
			punctuationMarks.add(')');
			punctuationMarks.add('{');
			punctuationMarks.add('}');
			punctuationMarks.add('[');
			punctuationMarks.add(']');
			punctuationMarks.add(',');
			punctuationMarks.add(';');
		}

		digits = new HashSet<Character>();
		{
			digits.add('0');
			digits.add('1');
			digits.add('2');
			digits.add('3');
			digits.add('4');
			digits.add('5');
			digits.add('6');
			digits.add('7');
			digits.add('8');
			digits.add('9');
		}

		hexadecimals = new HashSet<Character>();
		{
			hexadecimals.add('a');
			hexadecimals.add('b');
			hexadecimals.add('c');
			hexadecimals.add('d');
			hexadecimals.add('e');
			hexadecimals.add('f');
			hexadecimals.add('A');
			hexadecimals.add('B');
			hexadecimals.add('C');
			hexadecimals.add('D');
			hexadecimals.add('E');
			hexadecimals.add('F');

		}

	}

	private void nextLine() {

		try {
			String s;   
			if((s = in.readLine()) != null) {
				this.currentLine = s;
			}
			else {
				scanComplete = true;
				this.currentLine = "$";
			}
			lineNumber++;
		} 
		catch(IOException e) {
			throw new RuntimeException(e);
		}

	}

	// -------------------- NextToken() scans the code line-by-line
	public String NextToken() throws Exception {

		if( scanComplete ) 
			return "$";

		// -------------------- removing comments before scanning the line
		removeComments();



		// --------------------  removing white spaces from beginning of the current line 
		while( currentLine == null  || currentLine.equals("") )
			nextLine();

		boolean isAllWhite = true; 
		for( int i=0; i<currentLine.length(); i++)
			if( !Character.isWhitespace(currentLine.charAt(i)) ) {
				isAllWhite = false;
				break;
			}
		if( isAllWhite )
			nextLine();

		int beginIndex = 0;

		while( beginIndex < currentLine.length()-1 &&
				Character.isWhitespace(new Character(currentLine.charAt(beginIndex))) )
			beginIndex++; 

		currentLine = currentLine.substring(beginIndex);

		beginIndex =  0;
		int endIndex = beginIndex;
		boolean inString = false;
		if(currentLine.charAt(beginIndex) != '\"' ) {
			while( endIndex != currentLine.length() &&
					!Character.isWhitespace(new Character(currentLine.charAt(endIndex))) )
				endIndex++; 
		}
		else {
			inString = true;
			endIndex++;
			while( endIndex != currentLine.length() &&
					!( (currentLine.charAt(endIndex-1)!='\\') && currentLine.charAt(endIndex)=='\"' ) )
				endIndex++; 
			endIndex++;
		}
		while( currentLine == null  || currentLine.equals("") )
			nextLine();
		// -------------------- white space removal finished
		if(!inString) {
			// - finding operators -
			// --------------------- finding arithmetic operators and punctuation marks
			if( arithmaticOperators.contains( currentLine.charAt(beginIndex) ) 
					|| punctuationMarks.contains( currentLine.charAt(beginIndex)) ) {
				token = Character.toString( currentLine.charAt(beginIndex) );
				currentLine = currentLine.substring(1);
				CV = "";
				return token;
			}
			for( int i=beginIndex; i < endIndex; i++ ) {
				if( arithmaticOperators.contains( currentLine.charAt(i)) 
						|| punctuationMarks.contains( currentLine.charAt(i)) 
						|| relativeOperators.contains( currentLine.charAt(i))
						|| logicalOperations.contains( currentLine.charAt(i)) ) {
					endIndex = i;
					break;
				}
			}
			// -------------------- finding relative operators
			if( relativeOperators.contains( currentLine.charAt(beginIndex)) ) {
				if( currentLine.length() == 1 ) {
					token = Character.toString( currentLine.charAt(beginIndex) );
					currentLine = currentLine.substring(1);
					CV = "";
					return token;
				}
				else {
					if( currentLine.charAt(beginIndex+1) == '='	) {
						token = currentLine.substring(beginIndex,beginIndex+2);
						currentLine = currentLine.substring(2);
						CV = "";
						return token;
					}
					else {
						token = Character.toString( currentLine.charAt(beginIndex) );
						currentLine = currentLine.substring(1);
						CV = "";
						return token;
					}
				}
			}
			// -------------------- finding logical operators
			if( logicalOperations.contains( currentLine.charAt(beginIndex)) ) {
				if( currentLine.length() == 1 ) {
					token = "Error: " + Character.toString( currentLine.charAt(beginIndex) );
					currentLine = currentLine.substring(1);
					throw new Exception("invalid token at line : " + lineNumber + " = " + currentLine.substring(beginIndex,endIndex));
				}
				else {
					if( currentLine.charAt(beginIndex+1) == currentLine.charAt(beginIndex) ) {
						token = currentLine.substring(beginIndex,beginIndex+2);
						currentLine = currentLine.substring(2);
						CV = "";
						return token;
					}
					else {
						token = "Error: " + Character.toString( currentLine.charAt(beginIndex) );
						currentLine = currentLine.substring(1);
						throw new Exception("invalid token at line : " + lineNumber + " = " + currentLine.substring(beginIndex,endIndex));
					}
				}
			}

			// -------------------- finding char_literal
			// ------------------------------------------- char_literal
			if( currentLine.charAt(beginIndex) == '\'' ) {

				// -------------------- simple case, length = 1
				if( currentLine.length() > beginIndex + 2 ) {

					// -------------------- correct form
					if( !( currentLine.charAt(beginIndex+1) == '\\' 
							|| currentLine.charAt(beginIndex+1) == '\''
							|| currentLine.charAt(beginIndex+1) == '\"' )
							&& currentLine.charAt(beginIndex+2) == '\'' ) {
						token = "char_literal";
						CV = Character.toString(currentLine.charAt(beginIndex+1));
						currentLine = currentLine.substring(3);
						return token;
					}

				}
				// -------------------- cases with '\'marks, length = 2
				if ( currentLine.length() > beginIndex + 3 ) {

					if( currentLine.charAt(beginIndex+1) == '\\'
							&& currentLine.charAt(beginIndex+3) == '\'' ) {
						if( currentLine.charAt(beginIndex+2) == '\\' ) {
							token = "char_literal";
							CV = Character.toString('\\');
							currentLine = currentLine.substring(4);
							return token;
						}
						if( currentLine.charAt(beginIndex+2) == 't' ) {
							token = "char_literal";
							CV = Character.toString('\t');
							currentLine = currentLine.substring(4);
							return token;
						}
						if( currentLine.charAt(beginIndex+2) == 'n' ) {
							token = "char_literal";
							CV = Character.toString('\n');
							currentLine = currentLine.substring(4);
							return token;
						}
						if( currentLine.charAt(beginIndex+2) == '\"' ) {
							token = "char_literal";
							CV = Character.toString('\"');
							currentLine = currentLine.substring(4);
							return token;
						}
						if( currentLine.charAt(beginIndex+2) == '\'' ) {
							token = "char_literal";
							CV = Character.toString('\'');
							currentLine = currentLine.substring(4);
							return token;
						}
					}
					else {
						token = "Error: " + Character.toString( currentLine.charAt(beginIndex) );
						throw new Exception("invalid token at line : " + lineNumber + " = " + currentLine.substring(beginIndex,endIndex));
					}

				} 
				// -------------------- there'd be some error...
				else{
					token = "Error: " + Character.toString( currentLine.charAt(beginIndex) );
					throw new Exception("invalid token at line : " + lineNumber + " = " + currentLine.substring(beginIndex,endIndex));
				}
			}
		}
		// -------------------- finding string_literal
		// --------------------------------------------- string_literal 
		if( currentLine.charAt(beginIndex) == '"' ) {
			// -------------------- length should be more than 2
			if( currentLine.length() > beginIndex+1 ) {

				int CVendIndex = beginIndex;
				CVendIndex++;
				while( CVendIndex < endIndex ) {
					if ( currentLine.charAt(CVendIndex-1) != '\\' 
							&& currentLine.charAt(CVendIndex) == '"' ) {
						token = "string_literal";
						CV = currentLine.substring(beginIndex+1,CVendIndex);
						// -------------------- fixing CV value
						for(int i=0; i<CV.length()-2; i++) {
							if(CV.charAt(i)=='\\') {
								if(CV.charAt(i+1)=='\\')
									CV = CV.substring(0, i) + Character.toString('\\') + CV.substring(i+2);
								if(CV.charAt(i+1)=='\'')
									CV = CV.substring(0, i) + Character.toString('\'') + CV.substring(i+2);
								if(CV.charAt(i+1)=='t')
									CV = CV.substring(0, i) + Character.toString('\t') + CV.substring(i+2);
								if(CV.charAt(i+1)=='n')
									CV = CV.substring(0, i) + Character.toString('\n') + CV.substring(i+2);
								if(CV.charAt(i+1)=='\"')
									CV = CV.substring(0, i) + Character.toString('\"') + CV.substring(i+2);	
							}
						}
						// -------------------- fixing CV value, a special case :D
						boolean changed = false;
						int i = CV.length()-2;
						if( i > 0 ) {
							if(CV.charAt(i)=='\\') {
								if( !changed && CV.charAt(i+1)=='\\') {
									CV = CV.substring(0, i) + Character.toString('\\');
									changed = true;
								}
								if( !changed && CV.charAt(i+1)=='\'') {
									CV = CV.substring(0, i) + Character.toString('\'');
									changed = true;
								}
								if( !changed  && CV.charAt(i+1)=='t') {
									CV = CV.substring(0, i) + Character.toString('\t');
									changed = true;
								}
								if( !changed  && CV.charAt(i+1)=='n') {
									CV = CV.substring(0, i) + Character.toString('\n');
									changed = true;
								}
								if(  !changed && CV.charAt(i+1)=='\"') {
									CV = CV.substring(0, i) + Character.toString('\"');
									changed = true;
								}
							}
						}
						token = "string_literal";
						currentLine = currentLine.substring(CVendIndex+1);
						return token;
					}

					CVendIndex++;

				}



				// -------------------- maybe unnecessary :|
				//token = "Error: " + Character.toString( currentLine.charAt(beginIndex) );
				//throw new Exception("invalid token at line : " + lineNumber + " = " + currentLine.substring(beginIndex,endIndex));
			}

			// -------------------- there'd be some errors...
			else {
				token = "Error: " + Character.toString( currentLine.charAt(beginIndex) );
				throw new Exception("invalid token at line : " + lineNumber + " = " + currentLine.substring(beginIndex,endIndex));
			}


		}
		// -------------------- finding number_literals
		// ----------------------------------------------- number_literal 
		int CVendIndex;
		boolean isFloat = true;
		boolean isHex = true;
		boolean isInt = true;
		boolean isID = true;

		//---- first character is (dot)...
		if( currentLine.charAt(beginIndex) == '.'  ) {

			//---- is a single (dot)
			if( currentLine.length()-1 == beginIndex
					|| Character.isWhitespace(currentLine.charAt(beginIndex+1)) 
					|| beginIndex+1 == endIndex ) {
				token = ".";
				CV = "";
				currentLine = currentLine.substring(1);
				return token;	
			}
			//---- (dot)-beginning float_literal
			if( currentLine.length() > beginIndex ) {
				CVendIndex = beginIndex;
				CVendIndex++;
				while( CVendIndex < endIndex ) {
					if( !digits.contains(currentLine.charAt(CVendIndex)) 
							&& currentLine.charAt(CVendIndex)!='.' ) {
						isFloat = false;
					}
					if( Character.isWhitespace(currentLine.charAt(CVendIndex))
							|| currentLine.charAt(CVendIndex)=='.')
						break;

					CVendIndex++;

				}
				if( isFloat == true ) {
					token = "float_literal";
					CV = currentLine.substring(beginIndex,CVendIndex);
					currentLine = currentLine.substring(CVendIndex);
					return token;
				}	
				// -------------------- maybe it's not always a float_literal
				else {
					token = ".";
					CV = "";
					currentLine = currentLine.substring(1);
					return token;	
				}
			}	
		}
		//---- first character is (zero)...
		if ( currentLine.charAt(beginIndex) == '0' ) {

			//---- is a single (zero)
			if( currentLine.length()-1 == beginIndex
					|| Character.isWhitespace(currentLine.charAt(beginIndex+1))
					|| beginIndex+1 == endIndex ) {
				token = "int_literal";
				CV = Character.toString(currentLine.charAt(0));
				currentLine = currentLine.substring(1);
				return token;	
			}
			//---- (zero)-beginning hex_literal or int_literal
			if( currentLine.length() > beginIndex+1 ) {
				//-- "0x" things
				if( currentLine.charAt(beginIndex+1) == 'x' ) {

					CVendIndex = beginIndex;
					CVendIndex+=2;
					while( CVendIndex < endIndex ) {
						if( !digits.contains(currentLine.charAt(CVendIndex))
								&& !hexadecimals.contains(currentLine.charAt(CVendIndex)) 
								&& currentLine.charAt(CVendIndex)!='.') {
							isHex = false;
						}
						if( Character.isWhitespace(currentLine.charAt(CVendIndex))
								|| currentLine.charAt(CVendIndex)=='.')
							break;

						CVendIndex++;

					}
					if( isHex == true ) {
						token = "int_literal";
						CV = Character.toString(currentLine.charAt(0));
						currentLine = currentLine.substring(CVendIndex);
						return token;
					}
					else {
						token = "Error: " + Character.toString( currentLine.charAt(beginIndex) );
						throw new Exception("invalid token at line : " + lineNumber + " = " + currentLine.substring(beginIndex,endIndex));
					}

				}
				//-- "0(digit)" things
				else if( digits.contains(currentLine.charAt(beginIndex+1)) ) {

					CVendIndex = beginIndex;
					CVendIndex++;
					isFloat = false;
					while( CVendIndex < endIndex ) {

						if( Character.isWhitespace(currentLine.charAt(CVendIndex))
								|| ( currentLine.charAt(CVendIndex)=='.' && isFloat)	)
							break;

						if( !isFloat && currentLine.charAt(CVendIndex) == '.' ) {
							isFloat = true;
							isInt = false;
							CVendIndex++;
						}
						if( isFloat && currentLine.charAt(CVendIndex) == '.' ) {
							//isFloat = false;
							isInt = false;
							break;
						}

						if( !digits.contains(currentLine.charAt(CVendIndex)) ) {
							isInt = false;
						}

						CVendIndex++;

					}
					if( isInt == true ) {
						token = "int_literal";
						CV = currentLine.substring(beginIndex,CVendIndex);
						currentLine = currentLine.substring(CVendIndex);
						return token;
					}
					else if( isFloat == true ) {
						token = "float_literal";
						CV = currentLine.substring(beginIndex,CVendIndex);
						currentLine = currentLine.substring(CVendIndex);
						return token;
					}
					else {
						token = "Error: " + Character.toString( currentLine.charAt(beginIndex) );
						throw new Exception("invalid token at line : " + lineNumber + " = " + currentLine.substring(beginIndex,endIndex));
					}

				}
				//-- "0." things
				else if( currentLine.charAt(beginIndex+1)=='.' ) {
					CVendIndex = beginIndex+1;
					CVendIndex++;
					isFloat = true;
					while( CVendIndex < endIndex ) {
						if( !digits.contains(currentLine.charAt(CVendIndex)) 
								&& currentLine.charAt(CVendIndex)!='.') {
							isFloat = false;
						}
						if( Character.isWhitespace(currentLine.charAt(CVendIndex))
								&& currentLine.charAt(CVendIndex)!='.' )
							break;

						CVendIndex++;

					}
					if( isFloat == true ) {
						token = "float_literal";
						CV = currentLine.substring(beginIndex,CVendIndex);
						currentLine = currentLine.substring(CVendIndex);
						return token;
					}
					else {
						token = "Error: " + Character.toString( currentLine.charAt(beginIndex) );
						throw new Exception("invalid token at line : " + lineNumber + " = " + currentLine.substring(beginIndex,endIndex));
					}
				}


			}
		}

		//---- first character is a (digit).
		if( digits.contains(currentLine.charAt(beginIndex)) ) {

			//---- is a single (digit)
			if( currentLine.length()-1 == beginIndex
					|| Character.isWhitespace(currentLine.charAt(beginIndex+1))
					|| beginIndex+1 == endIndex ) {
				token = "int_literal";
				CV = Character.toString(currentLine.charAt(0));
				currentLine = currentLine.substring(1);
				return token;	
			}
			CVendIndex = beginIndex;
			CVendIndex++;
			isFloat = false;
			while( CVendIndex < endIndex ) {

				if( Character.isWhitespace(currentLine.charAt(CVendIndex))
						|| ( currentLine.charAt(CVendIndex)=='.' && isFloat)	)
					break;

				if( !isFloat && currentLine.charAt(CVendIndex) == '.' ) {
					isFloat = true;
					isInt = false;
					CVendIndex++;
				}
				if( isFloat && currentLine.charAt(CVendIndex) == '.' ) {
					//isFloat = false;
					isInt = false;
					break;
				}

				if( !digits.contains(currentLine.charAt(CVendIndex)) ) {
					isInt = false;
				}

				CVendIndex++;

			}
			if( isInt == true ) {
				token = "int_literal";
				CV = currentLine.substring(beginIndex,CVendIndex);
				currentLine = currentLine.substring(CVendIndex);
				return token;
			}
			else if( isFloat == true ) {
				token = "float_literal";
				CV = currentLine.substring(beginIndex,CVendIndex);
				currentLine = currentLine.substring(CVendIndex);
				return token;
			}
			else {
				token = "Error: " + Character.toString( currentLine.charAt(beginIndex) );
				throw new Exception("invalid token at line : " + lineNumber + " = " + currentLine.substring(beginIndex,endIndex));
			}
		}

		// -------------------- finding IDs
		if( Character.isLetter(currentLine.charAt(beginIndex)) ) {

			if( currentLine.length()-1 == beginIndex
					|| Character.isWhitespace(currentLine.charAt(beginIndex+1))
					|| beginIndex+1 == endIndex ) {
				token = "id";
				CV = Character.toString(currentLine.charAt(0));
				currentLine = currentLine.substring(1);
				return token;
			}

			CVendIndex = beginIndex;
			CVendIndex++;
			while( CVendIndex < endIndex ) {

				if( currentLine.charAt(CVendIndex)=='.')
					break;

				if( !Character.isDigit(currentLine.charAt(CVendIndex))  
						&& !Character.isLetter(currentLine.charAt(CVendIndex) )
						&& currentLine.charAt(CVendIndex)!='_' ) {
					isID = false;
					break;
				}



				CVendIndex++;
			}
			if( isID == true ) {
				if( keywords.contains(currentLine.substring(beginIndex,CVendIndex)) ) {
					token = currentLine.substring(beginIndex,CVendIndex);
					CV = currentLine.substring(beginIndex,CVendIndex);
					currentLine = currentLine.substring(CVendIndex);
					return token;
				}
				if( currentLine.substring(beginIndex,CVendIndex).equals("true")
						|| currentLine.substring(beginIndex,CVendIndex).equals("false") ) {
					token = "bool_literal";
					CV = currentLine.substring(beginIndex,CVendIndex);
					currentLine = currentLine.substring(CVendIndex);
					return token;
				}
				token = "id";
				CV = currentLine.substring(beginIndex,CVendIndex);
				currentLine = currentLine.substring(CVendIndex);
				return token;
			}
			//else {
			//	token = "Error: " + Character.toString( currentLine.charAt(beginIndex) );
			//	throw new Exception("invalid token at line : " + lineNumber + " = " + currentLine.substring(beginIndex,endIndex));
			//}

		}

		token = currentLine.substring(beginIndex, endIndex);
		CV = currentLine.substring(beginIndex,endIndex);
		currentLine = currentLine.substring(endIndex);

		return token;
	}

	// -------------------- method to remove comments
	private void removeComments() {

		while( currentLine == null  || currentLine.equals("") )
			nextLine();
		
		boolean isAllWhite = true; 
		for( int i=0; i<currentLine.length(); i++)
			if( !Character.isWhitespace(currentLine.charAt(i)) ) {
				isAllWhite = false;
				break;
			}
		if( isAllWhite )
			nextLine();

		int beginIndex = 0;

		while( beginIndex < currentLine.length()-1 &&
				Character.isWhitespace(new Character(currentLine.charAt(beginIndex))) )
			beginIndex++; 

		currentLine = currentLine.substring(beginIndex);


		while( currentLine.startsWith("//") ) {
			nextLine();
			while( currentLine == null  || currentLine.equals("") )
				nextLine();

			isAllWhite = true; 
			for( int i=0; i<currentLine.length(); i++)
				if( !Character.isWhitespace(currentLine.charAt(i)) ) {
					isAllWhite = false;
					break;
				}
			if( isAllWhite )
				nextLine();

			beginIndex = 0;

			while( beginIndex < currentLine.length()-1 &&
					Character.isWhitespace(new Character(currentLine.charAt(beginIndex))) )
				beginIndex++; 

			currentLine = currentLine.substring(beginIndex);

			beginIndex =  0;
			int endIndex = beginIndex;

			while( endIndex != currentLine.length() &&
					!Character.isWhitespace(new Character(currentLine.charAt(endIndex))) )
				endIndex++;
		}

		if( currentLine.startsWith("/*") ) {
			while( !currentLine.contains("*/") ) {
				nextLine();
				while( currentLine == null  || currentLine.equals("") )
					nextLine();

				isAllWhite = true; 
				for( int i=0; i<currentLine.length(); i++)
					if( !Character.isWhitespace(currentLine.charAt(i)) ) {
						isAllWhite = false;
						break;
					}
				if( isAllWhite )
					nextLine();

				beginIndex = 0;

				while( beginIndex < currentLine.length()-1 &&
						Character.isWhitespace(new Character(currentLine.charAt(beginIndex))) )
					beginIndex++; 

				currentLine = currentLine.substring(beginIndex);

				beginIndex =  0;
				int endIndex = beginIndex;

				while( endIndex != currentLine.length() &&
						!Character.isWhitespace(new Character(currentLine.charAt(endIndex))) )
					endIndex++;
			}
			currentLine = currentLine.substring(currentLine.indexOf("*/")+2);
		}

	}
	// -------------------- end of removing comments method


	// -------------------- some setters and getters
	public int getCurrentLineNumber() {
		return lineNumber;
	}
	public String getCurrentLine() {
		return currentLine;
	}
	public String getCV() {
		return CV;
	}



	public static void main(String[] args) {

		Scanner scanner;
		try {
			scanner = new Scanner("Test_4.L");

			int tokenNumber = 0;
			try {
				while( !scanner.getCV().equals("$")  ) {
					System.out.println(tokenNumber + " : " + scanner.NextToken() + "\t" + scanner.getCV());
					tokenNumber++;
					//System.out.println(scanner.getCurrentLine());
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		} catch (Exception e1) {
			e1.printStackTrace();
		}


	}


}
