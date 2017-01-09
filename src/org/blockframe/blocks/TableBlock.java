package org.blockframe.blocks;

import java.awt.Color;
import java.io.IOException;

import org.blockframe.core.Block;
import org.blockframe.core.Canvas;
import org.blockframe.core.DebugLog;
import org.blockframe.core.Layout;
import org.blockframe.core.Quill;
import org.blockframe.core.Layout.Alignment;
import org.blockframe.core.Layout.Justification;
import org.blockframe.painters.Scribe;


/**
 * Displays a table - rows and columns of cells, with cell-borders and table borders. 
 */
public abstract class TableBlock extends Block implements DebugLog.Verbosity { 
	
	public final int rowCount ; 
	
	public final int columnCount ; 

	/**
	 * Table cell {@linkplain Block} instances, first index is row, second is column. 
	 */
	public final Block[][] blockRows ; 
	
	/**
	 * Table cell {@linkplain PlacedBlock} instances, first index is row, second is column. 
	 */
	public final PlacedBlock[][] placedBlockRows ; 
	
	protected PlacedBlock placedTable ; 
	
	/**
	 * The top of the cell, inside border, outside padding. 
	 */
	public final double[] rowTops ; 
	
	/**
	 * The bottom of the cell, inside border, outside padding. 
	 */
	public final double[] rowBottoms ; 
	
	/**
	 * The left of the cell, inside border, outside padding. 
	 */
	public final double[] columnLefts ; 
	
	/**
	 * The right of the cell, inside border, outside padding. 
	 */
	public final double[] columnRights ; 
	
	public static final double borderMinWidth = 0.5 ;
	
	private Color borderColor ; 

	/**
	 * Constructor. 
	 * Note that {@link TableBlock} does not use a {@link BlockSource}. It gets its child blocks using the {@link #getCellBlock(int, int)} method. 
	 * @param rowCount
	 * @param columnCount
	 */
	protected TableBlock(int rowCount, int columnCount) { 
		super(); 
		this.rowCount = rowCount ; 
		this.columnCount = columnCount ; 
		this.blockRows = new Block[rowCount][] ; 
		this.placedBlockRows = new PlacedBlock[rowCount][] ; 
		for (int row=0 ; row<rowCount ; row++) blockRows[row] = new Block[columnCount] ; 
		for (int row=0 ; row<rowCount ; row++) placedBlockRows[row] = new PlacedBlock[columnCount] ; 
		this.rowTops = new double[rowCount] ; 
		this.rowBottoms = new double[rowCount] ; 
		this.columnLefts = new double[columnCount] ; 
		this.columnRights = new double[columnCount] ; 
	}
	
	public Color getBorderColor() {
		return borderColor;
	}

	public void setBorderColor(Color borderColor) {
		this.borderColor = borderColor;
	}

	protected void fillCellArrays() { 
		for (int row=0 ; row<rowCount ; row++) { 
			for (int column=0 ; column<columnCount ; column++) { 
				Block cellBlock = getCellBlock(row, column); 
				blockRows[row][column] = cellBlock ; 
			}
		}
	}

	/**
	 * Returns the {@link Block} used to draw the content of the given cell. 
	 * The block excludes the borders of the cell, and does not need to fill the interior of the cell. 
	 */
	protected abstract Block getCellBlock(int row, int column); 
	
	/**
	 * Return left-right justification of content within cell.
	 * Base class returns {@linkplain Layout#CENTRE_H}. 
	 */
	protected Justification getHorizontalJustification(int row, int column) { 
		return Layout.CENTRE_H ;
	}
	
	/**
	 * Returns vertical alignment of content within cell. 
	 * Base class returns {@linkplain Layout#CENTRE_V}. 
	 */
	protected Alignment getVerticalAlignment(int row, int column) {
		return Layout.CENTRE_V ;
	}
	
	/**
	 * Returns the thickness of the outer borders of the table. 
	 * @param isSide True if this is the left border or right border (ie vertical line), false if it's the top or bottom border (ie horizontal line). 
	 * @param isBefore True if this is left or above the table, false if it's to the right or below. 
	 */
	protected double getTableBorderWidth(boolean isSide, boolean isBefore) { 
		return Math.max(borderMinWidth, quill.getFontSize() / 32);
	}
	
