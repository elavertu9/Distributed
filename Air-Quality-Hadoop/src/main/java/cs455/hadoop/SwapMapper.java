package cs455.hadoop;

import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Mapper;

import java.io.IOException;

public class SwapMapper extends Mapper<Text, Text, Text, IntWritable>
{
    IntWritable intWritable = new IntWritable(1);

    @Override
    protected void map(Text key, Text value, Context context) throws IOException, InterruptedException
    {
        /* input: state - uid (Text, Text)
           output: state - 1 (Text, IntWritable)
         */
        context.write(key, intWritable);
    }
}
