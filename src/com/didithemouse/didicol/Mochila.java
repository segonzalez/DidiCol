package com.didithemouse.didicol;

// (8) Mochila mochila (8)
// (8) Mochila mochila (8)

import android.content.Context;
import android.graphics.Color;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.animation.ScaleAnimation;
import android.widget.ImageView;

public class Mochila extends ImageView implements DropTarget {
		
	public Mochila(Context context) {
		super(context);
		// TODO Auto-generated constructor stub
	}
	public Mochila(Context context, AttributeSet attrs) {
		super(context, attrs);
		// TODO Auto-generated constructor stub
	}	
	public Mochila(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		// TODO Auto-generated constructor stub
	}
	
	public MochilaContents getContents(){
		return MochilaContents.getInstance();
	}
	
	
	@Override
	public void onDrop(DragSource source, int x, int y, int xOffset, int yOffset, DragView dragView, Object dragInfo) {

		if(!(dragInfo instanceof ExtendedImageView))
			return;				
		
		if (dropped != null) dropped.run();
		ExtendedImageView v = (ExtendedImageView) dragInfo;

		MochilaContents.getInstance().addItem(v);
		v.setContentDescription("dropped");
		ScaleAnimation anim = new ScaleAnimation(1.0f,1.0f,1.1f,1.1f);
		anim.setDuration(300);
		this.startAnimation(anim);
	}
		
	@Override
	public void onDragEnter(DragSource source, int x, int y, int xOffset,
			int yOffset, DragView dragView, Object dragInfo) {
		// TODO Auto-generated method stub
		
		this.setColorFilter(0xC00080ff);
		
	}
	@Override
	public void onDragOver(DragSource source, int x, int y, int xOffset,
			int yOffset, DragView dragView, Object dragInfo) {
		// TODO Auto-generated method stub
		
	}
	@Override
	public void onDragExit(DragSource source, int x, int y, int xOffset,
			int yOffset, DragView dragView, Object dragInfo) {
		// TODO Auto-generated method stub
		this.setColorFilter(Color.TRANSPARENT);
		
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
	
	Runnable dropped;

	public void setDropRunnable(Runnable r)
	{dropped = r;}
}
