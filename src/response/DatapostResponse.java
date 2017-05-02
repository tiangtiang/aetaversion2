package response;

import receive.SessionState;
import request.DatapostRequest;
import util.HttpResponse;

import com.sun.net.httpserver.HttpExchange;

public class DatapostResponse extends HttpResponse {

	private final DatapostRequest request;
//	private Logger log = Logger.getLogger(this.getClass());

	public DatapostResponse(HttpExchange ex) {
		super(ex);
		// TODO Auto-generated constructor stub
		request = new DatapostRequest(ex);
	}

	@Override
	public void response() {
		// TODO Auto-generated method stub
		SessionState state = request.judgeSession();
		if (state != SessionState.Valid) {
			new SessionErrorResponse(exchange, state).response();
		} else {
			boolean badRequest = request.validateParams("time", "probeId",
					"dataType", "account", "length", "terminalId");
			if (badRequest) {
				new BadRequestResponse(exchange, request.getResult()).response();
			} else {
				String result = "result=success";
				write(result);
//				log.debug(result);
				request.insertToDB();
			}
		}
	}

}
