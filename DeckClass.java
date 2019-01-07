// The "DeckClass" class.

import hsa.Console;
import java.awt.*;
import java.util.*;

public class DeckClass extends ShapeClass
{

    private Vector deck;

    public DeckClass ()
    {
	super (Color.green, 100, 70, 300, 300);
	deck = new Vector ();
    }


    public DeckClass (Color clr, int h, int x, int y)
    {
	super (clr, h, (int) (h * 0.7), x, y);
	deck = new Vector ();
    }



    public DeckClass (int NumOfDecks, Color clr, int h, int x, int y)
    {
	super (clr, h, (int) (h * 0.7), x, y);
	deck = new Vector ();

	for (int i = 0 ; i < 13 ; i++)
	{
	    for (int j = 1 ; j < 5 ; j++)
	    {
		for (int k = 0 ; k < NumOfDecks ; k++) //k copies of each card
		{
		    String FaceValues = "A23456789TJQK";
		    CardClass cc = new CardClass (FaceValues.substring (i, i + 1), j, false);
		    deck.addElement (cc);
		}
	    }
	}
    }


    public boolean isEmpty ()
    {
	return deck.isEmpty ();
    }


    public int getCardCount ()
    {
	deck.trimToSize ();
	return deck.size ();
    }


    public void shuffle ()
    {
	if (!isEmpty ())
	{
	    for (int i = 1 ; i < getCardCount () * 25 ; i++)
	    {
		addTop (getAndRemove ((int) (Math.random () * getCardCount ())));
	    }
	}
    }


    public void draw (Console c)
    {
	standardize ();
	if (isEmpty ())
	{
	    c.setColour (super.getColour ());
	    c.drawRect (super.getCentreX () - super.getWidth () / 2, super.getCentreY () - super.getHeight () / 2, super.getWidth (), super.getHeight ());
	}
	else if (!isEmpty ())
	{
	    getTop ().draw (c);
	}
	//draw stuff
    }


    public void erase (Console c)
    {
	if (getCardCount () > 0)
	{

	    getTop ().erase (c);
	}
	else
	{
	    Color prevclr = iColour;
	    iColour = new Color (30, 158, 23);
	    draw (c);
	    iColour = prevclr;
	}
    }


    public void draw (Graphics g)
    {
	standardize ();
	if (isEmpty ())
	{
	    g.setColor (super.getColour ());
	    g.drawRect (super.getCentreX () - super.getWidth () / 2, super.getCentreY () - super.getHeight () / 2, super.getWidth (), super.getHeight ());
	}
	else if (!isEmpty ())
	{
	    getTop ().draw (g);
	}
	//draw stuff
    }


    public void draw (int space, int handcc, boolean fixed, int cardMoving, Graphics g)
    {
	//standardize ();
	handcc += 1;
	// System.out.println ("************");
	if (!isEmpty ())
	{
	    for (int i = 0 ; i <= getCardCount () - 1 ; i++)
	    {
		if (!fixed && i != 0)
		{
		    getCard (i).setCenter (getCentreX () + (handcc - i) * (getWidth ()) + (handcc - i) * space, getCentreY ());
		}
		else if (fixed)
		{
		    if (i != cardMoving)
		    {
			getCard (i).setCenter (getCentreX () + (getCardCount () - 1 - i) * (getWidth ()) + (getCardCount () - 1 - i) * space, getCentreY ());
		    }

		}
		getCard (i).draw (g);
		//System.out.println (getCard (i).getFace ());
		getCard (cardMoving).draw (g); // bring current card to foreground
	    }


	}
	//System.out.println (getCardCount ()); //check hand size to make sure its right
    }


    public void erase (Graphics g)
    {
	if (getCardCount () > 0)
	{

	    getTop ().erase (g);
	}
	else
	{
	    Color prevclr = iColour;
	    iColour = new Color (30, 158, 23);
	    draw (g);
	    iColour = prevclr;
	}
    }


    public void addTop (CardClass cc)
    {
	deck.add (0, cc);
    }


    public void addBot (CardClass cc)
    {
	deck.addElement (cc);
    }


    public void addAt (CardClass cc, int num)
    {
	if (num >= 0 && num <= getCardCount ())
	{
	    deck.add (num, cc);
	}
	else if (num >= getCardCount ())
	{
	    addBot (cc);
	}
    }


    public void removeTop ()
    {
	if (getCardCount () >= 1)
	{
	    deck.remove (0);
	}
    }


    public void removeBot ()
    {
	deck.remove (getCardCount () - 1);
    }


    public void removeAt (int num)
    {
	deck.remove (num);
    }


    public void flipAll ()
    {
	for (int i = 0 ; i < deck.size () ; i++)
	{
	    ((CardClass) deck.elementAt (i)).flip ();
	}
    }


    public void flipTop ()
    {
	((CardClass) deck.elementAt (0)).flip ();
    }


    public CardClass getCard (int num)
    {
	if (!isEmpty () && num >= 0 && num <= getCardCount ())
	{
	    return (CardClass) deck.elementAt (num);
	}
	else
	{
	    return (CardClass) getTop ();
	}
    }


    public CardClass getAndRemove (int num)
    {
	if (!isEmpty () && num >= 0 && num <= getCardCount ())
	{
	    return (CardClass) deck.remove (num);
	}
	else
	{
	    return (CardClass) deck.remove (0);
	}
    }


    public CardClass getTop ()
    {
	return (CardClass) getCard (0);
    }


    public CardClass getBot ()
    {
	return getCard (getCardCount () - 1);
    }


    public void standardize ()
    {
	for (int i = 0 ; i < deck.size () ; i++)
	{
	    ((CardClass) deck.elementAt (i)).setFill (super.getColour ());
	    ((CardClass) deck.elementAt (i)).setHeight (super.getHeight ());
	    ((CardClass) deck.elementAt (i)).setCenter (super.getCentreX (), super.getCentreY ());
	}

    }


    public void swap (int a, int b)
    {
	if (a >= 0 && b >= 0 && a <= getCardCount () && b <= getCardCount ())
	{
	    Collections.swap (deck, a, b);
	}
    }


    public boolean isInside (int x, int y)
    {
	if (x >= (int) (super.getCentreX () - super.getWidth () / 2) && x <= (int) (super.getCentreX () + super.getWidth () / 2) && y >= (int) (super.getCentreY () - super.getHeight () / 2) && y <= (int) (super.getCentreY () + super.getHeight () / 2))
	{
	    return true;
	}
	else
	{
	    return false;
	}

    }
} // DeckClass class


