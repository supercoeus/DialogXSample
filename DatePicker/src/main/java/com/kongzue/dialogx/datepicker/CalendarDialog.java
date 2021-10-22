package com.kongzue.dialogx.datepicker;

import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.DecelerateInterpolator;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.kongzue.dialogx.DialogX;
import com.kongzue.dialogx.datepicker.interfaces.OnDateSelected;
import com.kongzue.dialogx.datepicker.view.ArrayWheelAdapter;
import com.kongzue.dialogx.datepicker.view.CalendarLabelTextView;
import com.kongzue.dialogx.datepicker.view.OnWheelChangedListener;
import com.kongzue.dialogx.datepicker.view.TableLayout;
import com.kongzue.dialogx.datepicker.view.WheelView;
import com.kongzue.dialogx.dialogs.BottomDialog;
import com.kongzue.dialogx.interfaces.BaseDialog;
import com.kongzue.dialogx.interfaces.OnBindView;
import com.kongzue.dialogx.interfaces.OnDialogButtonClickListener;
import com.kongzue.dialogx.style.MaterialStyle;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

/**
 * @author: Kongzue
 * @github: https://github.com/kongzue/
 * @homepage: http://kongzue.com/
 * @mail: myzcxhh@live.cn
 * @createTime: 2021/10/5 10:24
 */
public class CalendarDialog {
    
    protected int selectYearIndex, selectMonthIndex, selectDayIndex;
    protected BottomDialog bottomDialog;
    protected int maxYear = 2100, minYear = 1900;
    protected int maxYearMonth = 12, minYearMonth = 1;
    protected int maxYearDay = 30, minYearDay = 1;
    protected String yearLabel = "年", monthLabel = "月", dayLabel = "日";
    protected String title;
    protected int selectedYearIndex, selectedMonthIndex, selectedDayIndex;
    
    public static CalendarDialog build() {
        return new CalendarDialog();
    }
    
    public CalendarDialog setDefaultSelect(int year, int month, int day) {
        selectYearIndex = year - minYear;
        selectMonthIndex = month - 1;
        selectDayIndex = day - 1;
        
        selectedYearIndex = year - minYear;
        selectedMonthIndex = month - 1;
        selectedDayIndex = day - 1;
        return this;
    }
    
    public CalendarDialog cleanDefaultSelect() {
        selectYearIndex = 0;
        selectMonthIndex = 0;
        selectDayIndex = 0;
        return this;
    }
    
    CalendarLabelTextView selectDayViewCache;
    
