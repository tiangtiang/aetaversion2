package response;

import receive.SessionState;
import request.TimeSynRequest;

import com.sun.net.httpserver.HttpExchange;

import util.HttpResponse;
/**
 * 时间同步请求响应
 * @author tiang
 * @date 2017-4-19
 * @version 1.0
 */
public class TimeSynResponse extends HttpResponse{

	private TimeSynRequest request;
	public TimeSynResponse(HttpExchange ex) {
		super(ex);
		// TODO Auto-generated constructor stub
		request = new TimeSynRequest(ex);
	}

	@Override
	public void response() {
		// TODO Auto-generated method stub
		SessionState state = request.judgeSession();
		if(state == SessionState.Valid){
			boolean badRequest = request.isBadRequest();
			if(badRequest){
				new BadRequestResponse(exchange, request.getResult()).response();
			}else{
				write(getresponseString());
			}
		}else{
			new SessionErrorResponse(exchange, state).response();
		}
	}
	
	private String getresponseString(){
		return "result=success&time="+System.currentTimeMillis()/1000;		//成功，返回时间戳
	}

}
