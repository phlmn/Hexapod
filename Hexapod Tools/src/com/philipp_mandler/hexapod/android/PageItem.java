package com.philipp_mandler.hexapod.android;

import android.app.Fragment;


public class PageItem {
	private String m_label;
	private Fragment m_fragment;
	
	public PageItem(String label, Fragment fragment) {
		m_label = label;
		m_fragment = fragment;
	}
	
	public void setLabel(String label) {
		m_label = label;
	}
	
	public String getLabel() {
		return m_label;
	}
	
	public void setFragment(Fragment fragment) {
		m_fragment = fragment;
	}
	
	public Fragment getFragment() {
		return m_fragment;
	}
	
	public String toString() {
		return m_label;
	}
}
