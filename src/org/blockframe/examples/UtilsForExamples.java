package org.blockframe.examples;

import java.io.File;
import java.util.Random;

import org.blockframe.blocks.StringBlock;
import org.blockframe.core.Block;


/**
 * Utility methods and objects used in this package. 
 */
public class UtilsForExamples {
	
	static final Random random = new Random(); 
	
	/**
	 * Returns the folder the example PDFs will be written to. 
	 */
	public static File getExamplesDir() {
		String homeDirPath = System.getProperty("user.home"); 
		File homeDir = new File(homeDirPath); 
		File examplesDir = new File(homeDir, "BlockFrame examples"); 
		examplesDir.mkdirs();
		return examplesDir;
	}
	
	/**
	 * Returns an array of {@linkplain StringBlock}, holding text "one", "two", etc. 
	 */
	public static StringBlock[] getNumberBlocks(int top) { 
		StringBlock[] blocks = new StringBlock[top] ; 
		for (int i=0 ; i<top ; i++) blocks[i] = new StringBlock(number(i+1)); 
		return blocks ; 
	}
	
	/** Array of numbers zero to twenty. */
	public static String[] unitNumbers = new String[] {
		"zero", "one", "two", "three", "four", "five", "six", "seven", "eight", "nine", 
		"ten", "eleven", "twelve", "thirteen", "fourteen", "fifteen", "sixteen", "seventeen", "eighteen", "nineteen",
		"twenty"}; 
	
	/** Array of tens numbers - null, ten, twenty, ..., ninety. */
	public static String[] tensNumbers = new String[] {
		null, "ten", "twenty", "thirty", "forty", "fifty", "sixty", "seventy", "eighty", "ninety" 
	}; 
	
	/**
	 * Returns text for numbers 0 to 999, in lower-case. That is: zero, one, two, etc.  
	 */
	public static String number(int number) { 
		if (number < 0) { 
			throw new UnsupportedOperationException("Cannot compute text for number "+number); 
		} else if (number<unitNumbers.length) { 
			return unitNumbers[number] ; 
		} else { 
			StringBuffer sb = new StringBuffer(); 
			int unitsDigit = number % 10 ; 
			int tens = number / 10 ; 
			int tensDigit = tens % 10 ; 
			int hundreds = tens / 10 ; 
			int hundredsDigit = hundreds % 10 ; 
			if (hundreds>=10) { 
				throw new UnsupportedOperationException("Cannot compute text for number "+number); 
			}
			if (hundredsDigit>0) { 
				sb.append(unitNumbers[hundredsDigit]); 
				sb.append(" hundred"); 
				if (unitsDigit>0 || tensDigit>0) sb.append(" and"); 
			}
			if (tensDigit<=1) { 
				int mod100 = number%100;
				if (mod100>0) { 
					if (hundreds>0) sb.append(" "); 
					sb.append(unitNumbers[mod100]); 
				}
			} else { 
				if (tensDigit>0) { 
					if (hundreds>0) sb.append(" "); 
					sb.append(tensNumbers[tensDigit]); 
				}
				if (unitsDigit>0) { 
					if (tens>0) sb.append(" "); 
					sb.append(unitNumbers[unitsDigit]); 
				}
			}
			return sb.toString(); 
		}
	}

}
