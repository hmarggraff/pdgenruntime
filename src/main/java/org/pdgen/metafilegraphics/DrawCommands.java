// This software may be used as allowed by the Gnu Affero General Public License. Details are in the file LICENSE, that must be included in the distribution of ths software.
package org.pdgen.metafilegraphics;

import java.awt.*;

public interface DrawCommands {
    byte CLOSE = 0;
    byte DRAWLINE = 1;
    byte DRAWRECT = 2;
    byte FILLRECT = 3;
    byte FILLROUNDRECT = 4;
    byte DRAWROUNDRECT = 5;
    byte DRAWOVAL = 6;
    byte FILLOVAL = 7;
    byte FILLPOLYGON = 8;
    byte DRAWPOLYGON = 9;
    byte DRAWPOLYLINE = 10;
    byte FILLARC = 11;
    byte DRAWARC = 12;
    byte DRAWSTRING = 13;
    byte DRAWSTRINGF = 14;
    byte SEG_MOVETO = 15;
    byte SEG_LINETO = 16;
    byte SEG_QUADTO = 17;
    byte SEG_CUBICTO = 18;
    byte DRAWSHAPE = 19;
    byte SEG_CLOSE = 20;
    byte PATHEND = 21;
    byte FILLSHAPE = 22;
    byte SETBACKGROUND = 23;
    byte SETCOLOR = 24;
    byte SETFONT = 25;
    byte SETSTROKE = 26;
    byte CLEARCLIP = 27;
    byte CLIPSHAPE = 28;
    byte CLIPRECT = 29;
    byte SETCLIPSHAPE = 30;
    byte SETCLIPRECT = 31;
    byte IMAGETR = 32;
    byte ROTATE = 33;
    byte ROTATEXY = 34;
    byte GLYPHVECTOR = 35;
    byte SCALEXY = 36;
    byte SETPAINT = 37;
    byte SETTRANSFORM = 38;
    byte SHEAR = 39;
    byte CONCATTRANSFORM = 40;
    byte TRANSLATE = 41;
    byte SETPAINTMODE = 42;
    byte SETXORMODE = 43;
    byte CLEARRECT = 44;
    byte DISPOSE = 45;
    byte CREATE = 46;
    byte SETRENDERINGHINTS = 47;
    byte SETRENDERINGHINT = 48;
    byte ADDRENDERINGHINTS = 49;
    byte SETCOMPOSITEALPHA = 50;
    byte TRANSLATEF = 51;
    byte ALPHACOMPOSITE = 52;
    byte IMAGEXY = 53;
    byte IMAGEXYWH = 54;
    byte CLEARSETCLIP = 55;

    RenderingHints.Key[] renderingHintKeyTable = {
            RenderingHints.KEY_ALPHA_INTERPOLATION, //0
            RenderingHints.KEY_ANTIALIASING, //1
            RenderingHints.KEY_COLOR_RENDERING, //2
            RenderingHints.KEY_DITHERING,        //3
            RenderingHints.KEY_FRACTIONALMETRICS,//4
            RenderingHints.KEY_INTERPOLATION,
            RenderingHints.KEY_RENDERING,
            RenderingHints.KEY_STROKE_CONTROL,
            RenderingHints.KEY_TEXT_ANTIALIASING
    };

    Object[] renderingHintValueTable = {
            RenderingHints.VALUE_ALPHA_INTERPOLATION_DEFAULT, //0
            RenderingHints.VALUE_ALPHA_INTERPOLATION_QUALITY, //1
            RenderingHints.VALUE_ALPHA_INTERPOLATION_SPEED, //2
            RenderingHints.VALUE_ANTIALIAS_DEFAULT, //3
            RenderingHints.VALUE_ANTIALIAS_OFF, //4
            RenderingHints.VALUE_ANTIALIAS_ON, //5
            RenderingHints.VALUE_COLOR_RENDER_DEFAULT,  //6
            RenderingHints.VALUE_COLOR_RENDER_QUALITY,  // 7
            RenderingHints.VALUE_COLOR_RENDER_SPEED,    // 8
            RenderingHints.VALUE_DITHER_DEFAULT,        // 9
            RenderingHints.VALUE_DITHER_DISABLE,        //10
            RenderingHints.VALUE_DITHER_ENABLE,         //11
            RenderingHints.VALUE_FRACTIONALMETRICS_DEFAULT,//12
            RenderingHints.VALUE_FRACTIONALMETRICS_OFF,    //13
            RenderingHints.VALUE_FRACTIONALMETRICS_ON,            //14
            RenderingHints.VALUE_INTERPOLATION_BICUBIC,           //15
            RenderingHints.VALUE_INTERPOLATION_BILINEAR,          //16
            RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR,  //17
            RenderingHints.VALUE_RENDER_DEFAULT,                  //18
            RenderingHints.VALUE_RENDER_QUALITY,                  //19
            RenderingHints.VALUE_RENDER_SPEED,                    //20
            RenderingHints.VALUE_STROKE_DEFAULT,                  //21
            RenderingHints.VALUE_STROKE_NORMALIZE,                //22
            RenderingHints.VALUE_STROKE_PURE,                     //23
            RenderingHints.VALUE_TEXT_ANTIALIAS_DEFAULT,          //24
            RenderingHints.VALUE_TEXT_ANTIALIAS_OFF,              //25
            RenderingHints.VALUE_TEXT_ANTIALIAS_ON                //26
    };

}
