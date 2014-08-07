package com.didithemouse.didicol;

import java.util.ArrayList;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

public class LoadActivity extends Activity{
	private FrameLayout content = null;
	private DropPanelWrapper panel;
	private ImageView drawing = null;
	TextView inputText = null;
	Button terminar;
	Button tabInicio, tabDesarrollo, tabFin, tabEditado,tabOriginal,tabCorregido;
	int storyIndex=0; //0: inicio, 1:desarrollo, 2:fin
	MochilaContents mc = MochilaContents.getInstance();
	int textType=CORREGIDO;
	
	int currentLayout=0;
	
	RelativeLayout noteLayout,argLayout,drawLayout,descLayout;
	
	final static int objectSize = CreateActivity.objectSize;
	final static int ORIGINAL=0,EDITADO=1,CORREGIDO=2;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		// Remove title bar
		this.requestWindowFeature(Window.FEATURE_NO_TITLE);
		// Remove notification bar
		this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
				WindowManager.LayoutParams.FLAG_FULLSCREEN);

		setContentView(R.layout.load);
				
		content = (FrameLayout) findViewById(R.id.dibujoCanvas);
		content.setSystemUiVisibility(View.STATUS_BAR_HIDDEN);
		
		panel = MochilaContents.getInstance().getDropPanel();
		
		inputText = (TextView) findViewById(R.id.inputText);
		inputText.setClickable(false);
		inputText.setFocusable(false);
		inputText.setFocusableInTouchMode(false);
		
		tabInicio     = (Button) findViewById(R.id.tabinicio);
		tabDesarrollo = (Button) findViewById(R.id.tabdesarrollo);
		tabFin		  = (Button) findViewById(R.id.tabfin);
		
		noteLayout = (RelativeLayout) findViewById(R.id.notebookLayout);
		argLayout = (RelativeLayout) findViewById(R.id.argLayout);
		drawLayout = (RelativeLayout) findViewById(R.id.drawLayout);
		descLayout = (RelativeLayout) findViewById(R.id.descLayout);
		
		terminar = (Button) findViewById(R.id.terminar);	
		terminar.setOnClickListener(new View.OnClickListener() {
			boolean flag = true;
			public void onClick(View v) {
				if(!flag) return;
				flag = false;
				
				Intent i = new Intent(getApplicationContext(), EndingActivity.class);
				startActivity(i);
				finish();
			}
		});
		setStoryIndex(0);
		terminar.setClickable(true);
		
		ShowContent();
		
		tabEditado  = (Button) findViewById(R.id.tabeditado);
		tabOriginal = (Button) findViewById(R.id.taboriginal);
		tabCorregido = (Button) findViewById(R.id.tabcorregido);
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
			tabEditado.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View view) {
					setTextType(EDITADO);
				}
			});
			tabOriginal.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View view) {
					setTextType(ORIGINAL);
				}
			});		
			tabCorregido.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View view) {
					setTextType(CORREGIDO);
				}
			});	
			
		Button next = (Button) findViewById(R.id.next);	
		Button prev = (Button) findViewById(R.id.prev);	
		next.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				currentLayout = (currentLayout+1)%4;
				switchLayout();
			}
		});
		prev.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				currentLayout = Math.abs((currentLayout-1)%4);
				switchLayout();
			}
		});
		
		String argText="";
		for(String s: mc.getArgumentatorTexts()) {
			argText+=s;
			argText+="\n\n";
		}
		((EditText) findViewById(R.id.inputTextArg)).setText(argText);
		
		((TextView) findViewById(R.id.kidDataTitle)).
			setText("Datos del niño "+ mc.getKidName()+", su descripción fue:");
		((EditText) findViewById(R.id.inputTextDesc)).setText(mc.getDescription());
	
		ImageView imv = new ImageView(this);
		imv.setImageBitmap(mc.getDropPanel().getBitmap());
		((FrameLayout)findViewById(R.id.canvas_framelayout)).addView(imv);
		
		hidekeyboard();
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
					content.addView(img, lp);
				}
			}
			getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
			
	}
	 
	 public void setStoryIndex(int index)
	 {
		 //esconder/mostrar la tab segun corresponda
		 tabInicio.setBackgroundResource((index==0)? R.drawable.tabinicio : R.drawable.tabinicio_hidden);
		 tabDesarrollo.setBackgroundResource((index==1)? R.drawable.tabdesarrollo : R.drawable.tabdesarrollo_hidden);
		 tabFin.setBackgroundResource((index==2)? R.drawable.tabfin : R.drawable.tabfin_hidden);
		 if(textType==EDITADO) inputText.setText(mc.getTextEdited(index));
		 else if(textType==ORIGINAL) inputText.setText(mc.getTextOriginal(index));
		 else if(textType==CORREGIDO) inputText.setText(mc.getTextCorrected(index));
		 
		 storyIndex=index;
	 }
		
	 public void setTextType(int val){
		 textType = val;
		 if(val==EDITADO) tabEditado.setBackgroundResource(R.drawable.tabeditado);
		 else tabEditado.setBackgroundResource(R.drawable.tabeditado_hidden);
		 if(val==ORIGINAL) tabOriginal.setBackgroundResource(R.drawable.taboriginal);
		 else tabOriginal.setBackgroundResource(R.drawable.taboriginal_hidden);
		 if(val==CORREGIDO) tabCorregido.setBackgroundResource(R.drawable.tabcorregido);
		 else tabCorregido.setBackgroundResource(R.drawable.tabcorregido_hidden);
		 setStoryIndex(storyIndex);
	 }

	 public void switchLayout(){
		 if(currentLayout ==0) descLayout.setVisibility(View.VISIBLE);
		 else descLayout.setVisibility(View.INVISIBLE);
		 if(currentLayout ==1) noteLayout.setVisibility(View.VISIBLE);
		 else noteLayout.setVisibility(View.INVISIBLE);
		 if(currentLayout ==2) argLayout.setVisibility(View.VISIBLE);
		 else argLayout.setVisibility(View.INVISIBLE);
		 if(currentLayout ==3) drawLayout.setVisibility(View.VISIBLE);
		 else drawLayout.setVisibility(View.INVISIBLE);
	 }
	
	 void hidekeyboard(){
		 View ib = getCurrentFocus();
		 if(ib != null)
			((InputMethodManager) getSystemService(Activity.INPUT_METHOD_SERVICE)).
	        	hideSoftInputFromWindow(ib.getWindowToken(), 0);
	 }
	
	@Override
	public void onBackPressed() {
	}
}