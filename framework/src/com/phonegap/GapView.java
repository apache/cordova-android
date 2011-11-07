package com.phonegap;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.widget.EditText;

public class GapView extends WebView {

	public GapView(Context context) {
		super(context);
		// TODO Auto-generated constructor stub
	}

	@Override
	protected void attachViewToParent(View child, int index, ViewGroup.LayoutParams params)
	{
		if(child.getClass() != EditText.class)
			super.attachViewToParent(child, index, params);
		else
		{
			super.attachViewToParent(child, index, params);
		}
	}
	
	@Override
	protected boolean addViewInLayout(View child, int index, ViewGroup.LayoutParams params)
	{
		return super.addViewInLayout(child, index, params);
	}
	
	@Override
	protected boolean addViewInLayout(View child, int index, ViewGroup.LayoutParams params, boolean preventRequestLayout)
	{
		return super.addViewInLayout(child, index, params);
	}
}
