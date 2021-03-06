package com.jonathongrigg.proton.voltagecontrol;

import android.app.Activity;
import android.content.SharedPreferences;
import android.graphics.PorterDuff;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.Spinner;
import android.widget.Toast;

public class ProtonPrefs extends Activity{

	public final static String THEME_SETTING = "theme";
	public final static String SAVE_ON_BOOT = "saveonboot";

	//commands
	protected static final String C_LIST_INIT_D = "ls /etc/init.d/";
	protected static final String C_LIST_SUPER2 = "ls /etc/super2/";
	protected static final String C_LIST_ETC = "ls /etc/";
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		
		SharedPreferences protonPreferences = getSharedPreferences("protonSavedPrefs", 0);
		int choosenTheme = protonPreferences.getInt(THEME_SETTING, 0);
		if(choosenTheme == 1)
			setContentView(R.layout.settings);
		else if (choosenTheme == 2){
			setContentView(R.layout.settings_proton_blk_theme);
			findViewById(R.id.saveSettings).getBackground().setColorFilter(0xFF666666, PorterDuff.Mode.MULTIPLY);
		}
		else  { // load default theme
			setContentView(R.layout.settings_proton_theme);
			findViewById(R.id.saveSettings).getBackground().setColorFilter(0xFF8d2122, PorterDuff.Mode.MULTIPLY);
		}
		
				
		findViewById(R.id.saveSettings).setOnClickListener(mOnConfigClickListener);
		
		//load layout options from strings
		Spinner themeSpinner = (Spinner)findViewById(R.id.themeSpinner);
		ArrayAdapter<CharSequence> theme = ArrayAdapter.createFromResource(this, R.array.theme_array, R.xml.spinner_settings);
		theme.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		themeSpinner.setAdapter(theme);

        // Update spinner to show currently selected theme
        int curTheme = protonPreferences.getInt(THEME_SETTING, 0);
		themeSpinner.setSelection(curTheme);
		
		//Update checkbox to show user pref on saving voltages after reboot
		boolean saveBoot = protonPreferences.getBoolean(SAVE_ON_BOOT, false);
		CheckBox saveonbootCheckbox = (CheckBox) findViewById(R.id.checkBox1);
		saveonbootCheckbox.setChecked(saveBoot);
            
	}
	
	View.OnClickListener mOnConfigClickListener = new View.OnClickListener() {	
			
			public void onClick(View v) {
	
				switch(v.getId())	{
					case R.id.saveSettings:
						SharedPreferences protonPreferences = getSharedPreferences("protonSavedPrefs", 0);
						
						Spinner spinner = (Spinner)findViewById(R.id.themeSpinner);
						int selectedUnit = spinner.getSelectedItemPosition();
						
				        SharedPreferences.Editor editor = protonPreferences.edit();
				        editor.putInt(THEME_SETTING, selectedUnit);
				        editor.putBoolean(SAVE_ON_BOOT, ((CheckBox)findViewById(R.id.checkBox1)).isChecked());
				        editor.commit(); // save settings
				        if(!((CheckBox)findViewById(R.id.checkBox1)).isChecked())
				        	removeBootSettings();
				        finish();
						break;
				}
			}
	};
	
	private void removeBootSettings() {
		
		// Check if ROM is SuperAOSP (doesn't use init.d, uses super2)
		boolean superAosp = false;
		if (!ShellInterface.getProcessOutput(C_LIST_ETC).contains("super2")) {
			superAosp = true;
		}
				
		if (superAosp == false && !ShellInterface.getProcessOutput(C_LIST_INIT_D).contains("proton_voltage_control")) {
			Toast.makeText(getBaseContext(), "Error: No Saved Boot Settings Present", Toast.LENGTH_LONG).show();
		}
		else if (superAosp == true && !ShellInterface.getProcessOutput(C_LIST_SUPER2).contains("proton_voltage_control")) {
			Toast.makeText(getBaseContext(), "Error: No Saved Boot Settings Present", Toast.LENGTH_LONG).show();
		}
		else {
			if (superAosp = true) {
				ShellInterface.runCommand("busybox mount -o remount,rw  /system");
				ShellInterface.runCommand("rm /etc/super2/proton_voltage_control");
				ShellInterface.runCommand("busybox mount -o remount,ro  /system");
				Toast.makeText(this, "Removed settings saved in file \"/etc/super2/proton_voltage_control\"", Toast.LENGTH_LONG).show();
			}
			else {
				ShellInterface.runCommand("busybox mount -o remount,rw  /system");
				ShellInterface.runCommand("rm /etc/init.d/proton_voltage_control");
				ShellInterface.runCommand("busybox mount -o remount,ro  /system");
				Toast.makeText(this, "Removed settings saved in file \"/etc/init.d/proton_voltage_control\"", Toast.LENGTH_LONG).show();
			}
		}
	}

}
