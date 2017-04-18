package db;

import java.io.FileNotFoundException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.dom4j.DocumentException;

import com.mchange.v2.c3p0.ComboPooledDataSource;
import com.themis.common.db.ThemisConnectionFactory;

import db.DbConnectName;

public class DBPool {
	
	//数据源Map
	private static Map<String,ComboPooledDataSource> dataSourseMap = new HashMap<String,ComboPooledDataSource>();
	
	/**
	 * <p>描述：初始化所有的数据库连接源</p>
	 * @author 甘颖
	 * @date 2017-04-16
	 * @version 1.0
	 * @return 
	*/
	static {
		DbConnectName DBCN = new DbConnectName();
		DBCN.getAllDbNames();
		List<String> dbNames = DbConnectName.getDbNames();
		
		for(int i = 0;i < dbNames.size();i++){
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
	public static Connection getConnection(String connName) {
		try {
		ComboPooledDataSource  CPDS = dataSourseMap.get(connName);
		return CPDS.getConnection();
			
		} catch (SQLException e) {
			e.printStackTrace();
			return null;
		}
	}
	/**
	 * <p>描述：保持长连接，如果连接断掉重新连接</p>
	 * @param conn
	 * @param connName
	 * @author 何斯译
	 * @date 2014-7-28
	 * @version 1.0
	 * @modifier 修改人
	 * @modifyDate 修改时间
	 * @modifyContent 修改内容
	*/
	private static Connection getRetryConnection(Connection conn, String connName) {
		if(null == connName || connName.equals("")) {
			return null;
		}
		try {
//			if ((null == conn) || (conn.isClosed())) {
				conn = ThemisConnectionFactory.getConnection(connName);
//			}
			return conn;
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (SQLException e) {
			e.printStackTrace();
		} catch (DocumentException e) {
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}
	public static void main(String args[]) { 
		Connection conn = null;
		conn = DBPool.getConnection("aetaVersion_ds_1");
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
