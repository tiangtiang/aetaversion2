package request;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Map;

import com.sun.net.httpserver.HttpExchange;

import db.DBPool;

import util.HttpRequest;

public class ParametersRequest extends HttpRequest{

	public ParametersRequest(HttpExchange ex) {
		super(ex);
		initParaMap();
	}
	public String getParameters(Map<String, String> params){
		String result = null;
		/**
		 * 键值对包含的信息
		 * deviceType:设备类型
		 * deviceId:设备编号
		 * 根据不同的设备类型去不同的数据表中查询对应设备编号的参数信息，封装成{para1=value1&para2=value2}的形式返回
		 */
		String tableName = getTableNames(params.get("deviceType"));
		int deviceId = Integer.parseInt(params.get("deviceId"));
		Connection conn = DBPool.create().getConnection("aetaVersion_ds_1");
		if(tableName !=null)
		{
			try {
				PreparedStatement prestmt;
				prestmt = conn.prepareStatement("select t.* from "+ tableName +" t where t.terminalID = "+deviceId+" and t.NewFlag = 1");
				ResultSet ret = prestmt.executeQuery();
				if(ret.next()){//如果有新的参数则返回
					int paramsCount = ret.getInt("Count");
					for(int i = 1;i <=paramsCount;i++){
						result = "&para"+i+"="+ret.getInt("P"+String.valueOf(i));
					}
					result = result.substring(1);
					result = "{"+result +"}";
					//将此条参数更新为已取过的参数
					prestmt = conn.prepareStatement("update "+tableName+" t set t.NewFlag=0 where t.TerminalID="+deviceId);
					prestmt.executeUpdate();
					prestmt.close();
					conn.close();
				}
				
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
//		return "{para1=value1&para2=value2}";
		return result;
	}
	
	public int countParams(String params){
		String[] para = params.split("&");
		return para.length;
	}
	
	public String getTableNames(String deviceType){
		String tableName = null;
		if(deviceType.trim().equalsIgnoreCase("Dev01"))//设备类型为终端，则返回终端参数配置表表名
			tableName = "config_terminal";
		else if(deviceType.trim().equalsIgnoreCase("Dev03"))//设备类型为探头，则返回探头参数配置表表名
			tableName = "config_probe";
		else{	
		}
		return tableName;
	}
	
}