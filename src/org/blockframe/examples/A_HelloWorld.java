package org.blockframe.examples;

import java.io.File;
import java.io.IOException;

import org.blockframe.blocks.StringBlock;
import org.blockframe.core.DebugLog;
import org.blockframe.core.PdfDocument;


/**
 * Traditional first example - writing "Hello World". 
 * <p>
 * It will be written in <code>HOME_DIR/BlockFrame examples/A_HelloWorld.pdf</code>. 
 */
public class A_HelloWorld extends PdfDocument { 
	
	public static void main(String[] args) throws IOException { 
		new A_HelloWorld(); 
	}
	
	public A_HelloWorld() throws IOException {
		
		/*
		 * Set the debug logging to a medium level. 
		 * This will show the major steps of the framework on the console. 
		 */
		DebugLog.loggingVerbosity = LEAVING_6 ; 
		
		/*
		 * Create a {@link Block} for the text "Hello World". 
		 */
		StringBlock helloWorldBlock = new StringBlock("Hello World");
		
		/*
		 * Write this block into the pipeline of blocks to be displayed. 
		 */
		write(helloWorldBlock); 
		
		/*
		 * Measure the size of all the blocks we've written, lay them out on pages, and write it all to a PDF file. 
		 */
		File file = new File(UtilsForExamples.getExamplesDir(), getClass().getSimpleName()+".pdf"); 
		writeFile(file); 
		
		/*
		 * Flush the logging messages onto the console. 
		 */
		DebugLog.out(); 
		
		/*
		 * The log shows two major objects: a FrameVertical and a StringBlock. 
		 * 
		 * A FrameVertical is a container, which can hold many blocks. Here, it holds one block. 
		 * 
		 * For each page, the work is done in two passes: a 'fill' pass and a 'draw' pass. 
		 * 
		 * The fill pass traverses all the frames and blocks, measures their size, and positions them. 
		 * 
		 * The draw pass also traverses the entire frame-and-block tree on the page. 
		 * It draws each block in the appropriate position, using the tools in the <code>painters</code> package. 
		 */
	}

}
