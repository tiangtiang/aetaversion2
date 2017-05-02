package response;

import java.util.HashMap;
import java.util.Map;

import receive.SessionState;
import request.WarnRequest;

import com.sun.net.httpserver.HttpExchange;

import util.HttpResponse;
/**
 * 响应警告上传请求
 * @author tiang
 * @date 2017-4-19
 * @version 1.0
 */
public class WarnResponse extends HttpResponse {

	private WarnRequest request;
	public WarnResponse(HttpExchange ex) {
		super(ex);
		// TODO Auto-generated constructor stub
		request =  new WarnRequest(ex);
	}

	@Override
	public void response() {
		// TODO Auto-generated method stub
		SessionState state = request.judgeSession();
		if(state == SessionState.Valid){		//session有效
			//保存键值对
			Map<String, String> params = new HashMap<String, String>();		
			boolean badRequest = request.isBadRequest(params, "terminalId",
					"time", "warningType", "warningMsg");
			if(badRequest)		//请求无效
				new BadRequestResponse(exchange, request.getResult()).response();
			else{				//请求有效，发送是否插入成功
				write(getResponseString(request.insertToDB(params)));
			}
		}else{
			new SessionErrorResponse(exchange, state).response();
		}
	}
	
	private String getResponseString(boolean isSuccess){
		//失败类型为找不到警告类型
		return "result="+(isSuccess?"success":"fail&failCode=FC_003&failReason=unknownWarning");
	}
}
