package com.didithemouse.didicol.etapas;

import com.didithemouse.didicol.R;
import com.didithemouse.didicol.MyAbsoluteLayout.LayoutParams;

import android.os.Bundle;

import android.view.View;
import android.widget.FrameLayout;

public class ChinatownActivity extends EtapaActivity implements
		View.OnTouchListener {
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		title =  getString(R.string.chinatown);
		setContentView(R.layout.chinatown);
		mySurfaceView = (ChinatownSurfaceView) this.findViewById(R.id.surface_view);
		badgeDrawable = (R.drawable.badge_chinatown_small);
		etapa = EtapaEnum.CHINA;
		genericInicialization();
	}
	
	public static boolean visitedFlag = false;
	@Override
	public void setVisited(){visitedFlag = true;}
	public static boolean isVisited(){return visitedFlag;}
	
	@Override
	void setObjects() {
		
		posiciones[0] = new LayoutParams(LayoutParams.WRAP_CONTENT,LayoutParams.WRAP_CONTENT, 925 ,  560); //Arroz
		posiciones[1] = new LayoutParams(LayoutParams.WRAP_CONTENT,LayoutParams.WRAP_CONTENT, 975 ,  231); //Palitos
		posiciones[2] = new LayoutParams(LayoutParams.WRAP_CONTENT,LayoutParams.WRAP_CONTENT, 1087, -170); //Lampara
		
		//PONER EN EL ORDEN ANTERIOR !!
		drawables = new int[] {R.drawable.chinatown_arroz,
				   R.drawable.chinatown_palitos,
				   R.drawable.chinatown_lampara
		};
	}
	
	@Override
	protected void setOverlay() {
		FrameLayout overlay = (FrameLayout) findViewById(R.id.overlay);
		overlay.setForeground(getResources().getDrawable(R.drawable.chinatown_overlay));
	}
	
}
