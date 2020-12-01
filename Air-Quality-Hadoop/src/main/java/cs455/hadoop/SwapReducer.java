package cs455.hadoop;

import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Reducer;

import java.io.IOException;
import java.util.Map;
import java.util.TreeMap;

public class SwapReducer extends Reducer<Text, IntWritable, Text, IntWritable>
{
    private final TreeMap<IntWritable, Text> middleMan = new TreeMap<>();

    @Override
    protected void reduce(Text key, Iterable<IntWritable> values, Context context) throws IOException, InterruptedException
    {
        /* input: state - 1 (Text, IntWritable)
           output: 1 - state (IntWritable, Text)
         */
        int count = 0;
        for(IntWritable val : values) {
            count += val.get();
        }
        middleMan.put(new IntWritable(count), new Text(key.toString()));
    }

    @Override
    protected void cleanup(Context context) throws IOException, InterruptedException
    {
        for(Map.Entry<IntWritable, Text> entry : middleMan.descendingMap().entrySet()) {
            context.write(entry.getValue(), entry.getKey());
        }
    }
}
