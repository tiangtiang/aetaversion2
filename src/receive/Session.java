package receive;

import java.util.*;
/**
 * 服务器用以保存发送http请求的客户端的信息
 * @author tiang
 * @date 2017-4-17
 * @version 1.0
 */
public class Session {
	private static Map<String, SessionNode> sessions = 	//以终端Id和客户端信息作为session
		new HashMap<String, SessionNode>();
	/**
	 * 插入一条session数据
	 * @author tiang
	 * @date 2017-4-17
	 * @version 1.0
	 * @param terminalId 终端编号
	 */
	public synchronized void insert(String terminalId){
		sessions.put(terminalId, new SessionNode(terminalId));
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
		SessionNode node = sessions.get(ter);
		if(node == null)
			return null;
		else
			return node.sessionId;
	}
	/**
	 * 判断session是否超时
	 * @author tiang
	 * @date 2017-4-19
	 * @version 1.0
	 * @param ter 终端编号
	 * @return 是否超时
	 */
	public boolean isTimeOut(String ter){
		SessionNode node = sessions.get(ter);
		if(node == null)
			return true;
		else
			return sessions.get(ter).finalTime<System.currentTimeMillis()/1000;
	}
	/**
	 * 更新session的有效日期
	 * @author tiang
	 * @date 2017-4-19
	 * @version 1.0
	 * @param ter 终端编号
	 */
	public void updateTime(String ter){
		SessionNode node = sessions.get(ter);
		if(node == null)
			return ;
		else
			sessions.get(ter).finalTime = System.currentTimeMillis()/1000+1800;
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
			//当前时间戳加上三十分钟，之后过期,精确到秒
			finalTime = System.currentTimeMillis()/1000+1800;		
			sessionId = terminalId + "_" + finalTime;
		}
	}
}
