package response;

import java.util.HashMap;
import java.util.Map;

import receive.SessionState;
import request.ParametersRequest;
import util.HttpResponse;
import util.ParamToStr;

import com.sun.net.httpserver.HttpExchange;

/**
 * 参数配置请求响应
 * 
 * @author tiang
 * @date 2017-4-20
 * @version 1.0
 */
public class ParametersResponse extends HttpResponse {

	private final ParametersRequest request;

	public ParametersResponse(HttpExchange ex) {
		super(ex);
		// TODO Auto-generated constructor stub
		request = new ParametersRequest(ex);
	}

	@Override
	public void response() {
		// TODO Auto-generated method stub
		SessionState state = request.judgeSession();
		if (state != SessionState.Valid) { // session无效
			new SessionErrorResponse(exchange, state).response();
		} else {
			HashMap<String, String> params = new HashMap<String, String>();
			boolean badRequest = request.isBadRequest(params, "deviceType",
					"deviceId");
			if (badRequest) {
				new BadRequestResponse(exchange).response();
			} else {
				// 参数字符串
				String paraStr = request.getParameters(params);
				if (paraStr == null)
					write("result=null");
				int paraNum = request.countParams(paraStr);
				write(getResponseString(params, paraStr, paraNum));
			}
		}
	}

	public String getResponseString(Map<String, String> params, String paraStr,
			int paraNum) {
		params.put("paramNum", paraNum + "");
		params.put("params", paraStr);
		return ParamToStr.changeToString(params);
	}

}
