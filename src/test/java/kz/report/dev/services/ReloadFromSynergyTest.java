package kz.report.dev.services;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ReloadFromSynergyTest {

    @Test
    public void test() throws Exception {

        List<Integer> list = new ArrayList();

        for (int i = 0; i<19568; i++) {
            list.add(i);
        }

        long iter = Math.round(Math.ceil(list.size()/1000));

        System.out.println(iter);

        int b = (int) iter * 1000;

        System.out.println(list.subList(b, list.size()).size());


//        Reload reload = new Reload();
//        reload.insertDataToDataBase();
    }

    private long getRange(int size) {
        long iter = Math.round(Math.ceil(size/1000));
        if ((size % 1000) > 0) {
            iter++;
        }
        return iter;
    }
}