package response;

import java.util.*;

import receive.SessionState;
import request.LogfileRequest;
import runback.*;

import com.sun.net.httpserver.HttpExchange;

import util.HttpResponse;
/**
 * 日志上报请求响应
 * @author tiang
 * @date 2017-4-20
 * @version 1.0
 */
public class LogfileResponse extends HttpResponse{

	private LogfileRequest request;
	public LogfileResponse(HttpExchange ex) {
		super(ex);
		// TODO Auto-generated constructor stub
		request = new LogfileRequest(ex);
	}

	@Override
	public void response() {
		// TODO Auto-generated method stub
		SessionState state = request.judgeSession();
		if(state!=SessionState.Valid){				//session无效
			new SessionErrorResponse(exchange, state).response();
		}else{
			Map<String, String> params = new HashMap<String, String>();
			boolean badRequest = request.isBadRequest(params, "time",
					"deviceType", "deviceId", "logType", "logLength",
					"logContent", "terminalId");
			
			if(badRequest){			//请求无效
				new BadRequestResponse(exchange).response();
			}else{
				write("result=success");
				ThreadPool.execute(new LogfileThread(params));
			}
		}
	}

}
