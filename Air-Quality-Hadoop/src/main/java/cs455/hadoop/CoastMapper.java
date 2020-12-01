package cs455.hadoop;

import org.apache.hadoop.io.DoubleWritable;
import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Mapper;

import java.io.IOException;


public class CoastMapper extends Mapper<LongWritable, Text, Text, DoubleWritable>
{
    enum CoastStates {
        ALASKA(2, "West Coast"), CALIFORNIA(6, "West Coast"), CONNECTICUT(9, "East Coast"), DELAWARE(10, "East Coast"), FLORIDA(12, "East Coast"), GEORGIA(13, "East Coast"), MAINE(23, "East Coast"),
        MARYLAND(24, "East Coast"), MASSACHUSETTS(25, "East Coast"), NEW_HAMPSHIRE(33, "East Coast"), NEW_JERSEY(34, "East Coast"), NEW_YORK(36, "East Coast"), NORTH_CAROLINA(37, "East Coast"),
        OREGON(41, "West Coast"), PENNSYLVANIA(42, "East Coast"), RHODE_ISLAND(44, "East Coast"), SOUTH_CAROLINA(45, "East Coast"), VIRGINIA(51, "East Coast"), WASHINGTON(53, "West Coast");

        private int stateCode;
        private String coast;

        CoastStates(int stateCode, String coast) {
            this.stateCode = stateCode;
            this.coast = coast;
        }

        public int getStateCode() {
            return this.stateCode;
        }

        public String getCoast() {
            return this.coast;
        }
    }

    @Override
    protected void map(LongWritable key, Text value, Context context) throws IOException, InterruptedException
    {
        /* input: file chunk
           output: coast - measurement (Text, DoubleWritable)
         */
        String data = value.toString();
        String[] lines = data.split("\n");
        for(String line : lines) {
            String[] chunks = line.split(",");
            String stateCodeStr = chunks[0].replace("\"", "");
            String measurementStr = chunks[13];
            //String units = chunks[14].replace("\"", "");
            //String stateName = chunks[21].replace("\"", "");

            try {
                int stateCode = Integer.parseInt(stateCodeStr);
                double measurement = Double.parseDouble(measurementStr);

                String coast = whichCoast(stateCode);

                if(!coast.equals("none")) {
                    context.write(new Text(coast), new DoubleWritable(measurement));
                }
            } catch(NumberFormatException e) {
                //System.out.println("Encountered improperly formatted data...");
            }
        }
    }

    private String whichCoast(int location)
    {
        CoastStates[] states = CoastStates.values();
        for(CoastStates state : states) {
            if(location == state.getStateCode()) {
                return state.getCoast();
            }
        }
        return "none";
    }
}
