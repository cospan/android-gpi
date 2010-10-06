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

package com.cospandesign.android.gpi.widget.chart;

import java.util.Hashtable;
import java.util.LinkedList;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.graphics.Paint.Style;
import android.os.Handler;
import android.text.format.Time;
import android.view.View;

import com.cospandesign.gpi.R;

public class FloatGraphView extends View
{


	private final static int CONTINUOUS_GRAPH = 1;
	private final static int ROLLING_GRAPH = 2;
	
	
	//TODO Make these properties in the Future
	private int XPaddingDivisor = 16;
	private int YPaddingDivisor = 16;
	
	private int XMaxPaddingSize = 10;
	private int YMaxPaddingSize = 10;
	
	private int MaxNumber = 100;
	private int MinNumber = 0;
	
	
	private int GraphViewFlags = CONTINUOUS_GRAPH;
	
	private int NumberOfChannels = 0;
	private int DefaultNumberOfPoints = 100;
	private int NumberOfDataPoints = 100;
	private int MaxNumberOfPoints = 10000;
	private int DropCount = 0;
	private int SamplePerNumberOfRead = 1;
	private int PeriodInMillis = 1000;
	private int LastIndex = NumberOfDataPoints - 1;
	private int DataValidatyCount = 0;
	private boolean DataPointsChanged = true;
	
	private Time GraphTime;
	
	Timer mTimer;
	TimerTask mTimerTask;
	
	//private Hashtable<String, LinkedList<FloatTimePoint>> GraphData;
	private Hashtable<String, LinkedList<DoubleTimePoint>> GraphData;

	//Drawing
	
	Bitmap FrontBitmap;
	
	Bitmap BackBitmap;
	
	Bitmap NoDataBitmap;
	Paint MainPaint;
	
	//(FrontGraphisReady == true && NewGraphicsReady) : FrontBitmap/FrontPaint
	//(FontGraphicsReady == false && NewGraphicsReady) : BackBitmap/BackPaint
	private volatile boolean FrontGraphicsSelected = false;
	private volatile boolean NewGraphicsReady = false;
	
	//True when drawing
	private volatile boolean DrawLock = false;
	private volatile boolean UpdateMutex = false;
	
	//TODO perhaps another thing here for when there is a change in layout
	private boolean NewDataFlag = false;
	private boolean mAutoRangeFlag = true;
	
	
	//Drawing Graph View Functions
	public FloatGraphView(Context context)
	{
		super(context);
		//GraphData = new Hashtable<String, LinkedList<FloatTimePoint>>();
		GraphData = new Hashtable<String, LinkedList<DoubleTimePoint>>();
		setNumberOfDataPoints(DefaultNumberOfPoints);
		GraphTime = new Time();
		NumberOfChannels = 0;
		setBackgroundColor(Color.BLACK);
		NoDataBitmap = BitmapFactory.decodeResource(this.getContext().getResources(), R.drawable.no_data);
		MainPaint = new Paint();
		
		mTimer = new Timer();
		mTimerTask = new TimerTask(){
			public void run(){
				UpdateGraphics();
			}		
		};	
		//Schedule the timer based on the default properties
		mTimer.scheduleAtFixedRate(mTimerTask, 1000, 100);
	}

