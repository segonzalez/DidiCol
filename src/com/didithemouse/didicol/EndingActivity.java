package com.didithemouse.didicol;


import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;

public class EndingActivity extends Activity {
  
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
		
		setContentView(R.layout.ending);
		
		//BOTON reiniciar
		Button reiniciar = (Button) findViewById(R.id.reiniciar);
		reiniciar.setSystemUiVisibility(View.STATUS_BAR_HIDDEN);
		reiniciar.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				Intent inicioIntent = new Intent(v.getContext().getApplicationContext(), Principal.class);
				inicioIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP|Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_CLEAR_TASK);
				startActivity(inicioIntent);
			}
		});
		
		
		//BOTON cerrar
		Button cerrar = (Button) findViewById(R.id.cerrar);
		cerrar.setSystemUiVisibility(View.STATUS_BAR_HIDDEN);
		cerrar.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
		    	MochilaContents.getInstance().restart(null);
				LogX.cleanLogger();
				System.gc();
				finish();
			}
		});
    }
    
    @Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		finish();
	}
    
    @Override
    protected void onPause() {
        super.onPause();

    }
    
    @Override
    protected void onResume() {
        super.onResume();

    }
    @Override
	public void onBackPressed() {
	}
    
    @Override
    protected void onDestroy() {
    	super.onDestroy();
    	MochilaContents.getInstance().restart(null);
		LogX.cleanLogger();
		System.gc();
    }
}
    
