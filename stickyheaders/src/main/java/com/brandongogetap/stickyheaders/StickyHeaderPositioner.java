package com.brandongogetap.stickyheaders;

import android.content.Context;
import android.os.Build;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.MarginLayoutParams;
import android.view.ViewTreeObserver;
import android.widget.FrameLayout;

import androidx.annotation.Nullable;
import androidx.annotation.Px;
import androidx.annotation.VisibleForTesting;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.brandongogetap.stickyheaders.exposed.StickyHeaderListener;
import com.brandongogetap.stickyheaders.exposed.TestAdapterImpl;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Stack;

final class StickyHeaderPositioner {

    static final int NO_ELEVATION = -1;
    static final int DEFAULT_ELEVATION = 5;

    private static final int INVALID_POSITION = -1;

    private final RecyclerView recyclerView;
    private final boolean checkMargins;
    //    private Stack<HeaderInfo> headerInfos = new Stack<HeaderInfo>();
    private ArrayList<HeaderInfo> headerInfos = new ArrayList<HeaderInfo>();
    private final ViewTreeObserver.OnGlobalLayoutListener visibilityObserver = new ViewTreeObserver.OnGlobalLayoutListener() {
        @Override
        public void onGlobalLayout() {
            int visibility = StickyHeaderPositioner.this.recyclerView.getVisibility();
            if (currentHeader != null) {
                currentHeader.setVisibility(visibility);
            }
        }
    };

    private int getTotalHeaderHeight() {
        int ret = 0;
        for (HeaderInfo headerInfo : headerInfos) {
            if (headerInfo.isStubborn()) {
                ret += headerInfo.getHeight();
            }
        }
        return ret;
    }

    private void putHeaderInfo(int headerPosition, View header) {
//        if (!stubbornHeaderPositions.contains(headerPosition)) {
//            Log.e("suein", "[StickyHeaderPositioner -> putHeaderInfo] 이건 고집쎈 스티키야 " + headerPosition);
//            return;
//        }

        boolean isContained = false;
        for (HeaderInfo headerInfo : headerInfos) {
            if (header == headerInfo.getView()) {
                isContained = true;
                break;
            }
        }

        if (!isContained) {
            Log.e("suein", "[StickyHeaderPositioner -> putHeaderInfo getHeaderPositionToShow2 ] 추가추가 " + headerPosition + " | " + header.getHeight());
//            headerInfos.push(new HeaderInfo(stubbornHeaderPositions.contains(headerPosition), headerPosition, header, header.getHeight()));
            headerInfos.add(new HeaderInfo(stubbornHeaderPositions.contains(headerPosition), headerPosition, header, header.getHeight()));
        }
    }

//    private boolean removeHeaderInfo(View header) {
//        HeaderInfo willRemoveItem = null;
//        for (HeaderInfo headerInfo : headerInfos) {
//            if (headerInfo.getView() == header) {
//                willRemoveItem = headerInfo;
//            }
//        }
//
//        if (willRemoveItem != null) {
//            return headerInfos.remove(willRemoveItem);
//        }
//        return false;
//    }

    private boolean removeHeaderInfo(int headerIndex) {
        HeaderInfo willRemoveItem = null;
        for (HeaderInfo headerInfo : headerInfos) {
            if (headerInfo.getPosition() == headerIndex) {
                willRemoveItem = headerInfo;
            }
        }
        if (willRemoveItem != null) {
            boolean ret = headerInfos.remove(willRemoveItem);
            Log.e("suein", "[StickyHeaderPositioner -> removeHeaderInfo getHeaderPositionToShow2] 222 삭제 = " + headerIndex + ", ret = " + ret + ", headerInfos size = " + headerInfos.size());
            for (HeaderInfo headerInfo :
                    headerInfos) {
                Log.e("suein", "[StickyHeaderPositioner -> removeHeaderInfo getHeaderPositionToShow2 ] 살아 남은거 " + headerInfo.getPosition());
            }
            return ret;
        }
        return false;
    }

    private int getHeaderPositionInList(View header) {
        for (HeaderInfo headerInfo : headerInfos) {
            if (headerInfo.getView() == header) {
                Log.e("suein", "[StickyHeaderPositioner -> getHeaderPositionInList] 찾았다 " + headerInfo);
                return headerInfo.getPosition();
            }
        }
        return INVALID_POSITION;
    }

