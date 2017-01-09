package com.blockframe.core;

import java.io.IOException;

import com.blockframe.blocks.TableBlock;
import com.blockframe.core.BlockPipe.PipeLink;
import com.blockframe.core.DebugLog.StringGetter;
import com.blockframe.core.DebugLog.Verbosity;
import com.blockframe.core.Frame.PlacedFrame;
import com.blockframe.painters.Scribe;

/**
 * The base class for content in BlockFrame. 
 * A client will build subclass instances of {@linkplain Block}, and write them into a {@link PdfDocument}, or similar. 
 */
public abstract class Block implements Verbosity { 
	
	/**
	 * The {@link Quill} used to measure and draw this block. 
	 * It is set from calling {@link #inheritQuill(Quill)} early in the {@link #fill(Quill, Layout)} method. 
	 * <p>
	 * Subclasses should honour this convention. 
	 */
	protected Quill quill ; 
	
	/**
	 * Threshold for logging debug messages about this block. Zero is no messages, large is more. 
	 * @see DebugLog
	 */
	public double loggingVerbosity = 0 ; 
	
	/**
	 * Name to identify this block in logging messages. 
	 * Usually set lazily by {@link Block#getLogName()}. 
	 */
	public String loggingName ; 
	
	/**
	 * Links this block to the next one in a {@link BlockPipe}. 
	 */
	final BlockPipe.PipeLink pipeLink ; 
	
	/**
	 * Unique identifier for this block. 
	 */
	public final BlockId id = new BlockId(); 
	
	public Block() { 
		this.pipeLink = makePipeLink(); 
	}

	/**
	 * Intended to filter the {@link Quill} passed into the block's {@link #fill(Quill, Layout)} method (and thence the {@link #draw(Canvas, double, double)} method) ; 
	 * should be called early in an implementation of {@link #fill(Quill, Layout)}. 
	 * <p>
	 * This base-class method just passes on its argument, unchanged. 
	 * <p>
	 * If you wish your subclass to have different {@link Quill} parameters to its container, override this method, copy the instance and modify the fields. 
	 */
	protected Quill inheritQuill(Quill receivedQuill) { 
		return receivedQuill ; 
	}
	
	/**
	 * Intended to filter the {@link Layout} passed into the block's {@link #fill(Quill, Layout)} method ; should be called early in an implementation of {@link #fill(Quill, Layout)}. 
	 * <p>
	 * This base-class method just passes on its argument, unchanged. 
	 * <p>
	 * If you wish your subclass to have different {@link Layout} parameters to its container, override this method, copy the instance and modify the fields. 
	 */
	protected Layout inheritLayout(Layout receivedLayout) { 
		return receivedLayout ; 
	}
	
	/**
	 * Lay out the content of this <code>Block</code>. 
	 * <p>
	 * For leaf blocks (ie, those that don't contain other blocks), this method should measure the block, create a {@link PlacedBlock}, 
	 * and call {@link PlacedBlock#setDimensions(double, double)}. 
	 * <p>
	 * For container blocks, the methods {@link #fill(Quill, Layout)} and {@link PlacedBlock#setOffsetInContainer(double, double)} (or similar) should be called on each child. 
	 * <p>
	 * For {@link Frame} and its subclasses, this method should also read blocks from {@link Frame#reader}, and accept as many as desired into {@link PlacedFrame}.  
	 * <p>
	 * If overriding this method in subclasses, perhaps look at the source code for useful conventions. 
	 */
	public abstract PlacedBlock fill(Quill quill, Layout receivedLayout) throws IOException; 
	
	/**
	 * Draws the content of the block, with the top left corner as given, in page coordinates. 
	 * <p>
	 * WARNING: <code>BlockFrame</code> uses a co-ordinate system based at the top-left of the page, whereas PDF uses a co-ordinate system based at the bottom-left. 
	 * This detail might become important in implementations of this method. :-) 
	 * <p>
	 * If you are writing custom container blocks (like {@link Frame} or {@link TableBlock}, you should read the documentation on {@link PlacedBlock} and {@link PlacedBlock#draw(Canvas, double, double)}. 
	 * Specifically, when drawing children, this method should call the <code>draw</code> method in the {@linkplain PlacedBlock}, rather than the method in this class. 
	 * @see Canvas#getPdfBottom(double, double)
	 * @see Scribe
	 */
	public abstract void draw(Canvas canvas, double left, double top, double width, double height) throws IOException; 
	
