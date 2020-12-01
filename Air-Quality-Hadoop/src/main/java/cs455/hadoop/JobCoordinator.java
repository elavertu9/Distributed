package cs455.hadoop;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.DoubleWritable;
import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.mapred.jobcontrol.JobControl;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.lib.input.KeyValueTextInputFormat;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.jobcontrol.ControlledJob;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;
import org.apache.hadoop.io.Text;
import java.io.IOException;


public class JobCoordinator
{
    public static void main(String[] args)
    {

            if(args.length < 3) {
                System.out.println("Usage: cs455.hadoop.JobCoordinator /input /output questionNum");
                System.exit(0);
            } else {
                try {
                    int question = Integer.parseInt(args[2]);
                    switch(question) {
                        case 1:
                            Question1(args);
                            break;
                        case 2:
                            Question2(args);
                            break;
                        case 3:
                            Question3(args);
                            break;
                        case 4:
                            Question4(args);
                            break;
                        case 5:
                            Question5(args);
                            break;
                        case 6:
                            Question6(args);
                            break;
                        default:
                            System.out.println("Invalid Question number...");
                    }

                } catch(NumberFormatException e) {
                    System.out.println("3 command line option was not a question number");
                } catch(IOException e) {
                    System.err.println(e.getMessage());
                }
            }


    }

    private static void Question1(String[] args) throws IOException
    {
        /*
           Run on both datasets
           Which state has the most monitoring sites across the United States
           Note: a site is identified by the combination of the state code, county code, and site number
           State code @ index 1, county code @ index 2, Site number @ index 3
         */

        // Configure Job Controller
        JobControl jobControl = new JobControl("Job Chain");
        Configuration config = new Configuration();

        // Configure first job
        Job job1 = Job.getInstance(config);
        job1.setJarByClass(JobCoordinator.class);
        job1.setJobName("Q1 - Map");

        // Configure input output paths
        FileInputFormat.setInputPaths(job1, new Path(args[0]));
        FileOutputFormat.setOutputPath(job1, new Path(args[1] + "/temp"));

        // Configure mapper, reducer, and combiner classes
        job1.setMapperClass(SiteMapper.class);
        job1.setReducerClass(SiteReducer.class);
        job1.setNumReduceTasks(10);
        //job1.setCombinerClass(SiteReducer.class);

        // Set Output types
        job1.setOutputKeyClass(Text.class);
        job1.setOutputValueClass(Text.class);

        // Add Job to Controller
        ControlledJob controlledJob1 = new ControlledJob(config);
        controlledJob1.setJob(job1);

        jobControl.addJob(controlledJob1);


        // Create job 2
        Configuration config2 = new Configuration();

        Job job2 = Job.getInstance(config2);
        job2.setJarByClass(JobCoordinator.class);
        job2.setJobName("Q1 - Invert and Count");

        // Configure input output paths
        FileInputFormat.setInputPaths(job2, new Path(args[1] + "/temp"));
        FileOutputFormat.setOutputPath(job2, new Path(args[1] + "/final"));

        // Configure Mapper, reducer, and combiner classes
        job2.setMapperClass(SwapMapper.class);
        job2.setReducerClass(SwapReducer.class);
        //job2.setCombinerClass(SwapReducer.class);
        job2.setNumReduceTasks(1);

        // Set output type
        //job2.setMapOutputKeyClass(Text.class);
        //job2.setMapOutputValueClass(IntWritable.class);
        job2.setOutputKeyClass(Text.class);
        job2.setOutputValueClass(IntWritable.class);
        job2.setInputFormatClass(KeyValueTextInputFormat.class);

        job2.setSortComparatorClass(IntComparator.class);
        ControlledJob controlledJob2 = new ControlledJob(config2);
        controlledJob2.setJob(job2);

        // Make job2 dependent on job1
        controlledJob2.addDependingJob(controlledJob1);
        // add the job to the job control
        jobControl.addJob(controlledJob2);

        Thread jobControlThread = new Thread(jobControl);
        jobControlThread.start();
    }

    private static void Question2(String[] args) throws IOException
    {
        /*
           Run on Gases Dataset
           Does the east coast or west coast have higher mean levels of SO2?
           Note: there are a total of 4 and 16 states in the West Coast and East Coast, respectfully
           West Coast: California "CA", Oregon "OR", Washington "WA", Alaska "AK"
           East Coast: Maine "ME", New Hampshire "NH", Massachusetts "MA", Rhode Island "RI", Connecticut "CT", New York "NY", New Jersey "NJ", Delaware "DE", Maryland "MD", Virgina "VA",
           North Carolina "NC", South Carolina "SC", Georgia "GA", Florida "FL", Pennsylvania "PA", Washington DC "DC"
         */
        JobControl jobControl = new JobControl("Job Chain");
        Configuration config = new Configuration();

        Job job1 = Job.getInstance(config);
        job1.setJarByClass(JobCoordinator.class);
        job1.setJobName("Q2");

        FileInputFormat.setInputPaths(job1, new Path(args[0]));
        FileOutputFormat.setOutputPath(job1, new Path(args[1] + "/final"));

        job1.setMapperClass(CoastMapper.class);
        job1.setReducerClass(CoastReducer.class);
        //job1.setCombinerClass(CoastReducer.class); // If Reducer class == Combiner class, mean calculation does not work

        job1.setOutputKeyClass(Text.class);
        job1.setOutputValueClass(DoubleWritable.class);

        ControlledJob controlledJob1 = new ControlledJob(config);
        controlledJob1.setJob(job1);

        jobControl.addJob(controlledJob1);

        Thread jobControlThread = new Thread(jobControl);
        jobControlThread.start();
    }

