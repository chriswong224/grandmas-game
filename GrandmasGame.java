// The "GrandmasGame" class. 2017ICS4FPROJ
import java.applet.*;
import java.awt.*;
import java.awt.event.*;
import java.util.*;

public class GrandmasGame extends Applet implements ActionListener, MouseListener, MouseMotionListener
{
    // Place instance variables here
    Graphics g; //graphics canvas for drawing
    Button bQuit = new Button ("Quit");
    Button bNew = new Button ("New");
    Button bUndo = new Button ("Undo");
    Button bInfo = new Button ("Info");
    Choice cSpeed = new Choice ();

    DeckClass fDeck[] = new DeckClass [8]; //foundation decks
    DeckClass pDeck[] = new DeckClass [13]; //pile decks
    DeckClass d; //main deck
    DeckClass newDeck; //create the new deck (temporary, only used while dealing)
    DeckClass mDeck; //temporary deck that moves around, which is useful for animations

    DeckClass hand; //pile with swappable cards. needs a different method of drawing and handling

    //double buffer
    Graphics bg; //second screen
    Image os; //image that we will use

    //"screenshot" to copy over and help wtih lag
    Graphics sg;
    Image ss;

    boolean moving; //tells whether or not a card is currently moving. if a card is moving, allows for the temporary deck to be drawn
    boolean predeal; //predeal state of the game.
    boolean screenshot; //whether or not a screenshot is being created right now
    boolean usess; //whether or not a screenshot should be used right now
    boolean handFixed; //use the alternate draw method in the deck

    boolean intro; //whether or not the instruction screen should be displayed
    long SlightIntroDelay;
    boolean checkForIntroDelay;

    boolean TimerDrawDone;
    boolean inTimer;

    int hMove; //which deck is currently moving. they are integers to keep track of which array element to use
    int pMove;
    int fMove;

    int prevPile; //the pile in which the previous cards are from, so cards can be returned to the right place
    int spacing; //spacing between cards in the hand
    int handcc; //expected card count in the hand. used solely for drawing while it is animating the hand (accounts for gaps while drawing)
    int animationSpeed;

    int score; //keep track of the current score
    int timeInSeconds;

    long prevTimeMillis; //time for every 10 seconds passed
    long lastPaintTime;

    long timeSinceInit;


    //used for double clicking
    int dcspeed; // double click speed in ms
    long lmclick; //last mouse click time in ms
    int lastX; //last X pos
    int lastY; //last Y pos

    long lastDoubleClick;

    long prevClockTime;

    //SCORING SYSTEM
    //-2 POINTS FOR EVERY 10 SECONDS ELAPSED
    //-10 POINTS FOR EVERY SWAP IN HAND
    //+15 POINTS FOR EVERY MOVE TO FOUNDATION

    //INITIALIZTIONS/////////////////////////////////////

    public void init ()
    {
	setSize (1080, 720); //set the size of the applet
	g = getGraphics (); //get the graphics settings and apply them to g
	os = createImage (1080, 720); //create the second screen image
	bg = os.getGraphics (); //apply graphics settings to the second screen

	ss = createImage (1080, 720); //create a third image screen as a screenshot
	sg = ss.getGraphics (); //apply graphics settings to the third screen

	setBackground (new Color (30, 158, 23)); //initialize the background colour

	add (bQuit); //add buttons
	add (bNew);
	//add (bUndo);
	add (bInfo);

	cSpeed.add ("Fast"); //add choices to the drop down menu
	cSpeed.add ("Normal");
	cSpeed.add ("Slow");
	cSpeed.add ("Instant");

	add (cSpeed);

	initFoundations (); //initialize each of the following
	initPiles ();
	initDeck ();
	initNewDeck ();
	mDeck = new DeckClass (Color.red, 100, 0, 0); // give garbage values to start the movement deck

	moving = false; //temp deck does not start moving, tehrefore dont draw it
	predeal = true; //the deck has not been dealt yet. this prevents a lot of commands
	screenshot = false; // tells the paint method whether it is creating a screenshot or not
	usess = false; // tells the paint method whether or not the double buffer screen currently being created requires the screenshot to be used
	handFixed = false; //if the hand fan method is fixed yet (different drawing if it's in process of animating)
	intro = false; //the instructions screen should not appear at the very beginning
	hMove = -1; //keeps track of which hand to move
	pMove = -1;
	fMove = -1;

	TimerDrawDone = true; //prevents the program from drawing through the timer if a click method already has drawn and updated the screen
	inTimer = false;

	prevClockTime = 0;

	//used for double clicking
	dcspeed = 300; // double click speed in ms
	lmclick = 0; //last mouse click time in ms
	lastX = 0; //last X pos
	lastY = 0; //last Y pos
	lastDoubleClick = 0;

	SlightIntroDelay = 0;
	checkForIntroDelay = false;

	animationSpeed = 18;

	spacing = 15;
	handcc = 1;
	prevPile = -1; //keeps track of the previous pile that was dealed so the current hand can return to it

	bQuit.addActionListener (this); //add action listeners for all the buttons
	bNew.addActionListener (this);
	//bUndo.addActionListener (this);
	bInfo.addActionListener (this);

	addMouseListener (this);
	addMouseMotionListener (this);

	initHand (); //initialize the hand

	initTimer (); //initialize the timer

	// Place the body of the initialization method here
    } // init method


