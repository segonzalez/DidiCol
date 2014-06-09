package com.didithemouse.didicol.etapas;

import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;

class EtapaGestureListener extends GestureDetector.SimpleOnGestureListener {

	EtapaSurfaceView surfaceView;

	public EtapaGestureListener(EtapaSurfaceView etapaSurfaceView) {
		surfaceView = etapaSurfaceView;
	}

	@Override
	public boolean onSingleTapUp(MotionEvent ev) {
		// Log.d("onSingleTapUp",ev.toString());
		return true;
	}

	@Override
	public void onShowPress(MotionEvent ev) {
		// Log.d("onShowPress",ev.toString());
	}

	@Override
	public void onLongPress(MotionEvent ev) {
		// Log.d("onLongPress",ev.toString());
	}

	@Override
	public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX,
			float distanceY) {
		// Log.d("onScroll",e1.toString());
		surfaceView.move(distanceX, distanceY);
		return true;
	}

	@Override
	public boolean onDown(MotionEvent ev) {
		// Log.d("onDownd",ev.toString());
		
		if (!surfaceView.mRenderer.mScroller.isFinished()) { // is flinging
			surfaceView.mRenderer.mScroller.forceFinished(true); // to stop flinging on touch
		}
		if(surfaceView.mRenderer.endStory)
		{
			
		}
		return true;
	}

	@Override
	public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX,
			float velocityY) {
		Log.d("dev", "FLING!");
			surfaceView.mRenderer.mScroller.fling(0, 0,
					-(int) (velocityX * 0.5f), -(int) (velocityY * 0.5f),
					Integer.MIN_VALUE, Integer.MAX_VALUE, Integer.MIN_VALUE,
					Integer.MAX_VALUE);
			Log.d("dev",
					"Duration: "
							+ surfaceView.mRenderer.mScroller.getDuration());
			surfaceView.requestRender();
			return true;
	}
}
