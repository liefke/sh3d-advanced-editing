package de.starrunner.resources;

import java.text.*;
import java.util.*;

/**
 * This class provides convenience methods to access 
 * resource bundles containing locale-specific messages. 
 * 
 * Copyright (c) 2005 by Tobias Liefke
 * 
 * @see java.util.ResourceBundle
 *
 * @author Tobias Liefke
 * @version 1.0
 */
public class MessageBundle {
  private ResourceBundle bundle;
  private Locale locale;
  private NumberFormat numberFormat;
  private DateFormat dateFormat;
  private DateFormat timeFormat;
  private DateFormat dateTimeFormat;

  /**
   * Creates a new instance for a given message bundle
   * 
   * @param bundle the message bundle to use
   */
  public MessageBundle(ResourceBundle bundle) {
    this(bundle, bundle.getLocale());
  }

  /**
   * Creates a new instance for a given message bundle
   * 
   * @param bundle the message bundle to use
   * @param locale the locale to use
   */
  public MessageBundle(ResourceBundle bundle, Locale locale) {
    this.bundle = bundle;
    this.locale = locale;
  }

  /**
   * The associated message bundle.
   *
   * @return the message bundle
   */
  public ResourceBundle getBundle() {
    return bundle;
  }

  /**
   * The associated locale.
   * 
   * @return the locale
   */
  public Locale getLocale() {
    return locale;
  }

  /**
   * The language code for the locale, which is a lowercase ISO 639 code.
   * 
   * @return the language of the locale
   */
  public String getLanguage() {
    return locale.getLanguage();
  }

  /**
   * Checks if a message existis for a given key.
   *
   * @param key the key to check
   * @return true, if a message exists, false else
   */
  public boolean hasMsg(String key) {
    try {
      bundle.getString(key);
      return true;
    } catch (MissingResourceException e) {
      return false;
    }
  }

  /**
   * Retrieves a localized string with all arguments replaced.
   * 
   * If the arguments are empty, no replacement takes place.
   * 
   * @param key  the message key in the associated resource bundle
   * @param arguments  the arguments to place into the localized message
   * 
   * @return the localized message, filled with the arguments were nessesary
   * 
   * @see MessageFormat
   */
  public String msg(String key, Object... arguments) {
    String msg;
    try {
      msg = bundle.getString(key);
    } catch (MissingResourceException e) {
      System.err.println("Missing resource for: " + key);
      msg = key;
    }
    if (arguments.length == 0) {
      return msg;
    }
    try {
      return new MessageFormat(msg, locale).format(arguments);
    } catch (Exception e) {
      System.err.println("Could not format message: " + msg + " (" + e.toString() + ')');
      StringBuilder s = new StringBuilder(msg).append(": ");
      for (int i = 0; i < arguments.length; i++) {
        if (i > 0) {
          s.append(", ");
        }
        s.append(arguments[i]);
      }
      return s.toString();
    }
  }

  /**
   * The number format for the associated locale.
   * 
   * @return the number format for the current locale
   */
  public NumberFormat getNumberFormat() {
    if (numberFormat == null) {
      numberFormat = NumberFormat.getNumberInstance(locale);
    }
    return numberFormat;
  }

  /**
   * The short date-time format for the associated locale.
   * 
   * @return the date-time format for the current locale
   */
  public DateFormat getDateTimeFormat() {
    if (dateTimeFormat == null) {
      dateTimeFormat = DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT, locale);
    }
    return dateTimeFormat;
  }

  /**
   * The short date format for the associated locale.
   * 
   * @return the date format for the current locale
   */
  public DateFormat getDateFormat() {
    if (dateFormat == null) {
      dateFormat = DateFormat.getDateInstance(DateFormat.SHORT, locale);
    }
    return dateFormat;
  }

  /**
   * The date format for the associated locale using a pattern.
   * 
   * @param pattern The pattern to use, see {@link SimpleDateFormat}
   * 
   * @return the date format for the current locale
   */
  public DateFormat getDateFormat(String pattern) {
    return new SimpleDateFormat(pattern, locale);
  }

  /**
   * The short time format for the associated locale.
   * 
   * @return the time format for the current locale
   */
  public DateFormat getTimeFormat() {
    if (timeFormat == null) {
      timeFormat = DateFormat.getTimeInstance(DateFormat.SHORT, locale);
    }
    return timeFormat;
  }

}
