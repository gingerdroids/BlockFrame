package org.blockframe.examples;

import java.io.File;
import java.io.IOException;
import java.util.Date;

import org.blockframe.blocks.FrameReading;
import org.blockframe.blocks.FrameVertical;
import org.blockframe.blocks.SpacerHeight;
import org.blockframe.blocks.StringBlock;
import org.blockframe.blocks.TableBlock;
import org.blockframe.core.Block;
import org.blockframe.core.BlockPipe;
import org.blockframe.core.Canvas;
import org.blockframe.core.DebugLog;
import org.blockframe.core.Frame;
import org.blockframe.core.Layout;
import org.blockframe.core.PdfChapter;
import org.blockframe.core.PdfDocument;
import org.blockframe.core.Quill;
import org.blockframe.core.BlockPipe.BlockWriter;
import org.blockframe.painters.BezierCirclePainter;
import org.blockframe.painters.PathPainter;


/**
 * Demonstrates the use of a {@link PathPainter} subclass, and the Bezier-style blobs with various parameters. 
 */
public class I_BezierCircles extends PdfDocument { 

	public static void main(String[] args) throws IOException { 
		new I_BezierCircles(); 
	}
	
	protected I_BezierCircles() throws IOException {
		
		/*
		 * The class 'BezierCirclePainter' knows how to draw blobs using Bezier cubic splines. 
		 * With appropriate parameters, these blobs can look very like a circle. Or not like a circle at all. 
		 */
		write(new SimpleBezierBlock(0.0f)); 
		write(new SimpleBezierBlock(0.1f)); 
		write(new SimpleBezierBlock(0.2f)); 
		write(new SimpleBezierBlock(0.3f)); 
		write(new SimpleBezierBlock(0.4f)); 
		write(new SimpleBezierBlock(0.5f)); 
		write(new SimpleBezierBlock(0.6f)); 
		write(new SimpleBezierBlock(0.7f)); 
		write(new SimpleBezierBlock(0.8f)); 
		write(new SimpleBezierBlock(0.9f)); 
		write(new SimpleBezierBlock(1.0f)); 
		write(new SimpleBezierBlock(-0.1f)); 
		write(new SimpleBezierBlock(-0.3f)); 
		write(new SimpleBezierBlock(-0.5f)); 
		write(new SimpleBezierBlock(1.1f)); 
		write(new SimpleBezierBlock(1.5f)); 
		write(new UnevenBezierBlock(0.0f, 1.0f)); 
		write(new UnevenBezierBlock(1.0f, 0.0f)); 
		write(new UnevenBezierBlock(-0.5f, 1.0f)); 
		write(new UnevenBezierBlock(0.0f, 1.5f)); 


		File file = new File(UtilsForExamples.getExamplesDir(), getClass().getSimpleName()+".pdf"); 
		writeFile(file); 
		DebugLog.out(); 
	}
	
	/**
	 * Overrides the {@link PdfChapter} method which creates the top-level frame on a page. 
	 * We will use a {@link FrameReading} subclass, configured to lay out the (identically-sized) bezier blocks in a grid. 
	 */
	@Override
	public Frame newPageFrame(BlockPipe pipe, Page prevPage) { 
		return new BezierPageFrame(pipe); 
	}
	
	class BezierPageFrame extends FrameReading { 
		BezierPageFrame(BlockPipe pipe) { 
			super(pipe); 
			setHorizontalGap("WWWW"); 
		}
		@Override
		protected Layout inheritLayout(Layout receivedLayout) {
			return super.inheritLayout(receivedLayout).copyLeft(); // With uniform-sized bezier-blocks, this looks like a grid. 
		}
	}
	
	/**
	 * Draws and labels a Bezier "circle" using identical horizontal and vertical factors. 
	 */
	class SimpleBezierBlock extends FrameVertical { 
		SimpleBezierBlock(float bezierFactor) { 
			write(new BezierBlock(bezierFactor, bezierFactor)); 
			write(new SpacerHeight(true, 0.2)); 
			write(new StringBlock("Factors "+bezierFactor)); 
			write(new SpacerHeight(true, 2.0)); 
		}
		@Override
		protected Layout inheritLayout(Layout receivedLayout) {
			return super.inheritLayout(receivedLayout).copyCentreH().copyTight(true, null).copyAllowSplitting(false);
		}
	}

	/**
	 * Draws and labels a Bezier "circle" using with differing horizontal and vertical factors. 
	 */
	class UnevenBezierBlock extends FrameVertical { 
		UnevenBezierBlock(float horizontalBezierFactor, float verticalBezierFactor) { 
			write(new BezierBlock(horizontalBezierFactor, verticalBezierFactor)); 
			write(new SpacerHeight(true, 0.2)); 
			write(new StringBlock("H "+horizontalBezierFactor+",  V "+verticalBezierFactor)); 
			write(new SpacerHeight(true, 2.0)); 
		}
		@Override
		protected Layout inheritLayout(Layout receivedLayout) {
			return super.inheritLayout(receivedLayout).copyCentreH().copyTight(true, null).copyAllowSplitting(false);
		}
	}
	
	/**
	 * Draws a blob using the {@link BezierCirclePainter} class. 
	 */
	class BezierBlock extends Block { 
		
		final float horizontalBezierFactor ; 
		
		final float verticalBezierFactor ; 
		
		BezierBlock(float horizontalBezierFactor, float verticalBezierFactor) { 
			this.horizontalBezierFactor = horizontalBezierFactor ; 
			this.verticalBezierFactor = verticalBezierFactor ; 
		}
		
		float pdfDiameter = 72.0f ; 

		@Override
		public PlacedBlock fill(Quill quill, Layout receivedLayout) throws IOException {
			return new PlacedBlock().setDimensions(pdfDiameter, pdfDiameter); 
		}

		@Override
		public void draw(Canvas canvas, double left, double top, double width, double height) throws IOException {
			BezierCirclePainter pathPainter = new BezierCirclePainter(canvas); 
			float pdfLeft = (float) left ;
			float pdfBottom = canvas.getPdfBottom(top, height);
			pathPainter.traceBezierCircle(pdfLeft, pdfBottom, pdfDiameter, pdfDiameter, horizontalBezierFactor, verticalBezierFactor); 
			pathPainter.fillPath(); 
		}
		
	}

}
