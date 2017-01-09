package org.blockframe.core;

import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.blockframe.blocks.TableBlock;
import org.blockframe.core.Block.PlacedBlock;
import org.blockframe.core.DebugLog.StringGetter;
import org.blockframe.core.DebugLog.Verbosity;


/**
 * Class {@linkplain Layout} stores maximum size and layout parameters. 
 * <p>
 * {@linkplain Layout} objects are passed through the {@link Block#fill(Quill, Layout)} traversal of the content-tree via the {@link Block#inheritLayout(Layout)} method, 
 * potentially being copied and modified in that method. 
 */
public class Layout implements Verbosity { 
	
	public static final Justification CENTRE_H = new Justification("centre_h"); // TODO Americanize the spellings? "centre" to "center"? 
	public static final Justification LEFT = new Justification("left"); 
	public static final Justification RIGHT = new Justification("right"); 
	public static final Justification FULL = new Justification("full"); 
	
	public static final Alignment TOP = new Alignment("top"); 
	public static final Alignment CENTRE_V = new Alignment("centre_v"); 
	public static final Alignment BOTTOM = new Alignment("bottom"); 
	
	public double maxWidth ; 
	
	public double maxHeight ; 
	
	/**
	 * Whether the width of this block should be tight around its contents, or use the full width available. 
	 * <p>
	 * The default is to use the full width available. (Which differs from {@link #isHeightTight}.) 
	 */
	public boolean isWidthTight = false ; // TODO Doco that height-tight and width-tight have different defaults. 
	
	/**
	 * Whether the height of this block should be tight around its contents, or use the full height available. 
	 * <p>
	 * The default is to be tight around the contents. (Which differs from {@link #isWidthTight}.) 
	 */
	public boolean isHeightTight = true ; 
	
	/**
	 * Whether this block's contents may be split over more than one {@link PlacedBlock}. 
	 * <p>
	 * The default is <code>true</code>, but this is probably only relevant for {@link Frame} subclasses. 
	 */
	public boolean allowSplitting = true ; 
	
	/**
	 * How the children of this block are laid out within this block. 
	 * This is mostly relevant in {@link Frame}, {@link TableBlock} and their subclasses. 
	 * The default justification is {@link #FULL}. 
	 */
	public Justification justification = FULL ; 
	
	/**
	 * How the children of this block are laid out within this block. 
	 * This is mostly relevant in {@link Frame}, {@link TableBlock} and their subclasses. 
	 * The default alignment is {@link #BOTTOM}. 
	 */
	public Alignment alignment = BOTTOM ; 
	
	public Layout(double maxWidth, double maxHeight) { 
		this.maxWidth = maxWidth ; 
		this.maxHeight = maxHeight ; 
	}
	
	/**
	 * Copy constructor.
	 */
	private Layout(Layout old) { 
		this.maxWidth = old.maxWidth ; 
		this.maxHeight = old.maxHeight ; 
		this.isWidthTight = old.isWidthTight ; 
		this.isHeightTight = old.isHeightTight ; 
		this.justification = old.justification ; 
		this.allowSplitting = old.allowSplitting ; 
	}
	
	/**
	 * Returns a copy of this instance. 
	 * <p>
	 * In method {@link Block#inheritLayout(Layout)}, it would be more common to use one of the <code>copyXxxx</code> methods. 
	 */
	public Layout copy() { 
		return new Layout(this); 
	}
	
	/**
	 * Copy the instance, then update {@link #justification}. 
	 * @param justification Ignored if null. 
	 */
	public Layout copy(Justification justification) { 
		Layout layout = new Layout(this);
		if (justification!=null) layout.justification = justification ;
		return layout; 
	}

	/**
	 * Copy the instance, then update {@link #justification}. 
	 */
	public Layout copyCentreH() { Layout layout = new Layout(this); layout.justification = CENTRE_H ; return layout ; } 
	
	/**
	 * Copy the instance, then update {@link #justification}. 
	 */
	public Layout copyLeft() { Layout layout = new Layout(this); layout.justification = LEFT ; return layout ; } 
	
	/**
	 * Copy the instance, then update {@link #justification}. 
	 */
	public Layout copyRight() { Layout layout = new Layout(this); layout.justification = RIGHT ; return layout ; } 
	
	/**
	 * Copy the instance, then update {@link #justification}. 
	 */
	public Layout copyFull() { Layout layout = new Layout(this); layout.justification = FULL ; return layout ; } 

	/**
	 * Copy the instance, then update {@link #alignment}. 
	 */
	public Layout copyTop() { Layout layout = new Layout(this); layout.alignment = TOP ; return layout ; } 
	
