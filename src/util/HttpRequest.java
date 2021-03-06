package util;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;

import receive.ProbeTerminalMap;
import receive.Session;
import receive.SessionState;

import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpExchange;

/**
 * http请求类，解析http请求
 * 
 * @author tiang
 * @date 2017-4-17
 * @version 1.0
 */
public class HttpRequest {
	protected HttpExchange exchange;
	// http请求头信息
	private final Map<String, List<String>> headMap = new HashMap<String, List<String>>();
	// http请求参数信息
	private final Map<String, String> paramMap = new HashMap<String, String>();

	protected boolean badRequest = false; // 请求是否有误
	protected String result; // 错误信息
	protected Logger log = Logger.getLogger(this.getClass());

	public String getResult() {
		return result;
	}

	public String getType() {
		return getParam("deviceType");
	}

	public HttpRequest(HttpExchange ex) {
		exchange = ex;
		initHeaderMap();
		// initParaMap();
	}

	/**
	 * 判断终端编号是否与session中的编号匹配
	 */
	public void validTerminalId() {
		String sessionTerminalId = getTerminalId(); // session保存的终端编号
		String parameterTerminalId = getParam("terminalId"); // 报文体重的终端编号
		if (!sessionTerminalId.equals(parameterTerminalId)) {
			badRequest = true; // 请求有错误
			result = ParamToStr
					.formResult("FC_006", "terminalId was not match");
			log.error(result);
		}
	}

	/**
	 * 判断探头编号是否与session中的终端编号匹配
	 */
	public void validProbeId() {
		String probeId = getParam("deviceId"); // 报文体中的探头编号
		// 获取map中对应的终端编号
		String terminalId = new ProbeTerminalMap().getTerminalId(probeId);
		String sessionterminalId = getTerminalId(); // 获取session中的终端编号
		if (terminalId == null || !sessionterminalId.equals(terminalId)) {
			badRequest = true; // 请求有错误
			result = ParamToStr.formResult("FC_007", "probeId was not match");
			log.error(result);
		}
	}

	/**
	 * 获取http请求头信息，按照键值对形式返回
	 * 
	 * @author tiang
	 * @date 2017-4-17
	 * @version 1.0
	 * @return 头键值对
	 */
	public Map<String, List<String>> getHeader() {
		return headMap;
	}

	/**
	 * 获取http请求的参数信息，按照键值对的形式返回
	 * 
	 * @author tiang
	 * @date 2017-4-17
	 * @version 1.0
	 * @return 参数键值对
	 */
	public Map<String, String> getParameters() {
		return paramMap;
	}

	/**
	 * 获取http请求方法，POST或GET
	 * 
	 * @author tiang
	 * @date 2017-4-17
	 * @version 1.0
	 * @return 请求方法
	 */
	public String getMethod() {
		return exchange.getRequestMethod().trim().toUpperCase();
	}

	/**
	 * 获取请求的服务器地址，可以用于判断请求类型
	 * 
	 * @author tiang
	 * @date 2017-4-17
	 * @version 1.0
	 * @return 请求地址
	 */
	public String getRequstUri() {
		return exchange.getRequestURI().toString();
	}

	/**
	 * 从请求中获取头信息，添加到键值对里
	 * 
	 * @author tiang
	 * @date 2017-4-17
	 * @version 1.0
	 */
	private void initHeaderMap() {
		Headers head = exchange.getRequestHeaders();
		for (String s : head.keySet()) {
			headMap.put(s.toLowerCase(), head.get(s));
		}
	}

	/**
	 * 从请求头的键值对中获取信息
	 * 
	 * @author tiang
	 * @date 2017-4-17
	 * @version 1.0
	 * @param headKey
	 *            键
	 * @return 值
	 */
	public String getHeadValue(String headKey) {
		List<String> values = headMap.get(headKey.toLowerCase());
		if (values == null)
			return null;
		if (values.size() == 1)
			return values.get(0);
		StringBuilder sb = new StringBuilder();
		for (String str : values) {
			sb.append(str + "\n");
		}
		return sb.toString();
	}

	/**
	 * 获取上传的参数信息
	 * 
	 * @author tiang
	 * @date 2017-4-17
	 * @version 1.0
	 * @param key
	 *            参数键
	 * @return 参数值
	 */
	public String getParam(String key) {
		return paramMap.get(key.toLowerCase());
	}

	/**
	 * 从请求中获取传输的参数信息
	 * 
	 * @author tiang
	 * @date 2017-4-17
	 * @version 1.0
	 */
	protected void initParaMap() {
		String method = getMethod();
		if (method.equals("GET")) { // GET请求
			String params = exchange.getRequestURI().getQuery();
			if (params == null)
				return;
			badRequest = !analysis(params, paramMap);
		} else if (method.equals("POST")) { // Post请求
			getPostParam();
		}
	}

	/**
	 * 从key1=value1&ke2=value2类型的字符串中提取键值对， 键值对的key值全部为小写
	 * 
	 * @author tiang
	 * @date 2017-4-18
	 * @version 1.0
	 * @param str
	 *            输入字符串
	 * @param param
	 *            需要填充的键值对
	 * @return 是否填充成功
	 */
	protected boolean analysis(String str, Map<String, String> param) {
		if (str == null)
			return true;
		String[] paraPair = str.split("&"); // 将字符串按照&分割
		List<String> tempPair = handleBrace(paraPair); // 将括号内被分割的字符串重新拼接回来
		for (String pair : tempPair) {
			String[] entry = pair.split("="); // 按照=分割
			if (entry.length < 2) // 如果长度小于2，则跳过
				continue;
			else
				param.put(entry[0].toLowerCase().trim(),
						pair.substring(entry[0].length() + 1).trim()); // 处理value里面有等号的情况
		}
		return true;
	}

