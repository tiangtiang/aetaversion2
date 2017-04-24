package response;

import com.sun.net.httpserver.HttpExchange;

import util.HttpResponse;
/**
 * 找不到请求地址时响应404
 * @author tiang
 * @date 2017-4-18
 * @version 1.0
 */
public class NotFoundResponse extends HttpResponse {

	public NotFoundResponse(HttpExchange ex) {
		super(ex);
		// TODO Auto-generated constructor stub
	}

	@Override
	public void response() {
		// TODO Auto-generated method stub
		write(404, "NotFound");
	}
}
