package receive;

import java.io.File;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Map;

import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;

import util.HttpResponse;

import com.sun.net.httpserver.HttpExchange;

/**
 * 从配置文件中读取每个请求对应的响应类
 * @author tiang
 * @date 2017-4-17
 * @version 1.0
 */
public class ResponseFilter {
	//存储请求地址和其对应的处理类
	private Map<String, String> redirect = new HashMap<String, String>();
	private final String PATH = "conf/redirect.xml";
	
	private static ResponseFilter filter = new ResponseFilter();
	
	private ResponseFilter(){
		load();
	}
	
	public static ResponseFilter create(){
		return filter;
	}
	
	public String getRedirect(){
		return redirect.toString();
	}
	
	public HttpResponse getResponse(String url, HttpExchange ex){
		String res = redirect.get(url);
		HttpResponse response = null;
		try {
			Class<?> resclass = Class.forName("response."+res);			//加载响应类的class文件
			//获取该响应类的带HttpExchange的构造函数
			Constructor<?> construct = resclass.getConstructor(HttpExchange.class);
			//创建一个新的实例
			response = (HttpResponse)construct.newInstance(ex);
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (SecurityException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (NoSuchMethodException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IllegalArgumentException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InstantiationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InvocationTargetException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} 
		return response;
	}
	/**
	 * 加载配置文件
	 * @author tiang
	 * @date 2017-4-17
	 * @version 1.0
	 */
	private void load(){
		File xml = new File(PATH);
		SAXReader reader = new SAXReader();   
		Document doc = null;
		try {
			doc = reader.read(xml);
		} catch (DocumentException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		for(Object obj : doc.getRootElement().elements("http")){		//遍历所有的http节点
			Element http = (Element)obj;
			Element request = http.element("request");					//寻找请求地址
			Element response = http.element("response");				//寻找响应类
			redirect.put(request.getText(), response.getText());
		}
	}
}
