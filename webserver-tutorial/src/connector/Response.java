package connector;

import javax.servlet.ServletOutputStream;
import javax.servlet.ServletResponse;
import java.io.*;
import java.util.Locale;

/*
响应消息结构:
  HTTP/1.1 200 OK

作用：
  1. 获取资源(文件)
  2. 发送状态消息和文件
 */
public class Response implements ServletResponse {

  private static final int BUFFER_SIZE = 1024;

  // 我们需要知道Response对应的Request，从而得到URI信息
  Request request;
  // 通过OutputStream来输出信息
  OutputStream output;

  public Response(OutputStream output) {
    this.output = output;
  }

  public void setRequest(Request request) {
    this.request = request;
  }

  /**
   * 发送静态资源
   * @throws IOException IO读写异常
   */
  public void sendStaticResource() throws IOException {
    // 生成所要发送的文件，参数1是主目录，参数2是主目录下的路径
    File file = new File(ConnectorUtils.WEB_ROOT, request.getRequestURI());
    // 响应请求：发送文件和消息
    try {
      write(file, HttpStatus.SC_OK);
    } catch (IOException e) {
      write(new File(ConnectorUtils.WEB_ROOT, "404.html"), HttpStatus.SC_NOT_FOUND);
    }
  }

  /**
   * 将文件和信息从OutputStream中发送
   * @param resource 资源(文件)
   * @param status 状态，比如404，200
   * @throws IOException IO读写异常
   */
  private void write(File resource, HttpStatus status) throws IOException {
    // JDK1.8特性，try括号内的资源会在try语句结束后自动释放
    // 前提是这些可关闭的资源必须实现java.lang.AutoCloseable接口
    try (FileInputStream fis = new FileInputStream(resource)) {
      // 先发送消息头部
      output.write(ConnectorUtils.renderStatus(status).getBytes());
      // 要将文件转换成Buffer(byte数组)
      byte[] buffer = new byte[BUFFER_SIZE];
      int length = 0;
      // 文件比较大，所以需要while循环
      while ((length = fis.read(buffer, 0, BUFFER_SIZE)) != -1) {
        output.write(buffer, 0, length);
      }
    }
  }

  /*
  下面都是ServletResponse需要继承的方法
   */

  @Override
  public String getCharacterEncoding() {
    return null;
  }

  @Override
  public ServletOutputStream getOutputStream() throws IOException {
    return null;
  }

  /**
   * 返回OutputStream
   * @return 返回PrintWriter
   * @throws IOException 异常
   */
  @Override
  public PrintWriter getWriter() throws IOException {
    PrintWriter writer = new PrintWriter(output, true);
    return writer;
  }

  @Override
  public void setContentLength(int i) {

  }

  @Override
  public void setContentType(String s) {

  }

  @Override
  public void setBufferSize(int i) {

  }

  @Override
  public int getBufferSize() {
    return 0;
  }

  @Override
  public void flushBuffer() throws IOException {

  }

  @Override
  public void resetBuffer() {

  }

  @Override
  public boolean isCommitted() {
    return false;
  }

  @Override
  public void reset() {

  }

  @Override
  public void setLocale(Locale locale) {

  }

  @Override
  public Locale getLocale() {
    return null;
  }
}