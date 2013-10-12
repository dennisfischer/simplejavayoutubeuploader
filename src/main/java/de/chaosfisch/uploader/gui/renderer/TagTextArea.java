/*
 * Copyright (c) 2013 Dennis Fischer.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0+
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 *
 * Contributors: Dennis Fischer
 */

package de.chaosfisch.uploader.gui.renderer;

import com.google.common.base.Splitter;
import com.google.inject.Inject;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.EventHandler;
import javafx.scene.control.TextArea;
import javafx.scene.layout.StackPane;
import javafx.scene.web.WebEvent;
import javafx.scene.web.WebView;
import org.apache.commons.configuration.Configuration;

public class TagTextArea extends StackPane {

	public static final String               OLD_TAG_INPUT = "old_tags";
	private final       SimpleStringProperty tags          = new SimpleStringProperty();
	private final       WebView              webView       = new WebView();
	private final boolean useOldTags;

	@Inject
	public TagTextArea(final Configuration configuration) {
		useOldTags = configuration.getBoolean(OLD_TAG_INPUT, false);
		if (useOldTags) {
			initTextArea();
		} else {
			initPicker(webView);
		}
	}

	private void initTextArea() {
		final TextArea textArea = new TextArea();
		textArea.setWrapText(true);
		textArea.textProperty().bindBidirectional(tags);
		getChildren().add(textArea);
	}

	public void setTags(final String tags) {
		this.tags.set(tags);
		if (!useOldTags) {
			webView.getEngine().getLoadWorker().runningProperty().addListener(new ChangeListener<Boolean>() {
				@Override
				public void changed(final ObservableValue<? extends Boolean> observableValue, final Boolean oldValue, final Boolean newValue) {
					if (!newValue) {
						_createTags(tags);
					}
				}
			});
			if (!webView.getEngine().getLoadWorker().isRunning()) {
				_createTags(tags);
			}
		}
	}

	private void _createTags(final String tags) {
		webView.getEngine().executeScript("$(document).ready(function() { $('#myTags').tagit('removeAll'); });");
		final Iterable<String> tagIterator = Splitter.on(",").omitEmptyStrings().split(tags);
		for (final String tag : tagIterator) {
			webView.getEngine()
					.executeScript(String.format("$(document).ready(function() { $('#myTags').tagit('createTag', \"%s\"); });", tag
							.replace("\"", "\\\"")));
		}
	}

	public String getTags() {
		return tags.get();
	}

	public SimpleStringProperty tagsProperty() {
		return tags;
	}

	// initialize the date picker.
	private void initPicker(final WebView webView) {
		webView.getEngine().loadContent(getInlineHtml());
		webView.setContextMenuEnabled(false);
		webView.getEngine().setOnAlert(new EventHandler<WebEvent<String>>() {
			@Override
			public void handle(final WebEvent<String> event) {
				tags.set(event.getData());
			}
		});

		getChildren().add(webView);
	}

	// return an inline html template based upon the provided initialization parameters.
	private String getInlineHtml() {
		return "<!DOCTYPE html>\n" +
				"<html lang=\"en\">\n" +
				"\t<head>\n" +
				"\t\t<meta charset=\"utf-8\">\n" +
				"\t\t<script src=\"http://ajax.googleapis.com/ajax/libs/jquery/1.5.2/jquery.min.js\" type=\"text/javascript\" charset=\"utf-8\"></script>\n" +
				"\t\t<script src=\"https://ajax.googleapis.com/ajax/libs/jqueryui/1.8.12/jquery-ui.min.js\" type=\"text/javascript\" charset=\"utf-8\"></script>\n" +
				"\t\t<script src=\"http://aehlke.github.io/tag-it/js/tag-it.js\" type=\"text/javascript\" charset=\"utf-8\"></script>\n" +
				"\t\t<link href=\"http://aehlke.github.io/tag-it/css/jquery.tagit.css\" rel=\"stylesheet\" type=\"text/css\">\n" +
				"\t\t<link rel=\"stylesheet\" type=\"text/css\" href=\"http://code.jquery.com/ui/1.10.3/themes/smoothness/jquery-ui.css\">\n" +
				"\t\t<script type=\"text/javascript\">\n" +
				"\t\t\t$(document).ready(function() {\n" +
				"\t\t\t\t$(\"#myTags\").tagit({\n" +
				"\t\t\t\t\tremoveConfirmation: true,\n" +
				"\t\t\t\t\tallowSpaces: true,\n" +
				"\t\t\t\t\tsingleField: true,\n" +
				"\t\t\t\t\tsingleFieldDelimiter: \",\",\n" +
				"\t\t\t\t\tsingleFieldNode: \"#node\",\n" +
				"\t\t\t\t\tafterTagAdded: function(event, ui) {\n" +
				"\t\t\t\t\t\talert($(\"#myTags\").tagit(\"assignedTags\"));\n" +
				"\t\t\t\t\t},\n" +
				"\t\t\t\t\tafterTagRemoved: function(event, ui) {\n" +
				"\t\t\t\t\t\talert($(\"#myTags\").tagit(\"assignedTags\"));\n" +
				"\t\t\t\t\t}\n" +
				"\t\t\t\t});\n" +
				"\t\t\t});\n" +
				"\t\t</script>\n" +
				"\t\t<style type=\"text/css\">\n" +
				"\t\t\tul, html, body {\n" +
				"\t\t\t\toverflow: hidden;\t\n" +
				"    \t\t\t\tmargin: 0;\n" +
				"    \t\t\t\theight: 100%;\n" +
				"\t\t\t}\n" +
				"\t\t\t.ui-corner-all {\n" +
				"\t\t\t\tborder-radius: 0px;\n" +
				"\t\t\t}\n" +
				"\t\t\t.ui-widget {\n" +
				"    \t\t\t\tfont-family: Verdana,Arial,sans-serif;\n" +
				"    \t\t\t\tfont-size: 8pt;\n" +
				"\t\t\t}\n" +
				"\t\t</style>\n" +
				"\t</head>\n" +
				"\t<body>\n" +
				"\t\t<ul id=\"myTags\"></ul>\n" +
				"\t\t<input type=\"hidden\" id=\"node\" />\n" +
				"\t</body>\n" +
				"</html>";
	}
}