    private View currentHeader;
    private RecyclerView.ViewHolder currentViewHolder;

    private int lastBoundPosition = INVALID_POSITION;
    private List<Integer> headerPositions;
    private List<Integer> stubbornHeaderPositions;
    private int orientation;
    private boolean dirty;
    private float headerElevation = NO_ELEVATION;
    private int cachedElevation = NO_ELEVATION;
    @Nullable
    private StickyHeaderListener listener;

    StickyHeaderPositioner(RecyclerView recyclerView) {
        this.recyclerView = recyclerView;
        checkMargins = recyclerViewHasPadding();
    }

    void setHeaderPositions(List<Integer> headerPositions, List<Integer> stubbornHeaderPositions) {
        this.headerPositions = headerPositions;
        this.stubbornHeaderPositions = stubbornHeaderPositions;
    }

    View getHeaderPosition(Map<Integer, View> visibleHeaders) {
        if (headerInfos.size() <= 0) return null;

        View tempHeader = headerInfos.get(headerInfos.size() - 1).getView();
        for (Map.Entry<Integer, View> entry : visibleHeaders.entrySet()) {
            if (tempHeader == entry.getValue()) {
                return entry.getValue();
            }
        }

        return null;
    }


    int getHeaderPositionToShow2(Map<Integer, View> visibleHeaders, View headerForPosition) {

//        int headerPositionToShow = INVALID_POSITION;
//        if (headerIsOffset(headerForPosition)) {
//            int offsetHeaderIndex = headerPositions.indexOf(firstVisiblePosition);
//            if (offsetHeaderIndex > 0) {
//                return headerPositions.get(offsetHeaderIndex - 1);
//            }
//        }
//        for (Integer headerPosition : headerPositions) {
//            if (headerPosition <= firstVisiblePosition) {
//                headerPositionToShow = headerPosition;
//            } else {
//                break;
//            }
//        }


        int ret = INVALID_POSITION;
        int tempHeight = getTotalHeaderHeight();

        for (Map.Entry<Integer, View> entry : visibleHeaders.entrySet()) {
            if (entry.getValue().getTop() <= tempHeight) {
                ret = entry.getKey();
                Log.e("suein", "[StickyHeaderPositioner -> getHeaderPositionToShow2] 1111 반환 = " + ret);
            } else if (entry.getValue().getTop() > tempHeight) {
                break;
            }
        }


//        Log.e("suein", "[StickyHeaderPositioner -> getHeaderPositionToShow2] " + ret + ", lastBoundPosition = " + lastBoundPosition + ", visibleHeaders = " + visibleHeaders.size());
        if (ret == INVALID_POSITION && headerInfos.size() > 0) {
            ret = headerInfos.get(headerInfos.size() - 1).getPosition();
            Log.e("suein", "[StickyHeaderPositioner -> getHeaderPositionToShow2] 2222 반환 = " + ret + ", headerInfos = " + headerInfos.size());
        }

        return ret;
    }