	/**
	 * Returns the thickness of the cell border to the left, or above, of a cell. 
	 * <p>
	 * Typically, this is called once per row (and applies to the whole row), and once per column (and applies to the whole column). 
	 * So, if <code>isLeft</code> is true, implementations can probably ignore the value of <code>row</code>. Likewise, if <code>false</code>, probably ignore <code>column</code>. 
	 * <p>
	 * Implementations should handle just-out-of-range values for <code>row</code> and </code>column</code>. 
	 * 
	 * @param index Either the row or column index, depending on <code>isSide</code>: <code>true</code> means column index, otherwise the row index. 
	 */
	protected double getCellBorderWidth(int index, boolean isSide) { 
		return getTableBorderWidth(isSide, true); 
	}
	
	protected double getCellPadding(int row, int column, boolean isSide, boolean isBefore) { 
		return quill.getFontSize() / 2 ; 
	}
	
	/**
	 * <code>X</code>-coordinate of the corner of the cell, touching the border and outside any padding. 
	 */
	public final double getCellBorderCornerX(int column, boolean isLeft) { 
		if (isLeft) { 
			return columnLefts[column] ; 
		} else { 
			return columnRights[column] ; 
		}
	}
	
	/**
	 * <code>Y</code>-coordinate of the corner of the cell, touching the border and outside any padding. 
	 */
	public final double getCellBorderCornerY(int row, boolean isTop) { 
		if (isTop) { 
			return rowTops[row] ; 
		} else { 
			return rowBottoms[row] ; 
		}
	}
	
	@Override
	public PlacedBlock fill(Quill receivedQuill, Layout receivedLayout) throws IOException { 
		DebugLog.add(ENTERING_5, this, null, logMessage_enteringFill, null, null, true); 
		this.quill = inheritQuill(receivedQuill) ; 
		Layout tableLayout = inheritLayout(receivedLayout); // Layout object for whole table. Should not be modified during this method. 
		Layout cellLayout = tableLayout.copy(); // Layout object passed into cells. Might be modified by calls to inheritCellLayout().
		DebugLog.add(DETAIL_8, this, null, Layout.logMessage_layout, tableLayout, null, false); 
		//////  Invoke a 'fill' pass on each cell. 
		fillCellArrays(); 
		//////  Measure, and internally layout, each cell 
		for (int row=0 ; row<rowCount ; row++) { 
			for (int column=0 ; column<columnCount ; column++) { 
				Block cellBlock = blockRows[row][column]; 
				PlacedBlock cellPlacedBlock = cellBlock.fill(quill, inheritCellLayout(cellLayout, tableLayout, this, row, column)); 
				placedBlockRows[row][column] = cellPlacedBlock ; 
			}
		}
		//////  Compute row heights, tops, and bottoms. 
		double rowTop = 0 ; 
		for (int row=0 ; row<rowCount ; row++) { 
			rowTop += row==0 ? getTableBorderWidth(false, true) : getCellBorderWidth(row, false); 
			double rowHeight = 0 ; 
			for (int column=0 ; column<columnCount ; column++) { 
				double cellHeight = placedBlockRows[row][column].getHeight() + getCellPadding(row, column, false, true) + getCellPadding(row, column, false, false); 
				if (cellHeight>rowHeight) rowHeight = cellHeight ; 
			}
			this.rowTops[row] = rowTop ; 
			this.rowBottoms[row] = rowTop + rowHeight ; 
			//// Prepare for next row
			rowTop += rowHeight ; 
		}
		double tableHeight = rowBottoms[rowCount-1] + getTableBorderWidth(false, false); 
		//////  Compute column widths, lefts, and rights. 
		double columnLeft = 0 ; 
		for (int column=0 ; column<columnCount ; column++) { 
			columnLeft += column==0 ? getTableBorderWidth(true, true) : getCellBorderWidth(column, true); 
			double columnWidth = 0 ; 
			for (int row=0 ; row<rowCount ; row++) { 
				double cellWidth = placedBlockRows[row][column].getWidth() + getCellPadding(row, column, true, true) + getCellPadding(row, column, true, false); 
				if (cellWidth>columnWidth) columnWidth = cellWidth ; 
			}
			this.columnLefts[column] = columnLeft ; 
			this.columnRights[column] = columnLeft + columnWidth ; 
			//// Prepare for next column
			columnLeft += columnWidth ; 
		}
		double tableWidth = columnRights[columnCount-1] + getTableBorderWidth(true, false); 
		//////  Set position of cell blocks
		for (int row=0 ; row<rowCount ; row++) { 
			for (int column=0 ; column<columnCount ; column++) { 
				placedBlockRows[row][column].setOffsetInContainer(columnLefts[column], rowTops[row]); 
			}
		}
		//////  Set my dimensions
		PlacedBlock placedBlock = new PlacedBlock(); 
		placedBlock.setDimensions(tableWidth, tableHeight); 
		DebugLog.add(LEAVING_6, placedBlock, null, logMessage_leavingFill, null, null, false); 
		//////  Bye bye
		this.placedTable = placedBlock ; 
		return placedBlock ; 
	}
	
