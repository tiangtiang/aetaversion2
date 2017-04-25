package request;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;

import runback.DatapostThread;
import runback.ThreadPool;
import util.HttpRequest;

import com.sun.net.httpserver.HttpExchange;

public class DatapostRequest extends HttpRequest {

	private final HashMap<String, String> params = new HashMap<String, String>();
	private byte[] data;

	public DatapostRequest(HttpExchange ex) {
		super(ex);
		// TODO Auto-generated constructor stub
	}

	/**
	 * 从字节流中读取一行数据并转换成String返回
	 * 
	 * @author tiang
	 * @date 2017-4-25
	 * @version 1.0
	 * @param input
	 *            输入字节流
	 * @return 字符串
	 */
	private String readString(InputStream input) {
		StringBuilder sb = new StringBuilder();
		try {
			char cursor = (char) input.read();
			while (cursor != '\n') {
				sb.append(cursor);
				cursor = (char) input.read();
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		System.out.println(sb.toString());
		return sb.toString();
	}

	/**
	 * 获取数据
	 * 
	 * @author tiang
	 * @date 2017-4-25
	 * @version 1.0
	 * @return 请求是否存在问题
	 */
	public boolean getData() {
		InputStream input = exchange.getRequestBody(); // 输入流
		String boundary = getHeadValue("boundary"); // 获取分隔符
		String line = readString(input);
		while (line != null && !line.equals(boundary)) {
			analysis(line, params);
			line = readString(input);
		}
		badRequest = !validateData();
		try {
			int account = Integer.parseInt(params.get("account"));
			int length = Integer.parseInt(params.get("length"));
			data = new byte[account * length];
			input.read(data);
			if (data[data.length - 1] == -1) // 最后一个字节不存在，说明字节数量没有达到预期
				badRequest = true;
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

	/**
	 * 验证数据类型和数据点数是否匹配
	 * 
	 * @author tiang
	 * @date 2017-4-25
	 * @version 1.0
	 * @return
	 */
	private boolean validateData() {
		try {
			int dataType = Integer.parseInt(params.get("datatype"));
			int account = Integer.parseInt(params.get("account"));
			if (dataType == 4) {
				if (account == 150000)
					return true;
			} else if (dataType == 1 || dataType == 2 || dataType == 3) {
				if (account == 30000)
					return true;
			} else if (dataType == 7)
				if (account == 1)
					return true;
			return false;
		} catch (Exception e) {
			// TODO: handle exception
			return false;
		}
	}

	/**
	 * 获取数据并且判断应有的参数是否存在
	 * 
	 * @author tiang
	 * @date 2017-4-25
	 * @version 1.0
	 * @param strs
	 *            应有的参数列表
	 * @return 请求是否存在问题
	 */
	public boolean validateParams(String... strs) {
		// return whenInBodys(strs);
		boolean head = whenInHead(strs);
		boolean data = readData();
		return head || data;
	}

	/**
	 * 当参数在报头中时
	 * 
	 * @author tiang
	 * @date 2017-4-25
	 * @version 1.0
	 * @param strs
	 * @return
	 */
	private boolean whenInHead(String... strs) {
		for (String key : strs) {
			String value = null;
			if (key.equals("terminalId"))
				value = getTerminalId();
			else
				value = getHeadValue(key);
			if (value == null)
				badRequest = true;
			else
				params.put(key, value);
		}
		return badRequest;
	}

	/**
	 * 读取数据
	 * 
	 * @author tiang
	 * @date 2017-4-25
	 * @version 1.0
	 * @return
	 */
	private boolean readData() {
		InputStream input = exchange.getRequestBody(); // 输入流
		try {
			int account = Integer.parseInt(params.get("account"));
			int length = Integer.parseInt(params.get("length"));
			data = new byte[account * length];
			input.read(data);
			if (data[data.length - 1] == -1) // 最后一个字节不存在，说明字节数量没有达到预期
				badRequest = true;
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

	/**
	 * 当参数在报文体中时
	 * 
	 * @author tiang
	 * @date 2017-4-25
	 * @version 1.0
	 * @param strs
	 * @return
	 */
	@SuppressWarnings("unused")
	@Deprecated
	private boolean whenInBodys(String... strs) {
		boolean flag = getData();
		if (flag)
			return true;
		for (String str : strs) {
			String temp = params.get(str.toLowerCase());
			if (temp == null) {
				badRequest = true;
				break;
			}
		}
		return badRequest;
	}

	/**
	 * 插入数据库
	 * 
	 * @author tiang
	 * @date 2017-4-25
	 * @version 1.0
	 */
	public void insertToDB() {
		ThreadPool.execute(new DatapostThread(params, data));
	}
}
