package db;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import com.mchange.v2.c3p0.ComboPooledDataSource;

public class DBPool {
	
	//数据源Map
//	private static Map<String,Connection> dataSourseMap = new HashMap<String,Connection>();
	private static Map<String,ComboPooledDataSource> dataSourseMap = new HashMap<String,ComboPooledDataSource>();
	
	private static DBPool pool = new  DBPool();
	private DBPool(){
		init();
	}
	
	public static DBPool create(){
		return pool;
	}
	/**
	 * <p>描述：初始化所有的数据库连接源</p>
	 * @author 甘颖
	 * @date 2017-04-16
	 * @version 1.0
	 * @return 
	*/
	public void init() {
//		DbConnectName DBCN = new DbConnectName();
		DbConnectName.getAllDbNames();
		List<String> dbNames = DbConnectName.getDbNames();
		System.setProperty("com.mchange.v2.c3p0.cfg.xml","conf/c3p0-config.xml");
		for(int i = 0;i < dbNames.size();i++){
			//				dataSourseMap.put(dbNames.get(i), new ComboPooledDataSource(dbNames.get(i)).getConnection());
			dataSourseMap.put(dbNames.get(i), new ComboPooledDataSource(dbNames.get(i)));
		}
	}  
	
//	public static void initAllDataSourse(){
//		DbConnectName DBCN = new DbConnectName();
//		DBCN.getAllDbNames();
//		List<String> dbNames = DbConnectName.getDbNames();
//		for(int i = 0;i < dbNames.size();i++){
//			dataSourseMap.put(dbNames.get(i), new ComboPooledDataSource(dbNames.get(i)));
//		}
//	}
	
	/**
	 * <p>描述：根据dbConnectNameConfig.xml配置的数据源名，获得对应数据库连接</p>
	 * @param connName
	 * @return
	 * @author 甘颖
	 * @date 2016-4-16
	 * @version 1.0
	 * @modifier 修改人
	 * @modifyDate 修改时间
	 * @modifyContent 修改内容
	*/
	public Connection getConnection(String connName) {
				ComboPooledDataSource CPDS = dataSourseMap.get(connName);
//		System.out.println("startTime:"+new Date());
		Connection conn = null;
		try {
			conn = CPDS.getConnection();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
//		System.out.println("endTime:"+new Date());
//		return dataSourseMap.get(connName);
		return conn;
	}
	
	
	
	
	public static void main(String args[]) { 
		Connection conn = null;
		conn = DBPool.create().getConnection("aetaVersion_ds_1");
		PreparedStatement prestmt = null;
		try {
			prestmt = conn
			.prepareStatement("select t.* from channel t");
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		ResultSet rets = null;
		try {
			rets = prestmt.executeQuery();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		try {
			while(rets.next()) {
				System.out.println(	rets.getString(3));
			}
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    } 
		
}
