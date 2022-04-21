//******************************************************************************
// Copyright (C) 2019-2020 University of Oklahoma Board of Trustees.
//******************************************************************************
// Last modified: Sun Apr 12 19:51:48 2020 by Chris Weaver
//******************************************************************************
// Major Modification History:
//
// 20190203 [weaver]:	Original file.
// 20190220 [weaver]:	Adapted from swingmvc to fxmvc.
// 20200212 [weaver]:	Overhauled for Sp2020 PrototypeB.
// 20200228 [weaver]:	Added example solution code for PrototypeB.
// 20200229 [weaver]:	Overhauled for new PrototypeC in Spring 2020.
//
//******************************************************************************
//
//******************************************************************************

package edu.ou.cs.hci.assignment.prototypee.pane;

//import java.lang.*;
import java.util.*;
import javafx.beans.value.ObservableValue;
import javafx.collections.*;
import javafx.event.*;
import javafx.geometry.*;
import javafx.scene.*;
import javafx.scene.control.*;
import javafx.scene.image.*;
import javafx.scene.layout.*;
import javafx.scene.paint.*;
import javafx.scene.text.*;
import edu.ou.cs.hci.assignment.prototypee.*;
import edu.ou.cs.hci.resources.Resources;

//******************************************************************************

/**
 * The <CODE>EditorPane2</CODE> class.
 *
 * @author  Chris Weaver
 * @version %I%, %G%
 */
@SuppressWarnings("unchecked")
public final class EditorPane2 extends AbstractPane
{
	//**********************************************************************
	// Private Class Members
	//**********************************************************************

	private static final String	NAME = "Editor2";
	private static final String	HINT = "Movie Metadata Editor version 2";

	//**********************************************************************
	// Private Class Members (Layout)
	//**********************************************************************

	private static final double	W = 280;		// Item icon width
	private static final double	H = W * 1.5;	// Item icon height

	private static final Insets	PADDING =
		new Insets(10.0, 10.0, 10.0, 10.0);

	private static final Insets	PADDING_SMALL =
		new Insets(2.0, 2.0, 2.0, 2.0);

	//**********************************************************************
	// Private Class Members (Styling)
	//**********************************************************************

	private static final Font		FONT_LABEL =
		Font.font("SansSerif", FontWeight.BOLD, 12.0);

	private static final Border	BORDER = new Border(
		new BorderStroke(Color.LIGHTGRAY, BorderStrokeStyle.SOLID,
						 CornerRadii.EMPTY, BorderStroke.THIN));

	//**********************************************************************
	// Private Members
	//**********************************************************************

	// Data
	private final List<String>		gdata;		// Genre strings
	private final List<String>		rdata;		// Rating strings

	// Layout (widgets in same order as model)
	private TextField				cTitle;
	private TextField				cImageFile;
	private Spinner<Integer>		cYear;
	private ComboBox<String>		cRating;
	private Slider					cRuntime;

	private CheckBox				cAwardPicture;
	private CheckBox				cAwardDirecting;
	private CheckBox				cAwardCinematography;
	private CheckBox				cAwardActing;

	private Spinner<Double>		cAverageReviewScore;
	private Spinner<Integer>		cNumberOfReviews;
	private ArrayList<CheckBox>	cGenres;

	private TextField				cDirector;
	private CheckBox				cIsAnimated;
	private CheckBox				cIsColor;
	private TextArea				cSummary;
	private TextArea				cComments;

	// Layout (extras widgets for movie poster image)
	private ImageView				cImageView;
	private Button					cImageButton;

	// Support
	private boolean				ignoreRangeEvents;

	// Handlers
	private final ActionHandler	actionHandler;

	//**********************************************************************
	// Constructors and Finalizer
	//**********************************************************************

	public EditorPane2(Controller controller)
	{
		super(controller, NAME, HINT);

		// Get fixed data sets loaded by model from hardcoded file locations
		gdata = (List<String>)controller.getProperty("genres");
		rdata = (List<String>)controller.getProperty("ratings");

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

		Movie	movie = (Movie)controller.getProperty("movie");

		if (movie == null)
			populateWidgetsWithDefaultValues();
		else
			populateWidgetsWithCurrentValues(movie);

		if (movie != null)
			registerPropertyListeners(movie);
	}

