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
        //���һ��serverSocketͨ��
        ServerSocketChannel sChannel  = ServerSocketChannel.open();
        //����ͨ��Ϊ������
        sChannel.configureBlocking(false);
        //����ͨ����Ӧ��serverSocket�󶨵�port�˿�
        sChannel.socket().bind(new InetSocketAddress(port));
        //���һ��ͨ��������
        this.selector = Selector.open();
        //����ͨ����������ͨ���󶨣���δ��ͨ��ע��SelectionKey.OP_ACCEPTʱ�䣬ע����¼��󣬵����¼�����᷵��selector.select();
        //������¼�û�е�����selector.select()��һֱ����
        sChannel.register(selector, SelectionKey.OP_ACCEPT);
    }

    /**
     * ������ѯ�ķ�ʽ����selector���Ƿ�����Ҫ������¼�������в���д���
     * @param args
     */
    public void listen()throws Exception{

        System.out.println("server start:");
        //��ѯselector
        while(true){
            //��ע���¼�����ʱ����������;����÷���һֱ����
            selector.select();
            //���selector�еĵ�������ѡ�е���Ϊע����¼�
            Iterator ite = this.selector.selectedKeys().iterator();
            while(ite.hasNext()){
                Thread.sleep(1000);
                SelectionKey key = (SelectionKey)ite.next();
                //ɾ����ѡ�Ŀ��ԣ��Է��ظ�����
                ite.remove();
                //�ͻ������������¼�
                if(key.isAcceptable()){
                    ServerSocketChannel serverSocketChannel = (ServerSocketChannel)key.channel();
                    //��ȡ�Ϳͻ������ӵ�ͨ��
                    SocketChannel channel = serverSocketChannel.accept();
                    channel.configureBlocking(false);
                    //���￪ʼ���ͻ��˷���ͨ�Ű�
                    channel.write(ByteBuffer.wrap(new String("server send message").getBytes()));
                    //�ںͿͻ��˽������Ӻ�Ϊ�˿��Խ��տͻ��˵���Ϣ����Ҫ��ͨ�����ö���Ȩ�ޡ�
                    channel.register(selector,SelectionKey.OP_READ,SelectionKey.OP_WRITE);
                }else if(key.isReadable()){
                    read(key);
                }
            }
        }
    }

    /**
     * �����ȡ�ͻ��˷�������Ϣ���¼�
     * @param key
     * @throws IOException
     */
    public void read(SelectionKey key) throws IOException{
        //�������ɶ�ȡ��Ϣ���õ�������Socketͨ��
        SocketChannel channel = (SocketChannel)key.channel();
        ByteBuffer buffer = ByteBuffer.allocate(20);
        channel.read(buffer);
        byte[] data = buffer.array();
        String msg = new String(data).trim();
        System.out.println("server accept "+msg);
        msg ="server return";
        ByteBuffer outBuffer = ByteBuffer.wrap(msg.getBytes());
        //����Ϣ���͸��ͻ���
        channel.write(outBuffer);
    }

    public static void main(String[] args) throws Exception{
        NIOServer server = new NIOServer();
        server.initServer(8000);
        server.listen();
    }
}
