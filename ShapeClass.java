//ShapeClass.java

import hsa.Console;
import java.awt.*;

public abstract class ShapeClass
{

    protected Color iColour;
    protected int iHeight;
    protected int iWidth;
    protected int iCentreX;
    protected int iCentreY;

    //encapsulated data
    public ShapeClass ()
    {
	iColour = Color.red;
	iHeight = 100;
	iWidth = 50;
	iCentreX = 150;
	iCentreY = 200;
    }


    public ShapeClass (Color clr)
    {
	iColour = clr;
	iHeight = 100;
	iWidth = 50;
	iCentreX = 150;
	iCentreY = 200;
    }


    public ShapeClass (Color clr, int h, int w, int x, int y)
    {
	iColour = clr;
	iHeight = h;
	iWidth = w;
	iCentreX = x;
	iCentreY = y;
    }


    public abstract void draw (Console c);

    public void erase (Console c)
    {
	Color prevclr = iColour;
	iColour = new Color (30, 158, 23);
	draw (c);
	iColour = prevclr;
    }


    public abstract void draw (Graphics g);

    public void erase (Graphics g)
    {
	Color prevclr = iColour;
	iColour = new Color (30, 158, 23);
	draw (g);
	iColour = prevclr;
    }


    public void setColour (Color clr)
    {
	iColour = clr;
    }


    public void setHeight (int h)
    {
	if (h > 0)
	{
	    iHeight = h;
	}
    }


    public void setWidth (int w)
    {
	if (w > 0)
	{
	    iWidth = w;
	}
    }


    public void setCenter (int iNewCentreX, int iNewCentreY)
    {
	iCentreX = iNewCentreX;
	iCentreY = iNewCentreY;
    }


    public Color getColour ()
    {
	return iColour;
    }


    public int getWidth ()
    {
	return iWidth;
    }


    public int getHeight ()
    {
	return iHeight;
    }


    public int getCentreX ()
    {
	return iCentreX;
    }


    public int getCentreY ()
    {
	return iCentreY;
    }


    public int getCenterX ()
    {
	return getCentreX ();
    }


    public int getCenterY ()
    {
	return getCentreY ();
    }


    public void delay (int iDelayTime)
    {
	long lFinalTime = System.currentTimeMillis () + iDelayTime;
	do
	{
	}
	while (lFinalTime >= System.currentTimeMillis ());
    }
}


