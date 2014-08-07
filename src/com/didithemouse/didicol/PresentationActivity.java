package com.didithemouse.didicol;

import java.util.ArrayList;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import com.didithemouse.didicol.network.NetEvent;
import com.didithemouse.didicol.network.NetManager.NetEventListener;

public class PresentationActivity extends Activity{
	private FrameLayout content = null;
	private DropPanelWrapper panel;
	private ImageView drawing = null;
	TextView inputText = null;
	Button terminar;
	Button tabInicio, tabDesarrollo, tabFin, tabEditado,tabOriginal;
	int storyIndex=0; //0: inicio, 1:desarrollo, 2:fin
	int substoryIndex=0; //indexa páginas de inicio, desarrollo o fin
	MochilaContents mc = MochilaContents.getInstance();
	boolean mostrarOriginal=false;
	
	final static int objectSize = CreateActivity.objectSize;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		// Remove title bar
		this.requestWindowFeature(Window.FEATURE_NO_TITLE);
		// Remove notification bar
		this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
				WindowManager.LayoutParams.FLAG_FULLSCREEN);

		setContentView(R.layout.presentation);
		
		if(!mc.hasLoaded)
		mc.getNetManager().setTextListener(null);
		
		content = (FrameLayout) findViewById(R.id.dibujoCanvas);
		drawing = (ImageView) findViewById(R.id.bitmapDraw);
		content.setSystemUiVisibility(View.STATUS_BAR_HIDDEN);
		
		panel = MochilaContents.getInstance().getDropPanel();
		
		inputText = (TextView) findViewById(R.id.inputText);
		inputText.setClickable(false);
		inputText.setFocusable(false);
		inputText.setFocusableInTouchMode(false);
		
		tabInicio     = (Button) findViewById(R.id.tabinicio);
		tabDesarrollo = (Button) findViewById(R.id.tabdesarrollo);
		tabFin		  = (Button) findViewById(R.id.tabfin);
		
		if(!mc.hasLoaded)
		mc.getNetManager().setReadyListener(new NetEventListener() {
			@Override
			public void run(NetEvent ne, int fromClient) {
				changetab();
			}
		});
		
		terminar = (Button) findViewById(R.id.terminar);	
		terminar.setOnClickListener(new View.OnClickListener() {
			boolean flag = true;
			public void onClick(View v) {
				if(!flag) return;
				flag = false;
				
				if(!mc.hasLoaded){
					changetab();
					mc.getNetManager().sendMessage(new NetEvent("pres",true));
				}
				else{
					Intent i = new Intent(getApplicationContext(), EndingActivity.class);
					startActivity(i);
					finish();
				}
			}
		});
		setStoryIndex(0);
		if(mc.hasLoaded || mc.getEtapa(mc.LECTURA).ordinal()%3 == storyIndex){
			terminar.setClickable(true);
		}
		else{
			terminar.setBackgroundResource(R.drawable.flechaespera);
			terminar.setClickable(false);
		}		
		
		if(mc.hasLoaded){
			ShowContent();
		}
		else{
			content.setBackgroundResource(0);
			setInstruction();
		}
		
		
		mostrarOriginal=false;
		tabEditado  = (Button) findViewById(R.id.tabeditado);
		tabOriginal = (Button) findViewById(R.id.taboriginal);
		if(mc.hasLoaded)
		{

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
					setOriginal(false);
				}
			});
			tabOriginal.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View view) {
					setOriginal(true);
				}
			});
		}
		else
		{
			tabEditado.setVisibility(View.INVISIBLE);
			tabOriginal.setVisibility(View.INVISIBLE);
		}				
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
	 
	 public void setStoryIndex(int index)
	 {
		 //esconder/mostrar la tab segun corresponda
		 tabInicio.setBackgroundResource((index==0)? R.drawable.tabinicio : R.drawable.tabinicio_hidden);
		 tabDesarrollo.setBackgroundResource((index==1)? R.drawable.tabdesarrollo : R.drawable.tabdesarrollo_hidden);
		 tabFin.setBackgroundResource((index==2)? R.drawable.tabfin : R.drawable.tabfin_hidden);
		 
		 inputText.setText(mostrarOriginal?mc.getTextOriginal(index):mc.getTextEdited(index));
		 
		 int color = 0xFFFFFFFF;
		 if(index == 0) {color = (  0xFFFF4838 ); }
		 else if (index ==1) {color = (0xFF3848FF);}
		 else if (index ==2) {color = (0xFF008000);}
		 inputText.setTextColor(color);
		 
		 if(!mc.hasLoaded)
		 {if(index == 0) {drawing.setImageResource(R.drawable.fondoinicio);}
		 else if (index ==1) {drawing.setImageResource(R.drawable.fondochina);}
		 else if (index ==2) {drawing.setImageResource(R.drawable.fondoconey);}
		 }
		 storyIndex=index;
		 substoryIndex=0;
	 }
		
	 public void setOriginal(boolean val){
		 mostrarOriginal=val;
		 tabEditado.setBackgroundResource((!val)? R.drawable.tabeditado : R.drawable.tabeditado_hidden);
		 tabOriginal.setBackgroundResource(val? R.drawable.taboriginal : R.drawable.taboriginal_hidden);
		 setStoryIndex(storyIndex);
	 }

				
		
	boolean isWaiting = false;
	boolean kid1ready=false, kid2ready = false;
	public void changetab(){
		storyIndex = storyIndex+1;
		if(mc.getEtapa(mc.LECTURA).ordinal()%3 == storyIndex){
			terminar.setClickable(true);
			terminar.setBackgroundResource(R.drawable.flecha);
		}
		else{
			terminar.setBackgroundResource(R.drawable.flechaespera);
			terminar.setClickable(false);
		}
		setStoryIndex(storyIndex);
		setInstruction();
		
		if(storyIndex == 2){
			terminar.setClickable(true);
			terminar.setBackgroundResource(R.drawable.flecha);
			mc.getNetManager().setReadyListener(new NetEventListener() {
				@Override
				public void run(NetEvent ne, int fromClient) {
					if(fromClient == 0) kid1ready = true;
					else if(fromClient == 1) kid2ready = true;
					if(isWaiting && kid1ready && kid2ready)proceed();
				}
			});
			terminar.setOnClickListener(new View.OnClickListener() {
				boolean flag = true;
				public void onClick(View v) {
					if(!flag) return;
					flag = false;
					
					isWaiting = true;
					mc.getNetManager().sendMessage(new NetEvent("write",true) );
					terminar.setBackgroundResource(R.drawable.flechaespera);
					if(kid1ready && kid2ready)proceed();	
				}
			});
		}
		
	}
	
	public void setInstruction(){
		String name = "";
		if(mc.getEtapa(mc.LECTURA).ordinal()%3 == storyIndex)
			name=mc.getKidName();
		else if(storyIndex == mc.getNetManager().getKidEtapa(0,mc.LECTURA).ordinal()%3)
			name = mc.getNetManager().getKidName(0);
		else if(storyIndex == mc.getNetManager().getKidEtapa(1,mc.LECTURA).ordinal()%3)
			name = mc.getNetManager().getKidName(1);	
		((TextView)findViewById(R.id.instruction)).setText("Ahora lee: " + name);
		
	}
	
	public void proceed(){
		mc.getNetManager().setReadyListener(null);
		Intent i = new Intent(getApplicationContext(), ArgumentatorActivity.class);
		
		startActivity(i);
		finish();
	}
	
	@Override
	public void onBackPressed() {
	}
}