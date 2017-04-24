package receive;

import db.DBPool;

public class TestMain {
	public static void main(String[] args) {
		DBPool.create();
		new ReceiveServer(); 
		System.out.println("*****************服务器已启动******************");
	}
}
