// The "HandClass" class.
//does absolutely nothing
import hsa.Console;
import java.awt.*;
import java.util.*;

public class HandClass extends DeckClass
{
    int spacing;

    public HandClass ()
    {
	super ();
	spacing = 100;
    }


    public HandClass (Color clr, int h, int x, int y)
    {
	super (clr, h, x, y);
    }


    public void draw (Graphics g)
    {
	standardize ();
	if (!isEmpty ())
	{
	    for (int i = super.getCardCount () - 1 ; i >= 0 ; i--)
	    {
		getCard (i).setCenter (getCentreX () + i * (getWidth ()) + spacing, getCentreY ());
		getCard (i).draw (g);
	    }

	}
    }
} // HandClass class
