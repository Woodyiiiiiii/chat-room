package processor;

import connector.Request;
import connector.Response;

import java.io.IOException;

/**
 * 静态资源处理类
 * 用来调用Request和Response类
 */
public class StaticProcessor {

  // 处理请求，响应请求
  public void process(Request request, Response response) {
    try {
      response.sendStaticResource();
    } catch (IOException e) {
      e.printStackTrace();
    }
  }
}