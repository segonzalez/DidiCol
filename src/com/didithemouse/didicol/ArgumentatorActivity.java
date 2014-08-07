package com.didithemouse.didicol;

import java.util.ArrayList;

import android.app.Activity;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.Matrix;
import android.os.Bundle;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.PopupMenu;
import android.widget.PopupWindow;
import android.widget.Spinner;
import android.widget.TextView;

import com.didithemouse.didicol.etapas.EtapaActivity.EtapaEnum;
import com.didithemouse.didicol.network.NetEvent;
import com.didithemouse.didicol.network.NetManager.NetEventListener;

public class ArgumentatorActivity extends Activity {

	EditText inputText = null;
	TextView argText = null;
	Button terminar;
	MochilaContents mc = MochilaContents.getInstance();
	Spinner spinner;
	PopupMenu popup;
	public int currentIndex=0;
	
	final static int objectSize = CreateActivity.objectSize;
		
	String[][] selector;
	int [] selectedIndex;
	String []argumentatorTexts = new String[3];
	
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
		argText = (TextView) findViewById(R.id.argText_);
		
		mc.getNetManager().setReadyListener(new NetEventListener() {
			@Override
			public void run(NetEvent ne, int fromClient) {
				if(fromClient == 0) kid1ready = true;
				else if(fromClient == 1) kid2ready = true;
				if(isWaiting && kid1ready && kid2ready)changekid();
			}
		});
		
		inputText.setSystemUiVisibility(View.STATUS_BAR_HIDDEN);
		
