package org.blockframe.painters;

import java.io.IOException;

import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.blockframe.core.Block;
import org.blockframe.core.Canvas;
import org.blockframe.core.Layout;
import org.blockframe.core.Quill;


/**
 * This class does not draw exact circles, but it does draw curvey blobs that will serve for many purposes. 
 * Specifically, it draws quadrants, and each quadrant is a cubic Bezier curve. 
 * <p>
 * If you must have a perfect circle, you may need to trace it out yourself. 
 * Alternatively, you could use a circle glyph from a font, but remember fonts are likely to be "smart" about placing glyphs, with ascent-heights, descenders and whatever. 
 * <p>
 * All coordinates are based on a bottom-left set of axes, as per PDF-Box. 
 */
public class BezierCirclePainter extends PathPainter { 
	
	public BezierCirclePainter(Canvas canvas) { 
		super(canvas); 
	}
	
	/**
	 * Traces a quadrant drawn with a cubic Bezier spline. 
	 * Only the circumference is traced, not the radial lines. 
	 * This merely extends an existing path. It does not {@link PDPageContentStream#moveTo(float, float)} the beginning, or close the path. 
	 * The quadrant is traced clockwise. 
	 * <p>
	 * The coordinates are centred at the bottom left: PDF coordinates. The Y-coordinate increases upwards. 
	 * <p>
	 * There are two Bezier control points. Imagine a rectangle with one corner at the corner of the quadrant, and the opposite sides tangential to the extremes of the curve - that is, the bounding box of the curve. 
	 * One control point is on the opposite horizontal side, the other on the opposite vertical side. 
	 * 
	 * @param horizontalSignedRadius If positive, the curve is on the right and the corner point on the left. If negative, the curve is on the left. 
	 * @param verticalSignedRadius If positive, the curve is above the corner point. If negative, the curve is below. 
	 * @param horizontalBezierFactor If zero, the Bezier control point on the horizontal side is at <code>centreX</code>, creating a curve that falls in from the radius quickly. 
	 * If one, the Bezier control point is at the far corner of the bounding box, creating a curve that pushes out to the far corner. 
	 * @param verticalBezierFactor Similar to <code>horizontalBezierFactor</code>, but lying on the opposite vertical side. 
	 */
	public void appendBezierQuarterCircle(float centreX, float centreY, float horizontalSignedRadius, float verticalSignedRadius, float horizontalBezierFactor, float verticalBezierFactor) throws IOException { 
		float extremeX = centreX+horizontalSignedRadius;
		float extremeY = centreY+verticalSignedRadius;
		float bezier1X = centreX + horizontalBezierFactor * horizontalSignedRadius ; 
		float bezier2Y = centreY + verticalBezierFactor * verticalSignedRadius ; 
		if (verticalSignedRadius*horizontalSignedRadius>0) { 
			/* Here we know: this is a top-right or bottom-left quadrant. Clockwise starts at the vertical-extreme and horizontal-centre. */
			stream.curveTo(bezier1X, extremeY, extremeX, bezier2Y, extremeX, centreY); 
		} else { 
			/* Here we know: this is a bottom-right or top-left quadrant. Clockwise starts at the horizontal-extreme and vertical-centre. */
			stream.curveTo(extremeX, bezier2Y, bezier1X, extremeY, centreX, extremeY); 
		}
	}
	
	/**
	 * Traces a quadrant of a circle, approximately, using a cubic Bezier spline. 
	 * The path includes the curve and the two radiuses. The path is closed. 
	 * <p>
	 * This wraps two calls to {@link #appendBezierQuarterCircle(float, float, float, float, float, float)}. 
	 * The arguments are documented there. 
	 */
	public void traceBezierQuadrant(float centreX, float centreY, float horizontalSignedRadius, float verticalSignedRadius, float horizontalBezierFactor, float verticalBezierFactor) throws IOException { 
		float extremeX = centreX+horizontalSignedRadius;
		float extremeY = centreY+verticalSignedRadius;
		float bezier1X = centreX + horizontalBezierFactor * horizontalSignedRadius ; 
		float bezier2Y = centreY + verticalBezierFactor * verticalSignedRadius ; 
		if (verticalSignedRadius*horizontalSignedRadius>0) { 
			/* Here we know: this is a top-right or bottom-left quadrant. Clockwise starts at the vertical-extreme and horizontal-centre. */
			stream.moveTo(centreX, extremeY); 
		} else { 
			/* Here we know: this is a bottom-right or top-left quadrant. Clockwise starts at the horizontal-extreme and vertical-centre. */
			stream.moveTo(extremeX, centreY); 
		}
		appendBezierQuarterCircle(centreX, centreY, horizontalSignedRadius, verticalSignedRadius, horizontalBezierFactor, verticalBezierFactor); 
		stream.lineTo(centreX, centreY); 
		stream.closePath(); 
	}
	
