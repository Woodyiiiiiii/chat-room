import connector.ConnectorUtils;
import connector.HttpStatus;
import connector.Request;

import javax.servlet.*;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * 处理动态资源的组件——Servlet组件
 * 此类继承Servlet，实现service方法
 * 作用：返回时间(动态)
 * 作为资源的(摆在了webroot目录下)
 */
public class TimeServlet implements Servlet {

  @Override
  public void init(ServletConfig servletConfig) throws ServletException {

  }

  @Override
  public ServletConfig getServletConfig() {
    return null;
  }

  @Override
  public void service(ServletRequest servletRequest, ServletResponse servletResponse) throws ServletException, IOException {
    PrintWriter out = servletResponse.getWriter();
    out.println(ConnectorUtils.renderStatus(HttpStatus.SC_OK));
    out.println("What time is it now?");
    out.println(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
          .format(new Date()));
  }

  @Override
  public String getServletInfo() {
    return null;
  }

  @Override
  public void destroy() {

  }
}
