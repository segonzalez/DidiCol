package com.didithemouse.didicol;

import java.util.ArrayList;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.view.View;

public class DropPanelWrapper {

	//Arrays de wrappers e items, que manejaran tanto esta clase como dropPanel
	//Los items se generan en TIEMPO REAL en base a los wrappers, (cuando create&share llaman a 
	// los wrappers y revisan si estan en el array de items
	//, NO agregar cosas al array de items, solo a los wrappers
	private ArrayList<ViewWrapper> wrappers = new ArrayList<ViewWrapper>();
	private ArrayList<View> items = new ArrayList<View>();
	
	
	
	//Crear el dropPanel en base a la info que tenemos:
	DropPanel dp = null;
	public DropPanel getPanelView(Context context)
	{
		if (dp == null)
			dp =  new DropPanel(context,wrappers, items, getBitmap());
		dp.setBackgroundColor(Color.WHITE);
		//generateItems(context);
		return dp;
	}
	
	public void generateItems(Context context)
	{
		//Limpiamos objetos que no se pudieron generar
		ArrayList<ViewWrapper> tmp = new ArrayList<ViewWrapper>();
		for(int i = 0; i < wrappers.size(); i++)
		{
			if (wrappers.get(i) != null && wrappers.get(i).getView(context) != null)
				{tmp.add(wrappers.get(i));}
		}
		wrappers.clear(); wrappers = tmp;
		//Agregamos los objetos
		for (ViewWrapper vw : wrappers)
		{
			if (vw.getView(context) != null)
			items.add(vw.getView(context));
		}

	}
	
	public ArrayList<View> getItems(Context context)
	{
		return items;
	}
	
	// Bitmap para dibujo
	private Bitmap drawLayer = null;
	    
    /** @return Bitmap real para ser editado por FingerPaint */
    public Bitmap getBitmap()
    {
        if (drawLayer == null)
        	drawLayer = Bitmap.createBitmap(CreateActivity.canvasWidth, CreateActivity.canvasHeight, Bitmap.Config.ARGB_8888);
    	return drawLayer;
    }
    
    public boolean hasDrawn()  { return drawLayer != null; }
    
    public void setBitmap(Bitmap b)
    {
    	if( b==null|| !b.isMutable() || b.isRecycled()) return;
    	if (drawLayer != null) return;
    	drawLayer = b;
    }
    

	public int getNumItems()
	{
		return wrappers.size();
	}
	
	public ArrayList<ViewWrapper> getWrappers() {
		return wrappers;
	}


	double p_left=0;
	double p_top=0;
/*	public void addObject(ExtendedImageView v) {
		int vHeight = v.getHeight();
		int vWidth = v.getWidth();
		float vSize = Math.max(vHeight, vWidth);
		
		float scaleFactor = 1.0f;
		if(vSize > 100)
			scaleFactor = 0.5307692f - (0.0003076923f *vSize); 
		v.setScaleFactor(scaleFactor);		
		items.add(v);
		//Objeto de referencia: bandera de EEUU
		//Width : 247, Ocupa 1/3 de la diapo, scaleFactor 0.4547...
		double apparentSize = vWidth/247.0 * 0.3333 * (scaleFactor/0.45476922);
		//Fix feo: Si el tamaño es 0, quiere decir que es una medalla.
		//Esto porque los objetos que no se han rendereado son de tamaño 0.
		if(apparentSize < 0.02) apparentSize = 0.19880261848172287;
		
		if(p_left +apparentSize > 1){
			p_left = 0.0;
			p_top = 0.5;
		}
		ViewWrapper vw = new ViewWrapper(p_left, p_top, vWidth/2, vHeight/2, v);
		p_left += apparentSize;

		wrappers.add(vw);
	}*/
	

	public void replaceObject(View old, View n) {
		for(ViewWrapper vw : wrappers){
			if(vw.getView(old.getContext()) == old){
				vw.setView(n);
				if (items.contains(old))
				items.remove(old);
				items.add(n);
			}
		}
	}

	public void setWrappers(ArrayList<ViewWrapper> wrappers) {
		this.wrappers = wrappers;
	}

	
	public void killPanel(boolean clearViews)
	{
		cleanPanel(clearViews);
    	if(drawLayer != null)
    	{
    		drawLayer.recycle();
    		drawLayer = null;
    	}
	}
	
    public void cleanPanel(boolean clearViews)
    {
    	
    	dp = null;
    	if(clearViews)
    		items.clear();
    	for(ViewWrapper vw : wrappers)
    	{
    		vw.destroyView();
    	}
    }
	

}
