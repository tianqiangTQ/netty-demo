package com.tq.nio;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;

public class NIOTest {
    public static void main(String[] args) throws IOException {
//        copyFile01();
//        copyFile02();
        mapBuffer();
    }

    private static void copyFile01() throws IOException {
        Path path = Paths.get("1.txt");
        FileChannel fileChannel = FileChannel.open(path, StandardOpenOption.READ);
        FileOutputStream outputStream = new FileOutputStream("1_copy.txt");
        FileChannel targetChannel = outputStream.getChannel();

        ByteBuffer byteBuffer = ByteBuffer.allocate(512);
        int read = fileChannel.read(byteBuffer);
        while (read != -1) {
            /*
             * public final Buffer flip() {
             *         limit = position;
             *         position = 0;
             *         mark = -1;
             *         return this;
             *     }
             */
            byteBuffer.flip();
            targetChannel.write(byteBuffer);

            /*
             * public final Buffer clear() {
             *         position = 0;
             *         limit = capacity;
             *         mark = -1;
             *         return this;
             *     }
             */
            byteBuffer.clear();
            read = fileChannel.read(byteBuffer);
        }

        fileChannel.close();
        outputStream.close();
    }

    private static void copyFile02() throws IOException {
        Path path = Paths.get("1.txt");
        FileChannel fileChannel = FileChannel.open(path, StandardOpenOption.READ);
        FileOutputStream outputStream = new FileOutputStream("1_copy2.txt");
        FileChannel targetChannel = outputStream.getChannel();
        fileChannel.transferTo( 0, fileChannel.size(), targetChannel);

        fileChannel.close();
        outputStream.close();
    }

    private static void mapBuffer() throws IOException {
        RandomAccessFile randomAccessFile = new RandomAccessFile("1.txt", "rw");
        FileChannel channel = randomAccessFile.getChannel();
        MappedByteBuffer mappedByteBuffer = channel.map(FileChannel.MapMode.READ_WRITE, 0, 5);
        mappedByteBuffer.put(3, (byte) 'x');

        randomAccessFile.close();
    }
}