    private void initFoundations ()
    {
	for (int i = 0 ; i < 8 ; i++) //runs through each foundation, sending the position of the empty decks, creating the foundations
	{
	    fDeck [i] = new DeckClass (Color.red, 100, 250 + i * 100, 100); //check constructors`later
	}
    }


    private void initPiles ()
    {
	int y = 250; //values used to calculate the piles later on.
	int x = 225;
	for (int i = 0 ; i < 13 ; i++) //runs throught he 13 piles
	{
	    if (i == 5 || i == 10) //if the for loop is passed the 5th or 10th piles, start a new row
	    {
		y += 125;
		x = 350;
	    }
	    else
	    {
		x += 125; //else, shift one card down
	    }
	    pDeck [i] = new DeckClass (Color.red, 100, x, y); //create the new tableau deck at the specified calculated position
	}
    }


    private void initDeck ()
    {
	d = new DeckClass (Color.red, 100, 100, 100); //create the deck that deals in the main game
    }


    private void initHand ()
    {
	hand = new DeckClass (Color.red, 100, 75, 625); //create the hand with capabilities of fanning
    }


    private void initNewDeck ()
    {
	newDeck = new DeckClass (2, Color.red, 100, 100, (int) ((625 - 100) / 2) + 100); //create the new deck for predeal, using 2 decks
	newDeck.shuffle (); //shuffle the 2 decks together
	newDeck.flipAll (); //flip all the cards upside-down
    }


    private void initTimer ()  //create a timer for use in calculating score
    {
	Timer t = new Timer ();
	t.schedule (new TimerTask ()
	{
	    public void run ()
	    {
		if (!checkWin ()) //if the game has not been won yet,
		{
		    if (predeal) //if the game hasnt started yet, dont start the score or timer
		    {
			score = 0; //if the game has not begun yet, reinitialize these values
			timeInSeconds = 0;
			timeSinceInit = System.currentTimeMillis (); //finds the time since initialization
			prevTimeMillis = System.currentTimeMillis (); //previous time in which this timer was called.
		    }
		    else if (System.currentTimeMillis () - prevTimeMillis >= 10000) //if there has been more than 10 seconds since the previous update
		    {
			prevTimeMillis = System.currentTimeMillis (); //reset the time elapsed
			score -= 2; //loses 2 points for every 10 seconds elapsed

		    }
		    // if (!predeal)
		    // { //display time
		    //     if (System.currentTimeMillis () - lastPaintTime >= 500)
		    //     {
		    //         if (prevClockTime / 1000 != System.currentTimeMillis () / 1000)
		    //         {
		    //
		    //             prevClockTime = timeSinceInit;
		    //             inTimer = true;
		    //             update (g);
		    //             TimerDrawDone = true;
		    //             inTimer = false;
		    //         }
		    //
		    //     }
		    //}
		}
	    }
	}


	, 0, 500); //scheduled for every 500 ms
	Timer it = new Timer (); //create a second timer
	t.schedule (new TimerTask ()
	{
	    public void run ()
	    {
		checkIfIntro (); //checks if the intro overlay is currently on the screen
	    }
	}


	, 0, 300); //scheduled for every 300 ms
    }


