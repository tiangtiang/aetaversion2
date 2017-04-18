package response;

import com.sun.net.httpserver.HttpExchange;

import util.HttpResponse;

public class LoginResponse extends HttpResponse{

	public LoginResponse(HttpExchange ex) {
		super(ex);
		// TODO Auto-generated constructor stub
	}

	@Override
	public void response() {
		// TODO Auto-generated method stub
		write(this.getClass().toString());
	}
	
}
