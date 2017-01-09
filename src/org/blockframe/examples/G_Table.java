package org.blockframe.examples;

import java.io.File;
import java.io.IOException;

import org.blockframe.blocks.StringBlock;
import org.blockframe.blocks.StringBlockBold;
import org.blockframe.blocks.TableBlock;
import org.blockframe.core.Block;
import org.blockframe.core.Canvas;
import org.blockframe.core.DebugLog;
import org.blockframe.core.Layout;
import org.blockframe.core.PdfDocument;
import org.blockframe.core.Quill;
import org.blockframe.core.BlockPipe.BlockWriter;
import org.blockframe.core.Layout.Justification;
import org.blockframe.core.Quill.FontMgr;


/**
 * Demonstrates the {@link TableBlock} class, a subclass of {@link Block} for writing tables. 
 */
public class G_Table extends PdfDocument { 
	
	public static void main(String[] args) throws IOException { 
		new G_Table(); 
	}
	
	public G_Table() throws IOException { 
		
		/*
		 * Tables are constructed by subclassing TableBlock and overriding the methods. 
		 * 
		 * The content of the cells are supplied by overriding the 'getCellBlock' method. 
		 * In this table, getCellBlock returns different types for the top-left cell, cells in the row and column headers, and cells in the bulk of the table. 
		 * 
		 * The justification and alignment of the cell content are controlled by overriding the 'getHorizontalJustification' and 'getVerticalAlignment' methods. 
		 * In this table, getHorizontalJustification is overridden, but the default alignment is used. 
		 * 
		 * The borders between cells are drawn by a hierarchy of methods. 
		 * Each of the methods control the drawing of the borders at different granularities: an entire row, or the borders above / left of a particular cell. 
		 */
		write(new TimesTable()); 

		File file = new File(UtilsForExamples.getExamplesDir(), getClass().getSimpleName()+".pdf"); 
		writeFile(file); 
		DebugLog.out(); 
	}
	
	class TimesTable extends TableBlock { 
		
		/**
		 * Create a table from 1x1=1 to 10x10=100. 
		 */
		final static int maxNumber = 10 ; 
		
		final static int rowCount = maxNumber + 1 ; 
		
		final static int columnCount = maxNumber + 1 ; 
		
		protected TimesTable() {
			super(rowCount, columnCount); 
		}

		@Override
		protected Block getCellBlock(int row, int column) {
			if (row==0) { 
				if (column==0) { 
					return new TableTopLeftCell(); 
				} else { 
					return new TableFirstRowCell(column); 
				}
			} else { 
				if (column==0) { 
					return new TableFirstColumnCell(row); 
				} else { 
					return new TableBodyCell(row, column); 
				}
			}
		}
		
		@Override
		protected double getTableBorderWidth(boolean isSide, boolean isBefore) {
			return borderMinWidth ; 
		}

		@Override
		protected double getCellBorderWidth(int index, boolean isSide) { 
			double tableBorderWidth = getTableBorderWidth(isSide, true);
			if (index<=1) return tableBorderWidth ; 
			return Math.max(borderMinWidth, tableBorderWidth / 4); // Never used, unless the drawXxxxYyyyBorder methods are edited. 
		} 
		
		@Override
		protected Justification getHorizontalJustification(int row, int column) {
			return (column>0) ? Layout.RIGHT : Layout.CENTRE_H ; 
		}
		
		@Override
		protected void drawRowTopBorder(Canvas canvas, double tableLeft, double tableTop, int row, boolean wantInterstices) throws IOException {
			if (row<=1) super.drawRowTopBorder(canvas, tableLeft, tableTop, row, true);
		}
		
		@Override
		protected void drawColumnLeftBorder(Canvas canvas, double tableLeft, double tableTop, int column, boolean wantInterstices) throws IOException {
			if (column<=1) super.drawColumnLeftBorder(canvas, tableLeft, tableTop, column, true);
		}
		
		class TableTopLeftCell extends StringBlockBold { 
			public TableTopLeftCell() {
				super("Times"); 
			} 
		}
		
		class TableFirstRowCell extends StringBlockBold {
			public TableFirstRowCell(int column) {
				super(""+column); 
			} 
		}
		
		class TableFirstColumnCell extends StringBlockBold {
			public TableFirstColumnCell(int row) {
				super(""+row); 
			} 
		}
		
		class TableBodyCell extends StringBlock {
			public TableBodyCell(int row, int column) {
				super(""+(row*column)); 
			} 
		}
		
	}

}
