package com.didithemouse.didicol;

import com.didithemouse.didicol.etapas.EtapaActivity.EtapaEnum;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.view.View;
import android.widget.TextView;

public class ViewWrapper {
	//Cache de la view, se genera solo una vez por Activity.
	private View view;
	private double x;
	private double y; 
	private boolean displayed;
	
	private EtapaEnum etapa;	
	int drawableID = -1;
	float scaleFactor = 1;
	//Cache del bitmap en caso de que sea un ExtImgView
	Bitmap resizedBitmap = null;
	CharSequence itemText = "";
	
	//Si es un extended Image View
	public ViewWrapper (double _x2, double _y2, View v, EtapaEnum _etapa)
	{
		x = _x2; y = _y2; etapa=_etapa;
		setView(v);
	}
	
	public ViewWrapper (double _x2, double _y2,  int _drawableID, float _scaleFactor, EtapaEnum _etapa)
	{
		x = _x2; y = _y2;  etapa=_etapa;
		drawableID = _drawableID;
		scaleFactor = _scaleFactor;
	}


	//Generamos la view y retornamos. Si es ni imageview ni textview
	// retorna null, no hay problema en eso
	
	public View getView(Context context)
	{
		// Nos aseguramos de que no se genere una view 2 veces.
		if (view != null) return view;
		
		if (drawableID != -1)
		{
			//Aqui se escala la ext img view y se retorna
			ExtendedImageView v = new ExtendedImageView(context,drawableID, scaleFactor,etapa);
			if (scaleFactor < 1 && scaleFactor > 0)
			{
				Bitmap b = BitmapFactory.decodeResource(context.getResources(), drawableID);
				int vHeight = b.getHeight();
				int vWidth = b.getWidth();
				
				resizedBitmap = getResizedBitmap(b
						, (int)(vHeight*scaleFactor), (int)(vWidth*scaleFactor));
				v.setImageBitmap(resizedBitmap);
				b.recycle();
			}
			else
			{
				v.setImageResource(drawableID);
			}
			view = v;
		}
		else if (itemText != null)
		{
			TextView t = new TextView(context);
			t.setText(itemText);
			t.setTextSize(20);
			view = t;
		}
		if (view != null)
		view.setContentDescription("no");
		return view;
	}
	
    private Bitmap getResizedBitmap(Bitmap bm, int newHeight, int newWidth) {

    	int width = bm.getWidth();
    	int height = bm.getHeight();
    	float scaleWidth = ((float) newWidth) / width;
    	float scaleHeight = ((float) newHeight) / height;
    	// create a matrix for the manipulation
    	Matrix matrix = new Matrix();
    	// resize the bit map
    	matrix.postScale(scaleWidth, scaleHeight);
    	// recreate the new Bitmap
    	Bitmap resizedBitmap = Bitmap.createBitmap(bm, 0, 0, width, height, matrix, false);
    	bm.recycle();
    	return resizedBitmap;
    }
    
	//Guardamos datos relevantes de la view
	public void setView(View _view)
	{
		if(_view instanceof ExtendedImageView)
		{
			ExtendedImageView eim = (ExtendedImageView) _view;
			drawableID = eim.getDrawableID();
			scaleFactor = eim.getScaleFactor();
		}
		else if (_view instanceof TextView)
		{
			itemText = ((TextView) _view).getText();
		}
		view = _view;
	}
	
	public String getText()
	{
		return itemText.toString();
	}
	
	public float getScaleFactor()
	{
		return scaleFactor;
	}
	
	public int getDrawableID()
	{
		return drawableID;
	}
	
	public void destroyView()
	{
		if(resizedBitmap != null)
		{
			resizedBitmap.recycle();
		}
		view = null;
	}
	
	public double getX() {
		return x;
	}
	public void setX(double x) {
		this.x = x;
	}
	public double getY() {
		return y;
	}
	public void setY(double y) {
		this.y = y;
	}
	public boolean wasDisplayed() {
		return displayed;
	}
	public void setDisplayed(boolean displayed) {
		this.displayed = displayed;
	}
	
	@Override
	public boolean equals(Object vw)
	{
		if(vw instanceof ViewWrapper)
			return ((ViewWrapper)vw).drawableID == drawableID;
		return false;
	}

	public EtapaEnum getEtapa() {
		return etapa;
	}

}
