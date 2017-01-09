/**
 * The <code>core</code> package contains the central classes of BlockFrame. 
 * <p>
 * The <b>{@link org.blockframe.core.Block}</b> class is the base class for holding content. 
 * Its subclasses can hold content from as trivial as whitespace or a small segment of text (eg, {@link org.blockframe.blocks.SpacerWidth} and {@link org.blockframe.blocks.StringBlock}), up to complex multilayered containers (eg {@link org.blockframe.blocks.TableBlock}). 
 * <p>
 * The <b>{@link org.blockframe.core.Frame}</b> class is a subclass of {@link org.blockframe.core.Block}, which holds a flow of content. 
 * For example, a paragraph holds a flow of words, or a page holds a flow of paragraphs. 
 * In each page of a {@link org.blockframe.core.PdfDocument} or {@link org.blockframe.core.PdfChapter}, the root of the content tree is a {@link org.blockframe.core.Frame}. 
 * <p>
 * Most client applications creating a PDF document will subclass <b>{@link org.blockframe.core.PdfDocument}</b>, which in turn is a subclass of {@link org.blockframe.core.PdfChapter}. 
 * The client application will write content, ie {@link org.blockframe.core.Block} instances, 
 * into a {@link org.blockframe.core.PdfDocument} with the {@link org.blockframe.core.PdfChapter#write(Block)} method. 
 * <p>
 * The <b>{@link org.blockframe.core.Layout}</b> and <b>{@link org.blockframe.core.Quill}</b> classes hold format and appearance configuration. 
 * <p>
 * Once the content has been written to the {@link org.blockframe.core.PdfDocument}, there will be a tree of content. 
 * In a simple document, there will be a sequence of paragraphs at the top level. Each paragraph will contain a sequence of words. 
 * The tree will be traversed twice: first to measure and layout, then to write to the PDF document. 
 * <p>
 * Instances of {@link org.blockframe.core.Layout} and {@link org.blockframe.core.Quill} are passed down through the first of these traversals. 
 * Where the formatting should change, these instances are copied and modified, with the modified copies being passed down. 
 * <p>
 * The <b>{@link org.blockframe.core.Canvas}</b> class contains the objects required to draw to the PDF document. It is passed down through the second traversal. 
 * <p>
 * The <b>{@link org.blockframe.core.DebugLog}</b> class provides a configurable logging tool, intended for debugging. 
 */
package org.blockframe.core;



