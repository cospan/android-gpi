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

/* StructureParser.java
 *
 * Parses lcm files and sets up DLCM structures.
 *
 * This work is primarily based off of lcmgen originally written for LCM
 * 
 * the Creators of it include:
 *
 * edwinolson (Sorry I tried to find the name)
 * Albert Huang
 * David Moore
 */

package dlcm;

import dlcm.builder.StructureManager;
import java.util.ArrayList;

public class StructureParser {
	
	//Constants
	private final static String[] PRIMITIVE_TYPES = {
		"int8_t",
		"int16_t",
		"int32_t",
		"int64_t",
		"float",
		"double",
		"string",
		"byte",
		"boolean"
	};
	private final static String[] ARRAY_DIMENSION_TYPES = {
                "byte",
		"int8_t",
		"int16_t",
		"int32_t",
		"int64_t"
	};
	private final static String[] CONSTANT_TYPES = {
                "byte",
		"int8_t",
		"int16_t",
		"int32_t",
		"int64_t",
		"float",
		"double"
	};
	
	
	StructureTokenizer mTokenizer;
	String mParseString;
	StructureManager mStructureManager;
	Structure mStructure;
        String mPackage;

	//Constructor/initialize
	public StructureParser () {
		mPackage = new String("");
	}
	public StructureParser (StructureManager structureManager, String InputString) {
		this();
		mStructureManager = structureManager;
		mParseString = new String(InputString);
		mTokenizer = new StructureTokenizer(InputString);
	}
	public int Initialize (StructureManager dlcm, String InputString){
		mStructureManager = dlcm;
		mParseString = new String(InputString);
		mTokenizer = new StructureTokenizer(InputString);
		return 0;
	}