	/**
	 * Whether the recent call to {@link #fill(Quill, Layout)} placed all of the child-blocks it needed to, or whether there are outstanding child-blocks. 
	 * <p>
	 * NOTE: This method should only be called shortly after a call to {@linkplain #fill(Quill, Layout)} 
	 * (that is, while the state of the block is still the same as at the end of the {@linkplain #fill(Quill, Layout)} call).  
	 * <p>
	 * For most {@linkplain Block} subclasses, this will be the constant <code>true</code>. 
	 * However, for {@link Frame} subclasses, a call to {@linkplain #fill(Quill, Layout)} might not place all of the blocks in the frame's {@link Frame#pipe}. 
	 */
	public boolean isFillComplete() { 
		return true ; 
	}
	
	/**
	 * Sets the logging verbosity for this block, used in the <code>add</code> methods of {@link DebugLog} to filter logging. 
	 */
	public Block setLoggingVerbosity(double verbosity) { 
		this.loggingVerbosity = verbosity ; 
		return this ; 
	}

	/**
	 * Returns a brief string identifying this block. 
	 * Useful for debugging messages. 
	 */
	public String getLogName() {
		if (loggingName!=null) return loggingName ; 
		return this.getClass().getSimpleName()+"-"+id.str;
	}

	/**
	 * Generates log message when the {@link #fill(Quill, Layout)} method is entered. 
	 * May be overridden in subclasses, with more information. 
	 */
	protected static final StringGetter logMessage_enteringFill = new StringGetter() { 
		public String getString(Block block, PlacedBlock placedBlock, Object ignore0, Object ignore1) {
			return "fill() entering"; 
		}
	};

	/**
	 * Generates log message when the {@link #fill(Quill, Layout)} method is finishing. 
	 * May be overridden in subclasses, with more information. 
	 */
	protected static final StringGetter logMessage_leavingFill = new StringGetter() { 
		public String getString(Block block, PlacedBlock placedBlock, Object ignore0, Object ignore1) {
			return "fill() leaving"; 
		}
	};

	/**
	 * Generates log message when the {@link PlacedBlock#draw(Canvas, double, double)} method is entered. 
	 * May be overridden in subclasses, with more information. 
	 */
	protected static final StringGetter logMessage_enteringDraw = new StringGetter() { 
		public String getString(Block block, PlacedBlock placedBlock, Object leftObject, Object topObject) {
			Integer left = (leftObject!=null) ? (int)(0.0+(Double)leftObject) : null ; 
			Integer top = (topObject!=null) ? (int)(0.0+(Double)topObject) : null ; 
			return "draw() entering, in parent "+(int)placedBlock.getLeftInContainer()+","+(int)placedBlock.getTopInContainer()+", on page "+left+","+top; 
		}
	};

	/**
	 * Generates log message when the {@link PlacedBlock#draw(Canvas, double, double)} method is finishing. 
	 * May be overridden in subclasses, with more information. 
	 */
	protected static final StringGetter logMessage_leavingDraw = new StringGetter() { 
		public String getString(Block block, PlacedBlock placedBlock, Object ignore0, Object ignore1) {
			return "draw() leaving"; 
		}
	};
	
	/**
	 * Generates log message when setting the size of a block. 
	 */
	static StringGetter logMessage_blockSize = new StringGetter() { 
		public String getString(Block block, PlacedBlock placedBlock, Object ignore0, Object ignore1) {
			return "size "+(int)placedBlock.width+"x"+(int)placedBlock.height ;
		}
	};
	
