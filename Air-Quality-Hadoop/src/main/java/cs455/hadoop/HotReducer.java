package cs455.hadoop;

import org.apache.hadoop.io.DoubleWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Reducer;
import java.io.IOException;

public class HotReducer extends Reducer<Text, DoubleWritable, Text, DoubleWritable>
{
    @Override
    protected void reduce(Text key, Iterable<DoubleWritable> values, Context context) throws IOException, InterruptedException
    {
        /* input: state - measurement[] (Text, List<DoubleWritable>)
           output: state - mean (Text, DoubleWritable)
         */
        double measurementSum = 0.0;
        double countEntries = 0.0;

        for(DoubleWritable val : values) {
            countEntries++;
            measurementSum += val.get();
        }
        context.write(key, new DoubleWritable(measurementSum/countEntries));
    }
}
