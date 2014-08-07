package com.didithemouse.didicol.network;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import android.content.Context;
import android.net.DhcpInfo;
import android.net.wifi.WifiManager;
import android.text.format.Formatter;

//https://lab.dyne.org/AndroidUDPBroadcast

class IpScanner
{
	static final int SCANPORT = NetManager.SCANPORT;
	int timeout = 1000;
	ConcurrentHashMap<String, InetAddress> ips = new ConcurrentHashMap<String, InetAddress>();
	Runnable broadcastTask, receiverTask;
	ExecutorService broadcastExe, receiverExe;
	Context mContext;
	DatagramSocket socket;
	boolean working = true;
    
	
	public IpScanner(Context c){
		mContext =c;
		working = true;
    	broadcastTask= new Runnable (){
			@Override
			public void run() {
				Thread.currentThread().setName("broadcastThread");
				while(working){
					try {Thread.sleep(1000);} catch (InterruptedException e) {}
					InetAddress broadAddr = getBroadcastAddress();
					if(broadAddr != null) broadcast(broadAddr);	
				}
			}
		};
    	broadcastExe = Executors.newSingleThreadExecutor();	
		broadcastExe.execute(broadcastTask);
    	receiverTask= new Runnable (){
			@Override
			public void run() {
				Thread.currentThread().setName("receiverThread");
				try {
					socket = new DatagramSocket(SCANPORT);
				    socket.setBroadcast(true);
				    socket.setSoTimeout(0);
					receive();
				} catch (SocketException e) {}
			}
		};
    	receiverExe = Executors.newSingleThreadExecutor();
    	receiverExe.execute(receiverTask);
	}
			
	public String getOwnIp(){
		WifiManager wm = (WifiManager) mContext.getSystemService(Context.WIFI_SERVICE);
    	return Formatter.formatIpAddress(wm.getConnectionInfo().getIpAddress());
	}
	
	public ArrayList<String> getIps(ArrayList<String> toIgnore){
		ArrayList<String> arrayIps = Collections.list(ips.keys());
		arrayIps.remove(getOwnIp());
		if(toIgnore != null) arrayIps.removeAll(toIgnore);
		return arrayIps;
	}

	private void receive(){
	    byte[] buf = new byte[16];
	    try {
	      while (working) {
	        DatagramPacket packet = new DatagramPacket(buf, buf.length);
	        socket.receive(packet);
	        ips.put(packet.getAddress().getHostAddress(),packet.getAddress());
	      }
	    } catch (Exception e) { }
	}
	
	private void broadcast(InetAddress broadcast){
	    String data = "x";
	    if(broadcast != null)
	    {	
	    	DatagramPacket packet = new DatagramPacket(data.getBytes(), data.length(),
	           broadcast, SCANPORT);
	    	try {
				socket.send(packet);
			} catch (IOException e) {}
	    }
	}
	
	public void shutdown(){
		socket.close();
		receiverExe.shutdownNow();
		broadcastExe.shutdownNow();
		working=false;
		ips = new ConcurrentHashMap<String, InetAddress>();
	}

	private InetAddress getBroadcastAddress(){
	    WifiManager wifi = (WifiManager) mContext.getSystemService(Context.WIFI_SERVICE);
	    DhcpInfo dhcp = wifi.getDhcpInfo();
	    if (dhcp == null || dhcp.ipAddress == 0)
	    	return null;
	    int broadcast =(dhcp.ipAddress & dhcp.netmask) | ~dhcp.netmask;
	    byte[] quads = new byte[4];
	    for (int k = 0; k < 4; k++)
	      quads[k] = (byte) ((broadcast >> k * 8) & 0xFF);
	    try {
			return InetAddress.getByAddress(quads);
		} catch (UnknownHostException e) {
			return null;
		}
	}
	
}
    
