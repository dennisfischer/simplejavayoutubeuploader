/**************************************************************************************************
 * Copyright (c) 2014 Dennis Fischer.                                                             *
 * All rights reserved. This program and the accompanying materials                               *
 * are made available under the terms of the GNU Public License v3.0+                             *
 * which accompanies this distribution, and is available at                                       *
 * http://www.gnu.org/licenses/gpl.html                                                           *
 *                                                                                                *
 * Contributors: Dennis Fischer                                                                   *
 **************************************************************************************************/

package de.chaosfisch.data.category;

import de.chaosfisch.data.AbstractDAO;
import org.apache.commons.dbutils.QueryRunner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class CategoryDAO extends AbstractDAO<CategoryDTO> implements ICategoryDAO {

	private static final Logger LOGGER = LoggerFactory.getLogger(CategoryDAO.class);
	private final QueryRunner queryRunner;

	public CategoryDAO(final QueryRunner queryRunner) {
		super(CategoryDTO.class);
		this.queryRunner = queryRunner;
	}

	@Override
	public List<CategoryDTO> getAll() {
		try {
			return intern(queryRunner.query("SELECT * FROM categories", listResultSetHandler));
		} catch (final SQLException e) {
			LOGGER.error("Category getAll exception", e);
		}
		return new ArrayList<>(0);
	}

	@Override
	public void store(final CategoryDTO object) {
		try {
			LOGGER.debug("Updating CategoryDTO: {}", object);
			final int changed = queryRunner.update("UPDATE categories SET name=? WHERE youtubeId=?",
												   object.getName(),
												   object.getYoutubeId());
			if (0 == changed) {
				LOGGER.debug("Storing new CategoryDTO: {}", object);
				assert 0 != queryRunner.update("INSERT INTO categories (youtubeId, name) VALUES (?, ?)", object.getYoutubeId(), object.getName());
				intern(object);
			}
		} catch (final SQLException e) {
			LOGGER.error("Category store exception", e);
		}
	}

	@Override
	public void remove(final CategoryDTO object) {
		LOGGER.debug("Removing CategoryDTO: {}", object);
		try {
			assert 0 != queryRunner.update("DELETE FROM categories WHERE youtubeId = ?", object.getYoutubeId());
		} catch (final SQLException e) {
			LOGGER.error("Category remove exception", e);
		}
	}

	@Override
	public CategoryDTO get(final String id) {
		try {
			return intern(queryRunner.query("SELECT * FROM categories WHERE youtubeId = ?", singleResultSetHandler, id));
		} catch (final SQLException e) {
			LOGGER.error("Category get exception", e);
		}
		return null;
	}
}
