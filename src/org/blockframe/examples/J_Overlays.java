package org.blockframe.examples;

import java.awt.Color;
import java.io.File;
import java.io.IOException;

import org.blockframe.blocks.FrameReading;
import org.blockframe.blocks.FrameVertical;
import org.blockframe.blocks.SpacerFullHeight;
import org.blockframe.blocks.SpacerHeight;
import org.blockframe.blocks.StringBlock;
import org.blockframe.core.Block;
import org.blockframe.core.BlockPipe;
import org.blockframe.core.Canvas;
import org.blockframe.core.DebugLog;
import org.blockframe.core.Frame;
import org.blockframe.core.Layout;
import org.blockframe.core.PdfDocument;
import org.blockframe.core.Quill;
import org.blockframe.core.Layout.Justification;
import org.blockframe.painters.Scribe;


	/**
	 * This class demonstrates how to draw overlays. 
	 * <p>
	 * In particular, sometimes it's useful to draw an overlay showing the computed bounds of a block or frame. 
	 * The Scribe methods {@link Scribe#border(Canvas, Color, double, double, double, double, Double, Double)}, {@link Scribe#diagonals(Canvas, Color, Block, double, double, Double)} 
	 * and {@link Scribe#cross(Canvas, Color, Block, double, double, Double)} are intended for this. 
	 */
public class J_Overlays extends PdfDocument { 

	public static void main(String[] args) throws IOException { 
		new J_Overlays(); 
	}
	
	protected J_Overlays() throws IOException { 
		final double parSep = 1.0;
		final int shortLine = 3 ; 
		final int multiLine = 10 ; 
				
		/*
		 * Very wide page margins, so very narrow area for text, which works well for what we're trying to show in this example. 
		 */
		setMargins(144.0, null, 144.0, null); 
		
		/*
		 * Show four one-line paragraphs, with varying horizontal justification. 
		 * In each, the reading frame is configured to take the full width available to it.  
		 */
		write(new ParagraphFrame(shortLine, false, Layout.LEFT)); 
		write(new SpacerHeight(true, parSep)); 
		write(new ParagraphFrame(shortLine, false, Layout.CENTRE_H)); 
		write(new SpacerHeight(true, parSep)); 
		write(new ParagraphFrame(shortLine, false, Layout.RIGHT)); 
		write(new SpacerHeight(true, parSep)); 
		write(new ParagraphFrame(shortLine, false, Layout.FULL)); // Last line of full-justification is left-justified. 
		
		/*
		 * Small gap on page. 
		 */
		write(new SpacerHeight(true, parSep*2)); 

		/*
		 * Show four one-line paragraphs, with varying horizontal justification. 
		 * In each, the reading frame is configured to be tight around the text. 
		 */
		write(new ParagraphFrame(shortLine, true, Layout.LEFT)); 
		write(new SpacerHeight(true, parSep)); 
		write(new ParagraphFrame(shortLine, true, Layout.CENTRE_H)); 
		write(new SpacerHeight(true, parSep)); 
		write(new ParagraphFrame(shortLine, true, Layout.RIGHT)); 
		write(new SpacerHeight(true, parSep)); 
		write(new ParagraphFrame(shortLine, true, Layout.FULL)); 
		
		/*
		 * Space filling the rest of the page. Effectively, requesting a new page. 
		 */
		write(new SpacerFullHeight()); 
		

		/*
		 * Show four multi-line paragraphs, with varying horizontal justification. 
		 * In each, the reading frame is configured to take the full width available to it.  
		 */
		write(new ParagraphFrame(multiLine, false, Layout.LEFT)); 
		write(new SpacerHeight(true, parSep)); 
		write(new ParagraphFrame(multiLine, false, Layout.CENTRE_H)); 
		write(new SpacerHeight(true, parSep)); 
		write(new ParagraphFrame(multiLine, false, Layout.RIGHT)); 
		write(new SpacerHeight(true, parSep)); 
		write(new ParagraphFrame(multiLine, false, Layout.FULL)); 
		
		write(new SpacerFullHeight()); 
		
		/*
		 * Show four multi-line paragraphs, with varying horizontal justification. 
		 * In each, the reading frame is configured to be tight around the text. 
		 */
		write(new ParagraphFrame(multiLine, true, Layout.LEFT)); 
		write(new SpacerHeight(true, parSep)); 
		write(new ParagraphFrame(multiLine, true, Layout.CENTRE_H)); 
		write(new SpacerHeight(true, parSep)); 
		write(new ParagraphFrame(multiLine, true, Layout.RIGHT)); 
		write(new SpacerHeight(true, parSep)); 
		write(new ParagraphFrame(multiLine, true, Layout.FULL)); 
		
		/*
		 * Note that the text sits at the bottom of the StringBlock, with substantial space above it. 
		 * The descenders on the text go below the boundary of the StringBlock. (See the word "eight".)
		 */

		File file = new File(UtilsForExamples.getExamplesDir(), getClass().getSimpleName()+".pdf"); 
		writeFile(file); 
		DebugLog.out(); 
	}
	
	/**
	 * The top level container on each page will be a {@link J_Overlays.BorderedFrame}, which draws a red border around the edge of its content. 
	 */
	@Override
	public Frame newPageFrame(BlockPipe pipe, Page prevPage) {
		return new BorderedFrame(pipe); 
	}
	
	class BorderedFrame extends FrameVertical { 
		BorderedFrame(BlockPipe pipe) { 
			super(pipe); 
		}
		@Override
		public void draw(Canvas canvas, double left, double top, double width, double height) throws IOException {
			super.draw(canvas, left, top, width, height);
			Scribe.border(canvas, Color.RED, left, top, width, height, null, null); 
		}
	}
	
	@Override
	public Quill newPageQuill(Page prevPage) {
		return super.newPageQuill(prevPage).copySize(Quill.DEFAULT_FONT_SIZE*4);
	}
	
	private class ParagraphFrame extends FrameReading { 
		final boolean isWidthTight ; // Does not directly affect layout. Just holds the value for use in inheritLayout(). 
		final Justification justification ; // Does not directly affect layout. Just holds the value for use in inheritLayout(). 
		ParagraphFrame(int count, boolean isWidthTight, Justification justification) { 
			this.isWidthTight = isWidthTight ; 
			this.justification = justification ; 
			for (int i=0 ; i<count ; i++) { 
				write(new StringBlockDiagonals(UtilsForExamples.number(i+1))); 
			}
		}
		
		@Override
		protected Layout inheritLayout(Layout receivedLayout) {
			return super.inheritLayout(receivedLayout).copy(justification).copyTight(isWidthTight, null).copyAllowSplitting(false);
		}
		
		@Override
		public void draw(Canvas canvas, double left, double top, double width, double height) throws IOException {
			super.draw(canvas, left, top, width, height); 
			Scribe.border(canvas, Color.MAGENTA, left, top, width, height, null, null); 
		}
	}
	
	class StringBlockDiagonals extends StringBlock { 
		StringBlockDiagonals(String text) { 
			super(text); 
		}
		
		@Override
		public void draw(Canvas canvas, double left, double top, double width, double height) throws IOException {
			super.draw(canvas, left, top, width, height);
			Scribe.diagonals(canvas, Color.GREEN, left, top, width, height, null); 
		}
	}

}
