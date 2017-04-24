package runback;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import db.DBPool;

public class StateThread implements Runnable{

	private Map<String, String> params;
	public StateThread(Map<String, String> map){
		params = map;
	}
	@Override
	public void run() {
		insertToDB();
	}

	/**
	 * 将状态列表转换成map
	 * @author tiang
	 * @date 2017-4-19
	 * @version 1.0
	 * @param str 输入字符串
	 * @return map
	 */
	private Map<String, String> changeStringToMap(String str){
		HashMap<String, String> map = new HashMap<String, String>();
		str = str.replace("{|}", "");
		String[] pairs = str.split("&");
		for(String pair : pairs){
			String[] kvs = pair.split("=");
			if(kvs.length!=2)
				continue;
			else{
				map.put(kvs[0].toLowerCase(), kvs[1]);
			}
		}
		return map;
	}
	//状态数据入库
	public boolean insertToDB(){
		boolean result = false;
		//从状态列表中将每个状态以键值对的形式分离出来
		Map<String, String> kvs = changeStringToMap(params.get("stateList"));
		/**
		 * 将map中的deviceType,deviceId以及kvs中的状态插入数据库
		 */
		String deviceType = params.get("deviceType").trim();
		//设备类型为终端
		if(deviceType.equalsIgnoreCase("Dev01")){
			result = terminalInsertIntoDB(kvs);
		}else if(deviceType.equalsIgnoreCase("Dev03")){//设备类型为探头
			result = ProbeInsertIntoDB(kvs);
		}
		System.out.println(kvs);
	
		return result;
	}
	
