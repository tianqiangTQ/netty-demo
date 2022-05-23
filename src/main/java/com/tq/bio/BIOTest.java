package com.tq.bio;


import java.io.IOException;
import java.io.InputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class BIOTest {
    public static void main(String[] args) throws IOException {
        ServerSocket serverSocket = new ServerSocket(8888);
        System.out.println("服务端启动成功");
        // 客户端处理线程池
        ExecutorService threadPool = Executors.newCachedThreadPool();

        while (true) {
            System.out.println(Thread.currentThread().getId() + " main线程，阻塞：accept...");
            Socket socket = serverSocket.accept();

            threadPool.execute(() -> {
                try {
                    InputStream inputStream = socket.getInputStream();
                    byte[] bytes = new byte[1024];
                    System.out.println(Thread.currentThread().getId() + " 客户端线程，阻塞：read...");
                    int read = inputStream.read(bytes);
                    while (read != -1) {
                        System.out.println(new String(bytes));
                        read = inputStream.read(bytes);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    System.out.println("关闭和 client 的连接");
                    try {
                        socket.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            });

        }
    }
}
