package receive;

import org.apache.log4j.PropertyConfigurator;

import db.DBPool;

public class TestMain {
	public static void main(String[] args) {
		PropertyConfigurator.configure("conf/log4j.properties");
		DBPool.create();
		new ReceiveServer(); 
		System.out.println("*****************服务器已启动******************");
	}
}
