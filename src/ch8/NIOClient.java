package ch8;


import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.Iterator;

/**
 * Created by Administrator on 2015/11/8.
 */
public class NIOClient {

    //通道管理器
    private Selector selector;

    /**
     * 获得一个Socket通道，并对该通道做一些初始化操作
     * @param ip 连接服务器的ip
     * @param port 连接服务器的端口号
     * @throws IOException
     */
    public void initClient(String ip,int port)throws IOException{
        //获得一个socket通道
        SocketChannel channel = SocketChannel.open();
        //设置为非阻塞通道
        channel.configureBlocking(false);
        //获得一个通道管理器
        this.selector = Selector.open();
        //客户端连接服务器，其实方法并没有实现连接，需要在listen()方法中调
        //用channel.finishConnect();才能完成连接
        channel.connect(new InetSocketAddress(port));
        channel.register(selector, SelectionKey.OP_CONNECT);
    }

    /**
     *
     * @throws IOException
     */
    @SuppressWarnings("unchecked")
    public void listen()throws Exception{
        System.out.println("client start");
        //轮训访问selector
        while (true){
            Thread.sleep(1000);
            selector.select();
            //获得selector中选中的迭代器
            Iterator ite = this.selector.selectedKeys().iterator();
            while(ite.hasNext()){
                SelectionKey key = (SelectionKey)ite.next();
                //删除已选的可以，以防重复处理
                ite.remove();
                // 客户端请求连接事件
                if(key.isConnectable()){
                    SocketChannel channel = (SocketChannel)key.channel();
                    //如果正在连接，则完成连接
                    if(channel.isConnectionPending()){
                        channel.finishConnect();
                    }
                    //设置成非阻塞
                    channel.configureBlocking(false);
                    //这里给服务器发消息
                    channel.write(ByteBuffer.wrap("client send message".getBytes()));
                    //在和服务器连接成功后，为了可以接受到服务器的消息，需要给通道设置读的权限。
                    channel.register(this.selector,SelectionKey.OP_READ);

                    // 获得了可读的事件
                }
                if(key.isReadable()){
                    read(key);
                }
                if(key.isWritable()){
                    write(key);
                }
            }

        }

    }

    /**
     * 处理服务器发送的消息 的事件
     * @param key
     * @throws IOException
     */
    public void read(SelectionKey key) throws IOException{

        //客户端可读取的消息：得到事件发送的通道
        SocketChannel  channel = (SocketChannel )key.channel();
        //创建读取缓冲区
        ByteBuffer buffer = ByteBuffer.allocate(20);
        channel.read(buffer);
        byte[] data = buffer.array();
        String msg = new String(data).trim();
        System.out.println("client accept "+msg);
        msg="client return";
        ByteBuffer outBuffer = ByteBuffer.wrap(msg.getBytes());
        //将消息会送给客户端
        channel.write(outBuffer);
    }

    /**
     * 处理服务器发送的消息 的事件
     * @param key
     * @throws IOException
     */
    public void write(SelectionKey key) throws IOException{

        //客户端可读取的消息：得到事件发送的通道
        SocketChannel  channel = (SocketChannel )key.channel();
        //创建读取缓冲区
        ByteBuffer buffer = ByteBuffer.allocate(20);
        channel.read(buffer);
        byte[] data = buffer.array();
        String msg = new String(data).trim();
        System.out.println("client accept "+msg);
        msg="client return";
        ByteBuffer outBuffer = ByteBuffer.wrap(msg.getBytes());
        //将消息会送给客户端
        channel.write(outBuffer);
    }


    public static void main(String[] args)throws Exception{

        NIOClient client = new NIOClient();
        client.initClient("127.0.0.1",8000);
        client.listen();
    }
}
