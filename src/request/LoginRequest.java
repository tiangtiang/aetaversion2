package request;

import com.sun.net.httpserver.HttpExchange;

import receive.Session;
import util.HttpRequest;

public class LoginRequest extends HttpRequest {

	String terminalId;
	String password;
	public LoginRequest(HttpExchange ex) {
		super(ex);
		// TODO Auto-generated constructor stub
		terminalId = getParam("terminalId");
		password = getParam("password");
	}

	public boolean login(){
		if(terminalId == null || password == null){
			badRequest = true;
			return false;
		}else{
			if(terminalId.equals("tiang")&&password.equals("123456"))
				return true;
			else
				return false;
		}
	}
	
	public String keepSession(){
		Session ss = new Session();
		ss.insert(terminalId);
		return ss.getSessionId(terminalId);
	}
}
