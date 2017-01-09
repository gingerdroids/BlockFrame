package com.blockframe.core;

import java.io.IOException;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.common.PDRectangle;

import com.blockframe.blocks.FrameVertical;
import com.blockframe.core.Block.PlacedBlock;
import com.blockframe.core.DebugLog.StringGetter;
import com.blockframe.core.DebugLog.Verbosity;
import com.blockframe.examples.H_PageFrame;

/**
 * Top level class for adding pages to a PDF document. 
 * This class manages receiving content from the client, laying the content out over one or more pages, drawing the content and adding the pages to the supplied PDF-Box document. 
 * <p>
 * Clients who want to generate their document entirely within BlockFrame will probably code a subclass of {@linkplain PdfChapter} or its subclass {@link PdfDocument}. 
 */
public class PdfChapter implements Verbosity {
	
	/**
	 * The PDF-Box document we are writing into. 
	 */
	protected final PDDocument pdDocument ; 
	
	private double leftMargin = 36 ; // Default units are 1/72in, that is, 1pt. 
	private double topMargin = 36 ; 
	private double rightMargin = 36 ; 
	private double bottomMargin = 36 ; 
	
	protected final BlockPipe pipe ; 
	
	// TODO Perhaps... Keep pages separate rather than adding into the document in makePages. Code methods fillPages() and incorporatePages() to replace makePages().  
	
	private Page currentPage ;

	private int pageCount = 0 ; 
	
	/**
	 * Maximum number of pages that can be generated. 
	 * This is configurable. It's mainly intended to catch infinite loops. 
	 */
	private Integer maxPageCount = 1000 ; 
	
	/**
	 * Constructor. 
	 * @param pdDocument The PDF-Box document we are writing to. 
	 */
	protected PdfChapter(PDDocument pdDocument) { 
		this.pdDocument = pdDocument ; 
		this.pipe = new BlockPipe("document"); 
	}

	/**
	 * Getter method for field {@link #leftMargin}. 
	 * Use {@link #setMargins(Double, Double, Double, Double)} to modify the fields. 
	 */
	public double getLeftMargin() {
		return leftMargin;
	}

	/**
	 * Getter method for field {@link #topMargin}. 
	 * Use {@link #setMargins(Double, Double, Double, Double)} to modify the fields. 
	 */
	public double getTopMargin() {
		return topMargin;
	}

	/**
	 * Getter method for field {@link #rightMargin}. 
	 * Use {@link #setMargins(Double, Double, Double, Double)} to modify the fields. 
	 */
	public double getRightMargin() {
		return rightMargin;
	}

	/**
	 * Getter method for field {@link #bottomMargin}. 
	 * Use {@link #setMargins(Double, Double, Double, Double)} to modify the fields. 
	 */
	public double getBottomMargin() {
		return bottomMargin;
	}

	/**
	 * Sets the four margin fields. 
	 * Any <code>null</code> argument is ignored. 
	 * <p>
	 * The default units are 1/72in, that is, 1pt. 
	 */
	public void setMargins(Double leftMargin, Double topMargin, Double rightMargin, Double bottomMargin) { 
		if (leftMargin!=null) this.leftMargin = leftMargin ; 
		if (topMargin!=null) this.topMargin = topMargin ; 
		if (rightMargin!=null) this.rightMargin = rightMargin ; 
		if (bottomMargin!=null) this.bottomMargin = bottomMargin ; 
	}
	
	/**
	 * Manages the creation, filling and drawing of pages to absorb all the blocks written into this {@link PdfChapter} instance. 
	 * <p>
	 * Although this method is <code>final</code>, most of its functionality calls on overridable methods. 
	 */
	public final void makePages() throws IOException { 
		pipe.writer.close(); 
		while (pipe.reader.hasMore()) { 
			PDPage pdPage = new PDPage(); 
			pdDocument.addPage(pdPage); 
			Page prevPage = currentPage;
			this.currentPage = null ; 
			Page newPage = newPage(pipe, pdPage, prevPage); 
			this.currentPage = newPage ; 
			Canvas canvas = new Canvas(pdDocument, pdPage); 
			PlacedBlock placedPageBlock = fillPageFrame(newPage); 
			placedPageBlock.setOffsetInContainer(leftMargin, topMargin); 
			drawPageFrame(canvas, placedPageBlock.getLeftInContainer(), placedPageBlock.getTopInContainer(), placedPageBlock); 
			canvas.close(); 
			if (maxPageCount!=null && pageCount>maxPageCount) throw new RuntimeException("Have exceeded maximum page count of "+maxPageCount); 
		}
	}
	
	/**
	 * Adds a {@linkplain Block} to the list of top-level blocks in this document. 
	 * Often, each block will hold a paragraph of text. 
	 */
	public void write(Block block) { 
		pipe.writer.write(block); 
	}

