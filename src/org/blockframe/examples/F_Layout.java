package org.blockframe.examples;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

import org.blockframe.blocks.FrameReading;
import org.blockframe.blocks.SpacerHeight;
import org.blockframe.blocks.SpacerWidth;
import org.blockframe.blocks.StringBlock;
import org.blockframe.core.Block;
import org.blockframe.core.DebugLog;
import org.blockframe.core.Frame;
import org.blockframe.core.Layout;
import org.blockframe.core.PdfDocument;
import org.blockframe.core.Quill;
import org.blockframe.core.BlockPipe.BlockWriter;


/**
 * The font and colour of text is held in a {@link Quill} object, and set in the {@link Block#inheritQuill} method. 
 */
/**
 * The available width and height, and other layout information, is held in a {@link Layout} object. 
 * <p>
 * If a subclass wishes to modify the layout configuration, it will usually override the {@link Block#inheritLayout} method. 
 * The width and height are usually updated in the container-blocks, but {@link Block#inheritLayout} might modify the text alignment or justification. 
 */
public class F_Layout extends PdfDocument {

	public static void main(String[] args) throws IOException { 
		new F_Layout(); 
	}
	
	public F_Layout() throws IOException {
		
		write(new ParagraphFrame()); 
		writeGap(); 
		
		write(new LeftFrame()); 
		writeGap(); 
		
		write(new RightFrame()); 
		writeGap(); 
		
		write(new TopFrame()); 

		File file = new File(UtilsForExamples.getExamplesDir(), getClass().getSimpleName()+".pdf"); 
		writeFile(file); 
		DebugLog.out(); 
	}
	
	/**
	 * Writes a paragraph with the text fully justified (the default). 
	 */
	private static class ParagraphFrame extends FrameReading { 
		ParagraphFrame() { 
			write(new BigStringBlock()); 
			for (int i=0 ; i<40 ; i++) { 
				Block block = new StringBlock(UtilsForExamples.number(i+1)); 
				write(block); 
			}
		}
	}
	
	/**
	 * Writes some text in a larger font. 
	 */
	private static class BigStringBlock extends StringBlock { 
		BigStringBlock() { 
			super("Big Font"); 
		}
		@Override
		protected Quill inheritQuill(Quill receivedQuill) {
			return super.inheritQuill(receivedQuill).copySize(receivedQuill.getFontSize()*1.5f);
		}
	}

	/**
	 * Writes a paragraph with the text left-justified. 
	 */
	private static class LeftFrame extends ParagraphFrame { 
		@Override
		protected Layout inheritLayout(Layout receivedLayout) {
			return super.inheritLayout(receivedLayout).copyLeft();
		}
	}
	
	/**
	 * Writes a paragraph with the text right-justified. 
	 */
	private static class RightFrame extends ParagraphFrame { 
		@Override
		protected Layout inheritLayout(Layout receivedLayout) {
			return super.inheritLayout(receivedLayout).copyRight();
		}
	}

	/**
	 * Writes a paragraph with the text aligned along the top of the text, rather than the baseline of the letters. 
	 */
	private static class TopFrame extends ParagraphFrame { 
		@Override
		protected Layout inheritLayout(Layout receivedLayout) {
			return super.inheritLayout(receivedLayout).copyTop();
		}
	}

	/**
	 * Writes a vertical gap, about the height of a line of text. 
	 */
	private void writeGap() {
		write(new SpacerHeight(true, 1));
	}
	
}
