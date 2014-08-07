package com.didithemouse.didicol.etapas;


import android.animation.Animator;
import android.animation.Animator.AnimatorListener;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.animation.PropertyValuesHolder;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;

import com.didithemouse.didicol.DescriptionActivity;
import com.didithemouse.didicol.DragController;
import com.didithemouse.didicol.DragLayer;
import com.didithemouse.didicol.DropTarget;
import com.didithemouse.didicol.ExtendedImageView;
import com.didithemouse.didicol.LogX;
import com.didithemouse.didicol.Mochila;
import com.didithemouse.didicol.MochilaContents;
import com.didithemouse.didicol.MyAbsoluteLayout.LayoutParams;
import com.didithemouse.didicol.Principal;
import com.didithemouse.didicol.R;
import com.didithemouse.didicol.ViewWrapper;
import com.didithemouse.didicol.network.NetEvent;
import com.didithemouse.didicol.network.NetManager.NetEventListener;

public abstract class EtapaActivity extends Activity implements View.OnTouchListener {

	public enum EtapaEnum {INICIO, CHINA, CONEY, EMPIRE }
	protected EtapaEnum etapa;
	
	protected EtapaSurfaceView mySurfaceView;
	protected DragController dragController;
	protected DragLayer dragLayer;
	protected Handler handler;
	protected Button closeButton;
	protected int panelNumber;
	protected Mochila mochila;
	protected ImageView mochilaView;
	protected ExtendedImageView[] arrastrables;
	protected LayoutParams[] posiciones;
	protected final static int maxObjetos = 3;
	protected int numItems = 0;
	public static String title;
	protected ImageView badge;
	protected int badgeDrawable;
	protected ImageButton volver;
	protected boolean canGoBack;
	
