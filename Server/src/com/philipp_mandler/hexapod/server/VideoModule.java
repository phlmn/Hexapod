package com.philipp_mandler.hexapod.server;

import com.philipp_mandler.hexapod.hexapod.net.NetPackage;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.oio.OioSocketChannel;

import java.net.Socket;

public class VideoModule extends Module {

	//private SocketChannel m_socketChannel;

	public VideoModule() {
		super.setName("video");
	}

	@Override
	protected void onStart() {
	//	m_socketChannel = new OioSocketChannel(new Socket());
	}

	@Override
	protected void onStop() {

	}

	@Override
	public void tick(Time elapsedTime) {

	}

	@Override
	public void onDataReceived(ClientWorker client, NetPackage pack) {

	}

	@Override
	public void onCmdReceived(ClientWorker client, String[] cmd) {

	}

	@Override
	public void onClientDisconnected(ClientWorker client) {

	}

	@Override
	public void onClientConnected(ClientWorker client) {

	}
}
