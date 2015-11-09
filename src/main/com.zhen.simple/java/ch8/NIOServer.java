package ch8;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Iterator;

/**
 * Created by Administrator on 2015/11/8.
 */
public class NIOServer {

    private Selector selector;

    public void initServer(int port) throws IOException {
        //获得一个serverSocket通道
        ServerSocketChannel sChannel  = ServerSocketChannel.open();
        //设置通道为非阻塞
        sChannel.configureBlocking(false);
        //将该通道对应的serverSocket绑定到port端口
        sChannel.socket().bind(new InetSocketAddress(port));
        //获得一个通道管理器
        this.selector = Selector.open();
        //将该通道管理器和通道绑定，并未该通道注册SelectionKey.OP_ACCEPT时间，注册该事件后，当该事件到达会返回selector.select();
        //如果该事件没有到达则selector.select()会一直阻塞
        sChannel.register(selector, SelectionKey.OP_ACCEPT);
    }

    /**
     * 采用轮询的方式监听selector上是否有需要处理的事件；如果有测进行处理，
     * @param args
     */
    public void listen()throws Exception{

        System.out.println("server start:");
        //轮询selector
        while(true){
            //当注册事件到达时，方法返回;否则该方法一直阻塞
            selector.select();
            //获得selector中的迭代器，选中的项为注册的事件
            Iterator ite = this.selector.selectedKeys().iterator();
            while(ite.hasNext()){
                Thread.sleep(1000);
                SelectionKey key = (SelectionKey)ite.next();
                //删除已选的可以，以防重复处理
                ite.remove();
                //客户端请求连接事件
                if(key.isAcceptable()){
                    ServerSocketChannel serverSocketChannel = (ServerSocketChannel)key.channel();
                    //获取和客户端连接的通道
                    SocketChannel channel = serverSocketChannel.accept();
                    channel.configureBlocking(false);
                    //这里开始给客户端发送通信吧
                    channel.write(ByteBuffer.wrap(new String("server send message").getBytes()));
                    //在和客户端建立连接后，为了可以接收客户端的消息，需要给通道设置读的权限。
                    channel.register(selector,SelectionKey.OP_READ,SelectionKey.OP_WRITE);
                }else if(key.isReadable()){
                    read(key);
                }
            }
        }
    }

    /**
     * 处理读取客户端发来的消息的事件
     * @param key
     * @throws IOException
     */
    public void read(SelectionKey key) throws IOException{
        //服务器可读取消息：得到发生的Socket通道
        SocketChannel channel = (SocketChannel)key.channel();
        ByteBuffer buffer = ByteBuffer.allocate(20);
        channel.read(buffer);
        byte[] data = buffer.array();
        String msg = new String(data).trim();
        System.out.println("server accept "+msg);
        msg ="server return";
        ByteBuffer outBuffer = ByteBuffer.wrap(msg.getBytes());
        //将消息会送给客户端
        channel.write(outBuffer);
    }

    public static void main(String[] args) throws Exception{
        NIOServer server = new NIOServer();
        server.initServer(8000);
        server.listen();
    }
}
