package cs455.hadoop;

import org.apache.hadoop.io.DoubleWritable;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Mapper;
import java.io.IOException;

public class TimeMapper extends Mapper<LongWritable, Text, Text, DoubleWritable>
{
    @Override
    protected void map(LongWritable key, Text value, Context context) throws IOException, InterruptedException
    {
         /* input: file chunk
           output: time<GMT> - so2 (Text, DoubleWritable)
           [11] date gmt 1980-09-09 year-mo-da
           [12] time gmt 21:00
           [13] sample measurement
         */
         String data = value.toString();
         String[] lines = data.split("\n");
         for(String line : lines) {
             String[] chunks = line.split(",");
             String date = chunks[11].replace("\"", "");
             String time = chunks[12].replace("\"", "");
             String measurementStr = chunks[13].replace("\"", "");

            if(withinBounds(date) && onTheHour(time)) {
                try {
                    double measurement = Double.parseDouble(measurementStr);
                    context.write(new Text(time), new DoubleWritable(measurement));
                } catch(NumberFormatException e) {
                    //System.out.println("Encountered formatting error...");
                }
            }
         }
    }

    private boolean onTheHour(String time)
    {
        try {
            String[] chunks = time.split(":");
            int minute = Integer.parseInt(chunks[1]);
            return minute == 0;
        } catch(NumberFormatException e) {
            //System.out.println("Encountered formatting error...");
        }
        return false;
    }

    private boolean withinBounds(String date)
    {
        try {
            // Between 2000 - 2019
            String[] chunks = date.split("-");
            int year = Integer.parseInt(chunks[0]);
            return year >= 2000 && year <= 2019;
        } catch(NumberFormatException e) {
            //System.out.println("Encountered formatting error...");
        }
        return false;
    }
}