	public void setNumberOfDataPoints(int numberOfDataPoints)
	{
		
		//stupidity check
		if (numberOfDataPoints < 10){
			numberOfDataPoints = 10;
		}
		//Check to see if the size of the number of data points is larger, or smaller than the present setting

		Set<String> NameList = GraphData.keySet();
		
		for (String name : NameList){
			//LinkedList<FloatTimePoint> alf = GraphData.get(name);
			LinkedList<DoubleTimePoint> alf = GraphData.get(name);

			GraphTime.setToNow();
			
			long value = GraphTime.toMillis(true);
			
			if (!alf.isEmpty()){
				value = alf.getFirst().Time; 
			}
			while (alf.size() < numberOfDataPoints){
				value = (value - (PeriodInMillis / NumberOfDataPoints));
				//FloatTimePoint point = new FloatTimePoint(0.0f, value);
				DoubleTimePoint point = new DoubleTimePoint(0.0f, value);
				//point.Data = 0.0f;
				point.Data = 0.0;
				point.Time = value;
				alf.addFirst(point);
			}

			while (alf.size() > numberOfDataPoints){
				alf.removeFirst();
			}

		}

		NumberOfDataPoints = numberOfDataPoints;
		LastIndex = NumberOfDataPoints - 1;

		DataPointsChanged = true;
		DataValidatyCount = 0;

			
	}
	public void setPointsFromSampleRateMillis(int millisBetweenSample, int desiredPeriodMillis){
		SamplePerNumberOfRead = 0;
		//check to see if we can have one sample per data...
		if (desiredPeriodMillis < (millisBetweenSample * 10)){
			//one per, and just let it go
			desiredPeriodMillis = millisBetweenSample * 10;
		}
		while (((desiredPeriodMillis / millisBetweenSample)/SamplePerNumberOfRead) >  MaxNumberOfPoints){
			SamplePerNumberOfRead++;
		}	
		
		int dataPoints = (desiredPeriodMillis / millisBetweenSample)/SamplePerNumberOfRead;
		setNumberOfDataPoints(dataPoints);
	}
	public void setPointsFromSampleRateMicro(int microsBetweenSample, int desiredPeriodMillis){
		SamplePerNumberOfRead = 0;
		//check to see if we can have one sample per data...
		if (desiredPeriodMillis < (microsBetweenSample * 1000 * 10)){
			//one per, and just let it go
			desiredPeriodMillis = microsBetweenSample * 1000 * 10;
		}
		while ((((desiredPeriodMillis * 1000)/ microsBetweenSample)/SamplePerNumberOfRead) >  MaxNumberOfPoints){
			SamplePerNumberOfRead++;
		}	
		
		int dataPoints = ((desiredPeriodMillis * 1000) / microsBetweenSample)/SamplePerNumberOfRead;
		setNumberOfDataPoints(dataPoints);
	}
	public boolean setPeriod(int PeriodInMillis){
		//determine the average difference in millis for this information, and set call the setPointsFromSampleRateMillis
		int count = NumberOfDataPoints - 1;
		int currentIndex = 1;
		int previousIndex = 0;
		long averageValue = 0;
		
		//not enough data to determine a period
		if ((NumberOfDataPoints < 2) && 
				!DataPointsChanged){
			return false;
		}

		Set<String> NameList = GraphData.keySet();
		if (NameList.size() == 0){
			return false;
		}
		//LinkedList<FloatTimePoint> firstDataList = GraphData.get(NameList.toArray()[0]);
		LinkedList<DoubleTimePoint> firstDataList = GraphData.get(NameList.toArray()[0]);
		
		while (currentIndex < NumberOfDataPoints){
			
			averageValue = firstDataList.get(currentIndex).Time - firstDataList.get(previousIndex).Time;
			previousIndex = currentIndex;
			currentIndex++;
			if (currentIndex == firstDataList.size()){
				currentIndex = 0;
			}
		}
		
		averageValue = averageValue / count;
		setPointsFromSampleRateMillis((int)averageValue, PeriodInMillis);
		
		//Found a period
		return true;
	}
	public boolean canDetermineSampleRate(){
		
		//not enough data to determine a period
		
		if ((NumberOfDataPoints < 2) && 
				!DataPointsChanged){ //if we recently changed the number of data points then our data is going to be eronious for a while, we cant set the period automatically
		
			return false;
		}
		return true;
		
	}
	public void stop(){
		if (mTimerTask != null){
			mTimerTask.cancel();
		}
		if (mTimer != null){
			mTimer.cancel();
			mTimer.purge();
		}
		if (FrontBitmap != null){
			FrontBitmap.recycle();
		}
		if (BackBitmap != null){
			BackBitmap.recycle();
		}
	}
	
	//Graph Functions
	public void setGraphMax (int max){
		MaxNumber = max;
	}
	public void setGraphMin (int min){
		MinNumber = min;
	}
	public void addData(AddPointStruct[] points){
		
		if (points[0] != null){
			if (GraphData.get(points[0].Name) == null){
				addChannel(points[0].Name);
				setNumberOfDataPoints(NumberOfDataPoints);
			}
		}
		
		//check to see if we are at the end of the data
		if (DropCount + 1 < SamplePerNumberOfRead){
			DropCount++;
			return;
		}
		
		GraphTime.setToNow();
		
		//TODO find the MAX and MIN values of the graph, track them on the way in and then keep note of their location, when they drop out.
		for (AddPointStruct point : points){
			//point.NewPoint.Time = GraphTime.toMillis(true);
			point.NewPoint.Time = GraphTime.toMillis(true);
			GraphData.get(point.Name).removeFirst();
			GraphData.get(point.Name).addLast(point.NewPoint);
		}
		
		//if we recently changed the number of data points then our data is going to be eronious for a while, we cant set the period automatically
		if (DataPointsChanged){
			DataValidatyCount++;
			if (DataValidatyCount >= NumberOfDataPoints){
				DataPointsChanged = false;
			}
		}
		NewDataFlag = true;
	}
	public void addChannel(String name){
		//GraphData.put(name, new LinkedList<FloatTimePoint>());
		GraphData.put(name, new LinkedList<DoubleTimePoint>());
		NumberOfChannels = GraphData.size();
	}
	public void removeChannel(String name){
		GraphData.remove(name);
		NumberOfChannels = GraphData.size();
	}
	public void setMaxPossibleDataPoints(int max){
		MaxNumberOfPoints = max;
	}
	public int getNumberOfDataPoints()
	{
		return NumberOfDataPoints;
	}
	
