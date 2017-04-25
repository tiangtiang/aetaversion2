package runback;

import java.io.ByteArrayInputStream;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Map;

import db.DBPool;

public class DatapostThread implements Runnable {

	private final Map<String, String> params;
	private final byte[] data;
	private short[] dataAfterAnalysis;

	public DatapostThread(Map<String, String> map, byte[] bytes) {
		params = map;
		data = bytes;
	}

	@Override
	public void run() {
		insertToDB();
	}

	public void insertToDB() {
		/**
		 * 将params里的键值对和二进制数据插入数据库
		 */
		int dataType = Integer.parseInt(params.get("dataType"));

		if (dataType >= 1 && dataType <= 4) {
			// 原始数据和特征数据入库
			rawAndFeatureDataIntoDB(dataType);
		} else if (dataType == 7) {
			// 温度数据入库
			tempretureDataIntoDB(dataType);
		}
		System.out.println(data[0]);
		System.out.println(params);
	}

	// 原始数据和特征数据入库
	public void rawAndFeatureDataIntoDB(int dataType) {
		// 特征数据和原始数据对应的数据库表名
		String[] tableNames = new String[2];
		tableNames = getDataTableName(dataType);
		// 获取当前数据的时间戳
		int Time = Integer.parseInt(params.get("time"));
		// 获取当前数据对应的探头ID
		int ProbeID = Integer.parseInt(params.get("probeId"));
		// 获取当前数据的单字节点数
		int Length = Integer.parseInt(params.get("length"))
				* Integer.parseInt(params.get("account"));
		// 特征值-------均值
		float average = getAverage(dataType);
		// 特征值-------振铃计数
		int RDC = getRDC(dataType, 0);// 这里振铃计数的阈值暂时设为0.
		Peak peak = this.getPFandPA(dataType);
		// 特征值-------全局峰值频率
		double peak_frequency = peak.getPeak_frequency();
		// 特征值-------全局峰值幅值
		float peak_amplitude = peak.getPeak_amplitude();
		Connection conn = DBPool.create().getConnection("aetaVersion_ds_1");
		PreparedStatement prestmt;
		// 开启事务
		try {
			conn.setAutoCommit(false);

			// 插入原始数据
			prestmt = conn.prepareStatement("insert into " + tableNames[1]
					+ "(Time,ProbeID,Length,Value) values(?,?,?,?)");
			prestmt.setInt(1, Time);
			prestmt.setInt(2, ProbeID);
			prestmt.setInt(3, Length);
			prestmt.setBlob(4, new ByteArrayInputStream(data));
			int flag1 = prestmt.executeUpdate();
			if (flag1 > 0) {
				// 插入特征数据
				prestmt = conn
						.prepareStatement("insert into "
								+ tableNames[0]
								+ "(Time,ProbeID,average,Ring_down_count,peak_frequency,peak_amplitude) values(?,?,?,?,?,?)");
				prestmt.setInt(1, Time);
				prestmt.setInt(2, ProbeID);
				prestmt.setFloat(3, average);
				prestmt.setInt(4, RDC);
				prestmt.setDouble(5, peak_frequency);
				prestmt.setFloat(6, peak_amplitude);
				int flag2 = prestmt.executeUpdate();
				if (flag2 > 0) {// 操作成功，提交事务
					conn.commit();
				} else {// 操作失败，回滚事务
					conn.rollback();
				}
			} else {// 操作失败，回滚事务
				conn.rollback();
			}
			prestmt.close();
			conn.setAutoCommit(true);
			// conn.close();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	// 温度数据入库
	public void tempretureDataIntoDB(int dataType) {
		// 特征数据和原始数据对应的数据库表名
		String[] tableNames = new String[2];
		tableNames = getDataTableName(dataType);
		// 获取当前数据的时间戳
		int Time = Integer.parseInt(params.get("time"));
		// 获取当前数据对应的终端ID
		int terminalId = Integer.parseInt(params.get("terminalId"));
		// 获取当前数据对应的探头ID
		int ProbeID = Integer.parseInt(params.get("probeId"));
		// 特征值-------温度
		float tempreture = getTempreture();
		Connection conn = DBPool.create().getConnection("aetaVersion_ds_1");
		PreparedStatement prestmt;
		// 插入原始数据
		try {
			prestmt = conn.prepareStatement("insert into " + tableNames[0]
					+ "(Time,TerminalID,ProbeID,Tempreture) values(?,?,?,?)");
			prestmt.setInt(1, Time);
			prestmt.setInt(2, terminalId);
			prestmt.setInt(3, ProbeID);
			prestmt.setFloat(4, tempreture);
			prestmt.executeUpdate();
			prestmt.close();
			// conn.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	// 根据数据类型获取对应的数据库表名
	public String[] getDataTableName(int dataType) {
		String[] tableNames = new String[2];
		switch (dataType) {
		case 1:
			tableNames[0] = "finaldata_lowfreq_magn";
			tableNames[1] = "rawdata_lowfreq_magn";
			break;
		case 2:
			tableNames[0] = "finaldata_fullfreq_magn";
			tableNames[1] = "rawdata_fullfreq_magn";
			break;
		case 3:
			tableNames[0] = "finaldata_lowfreq_sound";
			tableNames[1] = "rawdata_lowfreq_sound";
			break;
		case 4:
			tableNames[0] = "finaldata_fullfreq_sound";
			tableNames[1] = "rawdata_fullfreq_sound";
			break;
		case 7:
			tableNames[0] = "finaldata_tempreture";
			break;
		default:
			System.out.println("--------dataType of rawdata is wrong!");
		}
		return tableNames;
	}

	// 计算原始数据的均值，计算方式为：将所有原始数据的值取绝对值然后再求和取平均
	public float getAverage(int dataType) {
		float result;
		// 这里是否需要对数组长度做检查还是在数据接收时就对数组长度作检查？？？？？？？
		long sum = 0;
		this.dataAfterAnalysis = new short[this.data.length / 2];
		// short temp = (short) ((0xff & data[1]) | (0xff00 & (data[0] << 8)));
		for (int i = 0; i < data.length; i = i + 2) {
			short temp1 = (short) ((0xff & data[i + 1]) | (0xff00 & (data[i] << 8)));
			// 将解析之后的字节数存放在short类型数组中
			this.dataAfterAnalysis[i / 2] = temp1;
			sum += Math.abs(temp1);
		}
		result = (float) (sum / (data.length / 2.0));
		if (dataType >= 1 && dataType <= 2)
			result = (result / 32767) * 12.288f;// 把低频电磁对应的原始数据的均值转化为对应的电压值，计算方式为(average/32767)*12.88
		else {
			result = (result / 32767) * 5f;// 把低频电磁对应的原始数据的均值转化为对应的电压值，计算方式为(average/32767)*5
		}
		return result;
	}

	// 计算原始数据的振铃计数，计算方式为：原始数据对应的波形图单向穿越0值的次数，这里的计算方式为向上穿越0值的次数（包括等于0的情况）
	public int getRDC(int dataType, int threshold) {
		int result = 0;
		// temp 为前一个数
		short temp = dataAfterAnalysis[0];
		// 这里是否需要对数组长度做检查还是在数据接收时就对数组长度作检查？？？？？？？
		for (int i = 1; i < dataAfterAnalysis.length; i++) {
			// temp1为后一个数
			short temp1 = dataAfterAnalysis[i];
			if (temp <= threshold && temp1 > threshold) {
				result++;
			}
			temp = temp1;
		}
		// 把低频电磁或低频地声对应的原始数据的振铃计数应该还需要除以60
		// （即之前的30000个点是60秒采集的，现在需要转换成每秒对应的单向穿越0值次数）
		// 另外注：全频电磁和全频地声30000个点是1秒钟的，不需要做处理
		if (dataType == 1 || dataType == 3)
			result = result / 60;
		return result;
	}

	// 计算特征数据----温度，计算方式为：原始数据对应的温度除以100
	public float getTempreture() {
		float result = 0;
		short temp = (short) ((0xff & data[1]) | (0xff00 & (data[0] << 8)));
		result = (float) (temp / 100.0);
		return result;
	}

	// 傅里叶变换 从北大徐伯星老师里复制过来的，具体实现细节没仔细研究
	public int FFT(float x[], float y[], int n, int sign) {
		if (n % 2 != 0 || (sign != -1 && sign != 1)) {
			// printf("Error:arguments.\n");
			return (-1);
		}
		int i, j, k, l, m = 0, n1, n2;
		float c, c1, e, s, s1, t, tr, ti;
		for (j = 1, i = 1; i < n; i++) {
			m = i;
			j = 2 * j;
			if (j == n)
				break;
		}
		n1 = n - 1;
		for (j = 0, i = 0; i < n1; i++) {
			if (i < j) {
				tr = x[j];
				ti = y[j];
				x[j] = x[i];
				y[j] = y[i];
				x[i] = tr;
				y[i] = ti;
			}
			k = n / 2;
			while (k < (j + 1)) {
				j = j - k;
				k = k / 2;
			}
			j = j + k;
		}
		n1 = 1;
		for (l = 1; l <= m; l++) {
			n1 = 2 * n1;
			n2 = n1 / 2;
			e = (float) 3.1415926 / n2;
			c = 1.0f;
			s = 0.0f;
			c1 = (float) Math.cos(e);
			s1 = (float) (-sign * Math.sin(e));
			for (j = 0; j < n2; j++) {
				for (i = j; i < n; i += n1) {
					k = i + n2;
					tr = c * x[k] - s * y[k];
					ti = c * y[k] + s * x[k];
					x[k] = x[i] - tr;
					y[k] = y[i] - ti;
					x[i] = x[i] + tr;
					y[i] = y[i] + ti;
				}
				t = c;
				c = c * c1 - s * s1;
				s = t * s1 + s * c1;
			}
		}
		if (sign == -1) {
			for (i = 0; i < n; i++) {
				x[i] /= n;
				y[i] /= n;
			}
		}
		return 0;
	}

	// 计算全局峰值频率和全局峰值幅值
	public Peak getFeature(float[] rawData, float sampleTimes) {
		int result = 0;
		// long sum = 0;
		int m = rawData.length > 8192 ? 8192 : rawData.length;
		float[] y = new float[m];
		float amp;
		float max = -32767.0f;
		int ret = FFT(rawData, y, m, 1);
		Peak peak = new Peak();
		if (ret == 0) {
			int points = (int) (m / sampleTimes);
			for (int i = 2; i <= points; i++) {
				amp = (float) Math.sqrt(rawData[i] * rawData[i] + y[i] * y[i]);
				if (max < amp) {
					peak.setPeak_frequency(i);
					// 下面被注释掉的代码为北大徐伯星老师的C++版代码
					// ft->TopFrequence = i;
					max = amp;
				}
			}
			peak.setPeak_amplitude(max / m);
			// 下面被注释掉的代码为北大徐伯星老师的C++版代码
			// ft->FreqAmplitude = max/m;
		} else {
			System.out.println("FFT err:ret=" + ret);
			result = -1;
		}
		result = 0;
		peak.setRet(result);
		return peak;
	}

	// 临时存储fft的结果的类
	class Peak {
		public float peak_amplitude; // 全局峰值幅值
		public double peak_frequency; // 全局峰值频率
		public int ret; // fft结果

		public float getPeak_amplitude() {
			return peak_amplitude;
		}

		public void setPeak_amplitude(float peakAmplitude) {
			peak_amplitude = peakAmplitude;
		}

		public double getPeak_frequency() {
			return peak_frequency;
		}

		public void setPeak_frequency(double peakFrequency) {
			peak_frequency = peakFrequency;
		}

		public int getRet() {
			return ret;
		}

		public void setRet(int ret) {
			this.ret = ret;
		}
	}

	// 计算低频电磁的全局峰值频率和全局峰值幅值
	public Peak getFeature_EM_Low(short[] rawData) {
		float[] rawData_temp = new float[rawData.length];
		for (int i = 0; i < rawData.length; i++) {
			rawData_temp[i] = rawData[i];
		}
		Peak peak = getFeature(rawData_temp, 2.5f);
		peak.setPeak_frequency(peak.getPeak_frequency() * 500 / 8192.0);
		peak.setPeak_amplitude(peak.getPeak_amplitude() * 2);
		// 下面被注释掉的代码为北大徐伯星老师的C++版代码
		// ft->TopFrequence = ft->TopFrequence*500/8192;
		// ft->FreqAmplitude = ft->FreqAmplitude*2;
		return peak;
	}

	// 计算全频电磁的全局峰值频率和全局峰值幅值
	public Peak getFeature_EM_Full(short[] rawData) {
		float[] rawData_temp = new float[rawData.length];
		for (int i = 0; i < rawData.length; i++) {
			rawData_temp[i] = rawData[i];
		}
		Peak peak = getFeature(rawData_temp, 3.0f);
		peak.setPeak_frequency(peak.getPeak_frequency() * 30000 / 8192);
		peak.setPeak_amplitude(peak.getPeak_amplitude() * 2);
		return peak;
	}

	// 计算低频地声的全局峰值频率和全局峰值幅值
	public Peak getFeature_SD_Low(short[] rawData) {
		float[] rawData_temp = new float[rawData.length];
		for (int i = 0; i < rawData.length; i++) {
			rawData_temp[i] = rawData[i];
		}
		Peak peak = getFeature(rawData_temp, 2.5f);
		peak.setPeak_frequency(peak.getPeak_frequency() * 500 / 8192);
		peak.setPeak_amplitude(peak.getPeak_amplitude() * 2);
		return peak;
	}

	// 计算全频地声的全局峰值频率和全局峰值幅值
	public Peak getFeature_SD_Full(short[] rawData) {
		float[] rawData_temp = new float[rawData.length];
		for (int i = 0; i < rawData.length; i++) {
			rawData_temp[i] = rawData[i];
		}
		Peak peak = getFeature(rawData_temp, 3.0f);
		peak.setPeak_frequency(peak.getPeak_frequency() * 150000 / 8192);
		peak.setPeak_amplitude(peak.getPeak_amplitude() * 2);
		return peak;
	}

	// 获取特征数据-------全局峰值频率和全局峰值幅值
	public Peak getPFandPA(int dataType) {
		Peak peak = null;
		switch (dataType) {
		case 1:
			peak = this.getFeature_EM_Low(this.dataAfterAnalysis);
			break;
		case 2:
			peak = this.getFeature_EM_Full(this.dataAfterAnalysis);
			break;
		case 3:
			peak = this.getFeature_SD_Low(this.dataAfterAnalysis);
			break;
		case 4:
			peak = this.getFeature_SD_Full(this.dataAfterAnalysis);
			break;
		default:
			System.out
					.println("dataType of rawData is wrong! The value of dataType is "
							+ dataType);
		}
		return peak;
	}

}
