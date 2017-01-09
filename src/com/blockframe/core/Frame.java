package com.blockframe.core;

import java.io.IOException;
import java.util.ArrayList;

import com.blockframe.blocks.FrameHorizontal;
import com.blockframe.blocks.FrameReading;
import com.blockframe.blocks.FrameVertical;
import com.blockframe.core.Block.PlacedBlock;
import com.blockframe.core.BlockPipe.BlockReader;
import com.blockframe.core.BlockPipe.BlockWriter;
import com.blockframe.core.BlockPipe.PipeLink;
import com.blockframe.core.DebugLog.StringGetter;
import com.blockframe.core.DebugLog.Verbosity;

/**
 * Accepts multiple child {@link Block} instances from a {@link BlockPipe}, and manages their layout and drawing. 
 * <p>
 * Overriding the {@link #fill} method: if you subclass {@link Frame} and code your own {@linkplain #fill} method, you will need to understand reverting pipes. 
 * See the <code>revertXxxx</code> methods and the calls to them in {@link FrameVertical}, {@link FrameHorizontal} and {@link FrameReading}. 
 */
public abstract class Frame extends Block implements Verbosity { 
	
	protected final BlockPipe pipe ; 
	protected final BlockWriter writer ; 
	protected final BlockReader reader ; 
	
	private double horizontalGapSize = 0.0 ; 
	private String horizontalGapTemplate = " " ; 
	private Quill horizontalGapQuill = null ; 

	public Frame(BlockPipe pipe) { 
		this.pipe = pipe ; 
		this.writer = pipe.writer ; 
		this.reader = pipe.reader ; 
	}
	
	public Frame() { 
		BlockPipe pipe = new BlockPipe(id.str); 
		this.pipe = pipe ; 
		this.writer = pipe.writer ; 
		this.reader = pipe.reader ; 
	}
	
	/**
	 * Sets the template string to use when computing the size of the horizontal gap. 
	 * <p>
	 * If set to <code>null</code>, the current value of {@link #horizontalGapSize} will remain. 
	 * @see #getHorizontalGap(Quill)
	 */
	public void setHorizontalGap(String horizontalGapTemplate) { 
		this.horizontalGapTemplate = horizontalGapTemplate ; 
		this.horizontalGapQuill = null ; 
	}

	/**
	 * Sets the size of the horizontal gap. 
	 * <p>
	 * Sets the template string to <code>null</code> (see {@link #setHorizontalGap(String)}). 
	 * @see #getHorizontalGap(Quill)
	 */
	public void setHorizontalGap(double horizontalGapSize) { 
		this.horizontalGapSize = horizontalGapSize ; 
		this.horizontalGapTemplate = null ; 
	}
	
	/**
	 * Returns the size of the gap to leave between horizontally adjacent children. 
	 * <p>
	 * If {@link #horizontalGapTemplate} is non-null, the size will be the width of that string using the given <code>quill</code>. 
	 * This will be written into {@link #horizontalGapSize}, and returned. 
	 * <p>
	 * Otherwise, the current value of {@link #horizontalGapSize} will be returned. 
	 */
	public double getHorizontalGap(Quill quill) throws IOException { 
		if (horizontalGapTemplate!=null) { 
			if (quill==null) throw new IllegalArgumentException("Argument 'quill' should not be null"); 
			if (quill!=this.horizontalGapQuill) { 
				this.horizontalGapSize = quill.getStringWidth(horizontalGapTemplate); 
				this.horizontalGapQuill = quill ; 
			}
		}
		return horizontalGapSize ; 
	}
	
	public void write(Block block) { 
		if (writer==null) throw new RuntimeException("Does not have a writer."); 
		writer.write(block); 
	}
	
	// TODO Frame and PdfChapter should have methods to access the last block written. Especially useful for debugging when the block is constructed in the write() arg-list. 
	
	/**
	 * Whether all the blocks in the {@link #pipe} have been placed. 
	 */
	@Override
	public boolean isFillComplete() { 
		return !reader.hasMore(); 
	}
	
	@Override
	public void draw(Canvas canvas, double left, double top, double width, double height) throws IOException {} 
	
	/**
	 * Extends the {@link PlacedBlock} with the functionality required for frames. 
	 */
	public class PlacedFrame extends PlacedBlock { 
		
		/**
		 * List of children that have been placed in this {@link PlacedFrame}. 
		 * Note: a single instance of {@linkplain Frame} can have multiple instances of {@linkplain PlacedFrame} to hold all its children. 
		 * (However, the usual case is only one instance, and rarely would there be more than two.)
		 */
		public final ArrayList<PlacedBlock> children = new ArrayList<PlacedBlock>(); 
		
