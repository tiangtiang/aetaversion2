package request;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Map;

import util.HttpRequest;

import com.sun.net.httpserver.HttpExchange;

import db.DBPool;

public class WarnRequest extends HttpRequest {

	public WarnRequest(HttpExchange ex) {
		super(ex);
		// TODO Auto-generated constructor stub
		initParaMap();
		validTerminalId();
	}

	public boolean insertToDB(Map<String, String> params) {
		// 告警表中每个终端只存一条最新的数据
		boolean result = false;
		/**
		 * 将键值对中的数据插入数据库表t_alarmInfo 传过来的参数名跟数据库字段名不一致，暂时先这样吧。 键值对中的键名 -- 数据库字段名
		 * terminalId -- terminalId time -- alarmTime warningType -- alarmType
		 * warningMsg -- alarmCode 最后返回插入是否成功
		 */
		Connection conn = DBPool.create().getConnection("aetaVersion_ds_1");
		try {
			PreparedStatement prestmt = conn
					.prepareStatement("select t.* from t_alarminfo t where t.terminalID = "
							+ Integer.parseInt(params.get("terminalId")));
			ResultSet ret = prestmt.executeQuery();
			if (ret.next())// 如果告警表中已有该终端的告警信息，则将此终端的告警信息更新为这条数据
			{
				prestmt.close();
				String sql = "update t_alarminfo set alarmTime = "
						+ Integer.parseInt(params.get("time"))
						+ ",alarmType ='" + params.get("warningType")
						+ "',alarmCode = '" + params.get("warningMsg")
						+ "' where terminalID = "
						+ Integer.parseInt(params.get("terminalId"));
				prestmt = conn.prepareStatement(sql);
				int effectedRows = prestmt.executeUpdate();
				if (effectedRows > 0)
					result = true;
				prestmt.close();
			} else {// 如果告警表中没有该终端的告警信息，则插入这条告警数据
				prestmt.close();
				prestmt = conn
						.prepareStatement("insert into t_alarminfo(alarmTime,terminalID,alarmType,alarmCode)"
								+ "VALUES(?,?,?,?)");
				prestmt.setInt(1, Integer.parseInt(params.get("time")));
				prestmt.setInt(2, Integer.parseInt(params.get("terminalId")));
				prestmt.setString(3, params.get("warningType"));
				prestmt.setString(4, params.get("warningMsg"));
				int effectedRows = prestmt.executeUpdate();
				if (effectedRows > 0)
					result = true;
				prestmt.close();
			}

		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
			try {
				conn.close();
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		return result;
	}
}
