package kz.report.dev.utils.ExcelUtils;

import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.Workbook;

public class FontBuilder {

    private Workbook workbook;
    private String fontName;
    private short fontHeight;
    private boolean isItalic;
    private boolean isBold;


    public FontBuilder(Workbook workbook) {
        this.workbook = workbook;
    }

    public FontBuilder withFontName(String fontName) {
        this.fontName = fontName;
        return this;
    }

    public FontBuilder withFontHeight(short fontHeight) {
        this.fontHeight = fontHeight;
        return this;
    }

    public FontBuilder setItalic() {
        this.isItalic = true;
        return this;
    }

    public FontBuilder resetItalic() {
        this.isItalic = false;
        return this;
    }

    public FontBuilder setBold() {
        this.isBold = true;
        return this;
    }

    public FontBuilder resetBold() {
        this.isBold = false;
        return this;
    }

    public FontBuilder from(Font from) {
        this.fontName = from.getFontName();
        this.isItalic = from.getItalic();
        this.fontHeight = from.getFontHeightInPoints();
        this.isBold = from.getBold();
        return this;
    }

    public Font build() {
        Font font = workbook.createFont();
        if (fontName != null && (!fontName.isEmpty())) {
            font.setFontName(fontName);
        }
        if (fontHeight >0) {
            font.setFontHeightInPoints(fontHeight);
        }
        if (isItalic) {
            font.setItalic(true);
        }
        if (isBold) {
            font.setBold(true);
        }
        return font;
    }


}
