package cs455.hadoop;

import org.apache.hadoop.io.DoubleWritable;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Mapper;
import java.io.IOException;

public class FortyMapper extends Mapper<LongWritable, Text, Text, DoubleWritable>
{
    @Override
    protected void map(LongWritable key, Text value, Context context) throws IOException, InterruptedException
    {
        /* input: File chunk
           output: year - measurement (Text, DoubleWritable)
           [11] date
           [12] time
           [13] sample measurement
         */
        String data = value.toString();
        String[] lines = data.split("\n");
        for(String line : lines) {
            String[] chunks = line.split(",");
            String date = chunks[11].replace("\"", "");
            String[] dateChunks = date.split("-");
            String year = dateChunks[0];
            String measurementStr = chunks[13].replace("\"", "");

            if(withinPastForty(year)) {
                try {
                    double measurement = Double.parseDouble(measurementStr);
                    context.write(new Text(year), new DoubleWritable(measurement));
                } catch(NumberFormatException e) {
                    //System.out.println("Encountered formatting error...");
                }
            }
        }
    }

    private boolean withinPastForty(String yearStr)
    {
        try {
            int currentDate = 2020;
            int year = Integer.parseInt(yearStr);
            return currentDate - year <= 40;
        } catch(NumberFormatException e) {
            //System.out.println("Encountered formatting error...");
        }
        return false;
    }
}
