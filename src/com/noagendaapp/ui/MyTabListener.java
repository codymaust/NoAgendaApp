package com.noagendaapp.ui;

import com.noagendaapp.R;

import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentTransaction;
import android.app.ActionBar.Tab;
import android.app.ActionBar.TabListener;

public class MyTabListener<T extends Fragment> implements TabListener 
{
	private Fragment mFragment;
	private final Activity mActivity;
	private final String mTag;
	private final Class<T> mClass;
	
	/**
     * Constructor used each time a new tab is created.
     * 
     * @param activity
     *            The host Activity, used to instantiate the fragment
     * @param tag
     *            The identifier tag for the fragment
     * @param clz
     *            The fragment's Class, used to instantiate the fragment
     */
	
	public MyTabListener(Activity activity, String tag, Class<T> clz)
	{
		mActivity = activity;
		mTag = tag;
		mClass = clz;
	}
	
    /* The following are each of the ActionBar.TabListener callbacks */
	public void onTabSelected(Tab tab, FragmentTransaction ft)
	{
		// Check if the fragment is already initalized
		if (mFragment == null)
		{
			// If not, instantiate and add it to the activity
			mFragment = Fragment.instantiate(mActivity, mClass.getName());
			ft.add(R.id.content_layout, mFragment, mTag);
		} else {
			// If it exists, simply attach it in the order to show it
	        //ft.setCustomAnimations(android.R.animator.fade_in, R.animator.test);
			ft.attach(mFragment);
		}
	}

    public void onTabUnselected(Tab tab, FragmentTransaction ft) 
    {
        if (mFragment != null) 
        {
          //ft.setCustomAnimations(android.R.animator.fade_in, R.animator.test);
          ft.detach(mFragment);
        }
      }

      public void onTabReselected(Tab tab, FragmentTransaction ft) 
      {
      }
}