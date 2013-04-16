/*
 * Copyright (c) 2013 Dennis Fischer.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0+
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 *
 * Contributors: Dennis Fischer
 */

package org.chaosfisch.youtubeuploader.db.validation;

import org.chaosfisch.util.TagParser;

public class TagValidator implements Validator<String> {

    @Override
    public boolean validate(final String string) {
        return string == null || TagParser.isValid(string);
    }
}
