package server;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * 聊天室服务器实现
 * 用来连接客户端，并不真正读写数据
 */
public class ChatServer {

    private int DEFAULT_PORT = 8888;
    private final String QUIT_MSG = "quit";

    /**
     * 使用线程池实现伪异步IO模型
     */
    private ExecutorService executorService;

    private ServerSocket serverSocket;
    private Map<Integer, Writer> connectedClients;

    public ChatServer() {
        /**
         * 以前的同步IO模型，多少个用户就开多少个线程
         * 这样十分浪费资源，使用线程池改进这个模型
         * 限制10个线程，如果第11个用户请求连接，则
         * 必须等待前10个线程中是否有一个释放，才能
         * 使用，否则等待
         */
        executorService = Executors.newFixedThreadPool(10);
        connectedClients = new HashMap<>();
    }

    /**
     * 新的客户端加入到服务器中
     * 注意：因为是多线程环境，map的put函数会造成
     *      map的不正常状态，因为是简单的多人聊天室，
     *      所以用最简单的synchronize方法，这样就只有
     *      一个线程能调用这个函数
     * @param socket 客户端对应的Socket
     * @throws IOException IO异常
     */
    public synchronized void addClient(Socket socket) throws IOException {
        if (socket != null) {
            int port = socket.getPort();
            BufferedWriter writer = new BufferedWriter(
                    new OutputStreamWriter(socket.getOutputStream())
            );
            connectedClients.put(port, writer);
            System.out.println("客户端[" + port + "]已连接到服务器");
        }
    }

    /**
     * 移除并关闭客户端
     * 为了线程安全，同样添加synchronize关键字
     * @param socket 客户端Socket
     * @throws IOException IO异常
     */
    public synchronized void removeClient(Socket socket) throws IOException {
        if (socket != null) {
            int port = socket.getPort();
            // 同例子，先关闭writer(等于关闭socket)，后移出HashMap
            if (connectedClients.containsKey(port)) {
                connectedClients.get(port).close();
            }
            connectedClients.remove(port);
            System.out.println("客户端[" + socket.getPort() + "]已断开连接");
        }
    }

    /**
     * 转发消息给其他的客户端
     * 遍历HashMap
     * @param socket 消息传送端口的Socket
     * @param fwdMsg   消息(字符串)
     * @throws IOException  IO异常
     */
    public synchronized void forwardMsg(Socket socket, String fwdMsg) throws IOException {
        for (Integer id : connectedClients.keySet()) {
            if (!id.equals(socket.getPort())) {
                Writer writer = connectedClients.get(id);
                writer.write(fwdMsg);
                writer.flush();
            }
        }
    }

    /**
     * 检查用户是否退出，由服务器端处理
     * @param msg 用户消息
     * @return 是否退出
     */
    public boolean readyToQuit(String msg) {
        return msg.equals(QUIT_MSG);
    }

    /**
     * 服务器关闭
     * 会更新server Socket的状态，同样需要线程安全
     */
    public synchronized void close() {
        if (serverSocket != null) {
            try {
                serverSocket.close();
                System.out.println("关闭服务器serverSocket");
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * 服务器启动
     */
    public void start() {
        try {
            // 绑定监听端口
            serverSocket = new ServerSocket(DEFAULT_PORT);
            System.out.println("启动服务器，监听端口" + DEFAULT_PORT + "...");

            while (true) {
                // 等待客户端连接
                Socket socket = serverSocket.accept();
                // 创建ChatHandler线程
//                new Thread(new ChatHandler(this, socket)).start();
                // 线程池创建ChatHandler线程
                executorService.execute(new ChatHandler(this, socket));
            }

        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            close();
        }
    }

    /**
     * 主函数启动服务器
     * @param args
     */
    public static void main(String[] args) {
        ChatServer chatServer = new ChatServer();
        chatServer.start();
    }
}
