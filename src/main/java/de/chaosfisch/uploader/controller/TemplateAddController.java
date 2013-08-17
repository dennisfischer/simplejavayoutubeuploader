/*
 * Copyright (c) 2013 Dennis Fischer.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0+
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 *
 * Contributors: Dennis Fischer
 */

package de.chaosfisch.uploader.controller;

import com.google.inject.Inject;
import de.chaosfisch.google.youtube.upload.metadata.Metadata;
import de.chaosfisch.google.youtube.upload.metadata.Monetization;
import de.chaosfisch.google.youtube.upload.metadata.Social;
import de.chaosfisch.google.youtube.upload.metadata.permissions.*;
import de.chaosfisch.serialization.IJsonSerializer;
import de.chaosfisch.uploader.ApplicationData;
import de.chaosfisch.uploader.template.ITemplateService;
import de.chaosfisch.uploader.template.Template;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.TextField;

import java.io.File;

public class TemplateAddController extends UndecoratedDialogController {
	@FXML
	public TextField title;

	@Inject
	private IJsonSerializer  jsonSerializer;
	@Inject
	private ITemplateService templateService;

	@FXML
	public void initialize() {
		assert null != title : "fx:id=\"title\" was not injected: check your FXML file 'TemplateAddDialog.fxml'.";
	}

	@FXML
	public void addTemplate(final ActionEvent actionEvent) {
		//TODO MOVE VALIDATION;
		if (!title.getText().isEmpty()) {

			final Template template = jsonSerializer.fromJSON(jsonSerializer.toJSON(standardTemplate), Template.class);
			template.setName(title.getText());
			template.setDefaultdir(new File(template.getDefaultdir().getPath()));
			templateService.insert(template);
			closeDialog(actionEvent);
		}
	}

	public static final Template standardTemplate;

	static {
		final Permissions permissions = new Permissions();
		permissions.setEmbed(true);
		permissions.setCommentvote(true);
		permissions.setComment(Comment.ALLOWED);
		permissions.setRate(true);
		permissions.setVisibility(Visibility.PUBLIC);
		permissions.setVideoresponse(Videoresponse.MODERATED);

		final Social social = new Social();
		social.setFacebook(false);
		social.setTwitter(false);
		social.setMessage("");

		final Monetization monetization = new Monetization();
		monetization.setClaim(false);
		monetization.setOverlay(false);
		monetization.setTrueview(false);
		monetization.setProduct(false);
		monetization.setInstream(false);
		monetization.setInstreamDefaults(false);
		monetization.setPartner(false);
		monetization.setClaimtype(ClaimType.AUDIO_VISUAL);
		monetization.setClaimoption(ClaimOption.MONETIZE);
		monetization.setAsset(Asset.WEB);
		monetization.setSyndication(Syndication.GLOBAL);

		final Metadata metadata = new Metadata();
		standardTemplate = new Template();
		standardTemplate.setPermissions(permissions);
		standardTemplate.setSocial(social);
		standardTemplate.setMonetization(monetization);
		standardTemplate.setMetadata(metadata);
		standardTemplate.setThumbnail(null);
		standardTemplate.setDefaultdir(new File(ApplicationData.HOME));
	}
}
