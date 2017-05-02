package receive;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import response.NotFoundResponse;
import util.HttpResponse;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

/**
 * 处理http请求，在这个类中根据不同的http请求调用不同的响应类进行响应
 * 
 * @author tiang
 * @date 2017-4-17
 * @version 1.0
 */
public class ReceiveHandler implements HttpHandler {

	private ExecutorService pool = Executors.newCachedThreadPool();

	/**
	 * 处理http请求
	 * 
	 * @author tiang
	 * @date 2017-4-17
	 * @version 1.0
	 * @param exchange
	 *            http上下文，包含了http请求和对应的http响应
	 */
	@Override
	public void handle(HttpExchange exchange) {
		// TODO Auto-generated method stub
		/*
		 * String url = exchange.getRequestURI().toString();
		 * System.out.println("请求" + url + "..."); HttpResponse res =
		 * ResponseFilter.create().getResponse(url, exchange); if (res == null)
		 * { res = new NotFoundResponse(exchange); System.out.println("找不到" +
		 * url + "..."); } res.response(); System.out.println("响应" + url +
		 * "...");
		 */
		pool.execute(new HandleThread(exchange));
	}

}

class HandleThread implements Runnable {

	private HttpExchange exchange;

	public HandleThread(HttpExchange ex) {
		exchange = ex;
	}

	@Override
	public void run() {
		// TODO Auto-generated method stub
		String url = exchange.getRequestURI().toString();
		System.out.println("请求" + url + "...");
		HttpResponse res = ResponseFilter.create().getResponse(url, exchange);
		if (res == null) {
			res = new NotFoundResponse(exchange);
			System.out.println("找不到" + url + "...");
		}
		res.response();
		System.out.println("响应" + url + "...");
	}

}