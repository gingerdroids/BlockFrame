package org.blockframe.blocks;

import java.io.IOException;

import org.apache.pdfbox.pdmodel.font.PDFont;
import org.blockframe.core.Block;
import org.blockframe.core.Canvas;
import org.blockframe.core.DebugLog;
import org.blockframe.core.Layout;
import org.blockframe.core.Quill;
import org.blockframe.painters.Scribe;


/**
 * Displays a string. 
 * It will be displayed in a rectangle (no line breaks) without left or right padding. 
 * <p>
 * PDF-Box draws the string in the bottom part of the rectangle, with the baseline sitting along the bottom of the rectangle. 
 * Descenders in the glyphs are drawn below the bottom of the rectangle. 
 * The top half of the rectangle is largely empty. 
 */
public class StringBlock extends Block implements DebugLog.Verbosity { 
	
	private final String text ; 
	
	private PDFont measuredFont ; 
	
	private double measuredFontSize = Double.MIN_VALUE ; 
	
	private double measuredWidth ; 
	
	private double measuredHeight ; 
	
	public StringBlock(String text) { 
		this.text = text ; 
	}

	@Override
	public PlacedBlock fill(Quill receivedQuill, Layout receivedLayout) throws IOException { 
		DebugLog.add(ENTERING_5, this, null, logMessage_enteringFill, null, null, false); 
		this.quill = inheritQuill(receivedQuill) ; 
		Layout layout = inheritLayout(receivedLayout); 
		DebugLog.add(DETAIL_8, this, null, Layout.logMessage_layout, layout, null, false); 
		PDFont quillFont = quill.getFont();
		float quillFontSize = quill.getFontSize() ; 
		if (quillFont!=measuredFont || quillFontSize!=measuredFontSize) { 
			this.measuredFont = quillFont ; 
			this.measuredFontSize = quillFontSize ; 
			this.measuredWidth = Quill.getStringWidth(text, quillFont, quillFontSize) ; 
			/* Prefer the font's height, to the specific string's height, so it is even along a line of text, and for separate lines of text. */
			this.measuredHeight = quillFont.getFontDescriptor().getFontBoundingBox().getHeight() / 1000 * quillFontSize ; 
		}
		PlacedBlock placedBlock = new PlacedBlock(); 
		placedBlock.setDimensions(measuredWidth, measuredHeight); 
		DebugLog.add(LEAVING_6, placedBlock, null, logMessage_leavingFill, null, null, false); 
		return placedBlock ; 
	}

	@Override
	public void draw(Canvas canvas, double left, double top, double width, double height) throws IOException { 
		Scribe.string(canvas, quill, text, left, top, width, height);
	}

	@Override
	public String getLogName() { 
		final int substrLength = 8 ; 
		String mySubstring = text.length() > substrLength ? text.substring(0, substrLength) + ".." : text ; 
		return super.getLogName() + "'" + mySubstring + "'" ;
	}

}
