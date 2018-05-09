package com.alanwgt.console;

import io.reactivex.Observable;
import io.reactivex.schedulers.Schedulers;

import java.io.*;

public class Terminator {

    // REMEMBER! observeOn(Schedulers.trampoline())
    // trampoline makes sure that the commands will be executed in the order that they arrived. Schedulers.io() sends
    // the data to a thread pool and the order can't be assured

    public Terminator() {}

    private Observable<String[]> lines(BufferedReader reader) {
        return Observable.<String[]>create(subscriber -> {
            String line;

            while ((line = reader.readLine()) != null) {
                if (line.trim().length() == 0) {
                    continue;
                }

                subscriber.onNext(line.split("\\s"));

                if (subscriber.isDisposed()) {
                    break;
                }
            }

            subscriber.onComplete();
        }).observeOn(Schedulers.io());
    }

    public Observable<String[]> linesFromInput() {
        return lines(
                new BufferedReader(
                        new InputStreamReader(System.in)
                )
        );
    }
}