	/**
	 * Generates log message when setting the position of a block. 
	 */
	public static StringGetter logMessage_blockLocation = new StringGetter() { 
		public String getString(Block block, PlacedBlock placedBlock, Object ignore0, Object ignore1) {
			return "location  "+(int)placedBlock.leftInContainer+","+(int)placedBlock.topInContainer ;
		}
	};
	
	/**
	 * Constructs a message describing how a child-block protrudes outside the bounds of its container. 
	 * It assumes the child does protrude somewhere. 
	 */
	public static StringGetter logMessage_outsideBounds = new StringGetter() { 
		public String getString(Block block, PlacedBlock placedBlock, Object parentObject, Object childObject) { 
			/* 14dec16: 'placedBlock' and 'childObject' are the same object. That's fine, and gives the caller flexibility wrt logging-verbosity. */
			PlacedBlock placedParent = (PlacedBlock) parentObject ; 
			PlacedBlock placedChild = (PlacedBlock) childObject ; 
			StringBuffer sb = new StringBuffer(); 
			sb.append("Child outside parent's bounds"); 
			if (placedChild.leftInContainer<0) sb.append(", left is "+(int)placedChild.leftInContainer); 
			if (placedChild.topInContainer<0) sb.append(", top is "+(int)placedChild.leftInContainer); 
			if (placedChild.leftInContainer+placedChild.width>placedParent.width) sb.append(", right is "+(int)(placedChild.leftInContainer+placedChild.width)+" (parent width "+(int)placedParent.width+")"); 
			if (placedChild.topInContainer+placedChild.height>placedParent.height) sb.append(", bottom is "+(int)(placedChild.topInContainer+placedChild.height)+" (parent height "+(int)placedParent.height+")"); 
			return sb.toString(); 
		}
	};

	/**
	 * Builds and returns the value for field {@link #pipeLink}. 
	 * <p>
	 * This should not be used to access the block's link. 
	 * That should be done by directly accessing the field. 
	 */
	BlockPipe.PipeLink makePipeLink() { 
		return new BlockPipeLink(this); 
	}
	
	/**
	 * A {@linkplain PlacedBlock} object contains the measurement and position information for a {@link Block}. 
	 * Each {@linkplain Block} has a {@linkplain PlacedBlock} instance associated with it, returned by {@link Block#fill(Quill, Layout)}. 
	 * <p>
	 * Well, that's the simple case. It is more complicated for {@link Frame} subclasses, because a single {@linkplain Frame} can be split into two (or more) parts. 
	 * For example, a paragraph may be split over two pages. 
	 * In that case, there will be two {@linkplain PlacedBlock} instances associated with the {@linkplain Frame}, returned by separate calls to {@linkplain Block#fill(Quill, Layout)}.
	 * One will contain the first half of the Frame's content, the other the second half. 
	 * <p>
	 * This complexity is fairly invisible to subclasses, unless you are coding a container-subclass - one which holds child blocks, and which can split into multiple parts. 
	 */
	public class PlacedBlock { 

		private double width ; 

		private double height ; 

		/**
		 * Horizontal offset of this block in its container. 
		 * That is, it is relative to its container, not absolute on the page.
		 * <p>
		 * Although stored in the here, this field is usually accessed by the container, to compute arguments for {@link #draw(Canvas, double, double)}. 
		 */
		private double leftInContainer ; 

		/**
		 * Vertical offset of this block in its container. 
		 * That is, it is relative to its container, not absolute on the page.
		 * <p>
		 * Although stored in the here, this field is usually accessed by the container, to compute arguments for {@link #draw(Canvas, double, double)}. 
		 */
		private double topInContainer ; 

		/**
		 * Sets the size of the block. 
		 */
		public PlacedBlock setDimensions(double width, double height) { 
			this.width = width ; 
			this.height = height ; 
			DebugLog.add(DETAIL_8, this, null, logMessage_blockSize, null, null, false); 
			return this ; 
		}

		public double getWidth() { 
			return width ; 
		}

		public double getHeight() { 
			return height ; 
		}

		public void setOffsetInContainer(double left, double top) { 
			this.leftInContainer = left ; 
			this.topInContainer = top ; 
			DebugLog.add(DETAIL_8, this, null, logMessage_blockLocation, null, null, false); 
		}

