/**
 * BlockFrame is a layer on top of PDF-Box. Essentially, PDF-Box knows <em>how</em> to write content to a PDF, but not <em>where</em>. 
 * BlockFrame fills this gap. It measures and positions content, then writes a PDF file using PDF-Box. 
 * <p>
 * BlockFrame has been developed for highly structured documents. For example, I've used it to layout and draw the grid and clues for a crossword. 
 * It is designed to be extensible. 
 * For a complex data structure, many of the component types will have a corresponding {@link Block} subclass to extract and format the content for that component. 
 * For example, look at the cell-types in {@link org.blockframe.examples.G_Table}. 
 * <p>
 * BlockFrame exposes the underlying PDF-Box objects, and much of the internal workings of BlockFrame itself. 
 * <p>
 * The <code>examples</code> package contains several example programs, as a tutorial and demonstration of BlockFrame. 
 * <p>
 * Text documents have many human-centric conventions - for example, indented first lines of paragraphs, page headers, footnotes. 
 * These can be done using BlockFrame, but there are currently no tools to make it simple. (If there is sufficient interest in BlockFrame, I will add a text-oriented package.)
 * If you are generating a text document, you might like to look at Ralf Stuckert's PDFBox-Layout, which is currently available on GitHub. 
 * (Also: http://hardmockcafe.blogspot.com.au/2016/04/pdf-text-layout-made-easy-with-pdfbox_17.html). 
 * <p>
 * BlockFrame uses coordinates based at the top-left, even though PDF uses coordinates based on the bottom-left of the page. 
 * Why? BlockFrame is Anglo-centric. 
 * It's easier (ie, less error prone) to code for an Anglo reading order with the origin at the top left. 
 * BlockFrame coordinates are <code>double</code>, whereas PDF-Box coordinates are <code>float</code>. 
 * <p>
 * Units of measurement are the same as PDF-Box. 
 */
package org.blockframe;