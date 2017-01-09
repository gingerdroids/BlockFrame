package org.blockframe.blocks;

import java.io.IOException;

import org.blockframe.core.Block;
import org.blockframe.core.BlockPipe;
import org.blockframe.core.DebugLog;
import org.blockframe.core.Frame;
import org.blockframe.core.Layout;
import org.blockframe.core.Quill;
import org.blockframe.core.Layout.Justification;


/**
 * A {@link Frame} subclass that lays out its children vertically. 
 */
public class FrameVertical extends Frame { 
	
	public FrameVertical() {}
	
	public FrameVertical(BlockPipe pipe) { 
		super(pipe); 
	}

	@Override
	public PlacedBlock fill(Quill receivedQuill, Layout receivedLayout) throws IOException { 
		DebugLog.add(ENTERING_5, this, null, logMessage_enteringFill, null, null, true); 
		this.quill = inheritQuill(receivedQuill) ; 
		Layout frameLayout = inheritLayout(receivedLayout); 
		DebugLog.add(ALL_9, this, null, pipe.logMessage_pipe, null, null, false); 
		DebugLog.add(DETAIL_8, this, null, Layout.logMessage_layout, frameLayout, null, false); 
		PlacedFrame placedFrame = this.new PlacedFrame(); 
		//////  Measure children and accept as many as we can fit
		double maxWidth = 0 ; 
		{
			Layout eatenLayout = frameLayout.copy(); // Copy of layout, whose max-height is eaten away as we add blocks. 
			while (reader.hasMore()) { 
				Block child = reader.read(); 
				PlacedBlock placedChild = child.fill(quill, eatenLayout); 
				double childHeight = placedChild.getHeight();
				if (eatenLayout.allowSplitting && childHeight>eatenLayout.maxHeight) { // Reject overflowing child. 
					// TODO Frame field boolean shouldAcceptHighBlock - controlling whether a single block that is too high is accepted (messy), or rejected (infinite loop danger). 
					/*
					 * We are rejecting one child, which hasn't yet been added to 'children'. 
					 * We should revert the child to its start, and push it back onto the feed so the next frame will receive it. 
					 */
					DebugLog.add(DETAIL_8, this, null, "child rejected because exceeds height.", true); 
					placedChild.revertToStart(/*placedFrame.placedId*/); 
					reader.revertTo(child); 
					break ; 
				}
				placedFrame.add(placedChild); 
				eatenLayout.reduceHeight(childHeight); 
				if (placedChild.getWidth()>maxWidth) maxWidth = placedChild.getWidth() ; 
				DebugLog.add(ALL_9, this, null, logMessage_childAccepted, placedChild, eatenLayout, false); 
				//////  Does the child need more space? 
				if (!child.isFillComplete()) { 
					/*
					 * The child could not place all of its children, and still has some outstanding to place. 
					 * There isn't room in this frame to place them, so we'll exit here and let our parent find more space. 
					 * Probably, we're at the end of a page here, and the new space will be at the top of the next page. 
					 */
					break ; 
				}
			}
		}
		double frameWidth = frameLayout.isWidthTight ? maxWidth : frameLayout.maxWidth ; 
		//////  Position children in this frame
		Justification justification = frameLayout.justification; 
		double nextTop = 0 ; 
		for (PlacedBlock child : placedFrame.children) { 
			double left ; 
			if (justification==Layout.LEFT || justification==Layout.FULL) left = 0 ; 
			else if (justification==Layout.RIGHT) left = frameWidth - child.getWidth() ; 
			else left = (frameWidth - child.getWidth()) / 2 ; 
			child.setOffsetInContainer(left, nextTop); 
			nextTop += child.getHeight() ; 
		}
		//////  Set my dimensions
		placedFrame.setDimensions(frameWidth, nextTop); // TODO Honour vertical alignment
		DebugLog.add(LEAVING_6, this, null, logMessage_leavingFill, placedFrame.size(), null, false); 
		return placedFrame ; 
	}
}