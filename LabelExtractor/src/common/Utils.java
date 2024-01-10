package common;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

import converter.Logger;

public class Utils {
	
	static byte[] noQrCodeBytes = null;
	
	public static byte[] getNoQrCodeBytes() {
		if (noQrCodeBytes == null) {
			try {
				noQrCodeBytes = Files.readAllBytes(Paths.get(new File("assets/img/NoQrCode.png").toURI()));
			} catch (IOException e) {
				Logger.print(e);
			}
		}
		return noQrCodeBytes;
	}

}
