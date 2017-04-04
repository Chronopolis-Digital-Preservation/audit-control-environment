package edu.umiacs.ace.util;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * Similar to the {@link FileSizeHandler}, but for KiB/MiB/etc when
 * we may not have access to the tlds
 *
 * Created by shake on 4/4/17.
 */
public class FileSizeFormatter {

    private Unit unit;

    public FileSizeFormatter(String unit) {
        this.unit = Unit.fromString(unit);
    }

    public String format(BigDecimal decimal) {
        // todo: not sure about the scale, it'll probably be something we want to investigate more
        BigDecimal result = decimal.divide(unit.divisor, 5, RoundingMode.HALF_UP);
        String displayUnit = unit == Unit.B ? "" : unit.name();
        return result.stripTrailingZeros().toPlainString() + " " + displayUnit;
    }

    public enum Unit {
        B(0), KiB(1), MiB(2), GiB(3), TiB(4);

        private BigDecimal divisor;

        Unit(int pow) {
            this.divisor = new BigDecimal(Math.pow(1024, pow));
        }

        private static Unit fromString(String unit) {
            if (unit == null) {
                return B;
            }

            if (unit.equalsIgnoreCase("kib")) {
                return KiB;
            } else if (unit.equalsIgnoreCase("mib")) {
                return MiB;
            } else if (unit.equalsIgnoreCase("gib")) {
                return GiB;
            } else if (unit.equalsIgnoreCase("tib")) {
                return TiB;
            }

            return B;
        }

    }

}
