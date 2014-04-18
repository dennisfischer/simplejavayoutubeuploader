/**************************************************************************************************
 * Copyright (c) 2014 Dennis Fischer.                                                             *
 * All rights reserved. This program and the accompanying materials                               *
 * are made available under the terms of the GNU Public License v3.0+                             *
 * which accompanies this distribution, and is available at                                       *
 * http://www.gnu.org/licenses/gpl.html                                                           *
 *                                                                                                *
 * Contributors: Dennis Fischer                                                                   *
 **************************************************************************************************/

package de.chaosfisch.youtube.upload.permission;

import de.chaosfisch.data.upload.PermissionDTO;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleObjectProperty;

public class Permission {

	private final SimpleBooleanProperty            ageRestricted       = new SimpleBooleanProperty();
	private final SimpleObjectProperty<Comment>    comment             = new SimpleObjectProperty<>(Comment.ALLOWED);
	private final SimpleBooleanProperty            commentvote         = new SimpleBooleanProperty(true);
	private final SimpleBooleanProperty            embed               = new SimpleBooleanProperty(true);
	private final SimpleBooleanProperty            publicStatsViewable = new SimpleBooleanProperty();
	private final SimpleBooleanProperty            rate                = new SimpleBooleanProperty(true);
	private final SimpleBooleanProperty            subsribers          = new SimpleBooleanProperty(true);
	private final SimpleObjectProperty<ThreeD>     threedD             = new SimpleObjectProperty<>(ThreeD.DEFAULT);
	private final SimpleObjectProperty<Visibility> visibility          = new SimpleObjectProperty<>(Visibility.PUBLIC);

	public Permission(final PermissionDTO permissionDTO) {
		ageRestricted.set(permissionDTO.isAgeRestricted());
		comment.set(Comment.valueOf(permissionDTO.getComment()));
		commentvote.set(permissionDTO.isCommentvote());
		embed.set(permissionDTO.isEmbed());
		publicStatsViewable.set(permissionDTO.isPublicStatsViewable());
		subsribers.set(permissionDTO.isSubcribers());
		rate.set(permissionDTO.isRate());
		threedD.set(ThreeD.valueOf(permissionDTO.getThreedD()));
		visibility.set(Visibility.valueOf(permissionDTO.getVisibility()));
	}

	public Permission() {
	}

	public boolean getSubsribers() {
		return subsribers.get();
	}

	public void setSubsribers(final boolean subsribers) {
		this.subsribers.set(subsribers);
	}

	public SimpleBooleanProperty subsribersProperty() {
		return subsribers;
	}

	public Visibility getVisibility() {
		return visibility.get();
	}

	public void setVisibility(final Visibility visibility) {
		this.visibility.set(visibility);
	}

	public SimpleObjectProperty<Visibility> visibilityProperty() {
		return visibility;
	}

	public ThreeD getThreedD() {
		return threedD.get();
	}

	public void setThreedD(final ThreeD threedD) {
		this.threedD.set(threedD);
	}

	public SimpleObjectProperty<ThreeD> threedDProperty() {
		return threedD;
	}

	public Comment getComment() {
		return comment.get();
	}

	public void setComment(final Comment comment) {
		this.comment.set(comment);
	}

	public SimpleObjectProperty<Comment> commentProperty() {
		return comment;
	}

	public boolean getCommentvote() {
		return commentvote.get();
	}

	public void setCommentvote(final boolean commentvote) {
		this.commentvote.set(commentvote);
	}

	public SimpleBooleanProperty commentvoteProperty() {
		return commentvote;
	}

	public boolean getEmbed() {
		return embed.get();
	}

	public void setEmbed(final boolean embed) {
		this.embed.set(embed);
	}

	public SimpleBooleanProperty embedProperty() {
		return embed;
	}

	public boolean getRate() {
		return rate.get();
	}

	public void setRate(final boolean rate) {
		this.rate.set(rate);
	}

	public SimpleBooleanProperty rateProperty() {
		return rate;
	}

	public boolean getAgeRestricted() {
		return ageRestricted.get();
	}

	public void setAgeRestricted(final boolean ageRestricted) {
		this.ageRestricted.set(ageRestricted);
	}

	public SimpleBooleanProperty ageRestrictedProperty() {
		return ageRestricted;
	}

	public boolean getPublicStatsViewable() {
		return publicStatsViewable.get();
	}

	public void setPublicStatsViewable(final boolean publicStatsViewable) {
		this.publicStatsViewable.set(publicStatsViewable);
	}

	public SimpleBooleanProperty publicStatsViewableProperty() {
		return publicStatsViewable;
	}

	public String getVisibilityIdentifier() {
		return visibility.get().getIdentifier();
	}
}
