package com.didithemouse.didicol;

import android.os.Bundle;
import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;

import com.didithemouse.didicol.R;
import com.didithemouse.didicol.network.NetEvent;
import com.didithemouse.didicol.network.NetManager.NetEventListener;

import android.text.SpannableString;
import android.text.Spanned;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.PopupMenu;
import android.widget.PopupMenu.OnMenuItemClickListener;
import android.widget.Spinner;
import android.widget.TextView;

public class ArgumentatorActivity extends Activity {

	private FrameLayout content = null;
	EditText inputText = null;
	TextView argText = null;
	Button terminar;
	MochilaContents mc = MochilaContents.getInstance();
	Spinner spinner;
	PopupMenu popup;
	public int currentIndex=0;
	
	final static int objectSize = CreateActivity.objectSize;
	
	String[] frase = new String[]{
			"Habia una vez un perro ", " y ", " a un gato. Y fueron "," por siempre."};
	
	String[][] selector = 	new String[][]{
			new String[]{"__","boxer", "rosado", "gato"},
			new String[]{"________", "persiguió","ladró","abrazó"},
			new String[]{"______________", "felices", "tristes", "pulgosos"}};
	
	int [] selectedIndex=new int[]{0,0,0};
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		// Remove title bar
		this.requestWindowFeature(Window.FEATURE_NO_TITLE);
		// Remove notification bar
		this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
				WindowManager.LayoutParams.FLAG_FULLSCREEN);

		setContentView(R.layout.argumentator);
		inputText = (EditText) findViewById(R.id.inputText);
		argText = (TextView) findViewById(R.id.argText);
		
		if(!mc.hasLoaded)
		mc.getNetManager().setReadyListener(new NetEventListener() {
			@Override
			public void run(NetEvent ne, int fromClient) {
				if(fromClient == 0) kid1ready = true;
				else if(fromClient == 1) kid2ready = true;
				if(isWaiting && kid1ready && kid2ready)proceed();
			}
		});
		
		content = (FrameLayout) findViewById(R.id.argCanvas);
		inputText.setSystemUiVisibility(View.STATUS_BAR_HIDDEN);

		//spinner = new Spinner(this);content.addView(spinner);
		
		inputText.setText(mc.getText(0)+"\n"+mc.getText(1)+ "\n" + mc.getText(2));
		
		View focus = getCurrentFocus();
		if(focus != null)
		((InputMethodManager) getSystemService(Activity.INPUT_METHOD_SERVICE)).hideSoftInputFromWindow(focus.getWindowToken(), 0);
		
		inputText.setClickable(false);
		inputText.setFocusable(false);
		inputText.setFocusableInTouchMode(false);
				
		terminar = (Button) findViewById(R.id.terminar);
		terminar.setOnClickListener(new View.OnClickListener() {
			boolean flag = true;
			public void onClick(View v) {
				if(!flag) return;
				flag = false;
				
				if(!mc.hasLoaded)
				{
					isWaiting = true;
					mc.getNetManager().sendMessage(new NetEvent("description",true) );
					terminar.setBackgroundResource(R.drawable.flechaespera);
					if(kid1ready && kid2ready)proceed();
				}
				else proceed();
				
			}
		});
		setupArgumentatorBox();
	}
	
	

	private void setupArgumentatorBox(){
	    String text = "";
	    for(int i =0; i<Math.max(frase.length,selector.length); i++){
	    	if(i<frase.length) text += frase[i];
	    	if(i<selector.length) text+= selector[i][selectedIndex[i]];
	    }
	    
	    SpannableString ss = new SpannableString(text);
	    for(int i=0; i<selector.length; i++)
	    	setSpanOnLink(ss, i, new LinkSelector(i));
	    
	    argText.setText(ss);
	    argText.setLinkTextColor(Color.RED);
	    argText.setMovementMethod(LinkMovementMethod.getInstance());
	    argText.setFocusable(false);
	}


	private void setSpanOnLink(SpannableString ss, int index, ClickableSpan cs) {
        int start = 0;
        for(int i =0; i<index; i++){
	    	if(i<frase.length) start += frase[i].length();
	    	if(i<selector.length) start+= selector[i][selectedIndex[i]].length();
	    }
        if(index<frase.length) start += frase[index].length();
        int end = start + selector[index][selectedIndex[index]].length();
        ss.setSpan(cs, start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
    }
	

		
	private void spawnMenu(int selectorIndex){
		if(popup==null)
			popup = new PopupMenu(this, argText);

		popup.getMenu().clear();
        String[] sel = selector[selectorIndex];
        
        for(int i=0; i< sel.length; i++){
            popup.getMenu().add(Menu.NONE, i, Menu.NONE, sel[i]);
        }

        popup.setOnMenuItemClickListener(new MenuClick(selectorIndex));
    	popup.show();
	}
	

	boolean isWaiting = false;
	boolean kid1ready=false, kid2ready = false;
	public void changekid(){
		
	}
	


	public void proceed(){
		if(!mc.hasLoaded)
			Saver.savePresentation(Saver.ActivityEnum.END);
		mc.getNetManager().setReadyListener(null);
		Intent i = new Intent(getApplicationContext(), EndingActivity.class);
		startActivity(i);
		finish();
	}
	
	@Override
	public void onBackPressed() {}
	
	class LinkSelector extends ClickableSpan{

		int selectorIndex=0;
		public LinkSelector(int _selectorIndex){
			selectorIndex = _selectorIndex;
		}
		
		@Override
		public void onClick(View widget) {
			if(mc.hasLoaded || currentIndex != mc.getEtapa(mc.TEXTO).ordinal()%3) return;
			spawnMenu(selectorIndex);
		}
		
	}


	class MenuClick implements OnMenuItemClickListener{
		int selectorIndex=0;
		public MenuClick(int _selectorIndex){
			selectorIndex = _selectorIndex;
		}
		
		@Override
		public boolean onMenuItemClick(MenuItem item) {
			int id= item.getItemId();
			selectedIndex[selectorIndex] = (id)%(selector[selectorIndex].length);
			setupArgumentatorBox();
			return false;
		}
		
	}
}
