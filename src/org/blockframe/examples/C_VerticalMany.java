package org.blockframe.examples;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

import org.blockframe.blocks.StringBlock;
import org.blockframe.core.Block;
import org.blockframe.core.DebugLog;
import org.blockframe.core.Frame;
import org.blockframe.core.PdfDocument;
import org.blockframe.core.BlockPipe.BlockWriter;


/**
 * Once a page is full, content automatically flows to a new page. 
 */
public class C_VerticalMany extends PdfDocument { 
	
	public static void main(String[] args) throws IOException { 
		new C_VerticalMany(); 
	}
	
	public C_VerticalMany() throws IOException { 
		
		/*
		 * Use less verbose logging than before - only show entering the major methods, but not leaving them. 
		 */
		DebugLog.loggingVerbosity = ENTERING_5 ; 
		
		/*
		 * Write too many blocks to fit vertically on one page. 
		 * 
		 * The FrameVertical created for the first page will measure, position and draw as many blocks as will fit on the page. 
		 * It also measures one more, which it realizes it can't place. 
		 * 
		 * The makePages method in PdfChapter will create a second FrameVertical, and hook the pipeline of blocks into that frame. 
		 */
		for (int i=0 ; i<100 ; i++) { 
			Block block = new StringBlock(UtilsForExamples.number(i+1)); 
			write(block); 
		}
		
		File file = new File(UtilsForExamples.getExamplesDir(), getClass().getSimpleName()+".pdf"); 
		writeFile(file); 
		DebugLog.out(); 
	}

}
