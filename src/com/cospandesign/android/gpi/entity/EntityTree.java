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

package com.cospandesign.android.gpi.entity;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.NoSuchElementException;

import com.cospandesign.android.gpi.controller.Controller;
import com.cospandesign.android.gpi.device.Device;
import com.cospandesign.android.gpi.medium.Medium;

public class EntityTree
{
	
	Controller mController;
	int mSize = 0;
	
	public EntityTree(){
		mController = null;
		mSize = 0;
	}
	public EntityTree(Controller c)
	{
		mController = c;
		scan();
	}
	public boolean add(Entity entity)
	{
		scan();
		if (entity instanceof Medium){
			//add medium
			mController.getMediums().add((Medium)entity);
			mSize++;
			mSize += ((Medium)entity).getDevices().size();
		}
		if (entity instanceof Device){
			//add device
			//find the parent, and then attach to that medium as well as the control tree
			if (!mController.getMediums().contains((Medium)((Device)entity).getParent()))
			{
				return false;
			}
			//Medium exists
			int index = (mController.getMediums().indexOf((Medium)((Device)entity).getParent()));
			mController.getMediums().get(index).getDevices().add((Device)entity);
			mSize++;

		}
		if (entity instanceof Controller){
			//can't have more than one controller
			if (mController != null)
			{
				return false;
			}
			mController = (Controller)entity;
			mSize = 1;
			mSize += mController.getMediums().size();
			for (Medium medium : mController.getMediums())
			{
				mSize += medium.getDevices().size();
			}
		}
		
		return true;
	}
	public void clear()
	{
		
		if (mController != null)
		{
			if (mController.getMediums().size() > 0)
			{
				for (Medium medium : mController.getMediums()){
					medium.getDevices().clear();
				}
				mController.getMediums().clear();
			}
		}
		mController = null;
		
		mSize = 0;
		
	}
	public boolean contains(Object obj)
	{
		scan();
		if (!(obj instanceof Entity))
		{
			//not even an entity
			return false;
		}
		
		Entity entity = (Entity)obj;
		if (entity instanceof Controller){
			if (entity.equals(mController))
			{
				return true;
			}
		}
		if (entity instanceof Medium){
			if (mController.getMediums().contains(entity))
			{
				return true;
			}
		}
		if (entity instanceof Device){
			for (Medium medium:mController.getMediums()){
				if (medium.getDevices().contains(entity)){
					return true;
				}
			}
		}
		
		return false;
	}
	public boolean isEmpty()
	{
		scan();
		if (mSize != 0)
		{
			return false;
		}
		return true;
	}
	public Iterator<Entity> iterator()
	{
		scan();
		return new EntityTreeIterator();
	}
	public boolean remove(Entity entity)
	{
		scan();
		//check all the different things inside the controller to see if it exists
		//Controller
		if (entity instanceof Controller){
			mController = null;
			mSize = 0;
		}
		//Medium
		if (entity instanceof Medium){
			if(!mController.getMediums().contains(entity))
			{
				return false;
			}
			int index = mController.getMediums().indexOf(entity);
			mSize -= mController.getMediums().get(index).getDevices().size();
			mController.getMediums().remove(entity);
			mSize--;
			return true;
		}
		//Device
		if (entity instanceof Device){
			for (Medium medium : mController.getMediums())
			{
				if (!medium.getDevices().contains(entity))
				{
					continue;
				}
				
				//make sure the device is pointing to the medium parent (Probably don't need this)
				if (((Medium)((Device)entity).getParent()).equals(medium))
				{
					medium.getDevices().remove(entity);
					mSize--;
					return true;
				}
				
			}
		}

		
		return false;
	}
	public int size()
	{
		scan();
		return mSize;
	}
	public void scan()
	{
		//if the user adds or removes something not using the tree method then, I need to update the size
		mSize = 1;
		mSize += mController.getMediums().size();
		for (Medium medium : mController.getMediums())
		{
			mSize += medium.getDevices().size();
		}
	}
	public Entity[] toArray()
	{
		Entity[] entityList = new Entity[this.mSize];;
		int index = 0;
		
		Iterator<Entity> iter = this.iterator();
		while (iter.hasNext()){
			entityList[index] = iter.next();
			index++;
		}
		
		return entityList;
	}
	public Controller getController(){
		return mController;
	}
	private class EntityTreeIterator implements Iterator
	{
		Controller c;
		ArrayList<Medium> m;
		int mIndex;
		ArrayList<Device> d;
		int dIndex;
		boolean start = true;
		
		public Entity currentEntity;

		public EntityTreeIterator(){
			c = mController;
			start = true;

			//Check if there are any mediums
			m = c.getMediums();
			if (m.size() > 0)
			{
				//There are mediums!
				//Check if the first medium has any devices?
				d = m.get(0).getDevices();
				if (d.size() > 0)
				{
					//There are devices!
					//set the currentEntity to the first device
					currentEntity = d.get(0);
				}
				else
				{
					//There are no devices
					d = null;
					//first entity is a medium
					currentEntity = m.get(0);
				}
			}
			else
			{
				//There are no mediums
				m = null;
				currentEntity = c;
			}

			mIndex = 0;
			dIndex = 0;
			
		}

		//@Override
		public boolean hasNext()
		{
			
			if (c == null)
			{
				return false;
			}
			if ((currentEntity == c) && (start == false))
			{
				//we reached the top, which is the last node
				return false;
			}
			
			return true;
		}
		//@Override
		public Entity next()
		{
			if (start == true)
			{
				start = false;
				return currentEntity;
			}
			
			if (c == null){
				throw new NoSuchElementException();
			}
			
			if (d!= null){
				dIndex++;
				if (dIndex >= d.size()){
					d = null;
				}
				else{
					currentEntity = d.get(dIndex);
					return currentEntity;
				}
				
			}
			if ((m != null) && (d == null)){
				//just came up from a device
				if (dIndex > 0) {
					currentEntity = m.get(mIndex); //return the current medium
					dIndex = 0;
					return currentEntity;
				}
				//we are moving to the next medium
				else { 
					mIndex++;
					if (mIndex >= m.size()){
						m = null;
					}
					else {
						d = m.get(mIndex).getDevices();
						//check if there are any devices
						if (d.size() > 0){
							dIndex = 0;
							currentEntity = d.get(0);
							return currentEntity;
						}
						//can't go to the devices
						else {
							//the medium is the currentEntity
							d = null;
							currentEntity = m.get(mIndex);
							return currentEntity;
						}
					}
				}
			}
			//we are done with all the Controller, Medium, and Device
			if ((currentEntity != c) && (c != null) && (m == null) && (d == null))
			{
				currentEntity = c;
				c = null;
				return currentEntity;
			}			

			return null;
		}
		//@Override
		public void remove()
		{
			throw new UnsupportedOperationException();
		}

		
	}
	
}
