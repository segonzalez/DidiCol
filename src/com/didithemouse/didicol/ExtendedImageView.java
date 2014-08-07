package com.didithemouse.didicol;

import android.content.Context;
import android.widget.ImageView;

import com.didithemouse.didicol.etapas.EtapaActivity.EtapaEnum;

public class ExtendedImageView extends ImageView {
	
	private ViewWrapper wrapper;
	private int drawableID;
	private float scaleFactor = 1;
	private EtapaEnum etapa;
	
	public ExtendedImageView(Context context, int _drawableID, float _scaleFactor, EtapaEnum _etapa) {
		super(context);
		if (scaleFactor == 0) scaleFactor = 1;
		scaleFactor = _scaleFactor;
		drawableID = _drawableID;
		etapa = _etapa;
	}
	
	public ViewWrapper getWrapper() {
		return wrapper;
	}

	public void setWrapper(ViewWrapper wrapper) {
		this.wrapper = wrapper;
	}
	
	public int getDrawableID()
	{
		return drawableID;
	}
	public void setScaleFactor(float _scaleFactor)
	{
		scaleFactor =_scaleFactor;
	}
	public float getScaleFactor()
	{
		return scaleFactor;
	}
	public EtapaEnum getEtapa()
	{
		return etapa;
	}
}