	/**
	 * 诸如key={key1=value1&key2=value2}形式的字符串，经过上一步的分隔成为key={key1=value1
	 * 和key2=value2} 该方法负责将其拼接起来
	 * 
	 * @author tiang
	 * @date 2017-4-20
	 * @version 1.0
	 * @param strs
	 *            字符串数组
	 * @return 拼接后的字符串列表
	 */
	private List<String> handleBrace(String[] strs) {
		List<String> list = new ArrayList<String>();
		for (int i = 0; i < strs.length; i++) {
			String tempStr = strs[i];
			if (tempStr.contains("{")) {
				while (!tempStr.contains("}")) {
					i++;
					tempStr += "&" + strs[i];
				}
			}
			list.add(tempStr);
		}
		return list;
	}

	/**
	 * 获取POST请求的参数列表，因为目前对参数没有进行任何修饰说明，所以比较简单， 日后若采用复杂的报文体结构，只需修改这个解析函数即可
	 * 
	 * @author tiang
	 * @date 2017-4-18
	 * @version 1.0
	 */
	private void getPostParam() {
		String boundry = getHeadValue("boundry"); // 获取分割符
		if (boundry != null) { // 存在分隔符，同时发送参数和文件

		} else { // 不存在分隔符，只发送了参数
			String content = getStringRequestBody();
			badRequest = !analysis(content, paramMap);
		}
	}

	/**
	 * 请求是否有问题
	 * 
	 * @author tiang
	 * @date 2017-4-18
	 * @version 1.0
	 * @return
	 */
	public boolean isBadRequest() {
		return badRequest;
	}

	/**
	 * 将请求体以字符串的形式读取
	 * 
	 * @author tiang
	 * @date 2017-4-17
	 * @version 1.0
	 * @return 请求体字符串
	 */
	public String getStringRequestBody() {
		InputStream in = exchange.getRequestBody();
		BufferedReader reader = new BufferedReader(new InputStreamReader(in));
		StringBuilder sb = new StringBuilder();
		String line;
		try {
			// 读一行数据，无数据时跳出循环
			while ((line = reader.readLine()) != null) {
				sb.append(line + "\n");
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		String str = sb.toString();
		// System.out.println(str);
		log.info("request content: " + str);
		return str.length() != 0 ? str.substring(0, str.length() - 1) : null;
	}

	/**
	 * 将请求体保存到文件中
	 * 
	 * @author tiang
	 * @date 2017-4-17
	 * @version 1.0
	 */
	public void saveFile() {
		InputStream in = exchange.getRequestBody();
		BufferedReader reader = new BufferedReader(new InputStreamReader(in));
		try {
			FileWriter writer = new FileWriter(new File("receive.txt"), false);
			String line;
			while ((line = reader.readLine()) != null)
				writer.write(line + "\n");
			writer.flush();
			writer.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	/**
	 * 获取终端编号
	 * 
	 * @author tiang
	 * @date 2017-4-19
	 * @version 1.0
	 * @return 终端编号
	 */
	protected String getTerminalId() {
		String sessionId = getHeadValue("sessionId");
		if (sessionId == null)
			return null;
		else
			return sessionId.split("_")[0];
	}

	/**
	 * 判断session是否存在以及是否超时
	 * 
	 * @author tiang
	 * @date 2017-4-19
	 * @version 1.0
	 * @return
	 */
	public SessionState judgeSession() {
		String sessionId = getHeadValue("sessionId");
		if (sessionId == null) { // 请求头中不包含session
			return SessionState.NotSend;
		} else {
			Session ss = new Session();
			String terminalId = sessionId.split("_")[0];
			String serverss = ss.getSessionId(terminalId); // 服务器端的session
			if (serverss == null) { // 服务器未保存session
				return SessionState.NotExist;
			} else if (!serverss.equals(sessionId)) { // session值不匹配
				return SessionState.Invalid;
			} else {
				if (ss.isTimeOut(terminalId)) {
					return SessionState.TimeOut;
				} else {
					ss.updateTime(terminalId); // 更新session
					return SessionState.Valid;
				}
			}
		}
	}

	/**
	 * 判断请求是否存在问题，键值对的参数未转化为小写
	 * 
	 * @author tiang
	 * @date 2017-4-25
	 * @version 1.0
	 * @param params
	 *            存放参数值的map
	 * @param keys
	 *            请求应包含的参数列表
	 * @return 请求是否存在问题
	 */
	public boolean isBadRequest(Map<String, String> params, String... keys) {
		for (String key : keys) {
			String value = null;
			if (key.equalsIgnoreCase("terminalId")) { // 如果是终端编号
				value = getTerminalId();
			} else {
				value = getParam(key);
			}
			if (value == null) {
				badRequest = true;
				// 缺少参数key
				result = "failCode=FC_001&failReason=request lack of parameter: "
						+ key;
				log.error(result);
				break;
			} else {
				params.put(key, value);
			}
		}
		return badRequest;
	}
}
