package edu.umiacs.util;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;

/**
 * @author toaster
 */
public class Argument {

    public static Date dateClone(Date date) {
        if (date == null) {
            return null;
        } else {
            Instant instant = date.toInstant().truncatedTo(ChronoUnit.SECONDS);
            return Date.from(instant);
        }

    }
}