    //DRAWS//////////////////////////////////////////////

    private void drawFoundations (Graphics g)
    {
	for (int i = 0 ; i < 8 ; i++) //loops through all the foundation piles
	{
	    fDeck [i].draw (g); //draws each foundation pile
	}
    }


    private void drawPiles (Graphics g)
    {
	for (int i = 0 ; i < 13 ; i++) //loops through all the tableau piles
	{
	    pDeck [i].draw (g); //draw the tableau piles
	}
    }


    private void drawDeck (Graphics g)
    {
	d.draw (g); //draw the deck (that deals)
    }


    private void drawNewDeck (Graphics g)
    {
	newDeck.draw (g); //draw the new deck (pregame)
    }


    private void drawHand (Graphics g)
    {
	hand.draw (spacing, handcc, handFixed, hMove, g); //draws the hand with appropriate spacing
    }


    private void drawIntro (Graphics g)  //code that draws the instruction screen
    {
	int qx = 150; //x pos
	int qy = 120; //y pos
	int ql = 800; //length
	int qh = 400; //height
	int bw = 15; //border width
	g.setColor (Color.blue);
	g.fillRect (qx - bw, qy - bw, ql + bw * 2, qh + bw * 2); //draw border
	g.setColor (new Color (30, 158, 23));
	g.fillRect (qx, qy, ql, qh); // draw rectangle inside
	Font f = new Font ("ComicSans", Font.PLAIN, 20);
	g.setColor (Color.red);
	g.setFont (f);
	//INSTRUCTIONS
	g.drawString ("Instructions", (qx + ql) / 2 + 10, qy + 25);
	g.drawString ("There are 13 tableaus in three rows of 5, 5, and 3.", qx + 5, qy + 50);
	g.drawString ("Refer to the each pile with a rank. For example, the first pile is the \"Ace\" pile.", qx + 5, qy + 75);
	g.drawString ("You can move the top card from any tableau pile to the foundations.", qx + 5, qy + 100);
	g.drawString ("Click the deck pile to turn over one card.", qx + 5, qy + 125);
	g.drawString ("The corresponding pile of the card turned over will spread out below the tableau.", qx + 5, qy + 150);
	g.drawString ("So if you get a 4 from the deck, then the 4th tableau pile will spread out.", qx + 5, qy + 175);
	g.drawString ("You can play the card from the deck or any of the cards in the spread to the foundations.", qx + 5, qy + 200);
	g.drawString ("Click the deck again and all of the cards from the spread will return to the tableau pile.", qx + 5, qy + 225);
	g.drawString ("Cards in the hand can be swapped to affect the order they are return to the tableau.", qx + 5, qy + 250);
	g.drawString ("Try to fill up every foundation to win!", qx + 5, qy + 275);
	g.drawString ("Scoring", (qx + ql) / 2 + 20, qy + 300);
	g.drawString ("+15 points for every move to foundation", qx + 5, qy + 325);
	g.drawString ("-10 points for every swap in hand", qx + 5, qy + 350);
	g.drawString ("-2 points for every 10 seconds passed", qx + 5, qy + 375);
    }


    ////////////////ACTIONS///////////////////

