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



package dlcm.builder;

import dlcm.Member;
import dlcm.Structure;
import java.io.IOException;

/** LCMStringGenerator.java
 *
 * Manages all DLCM structures
 *
 * Traverses all Dynamic structures, and creates a String representation, essentially this will
 * look like a user defined .lcm file, which {@link dlcm.builder.StructureManager} 
 * uses to generate dynamic structures
 *
 */
public class LCMStringGenerator {
    StructureManager mStructureManager;

    /**
     * Create a StringGenerator
     * @param structureManager Holds all the structures to stringafy
     */
    public LCMStringGenerator(StructureManager structureManager){
        mStructureManager = structureManager;
    }

    /**
     * Generates a string representation to be sent to remote publisher/subscriber
     * @return String representation of lcm structures
     * @throws IOException
     */
    public String generateLCMString() throws IOException{
        String lcmString = "";

        //lcmString += "//Auto Generated .lcm File\n\n";
        lcmString += "\n\n";

        //for each of the structures within StructureManager
        for (String structureName : mStructureManager.StructureMap.keySet()){
            //add the structure name
            Structure structure = mStructureManager.StructureMap.get(structureName);
            lcmString += generateStructureString(structure) + "\n\n";
        }
        return lcmString;
    }

    private String generateStructureString(Structure structure) throws IOException{
        String structureLCMString  = "";

        structureLCMString += "struct " + structure.getStructureName() + "\n";
        structureLCMString += "{\n";

        //go through the members of the structure and generate a string for each
        for (Member member : structure.getMemberList()){
            structureLCMString += generateMemberString(member) + "\n";
        }
        structureLCMString += "}";
        
        return structureLCMString;
    }
    private String generateMemberString(Member member) throws IOException{
        String memberLCMString = "";
        memberLCMString += "    ";

        if (member.isConstant()){
            memberLCMString += "const ";
        }
        memberLCMString += member.Type + " ";
        memberLCMString += member.Name;

        //check for arrays
        if (member.Dimensions > 0){
            for (int i = 0; i < member.Dimensions ; i++){
                memberLCMString += "[";
                if (member.isConstantDimension(i)){
                    memberLCMString += (new Long(member.getDimensionsSize(i))).toString();
                }
                else {
                    memberLCMString += member.getVariableDimensionName(i);
                }
                memberLCMString += "]";
            }
        }
        if (member.isConstant()){
            memberLCMString += " = " + member.getData().toString();
        }
        memberLCMString += ";";
        return memberLCMString;
    }
}
