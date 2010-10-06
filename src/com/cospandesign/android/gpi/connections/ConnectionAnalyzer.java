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

package com.cospandesign.android.gpi.connections;

import com.cospandesign.android.gpi.entity.Entity;
import com.cospandesign.android.gpi.workspace.ConnectionPointView;
import com.cospandesign.android.gpi.workspace.WorkspaceEntity;

public class ConnectionAnalyzer {

	Entity mEntity1;
	Entity mEntity2;
	
	public ConnectionAnalyzer(){
		
	}
	public boolean isConnected(ConnectionPointView cpv1, ConnectionPointView cpv2){
		if (cpv1.isInput() && !cpv2.isInput()){
			//cpv1 = input
			//cpv2 = output
			return cpv2.getEntity().isConnected(cpv2.toString(), cpv1.getEntity(), cpv2.toString());
		}
		if (!cpv1.isInput() && cpv2.isInput()){
			//cpv1 = output
			//cpv2 = input
			return cpv1.getEntity().isConnected(cpv1.toString(), cpv2.getEntity(), cpv2.toString());
		}
		return false;
	}
	//TODO perhaps I should just analyze all connections at once, and then on a drop turn off the status
	public int analyzeConnection (ConnectionPointView cpv1, ConnectionPointView cpv2){
		if (cpv1.isInput() && !cpv2.isInput()){
			return cpv2.getEntity().canConnect(cpv1.getEntity());
			//return cpv2.getEntity().canConnectOutput(cpv1.getEntity());
		}
		else if (!cpv1.isInput() && cpv2.isInput()){
			return cpv1.getEntity().canConnect(cpv2.getEntity());
			//return cpv1.getEntity().canConnectOutput(cpv2.getEntity());
		}
		return WorkspaceEntity.OUTPUT_BAD;
	}
	public boolean connect(ConnectionPointView cpv1, ConnectionPointView cpv2){
		if (cpv1.isInput() && !cpv2.isInput()){
			return cpv2.getEntity().requestToBeOutputListener(cpv2.toString(), cpv1.getEntity(), cpv1.toString());
		}
		else if (!cpv1.isInput() && cpv2.isInput()){
			return cpv1.getEntity().requestToBeOutputListener(cpv1.toString(), cpv2.getEntity(), cpv2.toString());
		}
		return false;
	}
	public int analyzeInputConnection (WorkspaceEntity target, Entity item){
		
		//analyze the output of the item, and the input of the target, if the 
		mEntity1 = target.getEntity();
		mEntity2 = item;
		return mEntity1.canConnectInput(mEntity2);
	}
	public int analyzeOutputConnection (WorkspaceEntity target, Entity item){
		//Analyze the output of the target, and the input of the item
		mEntity1 = target.getEntity();
		mEntity2 = item;
		
		//return mEntity1.canConnectOutput(mEntity2);
		return mEntity1.canConnect(mEntity2);
	}
	public String shortReason(){
		//TODO put status strings here
		return "Entities are not compatable";
	}
	public String longReason(){
		//TODO write out reason why Entities are not compatable
		return "Long Response as to why the entities are not compatable";
	}
}