    private void slide (int nX, int nY, int delay, int steps, Graphics g)
    {
	//System.out.println ("b");

	//use mDeck;, add card to mDeck whenever movement is desired
	//MAKE SURE TO SET MDECK POSITION BEFORE CALLING SLIDE
	if (steps == 0) //checks if the steps are a valid number, and pushes it up to 1, in case it's not (division by 0 is invalid0
	{
	    steps = 1;
	}

	double xDist = nX - mDeck.getCenterX (); //figure out the x distance required to travel
	double yDist = nY - mDeck.getCenterY (); //figure out the y distance required to travel

	double stepX = xDist / steps; //length of x steps
	double stepY = yDist / steps; //length of y steps

	double cX = mDeck.getCenterX (); //find the current x pos of the deck
	double cY = mDeck.getCenterY (); //find the current y pos of the deck

	screenshot = !screenshot; //set create screenshot to true
	update (bg); //update the screenshot screen
	screenshot = !screenshot; //set create screenshot to false
	usess = !usess; //set use screenshot to be true, instead of redrawing objects, redraws the screnshot once each time

	for (int i = 1 ; i <= steps ; i++)
	{
	    //System.out.println ("c");
	    moving = true; //cards are allowed to move

	    //mDeck.erase (g);

	    //update location of mDeck
	    cX += stepX;
	    cY += stepY;
	    mDeck.setCenter ((int) cX, (int) cY);

	    if (i == steps) //if the right number of steps have been reached
	    {
		mDeck.setCenter (nX, nY); //snap to the current position, in case of rounding errors
	    }
	    update (g); //update the screen again
	    mDeck.delay (delay); //delay based on the framerate
	}


	usess = !usess; //set use screenshot to false
	moving = false; //set the move state to false

	// System.out.println ("d");
    }


    private void deal (DeckClass curD, DeckClass newD, int delay, int steps, boolean doFlip)
    {
	mDeck.setCenter (curD.getCenterX (), curD.getCentreY ()); //set the center of the temp deck equal to the center of the card intended to move
	mDeck.removeTop (); //remove the previous top card on the mdeck
	mDeck.addTop (curD.getTop ()); //add a new card that is the one intended to be moved
	if (doFlip) //if the card needs to be flipped
	{
	    mDeck.flipTop (); //flip the card
	}


	curD.removeTop (); //remove the top card of the original deck (now that it's on the temporary moving deck)

	slide (newD.getCenterX (), newD.getCenterY (), delay, steps, g); //slide the temporary deck to the new position
	newD.addTop (mDeck.getTop ()); //add the card on top of the temporary deck to the top of the new slide location
    }


    private void deal (DeckClass curD, DeckClass newD, int delay, int steps)  //override for deal assuming that no flips are required
    {
	deal (curD, newD, delay, steps, false);
    }


    private void dealHand (DeckClass curD)  //works similarly to the deal method above
    {
	handcc = curD.getCardCount (); //set up cardcount now since itll change later
	for (int i = 0 ; i < handcc ; i++)
	{
	    mDeck.setCenter (curD.getCenterX (), curD.getCentreY ());
	    mDeck.removeTop (); //clear whatever previous garbage there was
	    mDeck.addTop (curD.getTop ());
	    curD.removeTop (); //sliding is calculated differently, due to the fanning out of the hand
	    moving = true; //make moving true
	    slide (hand.getCenterX () + (handcc - i) * (hand.getWidth ()) + (handcc - i) * spacing, hand.getCenterY (), 0, animationSpeed, g);
	    hand.addBot (mDeck.getTop ()); //add the temp card to the bottom of the hand instead of the top
	}


	moving = false; //make moving false
    }


