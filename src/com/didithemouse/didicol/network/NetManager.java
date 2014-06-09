package com.didithemouse.didicol.network;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InterfaceAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import com.didithemouse.didicol.MochilaContents;
import com.didithemouse.didicol.etapas.EtapaActivity.EtapaEnum;
import com.didithemouse.didicol.network.NetEvent.EventEnum;
import com.didithemouse.didicol.network.Server.PollListener;

import android.content.Context;
import android.net.wifi.WifiManager;
import android.os.AsyncTask;
import android.text.format.Formatter;
import android.util.Log;

public class NetManager{
	
	Server server = null;
	Client[] client = null;
	MochilaContents mc = MochilaContents.getInstance();
	
	Context context = null;
	
	String ownIp = "";
	ArrayList<String > ips = null;
	
	static final int PORT = 3389;
	
	AsyncTask<Void, Void, Void> scanIpsTask, startServerTask, connectClientsTask;
	
	private boolean isWorking = true;
	
	public NetManager(Context _context){
		context = _context;
		client = new Client[2];
		WifiManager wm = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
    	ownIp = Formatter.formatIpAddress(wm.getConnectionInfo().getIpAddress());
    	scanIpsTask= new AsyncTask<Void, Void, Void>(){
			@Override
			protected Void doInBackground(Void... params) {
				Thread.currentThread().setName("IpsThread");
				scanIps(); return null;
			}
		};
		
		scanIpsTask.executeOnExecutor(Executors.newSingleThreadExecutor(),new Void[]{null});
	}
	
