package server;

import java.io.Closeable;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.nio.channels.*;
import java.nio.charset.Charset;
import java.util.Set;

public class ChatServer {

    private static final int DEFAULT_PORT = 8888;
    private static final String QUIT = "quit";
    // 1024个字节的缓冲区
    private static final int BUFFER = 1024;

    private ServerSocketChannel server;
    // 不同于BIO的多线程，用Selector+单线程实现
    private Selector selector;
    // 从通道中读取消息
    private ByteBuffer rBuffer = ByteBuffer.allocate(BUFFER);
    // 向通道中写入消息
    private ByteBuffer wBuffer = ByteBuffer.allocate(BUFFER);
    // 统一解码
    private Charset charset = Charset.forName("UTF-8");
    // 自定义端口
    private int port;

    public ChatServer() {
        this(DEFAULT_PORT);
    }

    public ChatServer(int port) {
        this.port = port;
    }

    /**
     * 主要实现逻辑函数
     */
    public void start() {
        try {
            // 开启通道，默认阻塞
            server = ServerSocketChannel.open();
            // 开启非阻塞——BIO没有的
            server.configureBlocking(false);
            // socket函数返回绑定的serverSocket
            // bind函数将serverSocket绑定到对应端口
            server.socket().bind(new InetSocketAddress(port));

            // 创建selector
            selector = Selector.open();
            // 将serverSocketChannel注册到selector上，并开启accept状态监听
            // 以后一旦serverSocketChannel接收某个客户端连接，就能被发现
            server.register(selector, SelectionKey.OP_ACCEPT);
            // log
            System.out.println("启动服务器，监听端口" + port + "...");

            while (true) {
                // 至少有一个Channel注册的状态触发，否则阻塞
                // 这就是和BIO不同的地方，因为非阻塞，所以可以连接多个客户端
                selector.select();
                // 返回所有被触发的状态的集合
                Set<SelectionKey> selectionKeys = selector.selectedKeys();
                // handles处理过程——处理被触发的事件
                for (SelectionKey selectionKey : selectionKeys) {
                    handles(selectionKey);
                }
                // 清除状态，否则所有状态会一直保留，影响下一次
                selectionKeys.clear();
            }

        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            // 关闭selector，同时也会关闭所有被注册的channel
            close(selector);
        }
    }

    /**
     * 处理状态分为：accept(ServerSocketChannel), read(SocketChannel)
     * @param selectionKey 状态
     */
    private void handles(SelectionKey selectionKey) throws IOException {
        // ACCEPT事件——和客户端建立连接
        if (selectionKey.isAcceptable()) {
            ServerSocketChannel serverChannel = (ServerSocketChannel) selectionKey.channel();
            SocketChannel client = serverChannel.accept();
            client.configureBlocking(false);
            client.register(selector, SelectionKey.OP_READ);
            System.out.println(getClientName(client) + "已连接到服务器");
        }
        // READ事件——客户端向服务器端发送消息
        else if (selectionKey.isReadable()) {
            // 1.读取客户端发送的消息
            SocketChannel client = (SocketChannel) selectionKey.channel();
            String fwdMsg = receive(client);
            // 2. 转发给其他客户端
            if (fwdMsg.isEmpty()) {
                // 空消息，说明异常
                // 停止监听
                selectionKey.cancel();
                // 通知selector把当前select的调用马上返回(因为可以选择阻塞)
                selector.wakeup();
            }else {
                System.out.println(getClientName(client) + ":" + fwdMsg);
                // 转发给其他客户端
                forwardMessage(client, fwdMsg);

                // 检查用户是否退出
                if (readyToQuit(fwdMsg)) {
                    selectionKey.cancel();
                    selector.wakeup();
                    System.out.println(getClientName(client) + "已断开");
                }
            }
        }
    }

    private String getClientName(SocketChannel socketChannel) {
        return "客户端[" + socketChannel.socket().getPort() + "]";
    }

    /**
     * 从客户端中获取要转发的消息
     * @param client 客户端socketChannel
     * @return 转发的字符串
     */
    private String receive(SocketChannel client) throws IOException {
        // 把残留的消息清空
        rBuffer.clear();
        // 只要rBuffer能从client中读出数据
        while (client.read(rBuffer) > 0);
        // 将Buffer的写模式转换成读模式
        rBuffer.flip();
        // 按照UTF-8的格式，将rBuffer中的数据转换成字符串
        return String.valueOf(charset.decode(rBuffer));
    }

    /**
     * 将当前client发送的消息fwdMsg给其他客户端
     * @param client 消息源客户端
     * @param fwdMsg 转发消息
     */
    private void forwardMessage(SocketChannel client, String fwdMsg) throws IOException {
        for (SelectionKey key : selector.keys()) {
            if (key.isValid() && key.channel() instanceof SocketChannel) {
                SocketChannel connectedClient = (SocketChannel) key.channel();
                if (!client.equals(connectedClient)) {
                    wBuffer.clear();
                    wBuffer.put(charset.encode(getClientName(connectedClient) + ":" + fwdMsg));
                    wBuffer.flip();
                    while (wBuffer.hasRemaining()) {
                        connectedClient.write(wBuffer);
                    }
                }
            }
        }

    }

    private boolean readyToQuit(String msg) {
        return QUIT.equals(msg);
    }

    /**
     * 关闭IO
     * @param closable 系统IO资源
     */
    private void close(Closeable closable) {
        if (closable != null) {
            try {
                closable.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public static void main(String[] args) {
        ChatServer chatServer = new ChatServer(7777);
        chatServer.start();
    }

}