	// The controller calls this method when it removes a view.
	// Unregister event and property listeners for the nodes in the view.
	public void	terminate()
	{
		Movie	movie = (Movie)controller.getProperty("movie");

		if (movie != null)
			unregisterPropertyListeners(movie);

		unregisterWidgetHandlers();
	}

	// The controller calls this method whenever something changes in the model.
	// Update the nodes in the view to reflect the change.
	public void	update(String key, Object value)
	{
		// This was used with the old Model properties. Just for TextAreas now!

		// Apply updates that involve text+anchor+caret handling in TextAreas
		Movie	movie = (Movie)controller.getProperty("movie");

		if (movie == null)
			return;

		if (//"movie.summary".equals(key) ||
			"movie.summary.anchor".equals(key) ||
			"movie.summary.caret".equals(key))
		{
			String	text = movie.getSummary();//(String)controller.getProperty("movie.summary");
			int	anchor = (Integer)controller.get("movie.summary.anchor");
			int	caret = (Integer)controller.get("movie.summary.caret");

			ignoreRangeEvents = true;
			cSummary.setText(text);
			cSummary.selectRange(anchor, caret);
			ignoreRangeEvents = false;
		}

		if (//"movie.comments".equals(key) ||
			"movie.comments.anchor".equals(key) ||
			"movie.comments.caret".equals(key))
		{
			String	text = movie.getComments();//(String)controller.getProperty("movie.comments");
			int	anchor = (Integer)controller.get("movie.comments.anchor");
			int	caret = (Integer)controller.get("movie.comments.caret");

			ignoreRangeEvents = true;
			cComments.setText(text);
			cComments.selectRange(anchor, caret);
			ignoreRangeEvents = false;
		}
	}

	// The controller calls this method whenever something changes in the model.
	// Update the nodes in the view to reflect the change.
	public void	updateProperty(String key, Object newValue, Object oldValue)
	{
		//System.out.println("EditorPane2.updateProperty " + key +
		//				   " to " + newValue + " from " + oldValue);

		if ("movie".equals(key))
		{
			Movie	mold = (Movie)oldValue;
			Movie	mnew = (Movie)newValue;

			if (mold != null)
				unregisterPropertyListeners(mold);

			if (mnew == null)
				populateWidgetsWithDefaultValues();
			else
				populateWidgetsWithCurrentValues(mnew);

			if (mnew != null)
				registerPropertyListeners(mnew);
		}
	}

	//**********************************************************************
	// Private Methods (Widget and Property Management)
	//**********************************************************************

	private void	registerWidgetHandlers()
	{
		cTitle.setOnAction(actionHandler);
		cImageFile.setOnAction(actionHandler);
		cYear.valueProperty().addListener(this::changeInteger);
		cRating.getSelectionModel().selectedItemProperty().addListener(
														this::changeItem);
		cRuntime.valueProperty().addListener(this::changeDecimal);

		cAwardPicture.setOnAction(actionHandler);
		cAwardDirecting.setOnAction(actionHandler);
		cAwardCinematography.setOnAction(actionHandler);
		cAwardActing.setOnAction(actionHandler);

		cAverageReviewScore.valueProperty().addListener(this::changeDecimal);
		cNumberOfReviews.valueProperty().addListener(this::changeInteger);

		for (CheckBox c : cGenres)
			c.setOnAction(actionHandler);

		cDirector.setOnAction(actionHandler);
		cIsAnimated.setOnAction(actionHandler);
		cIsColor.setOnAction(actionHandler);

		cSummary.textProperty().addListener(this::changeComment);
		cSummary.anchorProperty().addListener(this::changeRange);
		cSummary.caretPositionProperty().addListener(this::changeRange);

		cComments.textProperty().addListener(this::changeComment);
		cComments.anchorProperty().addListener(this::changeRange);
		cComments.caretPositionProperty().addListener(this::changeRange);

		cImageButton.setOnAction(actionHandler);
	}

