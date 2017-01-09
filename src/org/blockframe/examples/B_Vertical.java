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
 * The default layout of the top-level content blocks on a page is vertical. 
 */
public class B_Vertical extends PdfDocument { 
	
	public static void main(String[] args) throws IOException { 
		new B_Vertical(); 
	}
	
	public B_Vertical() throws IOException { 
		DebugLog.loggingVerbosity = LEAVING_6 ; 
		
		/*
		 * Write five blocks to the pipeline of blocks to be displayed. 
		 * 
		 * Every page has a FrameVertical as its top-level container. 
		 * 
		 * The page's FrameVertical will measure these blocks and position them vertically. 
		 */
		write(new StringBlock("one")); 
		write(new StringBlock("two")); 
		write(new StringBlock("three")); 
		write(new StringBlock("four")); 
		write(new StringBlock("five")); 
		
		File file = new File(UtilsForExamples.getExamplesDir(), getClass().getSimpleName()+".pdf"); 
		writeFile(file); 
		DebugLog.out(); 
	}

}
