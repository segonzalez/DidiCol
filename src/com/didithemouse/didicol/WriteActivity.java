package com.didithemouse.didicol;

import java.util.ArrayList;

import android.app.Activity;
import android.content.Intent;
import android.graphics.PorterDuff;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import com.didithemouse.didicol.etapas.EtapaActivity.EtapaEnum;
import com.didithemouse.didicol.network.NetEvent;
import com.didithemouse.didicol.network.NetManager.NetEventListener;

public class WriteActivity extends Activity {

	private FrameLayout content = null;
	private DropPanelWrapper panel = null;
	private ImageView drawing = null;
	EditText inputText = null;
	Button terminar;
	Button tabInicio, tabDesarrollo, tabFin;
	int storyIndex=0; //0: inicio, 1:desarrollo, 2:fin
	int fixedStoryIndex=0;
	MochilaContents mc = MochilaContents.getInstance();
	TextView instruction=null;
	boolean isWaiting = false;
		
	final static int objectSize = CreateActivity.objectSize;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		// Remove title bar
		this.requestWindowFeature(Window.FEATURE_NO_TITLE);
		// Remove notification bar
		this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
				WindowManager.LayoutParams.FLAG_FULLSCREEN);

		setContentView(R.layout.write);
		
		content = (FrameLayout) findViewById(R.id.dibujoCanvas);
		drawing = (ImageView) findViewById(R.id.bitmapDraw);
		content.setSystemUiVisibility(View.STATUS_BAR_HIDDEN);
		
		panel = MochilaContents.getInstance().getDropPanel();
		
		inputText = (EditText) findViewById(R.id.inputText);
		
		View bg = findViewById(R.id.root);
		bg.setOnTouchListener(new View.OnTouchListener() {
		    @Override
		    public boolean onTouch(View v, MotionEvent event) {
				hidekeyboard();return false;
			}
		});	
		
		mc.getNetManager().setReadyListener(new NetEventListener() {
			@Override
			public void run(NetEvent ne, int fromClient) {
				if(fromClient == 0) kid1ready = ne.cond;
				else if(fromClient == 1) kid2ready = ne.cond;
				if(isWaiting2 && kid1ready && kid2ready)proceed();
			}
		});
		
		terminar = (Button) findViewById(R.id.terminar);
		terminar.setOnClickListener(new View.OnClickListener() {
			boolean flag = true;
			public void onClick(View v) {
				if(inputText.getText().toString().equals("") || !flag) return;
				flag = false;
				terminarClickListener1();
			}
		});
		terminar.setClickable(true);
		
		instruction=(TextView) findViewById(R.id.instruction);
		instruction.setClickable(false);
		instruction.setFocusable(false);
		instruction.setFocusableInTouchMode(false);
				
		tabInicio     = (Button) findViewById(R.id.tabinicio);
		tabDesarrollo = (Button) findViewById(R.id.tabdesarrollo);
		tabFin		  = (Button) findViewById(R.id.tabfin);
		
		tabInicio.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				setStoryIndex(0);
			}
		});
		tabDesarrollo.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				setStoryIndex(1);
			}
		});
		tabFin.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				setStoryIndex(2);
			}
		});
		
		tabInicio.setVisibility(View.INVISIBLE);
		tabDesarrollo.setVisibility(View.INVISIBLE);
		tabFin.setVisibility(View.INVISIBLE);
		
		inputText.setCursorVisible(true);
		
		inputText.setOnKeyListener(new View.OnKeyListener() {
			
			@Override
			public boolean onKey(View view, int i, KeyEvent keyevent) {
				terminar.setClickable(true);
				inputText.setOnKeyListener(null);
				return false;
			}
		});
		
		
		
		ShowContent();
		
		storyIndex = mc.OBJETOS;
		for(ViewWrapper w :panel.getWrappers())
		{
			if(w.getEtapa() != mc.getEtapa(mc.OBJETOS)) continue;
			if(w.getX() <0.33) storyIndex = 0;
			else if(w.getX()>0.33 && w.getX()<0.66) storyIndex = 1;
			else if(w.getX()>0.66) storyIndex = 2;
			break;
		}
		
		fixedStoryIndex = storyIndex;
		
		String ins = "";
		if(storyIndex ==0){ins = "inicio";	}
		else if(storyIndex ==1){ins = "desarrollo";	}
		else if(storyIndex ==2){ins = "final";	}
		
		instruction.setText("Escribe el "+ins+" de la historia.");
		
		inputText.setText(mc.getTextEdited(storyIndex));
		setStoryIndex(storyIndex);
		
		mc.getNetManager().setTextListener(new NetEventListener() {
			@Override
			public void run(NetEvent ne, int indx) {
				mc.setTextEdited(ne.i1, ne.message);
				if(!isWaiting) return;
				for(int i =0; i<3; i++){
					if(i!=storyIndex && mc.getTextEdited(i).equals("")) return;
				}
				terminar.getHandler().post(new Runnable() {
					@Override
					public void run() {
						setRevisar();
					}
				});
			}
		});
		
	}
	
	 String instructionTemp = "";
	 void terminarClickListener1(){
		 isWaiting = true;
		hidekeyboard();
		inputText.setClickable(false);
		inputText.setFocusable(false);
		inputText.setFocusableInTouchMode(false);
			
		mc.getNetManager().sendMessage(new NetEvent(storyIndex,inputText.getText().toString(),0));
		mc.setTextEdited(storyIndex,inputText.getText().toString() ); 
		instructionTemp=instruction.getText().toString();
		instruction.setText("Espere a los demás niños.");
		terminar.setBackgroundResource(R.drawable.flechaespera);
		
		terminar.setOnClickListener(new View.OnClickListener() {
			boolean flag = true;
			public void onClick(View v) {
				if(!flag) return; flag = false;
				terminarCancel1();
			}
		});
			
		//Si algun texto es "", no cambiar a Revisar con el handler.
		for(int i =0; i<3; i++){
			if(i!=storyIndex && mc.getTextEdited(i).equals("")) return;
		}
		terminar.getHandler().post(new Runnable() {
			@Override
			public void run() {
				setRevisar();
			}
		});
	 }
	 
	 void terminarCancel1(){
		isWaiting = false;
		setStoryIndex(fixedStoryIndex);
		mc.getNetManager().sendMessage(new NetEvent(storyIndex,"",0));
		instruction.setText(instructionTemp);
		terminar.setBackgroundResource(R.drawable.flecha);
		terminar.setOnClickListener(new View.OnClickListener() {
			boolean flag = true;
			public void onClick(View v) {
				if(!flag) return; flag = false;
				terminarClickListener1();
			}
		});
	 }
	
	 public void setRevisar(){
		 isWaiting = false;
		 mc.cloneTextsOriginal();
		 mc.getNetManager().setTextListener(new NetEventListener() {
				@Override
				public void run(NetEvent ne, int indx) {
					mc.setTextEdited(ne.i1, ne.message);
				}
		});
		 
		for(ViewWrapper w :panel.getWrappers())
		{
			if(w.getEtapa() != mc.getEtapa(mc.TEXTO)) continue;
			if(w.getX() <0.33) fixedStoryIndex = 0;
			else if(w.getX()>0.33 && w.getX()<0.66) fixedStoryIndex = 1;
			else if(w.getX()>0.66) fixedStoryIndex = 2;
			break;
		}
		
		int filter = 0;
		if(mc.getEtapa(mc.OBJETOS) == EtapaEnum.INICIO) filter = ( 0xFF008000 );
		else if(mc.getEtapa(mc.OBJETOS) == EtapaEnum.CHINA) filter = (0xFFFF4838);
		else if(mc.getEtapa(mc.OBJETOS) == EtapaEnum.CONEY) filter = (0xFF3848FF);
		if(fixedStoryIndex==0) filterInicio=filter;
		else if(fixedStoryIndex==1) filterDes=filter;
		else if(fixedStoryIndex==2) filterFin=filter;
		
		 setStoryIndex(fixedStoryIndex);
		 terminar.setBackgroundResource(R.drawable.flecha);
		 
		 String ins = "";
		if(fixedStoryIndex ==0){ins = "inicio";	}
		else if(fixedStoryIndex ==1){ins = "desarrollo";	}
		else if(fixedStoryIndex ==2){ins = "final";	}
			
		instruction.setText("Revisa el "+ins+" de la historia.");
			

		tabInicio.setVisibility(View.VISIBLE);
		tabDesarrollo.setVisibility(View.VISIBLE);
		tabFin.setVisibility(View.VISIBLE);
		terminar.setOnClickListener(new View.OnClickListener() {
			boolean flag = true;
			public void onClick(View v) {
				if(!flag) return;
				flag = false;
				terminarClickListener2();
			}
		});
	 }
	 
	 void terminarClickListener2(){
		isWaiting2 = true;
		if(fixedStoryIndex==storyIndex)
			mc.setTextEdited(fixedStoryIndex,inputText.getText().toString());
		mc.getNetManager().sendMessage(new NetEvent(fixedStoryIndex,mc.getTextEdited(fixedStoryIndex),0));
		mc.getNetManager().sendMessage(new NetEvent("write",true) );
			
		inputText.setClickable(false);
		inputText.setFocusable(false);
		inputText.setFocusableInTouchMode(false);
		
		instructionTemp=instruction.getText().toString();
		instruction.setText("Espere a los demás niños.");
			
		terminar.setBackgroundResource(R.drawable.flechaespera);
		if(kid1ready && kid2ready)proceed();
		else
		terminar.setOnClickListener(new View.OnClickListener() {
			boolean flag = true;
			public void onClick(View v) {
				if(!flag) return; flag = false;
				terminarCancel2();
			}
		});
	 }
	 
	 void terminarCancel2(){
		mc.getNetManager().sendMessage(new NetEvent("write",false) );
		isWaiting2 = false;
		inputText.setClickable(true);
		inputText.setFocusable(true);
		inputText.setFocusableInTouchMode(true);
		instruction.setText(instructionTemp);
		terminar.setBackgroundResource(R.drawable.flecha);
		terminar.setOnClickListener(new View.OnClickListener() {
			boolean flag = true;
			public void onClick(View v) {
				if(!flag) return; flag = false;
				terminarClickListener2();
			}
		});
	 }
	 

	 int filterInicio=0xFFFFFFFF,filterDes=0xFFFFFFFF, filterFin=0xFFFFFFFF;
	 public void setStoryIndex(int index)
	 {

		 //esconder/mostrar la tab segun corresponda
		 tabInicio.setBackgroundResource((index==0)? R.drawable.tabinicio_gray : R.drawable.tabinicio_hidden_gray);
		 tabDesarrollo.setBackgroundResource((index==1)? R.drawable.tabdesarrollo_gray : R.drawable.tabdesarrollo_hidden_gray);
		 tabFin.setBackgroundResource((index==2)? R.drawable.tabfin_gray : R.drawable.tabfin_hidden_gray);
		 		 
		 tabInicio.getBackground().setColorFilter(filterInicio,PorterDuff.Mode.MULTIPLY );
		 tabDesarrollo.getBackground().setColorFilter(filterDes,PorterDuff.Mode.MULTIPLY );
		 tabFin.getBackground().setColorFilter(filterFin,PorterDuff.Mode.MULTIPLY );
		 
		 if(index != fixedStoryIndex){
			 hidekeyboard();
			inputText.setClickable(false);
			inputText.setFocusable(false);
			inputText.setFocusableInTouchMode(false);
		 }
		 else{
			inputText.setClickable(true);
			inputText.setFocusable(true);
			inputText.setFocusableInTouchMode(true);
			inputText.setCursorVisible(true);
			inputText.requestFocusFromTouch();
		 }
		 
		 mc.setTextEdited(storyIndex,inputText.getText().toString() ); 
		 inputText.setText(mc.getTextEdited(index));
		 storyIndex=index;
		 
	 }
	 public void ShowContent() {
	    	content.removeAllViews();
			DropPanelWrapper p1 = panel;
	    	ArrayList<ViewWrapper> wrappers = p1.getWrappers();
			for(ViewWrapper w : wrappers){
				w.destroyView();
				View iv = w.getView(getApplicationContext());
				if(iv==null) continue;
				int left=0, top=0;
				if(iv instanceof ExtendedImageView){
					ExtendedImageView img = (ExtendedImageView)iv;
					left = (int)(w.getX()*CreateActivity.canvasWidth_mid);
					top = (int)(w.getY()*CreateActivity.canvasHeight_mid);
					FrameLayout.LayoutParams lp = new FrameLayout.LayoutParams((int)(objectSize*(CreateActivity.canvasWidth_mid/810f)), (int)(objectSize*(CreateActivity.canvasWidth_mid/810f)));
					lp.leftMargin = left;
					lp.topMargin = top;
					//if(img.getEtapa() == EtapaEnum.EMPIRE) img.setBackgroundResource(R.drawable.borderojo);
					//else if(img.getEtapa() == EtapaEnum.INICIO) img.setBackgroundResource(R.drawable.bordeazul);
					content.addView(img, lp);
				}
			}
			drawing.setImageDrawable(p1.getPanelView(this).getMediumBitmap());
			content.addView(drawing);
			getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
			
	}
	 
	 void hidekeyboard(){
		 View ib = getCurrentFocus();
		 if(ib != null)
			((InputMethodManager) getSystemService(Activity.INPUT_METHOD_SERVICE)).
	        	hideSoftInputFromWindow(ib.getWindowToken(), 0);
	 }

		
	@Override
	public void onBackPressed() {}

	
	boolean isWaiting2 = false;
	boolean kid1ready=false, kid2ready = false;
	public void proceed(){
		mc.getNetManager().setReadyListener(null);
		mc.getNetManager().setTextListener(null);

		Intent i = new Intent(getApplicationContext(), PresentationActivity.class);
		//LogX.i("Write","Ha comenzado la presentación.");
		startActivity(i);
		finish();
	}
}