package cs455.hadoop;

import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Mapper;

import java.io.IOException;

public class InvertMapper extends Mapper<Text, IntWritable, IntWritable, Text>
{
    @Override
    protected void map(Text key, IntWritable value, Context context) throws IOException, InterruptedException
    {
        context.write(value, key);
    }
    // Input: Alabama - count

}
