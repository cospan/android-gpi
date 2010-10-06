/* Author: David McCoy dave.mccoy@cospandesign.com
 *
 *     This file is part of Android GPI.
 *
 *  Android GPI is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  Android GPI is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with Android GPI.  If not, see <http://www.gnu.org/licenses/>.
 *
 */

package com.cospandesign.android.pubsub;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Set;

import dlcm.dlcm.DLCM;

public class PubSub {
    private DLCM mDLCM;
    public String mName;
    public Long mBoardId;
    //private HashMap<String, PubSubWidgetContainer> WidgetMap;
    ArrayList<String> mChannelNames;
    HashMap <String, Object> mStaticChannelMap;
    ArrayList<String> mUnparsedInstanceStrings;

    public PubSub(String name, Long boardId, DLCM dlcm){
        mName = new String(name);
        mBoardId = new Long(boardId);
        mDLCM = dlcm;
        mChannelNames = new ArrayList<String>();
        //WidgetMap = new HashMap<String, PubSubWidgetContainer>();
        mStaticChannelMap = new HashMap<String, Object>();
        mUnparsedInstanceStrings = new ArrayList<String>();
    }
/*
    public int getInstanceCount(){
        return WidgetMap.size();
    }
*/
    public boolean isSameBoard(long boardId){
        if (boardId == mBoardId){
            return true;
        }
        return false;
    }

    //Should only be used for local pubsubs
    public void AddChannelName(String channelName){
        mChannelNames.add(channelName);
    }

    //Should only be used for local pubsubs
    public void RemoveChannelName (String channelName){
        mChannelNames.remove(channelName);
    }

    public String GenerateRawInstanceString(){

        //go through the mDLCM

        String rawInstanceString = new String("# " + mName + " Instance Strings\n");
        for (String string : mChannelNames){
            String raw = mDLCM.GenerateChannelString(string);
            if (raw != null){
                rawInstanceString += raw;
            }
        }
        return rawInstanceString;
    }

    public void ParseRawInputString(String rawInstanceString) throws Exception{
        String[] channelNames = mDLCM.ParseChannel(rawInstanceString);
        mChannelNames.clear();
        for (String channelName : channelNames){
            mChannelNames.add(channelName);
        }
    }
    public void AddUnparsedString(String rawInstanceString){
        this.mUnparsedInstanceStrings.add(rawInstanceString);
    }
    public void RemoveUnparsedString(String rawInstanceString){
        mUnparsedInstanceStrings.remove(rawInstanceString);
    }
    public void ClearUnparsedStrings(){
        mUnparsedInstanceStrings.clear();
    }
    public ArrayList<String> getUnparsedInstanceStrings (){
        return mUnparsedInstanceStrings;
    }

    public boolean ContainsChannelName(String channelName){
        return mChannelNames.contains(channelName);
    }
    public String[] GetChannelNames (){
        String [] names = new String [mChannelNames.size()];
        mChannelNames.toArray((String[])names);
        return names;

    }
/*
    public void ShowWidgets(String channelName){
        WidgetMap.get(channelName).setVisible(true);
    }
*/
    public void AddStaticChannel(String channelName, Object staticObject){
        mStaticChannelMap.put(channelName, staticObject);
    }
    public Object getStaticChannel(String channelName){
        return mStaticChannelMap.get(channelName);
    }
    public Set<String> getStaticChannelNames(){
        return mStaticChannelMap.keySet();
    }
    /*
    public void setChannelNames (ArrayList<String> channelNames){
        if (channelNames == null){
            ChannelNames.clear();
        }
        ChannelNames = channelNames;
    }
    public void addChannelnames (ArrayList<String> channelNames){
        if (channelNames == null){
            return;
        }
        ChannelNames.addAll(channelNames);
    }

 */
}
