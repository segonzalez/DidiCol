package com.didithemouse.didicol;


import java.util.Random;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.text.InputType;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.didithemouse.didicol.Saver.ActivityEnum;
import com.didithemouse.didicol.etapas.ChinatownActivity;
import com.didithemouse.didicol.etapas.ConeyActivity;
import com.didithemouse.didicol.etapas.EtapaActivity.EtapaEnum;
import com.didithemouse.didicol.etapas.InicioActivity;
import com.didithemouse.didicol.network.NetEvent;
import com.didithemouse.didicol.network.NetManager.NetEventListener;

public class Principal extends Activity {
  
	EditText etNumber;
	EditText etName;
	EditText etGroup;
	Button cargar;
	Button comenzar;
	MochilaContents mc = MochilaContents.getInstance();
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
		
		setContentView(R.layout.principal);

		cleanup();
		
		View vcomenzar = findViewById(R.id.iniciars);
		etNumber = (EditText) findViewById(R.id.numKidText);
		etName = ((EditText) findViewById(R.id.kidNameText));
		etGroup = (EditText) findViewById(R.id.kidGroupNum);
		etNumber.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_DATETIME_VARIATION_NORMAL);
		etGroup.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_DATETIME_VARIATION_NORMAL);
		getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
			
		View bg = findViewById(R.id.root);
		bg.setOnTouchListener(new View.OnTouchListener() {
		    @Override
		    public boolean onTouch(View v, MotionEvent event) {
				hideSoftKeyboard();
				updateButtons();
				return false;
			}
		});		
		
		mc.getNetManager().setReadyListener(new NetEventListener() {
			@Override
			public void run(NetEvent ne, int fromClient) {
				if(fromClient == 0) kid1ready = true;
				else if(fromClient == 1) kid2ready = true;
				if(isWaiting && kid1ready && kid2ready)proceed();
			}
		});		
		
		comenzar = (Button) vcomenzar;
		comenzar.setSystemUiVisibility(View.STATUS_BAR_HIDDEN);
		comenzar.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {comenzarAction();}
		});
		setComenzarState(false);

		
		cargar = (Button) findViewById(R.id.load);
		cargar.setSystemUiVisibility(View.STATUS_BAR_HIDDEN);
		cargar.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				String textNumber = etNumber.getText().toString();
				int kidNumber = 0;
				try
				{
				   kidNumber = Integer.parseInt(textNumber);
				}
				catch (NumberFormatException ignoreException) { return;}
				
				MochilaContents.getInstance().setKid(kidNumber, "",0);

				ActivityEnum result = Saver.loadPresentation();
				if(result == ActivityEnum.END){
					MochilaContents.getInstance().hasLoaded=true;
					Intent intent = new Intent(v.getContext().getApplicationContext(), LoadActivity.class);
					
					startActivity(intent);
					finish();
		        }
			}
		});
		setCargarState(false);
		
		
		etNumber.setOnEditorActionListener(new TextView.OnEditorActionListener() {
			
			@Override
			public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
				updateButtons();
				return false;
			}
		});
		etName.setOnEditorActionListener(new TextView.OnEditorActionListener() {
			
			@Override
			public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
				updateButtons();
				return false;
			}
		});
		etGroup.setOnEditorActionListener(new TextView.OnEditorActionListener() {
			
			@Override
			public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
				updateButtons();
				return false;
			}
		});
		
		
		//PARA TESTEAR!!!
		//setCargarState(true);
		setComenzarState(true);
		int randint = 100000+new Random().nextInt(100000);
		etNumber.setText(randint+"");
		if(Build.SERIAL.equals("c1607f91d8d70cf"))
			etName.setText("Tablet05");
		else if(Build.SERIAL.equals("c1607850186e111"))
			etName.setText("Tablet06");
		else
			etName.setText("Tablet10");
		etGroup.setText("9");
		
		//mc.setTextEdited(0, "hola\n");mc.setTextEdited(1, "como\n");mc.setTextEdited(2, "estas\n");
		/*
		mc.setEtapas(EtapaEnum.CHINA, EtapaEnum.CONEY, EtapaEnum.INICIO);
		//mc.hasLoaded =true;
		startActivity(new Intent(getApplicationContext(), ArgumentatorActivity.class));
		finish();
		*/
		//etNumber.setText("2067637549");
    }
    
    void hideSoftKeyboard() {
        InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(Activity.INPUT_METHOD_SERVICE);
        inputMethodManager.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), 0);
    }
    
    void updateButtons()
    {
    	if( cargar == null ||  comenzar == null) return;
    	String textNumber = etNumber.getText().toString();
    	String kidName = etName.getText().toString();
    	String textGroup = etGroup.getText().toString();
		int kidNumber = 0;
		int kidGroup = 0;
		try
		{
		   kidNumber = Integer.parseInt(textNumber);
		   kidGroup  = Integer.parseInt(textGroup );
		}
		catch (NumberFormatException ignoreException) {}
		if(MochilaContents.getInstance().kidExists(kidNumber))
		{
			setCargarState(true);
			setComenzarState(false);
			
		}
		else if (kidName == null || kidName.equals("") || kidNumber == 0 || kidGroup==0 )
		{
			setCargarState(false);
			setComenzarState(false);
		}
		else
		{
			setCargarState(false);
			setComenzarState(true);
		}
    }
    
    void setCargarState(boolean isActive)
    {
    	cargar.setClickable(isActive);
		cargar.setFocusable(isActive);
		cargar.setTextColor(isActive? Color.WHITE:Color.DKGRAY);
    }
    void setComenzarState(boolean isActive)
    {
    	comenzar.setClickable(isActive);
    	comenzar.setFocusable(isActive);
    	comenzar.setTextColor(isActive? Color.WHITE:Color.DKGRAY);
    }
        
    @Override
    protected void onDestroy() {
    	super.onDestroy();

    };
    
    @Override
    protected void onPause() {
        super.onPause();

    }
    
    @Override
    protected void onResume() {
        super.onResume();

    }
    
    
	boolean isWaiting = false;
	boolean kid1ready=false, kid2ready = false;
    public void proceed(){
    	decidirEtapas(mc.getKidNumber(),mc.getNetManager().getKid(0),mc.getNetManager().getKid(1));
		EtapaEnum etapa = mc.getEtapa(mc.LECTURA);
		
		Intent intent = null;
		if(etapa == EtapaEnum.INICIO)
			intent = new Intent(comenzar.getContext().getApplicationContext(), InicioActivity.class);
		else if(etapa == EtapaEnum.CHINA)
			intent = new Intent(comenzar.getContext().getApplicationContext(), ChinatownActivity.class);
		else
			intent = new Intent(comenzar.getContext().getApplicationContext(), ConeyActivity.class);
		
		startActivity(intent);
		//startActivity(new Intent(getApplicationContext(), CorrectionActivity.class));
    	finish();
    }
    
    protected void cleanup() {
		mc.restart(this.getApplicationContext());
		isWaiting = false;	kid1ready=false; kid2ready = false;
		LogX.cleanLogger();
		System.gc();
    	Log.d("netconnect", "APP RESTART");
    }
    
    public void decidirEtapas(int num, int c1, int c2){

    	if(num < c1 && num < c2)     mc.setEtapas(EtapaEnum.INICIO,EtapaEnum.CHINA,EtapaEnum.CONEY);
		else if (num > c1 && num>c2) mc.setEtapas(EtapaEnum.CHINA,EtapaEnum.CONEY,EtapaEnum.INICIO);
		else 	                     mc.setEtapas(EtapaEnum.CONEY,EtapaEnum.INICIO,EtapaEnum.CHINA);
		
		if    (c1 < c2 && c1 < num) mc.getNetManager().setKidEtapas(0, EtapaEnum.INICIO,EtapaEnum.CHINA,EtapaEnum.CONEY);
		else if (c1 > c2 && c1>num) mc.getNetManager().setKidEtapas(0, EtapaEnum.CHINA,EtapaEnum.CONEY,EtapaEnum.INICIO);
		else                        mc.getNetManager().setKidEtapas(0, EtapaEnum.CONEY,EtapaEnum.INICIO,EtapaEnum.CHINA);
								
		if(c2 < c1 && c2 < num)     mc.getNetManager().setKidEtapas(1, EtapaEnum.INICIO,EtapaEnum.CHINA,EtapaEnum.CONEY);
		else if (c2 > c1 && c2>num) mc.getNetManager().setKidEtapas(1, EtapaEnum.CHINA,EtapaEnum.CONEY,EtapaEnum.INICIO);
		else                        mc.getNetManager().setKidEtapas(1, EtapaEnum.CONEY,EtapaEnum.INICIO,EtapaEnum.CHINA);
			
    	Log.d("netconnect", "kid: " + num + " c1: " + c1 + " c2 " + c2+ " Etapa: "+ mc.getEtapa(mc.LECTURA).toString() );
    }
    
    
    
	public boolean comenzarFlag = false;
	public void comenzarAction() {
		if(comenzarFlag) return;
		if(!comenzarFlag) comenzarFlag = true;
		String kidName = etName.getText().toString();
		String textNumber = etNumber.getText().toString();
		String textGroup = etGroup.getText().toString();
		int kidNumber = 0;
		int kidGroup=0;
		
		try	{  kidNumber = Integer.parseInt(textNumber); 
			   kidGroup = Integer.parseInt(textGroup);
		}catch (Exception ignoreException) { comenzarFlag=false;return;}
		
		if (kidName == null || kidName.equals("")) {comenzarFlag=false;return;}
		

		if (MochilaContents.getInstance().kidExists(kidNumber))	{comenzarFlag=false;return;}
		

		MochilaContents.getInstance().setKid(kidNumber, kidName,kidGroup);
		comenzar.setText("Espere, por favor");
		MochilaContents.getInstance().getNetManager().searchConnect(
			//Si se conectan
			new Runnable() {
				@Override
				public void run() {
					isWaiting=true;
					if(kid1ready && kid2ready)proceed();
			}},
			//Si falla la conexion
			new Runnable() {
				@Override
				public void run() {
					cleanup();
					comenzar.setText("Comenzar historia");
					comenzarFlag = false;
					comenzar.setOnClickListener(new View.OnClickListener() {
						public void onClick(View v) {comenzarAction();}
					});
			}}
		);
		Toast.makeText(getApplicationContext(), "Espere mientras se conectan las tablets...", Toast.LENGTH_SHORT).show();
	}
	
}