package request;

import com.sun.net.httpserver.HttpExchange;

import util.HttpRequest;
/**
 * 处理时间同步请求--暂时无用
 * @author tiang
 * @date 2017-4-19
 * @version 1.0
 */
public class TimeSynRequest extends HttpRequest{

	public TimeSynRequest(HttpExchange ex) {
		super(ex);
		// TODO Auto-generated constructor stub
		initParaMap();
		validTerminalId();
	}

}
