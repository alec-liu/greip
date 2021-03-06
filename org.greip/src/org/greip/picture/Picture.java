/**
 * Copyright (c) 2016 by Thomas Lorbeer. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 *
 **/
package org.greip.picture;

import java.io.InputStream;

import org.eclipse.swt.SWT;
import org.eclipse.swt.SWTException;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Composite;
import org.greip.common.Util;
import org.greip.decorator.ImageDecorator;
import org.greip.internal.BorderPainter;
import org.greip.internal.IBorderable;

/**
 * This class represents a non-selectable user interface object that displays an
 * image. Supported picture formats are PNG, BMP, JPEG, GIF (including animated
 * GIF), ICO and TIFF.
 * <dl>
 * <dt><b>Styles:</b></dt>
 * <dd>none</dd>
 * <dt><b>Events:</b></dt>
 * <dd>(none)</dd>
 * </dl>
 *
 * @author Thomas Lorbeer
 */
public class Picture extends Composite implements IBorderable {

	private final ImageDecorator decorator;
	private Point scaleTo;

	private final BorderPainter border = new BorderPainter(this);
	private int borderWidth = 0;
	private Color borderColor;
	private int cornerRadius;

	/**
	 * Constructs a new instance of this class given its parent and a style value
	 * describing its behavior and appearance.
	 * <p>
	 * The style value is either one of the style constants defined in class
	 * <code>SWT</code> which is applicable to instances of this class, or must
	 * be built by <em>bitwise OR</em>'ing together (that is, using the
	 * <code>int</code> "|" operator) two or more of those <code>SWT</code> style
	 * constants. The class description lists the style constants that are
	 * applicable to the class. Style bits are also inherited from superclasses.
	 * </p>
	 * <p>
	 * The size of the widget is the scaled size of the image. Is no image
	 * loaded, the size is Point(0, 0).
	 * </p>
	 *
	 * @param parent
	 *        a composite control which will be the parent of the new instance
	 *        (cannot be null)
	 * @param style
	 *        the style of control to construct
	 *
	 * @exception IllegalArgumentException
	 *            <ul>
	 *            <li>ERROR_NULL_ARGUMENT - if the parent is null</li>
	 *            </ul>
	 * @exception SWTException
	 *            <ul>
	 *            <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread
	 *            that created the parent</li>
	 *            </ul>
	 */
	public Picture(final Composite parent, final int style) {
		super(parent, style | SWT.DOUBLE_BUFFERED & ~SWT.BORDER);

		decorator = new ImageDecorator(this);
		addListener(SWT.Paint, e -> {
			if (scaleTo == null && e.height > 0 && e.width > 0) {
				decorator.scaleTo(new Point(e.width - 2 * borderWidth, e.height - 2 * borderWidth));
			}

			decorator.doPaint(e.gc, borderWidth, borderWidth);
			border.doPaint(e.gc, getParent().getBackground());
		});

		scaleTo(new Point(SWT.DEFAULT, SWT.DEFAULT));
	}

	@Override
	public Point computeSize(final int wHint, final int hHint, final boolean changed) {
		checkWidget();
		final Point size = decorator.getSize();
		return new Point(size.x + 2 * borderWidth, size.y + 2 * borderWidth);
	}

	/**
	 * Loads an image from the specified input stream. Throws an error if either
	 * an error occurs while loading the image, or if the image are not of a
	 * supported type.
	 *
	 * @param stream
	 *        the input stream to load the images from
	 *
	 * @exception IllegalArgumentException
	 *            <ul>
	 *            <li>ERROR_NULL_ARGUMENT - if the stream is null</li>
	 *            </ul>
	 * @exception SWTException
	 *            <ul>
	 *            <li>ERROR_IO - if an IO error occurs while reading from the
	 *            stream</li>
	 *            <li>ERROR_INVALID_IMAGE - if the image stream contains invalid
	 *            data</li>
	 *            <li>ERROR_UNSUPPORTED_FORMAT - if the image stream contains an
	 *            unrecognized format</li>
	 *            </ul>
	 */
	public void loadImage(final InputStream stream) {
		decorator.loadImage(stream);
		setSize(decorator.getSize());
	}

	/**
	 * Loads an image from the file with the specified name. Throws an error if
	 * either an error occurs while loading the image, or if the image are not of
	 * a supported type.
	 *
	 * @param filename
	 *        the name of the file to load the images from
	 *
	 * @exception IllegalArgumentException
	 *            <ul>
	 *            <li>ERROR_NULL_ARGUMENT - if the file name is null</li>
	 *            </ul>
	 * @exception SWTException
	 *            <ul>
	 *            <li>ERROR_IO - if an IO error occurs while reading from the
	 *            file</li>
	 *            <li>ERROR_INVALID_IMAGE - if the image file contains invalid
	 *            data</li>
	 *            <li>ERROR_UNSUPPORTED_FORMAT - if the image file contains an
	 *            unrecognized format</li>
	 *            </ul>
	 */
	public void loadImage(final String filename) {
		decorator.loadImage(filename);
		setSize(decorator.getSize());
	}

