package de.starrunner.sweethome3d;

import de.starrunner.resources.MessageBundle;
import de.starrunner.resources.Messages;

/** 
 * Offers static methods to access our message bundle.
 * 
 * Copyright (c) 2010 by Tobias Liefke
 *
 * @author Tobias Liefke
 * @version 1.0
 */
public final class Msg {
  static {
    Messages.addPackageResources("de.starrunner.sweethome3d", "de.starrunner.sweethome3d.package");
  }

  /**
   * Resolves the localized message for the given key.
   *
   * @param key the key in the associated resource bundle
   * @param arguments the arguments for the message (empty if not used)
   * @return the localized message, filled with the arguments were nessesary
   *
   * @see java.text.MessageFormat
   */
  public static final String msg(String key, Object... arguments) {
    return getMessages().msg(key, arguments);
  }

  /**
   * Resolves our message bundle.
   *
   * @return the message bundle for this package and the current language
   */
  public static final MessageBundle getMessages() {
    return Messages.getMessages(Msg.class);
  }

}
