package com.blockframe.core;

import java.awt.Color;
import java.io.IOException;

import org.apache.pdfbox.pdmodel.font.PDFont;
import org.apache.pdfbox.pdmodel.font.PDType1Font;

/**
 * Class {@linkplain Quill} mainly stores information about the font to write with, but can also hold related style-of-writing information. 
 * Currently (November2016), the only other information is colour. 
 * <p>
 * {@linkplain Quill} objects are passed through the {@link Block#fill(Quill, Layout)} traversal of the content-tree via the {@link Block#inheritQuill(Quill)} method, 
 * potentially being copied and modified in that method. 
 * <p>
 * Each block should store a copy of the {@linkplain Quill} instance for use in the later {@link Block#draw(Canvas, double, double, double, double)} traversal. 
 * <p>
 * PDF-Box has a standard set of fonts: see "https://pdfbox.apache.org/1.8/cookbook/workingwithfonts.html".
 */
public class Quill {

	public static final FontFamily TIMES_ROMAN = new FontFamily(PDType1Font.TIMES_ROMAN, PDType1Font.TIMES_BOLD, PDType1Font.TIMES_ITALIC, PDType1Font.TIMES_BOLD_ITALIC); 
	public static final FontFamily COURIER = new FontFamily(PDType1Font.COURIER, PDType1Font.COURIER_BOLD, PDType1Font.COURIER_OBLIQUE, PDType1Font.COURIER_BOLD_OBLIQUE); 
	public static final FontFamily HELVETICA = new FontFamily(PDType1Font.HELVETICA, PDType1Font.HELVETICA_BOLD, PDType1Font.HELVETICA_OBLIQUE, PDType1Font.HELVETICA_BOLD_OBLIQUE); 
	public static final FontStyle PLAIN = new FontStyle(); 
	public static final FontStyle BOLD = new FontStyle(); 
	public static final FontStyle ITALIC = new FontStyle(); 
	public static final FontStyle BOLD_ITALIC = new FontStyle(); 
	
	public static final FontFamily DEFAULT_FONT_FAMILY = TIMES_ROMAN;
	public static final FontStyle DEFAULT_FONT_STYLE = PLAIN;
	public static final int DEFAULT_FONT_SIZE = 10;
	
	private PDFont font ; 
	
	private float fontSize ; 
	
	protected FontMgr fontMgr = new FontMgr(); 
	
	private Color color ; 
	
	Quill() { 
		setFont(); 
	}
	
	/**
	 * Copy constructor. 
	 */
	private Quill(Quill old) { 
		this.font = old.font ; 
		this.fontSize = old.fontSize ; 
		this.fontMgr = new FontMgr(old.fontMgr); 
		this.color = old.color ; 
	}
	
	/**
	 * Creates a copy instance, and modifies the font fields. 
	 * <p>
	 * Null arguments are ignored. 
	 */
	public Quill copy(FontFamily family, FontStyle style, Float size) { 
		Quill quill = new Quill(this); 
		quill.font = this.font ; 
		quill.fontSize = this.fontSize ; 
		quill.fontMgr = new FontMgr(this.fontMgr).adjust(family, style, size); 
		quill.setFont(); 
		return quill ; 
	}
	
	/**
	 * Creates a copy instance, and modifies {@link #fontMgr}. 
	 */
	public Quill copyPlain() { Quill quill = new Quill(this); quill.fontMgr.style = PLAIN ; quill.setFont(); return quill ; } 
	
	/**
	 * Creates a copy instance, and modifies {@link #fontMgr}. 
	 */
	public Quill copyBold() { Quill quill = new Quill(this); quill.fontMgr.style = BOLD ; quill.setFont(); return quill ; } 
	
	/**
	 * Creates a copy instance, and modifies {@link #fontMgr}. 
	 */
	public Quill copyItalic() { Quill quill = new Quill(this); quill.fontMgr.style = ITALIC ; quill.setFont(); return quill ; } 
	
