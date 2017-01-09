package org.blockframe.examples;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

import org.blockframe.blocks.FrameReading;
import org.blockframe.blocks.FrameVertical;
import org.blockframe.blocks.StringBlock;
import org.blockframe.core.Block;
import org.blockframe.core.DebugLog;
import org.blockframe.core.Frame;
import org.blockframe.core.PdfDocument;
import org.blockframe.core.BlockPipe.BlockWriter;


/**
 * Frames can be nested. 
 * For example, a page's content is held in a {@link FrameVertical}, but that may contain a frame {@link FrameReading} which displays its content in a reading layout. 
 */
public class D_Reading extends PdfDocument {

	public static void main(String[] args) throws IOException { 
		File file = new File(UtilsForExamples.getExamplesDir(), "D_Reading.pdf"); 
		new D_Reading(); 
	}
	
	public D_Reading() throws IOException {
		
		/*
		 * Create a frame which lays out its children in reading layout - left-to-right top-to-bottom. 
		 * This does not replace the FrameVertical. PdfChapter will still create a FrameVertical to hold the content of the page. 
		 * 
		 * Every frame has a pipeline. 
		 * Because we don't pass in a pipeline to this reading-frame, it will create one of its own. 
		 * It is a separate pipeline to the one writing into the page's FrameVertical. 
		 */
		Frame readingFrame = new FrameReading();  
		
		/*
		 * It is possible to set a logging verbosity for individual frames, as well as a global verbosity. 
		 * Generally, it is more useful to only log a few frames. 
		 */
		readingFrame.loggingVerbosity = LEAVING_6 ; 

		/*
		 * Create and write many blocks. 
		 * 
		 * They are written into the reading-frame's pipeline. 
		 * 
		 * We only set a logging verbosity on the first and last block. 
		 */
		for (int i=0 ; i<100 ; i++) { 
			Block block = new StringBlock(UtilsForExamples.number(i+1)); 
			if (i==0 || i==100-1) block.loggingVerbosity = LEAVING_6 ; 
			readingFrame.write(block); 
		}
		
		/*
		 * Write five blocks into the page's FrameVertical. 
		 * The reading-frame is a block subclass. 
		 */
		write(readingFrame); 
		write(new StringBlock("WE")); 
		write(new StringBlock("ARE")); 
		write(new StringBlock("STILL")); 
		write(new StringBlock("VERTICAL")); 
		
		File file = new File(UtilsForExamples.getExamplesDir(), getClass().getSimpleName()+".pdf"); 
		writeFile(file); 
		DebugLog.out(); 
	}

}
