package com.company;

import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.Iterator;
import java.util.Scanner;
import java.util.Set;

public class Client {

    public static void main(String[] args) throws Exception {
        Selector selector = Selector.open();

        SocketChannel clientCh = SocketChannel.open();
        clientCh.configureBlocking(false);
        clientCh.register(selector, SelectionKey.OP_CONNECT);

        clientCh.connect(new InetSocketAddress("127.0.0.1", 9000));
        while (true) {
            selector.select();
            Set<SelectionKey> keys = selector.selectedKeys();
            Iterator<SelectionKey> it = keys.iterator();

            while (it.hasNext()) {
                SelectionKey key = it.next();
                it.remove();

                if (key.isConnectable()) {
                    SocketChannel ch = (SocketChannel) key.channel();
                    ch.finishConnect();
                    if (!ch.finishConnect()) {
                        ch.close();
                        continue;
                    }
                    System.out.println("Connecting with " + ch.getLocalAddress() + " to "
                            + ch.getRemoteAddress());
                    ch.configureBlocking(false);
                    ch.register(selector, SelectionKey.OP_READ);
                }

                if (key.isReadable()) {
                    System.out.println("READ");
                    SocketChannel ch = (SocketChannel) key.channel();
                    ByteBuffer buf = ByteBuffer.allocate(20);
                    int n = ch.read(buf);
                    if (n == -1) {
                        ch.close();
                        continue;
                    }
                    buf.flip();
                    String message = new String(buf.array());
                    System.out.println(message.trim());

                    if (message.trim().equalsIgnoreCase("Connected")) {
                        new Thread(() -> {
                            try {
                                while (true) {
                                    Scanner sc = new Scanner(System.in);
                                    SocketChannel ch2 = (SocketChannel) key.channel();
                                    System.out.println("WRITE");
                                    ByteBuffer buf2 = ByteBuffer.allocate(20);
//                                    System.out.print(ch2.getLocalAddress() + " : ");
                                    String msg = sc.nextLine();
                                    buf2.put(msg.getBytes());
                                    buf2.flip();
                                    ch2.write(buf2);

//                                    ch.read(buf2);
//                                    buf2.flip();
//                                    System.out.println(new String(buf2.array()));
                                }
                            } catch (Exception e) {

                            }
                        }).start();
                    }
                }
            }
        }
    }
}
