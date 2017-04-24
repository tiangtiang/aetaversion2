package response;

import receive.SessionState;
import request.DatapostRequest;

import com.sun.net.httpserver.HttpExchange;

import util.HttpResponse;

public class DatapostResponse extends HttpResponse {

	private DatapostRequest request;
	public DatapostResponse(HttpExchange ex) {
		super(ex);
		// TODO Auto-generated constructor stub
		request = new DatapostRequest(ex);
	}

	@Override
	public void response() {
		// TODO Auto-generated method stub
		SessionState state = request.judgeSession();
		if(state!=SessionState.Valid){
			new SessionErrorResponse(exchange, state).response();
		}else{
			boolean badRequest = request.validateParams("time", "probeId",
					"dataType", "account", "length");
			if(badRequest){
				new BadRequestResponse(exchange).response();
			}else{
				write("result=success");
				request.insertToDB();
			}
		}
	}

}