	private void	unregisterWidgetHandlers()
	{
		cTitle.setOnAction(null);
		cImageFile.setOnAction(null);
		cYear.valueProperty().removeListener(this::changeInteger);
		cRating.getSelectionModel().selectedItemProperty().removeListener(
														this::changeItem);
		cRuntime.valueProperty().removeListener(this::changeDecimal);

		cAwardPicture.setOnAction(null);
		cAwardDirecting.setOnAction(null);
		cAwardCinematography.setOnAction(null);
		cAwardActing.setOnAction(null);

		cAverageReviewScore.valueProperty().removeListener(this::changeDecimal);
		cNumberOfReviews.valueProperty().removeListener(this::changeInteger);

		for (CheckBox c : cGenres)
			c.setOnAction(null);

		cDirector.setOnAction(null);
		cIsAnimated.setOnAction(null);
		cIsColor.setOnAction(null);

		cSummary.textProperty().removeListener(this::changeComment);
		cSummary.anchorProperty().removeListener(this::changeRange);
		cSummary.caretPositionProperty().removeListener(this::changeRange);

		cComments.textProperty().removeListener(this::changeComment);
		cComments.anchorProperty().removeListener(this::changeRange);
		cComments.caretPositionProperty().removeListener(this::changeRange);

		cImageButton.setOnAction(null);
	}

	private void	registerPropertyListeners(Movie movie)
	{
		movie.titleProperty().addListener(this::handleChangeS);
		movie.imageProperty().addListener(this::handleChangeS);

		movie.yearProperty().addListener(this::handleChangeI);
		movie.ratingProperty().addListener(this::handleChangeS);
		movie.runtimeProperty().addListener(this::handleChangeI);

		movie.awardPictureProperty().addListener(this::handleChangeB);
		movie.awardDirectingProperty().addListener(this::handleChangeB);
		movie.awardCinematographyProperty().addListener(this::handleChangeB);
		movie.awardActingProperty().addListener(this::handleChangeB);

		movie.averageReviewScoreProperty().addListener(this::handleChangeD);
		movie.numberOfReviewsProperty().addListener(this::handleChangeI);
		movie.genreProperty().addListener(this::handleChangeI);

		movie.directorProperty().addListener(this::handleChangeS);
		movie.isAnimatedProperty().addListener(this::handleChangeB);
		movie.isColorProperty().addListener(this::handleChangeB);

		movie.summaryProperty().addListener(this::handleChangeS);
		movie.commentsProperty().addListener(this::handleChangeS);
	}

	private void	unregisterPropertyListeners(Movie movie)
	{
		movie.titleProperty().removeListener(this::handleChangeS);
		movie.imageProperty().removeListener(this::handleChangeS);

		movie.yearProperty().removeListener(this::handleChangeI);
		movie.ratingProperty().removeListener(this::handleChangeS);
		movie.runtimeProperty().removeListener(this::handleChangeI);

		movie.awardPictureProperty().removeListener(this::handleChangeB);
		movie.awardDirectingProperty().removeListener(this::handleChangeB);
		movie.awardCinematographyProperty().removeListener(this::handleChangeB);
		movie.awardActingProperty().removeListener(this::handleChangeB);

		movie.averageReviewScoreProperty().removeListener(this::handleChangeD);
		movie.numberOfReviewsProperty().removeListener(this::handleChangeI);
		movie.genreProperty().removeListener(this::handleChangeI);

		movie.directorProperty().removeListener(this::handleChangeS);
		movie.isAnimatedProperty().removeListener(this::handleChangeB);
		movie.isColorProperty().removeListener(this::handleChangeB);

		movie.summaryProperty().removeListener(this::handleChangeS);
		movie.commentsProperty().removeListener(this::handleChangeS);
	}

