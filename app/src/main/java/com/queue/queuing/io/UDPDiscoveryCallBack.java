package com.queue.queuing.io;

public interface UDPDiscoveryCallBack {
	
	void onDiscoverStarted();
	
	void onDiscoveredIpMac(String ip, String mac);
	void onDiscoveredIpName(String ip, String name);
	void onDiscoveredMacIpName(String mac, String ip, String name);
	
	void onDiscoverFinished();
}