    private void newDeal ()
    {
	initFoundations (); //initialize all the decks while there is a new deal
	initPiles ();
	initDeck ();
	initNewDeck ();
	initHand ();
	predeal = true; //predeal state becoems true


	// One card is dealt to each pile, then the deal continues back at the first pile.
	// Any time a king is dealt, the next card goes back onto the deck pile in the upper left.
	// After the 5th, 10th and 13th piles get their card, one card also goes to the deck pile.
	// Also, if a card is dealt that has a number that matches the number of the tableau pile, then the next card also goes to the deck.
	// So if an 8 is dealt to the 8th pile, the next card goes to the deck.
	int steps = animationSpeed; //steps
	int delay = 0; //delay
	int n = 0;
	String FaceValues = "A23456789TJQK"; //string containing face values that are to be used

	do
	{
	    boolean skip = false; //skip is used to determine whether or not the next pile should be skippe

	    deal (newDeck, pDeck [n], delay, steps, true);
	    // mDeck.setCenter (100, (int) ((625 - 100) / 2) + 100);
	    // mDeck.removeTop ();
	    // mDeck.addTop (newDeck.getTop ());
	    // mDeck.flipTop ();
	    // newDeck.removeTop ();
	    //
	    // slide (pDeck [n].getCenterX (), pDeck [n].getCenterY (), delay, steps, g);
	    // pDeck [n].addTop (mDeck.getTop ());

	    if ((FaceValues.indexOf (pDeck [n].getTop ().getFace ()) == n) || ("K".indexOf (pDeck [n].getTop ().getFace ()) > -1))
	    { //if the face card is equivalent to the current pile, skip the next pile
		skip = true;
	    }

	    if ((n == 4 || n == 9 || n == 12 || skip) && !newDeck.isEmpty ()) //check if its empty first in case needs to exit
	    {
		deal (newDeck, d, delay, steps);

		// mDeck.setCenter (100, (int) ((625 - 100) / 2) + 100);
		// mDeck.removeTop ();
		// mDeck.addTop (newDeck.getTop ());
		// newDeck.removeTop ();
		//
		// slide (100, 100, delay, steps, g);
		// d.addTop (mDeck.getTop ());
		if (skip)
		{
		    if (n < 12) //if n is less than 12, increment it
		    {
			n++;
		    }
		    else //loop back to 0 so it deals the next card to the first pile
		    {
			n = 0;
		    }
		}
	    }


	    if (n < 12) //if n is less than 12, increment it
	    {
		n++;
	    }
	    else //loop back to 0 so it deals then next card to the first pile
	    {
		n = 0;
	    }
	}


	while (!newDeck.isEmpty ())  //do all this as long as the new deck is not empty
	    ;

	predeal = false; //deal compleete. set predeal to false.
	update (g); //update the screen once more for safety

	//System.out.println (d.getCardCount ());
    }


    public boolean checkWin ()  //checks if the game has been won
    {
	for (int i = 0 ; i < 8 ; i++) //checks each card in the foundation
	{
	    if (fDeck [i].getCardCount () != 13) //if the card count in the foundation is not 13, return false
	    {
		return false;
	    }
	}


	System.out.println ("You win with a score of " + score + "!"); // display the score and win screen if the game ahs been won
	return true; //return true so that the score pauses counting
    }


    private void checkIfIntro ()  //checks if the intro is currently on the screen
    {
	if (checkForIntroDelay && System.currentTimeMillis () - SlightIntroDelay >= 50) //have a sligth delay for closing the intro screen
	{
	    intro = false; //reset these values
	    checkForIntroDelay = false;
	    update (g); //update screen again
	}
    }


    private void doubleClicked (int x, int y)  //checks if the double click location is valid, and if it is valid, do something accordingly
    {
	for (int i = 0 ; i < 13 ; i++) //check if x and y are inside any of the tableau cards
	{
	    if (pDeck [i].isInside (x, y) && !pDeck [i].isEmpty ()) //if point is inside pdeck and pdeck is not empty
	    {
		for (int j = 0 ; j < 8 ; j++) //find a valid move, and if there is a valid move, move there
		{
		    if (isValid (pDeck [i].getTop (), j)) //check if moving to each foundation is valid
		    {
			score += 15;
			deal (pDeck [i], fDeck [j], 0, animationSpeed);  //if all is valid, animate the deal
			break; //exit once movement is complete
		    }
		}
	    }
	}


	for (int i = 0 ; i < hand.getCardCount () ; i++) //check if x and y are inside any of the tableau cards
	{
	    if (hand.getCard (i).isInside (x, y)) //if point is inside pdeck and pdeck is not empty
	    {
		for (int j = 0 ; j < 8 ; j++) //find a valid move, and if there is a valid move, move there
		{
		    if (isValid (hand.getCard (i), j)) //check if moving to each foundation is valid
		    {
			fDeck [j].addTop (hand.getAndRemove (i));
			score += 15;
			update (g);
			break; //exit once movement is complete
		    }
		}
	    }
	}


	//System.out.println (x);
	//System.out.println (y);
    }


