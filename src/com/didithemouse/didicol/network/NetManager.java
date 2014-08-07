package com.didithemouse.didicol.network;

import java.util.ArrayList;
import java.util.concurrent.Executors;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import com.didithemouse.didicol.MochilaContents;
import com.didithemouse.didicol.etapas.EtapaActivity.EtapaEnum;
import com.didithemouse.didicol.network.NetEvent.EventEnum;
import com.didithemouse.didicol.network.Server.PollListener;

public class NetManager{
	
	Server server = null;
	Client[] client = null;
	MochilaContents mc = MochilaContents.getInstance();
	
	Context context = null;
	
	IpScanner scanner;

	
	static final int PORT = 3389, SCANPORT = 3390;
	
	AsyncTask<Void, Void, Void> startServerTask, connectClientsTask;
	
	private boolean isWorking = true;
	
	public NetManager(Context _context){
		context = _context;
		client = new Client[2];
		scanner = new IpScanner(context);
	}
	
	public boolean isConnected(){
		return (client[0]!=null && client[1] !=null && server != null && client[0].isWorking() && client[1].isWorking() && server.isWorking());
	}
	
	
	Runnable runOnSucess, runOnFailure;
	public void searchConnect(Runnable _onSucess, Runnable _onFailure)
	{
		runOnSucess=_onSucess;
		runOnFailure=_onFailure;
		//Comenzar el server
		startServerTask = new AsyncTask<Void, Void, Void>(){
			@Override
			protected Void doInBackground(Void... params) {
				Thread.currentThread().setName("StartServerThread");
				server = new Server(PORT,new PollListener() {
					@Override
					public void run(NetEvent ne,int i) {
						gotMessage(ne,i);
					}
				}); return null;
			}
		};
		startServerTask.executeOnExecutor(Executors.newSingleThreadExecutor(),new Void[]{null});
		////
		
		
		//Tratar de conectar los clientes
		connectClientsTask = new AsyncTask<Void, Void, Void>(){
			@Override
			protected Void doInBackground(Void... params) {
				Thread.currentThread().setName("ConnectClientThread");
				connectClients(); return null;
			}
			@Override
			protected void onPostExecute(Void result) {
				sendMessage(new NetEvent("inicio",true));
				scanner.shutdown();
				if(runOnSucess!= null) runOnSucess.run();
			};
		};
		connectClientsTask.executeOnExecutor(Executors.newSingleThreadExecutor(),new Void[]{null});
		/////
	}
	
	private void connectClients(){
		//Conectar clientes
		int tries=0;
		ArrayList<String> foundIps = new ArrayList<String>(); 
		while(foundIps.size()<2 && isWorking)
		{
			tries++;
			for(int i =0; i<2; i++)
			{
				if (client[i] == null || !client[i].isWorking() ) {
					labelFor:
					for(String ip: scanner.getIps(foundIps)){
						client[i] = new Client(ip,PORT);
						if (client[i].isWorking() ) {break labelFor;}
					}
					if(client[i] != null && client[i].isWorking()){
						foundIps.add(client[i].getIp());
						String ipx = "";
				    	for(String ip : scanner.getIps(foundIps) ) {ipx+=ip; ipx+="|";}
				    	String found = "";
				    	for(String ip : foundIps ) {found+=ip; found+="|";}
						Log.d("netconnect", "FOUND CLIENT " +client[i].getIp()
								+ "||ownip:"+scanner.getOwnIp() + "  ips:" + ipx + "  foundips:" + found);
					}
				}
			}
		}
	}
	
	public int getKid(int index){
		return client[index%2].getKidNo();
	}
	public String getKidName(int index){
		return client[index%2].getKidName();
	}
	public void setKidEtapas(int kidIndex, EtapaEnum e1, EtapaEnum e2, EtapaEnum e3){
		client[kidIndex].setEtapas(e1,e2,e3);
	}
	public EtapaEnum getKidEtapa(int kidIndex, int etapaID){
		return client[kidIndex].getEtapa(etapaID);
	}
	public void setKidDescription(int kidIndex, String desString){
		client[kidIndex].setDescription(desString);
	}
	public String getKidDescription(int kidIndex){
		return client[kidIndex].getDescription();
	}
	
	
	public void sendMessage(NetEvent ne){
		if(client!=null)
			for(Client c : client) 
				if(c!=null)c.send(ne);
	}
	
	NetEventListener coordListener = null;
	NetEventListener objectListener = null;
	NetEventListener textListener = null;
	NetEventListener readyListener = null;
	NetEventListener argListener = null;
	
	public void setCoordListener(NetEventListener r) {coordListener = r;}
	public void setObjectListener(NetEventListener r){objectListener = r;}
	public void setTextListener(NetEventListener r){textListener = r;}
	public void setReadyListener(NetEventListener r){readyListener = r;}
	public void setArgListener(NetEventListener r){argListener = r;}
	
	public void gotMessage(NetEvent ne, int i){
		if(ne == null) return;
		if(ne.type == EventEnum.text){
			if(textListener!= null)textListener.run(ne,i);
		}
		else if(ne.type == EventEnum.coordinate){
			if(coordListener!= null)coordListener.run(ne,i);
		}
		else if(ne.type == EventEnum.object){
			if(objectListener!= null)objectListener.run(ne,i);
		}
		else if(ne.type == EventEnum.isReady){
			if(readyListener!= null)readyListener.run(ne,i);
		}
		else if(ne.type == EventEnum.argumentator){
			if(argListener!= null)argListener.run(ne,i);
		}
	}
	
	public void cleanup(){
		isWorking = false;
		if(server != null) server.cleanup();
		server = null;
		for(int i =0; i<2; i++){
			if(client[i] != null)
			client[i].cleanup();
			client[i] =null;
		}
		if(startServerTask != null) startServerTask.cancel(true);
		if(connectClientsTask!= null)connectClientsTask.cancel(true);
		
		scanner.shutdown();
		
		coordListener = null;
		objectListener = null;
		textListener = null;
		readyListener = null;
	}
	    
	public interface NetEventListener {
		public void run(NetEvent ne, int fromClient);
	}
}
    
