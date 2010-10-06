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

/* ChannelParser.java
 *
 * Parses a String containing a channel name, the structure to use with that
 * name along with any other member specific values
 *
 */
package dlcm;

import dlcm.dlcm.DLCM;
import java.util.ArrayList;

/**
 *
 * @author David McCoy, ported from lcmgen lcm.googlecode.com
 *
 * Channel Parser will parse "Channel Strings" or Channels from remote
 * publisher/subscribers,
 *
 * Channel String: [Structure Name] [Name of Channel] ... [Member + modifier]
 * modifier is subject to change but in general it will relay a member can be LIRO, or RILO
 * LIRO = Local Input Remote Output
 * RILO = Remote Input Local Output
 */
public class ChannelParser {
	
	//DLCM mDLCM;
        DLCM mChannelManager;
	String mStatus;
	String [] mChannelStrings;
	int mCount = 0;
	
	//Constructors/Initializer
        /**
         *
         * @param channelManager DLCM associated with this ChannelParser
         */
        public ChannelParser(DLCM channelManager){
		mChannelManager = channelManager;
	}
        /**
         *
         * @param channelString raw unparsed "Channel String" this can have many channels, and comments "#" separated by "\n"
         * @return an Arraylist of Channel Strings
         * @throws Exception No strings to parse, malformed string, or no structure found for the channel
         */
        public String[] parseChannelString(String channelString) throws Exception{
		
		mStatus = new String("");
		//Split the string into separate channel strings
		mChannelStrings = channelString.split("\n");
                ArrayList<String> channelNames = new ArrayList<String>();
		if (mChannelStrings.length == 0){
			parseError ("No Strings to parse");
			return null;
		}
		for (String string : mChannelStrings){
			mCount++;
			String name = parseLine (string);
                        if (name != null){
                            channelNames.add(name);
                        }
		}
                Object [] os = new Object[channelNames.size()];
                os = channelNames.toArray();
                String [] names = new String [channelNames.size()];
                for (int i = 0; i < os.length; i++){
                    names[i] = (String)os[i];
                }
		return names;
	}
        /**
         * Returns true if the structure is unknown
         * @param channelString raw channels to check
         * @return true if structure are not found for this channel
         * @throws Exception No strings to parse, malformed packet
         */
        public boolean unknownStructures(String channelString) throws Exception{
            String structureName = null;
            String channelName = null;
            Structure structure = null;
            Channel channel = null;

            mChannelStrings = channelString.split("\n");
            if (mChannelStrings.length == 0){
                    parseError ("No Strings to parse");
            }


            for (String string : mChannelStrings){
                    mCount++;

                if (string.substring(0, 1).matches("#")){
                    parseInfo("Line : " + mCount + " Comment Found\n");
                    continue;
                }


                //get the structure names
                String[] tokens = string.split(" ");

                if (tokens.length < 2){
                    parseError("Line : " + mCount + " Not enough tokens to make an channel");
                }

                structureName = tokens[0];
                channelName = tokens[1];

                if (!mChannelManager.StructureExists(structureName)){
                    return true;
                }
            }
            return false;
        }
	private String parseLine(String channelLine) throws Exception{

		String structureName = null;
		String channelName = null;
		Structure structure = null;
		Channel channel = null;
		
		if (channelLine.substring(0, 1).matches("#")){
			parseInfo("Line : " + mCount + " Comment Found\n");
			return null;
		}
		
		
		//get the structure names
		String[] tokens = channelLine.split(" ");
		
		if (tokens.length < 2){
			parseError("Line : " + mCount + " Not enough tokens to make an channel");
		}
		
		structureName = tokens[0];
		channelName = tokens[1];

                if (!mChannelManager.StructureExists(structureName)){
			parseError("Line : " + mCount + " No Structure found");
		}
		
		//Clone the structure, setup an channel with that structure, and
		//put it into the channel map
		structure = mChannelManager.CloneStructure(structureName);
                channel = new Channel(mChannelManager, structure, channelName);
		mChannelManager.AddChannel(channelName, channel);
		
		//The rest is optional
		for (int i = 2; i < tokens.length; i++){
			String [] subTokens = tokens[i].split(":");
			if (subTokens.length > 1){
				if (parseMemberModifier(subTokens, channel)){
					//found member, and insterted properties
					continue;
				}
//Add other tokens here
				parseInfo("Unrecognized Tokens\n");
			}
		}
                return channelName;
	}
	private boolean parseMemberModifier(String [] tokens, Channel channel) throws Exception{
		if (!channel.mStructure.MemberMap.containsKey(tokens[0])){
			return false;
		}
		Member member = channel.mStructure.MemberMap.get(tokens[0]);
		
		//By default all channels can publish, and subscribe, but if the user
		//has gone through the trouble of setting members, than they want control
		//over publish and subscribe
		
		channel.setCanPublish(false);
		channel.setCanSubscribe(false);
		
		for (String modifier : tokens){
			if (modifier.matches("[pP]")){
				channel.setCanPublish(true);
				continue;
			}
			if (modifier.matches("[sS]")){
				channel.setCanSubscribe(true);
				continue;
			}
			if (modifier.matches("[0-9]")){
				try{
					if (member.Type.matches("int8_t")) member.Data = Byte.parseByte(modifier);
					else if (member.Type.matches("int16_t")) member.Data = Short.parseShort(modifier);
					else if (member.Type.matches("int32_t")) member.Data = Integer.parseInt(modifier);
					else if (member.Type.matches("int64_t")) member.Data = Long.parseLong(modifier);
					else if (member.Type.matches("float")) member.Data = Float.parseFloat(modifier);
					else if (member.Type.matches("double")) member.Data = Double.parseDouble(modifier);
					else if (member.Type.matches("boolean")) member.Data = new Boolean ((Byte.parseByte(modifier) != 0));
				}
				catch (Exception e){
					parseError(e.getMessage());
				}
				continue;
			}
			if (member.Type.matches("string")) member.Data = new String(modifier);
			continue;
		}
		return true;
	}
	private void parseError(String error_string) throws Exception{
		throw new Exception (error_string);
	}
	private void parseInfo (String info){
		mStatus += info;
	}

}
