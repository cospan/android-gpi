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

/* Structure.java
 *
 * Equivalent to a LCM structure, with dynamic components
 *
 */
package dlcm;

import dlcm.dlcm.DLCM;
import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import lcm.lcm.LCMEncodable;

/**
 *
 * @author David McCoy
 *
 * Runtime generated DLCM structure
 *
 */
public class Structure implements LCMEncodable {
	
	
	//Map/Array of members
	HashMap<String, Member> MemberMap;
	ArrayList<Member> MemberList;	//this is needed for hash reasons (order is important)
	DLCM mDLCM; //this should only be used for initialization

	//Names
	String mShortName;
	String mPackage;
	String mStructureName;
	String mLCMTypeName;
	
	//Hash
	public long LCM_FINGERPRINT;
	public long LCM_FINGERPRINT_BASE;
	
	//Flags
	boolean mStructureFlag;
	boolean mEnumeratorFlag;
	
	//Constructor/Initializer
        /**
         * No parameter constructure is not generally called
         */
        public Structure(){
            MemberMap = new HashMap<String, Member>();
            MemberList = new ArrayList<Member>();
	}
        /**
         * Constructor that is called
         *
         * @param name Name of the structure
         */
        public Structure(String name){
            this();
            mStructureName = new String (name);
	}
        /**
         * Initializes Structure, and initializes all members that this structure host
         *
         * @param dlcm DLCM instance hosting the structure
         *
         * @throws IOException
         * @throws ClassNotFoundException
         * @throws InstantiationException
         * @throws IllegalAccessException
         */
        public void initialize(DLCM dlcm) throws IOException, ClassNotFoundException, InstantiationException, IllegalAccessException{
            mDLCM = dlcm;
            for (Member member : MemberList){
                member.setParent(this);
                member.initialize();
            }
            LCM_FINGERPRINT = ((LCM_FINGERPRINT_BASE << 1) + ((LCM_FINGERPRINT_BASE >> 63) & 1));
	}
	
        //Getters Setters
        /**
         *
         * @return an ArrayList of Members
         */
        public ArrayList<Member> getMemberList(){
            return MemberList;
        }
        /**
         * Get a Member by name
         * @param memberName
         * @return a Member with the given name
         */
        public Member getMember(String memberName){
            return MemberMap.get(memberName);
        }

	//Flags
	void setStructure(boolean flag){
		mStructureFlag = flag;
	}
	void setEnumerator(boolean flag){
		mEnumeratorFlag = flag;
	}
	
        /**
         * 
         * @return true if Structure is a structure, and not an Enumeration
         */

	public boolean isStructure(){
		return mStructureFlag;
	}
        /**
         *
         * @return true if Structure is an Enumeration, and not a structure
         */
	public boolean isEnumerator(){
		return mEnumeratorFlag;
	}
	
	//Names
        /**
         * Not fully qualified name
         * @param string Unqualified name
         */
        public void setShortName(String string){
		mShortName = new String (string);
	}
        /**
         *
         * @param string Name of the structure
         */
        public void setStructureName(String string){
		mStructureName = new String (string);
	}
        /**
         *
         * @param string Name of the package for this structure
         */
        public void setPackage(String string){
		mPackage = new String (string);
	}
        /**
         *
         * @param string Fully qualified name of structure
         */
        public void setLCMTypeName(String string){
		mLCMTypeName = new String (string);
	}
	
        /**
         *
         * @return Unqualified short name
         */
        public String getShortName(){
		return mShortName;
	}
        /**
         *
         * @return get Name that DLCM will use to interact with this structure
         */
        public String getStructureName(){
		return mStructureName;
	}
        /**
         *
         * @return the package associated with this structure
         */
        public String getPackage(){
		return mPackage;
	}
        /**
         *
         * @return the fully qualified name
         */
        public String getLCMTypeName(){
		return mLCMTypeName;
	}
	
	//Overrides
	@Override
	public Object clone(){
		Structure s = new Structure();
		
		//Strings
		if (mShortName != null) 	s.mShortName        = new String    (mShortName);
		if (mPackage != null) 		s.mPackage          = new String    (mPackage);
		if (mStructureName != null)     s.mStructureName    = new String    (mStructureName);
		if (mLCMTypeName != null) 	s.mLCMTypeName      = new String    (mLCMTypeName);
		
		//Hash Values
		s.LCM_FINGERPRINT_BASE = LCM_FINGERPRINT_BASE;
		s.LCM_FINGERPRINT = LCM_FINGERPRINT;
		
		//Flags
		s.mStructureFlag = mStructureFlag;
		s.mEnumeratorFlag = mEnumeratorFlag;
		
		//Need to copy over the members one by one so were not passing reference
		//Is there a better way to do this?
		s.MemberList = new ArrayList<Member>();
		s.MemberMap = new HashMap<String, Member>();
		for (Member member : MemberList){
			Member m = (Member) member.clone();
			s.MemberList.add(m);
			s.MemberMap.put(m.Name, m);
			m.setParent(s);
		}
		return s;
	}

	//encode
        /**
         * encode data for LCM transmission
         * @param outs bytes of encoded data
         * @throws IOException
         */
        public void encode(DataOutput outs) throws IOException {
		//Send Hash
		outs.writeLong(LCM_FINGERPRINT);
		
		//Go through each member, and encode recursively
		for (Member member : MemberList){
			member.encode(outs);
		}
	}
        /**
         * decode data from an LCM transmission
         * @param ins bytes of data to decode
         * @throws IOException
         * @throws ClassNotFoundException
         */
        public void decode(DataInput ins) throws IOException, ClassNotFoundException {
		//Verify finger print
		if (ins.readLong() != LCM_FINGERPRINT){
			throw new IOException ("LCM Decode error: bad fingerprint");
		}
		
		for (Member member : MemberList){
			member.decode(ins);
		}
	}

        /**
         * Not used in this version
         * @param d
         * @throws IOException
         */
        public void _encodeRecursive(DataOutput d) throws IOException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    /**
     * Not used in this version
     * @param di
     * @throws IOException
     */
    public void _decodeRecursive(DataInput di) throws IOException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

}
