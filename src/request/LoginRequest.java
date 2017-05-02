package request;

import java.sql.*;

import com.sun.net.httpserver.HttpExchange;

import db.DBPool;

import receive.ProbeTerminalMap;
import receive.Session;
import util.HttpRequest;
/**
 * 处理登录请求
 * @author tiang
 * @date 2017-4-18
 * @version 1.0
 */
public class LoginRequest extends HttpRequest {

	String terminalId;
	String password;
	public LoginRequest(HttpExchange ex) {
		super(ex);
		// TODO Auto-generated constructor stub
		initParaMap();
		terminalId = getParam("terminalId");
		password = getParam("password");
	}
	/**
	 * 判断是否登录成功
	 * @author tiang
	 * @date 2017-4-18
	 * @version 1.0
	 * @return 是否登录成功
	 */
	public boolean login(){
		if(terminalId == null ){
			badRequest = true;
			//缺少参数key
			result = "failCode=FC_001&failReason=request lack of parameter: "+"terminalId";
			log.debug(result);
			return false;
		}else if( password == null){
			badRequest = true;
			//缺少参数key
			result = "failCode=FC_001&failReason=request lack of parameter: "+"password";
			log.debug(result);
			return false;
		}else{
			String dbpwd = getPassword(terminalId);
			if(password.equals(dbpwd))
				return true;
			else
				return false;
		}
	}
	/**
	 * 根据终端编号查询密码
	 * @author tiang
	 * @date 2017-4-18
	 * @version 1.0
	 * @param terminalId 终端编号
	 * @return 密码
	 */
	private String getPassword(String terminalId){
		String result = null;
		
		Connection conn = DBPool.create().getConnection("aetaVersion_ds_1");
		
		String sql = "select password from terminal where terminalId = "+terminalId;
		PreparedStatement statement = null;
		try {
			statement = conn.prepareStatement(sql);
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		ResultSet ret = null;
		try {
			ret = statement.executeQuery();
			if(ret.next()){
				return ret.getString(1).trim();
			}else{
				return null;
			}
			
		} catch (SQLException e) { 
//			return null;
			// TODO Auto-generated catch block
//			e.printStackTrace();
			
		}finally{
			try {
				conn.close();
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
		return result;
		
	}
	/**
	 * 创建或更新session并返回sessionId， 在终端探头对应表中插入一条新的对应关系
	 * @author tiang
	 * @date 2017-4-18
	 * @version 1.0
	 * @return sessionId
	 */
	public String keepSession(){
		Session ss = new Session();
		ss.insert(terminalId);
		//向终端探头对应表中添加数据
		ProbeTerminalMap ptm = new ProbeTerminalMap();
		ptm.addProbe(terminalId);
		return ss.getSessionId(terminalId);
	}
}
