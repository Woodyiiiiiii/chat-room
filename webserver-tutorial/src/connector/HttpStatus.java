package connector;

/**
 * 模拟HTTP协议的消息结构
 * 枚举体结构(枚举类)，其实就是一个类
 * 在某些情况下，一个类的对象是有限且固定的，如季节类，这种叫枚举类
 * 和普通的类一样，有自己的成员变量、成员方法、构造器
 */
public enum HttpStatus {
    SC_OK(200, "OK"),
    SC_NOT_FOUND(404, "File Not Found");

    private int statusCode;
    private String reason;

    HttpStatus(int code, String reason) {
        this.statusCode = code;
        this.reason = reason;
    }

    public int getStatusCode() {
        return statusCode;
    }

    public String getReason() {
        return reason;
    }
}