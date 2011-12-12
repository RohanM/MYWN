package com.rohanmitchell.mywn;

import android.app.ListActivity;
import android.os.Bundle;
import android.widget.ArrayAdapter;

public class MYWN extends ListActivity
{
    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

	setListAdapter(new ArrayAdapter<String>(this, R.layout.task_row, new String[] {"One", "Two", "Three"}));
    }
}