	//终端状态数据入库
	public boolean terminalInsertIntoDB(Map<String, String> kvs){
		boolean result = false;
		String tableName_current = null,tableName = null;
		int deviceId = Integer.parseInt(params.get("deviceId"));
		Connection conn = DBPool.create().getConnection("aetaVersion_ds_1");
		tableName = "terminalStatus_2017";
		tableName_current = "terminalCurrentStatus";
			//获取当前应用服务器系统时间
			Date CurrentSysTime = new Date();
			SimpleDateFormat format = new SimpleDateFormat( "yyyy-MM-dd HH:mm:ss");
			String CurrentSysTimeString = format.format(CurrentSysTime);
			//获取终端的系统时间
			long TerminalSysTime = Long.parseLong(kvs.get("CurrentTime"))*1000;
			Date time_tmp = new Date(TerminalSysTime);
			String TerminalTime = format.format(time_tmp);
			//获取终端上次重启时的重启时间
			long TerminaRebootLastTime = Long.parseLong(kvs.get("RunUpTime"))*1000;
			time_tmp = new Date(TerminaRebootLastTime);
			String TerminaRebootLastTimeString = format.format(time_tmp);
			//终端运行的软件版本信息
			String TerStartTime = kvs.get("Version");
			//SDStatus-SD卡状态
			int SDStatus = Integer.parseInt(kvs.get("SDState"));
			//FreeSpaceRate-SD卡剩余空间
			float FreeSpaceRate = Float.parseFloat(kvs.get("FreeSpaceRate"));
			//Network-网络状况
			int Network = Integer.parseInt(kvs.get("Network"));
			//DataRate-网口数据率
			int DataRate = Integer.parseInt(kvs.get("DataRate"));
			//BadPackRate-坏包率
			float BadPackRate = Float.parseFloat(kvs.get("BadPackRate"));
			try {
			PreparedStatement prestmt;
			//开启事务
			conn.setAutoCommit(false);
			//插入终端状态数据到terminalStatus_2017表中
			prestmt = conn.prepareStatement("INSERT into "+tableName+"(TerminalID,ServerSysTime,TerminalSysTime,TerStartTime,TerSoftVersion,SDStatus,FreeSpace,NetworkStatus,DataRate,BadPack)"
					+"values("+deviceId+",'"+CurrentSysTimeString+"','"+TerminalTime+"','"+TerminaRebootLastTimeString+"','"+TerStartTime+"',"+SDStatus+","+FreeSpaceRate+","+Network
					+","+DataRate+","+BadPackRate+")");
			int flag1 = prestmt.executeUpdate();
			if(flag1 >0){
				//查看当前终端状态表是否存在此deviceId的状态数据
				prestmt = conn.prepareStatement("select t.* from "+tableName_current+" t where t.TerminalID = " +deviceId);
				ResultSet ret = prestmt.executeQuery();
				if(ret.next()){//如果存在，则进行update操作
					prestmt = conn.prepareStatement("update "+tableName_current+"t set ServerSysTime='"+CurrentSysTimeString+"',TerminalSysTime='"+TerminalTime
							+"',TerStartTime='"+TerminaRebootLastTimeString+"',TerSoftVersion='"+TerStartTime+"',SDStatus="+SDStatus+",FreeSpace="+FreeSpaceRate
							+",NetworkStatus="+Network+",DataRate ="+DataRate+",BadPack="+BadPackRate+" where t.TerminalID="+deviceId);
					int flag2 = prestmt.executeUpdate();
					if(flag2 >0){//操作成功，提交事务
						conn.commit();
						result = true;
					}else{//操作失败，回滚事务
						result = false;
						conn.rollback();
					}
				}else{//如果不存在则进行insert操作
					prestmt = conn.prepareStatement("INSERT into "+tableName_current+"(TerminalID,ServerSysTime,TerminalSysTime,TerStartTime,TerSoftVersion,SDStatus,FreeSpace,NetworkStatus,DataRate,BadPack)"
							+"values("+deviceId+",'"+CurrentSysTimeString+"','"+TerminalTime+"','"+TerminaRebootLastTimeString+"','"+TerStartTime+"',"+SDStatus+","+FreeSpaceRate+","+Network
							+","+DataRate+","+BadPackRate+")");
					int flag2 = prestmt.executeUpdate();
					if(flag2 >0){//操作成功，提交事务
						result = true;
						conn.commit();
					}else{//操作失败，回滚事务
						result = false;
						conn.rollback();
					}
				}
			}
			prestmt.close();
			conn.setAutoCommit(true);
			conn.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return result;
	}
	
	//探头状态数据入库
	public boolean ProbeInsertIntoDB(Map<String, String> kvs){
		boolean result = false;
		String tableName_current = null,tableName = null;
		int deviceId = Integer.parseInt(params.get("deviceId"));
		Connection conn = DBPool.create().getConnection("aetaVersion_ds_1");
		tableName = "probeStatus_2017";
		tableName_current = "probeCurrentStatus";
			//获取当前应用服务器系统时间
			Date CurrentSysTime = new Date();
			SimpleDateFormat format = new SimpleDateFormat( "yyyy-MM-dd HH:mm:ss");
			String CurrentSysTimeString = format.format(CurrentSysTime);
			//获取终端的系统时间
			long TerminalSysTime = Long.parseLong(kvs.get("TimeStamp"))*1000;
			Date time_tmp = new Date(TerminalSysTime);
			String TerminalTime = format.format(time_tmp);
			//终端运行的软件版本信息
			String Version = kvs.get("Version");
			//EN-保留+使能端
			int EN = Integer.parseInt(kvs.get("EN"));
			//SynInterval-同步间隔
			int SynInterval = Integer.parseInt(kvs.get("SynInterval"));
			//ProbeID-探头号
			int ProbeID = Integer.parseInt(kvs.get("ProbeID"));
			//ProbeType-探头类型
			int ProbeType = Integer.parseInt(kvs.get("ProbeType"));
			//zerdrf_comp-零漂补偿系数
			int zerdrf_comp = Integer.parseInt(kvs.get("zerdrf_comp"));
			//multiple-放大倍数
			int multiple = Integer.parseInt(kvs.get("multiple"));
			try {
			PreparedStatement prestmt;
			//开启事务
			conn.setAutoCommit(false);
			//插入终端状态数据到terminalStatus_2017表中
			prestmt = conn.prepareStatement("INSERT into "+tableName+"(ProbeID,ProbeType,TerminalID,ServerSysTime,TerminalSysTime,ProSoftVersion,DataEnable,SyncInterVal,ZeroCorrectValue,DataAmpTimes)"
					+"values("+ProbeID+","+ProbeType+","+deviceId+",'"+CurrentSysTimeString+"','"+TerminalTime+"',"+Version+","+EN+","+SynInterval
					+","+zerdrf_comp+","+multiple+")");
			int flag1 = prestmt.executeUpdate();
			if(flag1 >0 ){
				//查看当前终端状态表是否存在此ProbeID的状态数据
				prestmt = conn.prepareStatement("select t.* from "+tableName_current+" t where t.ProbeID = " +ProbeID);
				ResultSet ret = prestmt.executeQuery();
				if(ret.next()){//如果存在，则进行update操作
					prestmt = conn.prepareStatement("update "+tableName_current+"t set ProbeType="+ProbeType+",TerminalID="+deviceId
							+",ServerSysTime='"+CurrentSysTimeString+"',TerminalSysTime='"+TerminalTime+"',ProSoftVersion='"+Version+"',DataEnable="+EN
							+",SyncInterVal="+SynInterval+",ZeroCorrectValue ="+zerdrf_comp+",DataAmpTimes="+multiple+" where t.ProbeID="+ProbeID);
					int flag2 = prestmt.executeUpdate();
					if(flag2 >0){//操作成功，提交事务
						conn.commit();
						result = true;
					}else{//操作失败，回滚事务
						result = false;
						conn.rollback();
					}
				}else{//如果不存在则进行insert操作
					prestmt = conn.prepareStatement("INSERT into "+tableName+"(ProbeID,ProbeType,TerminalID,ServerSysTime,TerminalSysTime,ProSoftVersion,DataEnable,SyncInterVal,ZeroCorrectValue,DataAmpTimes)"
							+"values("+ProbeID+","+ProbeType+","+deviceId+",'"+CurrentSysTimeString+"','"+TerminalTime+"',"+Version+","+EN+","+SynInterval
							+","+zerdrf_comp+","+multiple+")");
					int flag2 = prestmt.executeUpdate();
					if(flag2 >0){//操作成功，提交事务
						result = true;
						conn.commit();
					}else{//操作失败，回滚事务
						result = false;
						conn.rollback();
					}
				}
			}else{//操作失败，回滚事务
				result = false;
				conn.rollback();
			}
			prestmt.close();
			conn.setAutoCommit(true);
			conn.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return result;
	}
	
	
}
