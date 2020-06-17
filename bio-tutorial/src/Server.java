import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * 服务器端
 */
public class Server {
    public static void main(String[] args) {
        final String QUIT_MSG = "quit";
        final int DEFAULT_PORT = 8888;
        ServerSocket serverSocket = null;

        // 绑定监听端口
        try {
            serverSocket = new ServerSocket(DEFAULT_PORT);
            System.out.println("启动服务器，监听端口：" + DEFAULT_PORT);
            while (true) {
                // 等待客户端连接，阻塞式
                Socket socket = serverSocket.accept();
                System.out.println("客户端[" + socket.getPort() + "]已连接");
                BufferedReader reader = new BufferedReader(
                        new InputStreamReader(socket.getInputStream())
                );
                BufferedWriter writer = new BufferedWriter(
                        new OutputStreamWriter(socket.getOutputStream())
                );

                // 读取客户端消息
                String msg = null;
                while ((msg = reader.readLine()) != null) {
                    System.out.println("客户端[" + socket.getPort() + "]：" + msg);

                    // 回复客户发送的消息
                    writer.write("服务器：" + msg + "\n");
                    // 刷新缓冲区，确保清理
                    writer.flush();

                    // 查看客户端是否退出
                    if (QUIT_MSG.equals(msg)) {
                        System.out.println("客户端[" + socket.getPort() + "]已退出");
                        break;
                    }
                }
            }
        }catch (IOException e) {
            e.printStackTrace();
        }finally {
            if (serverSocket != null) {
                try {
                    serverSocket.close();
                    System.out.println("关闭serverSocket");
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
