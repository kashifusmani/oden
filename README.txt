==Implementation Summmary==
The code contains 2 runnables
    1. BytesCounter: This will output the total number of bytes served for each unique IP
    2. BytesCounterWindow: This will output the total number of bytes served for each unique IP per hour of tumbling window

Explanation of BytesCounter:
    This is a simple implementation where all entries are read from the given file (only ip and bytes served are collected) and grouped by ip address as key.
    Then the total number of bytes served is calculated as sum of all values
    - Example
            Input Payload
                140.112.68.165 [28:23:53:25] "GET /Software.html HTTP/1.0" 200 1497
                140.112.68.165 [29:23:53:36] "GET /Consumer.html HTTP/1.0" 200 1325
                tanuki.twics.com [28:23:53:53] "GET /News.html HTTP/1.0" 200 1014
                wpbfl2-45.gate.net [28:23:54:15] "GET / HTTP/1.0" 200 4889
                wpbfl2-45.gate.net [29:23:54:16] "GET /icons/circle_logo_small.gif HTTP/1.0" 200 2624
                wpbfl2-45.gate.net [30:23:54:18] "GET /logos/small_gopher.gif HTTP/1.0" 200 935
                140.112.68.165 [30:23:54:19] "GET /logos/us-flag.gif HTTP/1.0" 200 2788
                wpbfl2-45.gate.net [28:23:54:19] "GET /logos/small_ftp.gif HTTP/1.0" 200 124
                wpbfl2-45.gate.net [29:23:54:19] "GET /icons/book.gif HTTP/1.0" 200 156
                wpbfl2-45.gate.net [30:23:54:19] "GET /logos/us-flag.gif HTTP/1.0" 200 2788
                tanuki.twics.com [29:23:54:19] "GET /docs/OSWRCRA/general/hotline HTTP/1.0" 302 -
                wpbfl2-45.gate.net [28:23:54:20] "GET /icons/ok2-0.gif HTTP/1.0" 200 231
                tanuki.twics.com [30:23:54:25] "GET /OSWRCRA/general/hotline/ HTTP/1.0" 200 991
                tanuki.twics.com [28:23:54:37] "GET /docs/OSWRCRA/general/hotline/95report HTTP/1.0" 302 -
                wpbfl2-45.gate.net [29:23:54:37] "GET /docs/browner/adminbio.html HTTP/1.0" 200 4217
                tanuki.twics.com [29:23:54:40] "GET /OSWRCRA/general/hotline/95report/ HTTP/1.0" 200 1250
                wpbfl2-45.gate.net [30:23:55:01] "GET /docs/browner/cbpress.gif HTTP/1.0" 200 51661
                140.112.68.165 [28:23:55:21] "GET /Access/chapter1/s2-4.html HTTP/1.0" 200 4602
                tanuki.twics.com [30:23:55:23] "GET /docs/OSWRCRA/general/hotline/95report/05_95mhr.txt.html HTTP/1.0" 200 56431
                wpbfl2-45.gate.net [28:23:55:28] "GET /docs/Access HTTP/1.0" 302 -
                140.112.68.165 [29:23:55:33] "GET /logos/us-flag.gif HTTP/1.0" 200 2788
                wpbfl2-45.gate.net [29:23:55:46] "GET /information.html HTTP/1.0" 200 617
                wpbfl2-45.gate.net [30:23:55:47] "GET /icons/people.gif HTTP/1.0" 200 224
                wpbfl2-45.gate.net [28:23:56:03] "GET /docs/Access HTTP/1.0" 302 -
                wpbfl2-45.gate.net [29:23:56:12] "GET /Access/ HTTP/1.0" 200 2376
                wpbfl2-45.gate.net [30:23:56:14] "GET /Access/images/epaseal.gif HTTP/1.0" 200 2624
                140.112.68.165 [30:23:56:24] "GET /OSWRCRA/general/hotline/95report/ HTTP/1.0" 200 1250
                140.112.68.165 [28:23:56:36] "GET /emap/html/regions/four/ HTTP/1.0" 200 15173
                wpbfl2-45.gate.net [28:23:57:05] "GET /waisicons/unknown.gif HTTP/1.0" 200 83
                140.112.68.165 [29:23:57:06] "GET /OWOW/ HTTP/1.0" 200 1501
                wpbfl2-45.gate.net [29:23:57:08] "POST /cgi-bin/waisgate/134.67.99.11=earth1.epa.gov=210=/indexes/ACCESS=gopher%40earth1.epa.gov=0.00=:free HTTP/1.0" 200 26217
                wpbfl2-45.gate.net [30:23:57:12] "GET /waisicons/text.xbm HTTP/1.0" 200 527

            Output Payload
                140.112.68.165: 30924
                tanuki.twics.com: 59686
                wpbfl2-45.gate.net: 100293


