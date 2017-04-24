package request;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import com.sun.net.httpserver.HttpExchange;

import db.DBPool;

import util.HttpRequest;

public class CommandRequest extends HttpRequest {

	public CommandRequest(HttpExchange ex) {
		super(ex);
		initParaMap();
	}
	public String searchCommand(){
		String commandString = null;
		/**
		 * 封装成如下形式，参数列表为：
		 * time: 命令指定时间
		 * deviceType: 设备类型
		 * deviceId: 设备编号
		 * cmdLength: 命令长度
		 * command: 命令内容
		 */
		int termianlID = Integer.parseInt(this.getTerminalId().trim());
		Connection conn = DBPool.create().getConnection("aetaVersion_ds_1");
		try {
			PreparedStatement prestmt = conn.prepareStatement("select t.* from command t where t.terminalID = "+termianlID+" and t.NewFlag = 1");
			ResultSet ret = prestmt.executeQuery();
			if(ret.next()){//有新命令要执行则返回命令
				commandString = "time="+System.currentTimeMillis()/1000+"&deviceType="+ret.getInt("DeviceNum")+"&deviceId="+ret.getInt("DeviceID")+"&cmdLength="
									+ret.getString("Command").trim().length()+"&command="+ret.getString("Command");
				//将此条命令更新为已执行过的命令
				prestmt = conn.prepareStatement("update command t set t.NewFlag=0 where t.TerminalID="+termianlID);
				prestmt.executeUpdate();
				prestmt.close();
				conn.close();
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
//		return "time=1492677158&deviceType=dev01&deviceId=15&cmdLength=20&"+
//		"command=ls /root -l";
		return commandString;
	}

}
