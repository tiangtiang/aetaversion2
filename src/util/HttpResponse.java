package util;

import java.io.*;

import com.sun.net.httpserver.HttpExchange;

/**
 * http响应类
 * @author tiang
 * @date 2017-4-17
 * @version 1.0
 */
public abstract class HttpResponse {
	protected HttpExchange exchange;
	
	public HttpResponse(HttpExchange ex){
		exchange = ex;
	}
	/**
	 * http响应
	 * @author tiang
	 * @date 2017-4-17
	 * @version 1.0
	 */
	public abstract void response();
	
	/**
	 * 向http响应中写入信息，默认http状态码为200
	 * @author tiang
	 * @date 2017-4-17
	 * @version 1.0
	 * @param text 响应信息
	 */
	protected void write(String text){
		OutputStream out = exchange.getResponseBody();
		BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(out));
		try {
			exchange.sendResponseHeaders(200, text.length());
			writer.write(text);
			writer.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	/**
	 * 向http响应中写入信息，可设置状态码
	 * @author tiang
	 * @date 2017-4-17
	 * @version 1.0
	 * @param state http状态码
	 * @param text 响应信息
	 */
	protected void write(int state, String text){
		OutputStream out = exchange.getResponseBody();
		BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(out));
		try {
			exchange.sendResponseHeaders(state, text.length());
			writer.write(text);
			writer.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
