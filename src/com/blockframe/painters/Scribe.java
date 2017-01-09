package com.blockframe.painters;

import java.awt.Color;
import java.io.IOException;

import org.apache.pdfbox.pdmodel.PDPageContentStream;

import com.blockframe.core.Canvas;
import com.blockframe.core.Quill;

/**
 * {@linkplain Scribe} provides methods for writing to a PDF-Box {@link PDPageContentStream}. 
 * The {@linkplain Scribe} class provides whole items - for example, text, crosses and borders - 
 * whereas the {@link PathPainter} subclasses provide tools for building more custom drawings. 
 */
public class Scribe { 
	
	/**
	 * Draws the given text, using BlockFrame coordinates. 
	 * <p>
	 * The <code>width</code> argument is ignored. 
	 */
	public static void string(Canvas canvas, Quill quill, String text, double left, double top, double width, double height) throws IOException {
		float pdfLeft = (float) left ; 
		float pdfBottom = canvas.getPdfBottom(top, height);
		string_lb(canvas, quill, pdfLeft, pdfBottom, text);
	}

	/**
	 * Draws the given text, using PDF-Box coordinates. 
	 */
	public static void string_lb(Canvas canvas, Quill quill, float pdfLeft, float pdfBottom, String text) throws IOException { 
		PDPageContentStream stream = canvas.stream; 
		final Color quillColor = quill.getColor();
		Color oldColor = canvas.setNonStrokingColor(quillColor); 
		stream.beginText();
		stream.newLineAtOffset(pdfLeft, pdfBottom); 
		stream.setFont(quill.getFont(), quill.getFontSize()); 
		stream.showText(text); 
		stream.endText();
		canvas.restoreNonStrokingColor(oldColor); 
	}
	
	/**
	 * Draws a border around the block. To be exact, just inside the block, inset by the given amount. 
	 * <p>
	 * Useful for debugging, when you want to see what the measured dimensions of a block are. 
	 * @param insetBy May be null, defaults to no inset. Useful to allow nested blocks to display their borders. 
	 * @param thickness May be null, defaults to 1. 
	 */
	public static void border(Canvas canvas, Color color, double left, double top, double width, double height, Double insetBy, Double thickness) throws IOException { 
		float pdfLeft = (float) left ; 
		float pdfWidth = (float) width ; 
		float pdfHeight = (float) height ;
		float pdfTop = canvas.getPdfY(top); 
		float pdfBottom = canvas.getPdfY(top+pdfHeight); 
		float pdfThickness = (thickness!=null) ? (float) (thickness+0) : 1.0f ; 
		float pdfInsetBy = (insetBy!=null) ? (float) (insetBy+0) : 0.0f ; 
		rect_lbwh(canvas, true, false, color, pdfLeft+pdfInsetBy, pdfBottom+pdfInsetBy, pdfThickness, pdfHeight-2*pdfInsetBy); // Left 
		rect_lbwh(canvas, true, false, color, pdfLeft+pdfWidth-pdfInsetBy-pdfThickness, pdfBottom+pdfInsetBy, pdfThickness, pdfHeight-2*pdfInsetBy); // Right 
		rect_lbwh(canvas, true, false, color, pdfLeft+pdfInsetBy, pdfTop-pdfInsetBy-pdfThickness, pdfWidth-2*pdfInsetBy, pdfThickness); // Top 
		rect_lbwh(canvas, true, false, color, pdfLeft+pdfInsetBy, pdfBottom+pdfInsetBy, pdfWidth-2*pdfInsetBy, pdfThickness); // Bottom
	}

