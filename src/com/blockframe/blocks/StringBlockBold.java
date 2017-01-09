package com.blockframe.blocks;

import com.blockframe.core.Quill;

/**
 * Subclass of {@link StringBlock} which forces bold font. 
 */
public class StringBlockBold extends StringBlock {
	public StringBlockBold(String text) {
		super(text);
	} 
	@Override
	protected Quill inheritQuill(Quill receivedQuill) {
		return super.inheritQuill(receivedQuill).copyBold(); 
	}
}