package response;

import receive.SessionState;
import util.HttpResponse;

import com.sun.net.httpserver.HttpExchange;
/**
 * 请求的session有问题时响应
 * @author tiang
 * @date 2017-4-19
 * @version 1.0
 */
public class SessionErrorResponse extends HttpResponse {

	private SessionState state;
	public SessionErrorResponse(HttpExchange ex, SessionState state) {
		super(ex);
		// TODO Auto-generated constructor stub
		this.state = state;
	}

	@Override
	public void response() {
		// TODO Auto-generated method stub
		write(getResponseString());
	}
	
	private String getResponseString(){
		return "sessionError="+state.toString();
	}

}
