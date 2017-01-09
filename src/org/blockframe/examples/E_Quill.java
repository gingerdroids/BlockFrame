package org.blockframe.examples;

import java.awt.Color;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

import org.blockframe.blocks.FrameHorizontal;
import org.blockframe.blocks.FrameReading;
import org.blockframe.blocks.StringBlock;
import org.blockframe.core.Block;
import org.blockframe.core.DebugLog;
import org.blockframe.core.PdfDocument;
import org.blockframe.core.Quill;
import org.blockframe.core.BlockPipe.BlockWriter;


/**
 * The font and colour of text is held in a {@link Quill} object, and set in the {@link Block#inheritQuill} method. 
 */
public class E_Quill extends PdfDocument { 
	
	public static void main(String[] args) throws IOException { 
		new E_Quill(); 
	}
	
	public E_Quill() throws IOException { 

		/*
		 * BlockFrame uses subclassing and overriding methods to control formating and other properties. 
		 * 
		 * The fill method passes a Quill object into every block. 
		 * This quill controls the font and colour properties of the content of everything in that block (or frame, and recursively, the frame's children). 
		 * 
		 * To give a block different font or colour properties to its container, you should override the 'inheritQuill' method, and pass in a copied-and-modified Quill instance. 
		 * 
		 * The Quill class has many convenience methods to create a copy of an instance and edit specific fields. 
		 */
		
		write(new LineFrame()); 
		write(new BlueLineFrame()); 
		write(new GrayLineFrame()); 

		File file = new File(UtilsForExamples.getExamplesDir(), getClass().getSimpleName()+".pdf"); 
		writeFile(file); 
		DebugLog.out(); 
	}
	
	/**
	 * Writes a single line of text, consisting of <em>Hello</em> in various fonts and colours. 
	 */
	class LineFrame extends FrameReading { 
		LineFrame() { 
			write(new HelloBlock()); 
			write(new ItalicHelloBlock()); 
			write(new LargeHelloBlock()); 
			write(new RedHelloBlock()); 
			write(new HelveticaHelloBlock()); 
		}
	}
	
	/**
	 * Extends the class {@link StringBlock}, to write the specific text <em>Hello</em>. 
	 */
	class HelloBlock extends StringBlock { 
		HelloBlock() { 
			super("Hello"); 
		}
	}
	
	class ItalicHelloBlock extends HelloBlock {
		@Override
		protected Quill inheritQuill(Quill receivedQuill) {
			return super.inheritQuill(receivedQuill.copyItalic());
		}
	}
	
	class LargeHelloBlock extends HelloBlock {
		@Override
		protected Quill inheritQuill(Quill receivedQuill) {
			return super.inheritQuill(receivedQuill.copySize(18));
		}
	}
	
	class RedHelloBlock extends HelloBlock {
		@Override
		protected Quill inheritQuill(Quill receivedQuill) {
			return super.inheritQuill(receivedQuill.copy(Color.RED));
		}
	}
	
	class HelveticaHelloBlock extends HelloBlock { 
		@Override
		protected Quill inheritQuill(Quill receivedQuill) {
			return super.inheritQuill(receivedQuill.copy(Quill.HELVETICA, null, null));
		}
	}
	
	/**
	 * Writes a single line of text, using the overridden class {@link LineFrame}, but with default font colour blue. 
	 */
	class BlueLineFrame extends LineFrame { 
		@Override
		protected Quill inheritQuill(Quill receivedQuill) {
			return super.inheritQuill(receivedQuill.copy(Color.BLUE));
		}
	}

	/**
	 * Writes a single line of text, using the overridden class {@link LineFrame}, but with default font colour gray. 
	 */
	class GrayLineFrame extends LineFrame { 
		@Override
		protected Quill inheritQuill(Quill receivedQuill) {
			return super.inheritQuill(receivedQuill.copy(Color.GRAY));
		}
	}

}

