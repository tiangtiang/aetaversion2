package response;

import com.sun.net.httpserver.HttpExchange;

import util.HttpResponse;

public class BadRequestResponse extends HttpResponse{

	public BadRequestResponse(HttpExchange ex) {
		super(ex);
		// TODO Auto-generated constructor stub
	}

	@Override
	public void response() {
		// TODO Auto-generated method stub
		write(400, "BadRequest");
	}

}