    public CalendarDialog show(OnDateSelected onDateSelected) {
        bottomDialog = BottomDialog.show(new OnBindView<BottomDialog>(R.layout.dialogx_calendar) {
            
            private TextView txtDialogYearAndMonth;
            private ImageView imgDialogSelectYearAndMonth;
            private ImageView btnLastMonth;
            private ImageView btnNextMonth;
            private LinearLayout boxYearMonthSelector;
            private WheelView idYear;
            private WheelView idMonth;
            private ImageView imgCalendarScreenshot;
            private LinearLayout boxCalendar;
            private TableLayout tabCalendar;
            
            @Override
            public void onBind(BottomDialog dialog, View v) {
                bottomDialog = dialog;
                
                txtDialogYearAndMonth = v.findViewById(R.id.txt_dialog_year_and_month);
                imgDialogSelectYearAndMonth = v.findViewById(R.id.img_dialog_select_year_and_month);
                btnLastMonth = v.findViewById(R.id.btn_last_month);
                btnNextMonth = v.findViewById(R.id.btn_next_month);
                boxYearMonthSelector = v.findViewById(R.id.box_year_month_selector);
                idYear = v.findViewById(R.id.id_year);
                idMonth = v.findViewById(R.id.id_month);
                imgCalendarScreenshot = v.findViewById(R.id.img_calendar_screenshot);
                boxCalendar = v.findViewById(R.id.box_calendar);
                tabCalendar = v.findViewById(R.id.tab_calendar);
                
                initDefaultDate();
                initWheelYearAndMonthPicker();
                initCalendar();
                
                View.OnClickListener switchSelectYearMonthOrCalendarClick;
                txtDialogYearAndMonth.setOnClickListener(switchSelectYearMonthOrCalendarClick = new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (boxCalendar.getVisibility() == View.VISIBLE) {
                            boxCalendar.animate().alpha(0f).withEndAction(new Runnable() {
                                @Override
                                public void run() {
                                    boxCalendar.setVisibility(View.GONE);
                                }
                            });
                            boxYearMonthSelector.setAlpha(0f);
                            boxYearMonthSelector.setVisibility(View.VISIBLE);
                            boxYearMonthSelector.animate().alpha(1f);
                            imgDialogSelectYearAndMonth.animate().rotation(180).setDuration(100);
                            
                            btnLastMonth.animate().alpha(0f).withEndAction(new Runnable() {
                                @Override
                                public void run() {
                                    btnLastMonth.setVisibility(View.GONE);
                                }
                            });
                            btnNextMonth.animate().alpha(0f).withEndAction(new Runnable() {
                                @Override
                                public void run() {
                                    btnNextMonth.setVisibility(View.GONE);
                                }
                            });
                        } else {
                            boxYearMonthSelector.animate().alpha(0f).withEndAction(new Runnable() {
                                @Override
                                public void run() {
                                    boxYearMonthSelector.setVisibility(View.GONE);
                                }
                            });
                            boxCalendar.setAlpha(0f);
                            boxCalendar.setVisibility(View.VISIBLE);
                            boxCalendar.animate().alpha(1f);
                            imgDialogSelectYearAndMonth.animate().rotation(0).setDuration(100);
                            
                            btnLastMonth.setVisibility(View.VISIBLE);
                            btnNextMonth.setVisibility(View.VISIBLE);
                            btnLastMonth.animate().alpha(1f);
                            btnNextMonth.animate().alpha(1f);
                            
                            initCalendar();
                        }
                    }
                });
                imgDialogSelectYearAndMonth.setOnClickListener(switchSelectYearMonthOrCalendarClick);
                
                btnLastMonth.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        boxCalendar.post(new Runnable() {
                            @Override
                            public void run() {
                                Bitmap screenshot = screenshotView(boxCalendar);
                                imgCalendarScreenshot.setImageBitmap(screenshot);
                                
                                imgCalendarScreenshot.setVisibility(View.VISIBLE);
                                imgCalendarScreenshot.setX(0);
                                
                                boxCalendar.setX(-boxCalendar.getWidth());
                                
                                if (selectMonthIndex == 0) {
                                    selectMonthIndex = 11;
                                    selectYearIndex--;
                                } else {
                                    selectMonthIndex--;
                                }
                                initCalendar();
                                
                                imgCalendarScreenshot.animate().setInterpolator(new DecelerateInterpolator(2f)).x(boxCalendar.getWidth());
                                boxCalendar.animate().setInterpolator(new DecelerateInterpolator(2f)).x(0);
                            }
                        });
                    }
                });
                
                btnNextMonth.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        boxCalendar.post(new Runnable() {
                            @Override
                            public void run() {
                                Bitmap screenshot = screenshotView(boxCalendar);
                                imgCalendarScreenshot.setImageBitmap(screenshot);
                                
                                imgCalendarScreenshot.setVisibility(View.VISIBLE);
                                imgCalendarScreenshot.setX(0);
                                
                                boxCalendar.setX(boxCalendar.getWidth());
                                
                                if (selectMonthIndex == 11) {
                                    selectMonthIndex = 0;
                                    selectYearIndex++;
                                } else {
                                    selectMonthIndex++;
                                }
                                initCalendar();
                                
                                imgCalendarScreenshot.animate().setInterpolator(new DecelerateInterpolator(2f)).x(-boxCalendar.getWidth());
                                boxCalendar.animate().setInterpolator(new DecelerateInterpolator(2f)).x(0);
                            }
                        });
                    }
                });
            }
            
            private void initWheelYearAndMonthPicker() {
                List<String> yearList = new ArrayList<>();
                for (int i = minYear; i <= maxYear; i++) {
                    yearList.add(i + yearLabel);
                }
                ArrayWheelAdapter<String> yearAdapter = new ArrayWheelAdapter<String>(BottomDialog.getContext(), yearList);
                yearAdapter.setItemResource(R.layout.default_item_date);
                yearAdapter.setItemTextResource(R.id.default_item_date_name_tv);
                yearAdapter.setTextColor(bottomDialog.getResources().getColor(bottomDialog.isLightTheme() ? R.color.black60 : R.color.white70));
                idYear.setViewAdapter(yearAdapter);
                idYear.setCurrentItem(selectYearIndex < yearList.size() ? selectYearIndex : 0);
                idYear.addChangingListener(new OnWheelChangedListener() {
                    @Override
                    public void onChanged(WheelView wheel, int oldValue, int newValue) {
                        selectYearIndex = newValue;
                        txtDialogYearAndMonth.setText((selectYearIndex + minYear) + yearLabel + (selectMonthIndex + 1) + monthLabel);
                    }
                });
                
                List<String> monthList = new ArrayList<>();
                if (selectYearIndex == 0) {
                    //min
                    for (int i = Math.max(minYearMonth, 1); i <= 12; i++) {
                        monthList.add(i + monthLabel);
                    }
                } else if (selectYearIndex == yearList.size() - 1) {
                    //max
                    for (int i = 1; i <= (maxYearMonth == 0 ? 12 : Math.min(12, maxYearMonth)); i++) {
                        monthList.add(i + monthLabel);
                    }
                } else {
                    for (int i = 1; i <= 12; i++) {
                        monthList.add(i + monthLabel);
                    }
                }
                ArrayWheelAdapter<String> monthAdapter = new ArrayWheelAdapter<String>(BottomDialog.getContext(), monthList);
                monthAdapter.setItemResource(R.layout.default_item_date);
                monthAdapter.setItemTextResource(R.id.default_item_date_name_tv);
                monthAdapter.setTextColor(bottomDialog.getResources().getColor(bottomDialog.isLightTheme() ? R.color.black60 : R.color.white70));
                idMonth.setViewAdapter(monthAdapter);
                idMonth.setCurrentItem(selectMonthIndex < monthList.size() ? selectMonthIndex : 0);
                idMonth.addChangingListener(new OnWheelChangedListener() {
                    @Override
                    public void onChanged(WheelView wheel, int oldValue, int newValue) {
                        selectMonthIndex = newValue;
                        txtDialogYearAndMonth.setText((selectYearIndex + minYear) + yearLabel + (selectMonthIndex + 1) + monthLabel);
                    }
                });
            }
            
            private void initCalendar() {
                txtDialogYearAndMonth.setText((selectYearIndex + minYear) + yearLabel + (selectMonthIndex + 1) + monthLabel);
                
                Calendar calendar = Calendar.getInstance();
                calendar.set(Calendar.YEAR, (selectYearIndex + minYear));
                calendar.set(Calendar.MONTH, selectMonthIndex);
                calendar.set(Calendar.DAY_OF_MONTH, 1);
                
                boolean isFirstSunday = (calendar.getFirstDayOfWeek() == Calendar.SUNDAY);
                int weekDay = calendar.get(Calendar.DAY_OF_WEEK);
                if (isFirstSunday) {
                    weekDay = weekDay - 1;
                }
                tabCalendar.removeAllViews();
                for (int i = 0; i < weekDay; i++) {
                    tabCalendar.addView(new TextView(tabCalendar.getContext()));
                }
                
                int lastDay = getLastDayOfMonth((selectYearIndex + minYear), selectMonthIndex + 1);
                for (int i = 1; i <= lastDay; i++) {
                    CalendarLabelTextView dayView = new CalendarLabelTextView(tabCalendar.getContext());
                    dayView.setGravity(Gravity.CENTER);
                    dayView.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 18);
                    dayView.setText(String.valueOf(i));
                    dayView.setTag(i);
                    
                    setCalendarLabelStatus(dayView, false);
                    
                    //如果是之前选择的日期，放大+变红
                    if ((i == selectDayIndex + 1) && selectMonthIndex == selectedMonthIndex && selectYearIndex == selectedYearIndex) {
                        setCalendarLabelStatus(dayView, true);
                    }
                    
                    dayView.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            setCalendarLabelStatus(selectDayViewCache, false);
                            setCalendarLabelStatus((CalendarLabelTextView) v, true);
                        }
                    });
                    dayView.setOnTouchListener(new View.OnTouchListener() {
                        
                        boolean isTouch;
                        float x;
                        
                        @Override
                        public boolean onTouch(View v, MotionEvent event) {
                            switch (event.getAction()) {
                                case MotionEvent.ACTION_DOWN:
                                    x = event.getX();
                                    isTouch = true;
                                    break;
                                case MotionEvent.ACTION_CANCEL:
                                case MotionEvent.ACTION_UP:
                                    if (isTouch) {
                                        if (event.getX() - x > bottomDialog.dip2px(10)) {
                                            btnLastMonth.callOnClick();
                                        }
                                        if (event.getX() - x < -bottomDialog.dip2px(10)) {
                                            btnNextMonth.callOnClick();
                                        }
                                    }
                                    isTouch = false;
                                    break;
                            }
                            return false;
                        }
                    });
                    tabCalendar.addView(dayView);
                }
            }
        })
                .setCancelable(true)
                .setAllowInterceptTouch(false);
        if (DialogX.globalStyle instanceof MaterialStyle) {
            bottomDialog.setOkButton(R.string.dialogx_date_picker_ok_button, new OnDialogButtonClickListener<BottomDialog>() {
                @Override
                public boolean onClick(BottomDialog baseDialog, View v) {
                    int year = minYear + selectYearIndex;
                    int month = 1 + selectMonthIndex;
                    int day = 1 + selectDayIndex;
                    onDateSelected.onSelect(year + "-" + month + "-" + day,
                            year,
                            month,
                            day
                    );
                    return false;
                }
            }).setCancelButton(R.string.dialogx_date_picker_dialog_cancel, new OnDialogButtonClickListener<BottomDialog>() {
                @Override
                public boolean onClick(BottomDialog baseDialog, View v) {
                    onDateSelected.onCancel();
                    return false;
                }
            });
        } else {
            bottomDialog.setCancelButton(R.string.dialogx_date_picker_ok_button, new OnDialogButtonClickListener<BottomDialog>() {
                @Override
                public boolean onClick(BottomDialog baseDialog, View v) {
                    int year = minYear + selectYearIndex;
                    int month = 1 + selectMonthIndex;
                    int day = 1 + selectDayIndex;
                    onDateSelected.onSelect(year + "-" + month + "-" + day,
                            year,
                            month,
                            day
                    );
                    return false;
                }
            });
        }
        return this;
    }
    
    private void setCalendarLabelStatus(CalendarLabelTextView view, boolean select) {
        if (view == null) return;
        if (select) {
            selectDayIndex = (int) view.getTag() - 1;
            view.setTypeface(Typeface.defaultFromStyle(Typeface.BOLD));
            view.setTextColor(bottomDialog.getResources().getColor(
                    (bottomDialog.isLightTheme() ? R.color.white : R.color.black)
            ));
            view.setSelect(true);
            selectDayViewCache = view;
        } else {
            int i = view.getTag() != null ? (int) view.getTag() : -1;
            if (i == nowDay && selectMonthIndex == nowMonth && selectYearIndex == nowYear - minYear) {
                view.setTypeface(Typeface.defaultFromStyle(Typeface.NORMAL));
                view.setSelect(false);
                view.setTextColor(bottomDialog.getResources().getColor(R.color.dialogXCalendarToday));
            } else {
                view.setTypeface(Typeface.defaultFromStyle(Typeface.NORMAL));
                view.setSelect(false);
                view.setTextColor(bottomDialog.getResources().getColor(
                        (bottomDialog.isLightTheme() ? R.color.black : R.color.white)
                ));
            }
        }
    }
    
    //API-23+ 支持水波纹效果
    private Drawable getDefaultCalendarItemBackground() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            int[] attrs = new int[]{android.R.attr.selectableItemBackgroundBorderless};
            TypedArray ta = BottomDialog.getContext().obtainStyledAttributes(attrs);
            Drawable mDefaultFocusHighlightCache = ta.getDrawable(0);
            ta.recycle();
            return mDefaultFocusHighlightCache;
        }
        return null;
    }
    
    //记录今天日期
    private int nowYear, nowMonth, nowDay;
    
    private void initDefaultDate() {
        Calendar calendar = Calendar.getInstance();
        nowYear = calendar.get(Calendar.YEAR);
        if (selectYearIndex == 0) {
            selectYearIndex = calendar.get(Calendar.YEAR) - minYear;
        }
        nowMonth = calendar.get(Calendar.MONTH);
        if (selectMonthIndex == 0) {
            selectMonthIndex = calendar.get(Calendar.MONTH);
        }
        nowDay = calendar.get(Calendar.DAY_OF_MONTH);
        if (selectDayIndex == 0) {
            selectDayIndex = calendar.get(Calendar.DAY_OF_MONTH) - 1;
        }
    }
    
    private int getLastDayOfMonth(int year, int month) {
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.YEAR, year);
        cal.set(Calendar.MONTH, month - 1);
        cal.set(Calendar.DAY_OF_MONTH, 1);
        cal.add(Calendar.MONTH, 1);
        cal.set(Calendar.DAY_OF_MONTH, 0);
        return cal.get(Calendar.DAY_OF_MONTH);
    }
    
    private Bitmap screenshotView(View view) {
        view.setDrawingCacheEnabled(true);
        view.buildDrawingCache(true);
        Bitmap bitmap = Bitmap.createBitmap(view.getDrawingCache());
        view.setDrawingCacheEnabled(false);
        return bitmap;
    }
    
    
    public int getMaxYear() {
        return maxYear;
    }
    
    public int getMinYear() {
        return minYear;
    }
    
    public CalendarDialog setMaxYear(int maxYear) {
        this.maxYear = maxYear;
        return this;
    }
    
    public CalendarDialog setMinYear(int minYear) {
        this.minYear = minYear;
        return this;
    }
    
    public int getMaxYearMonth() {
        return maxYearMonth;
    }
    
    public CalendarDialog setMaxYearMonth(int maxYearMonth) {
        this.maxYearMonth = maxYearMonth;
        return this;
    }
    
    public int getMinYearMonth() {
        return minYearMonth;
    }
    
    public CalendarDialog setMinYearMonth(int minYearMonth) {
        this.minYearMonth = minYearMonth;
        return this;
    }
    
    public int getMaxYearDay() {
        return maxYearDay;
    }
    
    public CalendarDialog setMaxYearDay(int maxYearDay) {
        this.maxYearDay = maxYearDay;
        return this;
    }
    
    public int getMinYearDay() {
        return minYearDay;
    }
    
    public CalendarDialog setMinYearDay(int minYearDay) {
        this.minYearDay = minYearDay;
        return this;
    }
    
    public CalendarDialog setMinTime(int minYear, int minMonth, int minDay) {
        this.minYear = minYear;
        this.minYearMonth = minMonth;
        this.minYearDay = minDay;
        return this;
    }
    
    public CalendarDialog setMaxTime(int maxYear, int maxMonth, int maxDay) {
        this.maxYear = maxYear;
        this.maxYearMonth = maxMonth;
        this.maxYearDay = maxDay;
        return this;
    }
    
    
    public String getYearLabel() {
        return yearLabel;
    }
    
    public CalendarDialog setYearLabel(String yearLabel) {
        this.yearLabel = yearLabel;
        return this;
    }
    
    public String getMonthLabel() {
        return monthLabel;
    }
    
    public CalendarDialog setMonthLabel(String monthLabel) {
        this.monthLabel = monthLabel;
        return this;
    }
    
    public String getDayLabel() {
        return dayLabel;
    }
    
    public CalendarDialog setDayLabel(String dayLabel) {
        this.dayLabel = dayLabel;
        return this;
    }
    
}
