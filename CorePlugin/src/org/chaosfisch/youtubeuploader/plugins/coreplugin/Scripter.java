/*
 * Copyright (c) 2012, Dennis Fischer
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */

package org.chaosfisch.youtubeuploader.plugins.coreplugin;

import org.bushe.swing.event.annotation.AnnotationProcessor;
import org.bushe.swing.event.annotation.EventTopicPatternSubscriber;
import org.mozilla.javascript.Context;
import org.mozilla.javascript.Function;
import org.mozilla.javascript.tools.shell.Global;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Scripter
{
	public Map<String, List<String>> scripts = new HashMap<String, List<String>>(20);
	private final Global  scope;
	private final Context cx;

	public Scripter(final Global scope, final Context cx)
	{
		AnnotationProcessor.process(this);
		this.scope = scope;
		this.cx = cx;
	}

	public void registerFunction(final String event, final String function)
	{
		if (!scripts.containsKey(event)) {
			scripts.put(event, new ArrayList<String>(10));
		}

		scripts.get(event).add(function);
	}

	@EventTopicPatternSubscriber(topicPattern = "(.*)", priority = -1)
	public void onEvent(final String topic, final Object o)
	{
		if (scripts.containsKey(topic)) {
			for (final String function : scripts.get(topic)) {
				final Function fct = (Function) scope.get(function, scope);
				if (fct != Function.NOT_FOUND) {
					fct.call(cx, scope, scope, new Object[]{o});
				}
			}
		}
	}
}