package com.didithemouse.didicol;

import java.io.File;
import java.util.ArrayList;

import android.content.Context;
import android.os.Environment;

import com.didithemouse.didicol.etapas.ChinatownActivity;
import com.didithemouse.didicol.etapas.ConeyActivity;
import com.didithemouse.didicol.etapas.EmpireStateActivity;
import com.didithemouse.didicol.etapas.InicioActivity;
import com.didithemouse.didicol.etapas.EtapaActivity.EtapaEnum;
import com.didithemouse.didicol.network.NetManager;

public class MochilaContents {

	private DropPanelWrapper dropPanel;
	private ArrayList<ViewWrapper> items;
	private ArrayList<ViewWrapper> netItems;
	private String[] texts;
	private String[] textsOriginal;
	private String description = null;
	
	//private boolean created;
	public boolean hasLoaded;
	
	private int kidNumber = 0;
	private String kidName = "";
	private int kidGroup = 0;
	private String dirName = "" ;
	//private String RCSdir = Environment.getExternalStorageDirectory().getAbsolutePath() + "/My SugarSync Folders/My SugarSync/RC-Write/";
	private String RCSdir = Environment.getExternalStorageDirectory().getAbsolutePath() +"/TestWrite/";
	
	public EtapaEnum [] etapa;
	
	//Deshabilita log, guardado, etc. para debug.
	public static final boolean SAVING  = true;
	public static final boolean LOGGING = false;
	public static final boolean SKIP_OBJECTS = true;
	public static final boolean SKIP_MAP     = true;
	
	public static int numStages = 4;
	private int visitedPlaces = 0;
	public int getVisitedPlaces()
	{
		visitedPlaces = 0;
		if(EmpireStateActivity.visitedFlag) visitedPlaces++;
		if(ConeyActivity.visitedFlag ) visitedPlaces++;
		if(InicioActivity.visitedFlag) visitedPlaces++;
		if(ChinatownActivity.visitedFlag ) visitedPlaces++;
		return visitedPlaces;
	}
	
	
	private static MochilaContents INSTANCE = new MochilaContents();
	private MochilaContents() {

	}
	public static MochilaContents getInstance() {
        return INSTANCE;
    }
	
	public DropPanelWrapper getDropPanel() {
		if (dropPanel == null) dropPanel = new DropPanelWrapper();
		return dropPanel;
	}
	
	public void setDropPanel(DropPanelWrapper _dropPanel) {
		dropPanel = _dropPanel;
	}

	
	public void addItem(ExtendedImageView v) {
		float vSize = Math.max(v.getHeight(), v.getWidth());
		
		float scaleFactor = CreateActivity.objectSize*1.0f/vSize; 
		v.setScaleFactor(scaleFactor);

		ViewWrapper vw = new ViewWrapper(0, 0, v, v.getEtapa());
		items.add(vw);
		vw.destroyView();
	}
	
	public void addItem(ViewWrapper v) {
		items.add(v);
	}
	
	public ArrayList<ViewWrapper> getItems()
	{
		return items;
	}
	
	public void addNetItem(ViewWrapper v) {
		netItems.add(v);
	}
	public void mergeNetItems()
	{
		items.addAll(netItems);
		ArrayList<ViewWrapper> vws = new ArrayList<ViewWrapper>();
		while(items.size()>0){
			int index=0;int min=Integer.MAX_VALUE;
			for (int i =0; i<items.size(); i++){
				ViewWrapper vw = items.get(i);
				if(vw == null) continue;
				int drawid = vw.getDrawableID();
				if(min> drawid)
				{index=i;min = drawid;}
			}
			vws.add(items.get(index));
			items.remove(index);
		}
		items = vws;
		netItems.clear();
	}
	
	//public boolean isCreated() { return created;	}
	//public void setCreated(boolean created) {	this.created = created; }
	
	public void setText(int index, String text)
	{
		if(texts == null) texts = new String[] {"","",""};
		texts[index%3] = text;
	}
	public String getText(int index)
	{
		if(texts == null) texts = new String[] {"","",""};
		return texts[index%3];
	}
	public String getTextOriginal(int index)
	{
		if(textsOriginal == null) textsOriginal = new String[] {"","",""};
		return textsOriginal[index%3];
	}
	public void setTextOriginal(int index, String text)
	{
		if(textsOriginal == null) textsOriginal = new String[] {"","",""};
		textsOriginal[index%3] = text;
	}
	public String[] getTexts(){ return texts;}
	public String[] getTextsOriginal(){ return textsOriginal;}
	public void cloneTexts(){
		for(int i =0; i<3; i++)
		textsOriginal[i]=texts[i];
	}
	
	public void setDescription(String _description)
	{
		description=_description;
	}
	public String getDescription()
	{
		return description;
	}
	
	public void cleanPanels()
	{
		for(ViewWrapper wx: items)
			wx.destroyView();
		dropPanel.cleanPanel(true);
	}
	
	public void setKid(int _kidNumber,String _kidName, int _kidGroup) { 
		kidNumber = _kidNumber;
		kidName = _kidName != null? _kidName: "";
		kidGroup = _kidGroup;
		
		dirName = RCSdir +"/"+kidNumber+"/";

	}
	
	public void makeDirs(){
		if(!dirName.equals(""))
		(new File (dirName)).mkdirs();
	}
	
	public int getKidNumber(){ return kidNumber; }
	public int getKidGroup(){ return kidGroup; }
	public String getKidName(){ return kidName; }
	public String getDirectory() { return dirName; }
	
	
	public static int LECTURA=0, OBJETOS=1, TEXTO=2;
	public EtapaEnum getEtapa(int index){return etapa!=null? 
												etapa[index%3]:EtapaEnum.INICIO;}
	public void setEtapas(EtapaEnum e0, EtapaEnum e1, EtapaEnum e2){
		if (etapa == null) return;
		etapa[0] = e0; etapa[1] = e1; etapa[2] = e2;
	}
	
	private final String logDirname =  RCSdir + "/log/";
	
	public String getLogDirname()
	{
		File f = new File(logDirname);
		if (!f.exists()) f.mkdirs();
		return logDirname;
	}
	
	
	public boolean kidExists(int num)
	{
		String dirnameX = RCSdir +"/"+num+"/" ;
        return (new File(dirnameX)).exists();
        	
	}
	
	NetManager netManager = null;
	public NetManager getNetManager(){
		return netManager;
	}
	
	public void restart(Context _c)
	{
		if(netManager!= null) netManager.cleanup();
		if(_c != null)
			netManager = new NetManager(_c);
		
		Saver.clear();
		if (dropPanel != null)
			dropPanel.killPanel(true);
		if (items != null)
		for(ViewWrapper wx: items)
			wx.destroyView();
		
		netItems = new ArrayList<ViewWrapper>();
		
		items = new ArrayList<ViewWrapper>();
		dropPanel = new DropPanelWrapper();
		texts = new String[] {"","",""};
		textsOriginal= new String[] {"","",""};
		etapa = new EtapaEnum[3];
		//created = false;
		hasLoaded=false;
		
		description="";
		
		visitedPlaces = 1;
				
		InicioActivity.visitedFlag=false;
		EmpireStateActivity.visitedFlag = false;
		ConeyActivity.visitedFlag = false;
		ChinatownActivity.visitedFlag = false;

	}
	
	
	
}
