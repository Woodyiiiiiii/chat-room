package client;

import java.io.*;
import java.net.Socket;

/**
 * 聊天室客户端
 * 用来连接服务器并转发消息
 * 提供函数让Handler线程调用
 */
public class ChatClient {

    private final String DEFAULT_SERVER_HOST = "127.0.0.1";
    private final Integer DEFAULT_SERVER_PORT = 8888;
    private final String QUIT_MSG = "quit";

    private Socket socket;
    private BufferedReader reader;
    private BufferedWriter writer;

    /**
     * 发送消息给服务器
     * @param msg 字符串消息
     * @throws IOException 异常
     */
    public void send(String msg) throws IOException {
        if (!socket.isInputShutdown()) {
            writer.write(msg + "\n");
            writer.flush();
        }
    }

    // 从服务器端接收消息
    public String receive() throws IOException {
        String msg = null;
        if (!socket.isInputShutdown()) {
            msg = reader.readLine();
        }
        return msg;
    }

    // 检查用户是否准备退出
    public boolean readyToQuit(String msg) {
        return QUIT_MSG.equals(msg);
    }

    public void close() {
        if (writer != null) {
            try {
                System.out.println("关闭socket");
                writer.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    // 启动
    public void start() {
        // 创建Socket对象
        try {
            // 主逻辑函数，所以在函数内try catch，不是在函数外
            socket = new Socket(DEFAULT_SERVER_HOST, DEFAULT_SERVER_PORT);

            // 创建IO流
            reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            writer = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));

            // 处理用户的输入，多线程
            new Thread(new UserInputHandler(this)).start();

            // 处理服务器转发的消息
            String msg = null;
            while ((msg = receive()) != null) {
                System.out.println(msg);
            }

        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            close();
        }
    }

    public static void main(String[] args) {
        ChatClient chatClient = new ChatClient();
        chatClient.start();
    }
}
