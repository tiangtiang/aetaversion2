package response;

import receive.SessionState;
import request.CommandRequest;
import util.HttpResponse;

import com.sun.net.httpserver.HttpExchange;
/**
 * 命令查询请求响应
 * @author tiang
 * @date 2017-4-20
 * @version 1.0
 */
public class CommandResponse extends HttpResponse {

	private CommandRequest request;
	public CommandResponse(HttpExchange ex) {
		super(ex);
		// TODO Auto-generated constructor stub
		request = new CommandRequest(ex);
	}

	@Override
	public void response() {
		// TODO Auto-generated method stub
		SessionState state = request.judgeSession();
		if(state!=SessionState.Valid){			//session无效
			new SessionErrorResponse(exchange, state).response();
		}else{
			String cmd = request.searchCommand();		//获取命令
			if(cmd == null)			//如果没有新命令
				write("result=noCommand");
			else					//有新命令
				write(cmd);
		}
	}

}