	//Drawing
	@Override
	protected void onDraw(Canvas canvas)
	{
		Bitmap bitmap = null;
		Paint paint = null;
		
		DrawLock = true;
		
		//If Front Draw is lock, check the back
		if (NewGraphicsReady && FrontGraphicsSelected){
			bitmap = FrontBitmap;
		}
		else if (NewGraphicsReady && !FrontGraphicsSelected){
			bitmap = BackBitmap;
		}
		else{
			bitmap = NoDataBitmap;
			paint = MainPaint;
		}
		
		canvas.drawBitmap(bitmap, null, new RectF(0, 0, this.getLayoutParams().width, this.getLayoutParams().height), MainPaint);
		//The only flag that I can reset
		NewGraphicsReady = false;
		
		
		if (bitmap.equals(FrontBitmap)){
			if (FrontGraphicsSelected){
				//Nothing was switched before the thread had a chance to change it
				NewGraphicsReady = false;
			}
		}
		else{
			if (!FrontGraphicsSelected){
				//Nothing was switched before the thread had a chance to change it
				NewGraphicsReady = false;
			}
		}
		DrawLock = false;
	}
	@Override
	protected void onLayout(boolean changed, int left, int top, int right,
			int bottom)
	{
		this.getLayoutParams().height = right - left;
		this.getLayoutParams().width = bottom - top;
	}
	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec)
	{
		setMeasuredDimension(MeasureSpec.getSize(widthMeasureSpec), MeasureSpec.getSize(heightMeasureSpec));
	}
	public boolean setGraphViewFlags(int Flag){
		if (((Flag | CONTINUOUS_GRAPH) != 0) && ((Flag | ROLLING_GRAPH) != 0)){
			return false;
		}
		if ((Flag | CONTINUOUS_GRAPH) != 0){
			GraphViewFlags = CONTINUOUS_GRAPH;
		}
		if ((Flag | ROLLING_GRAPH) != 0){
			GraphViewFlags = ROLLING_GRAPH;
		}
		
		return true;
	}
	public void setAutoRange (boolean autoRange){
		mAutoRangeFlag = autoRange;
	}
	public boolean getAutoRange (){
		return mAutoRangeFlag;
	}
	
	//Graph Point Class
	/*
	public static class FloatTimePoint {
		public Float Data;
		public Long Time;
		public FloatTimePoint (float f, long t){
			Data = new Float(f);
			Time = new Long(t);
			
		}
	}
	*/
	public static class DoubleTimePoint {
		public Double Data;
		public Long Time;
		public DoubleTimePoint (double d, long t){
			Data = new Double(d);
			Time = new Long(t);
		}
	}
	public static class AddPointStruct{
		public String Name;
		//public FloatTimePoint NewPoint;
		public DoubleTimePoint NewPoint;
//		public AddPointStruct(String name, float data, long time){
		public AddPointStruct (String name, double data, long time){
			Name = name;
			//NewPoint = new FloatTimePoint(data, time);
			NewPoint = new DoubleTimePoint(data, time);
		}
	}

	
	//Drawing Background stuff
	//TODO Timer thread to start click off the drawing stuff
	
	//CallBack Handler, to respond to changes in runnable
	final Handler DrawingUpdateHandler = new Handler();
	//Post things with this runnable
	final Runnable DrawingUpdateRunnable = new Runnable(){
		public void run(){
			//do something for runnable
			UpdateMutex = false;
			invalidate();
		}
	};
	private void UpdateGraphics(){
		if (!NewDataFlag){
			return;			
		}
		NewDataFlag = false;
		if (UpdateMutex){
			//we are already updating, don't want to launch off too many threads
			return;
		}
		int maximum = 0;
		int minimum = 0;
/*
		Thread t = new Thread(){
			public void run(){
			*/
				//process Graphics
				Canvas GraphCanvas = null;
				Bitmap GraphBitmap = null;
				
				Canvas DrawingCanvas = null;
				Bitmap DrawingBitmap = null;
				
				float XPadding;
				float YPadding;
				
				int GraphHeight = (int)(MaxNumber - MinNumber); 
				
				RectF DrawableRectangle = null;
				
				int width = getLayoutParams().width;
				int height = getLayoutParams().height;
				
				//Create a bitmap for Graphical Data
				GraphBitmap = Bitmap.createBitmap((PeriodInMillis), GraphHeight, Bitmap.Config.ARGB_4444);
				GraphCanvas = new Canvas(GraphBitmap);

//int GraphMin = GraphHeight - MinNumber;
				//Run through all the paths that we have, and draw them onto the GraphBitmap
				int[] mColors = new int[] { Color.YELLOW, Color.RED, Color.BLUE, Color.GREEN, Color.CYAN };
				
				Paint pnt =	null;
				Path p = null;
				int i = 0;
				boolean setMin = true;
				for (String name : GraphData.keySet()){
					
					pnt = new Paint();
					pnt.setColor(Color.YELLOW);
					pnt.setStyle(Style.STROKE);
					pnt.setStrokeWidth(2.0f);
					i++;
					if (i == mColors.length){
						i = 0;
					}
					
					//LinkedList <FloatTimePoint> l = GraphData.get(name);
					LinkedList<DoubleTimePoint> l = GraphData.get(name);
					p = new Path();
					long startValue = l.get(0).Time;
					float step = ((float)(PeriodInMillis))/((float)NumberOfDataPoints);
					p.moveTo(0, (float) (GraphHeight - l.get(0).Data));
					if (setMin){
						minimum = (int) (double)l.get(0).Data;
						maximum = (int) (double)l.get(0).Data;
						setMin = false;
					}
					for (int j = 1; j < l.size(); j++){
						if (mAutoRangeFlag){
							if (l.get(j).Data > maximum){
								maximum = (int) (double) (l.get(j).Data);
							}
							if (l.get(j).Data < minimum){
								minimum = (int) (double) (l.get(j).Data);
							}
						}
						p.lineTo(step*j, (float) (GraphHeight - (l.get(j).Data - MinNumber)));
					}
					
					GraphCanvas.drawPath(p, pnt);
				}
				
				//Create a bitmap for drawing to the screen
				int bitmapWidth = getMeasuredWidth();
				int bitmapHeight = getMeasuredHeight();
				if (bitmapWidth < 1){
					bitmapWidth = 100;
				}
				if (bitmapHeight < 1){
					bitmapHeight = 100;
				}
				DrawingBitmap = Bitmap.createBitmap(bitmapWidth, bitmapHeight, Bitmap.Config.ARGB_4444	);
				DrawingCanvas = new Canvas(DrawingBitmap);
			
				//Get the Padding
				if (width/XPaddingDivisor > XMaxPaddingSize){
					XPadding = XMaxPaddingSize;
				}
				else{
					XPadding = width/XPaddingDivisor;
				}
				if (height/YPaddingDivisor > YMaxPaddingSize){
					YPadding = YMaxPaddingSize;
				}
				else{
					YPadding = height/YPaddingDivisor;
				}
				
				DrawableRectangle = new RectF(XPadding, YPadding, bitmapWidth - XPadding, bitmapHeight - YPadding);
				
				DrawingCanvas.drawBitmap(GraphBitmap, null, DrawableRectangle, pnt);

				Paint dimensionsPaint = new Paint();
				dimensionsPaint.setColor(Color.WHITE);
			
				
				//Draw the X axis line
				DrawingCanvas.drawLine(XPadding, height - YPadding, width - XPadding, height - YPadding, dimensionsPaint);
				//Draw the Y axis line
				DrawingCanvas.drawLine(XPadding, YPadding, XPadding, height - YPadding, dimensionsPaint);
				
				//For now used the set Max, Min values given by the properties
				
				
				//Go through each of the GraphData Lists, and draw out the line from the beginning of the graph to the end
				
				
				
				
				// find out which bitmap I can use
				if (DrawLock || NewGraphicsReady){
				
					//use the other buffer
					if (FrontGraphicsSelected){
						BackBitmap = DrawingBitmap;
						FrontGraphicsSelected = false;
						NewGraphicsReady = true;
					}
					else{
						FrontBitmap = DrawingBitmap;
						FrontGraphicsSelected = true;
						NewGraphicsReady = true;
					}
				}
				else{
					//Nothing is setup so just use FrontBitmap
					FrontBitmap = DrawingBitmap;
					FrontGraphicsSelected = true;
					NewGraphicsReady = true;
				}
//DrawingUpdateHandler.post(DrawingUpdateRunnable);
				if (maximum == minimum){
					if (maximum < Integer.MAX_VALUE){
						maximum++;
					}
					if (minimum > Integer.MIN_VALUE){
						minimum--;
					}
				}
				 MaxNumber = maximum;
				 MinNumber = minimum;
				 
				 GraphBitmap.recycle();

//For Debug, the real version will be a thread, and this must be sent to the post command
postInvalidate();

				 /*			}
			
	
		};
		
		t.start();
		*/

	}
	
	
}
