package kz.report.dev.utils.ExcelUtils;

import org.apache.poi.ss.usermodel.*;

public class StyleBuilder {

    private Workbook workbook;
    private Font font;
    private boolean needToWrapText;
    private HorizontalAlignment alignment;
    private VerticalAlignment verticalAlignment;
    private BorderStyle rightBorder;
    private BorderStyle leftBorder;
    private BorderStyle topBorder;
    private BorderStyle bottomBorder;



    public StyleBuilder(Workbook workbook) {
        this.workbook = workbook;
    }

    public StyleBuilder withFont(Font font) {
        this.font = font;
        return this;
    }

    public StyleBuilder withAlignment(HorizontalAlignment horizontalAlignment) {
        this.alignment = horizontalAlignment;
        return this;
    }

    public StyleBuilder withVerticalAlignment(VerticalAlignment verticalAlignment) {
        this.verticalAlignment = verticalAlignment;
        return this;
    }

    public StyleBuilder withWrappedText() {
        this.needToWrapText = true;
        return this;
    }

    public StyleBuilder withRightBorder(BorderStyle style) {
        this.rightBorder = style;
        return this;
    }

    public StyleBuilder withLeftBorder(BorderStyle style) {
        this.leftBorder = style;
        return this;
    }

    public StyleBuilder withTopBorder(BorderStyle style) {
        this.topBorder = style;
        return this;
    }

    public StyleBuilder withBottomBorder(BorderStyle style) {
        this.bottomBorder = style;
        return this;
    }

    public StyleBuilder withoutBorder() {
        this.topBorder = null;
        this.rightBorder = null;
        this.bottomBorder = null;
        this.leftBorder = null;
        return this;
    }

    public StyleBuilder from(CellStyle cellStyle) {
        this.needToWrapText = cellStyle.getWrapText();
        this.verticalAlignment = cellStyle.getVerticalAlignmentEnum();
        this.alignment = cellStyle.getAlignmentEnum();
        this.rightBorder = cellStyle.getBorderRightEnum();
        this.leftBorder = cellStyle.getBorderLeftEnum();
        this.topBorder = cellStyle.getBorderTopEnum();
        this.bottomBorder = cellStyle.getBorderBottomEnum();
        return this;
    }

    public CellStyle build() {
        CellStyle cellStyle = workbook.createCellStyle();
        if (font != null) {
            cellStyle.setFont(font);
        }
        if (alignment != null) {
            cellStyle.setAlignment(alignment);
        }
        if (verticalAlignment != null) {
            cellStyle.setVerticalAlignment(verticalAlignment);
        }
        if (needToWrapText) {
            cellStyle.setWrapText(true);
        }
        if (rightBorder != null) {
            cellStyle.setBorderRight(rightBorder);
        }
        if (leftBorder != null) {
            cellStyle.setBorderLeft(leftBorder);
        }
        if (topBorder != null) {
            cellStyle.setBorderTop(topBorder);
        }
        if (bottomBorder != null) {
            cellStyle.setBorderBottom(bottomBorder);
        }
        return cellStyle;
    }
}
