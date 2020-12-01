package cs455.hadoop;

import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Reducer;
import java.io.IOException;

public class SiteReducer extends Reducer<Text, Text, Text, Text>
{
    @Override
    protected void reduce(Text key, Iterable<Text> values, Context context) throws IOException, InterruptedException
    {
        /* input: uid - state (Text, Text)
           output: state - uid (Text, Text)
         */
        for(Text val : values) {
            context.write(val, key);
            break;
        }
    }
}
