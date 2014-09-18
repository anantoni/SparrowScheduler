/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package utils;
import java.util.concurrent.atomic.AtomicInteger;

/**
 *
 * @author anantoni
 */

public class AtomicCounter {
    private static final AtomicInteger c = new AtomicInteger(0);

    public static int increment() {
        return c.incrementAndGet();
    }

    public static int decrement() {
        return c.decrementAndGet();
    }

    public static int value() {
        return c.get();
    }

}
