package runback;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Map;

import db.DBPool;

public class LogfileThread implements Runnable {

	private final Map<String, String> params;

	public LogfileThread(Map<String, String> map) {
		params = map;
	}

	@Override
	public void run() {
		insertToDB();
	}

	private void insertToDB() {
		/**
		 * 将params键值对中的数据插入数据库中
		 */
		String deviceType = params.get("deviceType").trim();
		// 设备类型为终端
		if (deviceType.equalsIgnoreCase("Dev01")) {
			terminalInsertIntoDB();
		} else if (deviceType.equalsIgnoreCase("Dev03")) {// 设备类型为探头
			ProbeInsertIntoDB();
		}
		System.out.println(params);
	}

	// 终端日志数据入库
	public void terminalInsertIntoDB() {
		String tableName = null;
		int deviceId = Integer.parseInt(params.get("deviceId"));
		Connection conn = DBPool.create().getConnection("aetaVersion_ds_1");
		tableName = "terminallog";
		// 获取日志产生时终端的系统时间
		int Time = Integer.parseInt(params.get("time"));
		// logContent-日志内容
		String logContent = params.get("logContent");
		// logType-日志类型
		int logType = Integer.parseInt(params.get("logType"));
		// logLength-日志长度
		int logLength = Integer.parseInt(params.get("logLength"));
		try {
			PreparedStatement prestmt;
			// 插入终端状态数据到terminallog表中
			prestmt = conn.prepareStatement("INSERT into " + tableName
					+ "(Time,TerminalID,LogType,Length,Event)" + " values("
					+ Time + "," + deviceId + "," + logType + "," + logLength
					+ ",'" + logContent + "')");
			prestmt.executeUpdate();
			prestmt.close();
			// conn.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	// 探头日志数据入库
	public void ProbeInsertIntoDB() {
		String tableName = null;
		int terminalId = Integer.parseInt(params.get("terminalId"));
		int deviceId = Integer.parseInt(params.get("deviceId"));
		Connection conn = DBPool.create().getConnection("aetaVersion_ds_1");
		tableName = "probelog";
		// 获取日志产生时终端的系统时间
		int Time = Integer.parseInt(params.get("time"));
		// logContent-日志内容
		String logContent = params.get("logContent");
		// logType-日志类型
		int logType = Integer.parseInt(params.get("logType"));
		// logLength-日志长度
		int logLength = Integer.parseInt(params.get("logLength"));
		try {
			PreparedStatement prestmt;
			String sql = "INSERT into " + tableName
					+ "(Time,ProbeID,TerminalID,LogType,Length,Event)"
					+ "values(" + Time + "," + deviceId + "," + terminalId
					+ "," + logType + "," + logLength + ",'" + logContent
					+ "')";
			// 插入终端状态数据到terminallog表中
			prestmt = conn.prepareStatement(sql);
			prestmt.executeUpdate();
			prestmt.close();
			// conn.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

}
