package odin.takehome;

import java.time.LocalDateTime;
import java.time.ZoneId;

class Common {
    static String SEPARATOR = " ";
    static int IP_INDEX = 0;
    static int TIMESTAMP_INDEX = 1;
    static int YEAR = LocalDateTime.now(ZoneId.of("UTC")).getYear();
    static int MONTH = LocalDateTime.now(ZoneId.of("UTC")).getMonthValue();
    static int SECONDS_IN_HOURS = 3600;

}