		findViewById(R.id.argEvent_).setOnTouchListener(new View.OnTouchListener() {
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				currX = (int)event.getRawX(); currY=(int)event.getRawY();
				Matrix translate = new Matrix();
				translate.setTranslate(v.getLeft()-50, v.getTop());
				event.transform(translate);
				argText.dispatchTouchEvent(event);
				return false;
			}
		});

		populateSelector();
		
		inputText.setText(mc.getTextEdited(0)+"\n"+mc.getTextEdited(1)+ "\n" + mc.getTextEdited(2));
		
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
				isWaiting=true;
				mc.getNetManager().sendMessage(new NetEvent("argumentator",true) );
				terminar.setBackgroundResource(R.drawable.flechaespera);
				if(kid1ready && kid2ready)changekid();				
			}
		});	
		terminar.setVisibility(View.INVISIBLE);
		
		mc.getNetManager().setArgListener(new NetEventListener() {
			@Override
			public void run(NetEvent ne, int fromClient) {
				selectedIndex[ne.i1] = ne.i2;
				setupArgumentatorBox();
			}
		});
		
		setInstruction();
		
		setupArgumentatorBox();
	}
	
	private void setupArgumentatorBox(){
		checkForCompletion();
	    String text = "";
	    for(int i =0; i<selector.length; i++){
	    	text+= selector[i][selectedIndex[i]];
	    	text+= "  ";
	    }
	    
	    SpannableString ss = new SpannableString(text);
	    for(int i=0; i<selector.length; i++){
	    	if(selector[i].length>1)
	    	setSpanOnLink(ss, i, new LinkSelector(i));
	    }
	    
	    int[][] states = new int[][] {
	    	    new int[] { android.R.attr.state_enabled}, // enabled
	    	    new int[] { android.R.attr.state_selected}, // unchecked
	    	    new int[] { android.R.attr.state_focused}  // pressed
	    	};

	    	int[] colors = new int[] {
	    	    Color.RED,
	    	    Color.GREEN,
	    	    Color.BLUE
	    	};

	    	ColorStateList myList = new ColorStateList(states, colors);
	    
	    argText.setLineSpacing(0f,1.45f);
	    argText.setText(ss);
	    argText.setLinkTextColor(myList);
	    argText.setMovementMethod(LinkMovementMethod.getInstance());
	    argText.setFocusable(true);
	    
	}


	private void setSpanOnLink(SpannableString ss, int index, ClickableSpan cs) {
        int start = 0;
        for(int i =0; i<index; i++){
	    	start+= selector[i][selectedIndex[i]].length()+2;
	    }
        int end = start + selector[index][selectedIndex[index]].length();
        ss.setSpan(cs, start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
    }
	

	PopupWindow popupWindow;
	int currX=0, currY =0;
	private void spawnMenu(int selectorIndex){
		ListView lv = new ListView(this);
		ArrayAdapter<String> saa = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, selector[selectorIndex]); 
		lv.setAdapter(saa);
		
		lv.setOnItemClickListener(new MenuClick(selectorIndex));
		
		//http://www.java2s.com/Code/Android/UI/setListViewHeightBasedOnChildren.htm
        int totalHeight = 0;
        int maxWidth = 0;
        for (int i = 0; i < saa.getCount(); i++) {
            View listItem = saa.getView(i, null, lv);
            listItem.measure(0, 0);
            totalHeight += listItem.getMeasuredHeight();
            if (listItem.getMeasuredWidth() > maxWidth) 
            	maxWidth = listItem.getMeasuredWidth();
        }
        totalHeight = totalHeight + (lv.getDividerHeight() * (lv.getCount() - 1));
		maxWidth = maxWidth + lv.getVerticalScrollbarWidth();        
		if(popupWindow == null) popupWindow = new PopupWindow(this); 
		popupWindow.dismiss();
		popupWindow.setWidth(maxWidth);
		popupWindow.setHeight(totalHeight);
		popupWindow.setContentView(lv);
		popupWindow.setFocusable(true);
		popupWindow.showAtLocation(argText, Gravity.NO_GRAVITY, currX,currY);
	}
	

	boolean isWaiting = false;
	boolean kid1ready=false, kid2ready = false;
	public void changekid(){
		isWaiting=false;kid1ready=false; kid2ready=false;
		clearAndSaveArg();
		
		currentIndex++;	
		if(currentIndex == 3) {proceed();}
		else{
			setInstruction();
		
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
			terminar.setVisibility(View.INVISIBLE);
			setupArgumentatorBox();
		}
	}
	

	public void populateSelector(){
		String[] phrases = getString(R.string.arg_seleccion).split("\\r?\\n");
		ArrayList<String[]> listselector = new ArrayList<String[]>();
		for(String phrase : phrases){
			String [] item = phrase.split("\t");
			if(item.length>1 && item[0]!= null && !item[0].equals("  "))
			{	
				String [] item2 = new String[item.length+1];
				int min = Integer.MAX_VALUE;
				for(int i=0; i< item.length; i++){
					item2[i+1] = item[i]; if( item[i].length() < min) min=item[i].length();
				}
				item2[0] = new String(new char[Math.max(min,3)]).replace("\0", "_");
				item = item2;
			}
			else if (item.length>1 && item[0]!= null && item[0].equals("  "))
			{	
				int min = Integer.MAX_VALUE;
				for(int i=0; i< item.length; i++){
					if( item[i].length() < min) min=item[i].length();
				}
				item[0] = "__";
			}
			listselector.add(item);
		}
		selector = new String[listselector.size()][];
		selectedIndex = new int[listselector.size()];
		for(int i=0; i< listselector.size(); i++){	
			selector[i] = listselector.get(i); 
			selectedIndex[i] = 0;
		}
		
	}
	
	public void checkForCompletion(){
		int completedStuff=0;
		int toComplete =0;
		for(int i=0; i< selectedIndex.length; i++)
		{
			if(selector[i].length>1 && selectedIndex[i]!=0) completedStuff++;
			else if(selector[i].length==2) completedStuff++;
			if(selector[i].length>1 ) toComplete++;
		}
		if(toComplete == completedStuff){
			terminar.setVisibility(View.VISIBLE);
		}
	}
	
	public void clearAndSaveArg(){
		String text = "";
	    for(int i =0; i<selector.length; i++){
	    	if(!selector[i][selectedIndex[i]].equals("__")){
	    	text+= selector[i][selectedIndex[i]];
	    	text+= " ";}
	    }
	    argumentatorTexts[currentIndex] = text;
	    for(int i =0; i<selector.length; i++){
	    	selectedIndex[i]=0;
	    }
	}
	
	TextView insTextView;
	public void setInstruction(){
		if(insTextView==null ) insTextView=((TextView)findViewById(R.id.instruction));
		String name = "";
		if(mc.getEtapa(mc.TEXTO).ordinal()%3 == currentIndex)
			name=mc.getKidName();
		else if(currentIndex == mc.getNetManager().getKidEtapa(0,mc.TEXTO).ordinal()%3)
			name = mc.getNetManager().getKidName(0);
		else if(currentIndex == mc.getNetManager().getKidEtapa(1,mc.TEXTO).ordinal()%3)
			name = mc.getNetManager().getKidName(1);	
		
		if(currentIndex==0)
			insTextView.setText(name+": Lee el texto y argumenta sobre su ortografía.");
		else if(currentIndex==1)
			insTextView.setText(name+": Lee el texto y argumenta sobre su vocabulario.");
		else if(currentIndex==2)
			insTextView.setText(name+": Lee el texto y argumenta sobre su uso de los verbos.");
		
		if(mc.getEtapa(mc.TEXTO).ordinal()%3 == currentIndex){
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
		mc.getNetManager().setReadyListener(null);
		mc.getNetManager().setArgListener(null);;
		mc.setArgumentatorTexts(argumentatorTexts);
		Intent i = new Intent(getApplicationContext(), CorrectionActivity.class);
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
			if(currentIndex != mc.getEtapa(mc.TEXTO).ordinal()%3) return;
			spawnMenu(selectorIndex);
		}
		
	}

	
	class MenuClick implements OnItemClickListener{
		int selectorIndex=0;
		public MenuClick(int _selectorIndex){
			selectorIndex = _selectorIndex;
		}

		@Override
		public void onItemClick(AdapterView<?> parent, View arg1, int position,
				long id) {
			selectedIndex[selectorIndex] = (position)%(selector[selectorIndex].length);
			mc.getNetManager().sendMessage(new NetEvent(selectorIndex,selectedIndex[selectorIndex]));
			setupArgumentatorBox();
			popupWindow.dismiss();
		}
		
	}
}
