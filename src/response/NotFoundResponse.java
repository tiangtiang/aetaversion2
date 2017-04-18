package response;

import com.sun.net.httpserver.HttpExchange;

import util.HttpResponse;

public class NotFoundResponse extends HttpResponse {

	public NotFoundResponse(HttpExchange ex) {
		super(ex);
		// TODO Auto-generated constructor stub
	}

	@Override
	public void response() {
		// TODO Auto-generated method stub
		write(404, "找不到访问地址"+exchange.getRequestURI().toString());
	}

}
