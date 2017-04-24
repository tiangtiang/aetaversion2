package response;

import com.sun.net.httpserver.HttpExchange;

import util.HttpResponse;
/**
 * 请求有问题时回复响应
 * @author tiang
 * @date 2017-4-18
 * @version 1.0
 */
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
