package com.blockframe.blocks;

import java.io.IOException;

import com.blockframe.core.Block;
import com.blockframe.core.BlockPipe;
import com.blockframe.core.DebugLog;
import com.blockframe.core.Frame;
import com.blockframe.core.Layout;
import com.blockframe.core.Quill;
import com.blockframe.core.Layout.Alignment;

/**
 * A {@link Frame} subclass that lays out its children in a horizontal row. 
 */
public class FrameHorizontal extends Frame {
		
		public FrameHorizontal() {}
		
		public FrameHorizontal(BlockPipe pipe) {
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
			double horizontalGapSize = getHorizontalGap(quill);
			double sumWidth = 0 ; // Includes gaps. 
			double maxChildHeight = 0 ; 
			{
				Layout eatenLayout = frameLayout.copy(); // Copy of layout, whose max-width is eaten away as we add blocks. 
				while (reader.hasMore()) { 
					Block child = reader.read(); 
					PlacedBlock placedChild = child.fill(quill, eatenLayout); 
					double childWidth = placedChild.getWidth();
					double addedWidth = childWidth ; 
					if (sumWidth>0) addedWidth += horizontalGapSize ; 
					if (eatenLayout.allowSplitting && childWidth>eatenLayout.maxWidth && sumWidth>0) { // Reject overflowing child, unless it's the first on the line. 
						/*
						 * We are rejecting one child, which hasn't yet been added to 'children'. 
						 * We should revert the child to its start, and push it back onto the feed so the next frame will receive it. 
						 */
						DebugLog.add(DETAIL_8, this, null, "child rejected because exceeds width.", true); 
						placedChild.revertToStart(/*placedFrame.placedId*/); 
						reader.revertTo(child); 
						break ; 
					}
					placedFrame.add(placedChild); 
					eatenLayout.reduceWidth(horizontalGapSize+childWidth); // Maximum size of next child must exclude the gap before it. 
					DebugLog.add(ALL_9, this, null, logMessage_childAccepted, placedChild, eatenLayout, false); 
					sumWidth += addedWidth ; 
					double childHeight = placedChild.getHeight();
					if (childHeight>maxChildHeight) maxChildHeight = childHeight ; 
					//////  Does the child need more space? 
					if (!child.isFillComplete()) { 
						/* 
						 * The child could not place all of its children, and still has some outstanding to place. 
						 * There isn't room in this frame to place them, so we'll exit here and let our parent find more space. 
						 */
						break ; 
					}
				}
			}
			int childCount = placedFrame.size(); 
			//////  Calculate frame's width and gap size. 
			double frameWidth ;  
			double gap = horizontalGapSize ; 
			if (frameLayout.isWidthTight) { 
				frameWidth = sumWidth ; 
			} else { 
				frameWidth = frameLayout.maxWidth ; 
				double spareSpace = frameWidth - sumWidth ; 
				if (childCount>1) gap += spareSpace / (childCount-1); // TODO Honour justification 
			}
			//////  Position children in this frame
			//// Compute how much vertical gap to use above each word
			Alignment alignment = frameLayout.alignment ; 
			double dropFactor ; 
			if (alignment==Layout.TOP) { 
				dropFactor = 0 ; 
			} else if (alignment==Layout.CENTRE_V) { 
				dropFactor = 0.5f ; 
			} else { 
				dropFactor = 1 ; 
			}
			//// Do It - position the children 
			double nextLeft = 0 ; 
			for (PlacedBlock child : placedFrame.children) { 
				if (nextLeft>0) nextLeft += gap ; 
				double childTop = (maxChildHeight-child.getHeight()) * dropFactor ; 
				child.setOffsetInContainer(nextLeft, childTop); 
				nextLeft += child.getWidth() ; 
			}
			//////  Set my dimensions
			double frameHeight = frameLayout.isHeightTight ? maxChildHeight : frameLayout.maxHeight ; 
			placedFrame.setDimensions(nextLeft, frameHeight); 
			DebugLog.add(LEAVING_6, placedFrame, null, logMessage_leavingFill, childCount, null, false); 
			return placedFrame ; 
		}

	}