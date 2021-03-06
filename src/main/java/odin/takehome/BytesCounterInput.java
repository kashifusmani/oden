package odin.takehome;

import org.apache.beam.sdk.options.Description;
import org.apache.beam.sdk.options.PipelineOptions;
import org.apache.beam.sdk.options.Validation;

public interface BytesCounterInput extends PipelineOptions {
    @Description("Path of the file to read from")
    @Validation.Required
    String getInputFile();

    void setInputFile(String value);

    /**
     * Set this required option to specify where to write the output.
     */
    @Description("Path of the file to write to")
    @Validation.Required
    String getOutput();

    void setOutput(String value);
}