package cs455.hadoop;

import org.apache.hadoop.io.DoubleWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Reducer;

import java.io.IOException;

public class CoastReducer extends Reducer<Text, DoubleWritable, Text, DoubleWritable>
{
    @Override
    protected void reduce(Text key, Iterable<DoubleWritable> values, Context context) throws IOException, InterruptedException
    {
        /* input: coast - List<measurement> (Text, List<DoubleWritable>)
           output: coast - mean (Text, DoubleWritable)
         */
        double measurementSum = 0.0;
        double countEntries = 0.0;

        for(DoubleWritable val : values) {
            countEntries++;
            measurementSum += val.get();
        }
        double mean = measurementSum/countEntries;

        context.write(key, new DoubleWritable(mean));
    }
}
