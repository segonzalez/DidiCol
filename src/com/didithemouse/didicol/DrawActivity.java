package com.didithemouse.didicol;

import com.didithemouse.didicol.Saver.ActivityEnum;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.RelativeLayout;

public class DrawActivity extends Activity {
	
	private FingerPaint fp;
	MochilaContents mc = MochilaContents.getInstance();
	final static int canvasHeight = CreateActivity.canvasHeight;
	final static int canvasWidth = CreateActivity.canvasWidth;
	
	private RelativeLayout toolbar;
	private ImageButton[] toolbar_button;
	Button terminar;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		// Remove title bar
		this.requestWindowFeature(Window.FEATURE_NO_TITLE);
		// Remove notification bar
		this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
				WindowManager.LayoutParams.FLAG_FULLSCREEN);

		setContentView(R.layout.draw);
		
		FrameLayout fl = (FrameLayout) findViewById(R.id.canvas_framelayout);
		fp= new FingerPaint(this.getApplicationContext());
		fp.setBitmap(mc.getDropPanel().getBitmap());
		fl.addView(fp);
		fp.setMinimumWidth(canvasWidth);
		fp.setMinimumHeight(canvasHeight);
		
		/*Aqui seteamos fingerpaint */
		toolbar = (RelativeLayout) findViewById(R.id.canvas_toolbarlayout);
		
		toolbar_button = new ImageButton[]{(ImageButton)(toolbar.getChildAt(0)),
				(ImageButton)(toolbar.getChildAt(1))};
		
		//LAPIZ
		toolbar_button[0].setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				fp.setDraw();	
				changeSelectedToolbar(0);
			}
		});
		
		//Seteamos que hara el click de la GOMA
		toolbar_button[1].setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				fp.setErase();
				changeSelectedToolbar(1);
			    }
		});
		changeSelectedToolbar(0);
		terminar = (Button) findViewById(R.id.terminar);	
		terminar.setOnClickListener(new View.OnClickListener() {
			boolean flag = true;
			public void onClick(View v) {
				if(!flag) return;
				flag = false;
				
				Saver.savePresentation(ActivityEnum.END);
				Intent i = new Intent(getApplicationContext(), EndingActivity.class);
				startActivity(i);
				finish();
				
			}
		});
	}

	void changeSelectedToolbar(int index)
	{
		for (int i=0; i < toolbar_button.length; i++) 
			toolbar_button[i].setColorFilter(0);
		toolbar_button[index].setColorFilter(0xC00080FF);
	}
	

	
	@Override
	public void onBackPressed() {}
	
}
