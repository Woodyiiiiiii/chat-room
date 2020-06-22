package connector;

import org.junit.Assert;
import org.junit.Test;
import util.TestUtils;

/**
 * 测试connector包中的Request类能否正确解析请求，获取uri
 */
public class RequestTest {

    private static final String validRequest = "GET /index.html HTTP/1.1";

    @Test
    public void givenValidRequest_thenExtrackUri() {
        Request request = TestUtils.createRequest(validRequest);
        // 检查uri是否等于/index.html
        // 如果等于，则检查通过；否则会将输出结果和期待结果一同输出作对比
        // Assert来自junit包
        Assert.assertEquals("/index1.html", request.getRequestURI());
    }
}
