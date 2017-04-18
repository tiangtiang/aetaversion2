package receive;

import util.HttpRequest;
import util.HttpResponse;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
/**
 * 处理http请求，在这个类中根据不同的http请求调用不同的响应类进行响应
 * @author tiang
 * @date 2017-4-17
 * @version 1.0
 */
public class ReceiveHandler implements HttpHandler{

	/**
	 * 处理http请求
	 * @author tiang
	 * @date 2017-4-17
	 * @version 1.0
	 * @param exchange http上下文，包含了http请求和对应的http响应
	 */
	@Override
	public void handle(HttpExchange exchange) {
		// TODO Auto-generated method stub
		HttpRequest request = new HttpRequest(exchange);
		String url = request.getRequstUri();
		HttpResponse res = ResponseFilter.create().getResponse(url, exchange);
		res.response();
	}

}