Explanation of BytesCounterWindow
    In this implementation, we collect ip adderss, timestamp and bytes served from all entries.
    Since timestamp does not contain year and month, We assume that all the records belong to same year and month.
    - All entries are grouped by IP address as key and value as List of (timestamp, bytes served) tuples.
    - The List of tuples is then sorted by increasing time.
    - For each IP, the starting time window is defined by the first entry in tuple list. This means that the
        1 hour windows can be different for 2 IP addresses.
    - Example
        Input Payload
            140.112.68.165 [28:23:53:25] "GET /Software.html HTTP/1.0" 200 1497
            140.112.68.165 [29:23:53:36] "GET /Consumer.html HTTP/1.0" 200 1325
            tanuki.twics.com [28:23:53:53] "GET /News.html HTTP/1.0" 200 1014
            wpbfl2-45.gate.net [28:23:54:15] "GET / HTTP/1.0" 200 4889
            wpbfl2-45.gate.net [29:23:54:16] "GET /icons/circle_logo_small.gif HTTP/1.0" 200 2624
            wpbfl2-45.gate.net [30:23:54:18] "GET /logos/small_gopher.gif HTTP/1.0" 200 935
            140.112.68.165 [30:23:54:19] "GET /logos/us-flag.gif HTTP/1.0" 200 2788
            wpbfl2-45.gate.net [28:23:54:19] "GET /logos/small_ftp.gif HTTP/1.0" 200 124
            wpbfl2-45.gate.net [29:23:54:19] "GET /icons/book.gif HTTP/1.0" 200 156
            wpbfl2-45.gate.net [30:23:54:19] "GET /logos/us-flag.gif HTTP/1.0" 200 2788
            tanuki.twics.com [29:23:54:19] "GET /docs/OSWRCRA/general/hotline HTTP/1.0" 302 -
            wpbfl2-45.gate.net [28:23:54:20] "GET /icons/ok2-0.gif HTTP/1.0" 200 231
            tanuki.twics.com [30:23:54:25] "GET /OSWRCRA/general/hotline/ HTTP/1.0" 200 991
            tanuki.twics.com [28:23:54:37] "GET /docs/OSWRCRA/general/hotline/95report HTTP/1.0" 302 -
            wpbfl2-45.gate.net [29:23:54:37] "GET /docs/browner/adminbio.html HTTP/1.0" 200 4217
            tanuki.twics.com [29:23:54:40] "GET /OSWRCRA/general/hotline/95report/ HTTP/1.0" 200 1250
            wpbfl2-45.gate.net [30:23:55:01] "GET /docs/browner/cbpress.gif HTTP/1.0" 200 51661
            140.112.68.165 [28:23:55:21] "GET /Access/chapter1/s2-4.html HTTP/1.0" 200 4602
            tanuki.twics.com [30:23:55:23] "GET /docs/OSWRCRA/general/hotline/95report/05_95mhr.txt.html HTTP/1.0" 200 56431
            wpbfl2-45.gate.net [28:23:55:28] "GET /docs/Access HTTP/1.0" 302 -
            140.112.68.165 [29:23:55:33] "GET /logos/us-flag.gif HTTP/1.0" 200 2788
            wpbfl2-45.gate.net [29:23:55:46] "GET /information.html HTTP/1.0" 200 617
            wpbfl2-45.gate.net [30:23:55:47] "GET /icons/people.gif HTTP/1.0" 200 224
            wpbfl2-45.gate.net [28:23:56:03] "GET /docs/Access HTTP/1.0" 302 -
            wpbfl2-45.gate.net [29:23:56:12] "GET /Access/ HTTP/1.0" 200 2376
            wpbfl2-45.gate.net [30:23:56:14] "GET /Access/images/epaseal.gif HTTP/1.0" 200 2624
            140.112.68.165 [30:23:56:24] "GET /OSWRCRA/general/hotline/95report/ HTTP/1.0" 200 1250
            140.112.68.165 [28:23:56:36] "GET /emap/html/regions/four/ HTTP/1.0" 200 15173
            wpbfl2-45.gate.net [28:23:57:05] "GET /waisicons/unknown.gif HTTP/1.0" 200 83
            140.112.68.165 [29:23:57:06] "GET /OWOW/ HTTP/1.0" 200 1501
            wpbfl2-45.gate.net [29:23:57:08] "POST /cgi-bin/waisgate/134.67.99.11=earth1.epa.gov=210=/indexes/ACCESS=gopher%40earth1.epa.gov=0.00=:free HTTP/1.0" 200 26217
            wpbfl2-45.gate.net [30:23:57:12] "GET /waisicons/text.xbm HTTP/1.0" 200 527

        Output Payload
            ========================
            IP : tanuki.twics.com
            Time: 2020:JULY:28:23, Bytes: 1014
            Time: 2020:JULY:29:23, Bytes: 1250
            Time: 2020:JULY:30:23, Bytes: 57422

            ========================
            IP : wpbfl2-45.gate.net
            Time: 2020:JULY:28:23, Bytes: 5327
            Time: 2020:JULY:29:23, Bytes: 36207
            Time: 2020:JULY:30:23, Bytes: 58759

            ========================
            IP : 140.112.68.165
            Time: 2020:JULY:28:23, Bytes: 21272
            Time: 2020:JULY:29:23, Bytes: 5614
            Time: 2020:JULY:30:23, Bytes: 4038


==How to run==
    - 2 parameters are required --inputFile=/full/path/to/test.txt --output=test-counts
    - To change the tumbling window period, change the SECONDS_IN_HOURS variable in Common.java
    - a sample test.txt file has been included corresponding to above examples.