package cs455.hadoop;

import org.apache.hadoop.io.DoubleWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Reducer;

import java.io.IOException;

public class FortyReducer extends Reducer<Text, DoubleWritable, Text, DoubleWritable>
{
    @Override
    protected void reduce(Text key, Iterable<DoubleWritable> values, Context context) throws IOException, InterruptedException
    {
        /* input: year - List<measurements> (Text, List<DoubleWritable>)
           output: year - mean (Text, DoubleWritable)
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
