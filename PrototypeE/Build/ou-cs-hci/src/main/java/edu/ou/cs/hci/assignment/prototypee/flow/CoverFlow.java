//******************************************************************************
// Copyright (C) 2020 University of Oklahoma Board of Trustees.
//******************************************************************************
// Last modified: Mon Apr 13 19:43:50 2020 by Chris Weaver
//******************************************************************************
// Major Modification History:
//
// 20190203 [weaver]:	Original file.
// 20190220 [weaver]:	Adapted from swingmvc to fxmvc.
// 20200412 [weaver]:	Adapted from fxmvc CyclePane to prototypee CoverFlow.
//
//******************************************************************************
//
//******************************************************************************

package edu.ou.cs.hci.assignment.prototypee.flow;

//import java.lang.*;
import java.util.*;
import javafx.animation.*;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.value.ObservableValue;
import javafx.collections.*;
import javafx.event.*;
import javafx.scene.*;
import javafx.scene.control.*;
import javafx.scene.effect.*;
import javafx.scene.input.*;
import javafx.scene.layout.*;
import javafx.scene.paint.*;
import javafx.scene.shape.Rectangle;
import javafx.util.Duration;
import edu.ou.cs.hci.assignment.prototypee.*;
import edu.ou.cs.hci.assignment.prototypee.pane.AbstractPane;

//******************************************************************************

/**
 * The <CODE>CoverFlow</CODE> class.
 *
 * @author  Chris Weaver
 * @version %I%, %G%
 */
@SuppressWarnings("unchecked")
public final class CoverFlow extends AbstractPane
{
	//**********************************************************************
	// Private Class Members
	//**********************************************************************

	private static final String	NAME = "Coverflow";
	private static final String	HINT = "Coverflow Layout for Images";

	//**********************************************************************
	// Private Class Members (Animation)
	//**********************************************************************

	// You can adjust the speed and smoothness of animation if you like.
	private static final double	FRAMERATE = 40.0;
	private static final Duration	DURATION = Duration.seconds(0.6);

	//**********************************************************************
	// Private Class Members (Effects)
	//**********************************************************************

	private static final LinearGradient	GRADIENT =
		new LinearGradient(0.0, 0.0, 0.0, 1.0, true, CycleMethod.NO_CYCLE,
						   new Stop(0.00, Color.web("#606060", 1.00)),
						   new Stop(1.00, Color.web("#202020", 1.00)));

	//**********************************************************************
	// Private Members
	//**********************************************************************

	// Data
	private final List<String>			gdata;		// Genre strings
	private final List<String>			rdata;		// Rating strings

	// Layout
	private StackPane					base;		// Root parent
	private Rectangle					fill;		// Background rectangle
	private Pane						flow;		// Items, in layout order
	private ArrayList<CoverItem>		list;		// Items, in data order

	// Animation
	private final SimpleDoubleProperty	locus;		// Animated index point
	private Animation					animation;

	// TODO #04a: Add members for your left and right navigation buttons here.

	// Handlers
	private final ActionHandler		actionHandler;

	//**********************************************************************
	// Constructors and Finalizer
	//**********************************************************************

	public CoverFlow(Controller controller)
	{
		super(controller, NAME, HINT);

		// Get fixed data sets loaded by model from hardcoded file locations
		gdata = (List<String>)controller.getProperty("genres");
		rdata = (List<String>)controller.getProperty("ratings");

		list = new ArrayList<CoverItem>();
		locus = new SimpleDoubleProperty();

		// Create a listener for various widgets that emit ActionEvents
		actionHandler = new ActionHandler();

		// Construct the pane
		setBase(buildPane());
	}

	//**********************************************************************
	// Public Methods (Controller)
	//**********************************************************************

	// The controller calls this method when it adds a view.
	// Set up the nodes in the view with data accessed through the controller.
	public void	initialize()
	{
		registerWidgetHandlers();

		// Set up initial background and items based on scene size
		fill.setWidth(flow.getWidth());
		fill.setHeight(flow.getHeight());

		// Keep the background the same size as the entire pane
		flow.widthProperty().addListener(this::changeWidth);
		flow.heightProperty().addListener(this::changeHeight);

		// Register a method to handle changes to the locus by the animation
		locus.addListener(this::changeLocus);

		createItems();

		updateItems();
		updatePane();
		updateLayout();
	}

	// The controller calls this method when it removes a view.
	// Unregister event and property listeners for the nodes in the view.
	public void	terminate()
	{
		unregisterWidgetHandlers();

		flow.widthProperty().removeListener(this::changeWidth);
		flow.heightProperty().removeListener(this::changeHeight);

		locus.removeListener(this::changeLocus);

		deleteItems();
	}

	// The controller calls this method whenever something changes in the model.
	// Update the nodes in the view to reflect the change.
	public void	update(String key, Object value)
	{
		// This was used with the old Model properties.
	}

