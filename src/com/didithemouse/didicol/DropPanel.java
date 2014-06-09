package com.didithemouse.didicol;

import java.util.ArrayList;

import com.didithemouse.didicol.CreateActivity;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.util.Log;
import android.view.View;

public class DropPanel extends DragLayer {
	
	private ArrayList<View> items = new ArrayList<View>();
	private ArrayList<ViewWrapper> wrappers = new ArrayList<ViewWrapper>();
	private Bitmap drawLayer;
	
	public DropPanel(Context context,
			ArrayList<ViewWrapper> _wrappers, ArrayList<View> _items,
			Bitmap _drawLayer) {
        super(context, null);
        wrappers = _wrappers;
        items = _items;
        drawLayer = _drawLayer;
    } 

	
	@Override
	public void onDrop(DragSource source, int x, int y, int xOffset, int yOffset, DragView dragView, Object dragInfo) {
		if (!(dragInfo instanceof ExtendedImageView)) return;
		
		ExtendedImageView v = (ExtendedImageView) dragInfo;
		boolean wasDisplayed = true;
		if(items.contains(v)){
			items.remove(v);
			ViewWrapper w = findWrapperByView(v);
			wasDisplayed = w.wasDisplayed();
			wrappers.remove(w);			
		}
		
		MyAbsoluteLayout.LayoutParams lp = (MyAbsoluteLayout.LayoutParams)this.getLayoutParams();
		double porcentaje_left = (x-xOffset-lp.x)*1f/lp.width;
		double porcentaje_top = (y-yOffset-lp.y)*1f/lp.height;
		items.add(v);		
		ViewWrapper vw = new ViewWrapper(porcentaje_left, porcentaje_top,v,v.getEtapa());
		vw.setDisplayed(wasDisplayed);
		wrappers.add(vw);
		
		upgradeNet(x - xOffset,y - yOffset,xOffset,yOffset,dragInfo);
	}
	
	public void addObject(ExtendedImageView v) {
		boolean wasDisplayed = true;
		if(items.contains(v)){
			items.remove(v);
			ViewWrapper w = findWrapperByView(v);
			wasDisplayed = w.wasDisplayed();
			wrappers.remove(w);			
		}
		MyAbsoluteLayout.LayoutParams lp = (MyAbsoluteLayout.LayoutParams)this.getLayoutParams();
		double porcentaje_left = (v.getLeft()-lp.x)*1f/lp.width;
		double porcentaje_top = (v.getTop()-lp.y)*1f/lp.height;
		items.add(v);
		ViewWrapper vw = new ViewWrapper(porcentaje_left, porcentaje_top,v,v.getEtapa());
		vw.setDisplayed(wasDisplayed);
		v.setWrapper(vw);
		wrappers.add(vw);
	}
	public void updateObjectWrapper(ExtendedImageView v) {
		boolean wasDisplayed = true;
		ViewWrapper w = findWrapperByView(v);
		wasDisplayed = w.wasDisplayed();
		wrappers.remove(w);
		MyAbsoluteLayout.LayoutParams lp = (MyAbsoluteLayout.LayoutParams)this.getLayoutParams();
		double porcentaje_left = (v.getLeft()-lp.x)*1f/lp.width;
		double porcentaje_top = (v.getTop()-lp.y)*1f/lp.height;
		ViewWrapper vw = new ViewWrapper(porcentaje_left, porcentaje_top,v,v.getEtapa());
		vw.setDisplayed(wasDisplayed);
		v.setWrapper(vw);
		wrappers.add(vw);
	}
	
	public void updateObject(DragSource source, int x, int y, int xOffset, int yOffset, DragView dragView, Object dragInfo) {
		if (!(dragInfo instanceof ExtendedImageView)) return;
		ExtendedImageView v = (ExtendedImageView) dragInfo;
		int width, height;
		width = ((DragLayer)source).getMeasuredWidth();
		height = ((DragLayer)source).getMeasuredHeight();
		double porcentaje_left = (x-xOffset)*1f/width;
		double porcentaje_top = (y-yOffset)*1f/height;
		items.add(v);		
		ViewWrapper vw = new ViewWrapper(porcentaje_left, porcentaje_top,v,v.getEtapa());
		wrappers.add(vw);
	}
	
	@Override
	public void onDragEnter(DragSource source, int x, int y, int xOffset,
			int yOffset, DragView dragView, Object dragInfo) {
		// TODO Auto-generated method stub
		
		this.setBackgroundColor(0xC00080ff);
		
	}

	@Override
	public void onDragOver(DragSource source, int x, int y, int xOffset,
			int yOffset, DragView dragView, Object dragInfo) {
		MyAbsoluteLayout.LayoutParams lp = (MyAbsoluteLayout.LayoutParams)this.getLayoutParams();
		upgradeNet(x-xOffset+lp.x,y-yOffset+lp.y,xOffset,yOffset,dragInfo);
}
	@Override
	public void onDragExit(DragSource source, int x, int y, int xOffset,
			int yOffset, DragView dragView, Object dragInfo) {
		this.setBackgroundColor(Color.WHITE);
	
		View v = (View) dragInfo;		
		if(items.contains(v)){
			items.remove(v);
			ViewWrapper w = findWrapperByView(v);
			wrappers.remove(w);			
		}
	
		
	}
	private ViewWrapper findWrapperByView(View v) {
		
		for(ViewWrapper vw : wrappers){
			if(vw.getView(this.getContext()) == v){
				return vw;
			}
		}
		return null;
	}

	@Override
	public boolean acceptDrop(DragSource source, int x, int y, int xOffset,
			int yOffset, DragView dragView, Object dragInfo) {
		return true;
	}
	@Override
	public Rect estimateDropLocation(DragSource source, int x, int y,
			int xOffset, int yOffset, DragView dragView, Object dragInfo,
			Rect recycle) {
		// TODO Auto-generated method stub
		return null;
	}

    private BitmapDrawable drawLayerDrawable = null;   
    public Drawable getMediumBitmap()
    {
        if (drawLayerDrawable == null)
                drawLayerDrawable = new BitmapDrawable(drawLayer);
        drawLayerDrawable.setBounds(0, 0, CreateActivity.canvasWidth_mid, CreateActivity.canvasHeight_mid);
        return drawLayerDrawable;
    }
}