    //ACTION LISTENER
    public void actionPerformed (ActionEvent e)
    {
	Object objSource = e.getSource ();

	// System.out.println ("a");

	if (objSource == bQuit)
	{
	    System.exit (0);
	}


	if (objSource == bNew)
	{
	    if (!intro)
	    {
		newDeal ();
	    }
	}


	if (objSource == bUndo)
	{

	}


	if (objSource == bInfo)
	{
	    if (!intro) //if the intro screen is not being displayed
	    {
		intro = true; //change intro to false
		update (g); //update the screen
	    }
	    else
	    {
		SlightIntroDelay = System.currentTimeMillis ();
		checkForIntroDelay = true;
	    }
	}
    }


    public boolean action (Event e, Object o)  //set the animation speed based on which choice is chosen
    {
	if (cSpeed.getSelectedIndex () == 0)
	{
	    animationSpeed = 18;
	}


	else if (cSpeed.getSelectedIndex () == 1)
	{
	    animationSpeed = 50;
	}


	else if (cSpeed.getSelectedIndex () == 2)
	{
	    animationSpeed = 100;
	}


	else if (cSpeed.getSelectedIndex () == 3)
	{
	    animationSpeed = 0;
	}


	return true;
    }


    //MOUSE LISTENER
    public void mouseClicked (MouseEvent e)  //click and let go
    {
	if (!intro)
	{
	    if (d.isInside (e.getX (), e.getY ()) && !d.isEmpty ()) //deal from deck
	    {
		String FaceValues = "A23456789TJQK";
		//return current hand to tableau
		while (!hand.isEmpty ())
		{
		    pDeck [prevPile].addBot (hand.getAndRemove (0));
		    handcc--;
		}
		handFixed = false;
		//deal
		deal (d, hand, 0, animationSpeed, true);
		update (g);

		//add corresponding pile to hand
		prevPile = FaceValues.indexOf (hand.getTop ().getFace ());
		dealHand (pDeck [prevPile]);

		//current queue looks like this : 0, 5,4,3,2,1
		hand.addBot (hand.getAndRemove (0));
		//changes to 5,4,3,2,1,0
		handFixed = true;

		update (g);
		//hand needs to fan out
	    }
	    //check for double click

	    if (lastX == e.getX () && lastY == e.getY () && System.currentTimeMillis () - lmclick <= dcspeed && System.currentTimeMillis () - lastDoubleClick > 100) //if the x and y coords are still the same, and less than 300 ms have pased, it is a double click
	    {
		doubleClicked (e.getX (), e.getY ()); //send the info to the double click method
		lastDoubleClick = System.currentTimeMillis (); //restrict how many double clicks can happen at a times it's not spammed
	    }
	    else
	    { //get info for the next click
		lmclick = System.currentTimeMillis (); //last mouse click time in ms
		lastX = e.getX (); //last X pos
		lastY = e.getY (); //last Y pos
	    }


	}
    }


    public void mouseEntered (MouseEvent e)
    {
    }


    public void mouseExited (MouseEvent e)
    {
    }


    public void mousePressed (MouseEvent e)  // active on press, doesn't wait for it to let go
    {
	if (!intro)
	{
	    //hand stuff
	    for (int i = 0 ; i < hand.getCardCount () ; i++)
	    {
		if (hand.getCard (i).isInside (e.getX (), e.getY ()))
		{
		    hMove = i; //make hmove equal to a certain card
		    //System.out.println (hMove);
		}
	    }
	    //tableau pile stuff
	    for (int i = 0 ; i < 13 ; i++)
	    {
		if (pDeck [i].isInside (e.getX (), e.getY ()) && !pDeck [i].isEmpty ()) //if mouse click is inside a tableau pile
		{
		    pMove = i; //make pmove equal to a certain card
		    mDeck.removeTop (); //remove the top deck off the temp pile
		    mDeck.addTop (pDeck [pMove].getAndRemove (0)); //add the top card  of the tableau deck to the temporary deck
		    moving = true; //set moving to true
		    break; //exit the loop because there is no point of continuing to check
		}
	    }
	    //foundation pile stuff (transfer halfway)
	    for (int i = 0 ; i < 8 ; i++)
	    {
		if (fDeck [i].isInside (e.getX (), e.getY ()) && !fDeck [i].isEmpty ()) //if mouse is inside a foundation pile
		{ //works similarly to above
		    fMove = i;
		    mDeck.removeTop ();
		    mDeck.addTop (fDeck [fMove].getAndRemove (0));
		    moving = true;
		    break;
		}
	    }

	}
    }


