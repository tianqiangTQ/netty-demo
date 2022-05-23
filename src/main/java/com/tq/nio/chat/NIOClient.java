package com.tq.nio.chat;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.Iterator;
import java.util.Scanner;

public class NIOClient {
    private static final String HOST = "127.0.0.1";
    private static final int PORT = 8888;

    private Selector selector;
    private SocketChannel socketChannel;
    private String userName;

    public NIOClient() throws IOException {
        selector = Selector.open();
        socketChannel = SocketChannel.open(new InetSocketAddress(HOST, PORT));
        socketChannel.configureBlocking(false);
        socketChannel.register(selector, SelectionKey.OP_READ);

        userName = socketChannel.getLocalAddress().toString().substring(1);
    }

    public static void main(String[] args) throws IOException {
        NIOClient nioClient = new NIOClient();
        new Thread(() -> {
            while (true) {
                try {
                    nioClient.read();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }).start();

        nioClient.send();

    }

    private void read() throws IOException {
        int select = selector.select();
        if (select == 0) {
            return;
        }
        Iterator<SelectionKey> keyIterator = selector.selectedKeys().iterator();
        while (keyIterator.hasNext()) {
            SelectionKey key = keyIterator.next();
            keyIterator.remove();
            if (key.isReadable()) {
                SocketChannel channel = (SocketChannel) key.channel();
                ByteBuffer byteBuffer = ByteBuffer.allocate(1024);
                try {
                    channel.read(byteBuffer);
                } catch (IOException e) {
                    // 断开连接
                    channel.close();
                    key.cancel();
                    continue;
                }
                String msg = new String(byteBuffer.array());
                System.out.println("接收到消息：" + msg);
            }
        }
    }

    private void send() throws IOException {
        Scanner scanner = new Scanner(System.in);
        while (scanner.hasNextLine()) {
            String s = scanner.nextLine();
            String msg = userName + "说：" + s;
            socketChannel.write(ByteBuffer.wrap(msg.getBytes()));
        }
    }
}
