package receive;

import java.util.*;
/**
 * 服务器用以保存发送http请求的客户端的信息
 * @author tiang
 * @date 2017-4-17
 * @version 1.0
 */
public class Session {
	private static Map<String, SessionNode> sessons = 	//以终端Id和客户端信息作为session
		new HashMap<String, SessionNode>();
	/**
	 * 插入一条session数据
	 * @author tiang
	 * @date 2017-4-17
	 * @version 1.0
	 * @param terminalId 终端编号
	 */
	public synchronized void insert(String terminalId){
		sessons.put(terminalId, new SessionNode(terminalId));
	}
	/**
	 * 获取sessionId
	 * @author tiang
	 * @date 2017-4-17
	 * @version 1.0
	 * @param ter 终端编号
	 * @return sessionId
	 */
	public String getSessionId(String ter){
		return sessons.get(ter).sessionId;
	}
	
	/**
	 * 保存客户端的信息
	 * @author tiang
	 * @date 2017-4-17
	 * @version 1.0
	 */
	class SessionNode{
		public String terminalId;	//终端编号
		public String sessionId;	//以终端编号+时间戳作为sessionId
		public long finalTime;		//session过期日期
		public SessionNode(String ter){
			terminalId = ter;
			finalTime = System.currentTimeMillis();
			sessionId = terminalId + finalTime;
		}
	}
}