	public boolean isConnected(){
		return (client[0]!=null && client[1] !=null && server != null && client[0].isWorking() && client[1].isWorking() && server.isWorking());
	}
	
	
	Runnable ru;
	public void search(Runnable r)
	{
		ru = r;
		//Comenzar el server
		startServerTask = new AsyncTask<Void, Void, Void>(){
			@Override
			protected Void doInBackground(Void... params) {
				Thread.currentThread().setName("StartServerThread");
				server = new Server(PORT,ownIp,new PollListener() {
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
				if(ru!= null) ru.run();
			};
		};
		connectClientsTask.executeOnExecutor(Executors.newSingleThreadExecutor(),new Void[]{null});
		/////
	}
	
	
	private void scanIps()
	{
    	Log.d("netconnect", "ownip is "+ownIp );
    	ips = new ipScanner().scan();
    	ips.remove(ownIp);
    	String ipx = "";
    	for(String ip : ips ) {ipx+=ip; ipx+="|";}
    	Log.d("netconnect", "scan finished, ips:" + ipx);
	}
	private void connectClients(){
		while(ips == null && isWorking){}
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
					for(String ip: ips){
						client[i] = new Client(ip,PORT);
						if (client[i].isWorking() ) {break labelFor;}
					}
					if(client[i] != null && client[i].isWorking()){
						ips.remove(client[i].getIp());
						foundIps.add(client[i].getIp());
						Log.d("netconnect", "FOUND CLIENT " +client[i].getIp());
					}
				}
			}
			if(tries%100 == 99){
				//rescan every 100 tries
				ips = new ipScanner().scan();
	    		ips.remove(ownIp);
	    		ips.remove(foundIps);
			}
		}
	}
	
	public int getClientKid(int index){
		return client[index%2].getKidNo();
	}
	public String getClientKidN(int index){
		return client[index%2].getKidName();
	}

	public void setClientEtapas(int index, EtapaEnum e1, EtapaEnum e2, EtapaEnum e3){
		client[index].setEtapas(e1,e2,e3);
	}
	public EtapaEnum getClientEtapas(int index, int etapa){
		return client[index].getEtapa(etapa);
	}
	
	
	
	public void sendMessage(NetEvent ne){
		if(client!=null)
			for(Client c : client) 
				if(c!=null)c.send(ne);
	}
	
	NetEventListener coordListener = null;
	NetEventListener objectListener = null;
	NetEventListener textListener = null;
	NetEventListener drawListener = null;
	NetEventListener readyListener = null;
	
	public void setCoordListener(NetEventListener r) {coordListener = r;}
	public void setObjectListener(NetEventListener r){objectListener = r;}
	public void setDrawListener(NetEventListener r){drawListener = r;}
	public void setTextListener(NetEventListener r){textListener = r;}
	public void setReadyListener(NetEventListener r){readyListener = r;}
	
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
		else if(ne.type == EventEnum.draw){
			if(drawListener!= null)drawListener.run(ne,i);
		}
		else if(ne.type == EventEnum.isReady){
			if(readyListener!= null)readyListener.run(ne,i);
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
		if(scanIpsTask!= null) scanIpsTask.cancel(true);
		if(startServerTask != null) startServerTask.cancel(true);
		if(connectClientsTask!= null)connectClientsTask.cancel(true);
		
		coordListener = null;
		objectListener = null;
		textListener = null;
		drawListener = null;
		readyListener = null;
	}
	
	/////////////////////////////////1979807630
    private class ipScanner
    {
        private ExecutorService mPool;
        int THREADS = 256;
        long START=3232235520l, END=3232236031l;
        //3232301055
        ConcurrentHashMap<Long, String> ips = new ConcurrentHashMap<Long, String>();
    	int timeout = 1000;
        
    	public ipScanner(){
    		if (ownIp == null || ownIp.equals("")) return;
    		START = getUnsignedPrefixFromIp(ownIp);
    		END   = START+THREADS-1; 		
    	}
    	
        private ArrayList<String> scan(){
        	return scan(1000);
        }
        private ArrayList<String> scan(int timeout){
        	this.timeout = timeout;
        	mPool = Executors.newFixedThreadPool(THREADS);
        	for (long i = START; i <= END; i++) {
                launch(i);
            }
        	mPool.shutdown();
            try {
				mPool.awaitTermination(1000, TimeUnit.MILLISECONDS);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
            return Collections.list(ips.elements());
        }
        private void launch(long addr) {
            if(!mPool.isShutdown()) {
                mPool.execute(new CheckIpRunnable(addr));
                
            }
            
        }
        //RUNNABLE QUE CHECKEA IP
        class CheckIpRunnable implements Runnable{
        	private String addr;
        	long  laddr;

            CheckIpRunnable(long laddr) {
            	this.laddr = laddr;
                this.addr = getIpFromLongUnsigned(laddr);
            }
            


            public void run() {
            	InetAddress h;
				try {
	                // Native InetAddress check
					h = InetAddress.getByName(addr);
					try {
						if (h.isReachable(timeout)) {
						    //Log.d("netconnect", "found using InetAddress ping "+addr);
						    ips.put(laddr,addr);
						    return;
						}
					} catch (IOException e) {
						e.printStackTrace();
					}
				} catch (UnknownHostException e1) {
					e1.printStackTrace();
				}
                
            }
            }
        }
    
    
    public static InetAddress getBroadcast(InetAddress inetAddr){

        NetworkInterface temp;
        InetAddress iAddr=null;
     try {
         temp = NetworkInterface.getByInetAddress(inetAddr);
         List<InterfaceAddress> addresses = temp.getInterfaceAddresses();

         for(InterfaceAddress inetAddress:addresses)
          iAddr=inetAddress.getBroadcast();
         
         Log.d("netconnect","Broadcast="+iAddr);
         return iAddr;  

     } catch (SocketException e) {}
      return null; 
 }
    
    
    // 192.168.0.122 -> int(192.168.0.0)
    public static long getUnsignedPrefixFromIp(String ip_addr) {
        String[] a = ip_addr.split("\\.");
        return (Integer.parseInt(a[0]) * 16777216 + Integer.parseInt(a[1]) * 65536
                + Integer.parseInt(a[2]) * 256 );
    }
    // int(192.168.0.122) -> "192.168.0.0"
    public static String getIpFromLongUnsigned(long ip_long) {
        String ip = "";
        for (int k = 3; k > -1; k--) {
            ip = ip + ((ip_long >> k * 8) & 0xFF) + ".";
        }
        return ip.substring(0, ip.length() - 1);
    }
    
	public interface NetEventListener {
		public void run(NetEvent ne, int fromClient);
	}
}
    
