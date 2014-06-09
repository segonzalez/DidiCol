package com.didithemouse.didicol.network;

import java.io.Serializable;

public class NetEvent implements Serializable{
	public static enum EventEnum {coordinate, object, text,newConnection, draw, isReady};
	private static final long serialVersionUID = 1L;
	public int i1,i2,i3;
	public float f1,f2,f3,f4;
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
	
	public NetEvent(int color,boolean isPainting, float x, float y, float sX, float sY, String event){
		type = EventEnum.draw;
		f1=x;f2=y;f3=sX;f4=sY;i1=color;cond=isPainting;message=event;
	}
	
	public NetEvent(int textNo, String text)
	{
		type = EventEnum.text;
		i1=textNo; message=text;
	}
	
	public NetEvent(String activity, boolean isReady){
		type=EventEnum.isReady;
		message = activity; cond = isReady;
	}
}
