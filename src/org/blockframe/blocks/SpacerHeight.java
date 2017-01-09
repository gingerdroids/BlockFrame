package org.blockframe.blocks;

import java.io.IOException;

import org.blockframe.core.Block;
import org.blockframe.core.Canvas;
import org.blockframe.core.Layout;
import org.blockframe.core.Quill;


/**
 * White space block of a given height. 
 * The height may be either absolute, or string-based and vary with the font. 
 * <p>
 * The width of the frame is very small, but non-zero. 
 */
public final class SpacerHeight extends Block { 
	
	private final Double fontMultiple ; 
	private double spacerSize = 0.0f ; 
	private Quill spacerQuill = null ; 
	
	/**
	 * Constructor, for both variants - absolute height and font varying height. 
	 * @param isFontMultiple Whether the height varies with the font, or is absolute. 
	 * @param spacerHeight Either the multiple of the font height, or an absolute height. 
	 */
	public SpacerHeight(boolean isFontMultiple, double spacerHeight) { 
		if (isFontMultiple) { 
			this.fontMultiple = spacerHeight ; 
		} else { 
			this.fontMultiple = null ; 
			this.spacerSize = spacerHeight ; 
		}
	}

	/**
	 * Returns the spacer height in absolute units, calculating from the template and font if required. 
	 * Subsequent calls with the same quill use a cached value. 
	 */
	public double getSpacerSize(Quill quill) throws IOException { 
		if (fontMultiple!=null) { 
			if (quill==null) throw new IllegalArgumentException("Argument 'quill' should not be null"); 
			if (quill!=this.spacerQuill) { 
				this.spacerSize = quill.getFontHeight() * fontMultiple ; 
				this.spacerQuill = quill ; 
			}
		}
		return spacerSize ; 
	}
	
	@Override
	public PlacedBlock fill(Quill quill, Layout receivedLayout) throws IOException {
		PlacedBlock placedBlock = new PlacedBlock(); 
		placedBlock.setDimensions(Double.MIN_VALUE, getSpacerSize(quill)); // Non-zero width, so it won't trigger warnings. 
		return placedBlock ; 
	}
	
	@Override
	public void draw(Canvas canvas, double left, double top, double width, double height) throws IOException {} 
}