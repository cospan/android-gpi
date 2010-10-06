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

import dlcm.Structure;
import dlcm.StructureListener;
import dlcm.StructureParser;
import dlcm.dlcm.DLCM;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;

/** StructureManager.java
 *
 * Manages all DLCM structures
 *
 * This class allows programs to add, delete, query structures
 * 
 */
public class StructureManager {
    public HashMap <String, Structure> StructureMap;
    public ArrayList <DLCM> ChannelManagerList;
    public ArrayList <StructureListener> StructureListeners;
    public long StructureManagerID;

    //Constructor
    public StructureManager(){
        StructureMap = new HashMap<String, Structure>();
        ChannelManagerList = new ArrayList<DLCM>();
        StructureListeners = new ArrayList<StructureListener>();
        StructureManagerID = new Random().nextLong();
    }

    //StructureListener
    /**
     * Adds a listener to receive callbacks when something changes within the structure
     * @param listener
     * @see dlcm.StructureListener
     */
    public void addStructureListener(StructureListener listener){
        StructureListeners.add(listener);
    }
    /**
     * Remove a listener
     * @param listener
     * @see dlcm.StructureListener
     */
    public void removeStructureListner(StructureListener listener){
        if (StructureListeners.contains(listener)){
            StructureListeners.remove(listener);
        }
    }
    /**
     * Removes all listeners
     * @see dlcm.StructureListener
     */
    public void removeAllStructureListener(){
        StructureListeners.clear();
    }
    /**
     * Notify all listeners when soemthing will change
     * @see dlcm.StructureListener
     */
    public void structureChangingNotify(){
        for (StructureListener listener : StructureListeners){
            listener.structureChanging();
        }
    }
    /**
     * Notify all listeners that something changed
     * @see dlcm.StructureListener
     */
    public void structureChangedNotify(){
        for (StructureListener listener : StructureListeners){
            listener.structureChanged();
        }

    }

    //Cleanup
    /**
     * Reset the Structure Manager essentially removing all structures
     */
    public void resetStructures(){
        structureChangingNotify();
        //Go through each of the ChannelMangers, and tell them to reset as well
        for (DLCM channelManager : ChannelManagerList){
            channelManager.ResetManager();
        }
        StructureMap.clear();
        structureChangedNotify();
    }

    /**
     * Each structure manager needs a Board Identification number to separate it out from other StructureManager
     * Board identification numbers allow multiple LCM/DLCM/Publisher/Subscribers to share the same StructureManager
     * @return the StructureManager specific board identification number
     */
    public long getBoardID(){
        return StructureManagerID;
    }
    /**
     *
     * @param structureName
     * @return true if the StructureManager contains the structure with the name
     */
    public boolean containsStructure(String structureName){
        return StructureMap.keySet().contains(structureName);
    }
    /**
     * gets the structure associated with the name, if StructureManager doesn't contain
     * the structure associated with the name, then null is returned
     *
     * @param structureName
     * @return Structure if exists, otherwise null
     */
    public Structure getStructure(String structureName){
        if (containsStructure(structureName)){
            return StructureMap.get(structureName);
        }
        return null;
    }
    /**
     * Add a structure to the structure manager
     *
     * @param structure
     */
    public void addStructure(Structure structure){
        structureChangingNotify();
        if (StructureMap.keySet().contains(structure.getStructureName())){
            StructureMap.remove(structure.getStructureName());
        }
        StructureMap.put(structure.getStructureName(), structure);
        structureChangedNotify();
    }
    /**
     * Remove a structure from the structure manager
     *
     * @param structureName
     */
    public void removeStructure(String structureName){
        structureChangingNotify();
        for (DLCM channelManager : ChannelManagerList){
            channelManager.RemoveStructure(structureName);
        }

        if (StructureMap.keySet().contains(structureName)){
            StructureMap.remove(structureName);
        }
        structureChangedNotify();
    }
    //TODO add this function whenever an channel is destroyed, on closing
    /**
     * Removes a channel manager (DLCM) from the structure manager
     * @param channelManager
     */
    public void removeChannelManager(DLCM channelManager){
        if (ChannelManagerList.contains(channelManager)){
            ChannelManagerList.remove(channelManager);
        }
    }

    /**
     * Parse a structure string, a structure string is essentially a .lcm file
     *
     * @param parseString
     * @throws Exception
     */
    public void parseString(String parseString) throws Exception{
        StructureParser paser = new StructureParser(this, parseString);
        int res = 0;
        do {
            res = paser.ParseEntity();
        }while (res > -1);
        structureChangedNotify();
    }

    /**
     * return a string representation of all the structures in the StructureManager
     * @return string which is equivalent to an .lcm file
     * @throws IOException
     */
    public String getLCMString() throws IOException{
        LCMStringGenerator stringGenerator = new LCMStringGenerator(this);
        String generatedString = stringGenerator.generateLCMString();
        return generatedString;

    }
    /**
     * Generate C code for LCM, not yet implemented
     */
    public void generateC(){

    }
    /**
     * Generate Java code for LCM, not yet implemented
     */
    public void generateJava(){

    }
    /**
     * Generate Python code for LCM, not yet implemented
     */
    public void generatePython(){
        
    }
}
