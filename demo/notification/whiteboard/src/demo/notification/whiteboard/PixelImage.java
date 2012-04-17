package demo.notification.whiteboard;


import java.awt.Color;
import java.awt.image.MemoryImageSource;
import java.awt.image.ImageProducer;

/**
 * PixelImage.java
 * A pixel-based image.
 *
 *
 * @author Nicolas Noffke, Torsten Fink
 */

public class PixelImage  {
    private static final int ALPHA = 255 << 24;
    protected int[] m_pixels;
    private int width;
    private int height;

    /**
      The constructor. Set up buffer.

      @param height the images height.
      @param width the images width.
     */
    public PixelImage(int width, int height) {
        m_pixels = new int[width * height];
        this.width=width;
        this.height=height;
        clearAll();
    }

    /**
     * sets a pixel to a specific color.
     *
     * @param x the pixels x value.
     * @param y the pixels y value.
     * @param red the pixels red value.
     * @param green the pixels green value.
     * @param blue the pixels blue value.
     */
    public void setPixel(int x, int y, int red, int green, int blue) {
        //ALPHA is needed, otherwise, if left 0, the pixel gets transparent.
        m_pixels[width * y + x] = ALPHA | (red << 16) |  (green << 8) | blue;
    }

    /**
     * sets a pixel to a specific color.
     *
     * @param x the pixels x value.
     * @param y the pixels y value.
     * @param color the pixels color.
     */
    public void setPixel(int x, int y, Color color) {
        m_pixels[width * y + x] = color.getRGB();
    }


    /**
     * gets the ImageProducer for this image.
     *
     * @return the ImageProducer for this image.
     */
    public ImageProducer getProducer() {
        return new MemoryImageSource(width, height, m_pixels, 0, width);
    }

    /**
     * gets the pixel buffer of this image.
     *
     * @return the pixel buffer.
     */
    public int[] getPixelBuffer() {
        return m_pixels;
    }

    public void setPixelBuffer(int[] data) {
	m_pixels = data;
    }

    /**
       draws a line in the image. The incremental line scan-conversion
       algorithm is used (see "Computer Graphics"; Foley, vanDam,Feiner,Hughes).
       (x0,y0) is the starting point, (x1,y1) the ending point.

     */

    public void drawLine(int x0,int y0, int x1, int y1,
                         int red,int green, int blue) {
        //System.out.println("Draw: ("+x0+","+y0+") ("+x1+","+y1+")");
        // do some clipping
        if ((x0<0)||(x1<0)||(y1<0)||(y0<0)||
                (x0>=width)||(x1>=width)||(y0>=height)||(y1>=height)) {
            return;
        }
        // parameters are ok
        if ( (x0==x1) && (y0==y1) ) {
            setPixel(x0,y0,red,green,blue);
        } else {
            float grad =                // cast is necessary
                ((float) (y1-y0))/
                ((float) (x1-x0));
            if ((grad >= -1.0)&&(grad <= 1.0)) { // loop over x
                if (x0>x1) { // change points
                    int change = x1;
                    x1 = x0;
                    x0 = change;
                    change = y1;
                    y1 = y0 ;
                    y0=change;
                }
                for(float y= (float)y0;
                        x0<=x1;
                        x0++) {
                    setPixel(x0,(int) (y+0.5),
                             red,green,blue);
                    y+= grad;
                }
            } else { // loop over y
                grad = ((float) 1.0)/grad;
                if (y0>y1) { // change points
                    int change = x1;
                    x1 = x0;
                    x0 = change;
                    change = y1;
                    y1 = y0 ;
                    y0=change;
                }
                for(float x= (float)x0;
                        y0<=y1;
                        y0++,x+=grad) {
                    setPixel((int) (x+0.5),y0,
                             red,green,blue);
                }
            }
        }
    }

    public void clearAll() {
        for(int x=0;x<width;x++)
            for(int y=0;y<height;y++)
                setPixel(x,y,0,0,0);
    }


} // PixelImage
