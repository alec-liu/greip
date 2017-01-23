/**
 * Copyright (c) 2016 by Thomas Lorbeer
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 **/
package org.greip.calculator;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.ParsePosition;
import java.util.function.BiConsumer;
import java.util.function.BiPredicate;

import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.dnd.Clipboard;
import org.eclipse.swt.dnd.TextTransfer;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.greip.common.Util;

public class Calculator extends Composite {

	private enum KeyHandlers {
		CTRL_C(
			(e, c) -> e.stateMask == SWT.CTRL && e.keyCode == 'c',
			(e, c) -> {
				final Clipboard cb = new Clipboard(e.display);
				final TextTransfer textTransfer = TextTransfer.getInstance();
				cb.setContents(new Object[] { c.lblResult.getText() }, new Transfer[] { textTransfer });
			}),
		CTRL_MINUS(
			(e, c) -> e.keyCode == '-' && e.stateMask == SWT.CTRL,
			(e, c) -> c.processAction('�')),
		CTRL_CR(
			(e, c) -> e.keyCode == SWT.CR && e.stateMask == SWT.CTRL,
			(e, c) -> {
				c.processAction('=');
				c.propagateValue();
			}),
		DEFAULT_ACTION(
			(e, c) -> c.formula.isLegalAction(e.character),
			(e, c) -> c.processAction(e.character));

		private final BiPredicate<Event, Calculator> predicate;
		private final BiConsumer<Event, Calculator> consumer;

		KeyHandlers(final BiPredicate<Event, Calculator> predicate, final BiConsumer<Event, Calculator> consumer) {
			this.predicate = predicate;
			this.consumer = consumer;
		}

		public static void execute(final Event e, final Calculator calculator) {
			for (final KeyHandlers handler : values()) {
				if (handler.predicate.test(e, calculator)) {
					handler.consumer.accept(e, calculator);
					break;
				}
			}
		}
	}

	private static final char SPACER = (char) -1;

	private Composite resultPanel;
	private Label lblResult;
	private Label lblFormula;
	private Text txtFocus;

	private final Formula formula = new Formula();
	private BigDecimal value = BigDecimal.ZERO;
	private Color resultBackground;
	private Color resultForeground;

	public Calculator(final Composite parent) {
		super(parent, SWT.NONE);

		setLayout(GridLayoutFactory.fillDefaults().margins(5, 5).spacing(2, 2).numColumns(5).create());
		setBackground(getDisplay().getSystemColor(SWT.COLOR_DARK_GRAY));
		setBackgroundMode(SWT.INHERIT_FORCE);
		addListener(SWT.Resize, e -> showFormula());

		createFocusControl();
		createResultPanel();
		createButtons();
	}

	private void createFocusControl() {
		txtFocus = new Text(this, SWT.NONE);
		txtFocus.setLayoutData(GridDataFactory.fillDefaults().exclude(true).create());
		txtFocus.addListener(SWT.KeyDown, e -> KeyHandlers.execute(e, this));
	}

	private void createResultPanel() {
		resultPanel = new Composite(this, SWT.BORDER);
		resultPanel.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false, 5, 1));
		resultPanel.setLayout(GridLayoutFactory.swtDefaults().margins(3, 1).spacing(0, 0).create());
		resultPanel.setBackground(getResultBackground());

		new Label(this, SWT.NONE).setLayoutData(GridDataFactory.fillDefaults().hint(4, 2).span(5, 1).create());

		lblFormula = createInfoLabel(resultPanel);
		Util.applyDerivedFont(lblFormula, -2, SWT.NONE);

		lblResult = createInfoLabel(resultPanel);
		lblResult.setText("0");
		Util.applyDerivedFont(lblResult, 2, SWT.BOLD);
	}

	private Label createInfoLabel(final Composite parent) {
		final Label lbl = new Label(parent, SWT.RIGHT);

		lbl.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
		lbl.setBackground(getResultBackground());
		lbl.setForeground(getResultForeground());

		return lbl;
	}

	private void createButtons() {
		createButtonsFor('7', '8', '9', SPACER, '/', 'C');
		createButtonsFor('4', '5', '6', SPACER, '*', '�');
		createButtonsFor('1', '2', '3', SPACER, '-');
		createButton('=', 1, 2, 0);
		createButton('0', 2, 1, 0);
		createButtonsFor(',', SPACER, '+');
	}

	private void createButtonsFor(final char... actions) {
		int indent = 0;

		for (int i = 0; i < actions.length; i++) {
			if (actions[i] == SPACER) {
				indent = 3;
			} else {
				createButton(actions[i], 1, 1, indent);
				indent = 0;
			}
		}
	}

	private Button createButton(final char action, final int hSpan, final int vSpan, final int indent) {
		final Button btn = new Button(this, SWT.PUSH);

		btn.setText(String.valueOf(action));
		btn.addListener(SWT.Selection, e -> processAction(action));
		btn.addListener(SWT.Traverse, e -> e.doit = (e.detail != SWT.TRAVERSE_RETURN));
		btn.setLayoutData(
				GridDataFactory.fillDefaults().span(hSpan, vSpan).grab(true, true).minSize(30, SWT.DEFAULT).indent(indent, 0).create());

		return btn;
	}

	private void processAction(final char action) {

		try {
			lblResult.setText(formula.processAction(action));
			lblResult.setForeground(getResultForeground());

		} catch (final ParseException | ArithmeticException e) {
			lblResult.setText("Fehler");
			lblResult.setForeground(getDisplay().getSystemColor(SWT.COLOR_RED));
		}

		showFormula();
		txtFocus.setFocus();
	}

	private void showFormula() {
		Util.withResource(new GC(lblFormula), gc -> {
			final int width = lblFormula.getSize().x;
			lblFormula.setText(reverse(Util.shortenText(gc, reverse(formula.format()), width, SWT.NONE)));
		});
	}

	private static String reverse(final String text) {
		return new StringBuilder(text).reverse().toString();
	}

	private void propagateValue() {
		value = (BigDecimal) formula.getDecimalFormat().parse(lblResult.getText(), new ParsePosition(0));
		notifyListeners(SWT.Selection, new Event());
	}

	public void setValue(final BigDecimal value) {
		formula.init(value);
		processAction('=');
	}

	public BigDecimal getValue() {
		return value;
	}

	public void setDecimalFormat(final DecimalFormat format) {
		formula.setDecimalFormat(format);
		processAction('=');
	}

	private Color getResultForeground() {
		return Util.nvl(resultForeground, getDisplay().getSystemColor(SWT.COLOR_INFO_FOREGROUND));
	}

	public void setResultForeground(final Color resultForeground) {
		this.resultForeground = resultForeground;

		lblResult.setForeground(resultForeground);
		lblFormula.setForeground(resultForeground);
	}

	private Color getResultBackground() {
		return Util.nvl(resultBackground, getDisplay().getSystemColor(SWT.COLOR_INFO_BACKGROUND));
	}

	public void setResultBackground(final Color resultBackground) {
		this.resultBackground = resultBackground;

		lblResult.setBackground(resultBackground);
		lblFormula.setBackground(resultBackground);
		resultPanel.setBackground(resultBackground);
	}
}