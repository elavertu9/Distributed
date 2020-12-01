package cs455.hadoop;

import org.apache.hadoop.io.DoubleWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Reducer;

import java.io.IOException;

public class StateReducer extends Reducer<Text, DoubleWritable, Text, DoubleWritable>
{
    @Override
    protected void reduce(Text key, Iterable<DoubleWritable> values, Context context) throws IOException, InterruptedException
    {
        /* input: state List<so2> (Text, List<DoubleWritable>)
           output: state - mean (Text, DoubleWritable)
           [11] date gmt 1980-09-09 year-mo-da
           [12] time gmt 21:00
           [13] sample measurement
           [21] state name
           (1) Arizona, (2) Puerto Rico, (3) Virgin Islands, (4) Texas, (5) Nevada,
           (6) Mississippi, (7) Florida, (8) Louisiana, (9) Arkansas, (10) Oklahoma
         */
        double count = 0.0;
        double entries = 0.0;
        for(DoubleWritable val : values) {
            count += val.get();
            entries++;
        }
        context.write(key, new DoubleWritable(count/entries));
    }
}
