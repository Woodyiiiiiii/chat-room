package connector;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletInputStream;
import javax.servlet.ServletRequest;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.util.Enumeration;
import java.util.Locale;
import java.util.Map;

/*
请求信息结构是类似HTTP协议的消息结构
GET /index.html HTTP/1.1
        Host: localhost:8888
        Connection: keep-alive
        Cache-Control: max-age=0
        Upgrade-Insecure-Requests: 1
        User-Agent: Mozilla/5.0 (Macintosh; Intel Mac OS X 10_13_6) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/77.0.3865.90 Safari/537.36
*/

/*
  该类的主要逻辑函数是parse方法
  目的是处理并解析请求，获取URI字符串
 */

public class Request implements ServletRequest {

  private static final int BUFFER_SIZE = 1024;

  // 与Socket连接的InputStream
  private InputStream input;
  private String uri;

  public Request(InputStream input) {
    this.input = input;
  }

  public String getRequestURI() {
    return uri;
  }

  /**
   * 解析消息结构
   * 获取uri，了解请求是想要什么资源
   */
  public void parse() {
    int length = 0;
    byte[] buffer = new byte[BUFFER_SIZE];
    // 读取InputStream的消息
    // 将信息保存到Byte数组buffer中
    // 返回的是信息的长度
    try {
      length = input.read(buffer);
    } catch (IOException e) {
      e.printStackTrace();
    }

    // 将字节数组buffer转换成StringBuilder，再转换成String
    StringBuilder request = new StringBuilder();
    for (int j = 0; j < length; ++j) {
      request.append((char)buffer[j]);
    }
    uri = parseUri(request.toString());
  }

  /**
   * 假设消息结构的uri位置是在第一个空格和第二个空格之间
   * 根据空格的位置获取子字符串，从而获取uri
   * @param s 请求字符串
   * @return 获取请求资源字符串名称
   */
  private String parseUri(String s) {
    int index1, index2;
    // indexOf方法返回字符第一个出现在字符串中的位置
    index1 = s.indexOf(' ');
    if (index1 != -1) {
      index2 = s.indexOf(' ', index1 + 1);
      if (index2 > index1) {
        return s.substring(index1 + 1, index2);
      }
    }
    // 请求结构错误，返回空串
    return "";
  }

  @Override
  public Object getAttribute(String s) {
    return null;
  }

  @Override
  public Enumeration getAttributeNames() {
    return null;
  }

  @Override
  public String getCharacterEncoding() {
    return null;
  }

  @Override
  public void setCharacterEncoding(String s) throws UnsupportedEncodingException {

  }

  @Override
  public int getContentLength() {
    return 0;
  }

  @Override
  public String getContentType() {
    return null;
  }

  @Override
  public ServletInputStream getInputStream() throws IOException {
    return null;
  }

  @Override
  public String getParameter(String s) {
    return null;
  }

  @Override
  public Enumeration getParameterNames() {
    return null;
  }

  @Override
  public String[] getParameterValues(String s) {
    return new String[0];
  }

  @Override
  public Map getParameterMap() {
    return null;
  }

  @Override
  public String getProtocol() {
    return null;
  }

  @Override
  public String getScheme() {
    return null;
  }

  @Override
  public String getServerName() {
    return null;
  }

  @Override
  public int getServerPort() {
    return 0;
  }

  @Override
  public BufferedReader getReader() throws IOException {
    return null;
  }

  @Override
  public String getRemoteAddr() {
    return null;
  }

  @Override
  public String getRemoteHost() {
    return null;
  }

  @Override
  public void setAttribute(String s, Object o) {

  }

  @Override
  public void removeAttribute(String s) {

  }

  @Override
  public Locale getLocale() {
    return null;
  }

  @Override
  public Enumeration getLocales() {
    return null;
  }

  @Override
  public boolean isSecure() {
    return false;
  }

  @Override
  public RequestDispatcher getRequestDispatcher(String s) {
    return null;
  }

  @Override
  public String getRealPath(String s) {
    return null;
  }
}