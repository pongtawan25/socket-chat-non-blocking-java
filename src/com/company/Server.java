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
                    clientCh.register(selector, SelectionKey.OP_WRITE);
                }

                if (key.isWritable()) {
                    System.out.println("Writing Connection");
                    SocketChannel ch = (SocketChannel) key.channel();
                    ByteBuffer buf = ByteBuffer.allocate(20);
                    String message = "Connected";
                    buf.put(message.getBytes());
                    buf.flip();
                    ch.write(buf);
                    ch.configureBlocking(false);
                    ch.register(selector, SelectionKey.OP_READ);
                }

                if (key.isReadable()) {
                    System.out.println("Reading");
                    SocketChannel ch = (SocketChannel) key.channel();
                    ByteBuffer buf = ByteBuffer.allocate(20);
                    ch.read(buf);
                    buf.flip();

                    int i = 1;
                    for (SocketChannel client : clientChList) {
                        System.out.println(i + " " + client.getRemoteAddress()
                                + " : " + new String(buf.array()));
                        i++;
                        client.write(buf);
                    }
                }
            }
        }
    }
}
