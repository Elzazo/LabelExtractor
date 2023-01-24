package converter;

import java.io.File;
import java.io.FileFilter;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Arrays;
import java.util.Comparator;

import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfReader;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.kernel.pdf.canvas.parser.PdfCanvasProcessor;
import com.itextpdf.kernel.pdf.canvas.parser.listener.IEventListener;

/**
 * 
 */

/**
 * @author a.lezza
 *
 */
public class Converter {

	/**
	 * @param args
	 * @throws InterruptedException 
	 */
	public static void main(String[] args) throws InterruptedException {
		Logger.initLog();
		displayBanner();
		
		final String path = System.getProperty("user.dir");
		Logger.print("Directory di lavoro = " + path);
		Thread.sleep(1000);
		File file = null;
		File[] pdfFiles = null;
		if (args == null || args.length != 1) {
			Logger.print("Verrano processati i file PDF nella directory " + path);
			Thread.sleep(1000);
			File dir = new File(path);
			pdfFiles = dir.listFiles(new FileFilter() {

				@Override
				public boolean accept(File pathname) {
					return pathname != null && pathname.toString().endsWith(".pdf");
				}
			});

		} else {
			file = new File(args[0]);
			pdfFiles = new File[] { file };
		}

		if (pdfFiles == null || pdfFiles.length == 0) {
			Logger.print("Nessun file PDF nella directory " + path);
			Logger.print("Esecuzione terminata");
			Thread.sleep(3000);
			return;
		}

		Logger.print("************** LISTA FILE ***********************");

		for (File pdf : pdfFiles) {
			Logger.print(pdf.getName());
		}
		Logger.print("*************************************************");
		Thread.sleep(3000);
		String outPath = path + File.separator + "etichetteZebra";
		Logger.print("I file PDF in ingresso verranno convertiti nella directory " + outPath);
		Thread.sleep(2000);
		{
			File createDirIfNecessary = new File(outPath);
			if (!createDirIfNecessary.exists()) {
				try {
					if (!createDirIfNecessary.mkdir()) {
						System.err.println("Impossibile creare la directory " + outPath);
						return;
					} else {
						Logger.print("Creata la directory di lavoro " + outPath);
						Thread.sleep(500);
					}
				} catch (Exception e) {
					System.err.println("Impossibile creare la directory " + outPath);
					return;
				}
			}
		}
		for (File pdfFile : pdfFiles) {
			try {
				extractImagesAndCreateTempFiles(pdfFile.getAbsolutePath(), outPath);
				Logger.print("File " + pdfFile + " elaborato.");
				createElaboratiDir(path+"/elaborati/");
				File moved = new File (path+"/elaborati/"+pdfFile.getName());
				if (moved.exists()) {
					moved.delete();
				}
				pdfFile.renameTo(new File (path+"/elaborati/"+pdfFile.getName()));
				Thread.sleep(2000);
			} catch (IOException e) {
				e.printStackTrace();
			}

		}
		
		Logger.close();
	}

	private static void createElaboratiDir(String path) {
		new File (path).mkdir();		
	}

	private static void displayBanner() {
		Logger.print();
		Logger.print();
		Logger.print();
		Logger.print("==============================================");	
		Logger.print("========== Convertitore etichette GeCo =======");
		Logger.print("=============== T.A.R SARDEGNA ===============");
		Logger.print("===========Aldo Lezza a.lezza@giaum.it========");
		Logger.print("==============================================");
		Logger.print();
		Logger.print();
		Logger.print();
	}

	private static void extractImagesAndCreateTempFiles(final String sourceFile, final String outPath)
			throws IOException {
		PdfDocument pdfDoc = new PdfDocument(new PdfReader(sourceFile));
		MyImageRenderListener myListener = new MyImageRenderListener(outPath);
		IEventListener listener = myListener;
		PdfCanvasProcessor parser = new PdfCanvasProcessor(listener);
		Logger.print(pdfDoc.getNumberOfPages()+ " pagine da processare nel file "+sourceFile);
		for (int i = 1; i <= pdfDoc.getNumberOfPages(); i++) {
			parser.processPageContent(pdfDoc.getPage(i));
		}
		myListener.cleanUp();
		pdfDoc.close();
		mergeFiles(new File(sourceFile).getName(), outPath);
	}

	private static void mergeFiles(String name, String outPath) {
		PdfWriter writer = null;
		String destFile = outPath + File.separator + name.substring(0, name.lastIndexOf(".pdf")) + "_etichette"
				+ ".pdf";
		try {
			writer = new PdfWriter(destFile);
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		// clean-up
		{
			File dest = new File(destFile);
			if (dest.exists()) {
				dest.delete();
			}
		}

		if (writer == null) {
			System.err.println("Impossibile scrivere nel file " + destFile);
			return;
		}

		File[] tempFiles = new File(outPath).listFiles(new FileFilter() {

			@Override
			public boolean accept(File pathname) {
				return pathname.getName().endsWith("pdf") && pathname.getName().matches("[0-9]+\\.pdf");
			}
		});
		
		
		Arrays.sort(tempFiles, new Comparator<File>() {

			@Override
			public int compare(File o1, File o2) {
				Integer one = Integer.parseInt(o1.getName().replaceAll(".pdf", ""));
				Integer two = Integer.parseInt(o2.getName().replaceAll(".pdf", ""));
				
				return one.equals(two) ? 0 : one < two ? -1 : 1;
			}
		});

		// Creating a PdfDocument
		PdfDocument pdf = new PdfDocument(writer);

		Logger.print("Riunisco i file...");
		for (File temp : tempFiles) {
			try {
				PdfReader reader = new PdfReader(temp);
				Logger.print("File " + temp.getName());
				PdfDocument document = new PdfDocument(reader);
				document.copyPagesTo(1, document.getNumberOfPages(), pdf);
				document.close();
				reader.close();
				temp.deleteOnExit();
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

		pdf.close();
	}

}
