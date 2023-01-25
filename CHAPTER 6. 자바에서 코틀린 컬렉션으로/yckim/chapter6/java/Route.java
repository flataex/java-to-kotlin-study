package chapter6.java;

import java.util.List;

public class Route {

    public static Location getDepartsFrom(List<Journey> route) {
        return route.get(0).getDepartsFrom();
    }
}