    private boolean isDuplicateFoundation (CardClass card)
    {
	for (int i = 0 ; i < 8 ; i++)
	{
	    if (!fDeck [i].isEmpty ()) //if the foundation deck is not empty
	    { //check if the curretn card is already a foundation on another pile
		if (card.getFace ().equals (fDeck [i].getBot ().getFace ()) && card.getSuit () == fDeck [i].getBot ().getSuit ())
		{
		    return true;
		}
	    }
	}


	return false; //if false, then we're good to add the card
    }


    private boolean isValid (CardClass card, int fNum)  //card to check, foundation pile to check against
    {
	if (fDeck [fNum].isEmpty ()) //new starter card
	{ //allow the card as long as it's an ace or a king and no ace / king of the same suit already exists
	    if ((card.getFace ().equals ("A") || card.getFace ().equals ("K")) && !isDuplicateFoundation (card))
	    {
		return true;
	    }

	}


	else if (fDeck [fNum].getCardCount () < 13) //if deck is not full
	{
	    String FaceValues = "A23456789TJQK";
	    // can always assume that the deck is not full yet bc it was previously checked
	    //start at ace going up
	    int cFV = FaceValues.indexOf (fDeck [fNum].getTop ().getFace ());
	    if (fDeck [fNum].getBot ().getSuit () == card.getSuit ())
	    {
		if (fDeck [fNum].getBot ().getFace ().equals ("A")) //card.getFace ().equals ())
		{
		    if (FaceValues.indexOf (card.getFace ()) == cFV + 1)
		    {
			return true;
		    }
		}
		else if (fDeck [fNum].getBot ().getFace ().equals ("K")) //start at king going down
		{
		    if (FaceValues.indexOf (card.getFace ()) == cFV - 1)
		    {
			return true;
		    }
		}
	    }

	}


	return false;
    }


    public void mouseReleased (MouseEvent e)
    {
	if (!intro)
	{
	    if (hMove > -1)
	    {
		//check where it's released at, and if that spot is a valid drop/swap location
		for (int i = 0 ; i < hand.getCardCount () ; i++)
		{
		    if (hand.getCard (i).isInside (e.getX (), e.getY ()) && i != hMove) //check against other hand cards except itself
		    {
			//if the above is true, i is the new target swap card
			hand.swap (i, hMove);
			score -= 10;
			break;
		    }
		}

		//check if it's dropped on a foundation
		for (int i = 0 ; i < 8 ; i++)
		{
		    if (fDeck [i].isInside (e.getX (), e.getY ()))
		    {
			//check if valid drop
			if (isValid (hand.getCard (hMove), i))
			{
			    fDeck [i].addTop (hand.getAndRemove (hMove));
			    score += 15;
			}
			break;
		    }
		}

		hMove = -1;
	    }

	    if (pMove > -1)
	    {
		moving = false;

		boolean removed = false;
		for (int i = 0 ; i < 8 ; i++)
		{
		    if (fDeck [i].isInside (e.getX (), e.getY ()))
		    {
			//check if valid drop
			if (isValid (mDeck.getTop (), i))
			{
			    fDeck [i].addTop (mDeck.getAndRemove (0));
			    removed = true;
			    score += 15;
			}
			break;
		    }
		}

		if (!removed)
		{
		    pDeck [pMove].addTop (mDeck.getTop ());
		}
		pMove = -1;
	    }

	    if (fMove > -1) //works the same way as above
	    {
		moving = false;

		boolean removed = false;
		for (int i = 0 ; i < 8 ; i++)
		{
		    if (fDeck [i].isInside (e.getX (), e.getY ()))
		    {
			//check if valid drop
			if (isValid (mDeck.getTop (), i))
			{
			    fDeck [i].addTop (mDeck.getAndRemove (0));
			    removed = true;
			}
			break;
		    }
		}

		if (!removed)
		{
		    fDeck [fMove].addTop (mDeck.getTop ());
		}
		fMove = -1;
	    }

	    update (g);
	}
    }


