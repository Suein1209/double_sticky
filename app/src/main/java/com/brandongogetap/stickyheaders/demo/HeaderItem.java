package com.brandongogetap.stickyheaders.demo;

import com.brandongogetap.stickyheaders.exposed.StickyHeader;
import com.brandongogetap.stickyheaders.exposed.StubbornStickyHeader;

class HeaderItem extends Item implements StickyHeader {
    HeaderItem(String title, String message) {
        super(title, message);
    }
}


class StubbornHeaderItem extends Item implements StubbornStickyHeader {
    StubbornHeaderItem(String title, String message) {
        super(title, message);
    }
}