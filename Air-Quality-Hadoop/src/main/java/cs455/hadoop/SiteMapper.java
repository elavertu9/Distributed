package cs455.hadoop;

import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Mapper;
import java.io.IOException;

public class SiteMapper extends Mapper<LongWritable, Text, Text, Text>
{
    @Override
    protected void map(LongWritable key, Text value, Context context) throws IOException, InterruptedException
    {
        /* input: file chunk
           output: uid - state (Text, Text)
           uid = stateCode + countyCode + siteNum
         */
        String data = value.toString();
        String[] lines = data.split("\n");
        for(String line : lines) {
            String[] chunks = line.split(",");

            String code = chunks[0].replace("\"", "") + chunks[1].replace("\"", "") + chunks[2].replace("\"", "");
            String stateName = chunks[21].replace("\"", "");

            context.write(new Text(code), new Text(stateName));
        }

    }
}