	MochilaContents mc = MochilaContents.getInstance();

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		this.requestWindowFeature(Window.FEATURE_NO_TITLE);
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
				WindowManager.LayoutParams.FLAG_FULLSCREEN);
		badge = new ImageView(this.getApplicationContext());
		isWaiting=false;
		mc.getNetManager().setObjectListener(new NetEventListener() {
			@Override
			public void run(NetEvent ne,int i) {
				mc.addNetItem(new ViewWrapper(0, 0, ne.i1,ne.f1,EtapaEnum.valueOf(ne.message)));
			}
		});
		canGoBack=true;
	}


	protected void genericInicialization()
	{
		mySurfaceView.setVisibility(View.INVISIBLE);
		ImageView badgeX = (ImageView) findViewById(R.id.badge);
		badgeX.setVisibility(View.GONE);
		mochila = (Mochila) findViewById(R.id.backpack);
		mochilaView = (ImageView) findViewById(R.id.backpack);
		dragController = new DragController(this);
		dragLayer = (DragLayer) findViewById(R.id.drag_layer);

		mochila.setDropRunnable(new Runnable(){
			@Override
			public void run() {
				mochila.setDropRunnable(null);
				canGoBack=false;
				dragController.removeDropTarget(dragLayer);
				dragController.removeDropTarget((DropTarget)mochila);

				LogX.i("Read (" + title + ")","Ha completado el tutorial.");		

				PropertyValuesHolder finalA = PropertyValuesHolder.ofFloat("alpha", 0.20f);
				ObjectAnimator disappear_backpack = ObjectAnimator
						.ofPropertyValuesHolder(mochilaView, finalA).setDuration(2000);
				AnimatorSet move = new AnimatorSet();
				move.play(disappear_backpack);
				move.start();

				move.addListener(new AnimatorListener() {
					@Override
					public void onAnimationStart(Animator animation) {}
					@Override
					public void onAnimationRepeat(Animator animation) {	}
					@Override
					public void onAnimationEnd(Animator animation) {
						MochilaContents.getInstance().getItems().clear();
						genericInicialization2();}
					@Override
					public void onAnimationCancel(Animator animation) {}
				});


			}});

		ImageView backpack = (ImageView) findViewById(R.id.backpack_intro);
		backpack.setVisibility(View.GONE);


		ExtendedImageView badge_eim = new ExtendedImageView(getApplicationContext(),badgeDrawable,1, etapa);
		badge_eim.setImageResource(badgeDrawable);
		badge_eim.setContentDescription("no");
		badge_eim.setVisibility(ImageView.VISIBLE);
		dragLayer.addView(badge_eim);
		badge_eim.setOnTouchListener(this);

		dragLayer.updateViewLayout(badge_eim, new LayoutParams(LayoutParams.WRAP_CONTENT,LayoutParams.WRAP_CONTENT, 390, 310));

		dragLayer.setDragController(dragController);
		dragController.addDropTarget(dragLayer);


		mochilaView.setContentDescription("no");
		dragController.addDropTarget((DropTarget) findViewById(R.id.backpack));

		DragLayer.LayoutParams lp1 = new LayoutParams(179, 205, 0, 540);
		dragLayer.updateViewLayout(mochilaView, lp1);

	}

	protected void genericInicialization2() {

		mySurfaceView.setActivity(this);
		mySurfaceView.setSystemUiVisibility(View.STATUS_BAR_HIDDEN);
		mySurfaceView.setVisibility(View.VISIBLE);

		LogX.i("Read (" + title + ")","Ha comenzado la etapa.");

		mochila = (Mochila) findViewById(R.id.backpack);

		mochila.setDropRunnable(new Runnable(){
			@Override
			public void run() {
				checkObjects(); }});

		inicializarBoton();

		/*** SETUP DE LAS VISTAS ***/
		setupViews();
		mochilaView.setAlpha(1f);
		mochilaView.setVisibility(View.VISIBLE);

	}

	@Override
	protected void onPause() {
		super.onPause();
		// Pausamos el thread de rendering.
		// TODO: de-allocate objects para liberar memoria
		mySurfaceView.onPause();
	}

	@Override
	protected void onResume() {
		super.onResume();
		// Resumimos el thread suspendido
		// TODO: si se hizo la de-allocation, re-allocar los objetos
		mySurfaceView.onResume();
	}

	public void showObjects() {
		//Animation appear = AnimationUtils.loadAnimation(this,
		//		R.animator.appear_long);
		dragLayer.setVisibility(View.VISIBLE);
		//dragLayer.startAnimation(appear);
		if(MochilaContents.SKIP_OBJECTS)
		{
			volver.setClickable(true);
			volver.setVisibility(View.VISIBLE);
		}
		else
		{
			volver.setVisibility(View.INVISIBLE);
		}
	}

	boolean hole_shown = false;

	public void showMouseHole() {
		if (!hole_shown) {
			hole_shown = true;
			View mousehole = findViewById(R.id.mousehole);
			mousehole.setVisibility(View.VISIBLE);
			mousehole.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					LogX.i("Read (" + title + ")", "Se hizo click en el agujero del ratón.");
					View mousehole = findViewById(R.id.mousehole);
					mousehole.setVisibility(View.GONE);
					mousehole.setClickable(false);
					mousehole.setAlpha(0);
					mySurfaceView.mRenderer.collectItems = true;
					setOverlay();
				}
			});
		}
	}

	public void hideMouseHole() {
		if(hole_shown) {
			hole_shown = false;
			View mousehole = findViewById(R.id.mousehole);
			mousehole.setVisibility(View.INVISIBLE);
			mousehole.setClickable(false);
		}
	}
	
	public Handler getHandler() {
		return this.handler;
	}

	int drawables[];
	protected void setupViews() {
		DragController dragController = this.dragController;
		dragLayer = (DragLayer) findViewById(R.id.drag_layer);
		dragLayer.setVisibility(View.INVISIBLE);
		dragLayer.setDragController(dragController);
		dragController.addDropTarget(dragLayer);

		mochilaView = (ImageView) findViewById(R.id.backpack);
		arrastrables = new ExtendedImageView[maxObjetos];
		posiciones = new LayoutParams[maxObjetos];

		setObjects();

		for (int i = 0; i < drawables.length && i < maxObjetos; i++) {
			if (posiciones[i] != null) {
				arrastrables[i] = new ExtendedImageView(this.getApplicationContext(),drawables[i],1,etapa);
				arrastrables[i].setImageResource(drawables[i]);
				arrastrables[i].setVisibility(ImageView.VISIBLE);
				dragLayer.addView(arrastrables[i]);
				arrastrables[i].setContentDescription("no");
				arrastrables[i].setOnTouchListener(this);
				
				dragLayer.updateViewLayout(arrastrables[i], posiciones[i]);
			}
		}

		dragLayer.removeView(mochilaView);
		dragLayer.addView(mochilaView);
		mochilaView.setContentDescription("no");
		dragController.addDropTarget((DropTarget) findViewById(R.id.backpack));

		DragLayer.LayoutParams lp1 = new LayoutParams(179, 205, 0, 540);
		dragLayer.updateViewLayout(mochilaView, lp1);
		volver.setClickable(false);

	}
	
	public boolean startDrag(View v) {
		Object dragInfo = v;
		dragController.startDrag(v, dragLayer, dragInfo,
				DragController.DRAG_ACTION_MOVE);
		return true;
	}

	public void toast(String msg) {
	}

	@Override
	public boolean onTouch(View v, MotionEvent m) {
		//EVITAR Multitouch
    	if(m.getPointerCount() > 1) return true;
    	
		if (m.getAction() == MotionEvent.ACTION_MOVE
				|| m.getAction() == MotionEvent.ACTION_DOWN)
			return startDrag(v);
		return false;
	}
	
	protected void checkObjects()
	{
 		numItems++;
		int counter = 0;
		// -1 porque son solo 2
		for (int i = 0; i < maxObjetos-1; i++)
		{if (arrastrables[i] == null) counter++;}
		if (numItems >= maxObjetos - counter-1)
		{
			volver.setVisibility(View.VISIBLE);
			volver.setColorFilter(0);
			volver.setClickable(true);
			LogX.i("Read (" + title + ")","Se ha guardado un objeto en la mochila.");
			mochila.setDropRunnable(null);
		}
	}

	abstract protected void setVisited();

	abstract void setObjects();

	@Override
	protected void onDestroy() {
		super.onDestroy();
		LogX.i("Read (" + title + ")","Ha finalizado la etapa.");
		this.mySurfaceView.setVisibility(View.GONE);
		this.mySurfaceView.destroyDrawingCache();
		this.mySurfaceView.mRenderer.finish();
		this.mySurfaceView = null;
		
		dragController = null;
		dragLayer = null;
		handler = null;
		closeButton = null;
		mochila = null;
		mochilaView = null;
		arrastrables = null;
		posiciones = null;
		badge = null;
		volver = null;
		isWaiting=false;
		kid1ready=false; kid2ready = false;
		System.gc();
	}

	boolean isWaiting = false;
	boolean kid1ready=false, kid2ready = false;
	protected void inicializarBoton() {
		mc.getNetManager().setReadyListener(new NetEventListener() {
			@Override
			public void run(NetEvent ne, int fromClient) {
				if(fromClient == 0) kid1ready = true;
				else if(fromClient == 1) kid2ready = true;
				if(isWaiting && kid1ready && kid2ready)proceed();
			}
		});
		volver = (ImageButton) findViewById(R.id.volver);
		volver.setOnClickListener(new View.OnClickListener() {
			boolean flag = true;
			public void onClick(View v) {
				if(!flag) return;
				flag = false;
				isWaiting = true;
				mc.getNetManager().sendMessage(new NetEvent("etapa",true) );
				volver.setImageResource(R.drawable.flechaespera);
				if(kid1ready && kid2ready)proceed();
			}
		});
	}
	
	protected void setOverlay()
	{
		
	}
	@Override
	public void onBackPressed() {
		if(!canGoBack) return;
		mc.getNetManager().cleanup();
		Intent intent = new Intent(this.getApplicationContext(), Principal.class);
		startActivity(intent);
		finish();
	}
	
	public void proceed(){
		mc.getNetManager().setReadyListener(null);
		MochilaContents.getInstance().cleanPanels();
		Intent intent = new Intent(this.getApplicationContext(), DescriptionActivity.class);
		setVisited();
		startActivity(intent);
		finish();
	}

}