	private void	populateWidgetsWithDefaultValues()
	{
		cTitle.setText("");
		cImageFile.setText("");
		cYear.getValueFactory().setValue(0);
		cRating.getSelectionModel().select(0);
		cRuntime.setValue(0);

		cAwardPicture.setSelected(false);
		cAwardDirecting.setSelected(false);
		cAwardCinematography.setSelected(false);
		cAwardActing.setSelected(false);

		cAverageReviewScore.getValueFactory().setValue(0.0);
		cNumberOfReviews.getValueFactory().setValue(0);

		for (CheckBox c : cGenres)
			c.setSelected(false);

		cDirector.setText("");
		cIsAnimated.setSelected(false);
		cIsColor.setSelected(false);

		cSummary.setText("");
		cComments.setText("");

		cImageView.setImage(null);
	}

	private void	populateWidgetsWithCurrentValues(Movie movie)
	{
		cTitle.setText(movie.getTitle());
		cImageFile.setText(movie.getImage());
		cYear.getValueFactory().setValue(movie.getYear());
		cRating.getSelectionModel().select(movie.getRating());
		cRuntime.setValue(movie.getRuntime());

		cAwardPicture.setSelected(movie.getAwardPicture());
		cAwardDirecting.setSelected(movie.getAwardDirecting());
		cAwardCinematography.setSelected(movie.getAwardCinematography());
		cAwardActing.setSelected(movie.getAwardActing());

		cAverageReviewScore.getValueFactory().setValue(movie.getAverageReviewScore());
		cNumberOfReviews.getValueFactory().setValue(movie.getNumberOfReviews());

		// Map bit positions to genre hits/misses
		int	genre = movie.getGenre();
		int	n = 0;

		for (CheckBox c : cGenres)
		{
			c.setSelected((genre & (1 << n)) != 0);
			n++;
		}

		cDirector.setText(movie.getDirector());
		cIsAnimated.setSelected(movie.getIsAnimated());
		cIsColor.setSelected(movie.getIsColor());

		cSummary.setText(movie.getSummary());
		cComments.setText(movie.getComments());

		cImageView.setImage(movie.getImageAsImage(FX_ICON, W, H));
	}

	//**********************************************************************
	// Private Methods (Layout)
	//**********************************************************************

	private Pane	buildPane()
	{
		buildWidgets();

		return buildLayout();
	}

	// Create and set up widgets, in same order as attributes in model
	private void	buildWidgets()
	{
		cTitle = new TextField();
		cTitle.setPrefColumnCount(40);

		cImageFile = new TextField();
		cImageFile.setPrefColumnCount(18);
		cImageFile.setPromptText("Location of image file");

		cYear = new Spinner<Integer>(1900, 2040, 0, 1);
		cYear.setEditable(true);
		cYear.getEditor().setPrefColumnCount(5);

		cRating = new ComboBox<String>();
		cRating.getItems().addAll(rdata);
		cRating.setEditable(false);
		cRating.setVisibleRowCount(5);

		cRuntime = new Slider(0.0, 360.0, 120.0);
		cRuntime.setOrientation(Orientation.HORIZONTAL);
		cRuntime.setMajorTickUnit(120.0);
		cRuntime.setMinorTickCount(120);
		cRuntime.setShowTickLabels(true);
		cRuntime.setShowTickMarks(false);
		cRuntime.setSnapToTicks(true);

		cAwardPicture = buildCheckBox("Picture");
		cAwardDirecting = buildCheckBox("Directing");
		cAwardCinematography = buildCheckBox("Cinematography");
		cAwardActing = buildCheckBox("Acting");

		cAverageReviewScore = new Spinner<Double>(0.0, 10.0, 0.0, 0.1);
		cAverageReviewScore.setEditable(true);
		cAverageReviewScore.getEditor().setPrefColumnCount(4);

		cNumberOfReviews = new Spinner<Integer>(0, 9999999, 0, 1);
		cNumberOfReviews.setEditable(true);
		cNumberOfReviews.getEditor().setPrefColumnCount(7);

		cGenres = new ArrayList<CheckBox>();

		for (String genre : gdata)
			cGenres.add(buildCheckBox(genre));

		cDirector = new TextField();
		cDirector.setPrefColumnCount(40);

		cIsAnimated = buildCheckBox("Animated");
		cIsColor = buildCheckBox("Color");

		// Set up Summary TextArea to track text, caret, and selection actions.
		cSummary = new TextArea();
		cSummary.setMinSize(200, 0);
		cSummary.setPrefRowCount(10);
		cSummary.setWrapText(true);

		// Set up Comments TextArea to track text, caret, and selection actions.
		cComments = new TextArea();
		cComments.setPrefColumnCount(72);
		cComments.setWrapText(true);

		cImageView = new ImageView();
		cImageButton = new Button("Choose...");
	}