    void updateHeaderState(int firstVisiblePosition, Map<Integer, View> visibleHeaders, ViewRetriever viewRetriever, boolean atTop) {

        if (visibleHeaders.size() <= 0) return;

//        int tempInfoPosition = headerInfos.size() > 0 ? headerInfos.size() - 1 : firstVisiblePosition;
//        int headerPositionToShow2 = atTop ? INVALID_POSITION : currentHeadPosition >= 0 ? currentHeadPosition : 0;

        int headerPositionToShow = atTop ? INVALID_POSITION : getHeaderPositionToShow(firstVisiblePosition, visibleHeaders.get(firstVisiblePosition));
        int headerPositionToShow2 = atTop ? INVALID_POSITION : getHeaderPositionToShow(firstVisiblePosition, visibleHeaders.get(firstVisiblePosition));


        View tempPosition = null;
        if (headerInfos.size() > 0) {
            tempPosition = headerInfos.get(headerInfos.size() - 1).getView();
            Log.e("suein", "[StickyHeaderPositioner -> updateHeaderState] 가져온 포지션 = " + (headerInfos.size() - 1) + "" + tempPosition);
        }


        int temp = atTop ? INVALID_POSITION : getHeaderPositionToShow2(visibleHeaders, tempPosition);

        headerPositionToShow = temp;


        View headerToCopy = visibleHeaders.get(headerPositionToShow);
        Log.e("suein", "[StickyHeaderPositioner -> updateHeaderState] A = " + headerPositionToShow2 + ", B = " + temp);

        if (headerPositionToShow != lastBoundPosition) {
            if (headerPositionToShow == INVALID_POSITION || checkMargins && headerAwayFromEdge(headerToCopy)) { // We don't want to attach yet if header view is not at edge
                Log.e("suein", "[StickyHeaderPositioner -> updateHeaderState] 삭제삭제삭제삭제삭제삭제삭제삭제 = headerPositionToShow= = " + headerPositionToShow);
                dirty = true;
                safeDetachHeader();
                lastBoundPosition = INVALID_POSITION;
            } else {

                Log.e("suein", "[StickyHeaderPositioner -> updateHeaderState] 여기???? 추가??? = headerPositionToShow= = " + headerPositionToShow);

                lastBoundPosition = headerPositionToShow;
                RecyclerView.ViewHolder viewHolder = viewRetriever.getViewHolderForPosition(headerPositionToShow);
                attachHeader(viewHolder, headerPositionToShow);
            }
        } else if (checkMargins) {
            /*
              This could still be our firstVisiblePosition even if another view is visible above it.
              See `#getHeaderPositionToShow` for explanation.
             */
            if (headerAwayFromEdge(headerToCopy)) {
                detachHeader(lastBoundPosition);
                removeHeaderInfo(lastBoundPosition);
                lastBoundPosition = INVALID_POSITION;
            }
        }
        checkHeaderPositions(visibleHeaders);
        recyclerView.post(new Runnable() {
            @Override
            public void run() {
                checkElevation();
            }
        });
    }

    // This checks visible headers and their positions to determine if the sticky header needs
    // to be offset. In reality, only the header following the sticky header is checked. Some
    // optimization may be possible here (not storing all visible headers in map).
    void checkHeaderPositions(final Map<Integer, View> visibleHeaders) {
        if (currentHeader == null) return;
        // This can happen after configuration changes.
        if (currentHeader.getHeight() == 0) {
            waitForLayoutAndRetry(visibleHeaders);
            return;
        }
        boolean reset = true;
        for (Map.Entry<Integer, View> entry : visibleHeaders.entrySet()) {
            if (entry.getKey() <= lastBoundPosition) {
                //suein 현재 가장 최 상단에 있는 header 까지는 걸른다.
                continue;
            }
//            Log.e("suein", "[StickyHeaderPositioner -> checkHeaderPositions] 다음꺼 뭐로?? = " + lastBoundPosition);

            Log.e("suein", "[StickyHeaderPositioner -> checkHeaderPositions] " + entry.getKey() + " || " + isNormalHeader(entry.getKey()));
            //suein 바로 다음에 오는 nextHeader를 넣는다.
            View nextHeader = entry.getValue();
            reset = offsetHeader(nextHeader, isNormalHeader(entry.getKey())) == -1;
            break;
        }
        if (reset) resetTranslation();
        currentHeader.setVisibility(View.VISIBLE);
    }

    boolean isNormalHeader(int headerIndex) {
        if (!stubbornHeaderPositions.contains(headerIndex)) {
            int index = headerPositions.indexOf(headerIndex);
            if (index != -1 && index != 0) {
                return !stubbornHeaderPositions.contains(headerPositions.get(index - 1));
            }
        }
        return false;
    }

    void setElevateHeaders(int dpElevation) {
        if (dpElevation != NO_ELEVATION) {
            // Context may not be available at this point, so caching the dp value to be converted
            // into pixels after first header is attached.
            cachedElevation = dpElevation;
        } else {
            headerElevation = NO_ELEVATION;
            cachedElevation = NO_ELEVATION;
        }
    }

    void reset(int orientation) {
        this.orientation = orientation;
        lastBoundPosition = INVALID_POSITION;
        dirty = true;
        safeDetachHeader();
    }

    void clearHeader() {
        detachHeader(lastBoundPosition);
        removeHeaderInfo(lastBoundPosition);
    }

