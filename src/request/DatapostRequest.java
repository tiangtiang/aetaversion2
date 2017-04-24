package request;

import java.io.*;
import java.util.HashMap;

import runback.DatapostThread;
import runback.ThreadPool;

import com.sun.net.httpserver.HttpExchange;

import util.HttpRequest;

public class DatapostRequest extends HttpRequest {

	private HashMap<String, String> params = new HashMap<String, String>();
	private byte[] data;
	public DatapostRequest(HttpExchange ex) {
		super(ex);
		// TODO Auto-generated constructor stub
	}
	
	private String readString(InputStream input){
		StringBuilder sb = new StringBuilder();
		try {
			char cursor = (char)input.read();
			while(cursor!='\n'){
				sb.append(cursor);
				cursor = (char)input.read();
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		System.out.println(sb.toString());
		return sb.toString();
	}
	
	public boolean getData(){
		InputStream input = exchange.getRequestBody();		//输入流
		String boundary = getHeadValue("boundary");			//获取分隔符	
		String line = readString(input);
		while(line!=null && !line.equals(boundary)){
			analysis(line, params);
			line = readString(input);
		}
		try {
			int account = Integer.parseInt(params.get("account"));
			int length = Integer.parseInt(params.get("length"));
			data = new byte[account*length];
			input.read(data);
			FileOutputStream out = new FileOutputStream("receive");
			out.write(data);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (Exception e) {
			// TODO: handle exception
			badRequest = true;
		}
		return badRequest;
	}
	
	public boolean validateParams(String ... strs){
		boolean flag = getData();
		if(flag)
			return true;
		for(String str : strs){
			String temp = params.get(str.toLowerCase());
			if(temp == null){
				badRequest = true;
				break;
			}
		}
		return badRequest;
	}
	
	public void insertToDB(){
		ThreadPool.execute(new DatapostThread(params, data));
	}
}