	// Did the layout of widgets using no subpanes and absolute positioning for
	// everything, including any Labels or other Nodes used for decoration. See
	// the API documentation of javafx.scene.layout.Pane and the examples below.
	private Pane	buildLayout()
	{
		// Create a parent pane to contain all widgets, labels, decorations
		Pane	pane = new Pane();

		// Example 1: Create and position a Label for the cTitle TextField
		//Label	lTitle = new Label("Title");

		//lTitle.setFont(FONT_LABEL);
		//lTitle.relocate(10, 10);

		// Example 2: Position the cTitle TextField itself
		//cTitle.relocate(10, 25);
		//cTitle.setPrefWidth(251);

		// Add all of the widgets, labels, decoration nodes, etc. to the pane
		//pane.getChildren().addAll(lTitle, cTitle);

		// Note: The below reproduces the exact starting positions of all labels
		// and widgets in the original EditorPane, *without any GPoD tweaking*.
		placeNode(pane,  10,  10, createLabel("Title"));
		placeNode(pane,  10,  25, cTitle);
		cTitle.setPrefWidth(251);

		placeNode(pane, 281,  10, createLabel("Year"));
		placeNode(pane, 281,  25, cYear);

		placeNode(pane, 378,  10, createLabel("Rating"));
		placeNode(pane, 378,  25, cRating);
		cDirector.setPrefWidth(90);

		placeNode(pane, 482,  10, createLabel("Runtime"));
		placeNode(pane, 482,  25, cRuntime);

		placeNode(pane,  10,  72, createLabel("Director"));
		placeNode(pane,  10,  87, cDirector);
		cDirector.setPrefWidth(251);

		placeNode(pane, 281, 201, createLabel("Average Score"));
		placeNode(pane, 281, 216, cAverageReviewScore);

		placeNode(pane, 281, 263, createLabel("Number of Reviews"));
		placeNode(pane, 281, 278, cNumberOfReviews);

		placeNode(pane,  10, 134, createLabel("Summary"));
		placeNode(pane,  10, 156, cSummary);
		cSummary.setPrefSize(251, 157);

		placeNode(pane, 122, 134, cIsAnimated);
		placeNode(pane, 203, 134, cIsColor);

		placeNode(pane,  10, 333, createLabel("Comments"));
		placeNode(pane,  10, 348, cComments);
		cComments.setPrefSize(612, 152);

		placeNode(pane, 281,  78, createLabel("Awards"));
		placeNode(pane, 281,  93, cAwardPicture);
		placeNode(pane, 281, 115, cAwardDirecting);
		placeNode(pane, 281, 137, cAwardCinematography);
		placeNode(pane, 281, 159, cAwardActing);

		placeNode(pane, 421,  78, createLabel("Genres"));

		int	y = 93;

		for (CheckBox cGenre : cGenres)
		{
			placeNode(pane, 421, y, cGenre);
			y += 22;
		}

		BorderPane	pimage = new BorderPane(cImageView);

		pimage.setBorder(BORDER);
		pimage.setPrefSize(308, 459);

		placeNode(pane, 642,  10, pimage);

		placeNode(pane, 642, 473, cImageFile);
		cImageFile.setPrefWidth(233);

		placeNode(pane, 879, 473, cImageButton);

		// Return the parent pane as the "root" of the EditorPane2 layout
		return pane;
	}

