/**
 * Copyright (c) 2016 by Thomas Lorbeer
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 **/
package org.greip.decorator;

import org.eclipse.swt.graphics.Cursor;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Listener;

public interface IDecorator {

	void addSettingsChangedListener(Listener listener);

	void dispose();

	void doPaint(GC gc, int x, int y);

	void doPaint(GC gc, Point pos);

	Cursor getCursor();

	Point getSize();

	void removeSettingsChangedListener(Listener listener);

	void setCursor(Cursor cursor);
}