	/**
	 * Sets the image to the argument, which may be null indicating that no image
	 * should be displayed.
	 *
	 * @param image
	 *        the image to display on the receiver (may be null)
	 *
	 * @exception IllegalArgumentException
	 *            <ul>
	 *            <li>ERROR_INVALID_ARGUMENT - if the image has been
	 *            disposed</li>
	 *            </ul>
	 * @exception SWTException
	 *            <ul>
	 *            <li>ERROR_WIDGET_DISPOSED - if the receiver has been
	 *            disposed</li>
	 *            <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread
	 *            that created the receiver</li>
	 *            </ul>
	 */
	public void setImage(final Image image) {
		decorator.setImage(image);
		setSize(decorator.getSize());
	}

	/**
	 * Scale the image to specified size. Default is <code>Point(SWT.DEFAULT,
	 * SWT.DEFAULT)</code>, that means the original image size.
	 * <code>Point(100, SWT.DEFAULT)</code> means scale to width 100px and
	 * calculate the new height.
	 *
	 * @param scaleTo
	 *        the new image size.
	 *
	 * @exception SWTException
	 *            <ul>
	 *            <li>ERROR_WIDGET_DISPOSED - if the receiver has been
	 *            disposed</li>
	 *            <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread
	 *            that created the receiver</li>
	 *            <li>ERROR_NULL_ARGUMENT - if the new size is null</li>
	 *            </ul>
	 */
	public void scaleTo(final Point scaleTo) {
		this.scaleTo = scaleTo;
		decorator.scaleTo(scaleTo == null ? new Point(SWT.DEFAULT, SWT.DEFAULT) : scaleTo);
		setSize(decorator.getSize());
	}

	/**
	 * Gets the border width around the image.
	 *
	 * @return the border width in pixels.
	 */
	@Override
	public int getBorderWidth() {
		return borderWidth == 0 ? super.getBorderWidth() : borderWidth;
	}

	/**
	 * Sets the border width araund the image.
	 *
	 * @param borderWidth
	 *        the new border width
	 *
	 * @exception IllegalArgumentException
	 *            <ul>
	 *            <li>ERROR_INVALID_ARGUMENT - if the with less than zero</li>
	 *            </ul>
	 * @exception SWTException
	 *            <ul>
	 *            <li>ERROR_WIDGET_DISPOSED - if the receiver has been
	 *            disposed</li>
	 *            <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread
	 *            that created the receiver</li>
	 *            </ul>
	 */
	public void setBorderWidth(final int borderWidth) {
		if (borderWidth < 0) SWT.error(SWT.ERROR_INVALID_ARGUMENT);
		this.borderWidth = borderWidth;
		redraw();
	}

	/**
	 * Get the current border color.
	 *
	 * @return the color
	 */
	@Override
	public Color getBorderColor() {
		return Util.nvl(borderColor, getDisplay().getSystemColor(SWT.COLOR_WIDGET_BORDER));
	}

	/**
	 * Sets the new border color.
	 *
	 * @param borderColor
	 *        the border color or <code>null</code> to sets the default color
	 *
	 * @exception SWTException
	 *            <ul>
	 *            <li>ERROR_WIDGET_DISPOSED - if the receiver has been
	 *            disposed</li>
	 *            <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread
	 *            that created the receiver</li>
	 *            <li>ERROR_NULL_ARGUMENT - if the new size is null</li>
	 *            </ul>
	 */
	public void setBorderColor(final Color borderColor) {
		this.borderColor = Util.checkResource(borderColor, true);
		redraw();
	}

	/**
	 * Gets the radius of the rounded corners.
	 *
	 * @return the radius
	 */
	@Override
	public int getCornerRadius() {
		return cornerRadius;
	}

	/**
	 * Defines the radius of the rounded corners.
	 *
	 * @param cornerRadius
	 *        the radius of the rounded corners
	 *
	 * @exception InvalidArgumentException
	 *            <ul>
	 *            <li>ERROR_INVALID_ARGUMENT - if the corner radius less then
	 *            zero</li>
	 *            </ul>
	 *
	 * @see #setBorderColor(Color)
	 * @see #setBorderWidth(int)
	 */
	public void setCornerRadius(final int cornerRadius) {
		if (cornerRadius < 0) SWT.error(SWT.ERROR_INVALID_ARGUMENT);
		this.cornerRadius = cornerRadius;
		redraw();
	}

	@Override
	public Rectangle getClientArea() {
		final Point size = getSize();
		return new Rectangle(0, 0, size.x, size.y);
	}
}
