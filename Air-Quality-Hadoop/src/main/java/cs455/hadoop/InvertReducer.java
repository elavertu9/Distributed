package cs455.hadoop;

import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Reducer;

import java.io.IOException;

public class InvertReducer extends Reducer<IntWritable, Text, IntWritable, Text>
{
    @Override
    protected void reduce(IntWritable key, Iterable<Text> values, Context context) throws IOException, InterruptedException
    {
        for(Text val : values) {
            context.write(key, val);
            break;
        }
    }
}