	/**
	 * Computes, or passes through, the {@linkplain} Layout} object to be used in the given cell. 
	 * <p>
	 * This implementation merely passes through the layout passed into the previous, which is initially copied from the table's layout. 
	 * <p>
	 * This implementation does a raster scan through the cells: On the top row from left to right, then each row downwards.  
	 */
	protected Layout inheritCellLayout(Layout prevCellLayout, Layout tableLayout, TableBlock tableBlock, int row, int column) { 
		return prevCellLayout ; 
	}
	
	@Override
	public void draw(Canvas canvas, double left, double top, double width, double height) throws IOException { 
		//////  Draw cell contents
		for (int row=0 ; row<rowCount ; row++) { 
			for (int column=0 ; column<columnCount ; column++) { 
				PlacedBlock cellPlacedBlock = placedBlockRows[row][column]; 
				//// Compute boundaries of content - ie, inside the padding. 
				double cellInteriorLeft = columnLefts[column] + getCellPadding(row, column, true, true); // Left boundary of content of cell (ie, inside the padding). 
				double cellInteriorRight = columnRights[column] - getCellPadding(row, column, true, false); 
				double cellInteriorTop = rowTops[row] + getCellPadding(row, column, false, true);
				double cellInteriorBottom = rowBottoms[row] - getCellPadding(row, column, false, false);
				//// Compute offsets to implement the required justification (cell content may be smaller than the cell interior)
				Justification horizontalJustification = getHorizontalJustification(row, column); 
				Alignment verticalAlignment = getVerticalAlignment(row, column); 
				double contentLeftOffset = cellInteriorLeft + computeJustificationOffset(horizontalJustification, cellInteriorRight-cellInteriorLeft, cellPlacedBlock.getWidth()); 
				double contentTopOffset = cellInteriorTop + computeAlignmentOffset(verticalAlignment, cellInteriorBottom-cellInteriorTop, cellPlacedBlock.getHeight()); 
				//// Draw the content
				cellPlacedBlock.draw(canvas, left+contentLeftOffset, top+contentTopOffset); 
			}
		}
		//////  Draw table borders and corners
		drawBorders(canvas, left, top);
	}
	
	/**
	 * Computes the gap from the inside of the left padding, to the left of the content block. 
	 */
	protected double computeJustificationOffset(Justification justification, double containerSize, double contentSize) { 
		double gap = containerSize - contentSize ; 
		if (justification==Layout.LEFT) { 
			return 0 ; 
		} else if (justification==Layout.RIGHT) { 
			return gap ; 
		} else { 
			return gap / 2 ; 
		}
	}
	
	/**
	 * Computes the gap from the inside of the top padding, to the top of the content block. 
	 */
	protected double computeAlignmentOffset(Alignment alignment, double containerSize, double contentSize) { 
		double gap = containerSize - contentSize ; 
		if (alignment==Layout.TOP) { 
			return 0 ; 
		} else if (alignment==Layout.BOTTOM) { 
			return gap ; 
		} else { 
			return gap / 2 ; 
		}
	}

	/**
	 * Draw the internal and external borders of the table. 
	 * <p>
	 * This implementation delegates to {@link #drawTableBorder(Canvas, double, double, boolean, boolean)}, {@link #drawTableCorner(Canvas, double, double, boolean, boolean)}, 
	 * {@link #drawRowTopBorder(Canvas, double, double, int, boolean)} and {@link #drawColumnLeftBorder(Canvas, double, double, int, boolean)}. 
	 */
	public void drawBorders(Canvas canvas, double tableLeft, double tableTop) throws IOException {
		//////  Draw outer borders of table
		drawTableBorder(canvas, tableLeft, tableTop, false, false); 
		drawTableBorder(canvas, tableLeft, tableTop, false, true); 
		drawTableBorder(canvas, tableLeft, tableTop, true, false); 
		drawTableBorder(canvas, tableLeft, tableTop, true, true); 
		drawTableCorner(canvas, tableLeft, tableTop, false, false); 
		drawTableCorner(canvas, tableLeft, tableTop, false, true); 
		drawTableCorner(canvas, tableLeft, tableTop, true, false); 
		drawTableCorner(canvas, tableLeft, tableTop, true, true); 
		//////  Draw internal borders 
		for (int row=1 ; row<rowCount ; row++) { 
			drawRowTopBorder(canvas, tableLeft, tableTop, row, true); 
		}
		for (int column=1 ; column<columnCount ; column++) { 
			drawColumnLeftBorder(canvas, tableLeft, tableTop, column, false); 
		}
	}
	
