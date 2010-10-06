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

package com.cospandesign.android.gpi.workspace;

import android.graphics.Rect;

public class ViewPort{
	
	//Size of the View port
	public int mSizeX;
	public int mSizeY;
	
	//Padding between cells
	public float mPaddingX;
	public float mPaddingY;
	
	private float mDPaddingX;
	private float mDPaddingY;
	
	//Size of the cells
	public float mCellSizeX;
	public float mCellSizeY;
	
	private float mDCellSizeX;
	private float mDCellSizeY;
	
	//Maximum X,Y can scroll
	public int mMaxX;
	public int mMaxY;
	
	//Zoom of the image
	//private float Scale;
	
	//Location of viewport, this is a little tricky because it changes with respect to SCALING, and ZOOM
	public float x;
	public float y;
	
	public Rect mVRect;
	
	public ViewPort (int cellSizeX, int cellSizeY, int paddingX, int paddingY){
		
		mDCellSizeX = cellSizeX;
		mCellSizeX = cellSizeX;
		
		mDCellSizeY = cellSizeY;
		mCellSizeY = cellSizeY;
		
		mDPaddingX = paddingX;
		mPaddingX = paddingX;
		
		mDPaddingY = paddingY;
		mPaddingY = paddingY;
		
		//Scale = 1.0f;
		
		mVRect = new Rect();
		
		x = 0;
		y = 0;
	}
	
	public void setSize(int width, int height){
		mSizeX = width;
		mSizeY = height;
		
		setRect();
	}
	
	public void calculateMaxDimensions(int canvasCellsX, int canvasCellsY){
		mMaxX = (int) ((canvasCellsX * mCellSizeX + ((canvasCellsX + 1) * mPaddingX)) - mSizeX);
		mMaxY = (int) ((canvasCellsY * mCellSizeY + ((canvasCellsY + 1) * mPaddingY)) - mSizeY);
	}
	
	private void setRect(){
		mVRect.set((int)x, (int)y, (int)(x + mSizeX), (int)(y + mSizeY) );
	}
	public void SetScale(float scale){
		//if the scale changed, we need to modify a bunch of other stuff
		//Scale = scale;
		
		mCellSizeX = mDCellSizeX * scale;
		mCellSizeY = mDCellSizeX * scale;
		if (mCellSizeX <= 0.001f){
			mCellSizeX = 0.001f;
		}
		if (mCellSizeY <= 0.001f){
			mCellSizeY = 0.001f;
		}
		
		mPaddingX = mDPaddingX * scale;
		mPaddingY = mDPaddingY * scale;
		if (mPaddingX < 0.0f){
			mPaddingX = 0.0f;
		}
		if (mPaddingY < 0.0f){
			mPaddingY = 0.0f;
		}
		
	}

	public float getDPaddingX()
	{
		return mDPaddingX;
	}

	public float getDPaddingY()
	{
		return mDPaddingY;
	}

	public float getDCellSizeX()
	{
		return mDCellSizeX;
	}

	public float getDCellSizeY()
	{
		return mDCellSizeY;
	}

	
}