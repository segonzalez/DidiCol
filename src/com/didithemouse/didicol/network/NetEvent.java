package com.didithemouse.didicol.network;

import java.io.Serializable;

public class NetEvent implements Serializable{
	public static enum EventEnum {coordinate, object, text,newConnection, isReady,argumentator};
	private static final long serialVersionUID = 1L;
	public int i1,i2,i3;
	public float f1,f2;
	public boolean cond;
	public String message ="";
	public EventEnum type;
	
	public NetEvent(int _x, int _y,float xOffset,float yOffset, int _objeto, boolean fromDropPanel){
		type = EventEnum.coordinate;
		i1=_x;i2=_y;i3=_objeto;cond = fromDropPanel;
		f1=xOffset;f2=yOffset;
	}
	public NetEvent(int _objeto, float _scale, String etapa){
		type= EventEnum.object;
		i1 = _objeto; f1 = _scale;message=etapa;
	}
	public NetEvent(int kidNum, int kidGroup,String kidName){
		type = EventEnum.newConnection;
		message=kidName;i1=kidNum;i2=kidGroup;
	}
	
	
	public NetEvent(int textNo, String text, int position)
	{
		type = EventEnum.text;
		i1=textNo; message=text; i2=position;
	}
	
	public NetEvent(int selectorNumber, int selection){
		type=EventEnum.argumentator;
		i1=selectorNumber; i2=selection;
	}
	
	public NetEvent(String activity, boolean isReady){
		type=EventEnum.isReady;
		message = activity; cond = isReady;
	}
}
