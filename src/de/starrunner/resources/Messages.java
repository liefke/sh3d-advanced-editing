package de.starrunner.resources;

import java.util.*;

/** 
 * Offers static methods to localize different projects.
 *
 * Copyright (c) 2005 by Tobias Liefke
 * 
 * @author Tobias Noebel
 * @version 1.0
 */
public final class Messages {
  private static final Map<String, MessageBundle> bundles = new HashMap<String, MessageBundle>();
  private static final Map<String, String> packageResources = new HashMap<String, String>();

  static {
    packageResources.put("de.starrunner", "de.starrunner.resources.Messages");
  }

  private static Locale cachedLocale = Locale.getDefault();
  private static final MessageBundle emptyMessages = new MessageBundle(new ListResourceBundle() {
    private final Object[][] contents = new Object[0][];

    @Override
    protected Object[][] getContents() {
      return contents;
    }
  });

  /**
   * Adds the given resource bundle for the given package.
   *
   * @param rootPackage the package that the given resource bundle is responsible for
   * @param resourceBundle the resource bundle (without any .properties suffix)
   */
  public static final void addPackageResources(String rootPackage, String resourceBundle) {
    packageResources.put(rootPackage, resourceBundle);
    for (Iterator<String> entries = bundles.keySet().iterator(); entries.hasNext();) {
      String p = entries.next();
      if (p.startsWith(rootPackage) && (p.length() == rootPackage.length() || p.charAt(rootPackage.length()) == '.')) {
        entries.remove();
      }
    }
  }

  /**
   * Removes a registered package.
   *
   * @param rootPackage the package to remove
   */
  public static final void removePackageResources(String rootPackage) {
    packageResources.remove(rootPackage);
    bundles.remove(rootPackage);
  }

  private static final void checkLocale() {
    if (cachedLocale != Locale.getDefault()) {
      cachedLocale = Locale.getDefault();
      bundles.clear();
    }
  }

  /**
   * Resolves the message for the class of a specific object.
   *
   * If the given object is {@code null}, the default message bundle is used.
   * 
   * @param forObject the object
   * @param key the message key
   * @param arguments the message parameter
   * @return the formated message for that object
   */
  public static final String msgFor(Object forObject, String key, Object... arguments) {
    return getMessages(forObject).msg(key, arguments);
  }

  /**
   * Resolves the message for a specific class.
   *
   * @param forClass the class
   * @param key the message key
   * @param arguments the message parameter
   * @return the formated message for that object
   */
  public static final String msg(Class<?> forClass, String key, Object... arguments) {
    return getMessages(forClass).msg(key, arguments);
  }

  /**
   * Resolves the message bundle for a specific object.
   * 
   * If the given object is {@code null}, the default message bundle is returned.
   *
   * @param forObject the object
   * @return the message bundle for that object
   */
  public static final MessageBundle getMessages(Object forObject) {
    return getMessages(forObject == null ? MessageBundle.class : forObject.getClass());
  }

  /**
   * Resolves the message bundle for a specific class.
   *
   * @param forClass the class
   * @return the message bundle for that class
   */
  public static final MessageBundle getMessages(Class<?> forClass) {
    checkLocale();
    String className = forClass.getName();
    MessageBundle messageBundle = bundles.get(className);
    if (messageBundle == null) {
      String resources = packageResources.get(className);
      String searching = className;
      while (resources == null) {
        int index = searching.lastIndexOf('.');
        if (index < 0) {
          System.err.println("Resources not found for: " + forClass.getName());
          bundles.put(className, emptyMessages);
          return emptyMessages;
        }
        searching = searching.substring(0, index);
        messageBundle = bundles.get(searching);
        if (messageBundle != null) {
          bundles.put(className, messageBundle);
          return messageBundle;
        }
        resources = packageResources.get(searching);
      }
      try {
        messageBundle = new MessageBundle(ResourceBundle.getBundle(resources, cachedLocale));
      } catch (MissingResourceException e) {
        System.err.println("Resourcebundle not found: " + resources);
        messageBundle = emptyMessages;
      }
      bundles.put(className, messageBundle);
    }
    return messageBundle;
  }

}