	// The controller calls this method whenever something changes in the model.
	// Update the nodes in the view to reflect the change.
	public void	updateProperty(String key, Object newValue, Object oldValue)
	{
		if ("movie".equals(key))
		{
			updateItems();
			updatePane();
			updateLayout();

			updateAnimation((Movie)newValue);
		}
		else if ("movies".equals(key))
		{
			deleteItems();
			createItems();

			updateItems();
			updatePane();
			updateLayout();
		}
	}

	//**********************************************************************
	// Private Methods (Layout)
	//**********************************************************************

	private Pane	buildPane()
	{
		// Create the background filled with a top-to-bottom gradient
		fill = new Rectangle();
		fill.setFill(GRADIENT);

		// Create a group for laying out items front to back
		flow = new Pane();
		flow.setMinSize(Region.USE_PREF_SIZE, Region.USE_PREF_SIZE);
		flow.setPrefSize(640, 180);
		flow.setMaxSize(Region.USE_PREF_SIZE, Region.USE_PREF_SIZE);

		// Stack the background fill and item flow
		base = new StackPane(fill, flow);

		// Try to keep the keyboard focus to allow keypress navigation
		base.setFocusTraversable(true);
		base.addEventFilter(KeyEvent.KEY_PRESSED, this::handleKeyPressed);
		base.addEventFilter(MouseEvent.MOUSE_ENTERED, this::handleMouseEntered);
		base.addEventFilter(MouseEvent.MOUSE_MOVED, this::handleMouseMoved);

		// TODO #04b: Create your buttons and add them to the base pane on top.
		// A good way is to put them all in a Group and add that to the pane.

		return base;
	}

	//**********************************************************************
	// Private Methods (Widget Management)
	//**********************************************************************

	// TODO #5a: Register each of your buttons with the action handler.
	private void	registerWidgetHandlers()
	{
	}

	// TODO #5b: Unregister each of your buttons with the action handler.
	private void	unregisterWidgetHandlers()
	{
	}

	//**********************************************************************
	// Private Methods (Items)
	//**********************************************************************

	private CoverItem	createItem(Movie movie, boolean selected)
	{
		CoverItem	item = new CoverItem(gdata, rdata);

		item.setMovie(movie);
		item.setSelected(selected);

		// Register a method to handle mouse clicks on each label
		item.addEventHandler(MouseEvent.MOUSE_CLICKED,
							 this::handleMouseClicked);

		return item;
	}

	private void	deleteItem(CoverItem item)
	{
		item.setMovie(null);
		item.setSelected(false);

		// Unregister a method to handle mouse clicks on each label
		item.removeEventHandler(MouseEvent.MOUSE_CLICKED,
								this::handleMouseClicked);
	}

	private void	createItems()
	{
		List<Movie>	movies = (List<Movie>)controller.getProperty("movies");

		for (Movie movie : movies)
			list.add(createItem(movie, false));
	}

	private void	deleteItems()
	{
		for (CoverItem item : list)
			deleteItem(item);

		list.clear();
	}

	private void	updateItems()
	{
		Movie			movie = (Movie)controller.getProperty("movie");

		for (CoverItem item : list)
			item.setSelected(item.getMovie() == movie);
	}

	//**********************************************************************
	// Private Methods (Layout)
	//**********************************************************************

	private int	getCurrentFocus()
	{
		List<Movie>	movies = (List<Movie>)controller.getProperty("movies");
		Movie			movie = (Movie)controller.getProperty("movie");

		if ((movies == null) || (movie == null))
			return 0;
		else
			return movies.indexOf(movie);
	}

	// This method populates the flow with the item for each movie. The items
	// are ordered in a split fashion, with the selected movie on top and movies
	// before and after progressively below it in dovetail fashion.
	//
	// TODO #12: If your design uses a different front-to-back ordering
	// of items, rewrite this code to put them into the flow in that order.
	private void	updatePane()
	{
		int						focus = getCurrentFocus();
		ObservableList<Node>		nodes = flow.getChildren();

		nodes.clear();						// Start empty

		ListIterator<CoverItem>	iprev = list.listIterator(focus);
		ListIterator<CoverItem>	inext = list.listIterator(focus);

		nodes.add(iprev.next());			// Add the selected movie

		if (iprev.hasPrevious())			// Prev movie, if any
			iprev.previous();

		if (inext.hasNext())				// Next movie, if any
			inext.next();

		while (iprev.hasPrevious() || inext.hasNext())
		{
			if (iprev.hasPrevious())		// Dovetail movies from before...
				nodes.add(iprev.previous());

			if (inext.hasNext())			// ...and after, moving out to ends
				nodes.add(inext.next());
		}

		FXCollections.reverse(nodes);
	}

