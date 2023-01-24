/**
 * 
 */
package converter;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.Calendar;

/**
 * @author a.lezza
 * Logs to local file and standard input
 *
 */
public class Logger {
	
	static final SimpleDateFormat sdf = new SimpleDateFormat("[dd:MM:yyyy hh:mm:ss]");
	static PrintWriter pw;
	
	public static void print() {
		System.out.println();
		pw.println();
	}
	
	public static void print(String toPrint) {
		String p = sdf.format(Calendar.getInstance().getTime())+ " " + toPrint;
		System.out.println(p);
		if (pw != null) {
			pw.println(p);
		}
	}

	public static void initLog() {
		final String path = System.getProperty("user.dir");
		File logFile = new File(path+"/log.log");
		if (!logFile.exists()) {
			try {
				logFile.createNewFile();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		try {
			pw = new PrintWriter(logFile);
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		
	}
	
	public static void close() {
		if (pw != null) {
			pw.flush();
			pw.close();
		}
	}
	

}
