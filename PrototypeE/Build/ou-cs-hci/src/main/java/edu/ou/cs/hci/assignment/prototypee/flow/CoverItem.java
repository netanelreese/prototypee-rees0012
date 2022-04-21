//******************************************************************************
// Copyright (C) 2020 University of Oklahoma Board of Trustees.
//******************************************************************************
// Last modified: Mon Apr 13 18:23:17 2020 by Chris Weaver
//******************************************************************************
// Major Modification History:
//
// 20200412 [weaver]:	Original file.
//
//******************************************************************************
//
//******************************************************************************

package edu.ou.cs.hci.assignment.prototypee.flow;

//import java.lang.*;
import java.util.List;
import javafx.beans.value.ObservableValue;
import javafx.scene.Cursor;
import javafx.scene.control.*;
import javafx.scene.effect.*;
import javafx.scene.image.*;
import javafx.scene.input.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.*;
import javafx.scene.text.*;
import edu.ou.cs.hci.assignment.prototypee.Movie;

//******************************************************************************

/**
 * The <CODE>CoverItem</CODE> class.
 *
 * @author  Chris Weaver
 * @version %I%, %G%
 */
public final class CoverItem extends StackPane
{
	//**********************************************************************
	// Public Class Members (Resources)
	//**********************************************************************

	public static final String	RSRC		= "edu/ou/cs/hci/resources/";
	public static final String	FX_ICON	= RSRC + "example/fx/icon/";

	//**********************************************************************
	// Public Class Methods (Resources)
	//**********************************************************************

	// Convenience method to create a node for an image located in resources
	// relative to the FX_ICON package. See static member definitions above.
	public static ImageView	createFXIcon(String url, double w, double h)
	{
		Image	image = new Image(FX_ICON + url, w, h, false, true);

		return new ImageView(image);
	}

	//**********************************************************************
	// Private Class Members (Effects)
	//**********************************************************************

	private static final Font			FONT =
		Font.font("Serif", FontWeight.BOLD, FontPosture.ITALIC, 18.0);

	private static final ColorAdjust	COLOR_ADJUST =
		new ColorAdjust(-0.25, -0.25, -0.25, 0.0);

	private static final Glow			GLOW =
		new Glow(0.5);

	//**********************************************************************
	// Private Members
	//**********************************************************************

	// Data
	private final List<String>		gdata;		// Genre strings
	private final List<String>		rdata;		// Rating strings

	// Members
	private Movie					movie;		// Movie represented
	private boolean				selected;	// Selected in the coverflow?

	// TODO #08: Add members for the elements used in your item layout.
	private Label					label;

	//**********************************************************************
	// Constructors and Finalizer
	//**********************************************************************

	public CoverItem(List<String> gdata, List<String> rdata)
	{
		// Get fixed data sets loaded by model from hardcoded file locations
		this.gdata = gdata;
		this.rdata = rdata;

		movie = null;
		selected = false;

		createLayout();

		// Use default layout and styling whenever the movie is null
		updateLayout();						// Build the layout
		updateStyles();						// Apply any styles
	}

	//**********************************************************************
	// Public Methods (Getters and Setters)
	//**********************************************************************

	public Movie	getMovie()
	{
		return movie;
	}

	public void	setMovie(Movie movie)
	{
		if (this.movie == movie)				// No change, do nothing
			return;

		if (this.movie != null)				// Unregister old movie
			unregisterPropertyListeners();

		this.movie = movie;					// Remember new movie

		updateLayout();						// Rebuild the layout
		updateStyles();						// Reapply any styles

		if (this.movie != null)				// Register new movie
			registerPropertyListeners();
	}

	public boolean	getSelected()
	{
		return selected;
	}

	public void	setSelected(boolean selected)
	{
		if (this.selected == selected)			// No change, do nothing
			return;

		this.selected = selected;				// Remember new selection state

		// In many designs, changing the selection only affects styles.
		// Comment out the next line if selection doesn't affect layout.
		updateLayout();						// Rebuild the layout
		updateStyles();						// Reapply any styles
	}

	//**********************************************************************
	// Private Methods (Layout)
	//**********************************************************************

