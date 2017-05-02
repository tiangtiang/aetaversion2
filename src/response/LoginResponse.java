package response;

import com.sun.net.httpserver.HttpExchange;

import request.LoginRequest;
import util.HttpResponse;
/**
 * 登录请求响应
 * @author tiang
 * @date 2017-4-18
 * @version 1.0
 */
public class LoginResponse extends HttpResponse{

	private LoginRequest request;		//处理登录请求
	public LoginResponse(HttpExchange ex) {
		super(ex);
		// TODO Auto-generated constructor stub
		request = new LoginRequest(exchange);
	}

	@Override
	public void response() {
		// TODO Auto-generated method stub
		boolean login = request.login();
		if(request.isBadRequest())
			new BadRequestResponse(exchange, request.getResult()).response();
		else
			write(getResponseString(login));
	}
	/**
	 * 返回响应字符串，如果成功，则响应成功，并返回sessionId，如果失败，则响应失败
	 * @author tiang
	 * @date 2017-4-18
	 * @version 1.0
	 * @param login 登录是否成功
	 * @return 响应字符串
	 */
	private String getResponseString(boolean login){
		if(login){
			return "result=success&sessionId="+request.keepSession();
		}else{
			//用户名密码不正确
			return "result=fail&failCode=FC_002&failReason=terminalId or password was not correct";
		}
	}
}
