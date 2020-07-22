package odin.takehome;


import org.apache.beam.sdk.Pipeline;
import org.apache.beam.sdk.io.TextIO;
import org.apache.beam.sdk.options.PipelineOptionsFactory;
import org.apache.beam.sdk.transforms.*;
import org.apache.beam.sdk.values.KV;
import org.apache.beam.sdk.values.PCollection;

public class BytesCounter {

    private static class FormatAsTextFn extends SimpleFunction<KV<String, Long>, String> {
        @Override
        public String apply(KV<String, Long> input) {
            return input.getKey() + ": " + input.getValue();
        }
    }

    //TODO: Mention that the input file needs to be complete path
    private static class ExtractIpBytes extends DoFn<String, KV<String, Long>> {

        @ProcessElement
        public void processElement(@Element String element, OutputReceiver<KV<String, Long>> receiver) {
            String SEPARATOR = " ";
            int IP_INDEX = 0;
            String[] words = element.split(SEPARATOR);
            long bytesCount = 0L;
            if (! words[words.length - 1].equals("-")) {
                bytesCount = Long.parseLong(words[words.length - 1]);
            }
            receiver.output(KV.of(words[IP_INDEX], bytesCount));
        }
    }

    private static class CountByIp
            extends PTransform<PCollection<String>, PCollection<KV<String, Long>>> {
        @Override
        public PCollection<KV<String, Long>> expand(PCollection<String> lines) {
            PCollection<KV<String, Long>> ipBytes = lines.apply(ParDo.of(new ExtractIpBytes()));
            PCollection<KV<String, Iterable<Long>>> wordCounts = ipBytes.apply(GroupByKey.create());
            return wordCounts.apply(ParDo.of(new DoFn<KV<String, Iterable<Long>>, KV<String, Long>>() {
                @ProcessElement
                public void processElement(ProcessContext c) {
                    String ip = c.element().getKey();
                    Iterable<Long> allBytes = c.element().getValue();
                    Long sumBytes = 0L;
                    for (Long bytes : allBytes) {
                        sumBytes = sumBytes + bytes;
                    }
                    c.output(KV.of(ip, sumBytes));
                }
            }));
        }
    }

    private static void bytesByIp(BytesCounterInput options) {
        Pipeline p = Pipeline.create(options);
        p.apply("ReadLines", TextIO.read().from(options.getInputFile()))
                .apply(new CountByIp())
                .apply(MapElements.via(new FormatAsTextFn()))
                .apply("WriteCounts", TextIO.write().to(options.getOutput()));
        p.run().waitUntilFinish();
    }

    public static void main(String[] args) {
        BytesCounterInput options = PipelineOptionsFactory.fromArgs(args).withValidation().as(BytesCounterInput.class);
        bytesByIp(options);
    }
}
