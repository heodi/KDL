package com.iceapp;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import android.os.Bundle;
import android.os.Environment;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ListActivity;
import android.content.DialogInterface;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.Toast;

public class FileBrowser extends ListActivity {
	 
	private List<String> item = null;
	private List<String> path = null;
	private String root;
	private TextView myPath;
	private String localDir;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.file_browser);
		myPath = (TextView)findViewById(R.id.path);
	    root = Environment.getExternalStorageDirectory().getPath();
	    getDir(root);
	    
	}
	    
	private void getDir(String dirPath){
		myPath.setText("Location: " + dirPath);
	    item = new ArrayList<String>();
	    path = new ArrayList<String>();
	    File f = new File(dirPath);
	    File[] files = f.listFiles();
	     
	    if(!dirPath.equals(root)){
	    	item.add(root);
	    	path.add(root);
	    	item.add("../");
	    	path.add(f.getParent()); 
	    }
	     
	    for(int i=0; i < files.length; i++){
	    	File file = files[i];
	    	if(!file.isHidden() && file.canRead()){
	    		path.add(file.getPath());
	    		if(file.isDirectory()){
	    			item.add(file.getName() + "/");
	    		}else{
	    			item.add(file.getName());
	    		}
	    	} 
	    }

	    ArrayAdapter<String> fileList = new ArrayAdapter<String>(this, R.layout.file_browser_item, item);
	    setListAdapter(fileList); 
	    this.getListView().setLongClickable(true);
	    this.getListView().setOnItemLongClickListener(new OnItemLongClickListener() {
	    	@Override
            public boolean onItemLongClick(AdapterView<?> arg0, View arg1,int position, long row_id) {
	    		localDir=path.get(position);
	    		//Pop out alert dialog
	    		openAlert(arg1);

	    	    return true;
            }
        });                      
	}

	@Override
	protected void onListItemClick(ListView l, View v, int position, long id) {
	// TODO Auto-generated method stub
		File file = new File(path.get(position));
		if (file.isDirectory()){
			if(file.canRead()){
				getDir(path.get(position));
			}else{
				new AlertDialog.Builder(this)
				.setIcon(R.drawable.ic_launcher)
				.setTitle("[" + file.getName() + "] folder can't be read!")
				.setPositiveButton("OK", null).show(); 
			} 
		}else {
			new AlertDialog.Builder(this)
			.setIcon(R.drawable.ic_launcher)
			.setTitle("[" + file.getName() + "]")
			.setPositiveButton("OK", null).show();
		}
	}
	
	private void openAlert(View view) {
		AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(FileBrowser.this);
		alertDialogBuilder.setTitle("¤w¿ï¾Ü¸ô®|");
		alertDialogBuilder.setMessage(localDir);
		// set positive button: Yes message
		alertDialogBuilder.setPositiveButton("Yes",new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog,int id) {
				// Return selected path
				Bundle bundle = new Bundle();  
	    	    bundle.putString("path", /*myPath.getText().toString()+*/localDir);  
	    	    Intent intent = new Intent();  
	    	    intent.putExtras(bundle);  
	    	    setResult(RESULT_OK, intent);  
	    	    finish();
			}
		});
		// set negative button: No message
		alertDialogBuilder.setNegativeButton("No",new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog,int id) {
				// cancel the alert box and put a Toast to the user
				dialog.cancel();
				Log.d("dialog", "Alert dialog: no");
			}
		});
		
		AlertDialog alertDialog = alertDialogBuilder.create();
		// show alert
		alertDialog.show();
	}

	
}