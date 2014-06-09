package com.didithemouse.didicol.network;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import android.util.Log;

import com.didithemouse.didicol.MochilaContents;
import com.didithemouse.didicol.etapas.EtapaActivity.EtapaEnum;
import com.didithemouse.didicol.network.NetEvent;

public class Client{

	Socket socket = null;
	String ip = "";
	int port = 3389;
	int kid = 0;
	String kidName = "";
	
	
	ObjectOutputStream objectOutputStream = null;
	ObjectInputStream objectInputStream = null;
	
	boolean isWorking =false;
	
	private ExecutorService mPool;
	MochilaContents mc = MochilaContents.getInstance();
	
		
	public Client(String _ip, int _port){
		ip = _ip;
		port = _port;
		kid=mc.getKidNumber();
		isWorking = false;
		
		startClient();
	}
	
	EtapaEnum [] etapas;
	public void setEtapas(EtapaEnum e1, EtapaEnum e2, EtapaEnum e3){
		etapas = new EtapaEnum[]{e1,e2,e3};
	}
	public EtapaEnum getEtapa(int index){
		if(etapas != null) return etapas[index];
		return EtapaEnum.INICIO;
	}
	
	public boolean isWorking(){
		return isWorking;
	}
	public String getIp(){
		return ip;
	}
	
	public int getKidNo(){return kid;}
	public String getKidName(){return kidName;}

	protected void startClient() {

		try {
			socket = new Socket(ip,port);
			objectOutputStream = new ObjectOutputStream(socket.getOutputStream());
			objectInputStream = new ObjectInputStream(socket.getInputStream());
		
			objectOutputStream.writeObject(new NetEvent(mc.getKidNumber(),mc.getKidGroup(),mc.getKidName()));
			objectOutputStream.flush();
			NetEvent ne = (NetEvent)objectInputStream.readObject();
			
			if( ne.i2 == 0 || ne.i2!= mc.getKidGroup())
			{
				socket.close();
				socket=null;
				Log.d("netconnect","closed connection to " + ip+ "(kid group was " + ne.i2 +")");}
			else {
				kid = ne.i1;
				kidName= ne.message;
				Log.d("netconnect","connected with KID " + kid);
				isWorking=true;
				socket.shutdownInput();
				return;
			}
		} 
		catch (UnknownHostException e) {}
		catch (IOException e) {} catch (ClassNotFoundException e) {}
		finally{
			if (!isWorking() && socket!= null){ try { socket.close(); socket=null;} catch (IOException e) {}}
			if (!isWorking() && objectOutputStream != null){ try { objectOutputStream.close();} catch (IOException e) {}}
			if (!isWorking() && objectInputStream  != null){ try { objectInputStream.close(); } catch (IOException e) {}}

		}	
		
	}

	public void send(NetEvent ne){
		if(mPool == null) mPool = Executors.newSingleThreadExecutor();
		if(mPool != null && ne != null)
		mPool.execute(new MessageRunnable(ne));
	}
	
	public void cleanup(){
		isWorking=false;
		if (socket!= null){ try { socket.close(); socket=null;} catch (IOException e) {}}
		if (objectOutputStream != null){ try { objectOutputStream.close();} catch (IOException e) {}}
		if (objectInputStream  != null){ try { objectInputStream.close(); } catch (IOException e) {}}
		if(mPool != null && !mPool.isShutdown() )mPool.shutdownNow();
	}
	
	class MessageRunnable implements Runnable{
		NetEvent ne;
		public MessageRunnable(NetEvent _ne){
			ne = _ne;
		}
		
		public void run(){
			try {
				if(!isWorking || objectOutputStream == null) return;

				Log.d("netconnect","sending msg..."+ne.type);
					objectOutputStream.writeObject(ne);
					objectOutputStream.flush();
			} catch (IOException e) {}
		}
		
	}
 
	
}