	/**
	 * Returns the {@link Page} that we are currently writing to. 
	 * This exposes the top-level frame, and the {@link Layout} and {@link Quill} passed into the top-level frame. 
	 */
	public final Page getCurrentPage() {
		return currentPage;
	}
	
	/**
	 * Getter for {@link #pageCount}. 
	 * The count is zero on initialization, and is incremented in {@link #newPage(BlockPipe, PDPage, Page)} after a new {@link Page} is created, 
	 * but before {@linkplain #newPage(BlockPipe, PDPage, Page)} returns. 
	 */
	public int getPageCount() { 
		return pageCount ; 
	}
	
	/**
	 * Increments field {@link #pageCount}. 
	 * On the first page, this will have the value <code>1</code>. 
	 * That is, it is a count of the number of pages created by {@link #newPage(BlockPipe, PDPage, Page)}, including the current page. 
	 */
	public final int incrementPageCount() { 
		this.pageCount ++ ; 
		return pageCount ; 
	}
	
	/**
	 * Setter for field {@link #maxPageCount}. 
	 */
	public PdfChapter setMaxPageCount(Integer maxPageCount) { 
		this.maxPageCount = maxPageCount ; 
		return this ; 
	}
	
	/**
	 * Exposes the top-level BlockFrame objects for the current page. 
	 * <p>
	 * The PDF-Box {@link PDDocument} is exposed via the field {@link PdfChapter#pdDocument}. 
	 * Other PDF-Box objects are exposed in the {@link Canvas} object passed down the {@link Block#draw(Canvas, double, double, double, double)} pass. 
	 */
	public static class Page { 
		public final Frame frame ; 
		public final Layout layout ; 
		public final Quill quill ; 
		public Page(Frame frame, Layout layout, Quill quill) { 
			this.frame = frame ; 
			this.layout = layout ; 
			this.quill = quill ; 
		}
	}

	/**
	 * This is the method called by {@link #makePages()} to generate a new page. 
	 * This implementation delegates building the fields of {@link Page} to the <code>newPageXxxx</code> methods. 
	 * <p>
	 * This method, and the <code>newPageXxxx</code> methods, are intended to be overridable if you wish to configure the top-level objects of a page. 
	 * For example, see the {@link H_PageFrame#newPageFrame(BlockPipe, Page)} method. 
	 * <p>
	 * The method {@link #incrementPageCount()} is called after the <code>newPageXxxx</code> methods and the <code>Page</code> constructor. 
	 */
	public Page newPage(BlockPipe pipe, PDPage pdPage, Page prevPage) { 
		Frame frame = newPageFrame(pipe, prevPage); 
		if (frame.pipe!=pipe) throw new RuntimeException("Top level frame must use BlockPipe from newPageFrame() arguments. Have you called 'super(pipe)'?"); 
		Layout layout = newPageLayout(pdPage, prevPage); 
		Quill quill = newPageQuill(prevPage);
		Page page = new Page(frame, layout, quill); 
		incrementPageCount(); 
		DebugLog.add(DETAIL_8, frame, null, logMessage_newPage, page, null, false); 
		return page; 
	}

	private StringGetter logMessage_newPage = new StringGetter() {
		public String getString(Block block, PlacedBlock placedBlock, Object pageObject, Object arg1) { 
			Page page = (Page) pageObject ; 
			return "Created new page "+page.getClass().getSimpleName()+", page count now "+pageCount+", frame is "+page.frame.getLogName();
		}
	}; 

	/**
	 * Creates the {@link Frame} object for a new page. 
	 */
	public Frame newPageFrame(BlockPipe pipe, Page prevPage) {
		return new FrameVertical(pipe);
	}
	
	/**
	 * Creates the {@link Layout} object for a new page. 
	 */
	public Layout newPageLayout(PDPage pdPage, Page prevPage) { 
		PDRectangle mediaBox = pdPage.getMediaBox(); 
		Layout layout = new Layout(mediaBox.getWidth()-(leftMargin+rightMargin), mediaBox.getHeight()-(topMargin+bottomMargin)); 
		return layout ; 
	}
	
	/**
	 * Creates the {@link Quill} object for a new page. 
	 */
	public Quill newPageQuill(Page prevPage) { 
		return new Quill(); 
	}
	
	/**
	 * Invokes the <code>fill</code> pass on the current page's {@link Frame}. 
	 * <p>
	 * This method is called by {@link #makePages()}. 
	 */
	protected PlacedBlock fillPageFrame(Page page) throws IOException { 
		PlacedBlock placedBlock = page.frame.fill(page.quill, page.layout); 
		return placedBlock ; 
	}
	
	/**
	 * Invokes the <code>draw</code> pass on the current page's {@link Frame}. 
	 * <p>
	 * This method is called by {@link #makePages()}. 
	 */
	protected void drawPageFrame(Canvas canvas, double left, double top, PlacedBlock placedPageFrame) throws IOException { 
		placedPageFrame.draw(canvas, left, top); 
	}

}