    //MOUSE MOTION LISTENER
    public void mouseDragged (MouseEvent e) //if the mouse is being dragged, update the card (if any) to the current mosue location
    {
	if (!intro)
	{
	    if (hMove > -1)
	    {
		hand.getCard (hMove).setCenter (e.getX (), e.getY ());
		update (g);

	    }
	    if (pMove > -1)
	    {
		mDeck.setCenter (e.getX (), e.getY ());
		update (g);
	    }
	    if (fMove > -1)
	    {
		mDeck.setCenter (e.getX (), e.getY ());
		update (g);
	    }
	}
    }


    public void mouseMoved (MouseEvent e)
    {
    }


    public void update (Graphics g)
    {
	paint (g);
    }


    private void drawTimeAndScore (Graphics g) //draws the time and score
    {
	Font f = new Font ("ComicSans", Font.PLAIN, 20); //create a font to draw the time and score in
	g.setColor (Color.red);
	g.setFont (f);

	timeInSeconds = (int) ((System.currentTimeMillis () - timeSinceInit) / 1000);
	//System.out.println (timeInSeconds);

	int minutes;
	if (timeInSeconds > 0) //if the time is greater than 0, make the minutes equal to the time in seconds divided by 60
	{
	    minutes = (int) (timeInSeconds / 60);
	}


	else
	{
	    minutes = 0; //make the minutes equal to 0
	}


	;
	int seconds = timeInSeconds - minutes * 60;

	g.drawString ("Score: " + Integer.toString (score), 20, 25);//draw the score

	// String TimeString = Integer.toString (seconds); //create the string containing the time elapsed
	// if (TimeString.length () == 1)
	// {
	//     TimeString = "0" + TimeString;
	// }
	// 
	// 
	// TimeString = ":" + TimeString;
	// TimeString = minutes + TimeString;
	// 
	// TimeString = "Time: " + TimeString;
	//g.drawString (TimeString, 160, 25);

	g.drawString ("Christopher Wong", 915, 710); //draws my name
    }


    public void paint (Graphics g)
    {

	if (TimerDrawDone) //if the timer is not currently drawing
	{
	    if (inTimer)//if currently in timer, make the timerdrawdone setting false
	    {
		TimerDrawDone = false;
	    }
	    if (!screenshot) //if not creating a screenshot, draw to the second screen normally
	    {
		bg.setColor (new Color (30, 158, 23));
		bg.fillRect (0, 0, 2222, 1500);
		if (usess)
		{
		    bg.drawImage (ss, 0, 0, this); //if the screenshot should be used, draw the screenshot on the screen
		    if (moving) //if there is currently something moving, draw the moving deck on the second screen, on top of the third screen
		    {
			mDeck.draw (bg);
		    }
		}
		else//if not using the screenshot
		{//draw everything to the screen normally
		    if (predeal)
		    {
			drawNewDeck (bg);
		    }

		    drawFoundations (bg);
		    drawPiles (bg);
		    drawDeck (bg);

		    drawHand (bg);

		    if (moving)
		    {
			mDeck.draw (bg);
		    }
		    drawTimeAndScore (bg);
		}

		if (intro)//if the intro screen should be open
		{
		    drawIntro (bg);//draw the intro screen
		}
		g.drawImage (os, 0, 0, this); //update the main screen by drawing the second screen onto it

	    }


	    else//if the program is currently creeating a screnshot
	    {
		sg.setColor (new Color (30, 158, 23));//set colours on the third screen
		sg.fillRect (0, 0, 2222, 1500);//draw a rectangle as the background on this third screen

		if (predeal) //draw everything normally, but on the third screen instead
		{
		    drawNewDeck (sg);
		}

		drawFoundations (sg);
		drawPiles (sg);
		drawDeck (sg);

		drawHand (sg);

		drawTimeAndScore (sg);
		if (intro)
		{
		    drawIntro (sg);
		}
	    }


	    //checkWin ();
	    //System.out.println (score);
	    //doublecheck these
	}


	lastPaintTime = System.currentTimeMillis (); //keep track of the last paint time, as to not have the timer paint at the same time

	// Place the body of the drawing method here
    } // paint method
} // GrandmasGame class


