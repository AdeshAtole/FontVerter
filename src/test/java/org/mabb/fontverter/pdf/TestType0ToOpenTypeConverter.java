/*
 * Copyright (C) Matthew Abboud 2016
 *
 * FontVerter is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * FontVerter is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with FontVerter. If not, see <http://www.gnu.org/licenses/>.
 */

package org.mabb.fontverter.pdf;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.font.PDFont;
import org.apache.pdfbox.pdmodel.font.PDType0Font;
import org.junit.After;
import org.junit.Assert;
import org.junit.Test;
import org.mabb.fontverter.FVFont;
import org.mabb.fontverter.TestUtils;
import org.mabb.fontverter.opentype.OpenTypeFont;
import org.mabb.fontverter.opentype.OtfNameConstants.RecordType;

import java.io.IOException;
import java.util.List;

public class TestType0ToOpenTypeConverter {
    private static PDDocument doc;

    public TestType0ToOpenTypeConverter() throws IOException {
        doc = PDDocument.load(TestUtils.readTestFile("pdf/HorariosMadrid_Segovia.pdf"));
    }

    @After
    public void cleanUp() throws IOException {
        doc.close();
    }

    @Test
    public void given_type0With_TTF_descendant_WithNoCmapTable_whenConverted_thenCmapTableCreated()
            throws IOException {
        PDFont rawType0Font = extractFont(doc, "UMAVUG+Garuda-Identity-H");

        FVFont font = PdfFontExtractor.convertType0FontToOpenType((PDType0Font) rawType0Font);
        font.normalize();
        OpenTypeFont otfFont = ((OpenTypeFont) font);

        Assert.assertNotNull(otfFont.getCmap());
        Assert.assertEquals(otfFont.getMxap().getNumGlyphs(), 69);
    }

    @Test
    public void given_type0_withTTF_withNoNameTable_whenConverted_thenHasNamesSet() throws IOException {
        PDFont rawType0Font = extractFont(doc, "UMAVUG+Garuda-Identity-H");

        FVFont font = PdfFontExtractor.convertType0FontToOpenType((PDType0Font) rawType0Font);
        font.normalize();
        OpenTypeFont otfFont = ((OpenTypeFont) font);

        Assert.assertEquals("UMAVUG+Garuda-Identity-H", otfFont.getNameTable().getName(RecordType.FULL_FONT_NAME));
        Assert.assertEquals("Garuda", otfFont.getNameTable().getName(RecordType.FONT_FAMILY));
        Assert.assertEquals("Normal", otfFont.getNameTable().getName(RecordType.FONT_SUB_FAMILY));
    }

    @Test
    public void given_type0ss() throws IOException {
        List<FVFont> fonts = extractFonts("pdf/brno30.pdf");
        Assert.assertNotNull(fonts);
    }

    private PDFont extractFont(PDDocument pdfFile, String name) throws IOException {
        PdfFontExtractor extractor = new PdfFontExtractor();
        List<PDFont> fonts = extractor.extractToPDFBoxFonts(pdfFile);
        return findFont(fonts, name);
    }

    private FVFont extractFont(String pdfFile, String name) throws IOException {
        PDDocument doc = PDDocument.load(TestUtils.readTestFile(pdfFile));

        PdfFontExtractor extractor = new PdfFontExtractor();
        List<PDFont> fonts = extractor.extractToPDFBoxFonts(doc);
        FVFont font = PdfFontExtractor.convertType0FontToOpenType((PDType0Font) findFont(fonts, name));
        font.normalize();
        return font;
    }

    private List<FVFont> extractFonts(String pdfFile) throws IOException {
        PDDocument doc = PDDocument.load(TestUtils.readTestFile(pdfFile));

        PdfFontExtractor extractor = new PdfFontExtractor();
        extractor.extractFontsToDir(doc, TestUtils.tempOutputPath);
        try {
            return extractor.extractToFVFonts(doc);
        } finally {
            doc.close();
        }
    }

    private PDFont findFont(List<PDFont> fonts, String name) {
        PDFont searchFont = null;
        for (PDFont fontOn : fonts)
            if (fontOn.getName().equals(name))
                searchFont = fontOn;

        Assert.assertNotNull(searchFont);
        return searchFont;
    }
}