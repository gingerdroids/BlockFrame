package org.blockframe.core;

import java.io.IOException;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.blockframe.blocks.FrameVertical;
import org.blockframe.core.Block.BlockId;
import org.blockframe.core.Block.PlacedBlock;



/**
 * Top level class for working on a page supplied by the caller. 
 * This is intended for PDF documents created substantially outside BlockFrame, but where BlockFrame is used to write some parts of the document. 
 * <p>
 * <b>WARNING December 2016: This code has not been tested.</b> 
 */
public class PdfOverlay { 
	
	protected final PDDocument document ; 
	protected final PDPage page ;
	private Layout templateSpec; 
	
	private BlockPipe pipe ; 
	
	public final BlockId id = new BlockId(); 
	
	public PdfOverlay(PDDocument document, PDPage page) { 
		this.document = document ; 
		this.page = page ; 
		PDRectangle mediaBox = page.getMediaBox(); 
		this.templateSpec = new Layout(mediaBox.getWidth(), mediaBox.getHeight()); 
		this.pipe = new BlockPipe("overlay"); 
	}
	
	public void build() throws IOException { 		
		Layout pageSpec = templateSpec.copyAndSetSize(page); 
		Frame pageFrame = new FrameVertical(pipe); // TODO Code method to supply a list of rectangles (which may be over-ridden). Return a frame which fills the rectangles in turn. 
		Canvas canvas = new Canvas(document, page); 
		Quill quill = new Quill(); 
		PlacedBlock placedBlock = pageFrame.fill(quill, pageSpec); 
		pageFrame.draw(canvas, placedBlock.getLeftInContainer(), placedBlock.getTopInContainer(), placedBlock.getWidth(), placedBlock.getHeight()); 
		canvas.close(); 
	}

}