	// TODO #09a: Create default layout and styling of the item.
	private void	createLayout()
	{
		// Create, style, and layout widgets in their initial state.
		// Focus on the aspects that don't change when the item is selected.
		label = new Label();
		label.setFont(FONT);
		label.setTextAlignment(TextAlignment.CENTER);
		label.setRotate(45.0);

		// You can use more levels of pane to allow application of extra effects
		StackPane	pane = new StackPane(label);

		pane.setEffect(new DropShadow());

		getChildren().addAll(pane);
	}

	// TODO #09b: Update item layout when its movie is selected/unselected.
	private void	updateLayout()
	{
		if (movie == null)
		{
			label.setText("");
		}
		else
		{
			label.setText(movie.getTitle());
		}
	}

	// TODO #09c: Update item styling when its movie is selected/unselected.
	private void	updateStyles()
	{
		if (selected)	// Make the item appear brighter
		{
			label.setTextFill(Color.YELLOW);
			label.setEffect(GLOW);
		}
		else			// Make the item appear darker
		{
			label.setTextFill(Color.WHITE);
			label.setEffect(null);
		}
	}

	//**********************************************************************
	// Private Methods (Property Management)
	//**********************************************************************

	// TODO #10a: Uncomment lines below for the movie properties you use.
	private void	registerPropertyListeners()
	{
		movie.titleProperty().addListener(this::handleChangeS);
		//movie.imageProperty().addListener(this::handleChangeS);

		//movie.yearProperty().addListener(this::handleChangeI);
		//movie.ratingProperty().addListener(this::handleChangeS);
		//movie.runtimeProperty().addListener(this::handleChangeI);

		//movie.awardPictureProperty().addListener(this::handleChangeB);
		//movie.awardDirectingProperty().addListener(this::handleChangeB);
		//movie.awardCinematographyProperty().addListener(this::handleChangeB);
		//movie.awardActingProperty().addListener(this::handleChangeB);

		//movie.averageReviewScoreProperty().addListener(this::handleChangeD);
		//movie.numberOfReviewsProperty().addListener(this::handleChangeI);
		//movie.genreProperty().addListener(this::handleChangeI);

		//movie.directorProperty().addListener(this::handleChangeS);
		//movie.isAnimatedProperty().addListener(this::handleChangeB);
		//movie.isColorProperty().addListener(this::handleChangeB);

		//movie.summaryProperty().addListener(this::handleChangeS);
		//movie.commentsProperty().addListener(this::handleChangeS);
	}

	// TODO #10b: Uncomment lines below for the movie properties you use.
	private void	unregisterPropertyListeners()
	{
		movie.titleProperty().removeListener(this::handleChangeS);
		//movie.imageProperty().removeListener(this::handleChangeS);

		//movie.yearProperty().removeListener(this::handleChangeI);
		//movie.ratingProperty().removeListener(this::handleChangeS);
		//movie.runtimeProperty().removeListener(this::handleChangeI);

		//movie.awardPictureProperty().removeListener(this::handleChangeB);
		//movie.awardDirectingProperty().removeListener(this::handleChangeB);
		//movie.awardCinematographyProperty().removeListener(this::handleChangeB);
		//movie.awardActingProperty().removeListener(this::handleChangeB);

		//movie.averageReviewScoreProperty().removeListener(this::handleChangeD);
		//movie.numberOfReviewsProperty().removeListener(this::handleChangeI);
		//movie.genreProperty().removeListener(this::handleChangeI);

		//movie.directorProperty().removeListener(this::handleChangeS);
		//movie.isAnimatedProperty().removeListener(this::handleChangeB);
		//movie.isColorProperty().removeListener(this::handleChangeB);

		//movie.summaryProperty().removeListener(this::handleChangeS);
		//movie.commentsProperty().removeListener(this::handleChangeS);
	}

	//**********************************************************************
	// Private Methods (Property Change Handlers)
	//**********************************************************************

	private void	handleChangeS(ObservableValue<? extends String> observable,
								  String oldValue, String newValue)
	{
		updateLayout();
		updateStyles();
	}

	private void	handleChangeB(ObservableValue<? extends Boolean> observable,
								  Boolean oldValue, Boolean newValue)
	{
		updateLayout();
		updateStyles();
	}

	private void	handleChangeD(ObservableValue<? extends Number> observable,
								  Number oldValue, Number newValue)
	{
		updateLayout();
		updateStyles();
	}

	private void	handleChangeI(ObservableValue<? extends Number> observable,
								  Number oldValue, Number newValue)
	{
		updateLayout();
		updateStyles();
	}
}

//******************************************************************************
