package cs455.hadoop;

import org.apache.hadoop.io.DoubleWritable;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Mapper;
import java.io.IOException;

public class StateMapper extends Mapper<LongWritable, Text, Text, DoubleWritable>
{
    @Override
    protected void map(LongWritable key, Text value, Context context) throws IOException, InterruptedException
    {
        /* input: file chunk
           output: state - so2 (Text, DoubleWritable)
           [11] date gmt 1980-09-09 year-mo-da
           [12] time gmt 21:00
           [13] sample measurement
           [21] state name
           (1) Arizona, (2) Puerto Rico, (3) Virgin Islands, (4) Texas, (5) Nevada,
           (6) Mississippi, (7) Florida, (8) Louisiana, (9) Arkansas, (10) Oklahoma
         */
        String data = value.toString();
        String[] lines = data.split("\n");
        for(String line : lines) {
            String[] chunks = line.split(",");
            String stateName = chunks[21].replace("\"", "");

            String measurementStr = chunks[13].replace("\"", "");

            if(stateName.equals("Arizona") || stateName.equals("Puerto Rico") || stateName.equals("Virgin Islands") ||
                stateName.equals("Texas") || stateName.equals("Nevada") || stateName.equals("Mississippi") || stateName.equals("Florida") ||
                stateName.equals("Louisiana") || stateName.equals("Arkansas") || stateName.equals("Oklahoma")) {
                try {
                    double measurement = Double.parseDouble(measurementStr);
                    context.write(new Text(stateName), new DoubleWritable(measurement));
                } catch(NumberFormatException e) {
                    //System.out.println("Encountered formatting error...");
                }
            }
        }

    }
}
