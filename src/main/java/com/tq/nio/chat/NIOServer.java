package com.tq.nio.chat;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectableChannel;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Iterator;
import java.util.Set;

public class NIOServer {
    private static final int PORT = 8888;
    private ServerSocketChannel serverSocketChannel;
    private Selector selector;

    public NIOServer() throws IOException {
        this.serverSocketChannel = ServerSocketChannel.open();
        this.selector = Selector.open();
        serverSocketChannel.configureBlocking(false);
        serverSocketChannel.bind(new InetSocketAddress(PORT));
        serverSocketChannel.register(selector, SelectionKey.OP_ACCEPT);
    }

    public static void main(String[] args) throws IOException {
        NIOServer nioServer = new NIOServer();
        nioServer.listen();
    }

    private void listen() throws IOException {

        while (true) {
            int select = selector.select();
            if (select == 0) {
                continue;
            }

            Iterator<SelectionKey> keyIterator = selector.selectedKeys().iterator();
            while (keyIterator.hasNext()) {
                SelectionKey key = keyIterator.next();
                keyIterator.remove();

                if (key.isAcceptable()) {
                    accept(serverSocketChannel, selector);
                }
                if (key.isReadable()) {
                    read(key, selector);
                }

            }

        }
    }

    private void read(SelectionKey key, Selector selector) throws IOException {
        SocketChannel socketChannel = (SocketChannel) key.channel();
        ByteBuffer byteBuffer = ByteBuffer.allocate(1024);
        try {
            socketChannel.read(byteBuffer);
        } catch (IOException e) {
            // 断开连接
            System.out.println("客户端离线->" + socketChannel.getRemoteAddress());
            socketChannel.close();
            key.cancel();
            return;
        }
        String msg = new String(byteBuffer.array());
        System.out.println("接收到客户端消息->" + msg);

        send(msg, selector, socketChannel);
    }

    private void send(String msg, Selector selector, SocketChannel self) throws IOException {
        Set<SelectionKey> keys = selector.keys();
        for (SelectionKey key : keys) {
            SelectableChannel channel = key.channel();
            if (channel instanceof SocketChannel && channel != self) {
                ((SocketChannel) channel).write(ByteBuffer.wrap(msg.getBytes()));
            }
        }
    }

    private void accept(ServerSocketChannel serverSocketChannel, Selector selector) throws IOException {
        SocketChannel accept = serverSocketChannel.accept();
        System.out.println("客户端上线->" + accept.getRemoteAddress().toString());
        accept.configureBlocking(false);
        accept.register(selector, SelectionKey.OP_READ);
    }

}
