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

    //ͨ��������
    private Selector selector;

    /**
     * ���һ��Socketͨ�������Ը�ͨ����һЩ��ʼ������
     * @param ip ���ӷ�������ip
     * @param port ���ӷ������Ķ˿ں�
     * @throws IOException
     */
    public void initClient(String ip,int port)throws IOException{
        //���һ��socketͨ��
        SocketChannel channel = SocketChannel.open();
        //����Ϊ������ͨ��
        channel.configureBlocking(false);
        //���һ��ͨ��������
        this.selector = Selector.open();
        //�ͻ������ӷ���������ʵ������û��ʵ�����ӣ���Ҫ��listen()�����е�
        //��channel.finishConnect();�����������
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
        //��ѵ����selector
        while (true){
            Thread.sleep(1000);
            selector.select();
            //���selector��ѡ�еĵ�����
            Iterator ite = this.selector.selectedKeys().iterator();
            while(ite.hasNext()){
                SelectionKey key = (SelectionKey)ite.next();
                //ɾ����ѡ�Ŀ��ԣ��Է��ظ�����
                ite.remove();
                // �ͻ������������¼�
                if(key.isConnectable()){
                    SocketChannel channel = (SocketChannel)key.channel();
                    //����������ӣ����������
                    if(channel.isConnectionPending()){
                        channel.finishConnect();
                    }
                    //���óɷ�����
                    channel.configureBlocking(false);
                    //���������������Ϣ
                    channel.write(ByteBuffer.wrap("client send message".getBytes()));
                    //�ںͷ��������ӳɹ���Ϊ�˿��Խ��ܵ�����������Ϣ����Ҫ��ͨ�����ö���Ȩ�ޡ�
                    channel.register(this.selector,SelectionKey.OP_READ);

                    // ����˿ɶ����¼�
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
     * ������������͵���Ϣ ���¼�
     * @param key
     * @throws IOException
     */
    public void read(SelectionKey key) throws IOException{

        //�ͻ��˿ɶ�ȡ����Ϣ���õ��¼����͵�ͨ��
        SocketChannel  channel = (SocketChannel )key.channel();
        //������ȡ������
        ByteBuffer buffer = ByteBuffer.allocate(20);
        channel.read(buffer);
        byte[] data = buffer.array();
        String msg = new String(data).trim();
        System.out.println("client accept "+msg);
        msg="client return";
        ByteBuffer outBuffer = ByteBuffer.wrap(msg.getBytes());
        //����Ϣ���͸��ͻ���
        channel.write(outBuffer);
    }

    /**
     * ������������͵���Ϣ ���¼�
     * @param key
     * @throws IOException
     */
    public void write(SelectionKey key) throws IOException{

        //�ͻ��˿ɶ�ȡ����Ϣ���õ��¼����͵�ͨ��
        SocketChannel  channel = (SocketChannel )key.channel();
        //������ȡ������
        ByteBuffer buffer = ByteBuffer.allocate(20);
        channel.read(buffer);
        byte[] data = buffer.array();
        String msg = new String(data).trim();
        System.out.println("client accept "+msg);
        msg="client return";
        ByteBuffer outBuffer = ByteBuffer.wrap(msg.getBytes());
        //����Ϣ���͸��ͻ���
        channel.write(outBuffer);
    }


    public static void main(String[] args)throws Exception{

        NIOClient client = new NIOClient();
        client.initClient("127.0.0.1",8000);
        client.listen();
    }
}
