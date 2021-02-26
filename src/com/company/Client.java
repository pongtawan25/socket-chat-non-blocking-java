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
        Scanner sc = new Scanner(System.in);
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

                    new Thread(() -> {
                        while (true) {
                            try {
                                ByteBuffer buf = ByteBuffer.allocate(20);
                                String massage = sc.nextLine();
                                buf.put(massage.getBytes());
                                buf.flip();
                                ch.write(buf);
                            } catch (Exception e) {

                            }
                        }
                    }).start();

                    System.out.println("Connecting with " + ch.getLocalAddress() + " to "
                            + ch.getRemoteAddress());
                    ch.configureBlocking(false);
                    ch.register(selector, SelectionKey.OP_READ);
                }

                if (key.isReadable()) {
                    SocketChannel ch = (SocketChannel) key.channel();
                    ByteBuffer buf = ByteBuffer.allocate(20);
                    int n = ch.read(buf);
                    if (n == -1) {
                        ch.close();
                        continue;
                    }
                    buf.flip();
                    System.out.println(new String(buf.array()));
                }

            }
        }
    }
}
