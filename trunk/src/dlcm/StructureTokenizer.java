/* Author: David McCoy dave.mccoy@cospandesign.com
 *
 *     This file is part of DLCM.
 *
 *  DLCM is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU Lesser General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  DLCM is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU Lesser General Public License for more details.
 *
 *  You should have received a copy of the GNU Lesser General Public License
 *  along with DLCM.  If not, see <http://www.gnu.org/licenses/>.
 *
 */

/* StructureTokenizer.java
 *
 * Lowest level of the parser. This is a conversion of the "tokenize" c
 * implmenation written for lcmgen. The creators include:
 *
 * edwinolson (Sorry I tried to find the name)
 * Albert Huang
 * David Moore
 */
package dlcm;

/**
 *
 * @author David McCoy ported from lcmgen from lcm.googlecode.com
 *
 * Tokenizes a LCM structure text
 */
public class StructureTokenizer {
	
	//Constants
	static final int MAX_TOKEN_LENGTH = 1024;
	
	static final String SINGLE_CHAR_TOKENS = "();\",;\'[]";
	static final String OP_CHARS = "!~<>=&|^%*+=/";
	
	String mToken;
	String mData;
	String mCurrentData;
	
	//Flags
	boolean mHasNext;
	
	//Unget
	int mUngetChar;
	
	//Constructor
        /**
         *
         */
        public StructureTokenizer(){
		//mToken = new char[MAX_TOKEN_LENGTH];
		mToken = new String("");
		
		mHasNext = false;
		mUngetChar = -1;
	}
        /**
         *
         * @param incommingData constructs a structure with incommingData as input
         */
        public StructureTokenizer(String incommingData){
		//incommingData = string to tokenize
		this();
		mData = incommingData;
		mCurrentData = new String (incommingData);
	}
	
	//Return a string representation of a token
        /**
         *
         * @return s string representation of a token
         */
        public String getToken(){
		mToken = mToken.trim();
		return mToken;
	}
        /**
         *
         * @return an integer token
         */
        public int tokenizeNext(){
		int res = 0;
		
		//Previous token is stored
		if (mHasNext){
			mHasNext = false;
			return 0;
		}
		
		//Loop till all data is retrieved
		while (true){
			
			res = tokenizeInternal();
			
			//Check if there is an error, or end of data
			if (res < 0){
				return -1;
			}
			
			//Check for block comments
			if (mToken.contains("/*")){
				if (mCurrentData.indexOf("*/") == -1){
					return -10;
				}
				mCurrentData = mCurrentData.substring(mCurrentData.indexOf("*/"));
				if (mCurrentData.length() == "*/".length()){
					return -1;
				}
				mCurrentData = mCurrentData.substring("*/".length());
				continue;				
			}
			
			//Check for end of line comments
			else if (mToken.contains("//")){
				//get rid of the line
				flushLine();
			}
			else{
				break;
			}
		}
		return res;
		
	}
        /**
         * Peek next token
         *
         * @return return the next token, but don't remove it
         */
        public int tokenizePeek(){
		int res = 0;
		if (mHasNext){
			return res;
		}
		res = tokenizeNext();
		mHasNext = true;
		return res;
	}
	
	//Internal
	private int tokenizeInternal(){
		int c = 0;
		
		mToken = "";
		
		//remove white spaces
		mCurrentData = mCurrentData.trim();
		
		//Have we reached the end?
		if (mCurrentData.length() == 0){
			return -1;
		}
		
		c = tokenizeNextCharacter();
		
		//is character literal?
		if (c == '\''){
			mToken += (char) c;
			c = tokenizeNextCharacter();
			
			if (c == '\\'){
				c = unescape((char)tokenizeNextCharacter());
			}
			
			if (mCurrentData.length() == 0){
				return -4;
			}
			
			mToken += (char) c;
			c = tokenizeNextCharacter();
			return mToken.length();
		}
		
		//is string literal?
		if (c == '\"'){
			boolean escape = false;
			
			//Add the initial quote
			mToken += (char) c;
			
			//Keep reading until the quote closes
			while (true){
				if (mCurrentData.length() == 0){
					return -4;
				}
				if (mToken.length() >= MAX_TOKEN_LENGTH){
					return -2;
				}
				
				c = tokenizeNextCharacter();
				
				if (escape){
					escape = false;
					c = unescape((char) c);
					continue;
				}
				
				if (c == '\"'){
					mToken += (char) c;
					return mToken.length();
				}
				
				if (c == '\\'){
					escape = true;
					continue;
				}
				
				mToken += (char) c;
			}
		}
		
		//is an operator?
		if (OP_CHARS.indexOf((char) c) != -1){
			while (OP_CHARS.indexOf((char) c) != -1){
				if (mToken.length() >= MAX_TOKEN_LENGTH){
					return -2;
				}
				mToken += (char) c;
				c = tokenizeNextCharacter();
			}
			tokenizeUngetChar((char) c);
			return mToken.length();
		}
		
		//AlphaNumeric blobs of data
		while (true){
			//If the token is too large
			if (mToken.length() >= MAX_TOKEN_LENGTH){
				return -2;
			}
			
			mToken += (char) c;
			
			//see if they are the special characters
			if (SINGLE_CHAR_TOKENS.indexOf(c) != -1){
				return mToken.length();
			}
			if (mCurrentData.length() == 0){
				break;
			}
			
			c = tokenizeNextCharacter();
			
			//see if the next char is a special character, or an op char
			if ((SINGLE_CHAR_TOKENS.indexOf((char) c) != -1) || (OP_CHARS.indexOf((char)c)) != -1){
				tokenizeUngetChar((char) c);
				return mToken.length();
			}
			
			//check for end of line, or white space
			if ((mCurrentData.length() == 0) || (((char) c) == '\n')){
				break;
			}
			
			if (mCurrentData.substring(0, 1).matches("[ \t\n\f\r]")){
				mToken += (char) c;
				break;
			}
		}
		
		return mToken.length();
	}
	private int tokenizeNextCharacter(){
		
		int c = 0;
		//Check if there is an character we has previously saved
		if (mUngetChar >= 0){
			c = mUngetChar;
			mUngetChar = -1;
			return c;
		}
		
		//Get a character from the input file
		//Read the next line if done with this one
		c = mCurrentData.charAt(0);
		mCurrentData = mCurrentData.substring(1);
				
		return c;
	}
	private int unescape(char c){
		switch (c){
		case 'n':
			return 10;
		case 'r':
			return 3;
		case 't':
			return 9;
		}
		return c;
	}
	private int tokenizeUngetChar(char c){
		mUngetChar = c;
		return 0;
	}
	private void flushLine(){
		//Eat white space
		//find the next occurance of /n, and go to the next line
		//if we reach the end of the file exit
		if (mCurrentData.indexOf('\n') > mCurrentData.length()){
			mCurrentData = "";
			return;
		}
		
		mCurrentData = mCurrentData.substring(mCurrentData.indexOf('\n') + 1);
		
		//handle windows based carriage return
		if (mCurrentData.charAt(0) == '\r'){
			if (mCurrentData.length() > 1){
				mCurrentData = mCurrentData.substring(1);
			}
			else{
				mCurrentData = "";
			}
		}
		
		//remove white spaces
		mCurrentData = mCurrentData.trim();
	}
}
