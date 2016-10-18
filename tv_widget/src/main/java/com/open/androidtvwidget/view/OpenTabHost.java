package com.open.androidtvwidget.view;

import java.util.ArrayList;
import java.util.List;

import com.open.androidtvwidget.R;
import com.open.androidtvwidget.adapter.BaseTabTitleAdapter;

import android.content.Context;
import android.graphics.Color;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TabHost;
import android.widget.TabWidget;

/**
 * 标题栏控件.
 * 
 * @author hailongqiu
 *
 */
public class OpenTabHost extends TabHost {

	public OpenTabHost(Context context) {
		super(context);
		init(context, null);
	}

	public OpenTabHost(Context context, AttributeSet attrs) {
		super(context, attrs);
		init(context, attrs);
	}

	private Context mContext;
	private TabWidget mTabWidget; // 标题栏.

	private void init(Context context, AttributeSet attrs) {
		this.mContext = context;
		LayoutInflater.from(context).inflate(R.layout.tabhost_title_head, this, true);
		mTabWidget = (TabWidget) findViewById(android.R.id.tabs);
		setup();
		mTabWidget.setStripEnabled(false);
		initViewEvents();
		this.setBackgroundColor(Color.TRANSPARENT);
	}

	private void initViewEvents() {
		setOnTabChangedListener(new OnTabChangeListener() {
			@Override
			public void onTabChanged(String tabId) {
				int position = getCurrentTab();
				OnTabSelectListener cb = mOnTabSelectListener;
				if (cb != null) {
					TabWidget tw = getTabWidget();
					View titleWidget = tw.getChildTabViewAt(position);
					cb.onTabSelect(OpenTabHost.this, titleWidget, position);
				}
			}
		});
	}

	private OnTabSelectListener mOnTabSelectListener;

	public void setOnTabSelectListener(OnTabSelectListener cb) {
		this.mOnTabSelectListener = cb;
	}

	public OnTabSelectListener getOnTabSelectListener() {
		return this.mOnTabSelectListener;
	}

	public void addTabWidget(TabHost.TabSpec tabSpec) {
		tabSpec.setContent(new DummyTabFactory(mContext));
		this.addTab(tabSpec);
	}

	class DummyTabFactory implements TabHost.TabContentFactory {
		private final Context mContext;

		public DummyTabFactory(Context context) {
			this.mContext = context;
		}
		
		/**
		 * 创建一个空的Content.
		 */
		@Override
		public View createTabContent(String tag) {
			View v = new View(mContext);
			v.setMinimumWidth(0);
			v.setMinimumHeight(0);
			return v;
		}
	}

	private BaseTabTitleAdapter mAdapter;
	private List<View> mCacheView = new ArrayList<View>();

	public void setAdapter(BaseTabTitleAdapter adapter) {
		mCacheView.clear();
		clearAllTabs();
		this.mAdapter = adapter;
		if (this.mAdapter != null) {
			int count = this.mAdapter.getCount();
			if (count > 0) {
				for (int i = 0; i < count; i++) {
					View titleView = this.mAdapter.getView(i, null, this);
					mCacheView.add(titleView);
					TabSpec tabSpec = this.newTabSpec(i + "").setIndicator(titleView);
					this.addTabWidget(tabSpec);
				}
				requestLayout();
			}
		}
	}
	
	public List<View> getAllTitleView() {
		return this.mCacheView;
	}
	
	public View getTitleViewIndexAt(int index) {
		return this.mCacheView.get(index);
	}
	
	public View getTitleViewIdAt(int index) {
		return this.mCacheView.get(index).findViewById(this.mAdapter.getTitleWidgetID(index));
	}
	
	public interface OnTabSelectListener {
		public void onTabSelect(OpenTabHost openTabHost, View titleWidget, int postion);
	}
}