	/**
	 * Copy the instance, then update {@link #alignment}. 
	 */
	public Layout copyCentreV() { Layout layout = new Layout(this); layout.alignment = CENTRE_V ; return layout ; } 
	
	/**
	 * Copy the instance, then update {@link #alignment}. 
	 */
	public Layout copyBottom() { Layout layout = new Layout(this); layout.alignment = BOTTOM ; return layout ; } 
	
	public Layout copyTight(Boolean isWidthTight, Boolean isHeightTight) { return new Layout(this).setTight(isWidthTight, isHeightTight); }
	
	public Layout setTight(Boolean isWidthTight, Boolean isHeightTight) { 
		if (isWidthTight!=null) this.isWidthTight = isWidthTight ; 
		if (isHeightTight!=null) this.isHeightTight = isHeightTight ; 
		return this ; 
	}
	
	public Layout copyAllowSplitting(boolean allowSplitting) { return new Layout(this).setAllowSplitting(allowSplitting); }
	
	public Layout setAllowSplitting(boolean allowSplitting) {
		this.allowSplitting = allowSplitting;
		return this ;
	}
	
	/**
	 * Reduce field {@link #maxWidth} by the given amount. 
	 * If it becomes negative, it is set to zero. 
	 */
	public void reduceWidth(double reduceBy) { 
		this.maxWidth -= reduceBy ; 
		if (this.maxWidth<0) { 
			DebugLog.add(WARNING_4, null, null, "Width reduced to below zero? Set to zero.", true); 
			this.maxWidth = 0 ; 
		}
	}
	
	/**
	 * Reduce field {@link #maxHeight} by the given amount. 
	 * If it becomes negative, it is set to zero. 
	 */
	public void reduceHeight(double reduceBy) { 
		this.maxHeight -= reduceBy ; 
		if (this.maxHeight<0) { 
			DebugLog.add(WARNING_4, null, null, "Height reduced to below zero? Set to zero.", true); 
			this.maxHeight = 0 ; 
		}
	}
	
	/**
	 * Sets the maximum width and height. 
	 * @param maxWidth Ignored if null. 
	 * @param maxHeight Ignored if null. 
	 */
	public Layout setSize(Double maxWidth, Double maxHeight) { 
		if (maxWidth!=null) this.maxWidth = maxWidth ; 
		if (maxHeight!=null) this.maxHeight = maxHeight ; 
		return this ; 
	}
	
	Layout copyAndSetSize(PDPage page) { 
		Layout layout = new Layout(this); 
		PDRectangle mediaBox = page.getMediaBox(); 
		layout.maxWidth = mediaBox.getWidth(); 
		layout.maxHeight = mediaBox.getHeight(); 
		return layout ; 
	}
	
	/**
	 * Returns a concise summary of the layout, suitable for logging. 
	 */
	public String getLogDescription() { 
		StringBuffer sb = new StringBuffer(); 
		sb.append("layout  "); 
		sb.append("max size "+(int)maxWidth+"x"+(int)maxHeight+", "); 
		sb.append("alignment "+alignment.name+", "); 
		sb.append("justification "+justification.name); 
		sb.append(", "); 
		if (isWidthTight) { 
			if (isHeightTight) { 
				sb.append("tight width&height"); 
			} else { 
				sb.append("tight width, full height"); 
			}
			sb.append(""); 
		} else { 
			if (isHeightTight) { 
				sb.append("full width, tight height"); 
			} else { 
				sb.append("full width&height"); 
			}
			sb.append(""); 
		}
		sb.append(", "); 
		if (allowSplitting) { 
			sb.append("allow splitting"); 
		} else { 
			sb.append("no splitting"); 
		}
		return sb.toString(); 
	}
	
	/**
	 * Generates brief description of the layout, including remaining bounds. 
	 * The first message-arg is the {@linkplain Layout} object. 
	 */
	public static StringGetter logMessage_layout = new StringGetter() { 
		public String getString(Block block, PlacedBlock placedBlock, Object layoutObject, Object ignore1) { 
			Layout layout = (Layout) layoutObject ; 
			return layout.getLogDescription();
		}
	};
	
	/**
	 * Base class for object-constants. 
	 * Each instance can have a name, suitable for logging. 
	 */
	private static class NamedConstant { 
		public final String name ; 
		NamedConstant(String name) { 
			this.name = name ; 
		}
	}
	
	/**
	 * Horizontal justification constants. 
	 */
	public static class Justification extends NamedConstant { public Justification(String name) { super(name); }} 
	
	/**
	 * Vertical alignment constants.
	 */
	public static class Alignment extends NamedConstant { public Alignment(String name) { super(name); }} 
	
}
