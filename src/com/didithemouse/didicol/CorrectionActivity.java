package com.didithemouse.didicol;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.didithemouse.didicol.etapas.EtapaActivity.EtapaEnum;
import com.didithemouse.didicol.network.NetEvent;
import com.didithemouse.didicol.network.NetManager.NetEventListener;

public class CorrectionActivity extends Activity {

	EditText inputText = null;
	TextView argText = null;
	Button terminar;
	MochilaContents mc = MochilaContents.getInstance();
	public int argIndex=0,storyIndex=0;
	Button tabInicio, tabDesarrollo, tabFin;
	
	String []argumentatorTexts = mc.getArgumentatorTexts();
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		// Remove title bar
		this.requestWindowFeature(Window.FEATURE_NO_TITLE);
		// Remove notification bar
		this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
				WindowManager.LayoutParams.FLAG_FULLSCREEN);

		setContentView(R.layout.correction);
		inputText = (EditText) findViewById(R.id.inputText);
		argText = (TextView) findViewById(R.id.argText_);
		argumentatorTexts = mc.getArgumentatorTexts();
		
		mc.getNetManager().setReadyListener(new NetEventListener() {
			@Override
			public void run(NetEvent ne, int fromClient) {
				if(fromClient == 0) kid1ready = true;
				else if(fromClient == 1) kid2ready = true;
				if(isWaiting && kid1ready && kid2ready)changekid();
			}
		});
		
		tabInicio     = (Button) findViewById(R.id.tabinicio);
		tabDesarrollo = (Button) findViewById(R.id.tabdesarrollo);
		tabFin		  = (Button) findViewById(R.id.tabfin);
		
		tabInicio.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				setStoryIndex(0,false);
			}
		});
		tabDesarrollo.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				setStoryIndex(1,false);
			}
		});
		tabFin.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				setStoryIndex(2,false);
			}
		});
		
		View bg = findViewById(R.id.root);
		bg.setOnTouchListener(new View.OnTouchListener() {
		    @Override
		    public boolean onTouch(View v, MotionEvent event) {
				hidekeyboard();return false;
			}
		});	
		
		inputText.setSystemUiVisibility(View.STATUS_BAR_HIDDEN);
		inputText.setCursorVisible(true);

		mc.cloneTextsCorrected();
		inputText.setText(mc.getTextCorrected(0));
		inputText.addTextChangedListener(new TextWatcher() {
			boolean flag = false;
			@Override
			public void onTextChanged(CharSequence s, int start, int before, int count) {
				if(flag|| ignoreNextTextChange || argIndex != mc.getEtapa(mc.TEXTO).ordinal()%3) return;
				flag=false;
				mc.getNetManager().sendMessage(new NetEvent(before,s.subSequence(start, start+count).toString(),start));
			}
			@Override
			public void beforeTextChanged(CharSequence s, int start, int count,	int after) {}
			@Override
			public void afterTextChanged(Editable s) {}
		});
		
		if(argIndex != mc.getEtapa(mc.TEXTO).ordinal()%3){
			hidekeyboard();
			inputText.setFocusable(false);
			inputText.setFocusableInTouchMode(false);		
		 }
				
		terminar = (Button) findViewById(R.id.terminar);
		terminar.setOnClickListener(new View.OnClickListener() {
			boolean flag = true;
			public void onClick(View v) {
				if(!flag) return;
				flag = false;
				isWaiting=true;
				mc.getNetManager().sendMessage(new NetEvent("argumentator",true) );
				terminar.setBackgroundResource(R.drawable.flechaespera);
				if(kid1ready && kid2ready)changekid();				
			}
		});	
		
		mc.getNetManager().setTextListener(new NetEventListener() {
			@Override
			public void run(NetEvent ne, int fromClient) {
				
				if(ne.i2 >=0){
					inputText.getText().insert(ne.i2, ne.message);
					if(ne.message.equals("")){
						if(ne.i2>=ne.i1-1)inputText.getText().delete(ne.i2-ne.i1+1, ne.i2+1);
						else inputText.getText().delete(0, ne.i1);
					}
					
				}
				else{
					mc.setTextCorrected(ne.i1,ne.message);
					setStoryIndex(-ne.i2-1, true);
				}
			}
		});
		
		setInstruction();
		argText.setText(argumentatorTexts[argIndex%3]);
	}
	
	
	 boolean ignoreNextTextChange = false;
	 public void setStoryIndex(int index, boolean isEvent)
	 {
		 if(!isEvent && argIndex != mc.getEtapa(mc.TEXTO).ordinal()%3)
		 	{return;}
		 //esconder/mostrar la tab segun corresponda
		 tabInicio.setBackgroundResource((index==0)? R.drawable.tabinicio : R.drawable.tabinicio_hidden);
		 tabDesarrollo.setBackgroundResource((index==1)? R.drawable.tabdesarrollo : R.drawable.tabdesarrollo_hidden);
		 tabFin.setBackgroundResource((index==2)? R.drawable.tabfin : R.drawable.tabfin_hidden);
		
		 inputText.requestFocusFromTouch();
		 
		 if(!isEvent){
			 mc.getNetManager().sendMessage(new NetEvent(storyIndex,inputText.getText().toString(),-index-1));
		 }
		 mc.setTextCorrected(storyIndex,inputText.getText().toString() );
		 ignoreNextTextChange=true;
		 inputText.setText(mc.getTextCorrected(index));
		 ignoreNextTextChange=false;
		 storyIndex=index;
		 
	 }
	

	boolean isWaiting = false;
	boolean kid1ready=false, kid2ready = false;
	public void changekid(){
		isWaiting=false;kid1ready=false; kid2ready=false;
		
		argIndex++;	
		if(argIndex == mc.getEtapa(mc.TEXTO).ordinal()%3){
			inputText.setFocusable(true);
			inputText.setFocusableInTouchMode(true);	
			inputText.requestFocusFromTouch();
			showkeyboard();
		 }
		 else{
			hidekeyboard();
			inputText.setFocusable(false);
			inputText.setFocusableInTouchMode(false);
		 }
		
		if(argIndex == 3) {proceed();}
		else{
			setInstruction();
			argText.setText(argumentatorTexts[argIndex%3]);
		
			terminar.setBackgroundResource(R.drawable.flecha);
			terminar.setOnClickListener(new View.OnClickListener() {
			boolean flag = true;
			public void onClick(View v) {
					if(!flag) return;
					flag = false;
				
					isWaiting = true;
					mc.getNetManager().sendMessage(new NetEvent("argumentator",true) );
					terminar.setBackgroundResource(R.drawable.flechaespera);
					if(kid1ready && kid2ready)changekid();				
				}
			});
		}
	}
	
	TextView insTextView;
	public void setInstruction(){
		if(insTextView==null ) insTextView=((TextView)findViewById(R.id.instruction));
		String name = "";
		if(mc.getEtapa(mc.TEXTO).ordinal()%3 == argIndex)
			name=mc.getKidName();
		else if(argIndex == mc.getNetManager().getKidEtapa(0,mc.TEXTO).ordinal()%3)
			name = mc.getNetManager().getKidName(0);
		else if(argIndex == mc.getNetManager().getKidEtapa(1,mc.TEXTO).ordinal()%3)
			name = mc.getNetManager().getKidName(1);	
		
		if(argIndex==0)
			insTextView.
			setText(name+": Usando el argumentador, arregla la ortografía del texto.");
		else if(argIndex==1)
			insTextView.
			setText(name+": Usando el argumentador, arregla el vocabulario del texto.");
		else if(argIndex==2)
			insTextView.
			setText(name+": Usando el argumentador, arregla el uso de verbos del texto.");
	
		if(mc.getEtapa(mc.TEXTO).ordinal()%3 == argIndex){
			int filter = 0;
			if(mc.getEtapa(mc.OBJETOS) == EtapaEnum.INICIO) filter = ( 0xFF008000 );
			else if(mc.getEtapa(mc.OBJETOS) == EtapaEnum.CHINA) filter = (0xFFFF4838);
			else if(mc.getEtapa(mc.OBJETOS) == EtapaEnum.CONEY) filter = (0xFF3848FF);
			insTextView.setTextColor(filter);
		}
		else 
			insTextView.setTextColor(Color.BLACK);
	}

	public void proceed(){
		mc.setTextCorrected(storyIndex,inputText.getText().toString() );
		
		mc.getNetManager().setReadyListener(null);
		mc.getNetManager().setArgListener(null);;
		Intent i = new Intent(getApplicationContext(), DrawActivity.class);
		startActivity(i);
		finish();
	}
	
	@Override
	public void onBackPressed() {}
	
	 void hidekeyboard(){
		 View ib = getCurrentFocus();
		 if(ib != null)
			((InputMethodManager) getSystemService(Activity.INPUT_METHOD_SERVICE)).
	        	hideSoftInputFromWindow(ib.getWindowToken(), 0);
	 }
	 void showkeyboard(){
		 View ib = getCurrentFocus();
		 if(ib != null)
			((InputMethodManager) getSystemService(Activity.INPUT_METHOD_SERVICE)).
			showSoftInput(ib, InputMethodManager.SHOW_IMPLICIT);
	 }
}
