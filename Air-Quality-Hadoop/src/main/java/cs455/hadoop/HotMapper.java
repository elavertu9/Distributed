package cs455.hadoop;

import org.apache.hadoop.io.DoubleWritable;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Mapper;
import java.io.IOException;

public class HotMapper extends Mapper<LongWritable, Text, Text, DoubleWritable>
{
    @Override
    protected void map(LongWritable key, Text value, Context context) throws IOException, InterruptedException
    {
        /* input: File Chunk
           output: state - measurement (Text, DoubleWritable)
         */
        String data = value.toString();
        String[] lines = data.split("\n");
        for(String line : lines) {
            String[] chunks = line.split(",");

            String date = chunks[11].replace("\"", "");
            String measurementStr = chunks[13];
            String[] monthStr = date.split("-");
            String stateName = chunks[21].replace("\"", "");

            try {
                if(monthStr.length > 1) {
                    int month = Integer.parseInt(monthStr[1]);
                    double measurement = Double.parseDouble(measurementStr);

                    if(month == 7 || month == 8 || month == 9) {
                        context.write(new Text(stateName), new DoubleWritable(measurement));
                    }
                }
            } catch(NumberFormatException e) {
                //System.out.println("Encountered improperly formatted data...");
            }
        }
    }
}