	// TODO #11: Implement the layout of items in your cover flow design.
	// The example code below translates and scales each item relative to the
	// center of the flow, and applies a reflection effect to each one.
	private void	updateLayout()
	{
		List<Movie>	movies = (List<Movie>)controller.getProperty("movies");
		int			focus = getCurrentFocus();			// Selected item

		// Some simple geometric values used in the flow layout
		double			cx = 0.50 * flow.getWidth();		// Flow center x
		double			cy = 0.50 * flow.getHeight();		// Flow center y
		double			uw = flow.getWidth() / 11;			// Per-item width
		double			uh = flow.getHeight();				// Per-item height

		for (CoverItem item : list)
		{
			int	index = movies.indexOf(item.getMovie());

			// The animator interpolates a continuous value of the item index
			double	delta = index - locus.get();	// Animated version
			//int	delta = index - focus;			// Discrete "jump" version

			double	iw = 0.50 * item.getWidth();
			double	ih = 0.50 * item.getHeight();

			item.setTranslateX(cx - iw + delta * uw);
			item.setTranslateY(cy - ih);

			// Make items full-size in the middle, half-size toward the ends
			double	s = Math.max(0.5, 1.00 - 0.05 * delta*delta);

			item.setScaleX(s);
			item.setScaleY(s);

			// Apply reflection effect to the item
			item.setEffect(new Reflection());
		}

		// TODO #06: Make any necessary updates to the layout and styling of
		// your buttons below. Apply the expected enabling/disabling to each
		// one. One way to do this is to use the flow's width and height to
		// calculate absolute positions and sizes for each button.
	}

	private void	updateAnimation(Movie movie)
	{
		if (animation != null)					// Restart animation, since
			animation.stop();					// toLocus needs to change.

		List<Movie>	movies = (List<Movie>)controller.getProperty("movies");
		double			toLocus = (double)movies.indexOf(movie);

		animation = createAnimation(toLocus);	// Prepare to go there...
		animation.play();						// ...then go!
	}

	//**********************************************************************
	// Private Methods (Animation)
	//**********************************************************************

	// Movement amount & speed are proportional to total "distance" to shift
	// the flow from the before item to the after item. Easing is applied to
	// speed up the animation at the beginning and slow it down at the end.
	//
	// TODO #13: Experiment with different progressions, durations/rates,
	// and easing approaches to get the exact animation look you want.
	private Animation	createAnimation(double toLocus)
	{
		Timeline		timeline = new Timeline(FRAMERATE);
		List<KeyFrame>	kfs = timeline.getKeyFrames();

		kfs.add(new KeyFrame(DURATION,			// Rotate to destination
					new KeyValue(locus, toLocus, Interpolator.EASE_BOTH)));

		return timeline;
	}

	//**********************************************************************
	// Inner Classes (Event Handlers)
	//**********************************************************************

	private final class ActionHandler
		implements EventHandler<ActionEvent>
	{
		public void	handle(ActionEvent e)
		{
			Object	source = e.getSource();

			List<Movie>	movies =
				(List<Movie>)controller.getProperty("movies");
			Movie			movie = (Movie)controller.getProperty("movie");

			// TODO #07a: Update which movie is selected in the model, based on
			// which button was clicked, following the design specification.
		}
	}

	//**********************************************************************
	// Private Methods (Property Change Handlers)
	//**********************************************************************

	// Whenever the flow pane resizes, update the background and layout.
	private void	changeWidth(ObservableValue<? extends Number> observable,
								Number oldValue, Number newValue)
	{
		double	w = (Double)newValue;

		fill.setWidth(w);

		updateLayout();
	}

	// Whenever the flow pane resizes, update the background and layout.
	private void	changeHeight(ObservableValue<? extends Number> observable,
								 Number oldValue, Number newValue)
	{
		double	h = (Double)newValue;

		fill.setHeight(h);

		updateLayout();
	}

	// Whenever the locus changes, update positions and features of all items.
	// The animation repeatedly calls this method with interpolated locuss.
	private void	changeLocus(ObservableValue<? extends Number> observable,
								Number oldValue, Number newValue)
	{
		updateLayout();
	}

	//**********************************************************************
	// Private Methods (Input Handlers)
	//**********************************************************************

	private void	handleKeyPressed(KeyEvent e)
	{
		List<Movie>	movies = (List<Movie>)controller.getProperty("movies");
		Movie			movie = (Movie)controller.getProperty("movie");

		// TODO #07b: Update which movie is selected in the model, based on
		// which key was pressed, similar to how the buttons work.
		// Use HOME for first, END for last, PAGE UP for +5, PAGE DOWN
		// for -5, LEFT ARROW for -1, RIGHT ARROW for +1.

		e.consume();	// Consume all presses so they doesn't propagate up
	}

	// Try to hold the keyboard focus (ancestors like to claim it)
	private void	handleMouseEntered(MouseEvent e)
	{
		flow.requestFocus();
	}

	// Try to hold the keyboard focus (ancestors like to claim it)
	private void	handleMouseMoved(MouseEvent e)
	{
		flow.requestFocus();
	}

	// Change the selected item to the one clicked
	private void	handleMouseClicked(MouseEvent e)
	{
		Object		source = e.getSource();

		if (source instanceof CoverItem)
		{
			controller.setProperty("movie", ((CoverItem)source).getMovie());
			e.consume();
		}
	}
}

//******************************************************************************
