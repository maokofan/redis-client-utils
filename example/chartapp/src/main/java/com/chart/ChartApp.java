package com.chart;

import com.chart.redis.SubTest;

public class ChartApp {
    public static void main(String[] args) {
        try {
            SubTest.start();
            SubTest subTest = new SubTest();
            subTest.startSub();
            Thread.sleep(2000);
            subTest.startPub();
        } catch (Exception e) {
            e.printStackTrace();
        }

    }
}
