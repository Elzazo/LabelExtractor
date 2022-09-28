package converter;

import java.io.File;
import java.io.FileFilter;
import java.io.FileNotFoundException;
import java.io.IOException;

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
	 */
	public static void main(String[] args) {
		final String path = System.getProperty("user.dir");
		System.out.println("Directory di lavoro = " + path);
		File file = null;
		File[] pdfFiles = null;
		if (args == null || args.length != 1) {
			System.out.println("Verrano processati i file PDF nella directory " + path);
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
			System.out.println("Nessun file PDF nella directory " + path);
			System.out.println("Esecuzione terminata");
			return;
		}

		System.out.println("************** LISTA FILE ***********************");
		for (File pdf : pdfFiles) {
			System.out.println(pdf.getName());
		}
		System.out.println("*************************************************");
		String outPath = path + File.separator + "convertiti";
		System.out.println("I file PDF in ingresso verranno convertiti nella directory " + outPath);
		{
			File createDirIfNecessary = new File(outPath);
			if (!createDirIfNecessary.exists()) {
				try {
					if (!createDirIfNecessary.mkdir()) {
						System.err.println("Impossibile creare la directory " + outPath);
						return;
					} else {
						System.out.println("Creata la directory di lavoro " + outPath);
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
				System.out.println("File " + pdfFile + " processato.");
			} catch (IOException e) {
				e.printStackTrace();
			}

		}

	}

	private static void extractImagesAndCreateTempFiles(final String sourceFile, final String outPath)
			throws IOException {
		PdfDocument pdfDoc = new PdfDocument(new PdfReader(sourceFile));
		IEventListener listener = new MyImageRenderListener(outPath);
		PdfCanvasProcessor parser = new PdfCanvasProcessor(listener);
		for (int i = 1; i <= pdfDoc.getNumberOfPages(); i++) {
			parser.processPageContent(pdfDoc.getPage(i));
		}
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
		
		//clean-up
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

		// Creating a PdfDocument
		PdfDocument pdf = new PdfDocument(writer);

		System.out.println("Riunisco i file...");
		for (File temp : tempFiles) {
			try {
				PdfReader reader = new PdfReader(temp);
				System.out.println("File " + temp.getName());
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
