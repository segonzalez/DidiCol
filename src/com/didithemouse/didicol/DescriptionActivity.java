package com.didithemouse.didicol;

import java.util.ArrayList;

import android.os.Bundle;
import android.app.Activity;
import android.content.Intent;

import com.didithemouse.didicol.R;
import com.didithemouse.didicol.network.NetEvent;
import com.didithemouse.didicol.network.NetManager.NetEventListener;

import android.view.KeyEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.TextView;

public class DescriptionActivity extends Activity {

	private FrameLayout letraCanvas = null;
	EditText inputText = null;
	TextView instruction=null;
	Button terminar;
	MochilaContents mc = MochilaContents.getInstance();
	
	final static int objectSize = CreateActivity.objectSize;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		// Remove title bar
		this.requestWindowFeature(Window.FEATURE_NO_TITLE);
		// Remove notification bar
		this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
				WindowManager.LayoutParams.FLAG_FULLSCREEN);

		setContentView(R.layout.description);
		
		MochilaContents mc = MochilaContents.getInstance();
		ArrayList<ViewWrapper> wrappers = mc.getItems();
		for(ViewWrapper w : wrappers){
			mc.getNetManager().sendMessage(new NetEvent(w.getDrawableID(),w.getScaleFactor(),w.getEtapa().toString()));
		}
		
		mc.getNetManager().setReadyListener(new NetEventListener() {
			@Override
			public void run(NetEvent ne, int fromClient) {
				if(fromClient == 0) kid1ready = true;
				else if(fromClient == 1) kid2ready = true;
				if(isWaiting && kid1ready && kid2ready)proceed();
			}
		});
		
		inputText = (EditText) findViewById(R.id.inputText);
		instruction=(TextView) findViewById(R.id.instruction);
		instruction.setClickable(false);
		instruction.setFocusable(false);
		instruction.setFocusableInTouchMode(false);

		inputText.setSystemUiVisibility(View.STATUS_BAR_HIDDEN);
		
		letraCanvas =(FrameLayout) findViewById(R.id.letraCanvas);
		
		terminar = (Button) findViewById(R.id.terminar);
		terminar.setOnClickListener(new View.OnClickListener() {
			boolean flag = true;
			public void onClick(View v) {
				if(inputText.getText().toString().equals("") || !flag) return;
				flag = false;
				finishEdit();
			}
		});
		terminar.setClickable(true);			
		
		inputText.setOnKeyListener(new View.OnKeyListener() {
			
			@Override
			public boolean onKey(View view, int i, KeyEvent keyevent) {
				terminar.setClickable(true);
				inputText.setOnKeyListener(null);
				return false;
			}
		});		
		
	}
	
	public void finishEdit()
	{
		ShowContent();

		mc.mergeNetItems();
		InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(Activity.INPUT_METHOD_SERVICE);
        inputMethodManager.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), 0);
		
		MochilaContents.getInstance().setDescription(inputText.getText().toString());
		instruction.setText("Apóyate en lo que escribiste para contarle a tu grupo lo que leíste, además abajo están los objetos que recogiste.");
		inputText.setClickable(false);
		inputText.setFocusable(false);
		inputText.setFocusableInTouchMode(false);
				
		terminar.setOnClickListener(new View.OnClickListener() {
			boolean flag = true;
			public void onClick(View v) {
				if(!flag) return;
				flag = false;
				isWaiting = true;
				mc.getNetManager().sendMessage(new NetEvent("description",true) );
				terminar.setBackgroundResource(R.drawable.flechaespera);
				if(kid1ready && kid2ready)proceed();				
			}
		});
	}
	
	 public void ShowContent() {
	    	ArrayList<ViewWrapper> wrappers = mc.getItems();

			int left=350;
			for(ViewWrapper w : wrappers){
				w.destroyView();
				View iv = w.getView(getApplicationContext());
				if(iv==null) continue;
				if(iv instanceof ExtendedImageView){
					ExtendedImageView img = (ExtendedImageView)iv;
					FrameLayout.LayoutParams lp = new FrameLayout.LayoutParams(objectSize, objectSize);
					lp.leftMargin = left;
					lp.topMargin = 350;
					left+=130;
					letraCanvas.addView(img, lp);
				}
			}
			getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
			
	}

		
	@Override
	public void onBackPressed() {}

	boolean isWaiting = false;
	boolean kid1ready=false, kid2ready = false;
	public void proceed(){
		mc.getNetManager().setReadyListener(null);
		Intent i = new Intent(getApplicationContext(), CreateActivity.class);
		//LogX.i("Create","Ha comenzado la create.");
		startActivity(i);
		finish();
	}
}