	/**
	 * Draws one of the four borders of the table, excluding the corners. 
	 * <p>
	 * This implementation delegates to {@link #drawTableBorderAtRow(Canvas, double, double, int, boolean)}, {@link #drawTableBorderAtColumn(Canvas, double, double, int, boolean)}, 
	 * {@link #drawIntersticeLeftTop(Canvas, double, double, int, int)}. 
	 * 
	 * @param isSide True if this is the left border or right border (ie vertical line), false if it's the top or bottom border (ie horizontal line). 
	 * @param isBefore True if this is left or above the table, false if it's to the right or below. 
	 */
	protected void drawTableBorder(Canvas canvas, double tableLeft, double tableTop, boolean isSide, boolean isBefore) throws IOException { 
		if (isSide) { 
			for (int row=0 ; row<rowCount ; row++) { 
				drawTableBorderAtRow(canvas, tableLeft, tableTop, row, isBefore); 
				if (0<row&&row<rowCount) { 
					drawIntersticeLeftTop(canvas, tableLeft, tableTop, row, 0); 
					drawIntersticeLeftTop(canvas, tableLeft, tableTop, row, columnCount); 
				}
			}
		} else { 
			for (int column=0 ; column<columnCount ; column++) { 
				drawTableBorderAtColumn(canvas, tableLeft, tableTop, column, isBefore); 
				if (0<column&&column<columnCount) { 
					drawIntersticeLeftTop(canvas, tableLeft, tableTop, 0, column); 
					drawIntersticeLeftTop(canvas, tableLeft, tableTop, rowCount, column); 
				}
			}
		}
	}
	
	/**
	 * Draws a section of the table border, beside one row. 
	 * <p>
	 * This implementation delegates to {@link #drawBorderSegment(Canvas, double, double, double, double)}. 
	 */
	protected void drawTableBorderAtRow(Canvas canvas, double tableLeft, double tableTop, int row, boolean isLeft) throws IOException { 
		if (isLeft) { 
			drawBorderSegment(canvas, tableLeft+0, tableTop+rowTops[row], tableLeft+columnLefts[0], tableTop+rowBottoms[row]); 
		} else { 
			drawBorderSegment(canvas, tableLeft+columnRights[columnCount-1], tableTop+rowTops[row], tableLeft+placedTable.getWidth(), tableTop+rowBottoms[row]); 
		}
	}

	/**
	 * Draws a section of the table border, above or below one column. 
	 * <p>
	 * This implementation delegates to {@link #drawBorderSegment(Canvas, double, double, double, double)}. 
	 */
	protected void drawTableBorderAtColumn(Canvas canvas, double tableLeft, double tableTop, int column, boolean isTop) throws IOException {  
		if (isTop) { 
			drawBorderSegment(canvas, tableLeft+columnLefts[column], tableTop+0, tableLeft+columnRights[column], tableTop+rowTops[0]); 
		} else { 
			drawBorderSegment(canvas, tableLeft+columnLefts[column], tableTop+rowBottoms[rowCount-1], tableLeft+columnRights[column], tableTop+placedTable.getHeight()); 
		}
	}
	
	/**
	 * Draws one of the four table corners. 
	 * <p>
	 * This implementation delegates to {@link #drawIntersticeLeftTop(Canvas, double, double, int, int)}. 
	 */
	protected void drawTableCorner(Canvas canvas, double tableLeft, double tableTop, boolean isLeft, boolean isTop) throws IOException { 
		int row = isTop ? 0 : rowCount ; 
		int column = isLeft ? 0 : columnCount ; 
		drawIntersticeLeftTop(canvas, tableLeft, tableTop, row, column); 
	}
	
	/**
	 * Draws borders between rows of cells, specifically rows <code>row-1</code> and <code>row</code>, optionally including the interstices between columns. 
	 * <p>
	 * This implementation delegates to {@link #drawCellTopBorder(Canvas, double, double, int, int)} and {@link #drawIntersticeLeftTop(Canvas, double, double, int, int)}. 
	 * 
	 * @param wantInterstices Whether to draw the interstices of the borders at the corners of the cells. 
	 * The {@link #drawBorders(Canvas, double, double)} method in this class calls this method with <code>true</code>. 
	 */
	protected void drawRowTopBorder(Canvas canvas, double tableLeft, double tableTop, int row, boolean wantInterstices) throws IOException { 
		for (int column=0 ; column<columnCount ; column++) { 
			drawCellTopBorder(canvas, tableLeft, tableTop, row, column); 
			if (wantInterstices && column>0) drawIntersticeLeftTop(canvas, tableLeft, tableTop, row, column); 
		}
	}
	
