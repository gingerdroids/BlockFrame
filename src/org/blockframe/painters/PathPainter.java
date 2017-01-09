package org.blockframe.painters;

import java.io.IOException;

import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.blockframe.core.Canvas;


/**
 * The {@linkplain PathPainter} subclasses trace out PDF paths, and invoke {@link PDPageContentStream#fill()} and {@link PDPageContentStream#stroke()}. 
 * <p>
 * By convention, methods which trace a complete path - a closed path including the initial {@link PDPageContentStream#moveTo(float, float)} call - are called <code>traceXxxx</code>. 
 * Methods which just append to an existing path are called <code>appendXxxx</code>. 
 * <p>
 * WARNING: This is an extremely thin base class, and currently only has one subclass. It may be subsumed into its subclass(es) in a future version. 
 */
public class PathPainter {
	
	protected final Canvas canvas;
	
	protected final PDPageContentStream stream;

	public PathPainter(Canvas canvas) {
		this.canvas = canvas ; 
		this.stream = canvas.stream ; 
	}
	
	/**
	 * Convenience method for {@link PDPageContentStream#closePath()}. 
	 */
	public void closePath() throws IOException { 
		stream.closePath(); 
	}

	/**
	 * Convenience method for {@link PDPageContentStream#fill()}. 
	 */
	public void fillPath() throws IOException { 
		stream.fill(); 
	}

	/**
	 * Convenience method for {@link PDPageContentStream#stroke()}. 
	 */
	public void strokePath() throws IOException { 
		stream.stroke(); 
	}

}
