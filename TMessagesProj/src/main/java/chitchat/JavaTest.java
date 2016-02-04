package chitchat;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * Created by RaminBT on 21/01/2016.
 */
public class JavaTest {

    static class Dog{
        String name;
        int age;

        public Dog(String name, int age) {
            this.name = name;
            this.age = age;
        }

        public String getName() {
            return name;
        }

        public int getAge() {
            return age;
        }
    }



    public static void main(String[] artg){


        long start=TimeUnit.HOURS.toMillis(22) + TimeUnit.MINUTES.toMillis(30);
        long end  =TimeUnit.HOURS.toMillis(7) + TimeUnit.MINUTES.toMillis(0) + TimeUnit.DAYS.toMillis(1);

        long now  = TimeUnit.HOURS.toMillis(6) + TimeUnit.MINUTES.toMillis(10);

        log(now > start);
        log(now < end);

    }

    public static void log(Object a){
        System.out.println(a);
    }
}
