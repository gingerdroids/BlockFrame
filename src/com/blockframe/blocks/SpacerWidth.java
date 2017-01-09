package com.blockframe.blocks;

import java.io.IOException;

import com.blockframe.core.Block;
import com.blockframe.core.Frame;
import com.blockframe.core.Layout;
import com.blockframe.core.Canvas;
import com.blockframe.core.Quill;

/**
 * White space block of a given width. 
 * The width may be either absolute, or string-based and vary with the font. 
 * <p>
 * If used in a {@link Frame}, it might be that {@link Frame#setHorizontalGap(String)} would be more appropriate. 
 * <p>
 * The height of the frame is very small, but non-zero. 
 */
public final class SpacerWidth extends Block { 
	
	private double spacerSize = 0.0 ; 
	private final String spacerTemplate ; 
	private Quill spacerQuill = null ; 
	
	public SpacerWidth(double spacerWidth) { 
		this.spacerSize = spacerWidth ; 
		this.spacerTemplate = null ; 
	}
	
	public SpacerWidth(String spacerTemplate) { 
		if (spacerTemplate==null) throw new IllegalArgumentException("Cannot have null template."); 
		this.spacerTemplate = spacerTemplate ; 
		this.spacerQuill = null ; 
	}
	
	/**
	 * Convenience method for {@link #SpacerWidth(String)} with a single space as the template string. 
	 */
	public SpacerWidth() { 
		this(" "); 
	}
	
	/**
	 * Returns the spacer width in absolute units, calculating from the template and font if required. 
	 * Subsequent calls with the same quill use a cached value. 
	 */
	public double getSpacerSize(Quill quill) throws IOException { 
		if (spacerTemplate!=null) { 
			if (quill==null) throw new IllegalArgumentException("Argument 'quill' should not be null"); 
			if (quill!=this.spacerQuill) { 
				this.spacerSize = quill.getStringWidth(spacerTemplate); 
				this.spacerQuill = quill ; 
			}
		}
		return spacerSize ; 
	}
	
	@Override
	public PlacedBlock fill(Quill quill, Layout receivedLayout) throws IOException {
		PlacedBlock placedBlock = new PlacedBlock(); 
		placedBlock.setDimensions(getSpacerSize(quill), Double.MIN_VALUE); // Non-zero height, so it won't trigger warnings. 
		return placedBlock ; 
	}
	
	@Override
	public void draw(Canvas canvas, double left, double top, double width, double height) throws IOException {} 
}