	/**
	 * Creates a copy instance, and modifies {@link #fontMgr}. 
	 */
	public Quill copyBoldItalic() { Quill quill = new Quill(this); quill.fontMgr.style = BOLD_ITALIC ; quill.setFont(); return quill ; } 
	
	/**
	 * Creates a copy instance, and modifies the font size. 
	 */
	public Quill copySize(float newSize) { Quill quill = new Quill(this); quill.fontMgr.size = newSize ; quill.setFont(); return quill ; } 
	
	public PDFont getFont() {
		return font;
	}

	/**
	 * Sets the font, ignoring whatever is in {@link #fontMgr}. 
	 * Usually, client code would use {@link #fontMgr}. 
	 */
	public void setFont(PDFont font) { 
		this.font = font;
	}
	
	public float getFontSize() {
		return fontSize;
	}

	/**
	 * Sets the font size, ignoring whatever is in {@link #fontMgr}. 
	 * Usually, client code would use {@link #fontMgr}. 
	 */
	public void setFontSize(float fontSize) {
		this.fontSize = fontSize;
	}
	
	/**
	 * Sets {@link #font} and {@link #fontSize} from {@link #fontMgr}. 
	 */
	public void setFont() { 
		this.font = fontMgr.family.getStyleFont(fontMgr.style); 
		this.fontSize = fontMgr.size ; 
	}
	
	public Color getColor() {
		return color;
	}

	public void setColor(Color color) {
		this.color = color;
	}

	public Quill copy(Color color) { 
		Quill quill = new Quill(this); 
		quill.color = color ; 
		return quill ; 
	}
	
	public double getStringWidth(String str) throws IOException { 
		return getStringWidth(str, font, fontSize); 
	}
	
	public static double getStringWidth(String text, PDFont font, float fontSize) throws IOException {
		return font.getStringWidth(text) / 1000 * fontSize;
	}

	public double getFontHeight() { 
		return font.getFontDescriptor().getFontBoundingBox().getHeight() / 1000 * fontSize ; 
	}
	
	// TODO The FontMgr, FontFamily and FontStyle classes should extend Layout.NamedConstant. . 
	
	/**
	 * Holder for {@linkplain Quill} font configuration. 
	 */
	public static class FontMgr { 
		private FontFamily family ; 
		private FontStyle style ; 
		private float size ; 
		FontMgr() { 
			this.family = DEFAULT_FONT_FAMILY ; 
			this.style = DEFAULT_FONT_STYLE ; 
			this.size = DEFAULT_FONT_SIZE ; 
		}
		FontMgr(FontMgr templateFont) { 
			this.family = templateFont.family ; 
			this.style = templateFont.style ; 
			this.size = templateFont.size ; 
		}
		public FontMgr adjust(FontFamily family, FontStyle style, Float size) { 
			if (family!=null) this.family = family ; 
			if (style!=null) this.style = style ; 
			if (size!=null) this.size = size ; 
			return this ; 
		}
	}
	
	/**
	 * Font-family information needed by {@link FontMgr}. 
	 */
	public static class FontFamily { 
		public final PDType1Font plain ; 
		public final PDType1Font bold ; 
		public final PDType1Font italic ; 
		public final PDType1Font boldItalic ; 
		FontFamily(PDType1Font plain, PDType1Font bold, PDType1Font italic, PDType1Font boldItalic) {
			this.plain = plain ; 
			this.bold = bold ; 
			this.italic = italic ; 
			this.boldItalic = boldItalic ; 
		}
		public PDFont getStyleFont(FontStyle style) {
			if (style==Quill.PLAIN) return plain ; 
			if (style==Quill.BOLD) return bold ; 
			if (style==Quill.ITALIC) return italic ; 
			if (style==Quill.BOLD_ITALIC) return boldItalic ; 
			return plain ; // Oops. But need to return something. 
		}
	}
	
	/**
	 * Object-constants for styles within a font-family. 
	 * <p>
	 * The default implementation of {@link FontFamily} understands four different styles: plain, bold, italic, bold-italic. 
	 */
	public static class FontStyle {} 

}
