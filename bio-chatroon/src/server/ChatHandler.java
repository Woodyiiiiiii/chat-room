package server;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;

/**
 * 聊天室监听器实现
 * 用来处理用户输入的数据，每个客户端对应一个监听器
 * 读取对应的客户端Socket的消息，打印，并传给其他客户端
 * @author woodyiiiiiii
 */
public class ChatHandler implements Runnable {

    private ChatServer chatServer;
    private Socket socket;

    /**
     * @param chatServer 服务器端
     * @param socket 对应的客户端接口
     */
    public ChatHandler(ChatServer chatServer, Socket socket) {
        this.chatServer = chatServer;
        this.socket = socket;
    }

    /**
     * 函数功能：ChatHandler线程处理Socket流程
     *          都是交给服务器函数处理，只负责
     *          调用服务器函数顺序
     */
    @Override
    public void run() {
        try {
            // 存储新上线的客户
            chatServer.addClient(socket);

            // 读取用户发来的消息
            BufferedReader reader = new BufferedReader(
                    new InputStreamReader(socket.getInputStream())
            );

            String msg = null;
            // readLine函数等待输入，从而实现“客户端中聊天室”功能
            while ((msg = reader.readLine()) != null) {
                // 包装信息，添加客户端端口等信息
                String fwdMsg = "客户端[" + socket.getPort() + "]: " + msg;
                System.out.println(fwdMsg);

                // 将消息转发给聊天室在线的其他用户
                chatServer.forwardMsg(socket, fwdMsg + "\n");

                // 检查用户是否退出，即有无打出quit
                if (chatServer.readyToQuit(msg)) {
                    break;
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                chatServer.removeClient(socket);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