	private Label	createLabel(String text)
	{
		Label	label = new Label(text);

		label.setFont(FONT_LABEL);

		return label;
	}

	private void	placeNode(Pane pane, int x, int y, Node node)
	{
		node.relocate(x, y);
		pane.getChildren().addAll(node);
	}

	// Builds a padded Checkbox with the text.
	private CheckBox	buildCheckBox(String text)
	{
		CheckBox	c = new CheckBox(text);

		c.setPadding(PADDING_SMALL);

		return c;
	}

	// Builds a padded VBox with the text (as a label) above the child.
	private VBox	buildLabeledVBox(String text, Node child)
	{
		Label	label = new Label(text);
		VBox	vbox = new VBox(label, child);

		label.setFont(FONT_LABEL);
		vbox.setPadding(PADDING);

		return vbox;
	}

	//**********************************************************************
	// Private Methods (Property Change Handlers)
	//**********************************************************************

	// For ComboBox
	private void	changeItem(ObservableValue<? extends String> observable,
							   String oldValue, String newValue)
	{
		Movie	movie = (Movie)controller.getProperty("movie");

		if (movie == null)
			return;

		if (observable == cRating.getSelectionModel().selectedItemProperty())
			movie.setRating(newValue);
	}

	// For Slider, Spinner<Double>
	private void	changeDecimal(ObservableValue<? extends Number> observable,
								  Number oldValue, Number newValue)
	{
		Movie	movie = (Movie)controller.getProperty("movie");

		if (movie == null)
			return;

		if (observable == cRuntime.valueProperty())
			movie.setRuntime((int)Math.floor((Double)newValue));
		else if (observable == cAverageReviewScore.valueProperty())
			movie.setAverageReviewScore((Double)newValue);
	}

	// For Spinner<Integer>
	private void	changeInteger(ObservableValue<? extends Number> observable,
								  Number oldValue, Number newValue)
	{
		Movie	movie = (Movie)controller.getProperty("movie");

		if (movie == null)
			return;

		if (observable == cYear.valueProperty())
			movie.setYear((Integer)newValue);
		else if (observable == cNumberOfReviews.valueProperty())
			movie.setNumberOfReviews((Integer)newValue);
	}

	// For TextArea (changes to the text itself)
	private void	changeComment(ObservableValue<? extends String> observable,
								  String oldValue, String newValue)
	{
		Movie	movie = (Movie)controller.getProperty("movie");

		if (movie == null)
			return;

		if (observable == cSummary.textProperty())
			movie.setSummary(newValue);
		else if (observable == cComments.textProperty())
			movie.setComments(newValue);
	}

	// For TextArea (changes to range = caret position + selection anchor)
	private void	changeRange(ObservableValue<? extends Number> observable,
								Number oldValue, Number newValue)
	{
		// Ignore changes to TextArea range that come indirectly from the model.
		if (ignoreRangeEvents)
			return;

		// Handle changes to TextArea range that come directly from the user.
		if (observable == cSummary.caretPositionProperty())
			controller.set("movie.summary.caret", newValue);
		else if (observable == cComments.caretPositionProperty())
			controller.set("movie.comments.caret", newValue);

		if (observable == cSummary.anchorProperty())
			controller.set("movie.summary.anchor", newValue);
		else if (observable == cComments.anchorProperty())
			controller.set("movie.comments.anchor", newValue);
	}

	//**********************************************************************
	// Inner Classes (Event Handlers)
	//**********************************************************************