		public Block getBlock() { 
			return Block.this ; 
		}

		/**	 
		 * Positions this child within its container. 
		 * The container's size should be already set. That is, {@link #setDimensions(double, double)} should have already been called on <code>container</code>. 
		 */
		public void setOffsetInContainer(PlacedBlock container, ChildPlacer placer) { 
			if (width==0 && height==0) DebugLog.add(WARNING_4, Block.this, null, "Parent width&height not yet set?", true); 
			placer.setOffsetInContainer(container, this); 
		}

		public double getLeftInContainer() {
			return leftInContainer;
		}

		public double getTopInContainer() {
			return topInContainer;
		}

		/**
		 * This method is called by the {@link Block#draw(Canvas, double, double, double, double)} method of its container. 
		 * This method will call the {@link Block#draw(Canvas, double, double, double, double)} method of its own Block (see {@link #getBlock()}). 
		 */
		public void draw(Canvas canvas, double left, double top) throws IOException { 
			DebugLog.add(ENTERING_5, this, null, logMessage_enteringDraw, left, top, false); 
			Block.this.draw(canvas, left, top, width, height); 
			DebugLog.add(LEAVING_6, this, null, logMessage_leavingDraw, null, null, false); 
		}

		/**
		 * Restores the pipe-input to a block to its state before the block began filling ({@link #fill(Quill, Layout)}). 
		 * This method should also traverse child blocks, and restore them. 
		 * <p>
		 * This implementation does nothing, which will be fine for most subclasses, except {@link Frame}. 
		 * <p>
		 * Some background might be useful...
		 * <p>
		 * During the fill-pass, it may happen that a {@link Frame} accepts more children than it can actually fit, and has to backtrack. 
		 * The blocks have to be pushed back into the pipe they were read from. 
		 * And also, the children that are pushed back have to be reverted as well. 
		 * <p>
		 * The {@link Frame} class has several methods related to this functionality. 
		 * <p>
		 * Currently (November 2016), no other container classes are affected, but it is theoretically possible. 
		 * If you are writing a container subclass which consumes blocks from a pipe, it is an issue that needs to be considered. 
		 */
		public void revertToStart() {} 

		/**
		 * Whether the child's boundaries protrude outside this block's boundaries. 
		 */
		private boolean isChildOutsideBounds(PlacedBlock placedChild) { 
			if (placedChild.leftInContainer<0) return true ; 
			if (placedChild.topInContainer<0) return true ; 
			if (placedChild.leftInContainer+placedChild.width>this.width) return true ; 
			if (placedChild.topInContainer+placedChild.height>this.height) return true ; 
			return false ; 
		}

		/**
		 * Writes a log message if the child's boundaries protrude outside this block's boundaries. 
		 */
		public void logIfChildOutsideBounds(PlacedBlock placedChild) {
			if (isChildOutsideBounds(placedChild)) { 
				DebugLog.add(WARNING_4, placedChild, null, logMessage_outsideBounds, this, placedChild, false); 
			}
		}
	}

	/**
	 * Wrapper for code which knows how to place a child in a parent. 
	 * <p>
	 * This is usually used in {@link Block#setOffsetInContainer(PlacedBlock, PlacedBlock)}, to replace a call to {@link Block#setOffsetInContainer(PlacedBlock, PlacedBlock)}. 
	 */
	public abstract static class ChildPlacer { 
		
		/**
		 * Should be called instead of {@link Block#setOffsetInContainer(PlacedBlock, PlacedBlock)}. 
		 * <p>
		 * Any implementation of this should calculate the appropriate location for the child, then call {@link Block#setOffsetInContainer(PlacedBlock, PlacedBlock)}. 
		 */
		abstract void setOffsetInContainer(PlacedBlock parent, PlacedBlock child); 
		
		public static final ChildPlacer TOP_LEFT = new ChildPlacer() { 
			@Override
			void setOffsetInContainer(PlacedBlock parent, PlacedBlock child) { 
				child.setOffsetInContainer(0, 0); 
			}
		};
		
