package org.blockframe.core;

import org.blockframe.core.Block.PlacedBlock;
import org.blockframe.core.DebugLog.StringGetter;
import org.blockframe.core.DebugLog.Verbosity;

/**
 * Holds a pipeline of blocks for a container-block, usually a {@link Frame}, to layout and display. 
 * <p>
 * Although pipes are the main tool for passing content from the client to BlockFrame, they are fairly invisible to the client code. 
 * When the client code calls {@link PdfChapter#write(Block)} or {@link Frame#write(Block)}, the block ends up being added to a pipe. 
 * <p>
 * Blocks are pulled from a pipe with the {@link BlockReader#read()} method, usually in a frame's {@link Block#fill(Quill, Layout)} method. 
 */
public class BlockPipe implements Verbosity { 

	public final BlockWriter writer = new BlockWriter(); 
	
	public final BlockReader reader = new BlockReader();  
	
	public final String id ; 
	
	/**
	 * The most recent link <em>added</em> to the pipe. 
	 */
	private BlockPipe.PipeLink lastLinkAdded = null ; 
	
	/**
	 * Provides the next block in the pipe. 
	 * <p>
	 * Note: the next block might not be known until after {@link Block#fill(Quill, Layout)} on the current block has finished. 
	 * The next block may be a continuation of the current block.
	 * For example, if a paragraph is split between two pages. 
	 */
	private NextGetter_interface nextGetter = null ; 
	
	public BlockPipe(String id) { 
		this.id = id ; 
	}

	/**
	 * An interface into {@link BlockPipe} for writing blocks to the end of the pipe. 
	 */
	public final class BlockWriter { 

		private boolean isClosed = false ; 
		
		public void write(Block block) { 
			if (isClosed) DebugLog.add(WARNING_4, block, null, "Writing block to a closed pipe. Block is dropped.", true); // TODO BUG? This should return without writing. 
			BlockPipe pipe = BlockPipe.this;
			if (lastLinkAdded!=null) { 
				BlockPipe.PipeLink oldLast = lastLinkAdded ; 
				oldLast.setNext(block); 
				pipe.lastLinkAdded = block.pipeLink ; 
			} else { 
				/* Adding first block. */ 
				pipe.lastLinkAdded = block.pipeLink ; 
				pipe.nextGetter = new NextGetter(block); 
			}
		}

		public void close() {
			this.isClosed = true ; 
		}
	}

	/**
	 * An interface into {@link BlockPipe} for reading the next block from the pipe. 
	 */
	public final class BlockReader { 
		
		private Block peekNextBlock() { 
			if (nextGetter==null) return null ; 
			Block nextBlock = nextGetter.getNextBlockToFill(); 
			return nextBlock ; 
		}

		/**
		 * Whether there are more blocks in the pipe. 
		 * If this returns <code>false</code>, all subsequent calls to {@link #read()} will return <code>null</code>. 
		 */
		public boolean hasMore() { 
			Block nextBlock = peekNextBlock(); 
			if (nextBlock!=null) return true ; 
			writer.close(); // Don't allow writing after we've told a reader there are no more. 
			return false ; 
		}

		/**
		 * Returns the next block from the pipe. 
		 * This advances the cursor in the pipe. 
		 * Well, except that if the returned block is a frame, the next block to be read may be a later part of the same frame. 
		 */
		public Block read() {
			Block resultBlock = peekNextBlock(); 
			if (resultBlock!=null) { 
				BlockPipe.this.nextGetter = resultBlock.pipeLink ; 
				return resultBlock ; 
			} else { 
				writer.close(); // Don't allow writing after we've told a reader there are no more. 
				BlockPipe.this.nextGetter = null ; 
				return null ; 
			}
		}
		
		/**
		 * Reverts the pipe so the given block is the next block to be read. 
		 * The given block should be one previously read from this pipe. 
		 */
		public void revertTo(Block block) { 
			BlockPipe.this.nextGetter = new NextGetter(block); 
		}
		
	}
	
	/**
	 * Logs the first few blocks in the pipe. 
	 * The message-arguments may be <code>null</code>. 
	 */
	public final StringGetter logMessage_pipe = new StringGetter() { 
		public String getString(Block block, PlacedBlock placedBlock, Object ignore0, Object ignore1) {
			StringBuffer sb = new StringBuffer(); 
			sb.append(BlockPipe.this.getClass().getSimpleName()); 
			sb.append("-"); 
			sb.append(BlockPipe.this.id); 
			if (nextGetter!=null) { 
				Block tmpBlock = nextGetter.getNextBlockToFill(); 
				if (tmpBlock!=null) { 
					sb.append(", next blocks are"); 
					for (int i=0 ; i<3 && tmpBlock!=null ; i++) { 
						sb.append("  "); 
						sb.append(tmpBlock.getLogName()); 
						tmpBlock = tmpBlock.pipeLink.dbgGetNext(); 
					}
					if (tmpBlock!=null) { 
						sb.append("  ..."); 
					}
				} else { 
					sb.append(" is empty(?)"); 
				}
			} else { 
				sb.append(" is empty"); 
			}
			return sb.toString();
		}
	};
	
	/**
	 * Links the blocks in a {@link BlockPipe}. 
	 * Every {@link Block} has a unique instance associated with it. 
	 * @see Block#pipeLink
	 */
	abstract static class PipeLink implements NextGetter_interface { 
	
		/**
		 * The next block which was written to the {@linkplain BlockPipe}. 
		 */
		Block nextBlockWritten = null ; 
	
		void setNext(Block nextBlock) { 
			if (this.nextBlockWritten!=null) DebugLog.add(Verbosity.WARNING_4, nextBlock, null, "Block being linked into a pipe more than once.", true); 
			this.nextBlockWritten = nextBlock ; 
		}
	
		/**
		 * Typically, returns the {@link Block} associated with this link. See {@link Block#pipeLink}. 
		 * <p>
		 * Note: this method should not be called on subclass {@link NextGetter}. 
		 */
		abstract Block getBlock(); 
		
		/**
		 * Returns the next block in the pipe, regardless of whether the current block has finished processing. 
		 * This is useful for debugging. 
		 */
		Block dbgGetNext() {
			return nextBlockWritten ;
		} 
	}
	
	/**
	 * Provides method {@link #getNextBlockToFill()}. 
	 */
	interface NextGetter_interface { 
		/**
		 * Returns the next block that should be processed. 
		 * Usually, this will be the next block that was written to the pipe. 
		 * However, this can vary - see {@link Frame#FramePipeLink}. 
		 * <p>
		 * Warning: this method should not be called until the next block is known. 
		 * Specifically, until the {@link Block#fill(Quill, Layout)} method has completed in the block associated with this link. 
		 * <p>
		 * This method does not advance the pipe. An immediately subsequent call will return the same value. 
		 */
		Block getNextBlockToFill(); 
	}

	/**
	 * Holder for a next-block, used when we don't have a previous block to link from. 
	 * This happens for the first block in a pipe, and when reverting the pipe. 
	 */
	static class NextGetter implements NextGetter_interface { 
		
		Block nextBlock = null ; 
		
		NextGetter(Block nextBlock) { 
			this.nextBlock = nextBlock ; 
		}
		
		public Block getNextBlockToFill() {
			return nextBlock ;
		}
	}

}
