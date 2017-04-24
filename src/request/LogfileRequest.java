package request;

import com.sun.net.httpserver.HttpExchange;

import util.HttpRequest;

public class LogfileRequest extends HttpRequest {

	public LogfileRequest(HttpExchange ex) {
		super(ex);
		// TODO Auto-generated constructor stub
		initParaMap();
	}
	
}
