package com.didithemouse.didicol.etapas;

import com.didithemouse.didicol.R;
import com.didithemouse.didicol.MyAbsoluteLayout.LayoutParams;

import android.os.Bundle;

import android.view.View;
import android.widget.FrameLayout;

public class ConeyActivity extends EtapaActivity implements
		View.OnTouchListener {

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		title = getString(R.string.coney);
		setContentView(R.layout.coney);
		mySurfaceView = (ConeySurfaceView) this
				.findViewById(R.id.surface_view);
		badgeDrawable = (R.drawable.badge_coney_small);
		etapa = EtapaEnum.CONEY;
		genericInicialization();
	}
	
	@Override
	void setObjects() {
//deben ser los tama�os originales / 1.85                                                        x,y
		posiciones[0] = new LayoutParams(LayoutParams.WRAP_CONTENT,LayoutParams.WRAP_CONTENT, 352, -29); //Globo
		posiciones[1] = new LayoutParams(LayoutParams.WRAP_CONTENT,LayoutParams.WRAP_CONTENT,  13, 357); //Gorro
		posiciones[2] = new LayoutParams(LayoutParams.WRAP_CONTENT,LayoutParams.WRAP_CONTENT, 926, 400); //Cabritas
	
		//PONER EN EL ORDEN ANTERIOR !!
		drawables = new int[] {R.drawable.coney_globo,
				   R.drawable.coney_gorro,
				   R.drawable.coney_cabritas
		};
	}
		
	public static boolean visitedFlag = false;
	@Override
	public void setVisited(){visitedFlag = true;}
	public static boolean isVisited(){return visitedFlag;}
	
	@Override
	protected void setOverlay() {
		FrameLayout overlay = (FrameLayout) findViewById(R.id.overlay);
		overlay.setForeground(getResources().getDrawable(R.drawable.coney_overlay));
	}
}
