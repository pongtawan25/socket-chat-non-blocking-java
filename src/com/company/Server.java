package com.company;

import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Set;

public class Server {

    public static void main(String[] args) throws Exception {
        Selector selector = Selector.open();

        ServerSocketChannel serverCh = ServerSocketChannel.open();
        serverCh.configureBlocking(false);
        serverCh.bind(new InetSocketAddress(9000));
        serverCh.register(selector, SelectionKey.OP_ACCEPT);

        ArrayList<SocketChannel> clientChList = new ArrayList<>();
        System.out.println("Server Listening on port 9000");
        while (true) {
            selector.select();
            System.out.println("Got some event");
            Set<SelectionKey> keys = selector.selectedKeys();
            Iterator<SelectionKey> it = keys.iterator();

            while (it.hasNext()) {
                SelectionKey key = it.next();
                it.remove();

                if (key.isAcceptable()) {
                    ServerSocketChannel ch = (ServerSocketChannel) key.channel();
                    SocketChannel clientCh = ch.accept();
                    clientChList.add(clientCh);
                    System.out.println("Connect from " + clientCh.getRemoteAddress());
                    clientCh.configureBlocking(false);
                    clientCh.register(selector, SelectionKey.OP_READ);
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
                    String message = new String(buf.array());

                    int i = 1;
                    for (SocketChannel client : clientChList) {
                        ByteBuffer buf2 = ByteBuffer.allocate(20);
                        buf2.put(message.getBytes());
                        buf2.flip();
                        System.out.println(i + " " + client.getRemoteAddress()
                                + " : " + new String(buf2.array()));
                        client.write(buf2);
                        i++;
                    }
                }
            }
        }
    }
}
