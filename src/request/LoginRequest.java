package request;

import java.sql.*;

import com.sun.net.httpserver.HttpExchange;

import db.DBPool;

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
		if(terminalId == null || password == null){
			badRequest = true;
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
			
		}
		return result;
		
	}
	/**
	 * 创建或更新session并返回sessionId
	 * @author tiang
	 * @date 2017-4-18
	 * @version 1.0
	 * @return sessionId
	 */
	public String keepSession(){
		Session ss = new Session();
		ss.insert(terminalId);
		return ss.getSessionId(terminalId);
	}
}
