package response;

import util.HttpResponse;

import com.sun.net.httpserver.HttpExchange;
/**
 * 请求有问题时回复响应
 * @author tiang
 * @date 2017-4-18
 * @version 1.0
 */
public class BadRequestResponse extends HttpResponse{

//	private Logger log = Logger.getLogger(this.getClass());
	String result;
	public BadRequestResponse(HttpExchange ex, String result) {
		super(ex);
		// TODO Auto-generated constructor stub
		this.result = "result=fail&"+result;
	}

	@Override
	public void response() {
		// TODO Auto-generated method stub
//		log.debug(result);
		write(200, result);
	}

}
