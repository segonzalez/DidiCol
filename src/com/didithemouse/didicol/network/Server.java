package com.didithemouse.didicol.network;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.concurrent.Executors;

import com.didithemouse.didicol.MochilaContents;
import com.didithemouse.didicol.network.NetEvent.EventEnum;

import android.os.AsyncTask;
import android.util.Log;


public class Server{

	ServerSocket serverSocket = null;
	Socket[] client = null;
	MochilaContents mc = MochilaContents.getInstance();
	int port = 3389;
	String hostIP = "";

	int kid = 0;
	
	boolean serverWorking=false;
	
	AsyncTask<Void, Socket, Void> serverStart = null;
	ArrayList<AsyncTask<Integer, Object, Void>> clientPoll = null;

	
	
	ObjectInputStream objectInputStreams[] = null;
	
	public Server(int _port, String _hostIP, PollListener _pollListener){
		port = _port;
		hostIP = _hostIP;
		client = new Socket[2];
		objectInputStreams = new ObjectInputStream[2];
		clientPoll= new ArrayList<AsyncTask<Integer,Object, Void>>();
		pollListener = _pollListener;
		
		serverStart = new AsyncTask<Void, Socket, Void>(){
			@Override
			protected Void doInBackground(Void... params) {
				Thread.currentThread().setName("ServerThread");
				try {
					serverSocket = new ServerSocket(port);
					serverSocket.setSoTimeout(1000);
				} catch (IOException e) { }
				while((client[0]==null || client[1] == null) && !isCancelled())
				{
					try {
						publishProgress(serverSocket.accept());
					} catch (IOException e) {}
				}
				return null;
			}

			@Override
			protected void onProgressUpdate(Socket... values) {
				new AsyncTask<Socket, Void, Void>(){
					@Override
					protected Void doInBackground(Socket... params) {
						Thread.currentThread().setName("TryClientThread");
						tryClient(params[0]);
						return null;
				}}.executeOnExecutor(Executors.newSingleThreadExecutor(),values[0]);
			};
			@Override
			protected void onPostExecute(Void result) {
				Log.d("netconnect","serverFinished");
				serverWorking=true;
				AsyncPoll();
			};
			@Override
			protected void onCancelled(){
				asyncClean();
			}
		};
		serverStart.executeOnExecutor(Executors.newSingleThreadExecutor(),new Void[]{null});
	}
	
	public boolean isWorking(){
		return serverWorking;
	}
	
	
	public boolean isClientWorking(Socket socket){
		return (socket != null && socket.isConnected() && !socket.isClosed());
	}
	
	protected void tryClient(Socket s){
		if(s==null || client == null) return;
		synchronized(client){
		if(client[0] != null && client[1] != null) {
			if (s!= null && (!s.isBound()||!s.isConnected())){ try { s.close(); s=null;} catch (IOException e) {}}
			return;
		}}
		ObjectInputStream ois = null;
		ObjectOutputStream objectOutputStream = null;
		try {
			ois = new ObjectInputStream(s.getInputStream());
			objectOutputStream = new ObjectOutputStream(s.getOutputStream());
			
			NetEvent ne = (NetEvent)ois.readObject();
			if( ne == null || ne.type != EventEnum.newConnection  || ne.i2 == 0)
				objectOutputStream.writeObject(new NetEvent(mc.getKidNumber(), 0, mc.getKidName()));
			else
				objectOutputStream.writeObject(new NetEvent(mc.getKidNumber(), mc.getKidGroup(), mc.getKidName()));
			objectOutputStream.flush();
			
			if( ne != null && ne.type == EventEnum.newConnection &&ne.i2 != 0 && ne.i2==mc.getKidGroup()){
				Log.d("netconnect","ip CONNECTED to server: " + s.getInetAddress());
				if (s.getOutputStream() != null) s.shutdownOutput();
				synchronized(client){
					if(client[0]== null) {client[0] = s;objectInputStreams[0] =ois;}
					else if(client[1]== null) {client[1] = s;objectInputStreams[1] =ois;}
					else{s.close();s=null;}
				}
			}
			else{ 
				s.close();
				s=null;
			}
		} catch (IOException e) {} catch (ClassNotFoundException e) {}
		finally{
			if (s!= null && (!s.isBound()||!s.isConnected())){ try { s.close(); s=null;} catch (IOException e) {}}
			if (!isClientWorking(s) && objectOutputStream != null){ try { objectOutputStream.close();} catch (IOException e) {}}
			if (!isClientWorking(s) && ois  != null){ try { ois.close(); } catch (IOException e) {}}

		}
	}
	
	PollListener pollListener = null;
	protected void AsyncPoll(){
		for(int i =0; i<2;i++)
		clientPoll.add(new AsyncTask<Integer, Object, Void>(){
			@Override
			protected Void doInBackground(Integer... index) {
				Thread.currentThread().setName("AsyncPoll");
				int i = index[0];
				ObjectInputStream ois = objectInputStreams[i];
				while(serverWorking){
					{try {
						if(ois == null) return null;
						Object obj = ois.readObject();
						if(obj instanceof NetEvent){
							NetEvent ev = (NetEvent)obj;
							Log.d("netconnect", "got message " + ((ev!=null)?ev.type:"null"));
							publishProgress(ev,index[0]);
						}
					} 
					catch (ClassNotFoundException e) {} 
					catch (IOException e) {}
					}
				}		
				return null;
			}
			@Override
			protected void onProgressUpdate(Object... values) {
				if(pollListener != null)
					pollListener.run((NetEvent)values[0],(Integer)values[1]);
			};
			@Override
			protected void onCancelled() {
				serverWorking=false;
				asyncClean();
			}
		});
		for(int i =0; i<2;i++)
			clientPoll.get(i).executeOnExecutor(Executors.newSingleThreadExecutor(),i);
	}
	
	public void cleanup(){
		serverWorking=false;
		if(clientPoll != null) 
			for(int i =0; i<clientPoll.size();i++)
			{
				AsyncTask<Integer, Object, Void> async= clientPoll.get(i);
				if(async!=null)
					async.cancel(true);
			}
		if(serverStart!= null) serverStart.cancel(true);
		serverStart = null;
		clientPoll=null;
		asyncClean();
	}
	
	public void asyncClean(){
		if(client!=null)
			for(int i =0; i<2; i++)
				if (client[i]!= null){ try { client[i].close(); client[i]=null;} catch (IOException e) {}}
		if (objectInputStreams != null)
		{ 
			for(ObjectInputStream objectInputStream: objectInputStreams)
				if(objectInputStream != null)
					{try { objectInputStream.close(); } catch (IOException e) {}}
		}
		if (serverSocket != null) { try { serverSocket.close(); serverSocket=null;} catch (IOException e) {}}
		pollListener = null;
	}
	
	interface PollListener {
		public void run(NetEvent ne, int kidNo);
	}
	
}
