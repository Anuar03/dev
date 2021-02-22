package kz.report.dev.services.reload.reloadimpl;


import org.junit.jupiter.api.Test;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

class ReloadFromSynergyTest {

    @Test
    public void testMethod() {
        List<Integer> integerList = new ArrayList<>();
        Random random = new Random();
        for (int i = 0 ; i<123036 ; i++) {
            integerList.add(random.nextInt());
        }
        List<List<Integer>> resultList = chopIntoParts(integerList, 10);
        System.out.println("Full count = " + resultList.size());
        for (List list : resultList) {
            System.out.println(list.size());
        }

    }

    public static <T>List<List<T>> chopIntoParts(final List<T> ls, final int iParts )
    {
        final List<List<T>> lsParts = new ArrayList<List<T>>();
        final int iChunkSize = ls.size() / iParts;
        int iLeftOver = ls.size() % iParts;
        int iTake = iChunkSize;

        for( int i = 0, iT = ls.size(); i < iT; i += iTake )
        {
            if( iLeftOver > 0 )
            {
                iLeftOver--;

                iTake = iChunkSize + 1;
            }
            else
            {
                iTake = iChunkSize;
            }

            lsParts.add( new ArrayList<T>( ls.subList( i, Math.min( iT, i + iTake ) ) ) );
        }

        return lsParts;
    }

}