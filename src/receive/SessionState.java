package receive;
/**
 * session的状态
 * @author tiang
 * @date 2017-4-19
 * @version 1.0
 */
public enum SessionState {
	TimeOut,		//超时
	NotExist,		//服务器端不存在session
	NotSend,		//请求未发送session
	Invalid,		//session值不匹配
	Valid			//session有效
}