    private static void Question3(String[] args) throws IOException
    {
        /*
           What time of day (GMT) has the highest SO2 levels between 2000 - 2019?
           Capture the mean S02 levels for each hour (GMT) over all 20 years to justify your answer.
         */
        JobControl jobControl = new JobControl("Job Chain");
        Configuration config = new Configuration();

        Job job1 = Job.getInstance(config);
        job1.setJarByClass(JobCoordinator.class);
        job1.setJobName("Q3");

        FileInputFormat.setInputPaths(job1, new Path(args[0]));
        FileOutputFormat.setOutputPath(job1, new Path(args[1] + "/final"));

        job1.setMapperClass(TimeMapper.class);
        job1.setReducerClass(TimeReducer.class);
        //job1.setCombinerClass(TimeReducer.class);

        job1.setOutputKeyClass(Text.class);
        job1.setOutputValueClass(DoubleWritable.class);

        ControlledJob controlledJob1 = new ControlledJob(config);
        controlledJob1.setJob(job1);

        jobControl.addJob(controlledJob1);

        Thread jobControlThread = new Thread(jobControl);
        jobControlThread.start();
    }

    private static void Question4(String[] args) throws IOException
    {
        /*
           Has there been a change in SO2 levels over the last 40 years?
           Capture the mean SO2 levels for each year to justify your answer.
         */
        JobControl jobControl = new JobControl("Job Chain");
        Configuration config = new Configuration();

        Job job1 = Job.getInstance(config);
        job1.setJarByClass(JobCoordinator.class);
        job1.setJobName("Q4");

        FileInputFormat.setInputPaths(job1, new Path(args[0]));
        FileOutputFormat.setOutputPath(job1, new Path(args[1] + "/final"));

        job1.setMapperClass(FortyMapper.class);
        job1.setReducerClass(FortyReducer.class);

        job1.setOutputKeyClass(Text.class);
        job1.setOutputValueClass(DoubleWritable.class);

       ControlledJob controlledJob1 = new ControlledJob(config);
       controlledJob1.setJob(job1);

       jobControl.addJob(controlledJob1);

       Thread jobControlThread = new Thread(jobControl);
       jobControlThread.start();
    }

    private static void Question5(String[] args) throws IOException
    {
        /*
           What are the top 10 hottest states for the summer months (June, July, August)?
           Capture the mean temperature levels for the summer months (GMT) to justify your answer.
         */
        JobControl jobControl = new JobControl("Job Chain");
        Configuration config = new Configuration();

        Job job1 = Job.getInstance(config);
        job1.setJarByClass(JobCoordinator.class);
        job1.setJobName("Q5");

        FileInputFormat.setInputPaths(job1, new Path(args[0]));
        FileOutputFormat.setOutputPath(job1, new Path(args[1] + "/final"));

        job1.setMapperClass(HotMapper.class);
        job1.setReducerClass(HotReducer.class);

        job1.setOutputKeyClass(Text.class);
        job1.setOutputValueClass(DoubleWritable.class);

        ControlledJob controlledJob1 = new ControlledJob(config);
        controlledJob1.setJob(job1);

        jobControl.addJob(controlledJob1);

        Thread jobControlThread = new Thread(jobControl);
        jobControlThread.start();
    }

    private static void Question6(String[] args) throws IOException
    {
        /*
           What is the mean SO2 levels for the hottest states found in Question 5?
           (1) Arizona, (2) Puerto Rico, (3) Virgin Islands, (4) Texas, (5) Nevada,
           (6) Mississippi, (7) Florida, (8) Louisiana, (9) Arkansas, (10) Oklahoma
         */
        JobControl jobControl = new JobControl("Job Chain");
        Configuration config = new Configuration();

        Job job1 = Job.getInstance(config);
        job1.setJarByClass(JobCoordinator.class);
        job1.setJobName("Q6");

        FileInputFormat.setInputPaths(job1, new Path(args[0]));
        FileOutputFormat.setOutputPath(job1, new Path(args[1] + "/final"));

        job1.setMapperClass(StateMapper.class);
        job1.setReducerClass(StateReducer.class);

        job1.setOutputKeyClass(Text.class);
        job1.setOutputValueClass(DoubleWritable.class);

        ControlledJob controlledJob1 = new ControlledJob(config);
        controlledJob1.setJob(job1);

        jobControl.addJob(controlledJob1);

        Thread jobControlThread = new Thread(jobControl);
        jobControlThread.start();
    }
}