	/**
	 * Draws borders between columns of cells, specifically columns <code>column-1</code> and <code>column</code>, optionally including the interstices between rows. 
	 * <p>
	 * This implementation delegates to {@link #drawCellLeftBorder(Canvas, double, double, int, int)} and {@link #drawIntersticeLeftTop(Canvas, double, double, int, int)}. 
	 * @param wantInterstices Whether to draw the interstices of the borders at the corners of the cells. 
	 * The {@link #drawBorders(Canvas, double, double)} method in this class calls this method with <code>false</code>. 
	 */
	protected void drawColumnLeftBorder(Canvas canvas, double tableLeft, double tableTop, int column, boolean wantInterstices) throws IOException { 
		for (int row=0 ; row<rowCount ; row++) { 
			drawCellLeftBorder(canvas, tableLeft, tableTop, row, column); 
			if (wantInterstices && row>0) drawIntersticeLeftTop(canvas, tableLeft, tableTop, row, column); 
		}
	}
	
	/** 
	 * Draws an internal border between columns <code>column-1</code> and <code>column</code>, on row <code>row</code>. 
	 * <p>
	 * This implementation delegates to {@link #drawBorderSegment(Canvas, double, double, double, double)}. 
	 */
	protected void drawCellLeftBorder(Canvas canvas, double tableLeft, double tableTop, int row, int column) throws IOException { 
		try { 
			drawBorderSegment(canvas, tableLeft+columnRights[column-1], tableTop+rowTops[row], tableLeft+columnLefts[column], tableTop+rowBottoms[row]); 
		} catch (ArrayIndexOutOfBoundsException e) { 
			throw new ArrayIndexOutOfBoundsException("row is "+row+", column is "+column+" ; row, column counts are "+rowCount+", "+columnCount); 
		}
	}
	
	/** 
	 * Draws an internal border between rows <code>row-1</code> and <code>row+/code>, in column <code>column</code>. 
	 * <p>
	 * This implementation delegates to {@link #drawBorderSegment(Canvas, double, double, double, double)}. 
	 */
	protected void drawCellTopBorder(Canvas canvas, double tableLeft, double tableTop, int row, int column) throws IOException { 
		try { 
			drawBorderSegment(canvas, tableLeft+columnLefts[column], tableTop+rowBottoms[row-1], tableLeft+columnRights[column], tableTop+rowTops[row]); 
		} catch (ArrayIndexOutOfBoundsException e) { 
			throw new ArrayIndexOutOfBoundsException("row is "+row+", column is "+column+" ; row, column counts are "+rowCount+", "+columnCount); 
		}
	}
	
	/**
	 * Draws the interstices between cell corners, and also interstices within the table borders. 
	 * In this class, the corners of the table are drawn by {@link #drawTableCorner(Canvas, boolean, boolean)} rather than this method. 
	 * <p>
	 * This implementation delegates to {@link #drawBorderSegment(Canvas, double, double, double, double)}. 
	 * @param row May also be zero or row-count, to draw in the table border. 
	 * @param column May also be zero or column-count, to draw in the table border. 
	 */
	protected void drawIntersticeLeftTop(Canvas canvas, double tableLeft, double tableTop, int row, int column) throws IOException { 
		double offsetLeft = column > 0 ? columnRights[column-1] : 0 ;
		double offsetTop = row > 0 ? rowBottoms[row-1] : 0 ;
		double offsetRight = column < columnCount ? columnLefts[column] : placedTable.getWidth() ;
		double offsetBottom = row < rowCount ? rowTops[row] : placedTable.getHeight() ;
		drawBorderSegment(canvas, tableLeft+offsetLeft, tableTop+offsetTop, tableLeft+offsetRight, tableTop+offsetBottom); 
	}
	
	/**
	 * Draws a segment of a table or cell border. 
	 * <p>
	 * In this class, all the <code>drawXxxxBorder</code> methods ultimately call this method. 
	 * It is the only method in this class that makes a call into {@link Scribe} method. 
	 */
	protected void drawBorderSegment(Canvas canvas, double left, double top, double right, double bottom) throws IOException { 
		float pdfLeft = (float) left ; 
		float pdfWidth = (float) (right-left); 
		float pdfHeight = (float) (bottom-top);
		float pdfBottom = canvas.getPdfY(bottom); 
		Scribe.rect_lbwh(canvas, true, false, borderColor, pdfLeft, pdfBottom, pdfWidth, pdfHeight); 
	}

}
