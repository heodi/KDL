package com.iceapp;

import android.os.Bundle;
import android.content.ContentValues;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.*;
import android.app.Activity;

public class ToolsActivity extends Activity {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.tools_menu);
        this.setTitle("¤u¨ã");
        initViews();
        setListensers();
    }
    
    private Button PdaSetBtn, ResetNumBtn;
	
    private void initViews() {
    	PdaSetBtn = (Button)findViewById(R.id.butPdaSetting);
    	ResetNumBtn = (Button)findViewById(R.id.butResetOrdNum);
    }
    
    private void setListensers() {
    	PdaSetBtn.setOnClickListener(PdaSetClick);
    	ResetNumBtn.setOnClickListener(ResetNumClick);
    }
    
    private Button.OnClickListener PdaSetClick = new Button.OnClickListener() {
    	@Override
    	public void onClick(View v) {
			Intent intent = new Intent(ToolsActivity.this, PdaEntryActivity.class);
			startActivity(intent);
    	}
    };

    private Button.OnClickListener ResetNumClick = new Button.OnClickListener() {
    	@Override
    	public void onClick(View v) {
			Intent intent = new Intent(ToolsActivity.this, ResetOrderNumActivity.class);
			startActivity(intent);
    	}
    };
    
    
}
