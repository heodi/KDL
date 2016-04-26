package com.iceapp;

import java.io.File;

import android.support.v7.app.ActionBarActivity;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.support.v7.app.ActionBar;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.os.Environment;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.os.Build;
import android.widget.EditText;
import android.database.Cursor;
import android.content.ContentValues;
import android.content.Intent;
import android.app.Activity;
import android.view.MenuInflater;

public class LoginActivity extends Activity {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.login);
        this.setTitle("系統登入 ".concat(getString(R.string.version)));
        initViews();
        setListensers();
        useridTxt.requestFocus();
    }

    private Button LoginBtn, ClearBtn;
    private EditText useridTxt, passwordTxt;
    private DatabaseHelper dbh;
    private IceUtil util;
    private MenuInflater mi;

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        
    	mi = getMenuInflater();
    	mi.inflate(R.menu.tools_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

    	switch (item.getItemId()) {
    	
    		case R.id.impOpt:
				Intent it1 = new Intent(LoginActivity.this, ImportCsvActivity.class);
				startActivity(it1);
				break;

    		case R.id.stkRepOpt:
				Intent it2 = new Intent(LoginActivity.this, ReplenishActivity.class);
				startActivity(it2);
				break;
    	}
    	return true;
    }

    @Override
    public void onBackPressed() {
    	finish();
    }
    	
    
    private void initViews() {
    	LoginBtn = (Button)findViewById(R.id.butLogin);
    	ClearBtn = (Button)findViewById(R.id.butClear);
    	useridTxt = (EditText)findViewById(R.id.editUserId);
    	passwordTxt = (EditText)findViewById(R.id.editPassword);
    	util = new IceUtil();
    	dbh = new DatabaseHelper(this);
    }
    
    private void setListensers() {
    	LoginBtn.setOnClickListener(loginClick);
    	ClearBtn.setOnClickListener(ClearClick);
    }
    
    private Button.OnClickListener loginClick = new Button.OnClickListener() {
    	@Override
    	public void onClick(View v) {
    		boolean valid = validate();
    		if (valid) {
    			//remove all invoice PDF files 
        		File externalStorageDir = Environment.getExternalStorageDirectory();
        		File invDir = new File(externalStorageDir, "ICEAPP/INVOICES");
        		if (invDir.exists()) {
        			for(File file: invDir.listFiles()) file.delete();
        		}    			
    			Intent intent = new Intent(LoginActivity.this, ShowDateTimeActivity.class);
    			startActivityForResult(intent,1);
    		}
    	}
    };
    
    private Button.OnClickListener ClearClick = new Button.OnClickListener() {
    	@Override
    	public void onClick(View v) {
    		useridTxt.setText("");
    		passwordTxt.setText("");
    		useridTxt.requestFocus();
    	}    	
    };
    
    private boolean validate() {
    	String userid = useridTxt.getText().toString().toUpperCase();
    	String password = passwordTxt.getText().toString().toUpperCase();
    	String route;
    	boolean validlogin = true, validpass=true;    	
    	
    	if (userid.equals("")) {
    		util.showDialog("用戶編號不可空白", LoginActivity.this);
    		validlogin = false;
    	} else if (password.equals("")) {
    		util.showDialog("密碼不可空白", LoginActivity.this);
    		validpass = false;
    	} else {
        	SQLiteDatabase db = dbh.getReadableDatabase();
        	Cursor c = db.rawQuery("select route from pda where default_route = 'Y'", null);
        	if (c.moveToFirst()) {
        		route = c.getString(c.getColumnIndex("route"));
        		if (!userid.equals(route))
        			validlogin = false;
        		else {
        			if (userid.equals(this.getString(R.string.loginA1))) {
            			if (!password.equals(this.getString(R.string.loginA1Pwd)))
            				validpass = false;
        			} else if (userid.equals(this.getString(R.string.loginB1))) {
            			if (!password.equals(this.getString(R.string.loginB1Pwd)))
            				validpass = false;        				
        			} else if (userid.equals(this.getString(R.string.loginC1))) {
            			if (!password.equals(this.getString(R.string.loginC1Pwd)))
            				validpass = false;        				
        			} else if (userid.equals(this.getString(R.string.loginD1))) {
            			if (!password.equals(this.getString(R.string.loginD1Pwd)))
            				validpass = false;        				
        			} else if (userid.equals(this.getString(R.string.loginE1))) {
            			if (!password.equals(this.getString(R.string.loginE1Pwd)))
            				validpass = false;        				
        			} else if (userid.equals(this.getString(R.string.loginF1))) {
            			if (!password.equals(this.getString(R.string.loginF1Pwd)))
            				validpass = false;        				
        			} else if (userid.equals(this.getString(R.string.loginG1))) {
            			if (!password.equals(this.getString(R.string.loginG1Pwd)))
            				validpass = false;        				
        			} else if (userid.equals(this.getString(R.string.loginS1))) {
            			if (!password.equals(this.getString(R.string.loginS1Pwd)))
            				validpass = false;        				
        			} else if (userid.equals(this.getString(R.string.loginZ1))) {
            			if (!password.equals(this.getString(R.string.loginZ1Pwd)))
            				validpass = false;        				
        			}
        		}
        		if (!validlogin) {
            		util.showDialog("用戶編號不正確", LoginActivity.this);
            		useridTxt.requestFocus();    			
        		} else if (!validpass) {
        			util.showDialog("密碼不正確", LoginActivity.this);
        			passwordTxt.requestFocus();    			
        		}
        	} else {
        		util.showDialog("找不到預設線路", LoginActivity.this);
        		validlogin = false;
        	}    		
        	db.close();
    	}
    	
    	return validlogin ? validpass : validlogin;
    }       
    
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == 1) {
            finish();
        }
    }
}
