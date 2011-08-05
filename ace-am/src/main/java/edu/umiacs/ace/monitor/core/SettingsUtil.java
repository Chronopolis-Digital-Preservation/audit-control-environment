/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.umiacs.ace.monitor.core;

/**
 *
 * @author toaster
 */
public class SettingsUtil {

    /**
     *
     * @param c
     * @param attr
     * @return true if collection, settings are not null and parameter is "true"
     */
    public static boolean getBoolean(Collection c, String attr) {
        if (!containsKey(c, attr)) {
            return false;
        }

        return "true".equalsIgnoreCase(c.getSettings().get(attr));
    }

    public static String getString(Collection c, String attr) {
        if (!containsKey(c, attr)) {
            return null;
        }
        return c.getSettings().get(attr);

    }

    public static int getInt(Collection c, String attr, int def) {
        if (!containsKey(c, attr)) {
            return def;
        }
        try {
            return Integer.parseInt(c.getSettings().get(attr));
        } catch (NumberFormatException e) {
            return def;
        }
    }

    public static boolean containsKey(Collection c, String attr) {
        return (c != null && c.getSettings() != null
                && c.getSettings().containsKey(attr));

    }
}