    void clearVisibilityObserver() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            recyclerView.getViewTreeObserver().removeOnGlobalLayoutListener(visibilityObserver);
        } else {
            //noinspection deprecation
            recyclerView.getViewTreeObserver().removeGlobalOnLayoutListener(visibilityObserver);
        }
    }

    void setListener(@Nullable StickyHeaderListener listener) {
        this.listener = listener;
    }

    private float offsetHeader(View nextHeader, boolean isNextNormalSticky) {
//        boolean shouldOffsetHeader = shouldOffsetHeader(nextHeader);
//        float offset = -1;
//        if (shouldOffsetHeader) {
//            if (orientation == LinearLayoutManager.VERTICAL) {
//                offset = -(currentHeader.getHeight() - nextHeader.getY());
//
//                //여기서 기존 헤더 밀어내기
////                currentHeader.setTranslationY(offset);
//            } else {
//                offset = -(currentHeader.getWidth() - nextHeader.getX());
//                currentHeader.setTranslationX(offset);
//            }
//        }

        boolean shouldOffsetHeader2 = shouldOffsetHeader2(nextHeader, isNextNormalSticky);

        float offset = -1;
        if (shouldOffsetHeader2) {
            if (orientation == LinearLayoutManager.VERTICAL) {
                int totalHeaderHeight = getTotalHeaderHeight();
                if (isNextNormalSticky) {
                    totalHeaderHeight += currentHeader.getHeight();
                }
                offset = -(totalHeaderHeight - nextHeader.getY());
                Log.e("suein", "[StickyHeaderPositioner -> offsetHeader] getTotalHeaderHeight() = " + getTotalHeaderHeight() + ", nextHeader.getY() = " + (nextHeader.getY()) + ", offset = " + offset);
                //여기서 기존 헤더 밀어내기
                currentHeader.setTranslationY(offset);
            } else {
                offset = -(currentHeader.getWidth() - nextHeader.getX());
                currentHeader.setTranslationX(offset);
            }

        }
//        Log.e("suein", "[StickyHeaderPositioner -> offsetHeader] " + offset);


//        Log.e("suein", "[StickyHeaderPositioner -> offsetHeader] 기존 = " + offset + ", 새로운거 = " + (-(getTotalHeaderHeight() - nextHeader.getY())) + ", getTotalHeaderHeight = " + getTotalHeaderHeight() + ", currentHeader.getHeight() = " + currentHeader.getHeight());
//        Log.e("suein", "[StickyHeaderPositioner -> offsetHeader] " + offset + ",  newOffset = " + newOffset);

        return offset;
    }

    private boolean shouldOffsetHeader(View nextHeader) {
        if (orientation == LinearLayoutManager.VERTICAL) {
            return nextHeader.getY() < currentHeader.getHeight();
        } else {
            return nextHeader.getX() < currentHeader.getWidth();
        }
    }

    private boolean shouldOffsetHeader2(View nextHeader, boolean isNextNormalSticky) {
        if (orientation == LinearLayoutManager.VERTICAL) {
            int totalHeaderHeight = getTotalHeaderHeight();
            if (isNextNormalSticky) {
                totalHeaderHeight += currentHeader.getHeight();
            }
            Log.e("suein", "[StickyHeaderPositioner -> shouldOffsetHeader2] nextHeader.getY() = " + (nextHeader.getY()) + ", getTotalHeaderHeight() = " + totalHeaderHeight + " = " + (nextHeader.getY() < totalHeaderHeight));
            return nextHeader.getY() < totalHeaderHeight;
        } else {
            return nextHeader.getX() < currentHeader.getWidth();
        }
    }

    private void resetTranslation() {
        if (orientation == LinearLayoutManager.VERTICAL) {
            currentHeader.setTranslationY(0);
        } else {
            currentHeader.setTranslationX(0);
        }
    }

    /**
     * In case of padding, first visible position may not be accurate.
     * <p>
     * Example: RecyclerView has padding of 10dp. With clipToPadding set to false, a visible view
     * above the 10dp threshold will not be recognized as firstVisiblePosition by the LayoutManager.
     * <p>
     * To remedy this, we are checking if the firstVisiblePosition (according to the LayoutManager)
     * is a header (headerForPosition will not be null). If it is, we check its Y. If #getY is
     * greater than 0 then we know it is actually not the firstVisiblePosition, and return the
     * preceding header position (if available).
     */
    private int getHeaderPositionToShow(int firstVisiblePosition, @Nullable View headerForPosition) {

        Log.e("suein", "[StickyHeaderPositioner -> getHeaderPositionToShow] " + firstVisiblePosition);

        int headerPositionToShow = INVALID_POSITION;
        if (headerIsOffset(headerForPosition)) {
            int offsetHeaderIndex = headerPositions.indexOf(firstVisiblePosition);
            if (offsetHeaderIndex > 0) {
                return headerPositions.get(offsetHeaderIndex - 1);
            }
        }
        for (Integer headerPosition : headerPositions) {
            if (headerPosition <= firstVisiblePosition) {
                headerPositionToShow = headerPosition;
            } else {
                break;
            }
        }
        return headerPositionToShow;
    }

    private boolean headerIsOffset(View headerForPosition) {
        if (headerForPosition != null) {

            Log.e("suein", "[StickyHeaderPositioner -> headerIsOffset] true");

//            return orientation == LinearLayoutManager.VERTICAL ?
//                    headerForPosition.getY() > getTotalHeaderHeight() : headerForPosition.getX() > 0;
            return orientation == LinearLayoutManager.VERTICAL ? headerForPosition.getY() > 0 : headerForPosition.getX() > 0;
        }
        Log.e("suein", "[StickyHeaderPositioner -> headerIsOffset] false");
        return false;
    }


    private boolean headerIsOffset2(View headerForPosition) {
        if (headerForPosition != null) {

            Log.e("suein", "[StickyHeaderPositioner -> headerIsOffset] true");

//            return orientation == LinearLayoutManager.VERTICAL ?
//                    headerForPosition.getY() > getTotalHeaderHeight() : headerForPosition.getX() > 0;
            return orientation == LinearLayoutManager.VERTICAL ? headerForPosition.getY() > getTotalHeaderHeight() : headerForPosition.getX() > 0;
        }
        Log.e("suein", "[StickyHeaderPositioner -> headerIsOffset] false");
        return false;
    }

    @VisibleForTesting
    void attachHeader(RecyclerView.ViewHolder viewHolder, int headerPosition) {
//        if (currentViewHolder == viewHolder) {
//            //같은 홀더라면 업데이트 하고 아니라면 새로 만들어서 넣는다.
//            //왜냐면 항상 최상위기 때문
////            callDetach(lastBoundPosition);
//            //noinspection unchecked
//            Log.e("suein", "[StickyHeaderPositioner -> attachHeader] 헤더를 넣는다. = " + headerPosition + "" + currentViewHolder);
//            recyclerView.getAdapter().onBindViewHolder(currentViewHolder, headerPosition);
//            currentViewHolder.itemView.requestLayout();
//            checkTranslation();
//            callAttach(headerPosition);
//            dirty = false;
//            return;
//        } else {
//            detachHeader(lastBoundPosition);
//            this.currentViewHolder = viewHolder;
//            //noinspection unchecked
//            recyclerView.getAdapter().onBindViewHolder(currentViewHolder, headerPosition);
//            this.currentHeader = currentViewHolder.itemView;
//            callAttach(headerPosition);
//            resolveElevationSettings(currentHeader.getContext());
//            // Set to Invisible until we position it in #checkHeaderPositions.
//            currentHeader.setVisibility(View.INVISIBLE);
//            currentHeader.setId(R.id.header_view);
//            recyclerView.getViewTreeObserver().addOnGlobalLayoutListener(visibilityObserver);
//
//            Log.e("suein", "[StickyHeaderPositioner -> attachHeader] 헤더를 넣는다.");
//            getRecyclerParent().addView(currentHeader);
//            if (checkMargins) {
//                updateLayoutParams(currentHeader);
//            }
//            dirty = false;
//        }

        Log.e("suein", "[StickyHeaderPositioner -> attachHeader] 삭제될녀석 = " + lastBoundPosition + ",  = " + isNormalHeader(lastBoundPosition));

        if (isNormalHeader(lastBoundPosition)) {
            removeHeaderInfo(lastBoundPosition);
            detachHeader(lastBoundPosition);
        }

        this.currentViewHolder = viewHolder;
        //noinspection unchecked
//        recyclerView.getAdapter().onBindViewHolder(currentViewHolder, headerPosition);


        this.currentHeader = ((TestAdapterImpl) recyclerView.getAdapter()).getTestView(recyclerView.getContext(), recyclerView, headerPosition);
//        this.currentHeader = currentViewHolder.itemView;
        callAttach(headerPosition);
        resolveElevationSettings(currentHeader.getContext());
        // Set to Invisible until we position it in #checkHeaderPositions.
        currentHeader.setVisibility(View.INVISIBLE);
//        currentHeader.setId(resTest.get(bounceCount));
//        currentHeader.setId(R.id.header_view1);
        recyclerView.getViewTreeObserver().addOnGlobalLayoutListener(visibilityObserver);
        recyclerView.getViewTreeObserver().addOnGlobalLayoutListener(new VisibilityObserver2implements(headerPosition));

        Log.e("suein", "[StickyHeaderPositioner -> attachHeader] 헤더를 넣는다.");
        getRecyclerParent().addView(currentHeader);
        if (checkMargins) {
            updateLayoutParams(currentHeader);
        }

        FrameLayout.LayoutParams para = ((FrameLayout.LayoutParams) currentHeader.getLayoutParams());
        para.topMargin = para.topMargin + getTotalHeaderHeight();

        currentHeader.setLayoutParams(para);
        dirty = false;
    }

    class VisibilityObserver2implements implements ViewTreeObserver.OnGlobalLayoutListener {

        public int headerPosition;

        public VisibilityObserver2implements(int headerPosition) {
            this.headerPosition = headerPosition;
        }

        @Override
        public void onGlobalLayout() {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                recyclerView.getViewTreeObserver().removeOnGlobalLayoutListener(this);
            } else {
                //noinspection deprecation
                recyclerView.getViewTreeObserver().removeGlobalOnLayoutListener(this);
            }
            if (currentHeader != null) {
                putHeaderInfo(headerPosition, currentHeader);
                Log.e("suein", "[StickyHeaderPositioner -> attachHeader] 받기 전에는 " + getTotalHeaderHeight() + ", 포지션 = " + headerPosition);
            }
        }
    }


    private int currentDimension() {
        if (currentHeader == null) {
            return 0;
        }
        if (orientation == LinearLayoutManager.VERTICAL) {
            return currentHeader.getHeight();
        } else {
            return currentHeader.getWidth();
        }
    }

    private boolean headerHasTranslation() {
        if (currentHeader == null) {
            return false;
        }
        if (orientation == LinearLayoutManager.VERTICAL) {
            return currentHeader.getTranslationY() < 0;
        } else {
            return currentHeader.getTranslationX() < 0;
        }
    }

    private void updateTranslation(int diff) {
        if (currentHeader == null) {
            return;
        }
        if (orientation == LinearLayoutManager.VERTICAL) {

            Log.e("suein", "[StickyHeaderPositioner -> updateTranslation] 들어가는 값 = " + (currentHeader.getTranslationY() + diff));

            currentHeader.setTranslationY(currentHeader.getTranslationY() + diff);
        } else {
            currentHeader.setTranslationX(currentHeader.getTranslationX() + diff);
        }
    }

    /**
     * When a view is re-bound using the same view holder, the height may have changed. If the header has translation
     * applied, this could cause a flickering if the view's height has increased.
     */
    private void checkTranslation() {
        final View view = currentHeader;
        if (view == null) return;
        view.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            int previous = currentDimension();

            @Override
            public void onGlobalLayout() {
                Log.e("suein", "[StickyHeaderPositioner -> onGlobalLayout] 뷰생성");
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                    view.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                } else {
                    //noinspection deprecation
                    view.getViewTreeObserver().removeGlobalOnLayoutListener(this);
                }
                if (currentHeader == null) return;

                int newDimen = currentDimension();
                if (headerHasTranslation() && previous != newDimen) {
                    updateTranslation(previous - newDimen);
                }
            }
        });
    }

    private void checkElevation() {
        if (headerElevation != NO_ELEVATION && currentHeader != null) {
            if (orientation == LinearLayoutManager.VERTICAL && currentHeader.getTranslationY() == 0
                    || orientation == LinearLayoutManager.HORIZONTAL && currentHeader.getTranslationX() == 0) {
                elevateHeader();
            } else {
                settleHeader();
            }
        }
    }

    private void elevateHeader() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            if (currentHeader.getTag() != null) {
                // Already elevated, bail out
                return;
            }
            currentHeader.setTag(true);
            currentHeader.animate().z(headerElevation);
        }
    }

    private void settleHeader() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            if (currentHeader.getTag() != null) {
                currentHeader.setTag(null);
                currentHeader.animate().z(0);
            }
        }
    }

    private void detachHeader(int position) {
        if (currentHeader != null) {
            Log.e("suein", "[StickyHeaderPositioner -> detachHeader] " + position);
            getRecyclerParent().removeView(currentHeader);
            callDetach(position);
            clearVisibilityObserver();
            currentHeader = null;
            currentViewHolder = null;
        }
    }

    private void callAttach(int position) {
        if (listener != null) {
            listener.headerAttached(currentHeader, position);
        }
    }

    private void callDetach(int position) {
        if (listener != null) {
            listener.headerDetached(currentHeader, position);
        }
    }

    /**
     * Adds margins to left/right (or top/bottom in horizontal orientation)
     * <p>
     * Top padding (or left padding in horizontal orientation) with clipToPadding = true is not
     * supported. If you need to offset the top (or left in horizontal orientation) and do not
     * want scrolling children to be visible, use margins.
     */
    private void updateLayoutParams(View currentHeader) {
        MarginLayoutParams params = (MarginLayoutParams) currentHeader.getLayoutParams();
        matchMarginsToPadding(params);
    }

    private void matchMarginsToPadding(MarginLayoutParams layoutParams) {
        @Px int leftMargin = orientation == LinearLayoutManager.VERTICAL ?
                recyclerView.getPaddingLeft() : 0;
        @Px int topMargin = orientation == LinearLayoutManager.VERTICAL ?
                0 : recyclerView.getPaddingTop();
        @Px int rightMargin = orientation == LinearLayoutManager.VERTICAL ?
                recyclerView.getPaddingRight() : 0;
        layoutParams.setMargins(leftMargin, topMargin, rightMargin, 0);
    }

    private boolean headerAwayFromEdge(View headerToCopy) {
        if (headerToCopy != null) {
            return orientation == LinearLayoutManager.VERTICAL ?
                    headerToCopy.getY() > getTotalHeaderHeight() : headerToCopy.getX() > 0;
        }
        return false;
    }

    private boolean recyclerViewHasPadding() {
        return recyclerView.getPaddingLeft() > 0
                || recyclerView.getPaddingRight() > 0
                || recyclerView.getPaddingTop() > 0;
    }

    private ViewGroup getRecyclerParent() {
        return (ViewGroup) recyclerView.getParent();
    }

    private void waitForLayoutAndRetry(final Map<Integer, View> visibleHeaders) {
        final View view = currentHeader;
        if (view == null) return;
        view.getViewTreeObserver().addOnGlobalLayoutListener(
                new ViewTreeObserver.OnGlobalLayoutListener() {
                    @Override
                    public void onGlobalLayout() {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                            view.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                        } else {
                            //noinspection deprecation
                            view.getViewTreeObserver().removeGlobalOnLayoutListener(this);
                        }
                        // If header was removed during layout
                        if (currentHeader == null) return;
                        getRecyclerParent().requestLayout();
                        checkHeaderPositions(visibleHeaders);
                    }
                });
    }

    /**
     * Detaching while {@link StickyLayoutManager} is laying out children can cause an inconsistent
     * state in the child count variable in {@link android.widget.FrameLayout} layoutChildren method
     */
    private void safeDetachHeader() {
        final int cachedPosition = lastBoundPosition;
        removeHeaderInfo(cachedPosition);
        getRecyclerParent().post(new Runnable() {
            @Override
            public void run() {
                if (dirty) {
                    detachHeader(cachedPosition);
                }
            }
        });
    }

    @VisibleForTesting
    int getLastBoundPosition() {
        return lastBoundPosition;
    }

    private void resolveElevationSettings(Context context) {
        if (cachedElevation != NO_ELEVATION && headerElevation == NO_ELEVATION) {
            headerElevation = pxFromDp(context, cachedElevation);
        }
    }

    private float pxFromDp(Context context, int dp) {
        float scale = context.getResources().getDisplayMetrics().density;
        return dp * scale;
    }
}
