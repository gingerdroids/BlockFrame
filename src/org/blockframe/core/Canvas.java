package org.blockframe.core;

import java.awt.Color;
import java.io.IOException;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.blockframe.core.Block.PlacedBlock;


/**
 * Holder for the information needed to write onto a PDF-Box page. 
 * A {@linkplain Canvas} instance is passed down through the {@link Block#draw(Canvas, double, double, double, double)} pass through the content-tree. 
 * <p>
 * The PDF-Box {@link PDPage} and {@link PDPageContentStream} objects are exposed in this class. 
 */
public class Canvas { 
	
	/**
	 * The PDF-Box page that canvas writes through to. 
	 */
	public final PDPage page ; 
	
	/**
	 * The PDF-Box content-stream this canvas writes through. 
	 * <p>
	 * This is generated from both a specific {@link PDPage} and a specific {@link PDDocument}. 
	 */
	public final PDPageContentStream stream ; 
	
	/**
	 * Height of PDF-Box page. 
	 * <p>
	 * The top and bottom BlockFrame page margins are contained in this height. 
	 */
	public final double pageHeight ; // Needed for inverting the vertical co-ordinates. 
	
	/**
	 * Last colour requested by app-layer for stroking a colour. 
	 */
	private Color requestedStrokingColor ; 
	
	/**
	 * Last colour set to the stream for stroking a colour. 
	 */
	private Color actualStrokingColor ; 
	
	/**
	 * Last colour requested by app-layer for non-stroking a colour. 
	 */
	private Color requestedNonStrokingColor ; 
	
	/**
	 * Last colour set to the stream for non-stroking a colour. 
	 */
	private Color actualNonStrokingColor ; 

	/**
	 * Threshold for logging debug messages. Zero is no messages, large is more. 
	 * <p>
	 * I've mostly used the {@link Block#loggingVerbosity} field, but this is left here in case it's useful. 
	 * @see DebugLog
	 */
	public double loggingVerbosity = 0 ; 
	
	/**
	 * Constructor. 
	 * <p>
	 * It is important that {@link #close()} be called, because it calls {@link PDPageContentStream#close()}. 
	 * This is usually managed for you in the top level {@link PdfChapter} and similar classes. 
	 */
	Canvas(PDDocument document, PDPage page) throws IOException { 
		this.page = page ; 
		this.pageHeight = page.getMediaBox().getHeight(); 
		this.stream = new PDPageContentStream(document, page); 
	}
	
	/**
	 * Ensures the resources are tidied up. 
	 * The method <code>close</code> is called on field {@link #stream}. 
	 */
	public void close() throws IOException { 
		stream.close(); 
	}

	/**
	 * Converts BlockFrame vertical co-ordinates to PDF vertical co-ordinates. 
	 * This method does not allow for the height of a block (see {@link #getPdfBottom(double, double)}. 
	 * <p>
	 * BlockFrame uses co-ordinates based at the top-left, whereas PDF uses co-ordinates based at the bottom-left. 
	 * @param height
	 * @return
	 */
	public float getPdfY(double height) { 
		return (float) (pageHeight - height) ; 
	}
	
	/**
	 * Given the BlockFrame top co-ordinate of a Block, this method returns the PDF co-ordinate of the bottom of the block. 
	 * This might be useful when drawing the content of a block, because PDF draw-methods are based at the bottom left of the drawn object's rectangle. 
	 * @param block Provides the height of the block. 
	 */
	public float getPdfBottom(double top, PlacedBlock block) { 
		return (float) (pageHeight - (top+block.getHeight())); 
	}

	/**
	 * Given the BlockFrame top and height of a Block, this method returns the PDF co-ordinate of the bottom of the block. 
	 * This might be useful when drawing the content of a block, because PDF draw-methods are based at the bottom left of the drawn object's rectangle. 
	 */
	public float getPdfBottom(double top, double height) { 
		return (float) (pageHeight - (top+height)); 
	}
	
	/**
	 * Getter for field {@link Canvas#requestedStrokingColor}. 
	 * @see Canvas#setStrokingColor(Color)
	 */
	public Color getRequestedStrokingColor() {
		return requestedStrokingColor;
	}
	
	/**
	 * Getter for field {@link Canvas#actualStrokingColor}. 
	 * @see Canvas#setStrokingColor(Color)
	 */
	public Color getActualStrokingColor() { 
		return actualStrokingColor;
	}
	
	/**
	 * Ensures the stroking color of the {@link PDPageContentStream} is set to the appropriate colour. 
	 * When the app-layer is about to draw using the stroking colour, it should call this method to ensure the stream's colour is set correctly. 
	 * 
	 * @param color The colour to set the stream to. If this is null, the last colour requested is used (see {@link #restoreStrokingColor(Color)}). 
	 * @return The old color - useful for restoring the color once the caller has finished drawing whatever it had to draw. 
	 */
	public Color setStrokingColor(Color color) throws IOException { 
		Color oldColor = this.requestedStrokingColor ; 
		if (color!=null) this.requestedStrokingColor = color ; 
		if (requestedStrokingColor!=actualStrokingColor) { 
			Color newColor = requestedStrokingColor != null ? requestedStrokingColor : Color.BLACK ; 
			stream.setStrokingColor(newColor); 
			this.actualStrokingColor = requestedStrokingColor ; 
		}
		return oldColor ; 
	}
	
	/**
	 * Sets the canvas's requested colour for drawing. 
	 * But, does not modify the {@link PDPageContentStream}. 
	 * @param oldColor Usually, the return-value of a prior call to {@link #setStrokingColor(Color)}. 
	 */
	public void restoreStrokingColor(Color oldColor) { 
		this.requestedStrokingColor = oldColor ; 
	}

	/**
	 * Getter for field {@link Canvas#requestedNonStrokingColor}. 
	 * @see Canvas#setNonStrokingColor(Color)
	 */
	public Color getRequestedNonStrokingColor() {
		return requestedNonStrokingColor;
	}

	/**
	 * Getter for field {@link Canvas#actualNonStrokingColor}. 
	 * @see Canvas#setNonStrokingColor(Color)
	 */
	public Color getActualNonStrokingColor() {
		return actualNonStrokingColor;
	}

	/**
	 * Ensures the stroking color of the {@link PDPageContentStream} is set to the appropriate colour. 
	 * When the app-layer is about to draw using the non-stroking colour, it should call this method to ensure the stream's colour is set correctly. 
	 * 
	 * @param color The colour to set the stream to. If this is null, the last colour requested is used (see {@link #restoreNonStrokingColor(Color)}). 
	 * @return The old color - useful for restoring the color once the caller has finished drawing whatever it had to draw. 
	 * @see #restoreNonStrokingColor(Color)
	 */
	public Color setNonStrokingColor(Color color) throws IOException { 
		Color oldColor = this.requestedNonStrokingColor ; 
		if (color!=null) this.requestedNonStrokingColor = color ; 
		if (requestedNonStrokingColor!=actualNonStrokingColor) { 
			Color newColor = requestedNonStrokingColor != null ? requestedNonStrokingColor : Color.BLACK ; 
			stream.setNonStrokingColor(newColor); 
			this.actualNonStrokingColor = requestedNonStrokingColor ; 
		}
		return oldColor ; 
	}
	
	/**
	 * Sets the canvas's requested colour for drawing. 
	 * But, does not modify the {@link PDPageContentStream}. 
	 * @param oldColor Usually, the return-value of a prior call to {@link #setNonStrokingColor(Color)}. 
	 */
	public void restoreNonStrokingColor(Color oldColor) { 
		this.requestedNonStrokingColor = oldColor ; 
	}

}
