package com.example.Smartscale;

public class exercise {
    private String name;
    private double MET;

    public static final exercise[] exercises = {
            new exercise("Aerobic dancing", 6.2),
            new exercise("Backpacking", 7.0),
            new exercise("Baseball/Softball", 6.0),
            new exercise("Basketball",6.5),
            new exercise("Bicycling", 8.4),
            new exercise("Bowling", 3.8),
            new exercise("Boxing", 7.8),
            new exercise("Calisthenics",5.9),
            new exercise("Canoeing", 5.8),
            new exercise("Hockey", 7.8),
            new exercise("Football", 8.0),
            new exercise("Gardening", 5.8),
            new exercise("Golf", 4.8),
            new exercise("Gymnastics", 3.8),
            new exercise("Hiking", 5.3),
            new exercise("Horseback riding", 5.5),
            new exercise("Lacrosse", 8.0),
            new exercise("Martial Arts", 7.8),
            new exercise("Pilates", 3.0),
            new exercise("Racquetball", 8.5),
            new exercise("Rock climbing", 8.0),
            new exercise("Rugby", 7.3),
            new exercise("Running", 10.0),
            new exercise("Sailing", 4.5),
            new exercise("Scuba diving", 7.0),
            new exercise("Skateboarding", 5.0),
            new exercise("Skating", 7.0),
            new exercise("Skiing", 9.0),
            new exercise("Soccer", 8.5),
            new exercise("Squash", 7.3),
            new exercise("Surfing", 5.0),
            new exercise("Swimming", 10.3),
            new exercise("Table Tennis", 4.0),
            new exercise("Tai Chi", 3.0),
            new exercise("Tennis", 8.0),
            new exercise("Walking", 2.0),
            new exercise("Water Aerobics", 5.5),
            new exercise("Weightlifting", 6.0),
            new exercise("Wrestling", 6.0),
            new exercise("Yoga", 4.0)

    };
    private exercise(String name, double MET) {
        this.name = name;
        this.MET = MET;
    }

    public String getName() {
        return name;
    }
    public double getMET(){
        return MET;
    }
    public String toString() {
        return this.name;
    }
}