	//Parsing Functions
	public int ParseEntity() throws Exception{
		int res = 0;
		
		res = mTokenizer.tokenizeNext();
		
		//Check if were done
		if (res == -1){
			return -1;
		}
		//Check for errors
		if (res < -1){
			return -2;
		}
		
		//Not Done
		String token = mTokenizer.getToken();
		
		//Parse Package
		if (token.matches("package")){
			TokenizeNextOrFail("Package Name");
                        mStructure.setPackage(mTokenizer.getToken());
                        this.ParseRequire(";");
			return 0;
		}
		//Parse structure
		if (token.matches("struct")){
			ParseStructure();
			return 0;
		}
		//Parse Enumeration
		if (token.matches("enum")){
			ParseEnumeration();
			return 0;
		}
		//Parse Union
		if (token.matches("union")){
			//not implemnted yet
			return 0;
		}
		
		throw new Exception ("Missing struct/enum/union token");		
	}
	private void ParseStructure() throws Exception{
		String name;
		LcmTypeName lcName;
                TokenizeNextOrFail ("Structure Name Not Found");
		name = mTokenizer.getToken();
                lcName = this.ParseTypeName(name);

                CreateStructure(name);

                mStructure.setLCMTypeName(lcName.FullTypeName);
                mStructure.setPackage(lcName.Package);
                mStructure.setShortName(lcName.ShortName);

		ParseRequire("{");
		while (!ParseTryConsume("}")){
			ParseMember();
		}
		HashStructure();
	}
	private void ParseMember() throws Exception{
		
		//Check for errors and constants
		if (ParseTryConsume("struct")){
			throw new Exception ("Recursive structures not implemented");
		}
		else if (ParseTryConsume("enum")){
			throw new Exception ("Recursive enums not implemented");
		}
		else if (ParseTryConsume("union")){
			throw new Exception ("Recursive unions not implemented");
		}
		else if (ParseTryConsume("const")){
			ParseConstant();
			return;
		}
		
		//get the type
		TokenizeNextOrFail ("Member Type Unknown");
		String type = mTokenizer.getToken();

                LcmTypeName lcName = this.ParseTypeName(type);

		if (!type.substring(0, 1).matches("[a-zA-Z_]")){
			throw new Exception ("Invalid Member type: must start with [a-zA-Z_]");
		}
		
		//Consume all members on the line
		while (true){
			//Get the name
			TokenizeNextOrFail("Member Name");
			
			if (!isLegalMember(mTokenizer.getToken())){
				throw new Exception ("Invalid Member Name: must start with [a-zA-Z_]");
			}
			
			//make sure this name isn't already taken
			String name = mTokenizer.getToken();
			if (FindMember(name) != null){
				throw new Exception ("Duplicate Member name " + name);
			}
			
			//Create new member
			Member member = new Member(mStructure);

                        member.mLCMTypeName = lcName.FullTypeName;
                        member.mPackage = lcName.Package;
                        member.mShortName = lcName.ShortName;
			
			ArrayList<Boolean> constDimensions = new ArrayList<Boolean>();
			ArrayList<Integer> dimensionsConstantSize = new ArrayList<Integer>();
			ArrayList<String> dimensionsVariableSize = new ArrayList<String>();
			int dimensions = 0;
			
			while (ParseTryConsume("[")){
				TokenizeNextOrFail ("Array Size");
				//dimensions = 0;
				
				if (mTokenizer.getToken().substring(0, 1).matches("[0-9]")){
					dimensions++;
					constDimensions.add(true);
					dimensionsConstantSize.add(Integer.parseInt(mTokenizer.getToken()));
					dimensionsVariableSize.add(mTokenizer.getToken());
				}
				//Variable Array Size
				else{
					if (mTokenizer.getToken().substring(0, 1).matches("]")){
						throw new Exception ("Array sizes must be declared constant or variable");
					}
					if (!isLegalMember(mTokenizer.getToken())){
						throw new Exception ("Invalid array size name, must start with a [a-zA-Z_]");
					}
					//look for the variable name in the previous entries
					if (FindMember(mTokenizer.getToken()) == null){
						throw new Exception ("Variable name must be declared before Array");
					}
					if (!isArrayDimension(FindMember(mTokenizer.getToken()).Type)){
						throw new Exception ("Variable must be a valid integer type");
					}
					dimensions++;
					constDimensions.add(false);
					dimensionsConstantSize.add(0);
					dimensionsVariableSize.add(mTokenizer.getToken());
				}
				ParseRequire("]");
			}
			member.ConstDimensions = new Boolean[constDimensions.size()];
			member.DimensionsConstantSize = new Integer[dimensionsConstantSize.size()];
			member.DimensionsVariableSize = new String[dimensionsVariableSize.size()];
			
			//Add the array data
			if (dimensions > 0){
                                constDimensions.toArray(member.ConstDimensions);
                                dimensionsConstantSize.toArray(member.DimensionsConstantSize);
                                dimensionsVariableSize.toArray(member.DimensionsVariableSize);
				//member.ConstDimensions = (Boolean[]) constDimensions.toArray();
				//member.DimensionsConstantSize = (Integer[]) dimensionsConstantSize.toArray();
				//member.DimensionsVariableSize = (String[]) dimensionsVariableSize.toArray();
                                member.Dimensions = dimensions;
			}
			
			mStructure.MemberMap.put(name, member);
			mStructure.MemberList.add(member);
			
			member.setName(name);
			member.setType(type);
			
			if (!ParseTryConsume(",")){
				break;
			}
		}
		ParseRequire(";");
	}
	private void ParseConstant() throws Exception{
		//Check if we got the Constants type
		TokenizeNextOrFail ("Constant Type");
		
		//get the constant type
		if (!isLegalConstant(mTokenizer.getToken())){
			throw new Exception ("Invalid type for constant");
		}
		String type = mTokenizer.getToken();
		
		while (true){
			
			//Consume all constants that are on the line
			
			Member member = null;
			
			TokenizeNextOrFail("Name Identifier");
			
			if (!isLegalMember(mTokenizer.getToken())){
				throw new Exception ("Semantic Error: Name must start with [a-zA-Z_]");
			}
			
			String name = new String(mTokenizer.getToken());
			
			if (mStructure.MemberMap.containsKey(name)){
				throw new Exception ("Duplicate Constant names: " + name);
			}
			
			ParseRequire("=");
			
			TokenizeNextOrFail("Constant Value");
			
			//Create a new Constant member
			member = new Member (mStructure);
			
			if (type.matches("int8_t")){
				member.ConstantData = Byte.parseByte(mTokenizer.getToken());
				member.Data = Byte.parseByte(mTokenizer.getToken());
			}
                        else if (type.matches("int16_t")){
				member.Data = Short.parseShort(mTokenizer.getToken());
				member.ConstantData = Short.parseShort(mTokenizer.getToken());
			}
                        else if (type.matches("int32_t")){
				member.Data = Integer.parseInt(mTokenizer.getToken());
				member.ConstantData = Integer.parseInt(mTokenizer.getToken());
			}
                        else if (type.matches("int64_t")){
				member.Data = Long.parseLong(mTokenizer.getToken());
				member.ConstantData = Long.parseLong(mTokenizer.getToken());
			}
                        else if (type.matches("float")){
				member.Data = Float.parseFloat(mTokenizer.getToken());
				member.ConstantData = Float.parseFloat(mTokenizer.getToken());
			}
                        else if (type.matches("double")){
				member.Data = Double.parseDouble(mTokenizer.getToken());
				member.ConstantData = Double.parseDouble(mTokenizer.getToken());
			}
			else {
				throw new Exception ("Unable to parse constant member");
			}
			member.Name = new String (name);
			member.Type = new String(type);
			member.Constant = true;
			mStructure.MemberMap.put(name, member);
			mStructure.MemberList.add(member);
			if (!ParseTryConsume(",")){
				break;
			}
		}
		ParseRequire(";");
	}
	private void ParseEnumeration() throws Exception{
		String name;
		TokenizeNextOrFail("Enumerator Name");
		name = new String (mTokenizer.getToken());
		CreateEnumeration(name);
		ParseRequire("{");
		
		while (!ParseTryConsume ("}")){
			ParseEnumerationValue();
			ParseTryConsume(",");
			ParseTryConsume(";");
		}
		HashEnumeration();
	}
	private void ParseEnumerationValue() throws Exception{
		//Look for the name of the member
		TokenizeNextOrFail ("Enumeration Member Name");
		
		String name = new String (mTokenizer.getToken());
		Member member = new Member (mStructure);
		member.Type = new String ("uint32_t");
		
		//Specified an enumeration value
		if (ParseTryConsume ("==")){
			TokenizeNextOrFail ("Enumeration Value Literal");
			member.Data = new Integer (Integer.parseInt(mTokenizer.getToken()));
		}
		//didn't specify value
		else {
			int max = 0;
			for (Member m : mStructure.MemberList){
				max = (Integer) m.Data;
			}
			max++;
			member.Data = new Integer(max);
		}
		
		for (Member m : mStructure.MemberList){
			if (((Integer)m.Data) == ((Integer)member.Data)){
				throw new Exception ("Semantic Error: " + m.Name + " and " + name + " have the same value: " + ((Integer)m.Data));
			}
		}
		if (mStructure.MemberMap.keySet().contains(name)){
			throw new Exception ("Semantic Error: " + name + " declared twice");
		}
		
		mStructure.MemberMap.put(name, member);
		mStructure.MemberList.add(member);

	}
	private LcmTypeName ParseTypeName (String lcTypeName){
            //lctypename contains
                //lctypename = fullyqualified name e.g., "com.cospandesing.project1.control_t"
                //package = "com.cospandesing.project1"
                //shortname = "control_t"
            //the entire incomming string is lctypename
            LcmTypeName lcName = new LcmTypeName();

            lcName.FullTypeName = lcTypeName;

            //check to see if there is any "." spliting the line
            String strings[] = lcTypeName.split("\\.");
            if (strings.length == 0){
                //no sub types
                lcName.ShortName = lcTypeName;
                if (isPrimitive(lcTypeName)){
                    lcName.Package = "";
                }
                else{
                    //override the package name, with the last general one
                    lcName.Package = mPackage;
                    if (mPackage.length() > 0){
                        lcName.FullTypeName = lcName.Package + "." + lcName.ShortName;
                    }
                }
            }
            lcName.Package = strings[0];
            for (int i = 0; i < strings.length - 1; i++){
                lcName.Package += "." + strings[i];
            }
            lcName.ShortName = strings[strings.length - 1];

            return lcName;
        }

