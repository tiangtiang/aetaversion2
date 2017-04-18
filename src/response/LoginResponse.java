package response;

import com.sun.net.httpserver.HttpExchange;

import request.LoginRequest;
import util.HttpResponse;

public class LoginResponse extends HttpResponse{

	private LoginRequest request;
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
			new BadRequestResponse(exchange).response();
		else
			write(getResponseString(login));
	}
	
	private String getResponseString(boolean login){
		if(login){
			return "result=success&sessionId="+request.keepSession();
		}else{
			return "result=fail";
		}
	}
}
