package org.blockframe.core;

import java.io.File;
import java.io.IOException;

import org.apache.pdfbox.pdmodel.PDDocument;

/**
 * Top level class for writing a PDF file. 
 * This subclass of {@link PdfChapter} manages the creation of the PDF-Box document object, and writes the document to the supplied file. 
 */
public class PdfDocument extends PdfChapter { 
	
	protected PdfDocument() { 
		super(new PDDocument()); 
	}
	
	/**
	 * Writes a PDF to the given file. 
	 */
	public void writeFile(File file) throws IOException { 
		makePages();  
		pdDocument.save(file); 
		pdDocument.close(); 
	}

}