	/**
	 * Traces a half circle, approximately, using cubic Bezier splines. The path includes the diagonal. The path is closed. 
	 * <p>
	 * This wraps two calls to {@link #appendBezierQuarterCircle(float, float, float, float, float, float)}. 
	 * The arguments are documented there. 
	 */
	public void traceBezierRightHalf(float left, float bottom, float width, float height, float horizontalBezierFactor, float verticalBezierFactor) throws IOException { 
		float centreX = left ; 
		float centreY = bottom + height / 2 ; 
		float verticalRadius = height / 2 ; 
		float horizontalSignedRadius = width ; 
		stream.moveTo(left, bottom+height); 
		appendBezierQuarterCircle(centreX, centreY, horizontalSignedRadius, verticalRadius, horizontalBezierFactor, verticalBezierFactor); 
		appendBezierQuarterCircle(centreX, centreY, horizontalSignedRadius, -verticalRadius, horizontalBezierFactor, verticalBezierFactor); 
		stream.closePath(); 
	}

	/**
	 * Traces a half circle, approximately, using cubic Bezier splines. The path includes the diagonal. The path is closed. 
	 * <p>
	 * This wraps two calls to {@link #appendBezierQuarterCircle(float, float, float, float, float, float)}. 
	 * The arguments are documented there. 
	 */
	public void traceBezierLeftHalf(float left, float bottom, float width, float height, float horizontalBezierFactor, float verticalBezierFactor) throws IOException { 
		float centreX = left+width ; 
		float centreY = bottom + height / 2 ; 
		float verticalRadius = height / 2 ; 
		float horizontalSignedRadius = -width ; 
		stream.moveTo(centreX, bottom); 
		appendBezierQuarterCircle(centreX, centreY, horizontalSignedRadius, -verticalRadius, horizontalBezierFactor, verticalBezierFactor); 
		appendBezierQuarterCircle(centreX, centreY, horizontalSignedRadius, verticalRadius, horizontalBezierFactor, verticalBezierFactor); 
		stream.closePath(); 
	}

	/**
	 * Traces a half circle, approximately, using cubic Bezier splines. The path includes the diagonal. The path is closed. 
	 * <p>
	 * This wraps two calls to {@link #appendBezierQuarterCircle(float, float, float, float, float, float)}. 
	 * The arguments are documented there. 
	 */
	public void traceBezierTopHalf(float left, float bottom, float width, float height, float horizontalBezierFactor, float verticalBezierFactor) throws IOException { 
		float centreX = left + width / 2 ; 
		float centreY = bottom ; 
		float verticalSignedRadius = height ; 
		float horizontalRadius = width / 2 ; 
		stream.moveTo(left, bottom); 
		appendBezierQuarterCircle(centreX, centreY, -horizontalRadius, verticalSignedRadius, horizontalBezierFactor, verticalBezierFactor); 
		appendBezierQuarterCircle(centreX, centreY, horizontalRadius, verticalSignedRadius, horizontalBezierFactor, verticalBezierFactor); 
		stream.closePath(); 
	}

	/**
	 * Traces a half circle, approximately, using cubic Bezier splines. The path includes the diagonal. The path is closed. 
	 * <p>
	 * This wraps two calls to {@link #appendBezierQuarterCircle(float, float, float, float, float, float)}. 
	 * The arguments are documented there. 
	 */
	public void traceBezierBottomHalf(float left, float bottom, float width, float height, float horizontalBezierFactor, float verticalBezierFactor) throws IOException { 
		float centreX = left + width / 2 ; 
		float centreY = bottom + height ; 
		float verticalSignedRadius = - height ; 
		float horizontalRadius = width / 2 ; 
		stream.moveTo(left+width, bottom+height); 
		appendBezierQuarterCircle(centreX, centreY, horizontalRadius, verticalSignedRadius, horizontalBezierFactor, verticalBezierFactor); 
		appendBezierQuarterCircle(centreX, centreY, -horizontalRadius, verticalSignedRadius, horizontalBezierFactor, verticalBezierFactor); 
		stream.closePath(); 
	}

	/**
	 * Traces a half circle, approximately, using cubic Bezier splines. The path includes the diagonal. The path is closed. 
	 * <p>
	 * This wraps four calls to {@link #appendBezierQuarterCircle(float, float, float, float, float, float)}. 
	 * The arguments are documented there. 
	 */
	public void traceBezierCircle(float left, float bottom, float width, float height, float horizontalBezierFactor, float verticalBezierFactor) throws IOException { 
		float centreX = left + width / 2 ; 
		float centreY = bottom + height / 2 ; 
		float verticalRadius = height / 2 ; 
		float horizontalRadius = width / 2 ; 
		stream.moveTo(centreX, centreY+verticalRadius); 
		appendBezierQuarterCircle(centreX, centreY, horizontalRadius, verticalRadius, horizontalBezierFactor, verticalBezierFactor); 
		appendBezierQuarterCircle(centreX, centreY, horizontalRadius, -verticalRadius, horizontalBezierFactor, verticalBezierFactor); 
		appendBezierQuarterCircle(centreX, centreY, -horizontalRadius, -verticalRadius, horizontalBezierFactor, verticalBezierFactor); 
		appendBezierQuarterCircle(centreX, centreY, -horizontalRadius, verticalRadius, horizontalBezierFactor, verticalBezierFactor); 
		stream.closePath(); 
	}
	
}
