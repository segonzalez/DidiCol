package com.didithemouse.didicol.etapas;

import java.util.ArrayList;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

import android.content.Context;
import android.opengl.GLSurfaceView;
import android.opengl.GLU;
import android.os.Handler;
import android.widget.Scroller;

import com.didithemouse.didicol.DragLayer;
import com.didithemouse.didicol.Point;
import com.didithemouse.didicol.StoryRectangle;

abstract public class EtapaRenderer implements GLSurfaceView.Renderer {
	// dibujos 3x1
	protected ArrayList<StoryRectangle> squares;

	protected Context context;
	public Scroller mScroller;
	public float mAngle;
	public float movement_x, movement_y;
	public float camX = 2.996f, camY = 0.983f, camZoom =-10f;
	public int screenWidth, screenHeight;
	protected int memScrollX = 0, memScrollY = 0;
	protected float factorScrollX = 1, factorScrollY = 1;
	
	protected float limitBottom = 7.371641f, limitRight = -76f;
	protected float limitLeft = -1.9467353f,  limitTop = 2.8596454f;

	protected float unlock_delta = 1.5f;
	protected Point endOfStory_target;
	protected DragLayer draglayer;
	protected EtapaSurfaceView sv;
	protected Handler handler;
	protected float escala;
	public boolean endStory = false, zoomedOut = false, collectItems=false, finishDisappear = false;
	
	int [][] squaresDrawables = new int[6][9];

	public EtapaRenderer(Context context, float initX, float initY,
			EtapaSurfaceView etapaSurfaceView, Handler handler) {
		this.context = context;
		this.sv = etapaSurfaceView;
		this.handler = handler;
		this.camX = initX;
		this.camY = initY;
		this.mScroller = new Scroller(context);
		this.squares = new ArrayList<StoryRectangle>();
		escala = 1.28f;
		
		this.endOfStory_target = new Point(-48.917732f, 4.4619f,0f);
		limitRight = endOfStory_target.getX();
		for(int i = 0; i < 6; i++) for (int j = 0; j < 9; j++) squaresDrawables[i][j] =0;
		createSquares();
		
		for(int i = 0; i < 6; i++)
			for (int j = 0; j < 9; j++)
			{
				if(squaresDrawables[i][j]!= 0)
				squares.add(new StoryRectangle(squaresDrawables[i][j],escala, 8*escala*j, -2*escala*i));
			}
		/*
		for(float i = 0; i < 5; i++)
			for (float j = 0; j < 6; j++)
				squares.add(new StoryRectangle(R.drawable.empty_story_rect, escala, 8*escala*j, -2*escala*i));
		for(float i = 0; i < 6; i++)
			for (float j = 6; j < 9 ; j++)
				squares.add(new StoryRectangle(R.drawable.empty_object_rect, escala, 8*escala*j, -2*escala*i));
		*/

	}
	
	abstract void createSquares();

	public void onSurfaceCreated(GL10 gl, EGLConfig config) {
		// Cargamos las textures
		for (StoryRectangle s : squares) {
			s.loadGLTexture(gl, this.context);
		}

		gl.glEnable(GL10.GL_TEXTURE_2D); // Enable Texture Mapping ( NEW )
		gl.glShadeModel(GL10.GL_SMOOTH); // Enable Smooth Shading
		gl.glClearColor(1.0f, 1.0f, 1.0f, 1.0f); // Black Background
		gl.glClearDepthf(1.0f); // Depth Buffer Setup
		gl.glEnable(GL10.GL_DEPTH_TEST); // Enables Depth Testing
		gl.glDepthFunc(GL10.GL_LEQUAL); // The Type Of Depth Testing To Do

		// Really Nice Perspective Calculations
		gl.glHint(GL10.GL_PERSPECTIVE_CORRECTION_HINT, GL10.GL_NICEST);
	}

