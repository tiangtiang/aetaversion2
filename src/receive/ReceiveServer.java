package receive;

import java.io.IOException;
import java.net.InetSocketAddress;

import com.sun.net.httpserver.HttpServer;
/**
 * 接收http请求的服务器
 * @author tiang
 * @date 2017-4-17
 * @version 1.0
 */
public class ReceiveServer {
	private HttpServer server;

	public ReceiveServer(){
		try {
			//接收http请求的服务器
			server = HttpServer.create(new InetSocketAddress(8068), 0);		
			//处理http请求
			server.createContext("/", new ReceiveHandler());				
			server.start();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
