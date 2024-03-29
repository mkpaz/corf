package corf.base.io;

import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.font.PDFont;

import java.io.IOException;
import java.util.Objects;

public class PdfFont {

    protected static final float LINE_SPACING = 1.5f;

    protected final PDFont font;
    protected float fontSize;
    protected float lineSpacing;

    public PdfFont(PDFont font, float fontSize) {
        this(font, fontSize, LINE_SPACING);
    }

    public PdfFont(PDFont font, float fontSize, float lineSpacing) {
        this.font = Objects.requireNonNull(font, "font");
        this.fontSize = fontSize;
        this.lineSpacing = lineSpacing;
    }

    public PDFont getPDFont() {
        return font;
    }

    public float getFontSize() {
        return fontSize;
    }

    public void setFontSize(float fontSize) {
        this.fontSize = fontSize;
    }

    public float getLineSpacing() {
        return lineSpacing;
    }

    public void setLineSpacing(float lineSpacing) {
        this.lineSpacing = lineSpacing;
    }

    public void applyToStream(PDPageContentStream stream) throws IOException {
        stream.setFont(font, fontSize);
        stream.setLeading(LINE_SPACING * fontSize);
    }

    public float getStringWidth(String s) throws IOException {
        return font.getStringWidth(s) / 1000 * fontSize;
    }

    public float getFontHeight() {
        return font.getFontDescriptor().getFontBoundingBox().getHeight() / 1000 * fontSize;
    }

    @Override
    public String toString() {
        return "PdfFont{" +
                "font=" + font +
                ", fontSize=" + fontSize +
                ", lineSpacing=" + lineSpacing +
                '}';
    }
}