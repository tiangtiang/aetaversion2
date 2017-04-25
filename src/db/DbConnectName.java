package db;

import java.io.File;
import java.util.LinkedList;
import java.util.List;

import org.dom4j.Document;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;

public class DbConnectName {

	// 含多个数据库别名的配置文件
	private static final String CONFIGFILE = "conf/dbConnectNameConfig.xml";
	// 数据库别名
	private static List<String> dbNames = new LinkedList<String>();

	public static List<String> getDbNames() {
		return dbNames;
	}

	/**
	 * <p>
	 * 描述：获取所有的数据库配置在c3p0中对应的别名
	 * </p>
	 * 
	 * @author 甘颖
	 * @date 2017-04-16
	 * @version 1.0
	 */
	static void getAllDbNames() {
		try {
			File f = new File(CONFIGFILE);
			SAXReader reader = new SAXReader();
			Document doc = reader.read(f);
			Element root = doc.getRootElement();
			Element foo;
			// 将配置文件中的数据库别名读到dbNames中
			// for (Iterator i = root.elementIterator("name"); i.hasNext();) {
			// foo = (Element)i.next();
			// dbNames.add(foo.getText());
			// }
			for (Object obj : root.elements("name")) {
				foo = (Element) obj;
				dbNames.add(foo.getText());
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	/*
	 * public static void main(String args[]) { DbConnectName test = new
	 * DbConnectName(); test.getAllDbNames(); }
	 */

}