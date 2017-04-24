package util;

import java.util.HashMap;
import java.util.Map;

public class ParamToStr {
	public static String changeToString(Map<String, String> map){
		StringBuilder sb = new StringBuilder();
		for(String key : map.keySet()){
			sb.append(key+"="+map.get(key)+"&");
		}
		return sb.toString().substring(0, sb.length()-1);
	}
	
	public static void main(String[] args) {
		Map<String, String> map = new HashMap<String, String>();
		map.put("name", "tiang");
		map.put("score", 18+"");
		String str = changeToString(map);
		System.out.println(str);
	}
}
