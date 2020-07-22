package odin.takehome;

import org.apache.beam.sdk.Pipeline;
import org.apache.beam.sdk.extensions.sorter.BufferedExternalSorter;
import org.apache.beam.sdk.extensions.sorter.SortValues;
import org.apache.beam.sdk.io.TextIO;
import org.apache.beam.sdk.options.PipelineOptionsFactory;
import org.apache.beam.sdk.transforms.*;
import org.apache.beam.sdk.values.KV;
import org.apache.beam.sdk.values.PCollection;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class BytesCounterWindow extends Common {

    private static class FormatAsTextFn extends SimpleFunction<KV<String, Iterable<KV<LocalDateTime, Long>>>, String> {
        @Override
        public String apply(KV<String, Iterable<KV<LocalDateTime, Long>>> input) {
            StringBuilder sb = new StringBuilder();
            sb.append("========================\n");
            String ip = "IP : " + input.getKey() + "\n";
            sb.append(ip);
            Iterator<KV<LocalDateTime, Long>> it = input.getValue().iterator();
            while (it.hasNext()) {
                KV<LocalDateTime, Long> elem = it.next();
                String time = "Time: " + elem.getKey().getYear() + ":" + elem.getKey().getMonth() + ":" + elem.getKey().getDayOfMonth() + ":" + elem.getKey().getHour();
                sb.append(time);
                String bytesServed = ", Bytes: " + elem.getValue();
                sb.append(bytesServed);
                sb.append("\n");
            }
            return sb.toString();
        }
    }

    private static class ExtractWordsFn extends DoFn<String, KV<String, KV<LocalDateTime, Long>>> {

        @ProcessElement
        public void processElement(@Element String element, OutputReceiver<KV<String, KV<LocalDateTime, Long>>> receiver) {
            String[] words = element.split(SEPARATOR);
            long bytesCount = 0L;
            if (!words[words.length - 1].equals("-")) {
                bytesCount = Long.parseLong(words[words.length - 1]);
            }
            String[] timestampArr = words[TIMESTAMP_INDEX].split(":");
            int day = Integer.parseInt(timestampArr[0].substring(1));
            int hour = Integer.parseInt(timestampArr[1]);
            int minute = Integer.parseInt(timestampArr[2]);
            int second = Integer.parseInt(timestampArr[3].substring(0, timestampArr[3].length() - 1));
            LocalDateTime localDateTime = LocalDateTime.of(YEAR, MONTH, day, hour, minute, second);
            receiver.output(KV.of(words[IP_INDEX], KV.of(localDateTime, bytesCount)));
        }
    }

    private static class CalculateWindowsFn extends DoFn<KV<String, Iterable<KV<LocalDateTime, Long>>>, KV<String, Iterable<KV<LocalDateTime, Long>>>> {
        @ProcessElement
        public void processElement(ProcessContext c) {
            String ip = c.element().getKey();
            List<KV<LocalDateTime, Long>> metadata = new ArrayList<>();
            c.element().getValue().iterator().forEachRemaining(metadata::add);

            KV<LocalDateTime, Long> firstElem = metadata.get(0);
            LocalDateTime prevTimestamp = firstElem.getKey();
            long totalBytesHour = firstElem.getValue();

            List<KV<LocalDateTime, Long>> result = new ArrayList<>();

            for (int i = 1; i < metadata.size(); i++) {
                KV<LocalDateTime, Long> elem = metadata.get(i);
                LocalDateTime elementTime = elem.getKey();

                if (elementTime.toEpochSecond(ZoneOffset.UTC) - prevTimestamp.toEpochSecond(ZoneOffset.UTC) <= SECONDS_IN_HOURS) {
                    totalBytesHour += elem.getValue();
                } else {
                    result.add(KV.of(prevTimestamp, totalBytesHour));
                    prevTimestamp = elementTime;
                    totalBytesHour = elem.getValue();
                }
            }
            result.add(KV.of(prevTimestamp, totalBytesHour));
            c.output(KV.of(ip, result));
        }
    }

    private static class CountByIp
            extends PTransform<PCollection<String>, PCollection<KV<String, Iterable<KV<LocalDateTime, Long>>>>> {
        @Override
        public PCollection<KV<String, Iterable<KV<LocalDateTime, Long>>>> expand(PCollection<String> lines) {
            PCollection<KV<String, KV<LocalDateTime, Long>>> words = lines.apply(ParDo.of(new ExtractWordsFn()));
            PCollection<KV<String, Iterable<KV<LocalDateTime, Long>>>> groupedAndSorted = words.apply(GroupByKey.create())
                    .apply(SortValues.<String, LocalDateTime, Long>create(BufferedExternalSorter.options()));


            PCollection<KV<String, Iterable<KV<LocalDateTime, Long>>>> result = groupedAndSorted.apply(ParDo.of(new CalculateWindowsFn()));
            return result;
        }
    }

    private static void bytesByIpWindow(BytesCounterInput options) {
        Pipeline p = Pipeline.create(options);
        p.apply("ReadLines", TextIO.read().from(options.getInputFile()))
                .apply(new CountByIp())
                .apply(MapElements.via(new FormatAsTextFn()))
                .apply("WriteCounts", TextIO.write().to(options.getOutput()));
        p.run().waitUntilFinish();
    }


    public static void main(String[] args) {
        BytesCounterInput options = PipelineOptionsFactory.fromArgs(args).withValidation().as(BytesCounterInput.class);
        bytesByIpWindow(options);
    }
}
