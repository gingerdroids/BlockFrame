package com.blockframe.examples;

import java.awt.Color;
import java.io.File;
import java.io.IOException;

import com.blockframe.blocks.FrameReading;
import com.blockframe.blocks.FrameVertical;
import com.blockframe.blocks.SpacerWidth;
import com.blockframe.blocks.StringBlock;
import com.blockframe.core.Block;
import com.blockframe.core.BlockPipe;
import com.blockframe.core.Canvas;
import com.blockframe.core.DebugLog;
import com.blockframe.core.Frame;
import com.blockframe.core.Layout;
import com.blockframe.core.PdfChapter;
import com.blockframe.core.PdfDocument;
import com.blockframe.core.Quill;

/**
 * We can use a custom {@link Frame} subclass as the top-level frame on a page. 
 * This class creates a frame which divides its content into columns. 
 */
public class H_PageFrame extends PdfDocument { 

	public static void main(String[] args) throws IOException { 
		new H_PageFrame(); 
	}
	
	protected H_PageFrame() throws IOException {
		
		/*
		 * Write several junk paragraphs, which will more than fill a page. 
		 */
		for (int i=0 ; i<8 ; i++) { 
			write(new ParagraphFrame()); 
		}

		File file = new File(UtilsForExamples.getExamplesDir(), getClass().getSimpleName()+".pdf"); 
		writeFile(file); 
		DebugLog.out(); 
	}
	
	/**
	 * Customizes the frame holding the top-level content of each page. 
	 */
	@Override
	public Frame newPageFrame(BlockPipe pipe, Page prevPage) { 
		return new TwoColumnFrame(pipe); // Don't forget to pass 'pipe' to the constructor. 
	}
	
	/**
	 * The default {@link Quill} is modified to use colour {@link Color#BLUE}. 
	 */
	@Override
	public Quill newPageQuill(Page prevPage) {
		return super.newPageQuill(prevPage).copy(Color.BLUE);
	}
	
	private class ParagraphFrame extends FrameReading { 
		ParagraphFrame() { 
			write(new SpacerWidth("W")); // Indent at start of paragraph
			for (int i=0 ; i<100 ; i++) { 
				Block block = new StringBlock(UtilsForExamples.number(i+1)); 
				write(block); 
			}
		}
	}
	
	/**
	 * The custom {@link Frame} subclass to hold the top-level content of a page. 
	 * We configure this {@link PdfDocument} to use this class by overriding the {@link PdfChapter#newPageFrame(BlockPipe, Page)} method. 
	 */
	static class TwoColumnFrame extends Frame { 
		
		final Frame leftFrame ; 
		
		final Frame rightFrame ; 
		
		PlacedBlock leftPlacedFrame ; 
		
		PlacedBlock rightPlacedFrame ; 
		
		/**
		 * Coordinate of the left-edge of the second column. 
		 */
		double rightFrameLeftEdge ; 
		
		TwoColumnFrame(BlockPipe pipe) { 
			super(pipe); 
			this.leftFrame = new FrameVertical(pipe); 
			this.rightFrame = new FrameVertical(pipe); 
		}

		@Override
		public PlacedBlock fill(Quill receivedQuill, Layout receivedLayout) throws IOException { 
			DebugLog.add(ENTERING_5, this, null, logMessage_enteringFill, null, null, true); 
			this.quill = inheritQuill(receivedQuill) ; 
			Layout outerLayout = inheritLayout(receivedLayout); 
			DebugLog.add(DETAIL_8, this, null, pipe.logMessage_pipe, null, null, false); 
			DebugLog.add(DETAIL_8, this, null, Layout.logMessage_layout, outerLayout, null, false); 
			//////  Compute inner layout
			double outerMaxWidth = outerLayout.maxWidth ; 
			double outerMaxHeight = outerLayout.maxHeight ; 
			double gapWidth = quill.getStringWidth("WWWWWWWW"); 
			double innerMaxWidth = (outerMaxWidth-gapWidth) / 2 ; 
			Layout innerLayout = outerLayout.copy().setSize(innerMaxWidth, null); 
			this.rightFrameLeftEdge = innerMaxWidth + gapWidth ; 
			//////  Fill the two columns 
			this.leftPlacedFrame = leftFrame.fill(quill, innerLayout); 
			this.rightPlacedFrame = rightFrame.fill(quill, innerLayout); 
			//////  Set my dimensions
			PlacedBlock placedBlock = new PlacedBlock().setDimensions(outerMaxWidth, outerMaxHeight); // Does not honour Layout's "tight" property. 
			DebugLog.add(LEAVING_6, placedBlock, null, logMessage_leavingFill, null, null, false); 
			//////  Bye bye
			return placedBlock ; 
		}
		
		@Override
		public void draw(Canvas canvas, double left, double top, double width, double height) throws IOException { 
			leftPlacedFrame.draw(canvas, left, top);
			rightPlacedFrame.draw(canvas, left+rightFrameLeftEdge, top); 
		}
		
	}

}
