package com.didithemouse.didicol.etapas;

import android.os.Bundle;
import android.view.View;
import android.widget.FrameLayout;

import com.didithemouse.didicol.MyAbsoluteLayout.LayoutParams;
import com.didithemouse.didicol.R;

public class EmpireStateActivity extends EtapaActivity implements
		View.OnTouchListener {

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		title = getString(R.string.empirestate);
		setContentView(R.layout.empirestate);
		mySurfaceView = (EmpireStateSurfaceView) this
				.findViewById(R.id.surface_view);
		badgeDrawable = (R.drawable.badge_empirestate_small);
		etapa = EtapaEnum.EMPIRE;
		genericInicialization();
	}
	
	@Override
	void setObjects() {
		//deben ser los tama�os originales / 1.85                                                        x,y
				posiciones[0] = new LayoutParams(LayoutParams.WRAP_CONTENT,LayoutParams.WRAP_CONTENT, 458, 663); //Taxi
				posiciones[1] = new LayoutParams(LayoutParams.WRAP_CONTENT,LayoutParams.WRAP_CONTENT, 163, 516); //Semaforo	
				posiciones[2] = new LayoutParams(LayoutParams.WRAP_CONTENT,LayoutParams.WRAP_CONTENT, 141, 275); //Bandera
				//posiciones[3] = new LayoutParams(75, 239, 920, 450);
	
				//PONER EN EL ORDEN ANTERIOR !!
				drawables = new int[] {R.drawable.empirestate_taxi,
						   R.drawable.empirestate_semaforo,
						   R.drawable.empirestate_bandera
				};
	}
	
	public static boolean visitedFlag = false;
	@Override
	public void setVisited(){visitedFlag = true;}
	public static boolean isVisited(){return visitedFlag;}
	
	
	@Override
	protected void setOverlay() {
		FrameLayout overlay = (FrameLayout) findViewById(R.id.overlay);
		overlay.setForeground(getResources().getDrawable(R.drawable.empirestate_overlay));
	}
}