	/**
	 * Draws a cross in the block, from the mid-points of each side to the mid-point of the opposite side. 
	 * Useful for debugging, when you want to see what the measured dimensions of a block are. 
	 * @param thickness May be null, defaults to 1. 
	 */
	public static void cross(Canvas canvas, Color color, double left, double top, double width, double height, Double thickness) throws IOException { 
		PDPageContentStream stream = canvas.stream;
		float pdfLeft = (float) left ; 
		float pdfWidth = (float) width ; 
		float pdfHeight = (float) height ;
		float pdfTop = canvas.getPdfY(top); 
		float pdfBottom = canvas.getPdfY(top+pdfHeight); 
		float midX = pdfLeft + pdfWidth * 0.5f ; 
		float midY = pdfBottom + pdfHeight * 0.5f ; 
		float pdfThickness = (thickness!=null) ? (float) (thickness+0) : 1.0f ; 
		stream.setLineWidth(pdfThickness); 
		Color oldColor = canvas.setStrokingColor(color); 
		stream.moveTo(midX, pdfBottom); 
		stream.lineTo(midX, pdfTop); 
		stream.stroke(); 
		stream.moveTo(pdfLeft, midY); 
		stream.lineTo(pdfLeft+pdfWidth, midY); 
		stream.stroke(); 
		canvas.restoreStrokingColor(oldColor); 
	}
	
	/**
	 * Draws a diagonal cross from corner-to-corner of the block. 
	 * Useful for debugging, when you want to see what the measured dimensions of a block are. 
	 * @param thickness May be null, defaults to 1. 
	 */
	public static void diagonals(Canvas canvas, Color color, double left, double top, double width, double height, Double thickness) throws IOException { 
		PDPageContentStream stream = canvas.stream;
		float pdfLeft = (float) left ; 
		float pdfWidth = (float) width ; 
		float pdfHeight = (float) height ;
		float pdfTop = canvas.getPdfY(top); 
		float pdfBottom = canvas.getPdfY(top+pdfHeight); 
		float pdfThickness = (thickness!=null) ? (float) (thickness+0) : 1.0f ; 
		stream.setLineWidth(pdfThickness); 
		Color oldColor = canvas.setStrokingColor(color); 
		stream.moveTo(pdfLeft, pdfBottom); 
		stream.lineTo(pdfLeft+pdfWidth, pdfTop); 
		stream.stroke(); 
		stream.moveTo(pdfLeft, pdfTop); 
		stream.lineTo(pdfLeft+pdfWidth, pdfBottom); 
		stream.stroke(); 
		canvas.restoreStrokingColor(oldColor); 
	}
	
	/**
	 * Draws a rectangle, using BlockFrame coordinates. 
	 */
	public static void rect(Canvas canvas, boolean wantFill, boolean wantStroke, Color color, double left, double top, double right, double bottom) throws IOException {
		float pdfLeft = (float) left ; 
		float pdfWidth = (float) (right-left); 
		float pdfHeight = (float) (bottom-top);
		float pdfTop = canvas.getPdfY(top); 
		float pdfBottom = canvas.getPdfY(top+pdfHeight); 
		rect_lbwh(canvas, wantFill, wantStroke, color, pdfLeft, pdfBottom, pdfWidth, pdfHeight); 
	}

	/**
	 * Draws a rectangle with bottom and left as given (in PDF coordinates, not BlockFrame), and width and height as given. 
	 * <p>
	 * NOTE: In the calling code, remember to convert the <em>bottom</code> of the rectangle to PDF coordinates, not the top. 
	 */
	public static void rect_lbwh(Canvas canvas, boolean wantFill, boolean wantStroke, Color color, float pdfLeft, float pdfBottom, float pdfWidth, float pdfHeight) throws IOException { 
		PDPageContentStream stream = canvas.stream;
		stream.addRect(pdfLeft, pdfBottom, pdfWidth, pdfHeight); 
		if (wantFill) { 
			Color oldColor = canvas.setNonStrokingColor(color); 
			stream.fill(); 
			canvas.restoreNonStrokingColor(oldColor); 
		}
		if (wantStroke) { 
			Color oldColor = canvas.setStrokingColor(color); 
			stream.stroke();
			canvas.restoreStrokingColor(oldColor); 
		}
	}
	
}
