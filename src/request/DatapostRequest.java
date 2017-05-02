package request;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;

import receive.ProbeTerminalMap;
import runback.DatapostThread;
import runback.ThreadPool;
import util.HttpRequest;
import util.ParamToStr;

import com.sun.net.httpserver.HttpExchange;

public class DatapostRequest extends HttpRequest {

	private final HashMap<String, String> params = new HashMap<String, String>();
	private byte[] data;

	// private Logger log = Logger.getLogger(this.getClass());
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
		// System.out.println(sb.toString());
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
	@Deprecated
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
			out.close();
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
			// 数据类型与其规定的点数不符
			result = "failCode=FC_004&failReason=the account(" + account
					+ ") of data mismatch the dataType(" + dataType + ")";
			log.error(result);
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
		boolean validate = validateData();
		boolean data = readData();
		boolean term = validateTerminalId();
		boolean prob = validateProbeId();
		return head || data || validate || term || prob;
	}

	public boolean validateTerminalId() {
		String sessionTerminalId = getTerminalId(); // session保存的终端编号
		String parameterTerminalId = getHeadValue("terminalId"); // 报文头中的终端编号
		if (!sessionTerminalId.equals(parameterTerminalId)) {
			badRequest = true; // 请求有错误
			result = ParamToStr
					.formResult("FC_006", "terminalId was not match");
			log.error(result);
		}
		return badRequest;
	}

	public boolean validateProbeId() {
		String probeId = getHeadValue("probeId"); // 报文体中的探头编号
		// 获取map中对应的终端编号
		String terminalId = new ProbeTerminalMap().getTerminalId(probeId);
		String sessionterminalId = getTerminalId(); // 获取session中的终端编号
		if (terminalId == null || !sessionterminalId.equals(terminalId)) {
			badRequest = true; // 请求有错误
			result = ParamToStr.formResult("FC_007", "probeId was not match");
			log.error(result);
		}
		return badRequest;
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
			if (value == null) {
				// 缺少参数key
				result = "failCode=FC_001&failReason=request lack of parameter: "
						+ key;
				log.error(result);
				badRequest = true;
			} else
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
		String line = readString(input);
		while (!line.equals("\r")) {
			line = readString(input);
		}
		try {
			int account = Integer.parseInt(params.get("account"));
			int length = Integer.parseInt(params.get("length"));
			data = new byte[account * length];
			byte[] tail = new byte[1024];
			// int inputLength = input.available();
			// data = new byte[inputLength];
			// if(inputLength <account*length){
			// badRequest = true;
			// //数据长度未达到要求
			// result = "failCode=FC_005&failReason=bytes was not enough";
			// log.error(result);
			// }
			int off = 0; // data的起始填充位置
			int readLen = 0; // 读取的长度
			while (true) {
				readLen = input.read(data, off, (data.length - off)); // 多次读取
				if (readLen == 0 || readLen == -1) // 如果读到足够长度或者数据流结束时跳出
					break;
				off += readLen;
			}
			int tailLen = input.read(tail);
			if (off != data.length || tailLen < 48) {
				badRequest = true;
				// 数据长度未达到要求
				result = "failCode=FC_005&failReason=bytes was not enough";
				log.error(result);
			}
			// System.out.println("tail: "+ tailLen);
			// int realLen = input.read(data, 0, data.length);
			// System.out.println(read);
			// input.read(tail);
			// FileOutputStream out = new FileOutputStream("receive");
			// while(input.read(data)!=-1)

			// if (tail[0] != -1 && tail[1] != -1){ // 最后一个字节不存在，说明字节数量没有达到预期
			// badRequest = true;
			// //数据长度未达到要求
			// result = "failCode=FC_005&failReason=bytes was not enough";
			// log.error(result);
			// }
			// out.write(data);
			// out.close();
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
