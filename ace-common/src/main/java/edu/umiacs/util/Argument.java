package edu.umiacs.util;

import java.util.Date;

/**
 *
 * @author toaster
 */
public class Argument {

    public static Date dateClone(Date date)
    {
        if (date == null)
            return null;
        else
            return new Date(date.getTime());

    }
}