	//Parser Helper Functions
	private boolean ParseTryConsume (String token) throws Exception{
		int res = mTokenizer.tokenizePeek();
		boolean retValue = false;
		
		//Error
		if (res < 0){
			throw new Exception ("End of file found while looking for " + token);
		}
		
		if (token.compareTo(mTokenizer.getToken()) == 0){
			retValue = true;
		}
		if (retValue){
			mTokenizer.tokenizeNext();
		}
		return retValue;		
	}
	private void ParseRequire (String token) throws Exception{
		int res = mTokenizer.tokenizeNext();
		if ((res < 0) || (token.compareTo(mTokenizer.getToken()) != 0)){
			throw new Exception ("Structure Parse Could not find the needed token " + token);
		}
	}
	private void TokenizeNextOrFail(String description) throws Exception{
		int res = mTokenizer.tokenizeNext();
		if (res < 0){
			throw new Exception(description);
		}
	}
	
	//Helper Functions
	private boolean StringInArray(String string, String[] stringArray){
		for (String s : stringArray){
			if (s.matches(string)){
				return true;
			}
		}
		return false;
	}
	private boolean isPrimitive (String string){
		return (StringInArray(string, PRIMITIVE_TYPES));
	}
	private boolean isMemberReference(String string){
		//Process of elimination
		return ((!StringInArray(string, PRIMITIVE_TYPES)) && !string.matches("string"));
	}
	private boolean isArrayDimension(String string){
		return StringInArray(string, ARRAY_DIMENSION_TYPES);
	}
	private boolean isLegalMember(String string){
		char firstChar = string.charAt(0);
		return String.valueOf(firstChar).matches("[a-zA-Z_]");
	}
	private boolean isLegalConstant(String string){
		return StringInArray(string, CONSTANT_TYPES);
	}
	private Member FindMember (String name){
		if (!mStructure.MemberMap.containsKey(name)){
			return null;
		}
		return mStructure.MemberMap.get(name);
	}

