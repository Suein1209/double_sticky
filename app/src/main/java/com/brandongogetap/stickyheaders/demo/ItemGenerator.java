package com.brandongogetap.stickyheaders.demo;

import android.util.Log;

import java.util.ArrayList;
import java.util.List;

final class ItemGenerator {

    private ItemGenerator() {

    }

    static List<Item> largeListWithHeadersAt(int... positions) {
        List<Item> items = new ArrayList<>();
        for (int i = 0; i < 50; i++) {
            if (inArray(positions, i)) {
                items.add(new HeaderItem("Header at " + i, ""));
            } else {
                items.add(new Item("Item at " + i, "Item description at " + i));
            }
        }
        return items;
    }

    static List<Item> twoWithHeader() {
        List<Item> items = new ArrayList<>();
        items.add(new HeaderItem("header", ""));
        items.add(new Item("Item", ""));
        return items;
    }

    static List<Item> demoList() {
        List<Item> items = new ArrayList<>();

        for (int i = 1; i < 100; i++) {
            if (i % 5 == 0 && i <= 10) {
                items.add(new StubbornHeaderItem("S-Header at " + i, ""));
            }else if (i % 5 == 0) {
                items.add(new HeaderItem("N-Header at " + i, ""));
            } else {
                items.add(new Item("Item at " + i, "Item description at " + i));
            }


//            if (i == 0 || i % 5 == 0 || i < 30) {
//                Log.e("suein", "[ItemGenerator -> demoList] StubbornHeaderItem");
//                items.add(new StubbornHeaderItem("Header at " + i, ""));
//            } else if (i % 5 == 0) {
//                Log.e("suein", "[ItemGenerator -> demoList] HeaderItem");
//                items.add(new HeaderItem("SHeader at " + i, ""));
//            } else {
//                Log.e("suein", "[ItemGenerator -> demoList] Item");
//                items.add(new Item("Item at " + i, "Item description at " + i));
//            }
        }


//        for (int i = 0; i < 100; i++) {
//            if (i==0 || i == 2 || (i % 4 == 0 && i > 0)) {
//                items.add(new StubbornHeaderItem("Header at " + i, ""));
//            } else {
//                items.add(new Item("Item at " + i, "Item description at " + i));
//            }
//        }

        return items;
    }

    private static boolean inArray(int[] array, int value) {
        for (int i : array) {
            if (i == value) {
                return true;
            }
        }
        return false;
    }
}