		@Override
		public void draw(Canvas canvas, double left, double top) throws IOException { 
			DebugLog.add(ENTERING_5, this, canvas, logMessage_enteringDraw, left, top, true); 
			Frame.this.draw(canvas, left, top, getWidth(), getHeight()); 
			for (PlacedBlock child : children) { 
				logIfChildOutsideBounds(child); 
				child.draw(canvas, left+child.getLeftInContainer(), top+child.getTopInContainer()); 
			}
			DebugLog.add(LEAVING_6, this, canvas, logMessage_leavingDraw, children.size(), null, false); 
		}

		/**
		 * Reverts the {@link #reader} so all the given blocks are pushed back into the pipe. 
		 * All of the blocks which are pushed back are also reverted, to their reader's state when that child began filling. 
		 * <p>
		 * This is generally called when a frame has read a block (or several) from a reader, measured it and discovered it can't fit it in. 
		 * @param revertingChildren This should match exactly the blocks read from the reader, in the order they were read. 
		 */
		public void revertToFirstChildOfList(ArrayList<PlacedBlock> revertingChildren) {
			PlacedBlock newNextBlock = revertingChildren.get(0);  
			for (int i=revertingChildren.size()-1 ; i>=0 ; i--) { 
				/* Go backwards, coz in any given pipe, the earliest frame should be the one that takes effect. */
				revertingChildren.get(i).revertToStart(); 
			} 
			reader.revertTo(newNextBlock.getBlock());
		}
		
		@Override
		public void revertToStart() { 
			PlacedBlock firstChild = children.size()>0 ? children.get(0) : null ; 
			// TODO Log DETAIL_8 which child we are reverting to. 
			while (children.size()>0) { 
				/* Go backwards, coz in any given pipe, the earliest frame should be the one that takes effect. */
				children.remove(children.size()-1).revertToStart(); 
			}
			if (firstChild!=null) reader.revertTo(firstChild.getBlock()); 
		}
	
		/**
		 * Convenience method, acting on {@link #children}. 
		 */
		public final int size() {
			return children.size();
		}
	
		/**
		 * Convenience method, acting on {@link #children}. 
		 */
		public final void add(PlacedBlock block) {
			children.add(block); 
		}
	
		/**
		 * Convenience method, acting on {@link #children}. 
		 */
		public void addAll(ArrayList<PlacedBlock> blockList) {
			children.addAll(blockList); 
		}
		
	}
	
	@Override
	PipeLink makePipeLink() {
		return new FramePipeLink(this); 
	}
	
	/**
	 * An implementation of {@link PipeLink} that understands when a {@linkplain Frame} needs multiple calls to {@link Block#fill(Quill, Layout)} to consume all the children in its pipe. 
	 */
	static class FramePipeLink extends BlockPipe.PipeLink { 
		
		private final Frame frame ; 
		
		FramePipeLink(Frame frame) { 
			this.frame = frame ; 
		}
		
		public Block getNextBlockToFill() {
			if (frame.reader.hasMore()) { 
				return frame ; 
			} else { 
				return nextBlockWritten ; 
			}
		}
		
		Block getBlock() {  
			return frame ; 
		}
	}	
	
	/**
	 * Similar to {@link Block#logMessage_leavingFill}, except the first message-arg is an integer - how many children. 
	 */
	protected static final StringGetter logMessage_leavingFill = new StringGetter() { 
		public String getString(Block block, PlacedBlock placedBlock, Object countObject, Object ignore1) {
			Integer count = (Integer) countObject ; 
			return "fill() leaving with "+count+" children" ; 
		}
	};

	/**
	 * Similar to {@link Block#logMessage_leavingDraw}, except the first message-arg is an integer - how many children. 
	 */
	protected static final StringGetter logMessage_leavingDraw = new StringGetter() { 
		public String getString(Block block, PlacedBlock placedBlock, Object countObject, Object ignore1) {
			Integer count = (Integer) countObject ; 
			return "draw() leaving with "+count+" children" ;    
		}
	};
	
	/**
	 * Generates log message when a frame decides it can fit a child in its layout. 
	 * Information about the child's size and the remaining layout bounds is included. 
	 */
	protected static final StringGetter logMessage_childAccepted = new StringGetter() {
		public String getString(Block block, PlacedBlock placedBlock, Object childObject, Object eatenLayoutObject) {
			PlacedBlock placedChild = (PlacedBlock) childObject ; 
			Layout eatenLayout = (Layout) eatenLayoutObject ;
			String message = "Child "+placedChild.getBlock().getLogName()+" accepted, has size "+(int)placedChild.getWidth()+"x"+(int)placedChild.getHeight(); 
			if (eatenLayout!=null) message += ", layout remaining size "+(int)eatenLayout.maxWidth+"x"+(int)eatenLayout.maxHeight;
			return message ; 
		}
	};

}
