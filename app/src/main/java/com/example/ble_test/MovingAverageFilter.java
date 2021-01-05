package com.example.ble_test;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.ListIterator;
import java.util.Queue;

public class MovingAverageFilter {

    private int window_size = 10;
    private Queue<Integer> queue = new LinkedList<>();

    public int update(int rssi) {

        if (queue.size() < window_size) {
            queue.add(rssi);
            return 0;
        }
        if (queue.size() == window_size) {
            queue.remove();
            queue.add(rssi);

            int sum = 0;
            for (Integer item: queue){
                sum += item;
            }

            return (int) sum/window_size;
        }

        return 0;
    }
}
