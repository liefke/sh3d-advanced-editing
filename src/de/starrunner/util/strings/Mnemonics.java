package de.starrunner.util.strings;

import java.awt.Component;

import javax.swing.AbstractButton;
import javax.swing.JLabel;

/**
 * Utility class for working with mnemonics.
 * 
 * A mnemonic is the shortcut key for a button or label, 
 * which is usually underlined. To mark a mnemonic, add 
 * an ampersand ('&') before it, like in 'Open &File' 
 * for 'F' as mnemonic.
 * 
 * This class is not thread safe, use only from one thread 
 * (e.g. the event dispatching one in Swing).
 * 
 * Copyright (c) 1999 by Tobias Liefke
 * 
 * @author Tobias Liefke
 * @version 1.0
 */
public final class Mnemonics {
  /**
   * The character used as marker.
   */
  public static final char MARKER = '&';

  private static char lastMnemonic;
  private static int lastMnemonicPosition;
  private static String lastMnemonicText;
  private static String lastMnemonicExtraction;

  private Mnemonics() { /* Private constructor for utility method. */}

  /**
   * Remove the mnemonic marker from the given text.
   *
   * @param text the text that may contain the marker
   * @return the text without that marker or {@code null} if the text is {@code null}
   */
  public static final String removeMnemonic(String text) {
    if (text == null) {
      return null;
    }
    if (lastMnemonicText == text) {
      return lastMnemonicExtraction;
    }
    lastMnemonicText = text;
    int index = 0;
    do {
      if (index > 0) {
        text = text.substring(0, index) + text.substring(index + 1);
      }
      index = text.indexOf(MARKER, index);
    } while (++index > 0 && text.length() > index && text.charAt(index) == MARKER);
    if (index > 0 && index < text.length()) {
      lastMnemonic = text.charAt(index);
      if (Character.isLetter(lastMnemonic)) {
        lastMnemonic = Character.toUpperCase(lastMnemonic);
      }
      lastMnemonicPosition = index - 1;
      if (index == 1) {
        lastMnemonicExtraction = text.substring(1);
      } else {
        lastMnemonicExtraction = text.substring(0, index - 1) + text.substring(index);
      }
    } else {
      lastMnemonic = 0;
      lastMnemonicPosition = -1;
      lastMnemonicExtraction = text;
    }
    return lastMnemonicExtraction;
  }

  /**
   * Extract the mnemonic character from the given text.
   *
   * @param text the text with a potential marker for a mnemonic or {@code null}
   * 
   * @return the marked character or {@code 0} if none is marked
   */
  public static final char extractMnemonic(String text) {
    if (text == null) {
      return 0;
    }
    if (text != lastMnemonicText) {
      removeMnemonic(text);
    }
    return lastMnemonic;
  }

  /**
   * Extract the position of the mnemonic character from the given text.
   *
   * @param text the text with a potential marker for a mnemonic or {@code null}
   * @return the position of the marked character or {@code -1}
   */
  public static final int extractMnemonicPosition(String text) {
    if (text == null) {
      return -1;
    }
    if (text != lastMnemonicText) {
      removeMnemonic(text);
    }
    return lastMnemonicPosition;
  }

  /**
   * Builds a text that marks the first occurrence of the given mnemonic.
   * 
   * The case is ignored, to mark a specific character use {@link #buildTextWithMnemonic(String, int)}.
   *
   * @param text the text to mark
   * @param mnemonic the character to mark
   * @return the marked string
   */
  public static final String buildTextWithMnemonic(String text, char mnemonic) {
    if (text == null) {
      return null;
    }
    StringBuilder result = new StringBuilder(text.length() + 1);
    mnemonic = Character.toUpperCase(mnemonic);
    boolean hadMnemonic = false;
    for (int i = 0; i < text.length(); i++) {
      char c = text.charAt(i);
      if (!hadMnemonic && Character.toUpperCase(c) == mnemonic) {
        result.append(MARKER);
        hadMnemonic = true;
      } else if (c == MARKER) {
        result.append(MARKER);
      }
      result.append(c);
    }
    return result.toString();
  }

  /**
   * Builds a text that marks the given character position as mnemonic.
   * 
   * @param text the text to mark
   * @param position the position of the character to mark
   * @return the marked string
   */
  public static final String buildTextWithMnemonic(String text, int position) {
    if (text == null) {
      return null;
    }
    StringBuilder sb = new StringBuilder(text.length() + 1);
    for (int i = 0; i < text.length(); i++) {
      char c = text.charAt(i);
      if (position == i || c == MARKER) {
        sb.append(MARKER);
      }
      sb.append(c);
    }
    return sb.toString();
  }

  /**
   * Sets the mnemonic for a button.
   * 
   * Extracts the mnemonic from the current label.
   *
   * @param <B> the class of the button
   * @param button the button to configure
   * @return the configured button for further usage
   */
  public static final <B extends AbstractButton> B configure(B button) {
    return configure(button, button.getText());
  }

  /**
   * Sets the label and mnemonic for a button.
   * 
   * @param <B> the class of the button
   * @param button the button to configure
   * @param text the new label for the button, may contain the marked mnemonic
   * @return the configured button for further usage
   */
  public static final <B extends AbstractButton> B configure(B button, String text) {
    button.setText(removeMnemonic(text));
    button.setMnemonic(lastMnemonic);
    button.setDisplayedMnemonicIndex(lastMnemonicPosition);
    return button;
  }

  /**
   * Sets the mnemonic for a button.
   * 
   * Extracts the mnemonic from the current text of the label.
   *
   * @param <L> the class of the label
   * @param label the label to configure
   * @return the configured label for further usage
   */
  public static final <L extends JLabel> L configure(L label) {
    return configure(label, label.getText());
  }

  /**
   * Sets the text and mnemonic for a label.
   * 
   * @param <L> the class of the label
   * @param label the label to configure
   * @param text the new text for the label, may contain the marked mnemonic
   * @return the configured label for further usage
   */
  public static final <L extends JLabel> L configure(L label, String text) {
    label.setText(removeMnemonic(text));
    label.setDisplayedMnemonic(lastMnemonic);
    label.setDisplayedMnemonicIndex(lastMnemonicPosition);
    return label;
  }

  /**
   * Sets the mnemonic and associated component for a label.
   * 
   * Extracts the mnemonic from the current text of the label.
   * 
   * @param <L> the class of the label
   * @param label the label to configure
   * @param component for this component the given label shows the mnemonic
   * @return the configured label for further usage
   */
  public static final <L extends JLabel> L configure(L label, Component component) {
    return configure(label, label.getText(), component);
  }

  /**
   * Sets the text, mnemonic and associated component for a label.
   * 
   * @param <L> the class of the label
   * @param label the label to configure
   * @param text the new text for the label, may contain the marked mnemonic
   * @param component for this component the given label shows the mnemonic
   * @return the configured label for further usage
   */
  public static final <L extends JLabel> L configure(L label, String text, Component component) {
    configure(label, text);
    label.setLabelFor(component);
    return label;
  }

}