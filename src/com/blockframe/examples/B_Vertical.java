package com.blockframe.examples;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

import com.blockframe.blocks.StringBlock;
import com.blockframe.core.Block;
import com.blockframe.core.DebugLog;
import com.blockframe.core.Frame;
import com.blockframe.core.PdfDocument;
import com.blockframe.core.BlockPipe.BlockWriter;

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