	//Hash Functions
	private long HashUpdate (long hash, char c){
		//Make the hash dependent on the value of the given charcter.
		//the order that HashUpdate is called in IS important
		hash = ((hash << 8) ^ (hash >> 55)) + c;
		return hash;
	}
	private long HashStringUpdate (long hash, String string){
		//hash is dependent on each character in the string
		hash = HashUpdate (hash, (char)string.length());
		
		for (int i = 0; i < string.length(); i++){
			hash = HashUpdate(hash, string.charAt(i));
		}
		return hash;
	}
	private int HashStructure (){
		long hash = 0x12345678;
		
		//go through all the data members, and hash them
		if (!mStructure.isStructure()){
			return -1;
		}
		
		//TODO: check to make sure this is in order... it should be
		for (Member member : mStructure.MemberList){
			//Hash the name
			hash = HashStringUpdate (hash, member.Name);
			
			if (isPrimitive(member.Type)){
				hash = HashStringUpdate (hash, member.Type);
			}
			
			//Hash the dimension information
			int dimensions = member.Dimensions;
			hash = HashUpdate(hash, (char)dimensions);
			for (int i = 0; i < dimensions; i++){
				hash = HashUpdate(hash, (char)member.getEnumeratorConstantDimensions(i));
				hash = HashStringUpdate(hash, member.getVariableDimensionName(i));
			}
		}
		mStructure.LCM_FINGERPRINT_BASE = hash;
		return 0;
	}
	private int HashEnumeration (){
		long hash = 0x87654321;
		if (!mStructure.isEnumerator()){
			return -1;
		}
		hash = HashStringUpdate (hash, mStructure.getShortName());
		mStructure.LCM_FINGERPRINT_BASE = hash;
		return 0;
	}

	//Create Functions
	private void CreateStructure(String name){
		mStructure = new Structure();
		//TODO: Make sure there is no channels that can be using this structure
			//or else there could be two similar structures with different properties
		//See if we are replacing a structure within the Structure Map
		if (mStructureManager.StructureMap.containsKey(name)){
			mStructureManager.removeStructure(name);
		}
		mStructureManager.StructureMap.put(name, mStructure);
		mStructure.setStructure(true);
		mStructure.setStructureName(name);
	}
	private void CreateEnumeration(String name){
		mStructure = new Structure();

                LcmTypeName lcName = null;
		//TODO: Make sure there is no channels that can be using this structure
		//or else there could be two similar structures with different properties
		//See if we are replacing a structure within the Structure Map
		if (mStructureManager.StructureMap.containsKey(name)){
			mStructureManager.StructureMap.remove(name);
		}

                lcName = this.ParseTypeName(name);

                mStructure.setLCMTypeName(lcName.FullTypeName);
                mStructure.setPackage(lcName.Package);
                mStructure.setShortName(lcName.ShortName);


		mStructureManager.StructureMap.put(name, mStructure);
		mStructure.setEnumerator(true);
	}

        //Inner Class
        public class LcmTypeName{
            public String FullTypeName;
            public String Package;
            public String ShortName;
            LcmTypeName(){
                FullTypeName = "";
                Package = "";
                ShortName = "";
            }
        }
}
