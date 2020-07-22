package odin.takehome;

import java.time.LocalDateTime;
import java.time.ZoneId;

public class Common {
    public static int TIMESTAMP_INDEX = 1;
    public static int YEAR = LocalDateTime.now(ZoneId.of("UTC")).getYear();
    public static int MONTH = LocalDateTime.now(ZoneId.of("UTC")).getMonthValue();

}
