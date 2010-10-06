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

package com.cospandesign.android.gpi.widget.paint;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

public class PaintView extends View{

		
	private boolean mSelected = false;
	
	private Context mContext;
	private int mColor = Color.WHITE;
	private int mSize = 10;
	
	private Canvas mCanvas;
	private final Paint mPaint;
	private Bitmap mBitmap;
	
	private int mCanvasWidth = 200;
	private int mCanvasHeight = 200;
	
	private int mCurX = 0;
	private int mCurY = 0;
	private float mCurPressure = 10;
	private float mCurSize = 10;
	private int mCurWidth = 10;
	private boolean mCurDown = false;
	private final Rect mRect = new Rect();
	
	//Constructor
	public PaintView(Context context, AttributeSet attrs) {
		super(context, attrs);
		mPaint = new Paint();
		mPaint.setAntiAlias(true);
		mPaint.setARGB(255, 255, 255, 255);
			
	}
	


	@Override
	protected void onSizeChanged(int w, int h, int oldw, int oldh) {
		super.onSizeChanged(w, h, oldw, oldh);
		mCanvasWidth = w;
		mCanvasHeight = h;
		
		Bitmap newBitmap = Bitmap.createBitmap(mCanvasWidth, mCanvasHeight, Bitmap.Config.ARGB_8888);
		Canvas newCanvas = new Canvas();
		newCanvas.setBitmap(newBitmap);
		if (mBitmap != null){
			newCanvas.drawBitmap(mBitmap, 0, 0, null);	
		}
		mBitmap = newBitmap;
		mCanvas = newCanvas;
	}
	

	
	@Override
	public boolean onTouchEvent(MotionEvent event) {
		if (!mSelected){
			return false;
		}
		int action = event.getAction();
		mCurDown = ((action == MotionEvent.ACTION_DOWN) || (action == MotionEvent.ACTION_MOVE));
		int N = event.getHistorySize();
		for (int i = 0; i < N; i++){
			drawPoint(event.getHistoricalX(i), event.getHistoricalY(i), event.getHistoricalPressure(i), event.getHistoricalSize(i));
		}
		drawPoint(event.getX(), event.getY(), event.getPressure(), event.getSize());
		return true;
	}



	//Paint functions
	public void clear(){
		if (mCanvas != null){
			mPaint.setARGB(0xFF, 0, 0, 0);
			mCanvas.drawPaint(mPaint);
			invalidate();
		}
	}
	private void drawPoint (float x, float y, float pressure, float size){
		mCurX = (int)x;
		mCurY = (int)y;
		mCurPressure = pressure;
		mCurSize = size;
		
		mCurWidth = (int)(mCurSize * (getWidth()/3));
		
		if (mCurWidth < 1){
			mCurWidth = 1;
		}
		if (mCurDown && (mBitmap != null)){
			int pressureLevel = (int)(mCurPressure * 255);
			mPaint.setColor(mColor);
			mPaint.setAlpha(pressureLevel);
			mCanvas.drawCircle(mCurX, mCurY, mCurWidth, mPaint);
			mRect.set(mCurX - mCurWidth - 2, mCurY - mCurWidth - 2, mCurX + mCurWidth + 2, mCurY + mCurWidth + 2);
			invalidate(mRect);
		}
		
	}
	@Override
	protected void onDraw(Canvas canvas) {
		if (mBitmap != null){
			canvas.drawBitmap(mBitmap, 0, 0, null);
		}
	}



	//user event has selected
	public void setSelected(boolean selected){
		mSelected = selected;
	}
	public void setColor(int color){
		mColor = color;
	}
	public void setSize (int size){
		mSize = size;
	}
	

	
	
}
