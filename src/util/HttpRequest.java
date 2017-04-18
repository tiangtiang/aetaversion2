package util;

import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpExchange;

import java.io.*;
import java.util.*;
/**
 * http请求类，解析http请求
 * @author tiang
 * @date 2017-4-17
 * @version 1.0
 */
public class HttpRequest {
	private HttpExchange exchange;		
	//http请求头信息
	private Map<String, List<String>> headMap = new HashMap<String, List<String>>();	
	//http请求参数信息
	private Map<String, String> paramMap = new HashMap<String, String>();			
	
	protected boolean badRequest = false;
	public HttpRequest(HttpExchange ex){
		exchange = ex;
		initHeaderMap();
		initParaMap();
	}
	/**
	 * 获取http请求头信息，按照键值对形式返回
	 * @author tiang
	 * @date 2017-4-17
	 * @version 1.0
	 * @return 头键值对
	 */
	public Map<String, List<String>> getHeader(){
		return headMap;
	}
	/**
	 * 获取http请求的参数信息，按照键值对的形式返回
	 * @author tiang
	 * @date 2017-4-17
	 * @version 1.0
	 * @return 参数键值对
	 */
	public Map<String, String> getParameters(){
		return paramMap;
	}
	/**
	 * 获取http请求方法，POST或GET
	 * @author tiang
	 * @date 2017-4-17
	 * @version 1.0
	 * @return 请求方法
	 */
	public String getMethod(){
		return exchange.getRequestMethod().trim().toUpperCase();
	}
	/**
	 * 获取请求的服务器地址，可以用于判断请求类型
	 * @author tiang
	 * @date 2017-4-17
	 * @version 1.0
	 * @return 请求地址
	 */
	public String getRequstUri(){
		return exchange.getRequestURI().toString();
	}
	/**
	 * 从请求中获取头信息，添加到键值对里
	 * @author tiang
	 * @date 2017-4-17
	 * @version 1.0
	 */
	private void initHeaderMap(){
		Headers head = exchange.getRequestHeaders();
		for(String s : head.keySet()){
			headMap.put(s, head.get(s));
		}
	}
	/**
	 * 从请求头的键值对中获取信息
	 * @author tiang
	 * @date 2017-4-17
	 * @version 1.0
	 * @param headKey 键
	 * @return 值
	 */
	public String getHeadValue(String headKey){
		List<String> values = headMap.get(headKey);
		if(values == null)
			return null;
		StringBuilder sb = new StringBuilder();
		for(String str : values){
			sb.append(str+"\n");
		}
		return sb.toString();
	}
	/**
	 * 获取上传的参数信息
	 * @author tiang
	 * @date 2017-4-17
	 * @version 1.0
	 * @param key 参数键
	 * @return 参数值
	 */
	public String getParam(String key){
		return paramMap.get(key);
	}
	/**
	 * 从请求中获取传输的参数信息
	 * @author tiang
	 * @date 2017-4-17
	 * @version 1.0
	 */
	private void initParaMap(){
		String method = getMethod();
		if(method.equals("GET")){
			String params = exchange.getRequestURI().getQuery();
			if(params == null)
				return;
			badRequest = !analysis(params, paramMap);
		}else if(method.equals("POST")){
			getPostParam();
		}
	}
	
	private boolean analysis(String str, Map<String, String> param){
		String[] paraPair = str.split("&");
		for(String pair : paraPair){
			String[] entry = pair.split("=");
			if(entry.length != 2)
				return false;
			else
				param.put(entry[0], entry[1]);
		}
		return true;
	}
	
	private void getPostParam(){
		String boundry = getHeadValue("boundry");	//获取分割符
		if(boundry!=null){			//存在分隔符，同时发送参数和文件
			
		}else{						//不存在分隔符，只发送了参数
			String content = getStringRequestBody();
			badRequest = !analysis(content, paramMap);
		}
	}
	
	public boolean isBadRequest(){
		return badRequest;
	}
	/**
	 * 将请求体以字符串的形式读取
	 * @author tiang
	 * @date 2017-4-17
	 * @version 1.0
	 * @return 请求体字符串
	 */
	public String getStringRequestBody(){
		InputStream in = exchange.getRequestBody();
		BufferedReader reader = new BufferedReader(new InputStreamReader(in));
		StringBuilder sb = new StringBuilder();
		String line;
		try {
			while((line = reader.readLine())!= null){
				sb.append(line);
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return sb.toString();
	}
	/**
	 * 将请求体保存到文件中
	 * @author tiang
	 * @date 2017-4-17
	 * @version 1.0
	 */
	public void saveFile(){
		InputStream in = exchange.getRequestBody();
		BufferedReader reader = new BufferedReader(new InputStreamReader(in));
		try {
			FileWriter writer= new FileWriter(new File("receive.txt"), false);
			String line;
			while((line = reader.readLine())!=null)
				writer.write(line+"\n");
			writer.flush();
			writer.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
}
