package response;

import java.util.HashMap;

import receive.SessionState;
import request.StateRequest;
import runback.StateThread;
import runback.ThreadPool;
import util.HttpResponse;

import com.sun.net.httpserver.HttpExchange;

/**
 * 状态上报请求响应
 * 
 * @author tiang
 * @date 2017-4-20
 * @version 1.0
 */
public class StateResponse extends HttpResponse {

	private final StateRequest request;

	public StateResponse(HttpExchange ex) {
		super(ex);
		// TODO Auto-generated constructor stub
		request = new StateRequest(ex);
	}

	@Override
	public void response() {
		// TODO Auto-generated method stub
		SessionState state = request.judgeSession();
		if (state != SessionState.Valid) { // session无效
			new SessionErrorResponse(exchange, state).response();
		} else {
			HashMap<String, String> params = new HashMap<String, String>();
			// 获取参数值，同时判断是否接收成功
			boolean badRequest = request.isBadRequest(params, "deviceType",
					"deviceId", "stateList", "terminalId");
			if (badRequest) { // 请求无效
				new BadRequestResponse(exchange, request.getResult()).response();
			} else {
				write(getResponseString(true));			//接收成功
				ThreadPool.execute(new StateThread(params)); // 插入数据库
			}
		}
	}

	private String getResponseString(boolean result) {
		return "result=" + (result ? "success" : "fail");
	}
}
