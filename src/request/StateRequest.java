package request;

import util.HttpRequest;

import com.sun.net.httpserver.HttpExchange;

public class StateRequest extends HttpRequest {

	public StateRequest(HttpExchange ex) {
		super(ex);
		// TODO Auto-generated constructor stub
		initParaMap();
		validTerminalId();
		String type = getType();
		if (type.equalsIgnoreCase("dev03"))
			validProbeId();
	}

}