	// For Button, CheckBox, TextField
	private final class ActionHandler
		implements EventHandler<ActionEvent>
	{
		public void	handle(ActionEvent e)
		{
			Object	source = e.getSource();
			Movie	movie = (Movie)controller.getProperty("movie");

			if (movie == null)
				return;

			if (source == cTitle)
				movie.setTitle(cTitle.getText());
			else if (source == cImageFile)
				movie.setImage(cImageFile.getText());
			else if (source == cAwardPicture)
				movie.setAwardPicture(cAwardPicture.isSelected());
			else if (source == cAwardDirecting)
				movie.setAwardDirecting(cAwardDirecting.isSelected());
			else if (source == cAwardCinematography)
				movie.setAwardCinematography(cAwardCinematography.isSelected());
			else if (source == cAwardActing)
				movie.setAwardActing(cAwardActing.isSelected());
			else if (source == cDirector)
				movie.setDirector(cDirector.getText());
			else if (source == cIsAnimated)
				movie.setIsAnimated(cIsAnimated.isSelected());
			else if (source == cIsColor)
				movie.setIsColor(cIsColor.isSelected());
			else if (source == cImageButton)
				controller.trigger("choosing new image file for movie");
			else
				handleGenres(source, movie);
		}

		private void	handleGenres(Object source, Movie movie)
		{
			int		genre = movie.getGenre();
			int		n = 0;
			boolean	changed = false;

			for (CheckBox c : cGenres)
			{
				if (source == c)
				{
					genre = (c.isSelected() ? (genre+(1<<n)) : (genre-(1<<n)));
					changed = true;
				}

				n++;
			}

			if (changed)
				movie.setGenre(genre);
		}
	}

	//**********************************************************************
	// Private Methods (Property Change Handlers, Movie)
	//**********************************************************************

	private void	handleChangeS(ObservableValue<? extends String> observable,
								  String oldValue, String newValue)
	{
		Movie	movie = (Movie)controller.getProperty("movie");

		if (observable == movie.titleProperty())
			cTitle.setText(newValue);
		else if (observable == movie.imageProperty())
			cImageFile.setText(newValue);
		else if (observable == movie.ratingProperty())
			cRating.getSelectionModel().select(newValue);
		else if (observable == movie.directorProperty())
			cDirector.setText(newValue);

		else if (observable == movie.summaryProperty())
			cSummary.setText(newValue);// Needs caret handling like update()
		else if (observable == movie.commentsProperty())
			cComments.setText(newValue);// Needs caret handling like update()
	}

	private void	handleChangeB(ObservableValue<? extends Boolean> observable,
						   Boolean oldValue, Boolean newValue)
	{
		Movie	movie = (Movie)controller.getProperty("movie");

		if (observable == movie.awardPictureProperty())
			cAwardPicture.setSelected(newValue);
		else if (observable == movie.awardDirectingProperty())
			cAwardDirecting.setSelected(newValue);
		else if (observable == movie.awardCinematographyProperty())
			cAwardCinematography.setSelected(newValue);
		else if (observable == movie.awardActingProperty())
			cAwardActing.setSelected(newValue);

		else if (observable == movie.isAnimatedProperty())
			cIsAnimated.setSelected(newValue);
		else if (observable == movie.isColorProperty())
			cIsColor.setSelected(newValue);
	}

	private void	handleChangeD(ObservableValue<? extends Number> observable,
						   Number oldValue, Number newValue)
	{
		Movie	movie = (Movie)controller.getProperty("movie");

		if (observable == movie.averageReviewScoreProperty())
			cAverageReviewScore.getValueFactory().setValue((Double)newValue);
	}

	private void	handleChangeI(ObservableValue<? extends Number> observable,
						   Number oldValue, Number newValue)
	{
		Movie	movie = (Movie)controller.getProperty("movie");

		if (observable == movie.yearProperty())
			cYear.getValueFactory().setValue((Integer)newValue);
		else if (observable == movie.runtimeProperty())
			cRuntime.setValue((Integer)newValue);
		else if (observable == movie.numberOfReviewsProperty())
			cNumberOfReviews.getValueFactory().setValue((Integer)newValue);
		else if (observable == movie.genreProperty())
		{
			// Map bit positions to genre hits/misses
			int	genre = (Integer)newValue;
			int	n = 0;

			for (CheckBox c : cGenres)
			{
				c.setSelected((genre & (1 << n)) != 0);
				n++;
			}
		}
	}
}

//******************************************************************************
