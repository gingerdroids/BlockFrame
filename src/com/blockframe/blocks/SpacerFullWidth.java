package com.blockframe.blocks;

import java.io.IOException;

import com.blockframe.core.Block;
import com.blockframe.core.Layout;
import com.blockframe.core.Canvas;
import com.blockframe.core.Quill;

/**
 * Whitespace, taking the full width available in its {@link Layout}. 
 * It has small but non-zero height. 
 */
public final class SpacerFullWidth extends Block {
	@Override
	public PlacedBlock fill(Quill quill, Layout receivedLayout) throws IOException {
		PlacedBlock placedBlock = new PlacedBlock(); 
		placedBlock.setDimensions(receivedLayout.maxWidth, Double.MIN_VALUE); // Non-zero height, so it won't trigger warnings. 
		return placedBlock ; 
	}
	@Override
	public void draw(Canvas canvas, double left, double top, double width, double height) throws IOException {} 
}