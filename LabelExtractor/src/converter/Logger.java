/**
 * 
 */
package converter;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.text.SimpleDateFormat;
import java.util.Calendar;

/**
 * @author a.lezza Logs to local file and standard input
 *
 */
public class Logger {

	static final SimpleDateFormat sdf = new SimpleDateFormat("[dd:MM:yyyy hh:mm:ss]");
	static PrintWriter pw;

	public static void print() {
		System.out.println();
		pw.println();
	}

	private static void printException(Exception e, boolean toLog, boolean toConsole) {
		if (toConsole) {
			e.printStackTrace();
		}
		if (toLog) {
			StackTraceElement[] stel = e.getStackTrace();
			for (StackTraceElement s : stel) {
				pw.println(s.toString());
			}
		}
	}

	public static void printExceptionToLogOnly(Exception e) {
		printException(e, true, false);
	}

	public static void print(Exception e) {
		printException(e, true, true);
	}

	public static void printToLog(String toPrint) {
		if (pw != null) {
			pw.println(toPrint);
		}
	}

	public static void print(String toPrint) {
		String p = sdf.format(Calendar.getInstance().getTime()) + " " + toPrint;
		System.out.println(p);
		printToLog(toPrint);
	}

	public static void initLog() {
		final String path = System.getProperty("user.dir");
		String hostName = "local";
		try {
			hostName = InetAddress.getLocalHost().getHostName();
		} catch (Exception e) {
		}
		File logFile = new File(path + "/" + hostName + "_log.log");
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
