package converter;

import java.awt.AWTException;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.ConcurrentLinkedQueue;

import javax.imageio.ImageIO;

import com.itextpdf.io.image.ImageData;
import com.itextpdf.io.image.ImageDataFactory;
import com.itextpdf.kernel.geom.PageSize;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfString;
//import com.itextpdf.kernel.pdf.PdfName;
//import com.itextpdf.kernel.pdf.PdfString;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.kernel.pdf.canvas.parser.EventType;
import com.itextpdf.kernel.pdf.canvas.parser.data.IEventData;
import com.itextpdf.kernel.pdf.canvas.parser.data.ImageRenderInfo;
import com.itextpdf.kernel.pdf.canvas.parser.data.TextRenderInfo;
import com.itextpdf.kernel.pdf.canvas.parser.listener.IEventListener;
import com.itextpdf.kernel.pdf.xobject.PdfImageXObject;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Cell;
import com.itextpdf.layout.element.Image;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;

public class MyImageRenderListener implements IEventListener {

	protected String path;
	protected String extension = "png";
	private int labelNo = 1;

	private boolean startText = false;
	private StringBuilder sb = new StringBuilder();
	private Queue<ImageData> imageQueue = new ConcurrentLinkedQueue<>();

	
	public MyImageRenderListener(String path) {
		this.path = path;
	}

	
	
	public void eventOccurred(IEventData data, EventType type) {

		if (data == null) {
			return;
		}

		switch (type) {
		// retrieves any image in the document
		case RENDER_IMAGE:
			try {
				ImageRenderInfo renderInfo = (ImageRenderInfo) data;
				PdfImageXObject image = renderInfo.getImage();
				if (image == null) {
					return;
				}
				// imageFound = true;

				// You can access various value from dictionary here:
//				PdfString decodeParamsPdfStr = image.getPdfObject().getAsString(PdfName.DecodeParms);
//				String decodeParams = decodeParamsPdfStr != null ? decodeParamsPdfStr.toUnicodeString() : null;

				byte[] imageByte = image.getImageBytes(true);
				extension = image.identifyImageFileExtension();
				// saving image file to trim white surroundings
				File tempFile = File.createTempFile("qrTemp", ".png");
				tempFile.deleteOnExit();
				FileOutputStream fos = new FileOutputStream(tempFile);
				fos.write(imageByte);
				fos.close();
				File resized = processImage(tempFile);
				System.out.println("Immagine temporanea salvata in " + tempFile.getAbsolutePath());

				byte[] resizedBytes = new byte[(int) resized.length()];
				FileInputStream fis = new FileInputStream(resized);
				fis.read(resizedBytes, 0, resizedBytes.length);
				fis.close();
				imageQueue.add(ImageDataFactory.createPng(resizedBytes));

			} catch (Exception e) {
				System.out.println(e.getMessage());
			}
			break;
		default: {
			// will process only text after finding the image
			// if (imageFound) {

			if (data instanceof TextRenderInfo) {
				TextRenderInfo tri = (TextRenderInfo) data;
				PdfString pdfString = tri.getPdfString();
				if (pdfString != null) {
					String content = pdfString.getValue();
					if (content != null && content.length() > 0) {
						startText = true;
						String readString = tri.getPdfString().getValue();
						sb.append("  " + readString);
						sb.append("\n");
					}
				}
			} else if (startText) {
				System.out.println("Finito di leggere i testi.");
				System.out.println("Testo dopo l'immagine:");
				System.out.println(sb.toString());
				String filename = path + "\\" + labelNo + ".pdf";
				labelNo++;
				System.out.println("Etichetta temporanea verra' creata sul file " + filename);
				addImageAndText(imageQueue.poll(), sb.toString(), filename);
				resetSkipLogic();
				break;
			}
		}

		// }
		}
	}

	/**
	 * Gets the subset of the image which is the actual QR Code (60x60, starting at
	 * 20,20)
	 * 
	 * @param image The file containing the image with QRCode
	 * @return A temp file with the given bytes.
	 * @throws java.io.IOException
	 * @throws AWTException
	 */
	private File processImage(File image) throws java.io.IOException, AWTException {
		BufferedImage img = ImageIO.read(image);
		BufferedImage resizedbi = new BufferedImage(60, 60, BufferedImage.TYPE_INT_ARGB);
		Graphics2D graphics = resizedbi.createGraphics();
		graphics.drawImage(img, 0, 0, 60, 60, 20, 20, 80, 80, null);

		File resizedImageFile = File.createTempFile("qrResize", ".png");
		resizedImageFile.deleteOnExit();
		ImageIO.write(resizedbi, "png", resizedImageFile);
		return resizedImageFile;
	}

	/**
	 * Reset logics variable to skip empty texts after QR codes
	 */
	private void resetSkipLogic() {
		// imageFound = false;
		startText = false;
		sb = new StringBuilder();
		// img = null;
	}

	/**
	 * Constructs a file with the given image and text in a two-columns table
	 * 
	 * @param myImage The image to add to this dest file
	 * @param text    The text to add to the dest file
	 * @param dest    The dest file path
	 */
	private static void addImageAndText(final ImageData myImage, final String text, final String dest) {

		try {
			PdfWriter writer = new PdfWriter(dest);

			// Creating a PdfDocument
			PdfDocument pdf = new PdfDocument(writer);

			// Creating a Document
			Document document = new Document(pdf, new PageSize(1200, 600)); // PageSize.A4.rotate()

			Table table = new Table(new float[] { 150F, 150F, 440F });

			// Creating an Image object
			Image image = new Image(myImage);
			image.scale(9, 9); // 9,9 on A4

			Cell cell1 = new Cell(); // Creating a cell
			cell1.add(image); // Adding content to the cell
			table.addCell(cell1); // Adding cell to the table

			// Adding cell 2 to the table Cell
			Cell cell2 = new Cell(1, 2); // Creating a cell
			String[] lines = text.split("\n");
			{
				Paragraph paragraph = new Paragraph(lines[0]);
				paragraph.setFontSize(50); // 34 in A4
				cell2.add(paragraph);
			}
			{
				Paragraph paragraph = new Paragraph(lines[1]);
				paragraph.setFontSize(50); // 34 in A4
				cell2.add(paragraph); // Adding content to the cell
			}
			{
				Paragraph paragraph = new Paragraph(lines[2]);
				paragraph.setFontSize(50); // 34 in A4
				cell2.add(paragraph); // Adding content to the cell
			}
			{
				Paragraph paragraph = new Paragraph(lines[3]);
				paragraph.setFontSize(50); // 34 in A4
				cell2.add(paragraph); // Adding content to the cell
			}
			{
				Paragraph paragraph = new Paragraph(lines[4]);
				paragraph.setFontSize(90); // 34 in A4
				paragraph.setBold();
				cell2.add(paragraph); // Adding content to the cell
			}

			table.addCell(cell2);

			document.setMargins(10, 10, 10, 10);
			document.add(table);

			// Closing the document
			document.close();

			System.out.println("Table added");
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public Set<EventType> getSupportedEvents() {
		return null;
	}



	public void cleanUp() {
		System.out.println("Testo dopo l'immagine:");
		System.out.println(sb.toString());
		String filename = path + "\\" + labelNo + ".pdf";
		System.out.println("Etichetta temporanea verra' creata sul file " + filename);
		addImageAndText(imageQueue.poll(), sb.toString(), filename);
		resetSkipLogic();
		System.out.println("Lettura PDF completata");
	}
}
