package com.philipp_mandler.hexapod.android;

import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;

public class PagesFragment extends ListFragment {
	
	MainActivity parent;
	ArrayAdapter<PageItem> pages;
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {		
		pages = new ArrayAdapter<PageItem>(inflater.getContext(), android.R.layout.simple_list_item_1 ,android.R.id.text1);
        pages.add(new PageItem("Control Leg (Front)", new LegFragment()));
        //pages.add(new PageItem("Control Leg (Top)", new LegTopFragment()));
        setListAdapter(pages);
        
        if(!pages.isEmpty() && parent != null)
        	parent.getFragmentManager().beginTransaction().replace(R.id.content_fragment, pages.getItem(0).getFragment()).commit();
        
		return super.onCreateView(inflater, container, savedInstanceState);
	}
	
	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		if(activity instanceof MainActivity) {
			parent = (MainActivity)activity;
		}
	}
	
	@Override
	public void onListItemClick(ListView l, View v, int position, long id) {
		if(parent != null)
			if(parent.getFragmentManager().findFragmentById(R.id.content_fragment) != pages.getItem(position).getFragment())
				parent.getFragmentManager().beginTransaction().replace(R.id.content_fragment, pages.getItem(position).getFragment()).commit();
		
		super.onListItemClick(l, v, position, id);
	}
}