		public static final ChildPlacer TOP_CENTRE = new ChildPlacer() { 
			@Override
			void setOffsetInContainer(PlacedBlock parent, PlacedBlock child) { 
				double horizontalSpace = parent.getWidth() - child.getWidth() ; 
				child.setOffsetInContainer(horizontalSpace/2, 0); 
			}
		};
		
		public static final ChildPlacer TOP_RIGHT = new ChildPlacer() { 
			@Override
			void setOffsetInContainer(PlacedBlock parent, PlacedBlock child) { 
				double horizontalSpace = parent.getWidth() - child.getWidth() ; 
				child.setOffsetInContainer(horizontalSpace, 0); 
			}
		};
	
		public static final ChildPlacer CENTRE_LEFT= new ChildPlacer() { 
			@Override
			void setOffsetInContainer(PlacedBlock parent, PlacedBlock child) { 
				double verticalSpace = parent.getHeight() - child.getHeight() ; 
				child.setOffsetInContainer(0, verticalSpace/2); 
			}
		};
	
		public static final ChildPlacer CENTRE_CENTRE = new ChildPlacer() { 
			@Override
			void setOffsetInContainer(PlacedBlock parent, PlacedBlock child) { 
				double horizontalSpace = parent.getWidth() - child.getWidth() ; 
				double verticalSpace = parent.getHeight() - child.getHeight() ; 
				child.setOffsetInContainer(horizontalSpace/2, verticalSpace/2); 
			}
		};
		
		public static final ChildPlacer CENTRE_RIGHT = new ChildPlacer() { 
			@Override
			void setOffsetInContainer(PlacedBlock parent, PlacedBlock child) { 
				double horizontalSpace = parent.getWidth() - child.getWidth() ; 
				double verticalSpace = parent.getHeight() - child.getHeight() ; 
				child.setOffsetInContainer(horizontalSpace, verticalSpace/2); 
			}
		};
		
		public static final ChildPlacer BOTTOM_LEFT = new ChildPlacer() { 
			@Override
			void setOffsetInContainer(PlacedBlock parent, PlacedBlock child) { 
				double verticalSpace = parent.getHeight() - child.getHeight() ; 
				child.setOffsetInContainer(0, verticalSpace); 
			}
		};
		
		public static final ChildPlacer BOTTOM_CENTRE = new ChildPlacer() { 
			@Override
			void setOffsetInContainer(PlacedBlock parent, PlacedBlock child) { 
				double horizontalSpace = parent.getWidth() - child.getWidth() ; 
				double verticalSpace = parent.getHeight() - child.getHeight() ; 
				child.setOffsetInContainer(horizontalSpace/2, verticalSpace); 
			}
		};
		
		public static final ChildPlacer BOTTOM_RIGHT = new ChildPlacer() { 
			@Override
			void setOffsetInContainer(PlacedBlock parent, PlacedBlock child) { 
				double horizontalSpace = parent.getWidth() - child.getWidth() ; 
				double verticalSpace = parent.getHeight() - child.getHeight() ; 
				child.setOffsetInContainer(horizontalSpace, verticalSpace); 
			}
		};
	}

	/**
	 * Implementation of {@link PipeLink} to handle the normal case. 
	 */
	private static class BlockPipeLink extends BlockPipe.PipeLink { 
		
		private final Block block ; 
		
		BlockPipeLink(Block block) { 
			this.block = block ; 
		}
		
		public Block getNextBlockToFill() {
			return nextBlockWritten ; 
		}
		
		Block getBlock() {  
			return block ; 
		}
	}
	
	/**
	 * Provides a unique id for each block, useful in debugging. 
	 * They are sequential, so there is some semantic information. 
	 */
	public static final class BlockId { 
		private static long next = 0 ; 
		public final long idNumber ; 
		public final BlockId parentId ; 
		public final String str ; 
		BlockId() { 
			this.idNumber = next ++ ; 
			this.parentId = null ; 
			this.str = String.valueOf(idNumber); 
		}
	}
	
}