	public void onDrawFrame(GL10 gl) {
		gl.glClear(GL10.GL_COLOR_BUFFER_BIT | GL10.GL_DEPTH_BUFFER_BIT);

		// Revisamos si el usuarario hizo un gesto de scroll
		if (mScroller.computeScrollOffset()) {
			// calculamos delta de movimiento
			movement_x = factorScrollX * -1
					* (mScroller.getCurrX() - memScrollX) * 0.0055f;
			movement_y = factorScrollY * (mScroller.getCurrY() - memScrollY)
					* 0.0055f;
			memScrollX = mScroller.getCurrX();
			memScrollY = mScroller.getCurrY();
		}
		// Revisamos si termin� el scroll autom�tico
		else if (mScroller.isFinished()) {
			memScrollX = 0;
			memScrollY = 0;
			factorScrollX = 1;
			factorScrollY = 1;
		}
		// Hacemos los ajustes a la camara seg�n corresponda
		camX += movement_x;
		camY += movement_y;
		movement_x = 0;
		movement_y = 0;

		// Limitamos movimiento a ciertos bordes 
		//Log.d("pos", "Camera pos: " + camX + "," + camY + "," + camZoom);
		if (camX < limitRight) {
			camX = limitRight;
			factorScrollX = -factorScrollX * 0.3f;
		} else if (camX > limitLeft) {
			camX = limitLeft;
			factorScrollX = -factorScrollX * 0.3f;
		}
		
		if (camY > limitBottom) {
			camY = limitBottom;
			factorScrollY = -factorScrollY * 0.3f;
		} else if (camY < limitTop) {
			camY = limitTop;
			factorScrollY = -factorScrollY * 0.3f;
		}
		// Revisamos llegamos al final, de ser asi bloqueamos la pantalla
		float delta = 4.0f;
		if ( Math.abs(camX - endOfStory_target.getX()) < delta
				&& Math.abs(camY - endOfStory_target.getY()) < delta
				&& !collectItems) {
			handler.post(new Runnable() {
				public void run() {
					sv.showMouseHole();} });
		} else {
			handler.post(new Runnable() {
				public void run() {
					sv.hideMouseHole();
					} });
		}
		
		//Ocultamos la surfaceview
		if (collectItems && !zoomedOut) {
			//Ocultamos la surfaceview
			handler.post(new Runnable() {
				public void run() {
					sv.setAlpha(0);
					finishDisappear = true;
				}
			});
			//Borrar momentum
			factorScrollX =factorScrollY = 0;
			//Fixeamos la cámara y los limites impuestos
			endStory = true;
			limitLeft = 0.9357681f; limitTop = 2.1651459f;
			limitBottom = 9.843448f; limitRight = -78f;
			//esperamos al thread que deja el alpha en 0
			while(!finishDisappear) {}
			//zoomOut
			camX = -68.25183f;
			camY = 5.720143f;
			camZoom = -16.9f;
			
			
			//forzamos el zoomOut
			gl.glLoadIdentity();
			gl.glTranslatef(camX, camY, camZoom);
			for (StoryRectangle s : squares)
				s.draw(gl);	
			
			//mostramos los objetos, luego el canvas
			finishDisappear = false;
			zoomedOut = true;
			handler.post(new Runnable() {
				public void run() {
					sv.setAlpha(1);
					sv.showObjects();
					finishDisappear = true;							
				}
			});
			//esperamos al thread
			//while(!finishDisappear){}
		}

		gl.glLoadIdentity();
		// Movemos la c�mara a su nuevo punto 2D
		gl.glTranslatef(camX, camY, camZoom);
		// Log.d("camera", "(transformados) x:"+(-1*camX)+",y:"+(-1*camY));

		// IMPORTANTE: aqu� dibujamos los objetos!
		for (StoryRectangle s : squares)
			s.draw(gl);
		// for(StatueTextRectangle t: text) t.draw(gl);
		
	}

	public void finish() {
		for (StoryRectangle s : squares)
			s.clear();
	}

	@Override
	public void onSurfaceChanged(GL10 gl, int width, int height) {
		// TODO: revisar, puede no se ocupe por lock screen
		if (height == 0) { // Prevent A Divide By Zero By
			height = 1; // Making Height Equal One
		}

		gl.glViewport(0, 0, width, height); // Reset The Current Viewport
		gl.glMatrixMode(GL10.GL_PROJECTION); // Select The Projection Matrix
		gl.glLoadIdentity(); // Reset The Projection Matrix

		// Calculate The Aspect Ratio Of The Window
		GLU.gluPerspective(gl, 45.0f, (float) width / (float) height, 0.1f,
				100.0f);

		gl.glMatrixMode(GL10.GL_MODELVIEW); // Select The Modelview Matrix
		gl.glLoadIdentity(); // Reset The Modelview Matrix
	}

}
