package datacollect.ryan.grant.com.datacollect.barcode;

import android.content.Context;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Set;

import datacollect.ryan.grant.com.datacollect.R;
import datacollect.ryan.grant.com.datacollect.barcode.BarcodeCaptureActivity;
import datacollect.ryan.grant.com.datacollect.storage.SharedPrefHelper;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    // use a compound button so either checkbox or switch widgets work.
    private CompoundButton autoFocus;
    private CompoundButton useFlash;
    private AutoCompleteTextView mUserName;
    private Button mChangeUsername;
    private boolean isEditingUsername = false;
    private boolean noUser = true;
    private Set<String> history;

    private static final int RC_BARCODE_CAPTURE = 9001;
    private static final String TAG = "BarcodeMain";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar mToolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(mToolbar);

        mUserName = (AutoCompleteTextView) findViewById(R.id.user_name);

        mChangeUsername = (Button) findViewById(R.id.username_button);
        mChangeUsername.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                isEditingUsername = !isEditingUsername;
                if (isEditingUsername) {
                    mChangeUsername.setText("Save");
                    mUserName.setEnabled(true);
                    mUserName.setText("");
                    mUserName.requestFocus();
                    InputMethodManager imm = (InputMethodManager) MainActivity.this.getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.toggleSoftInput(InputMethodManager.SHOW_FORCED, InputMethodManager.HIDE_IMPLICIT_ONLY);
                } else {
                    mChangeUsername.setText("Edit");
                    mUserName.setEnabled(false);
                    if (!mUserName.getText().toString().equals("")) {
                        addSearchInput(mUserName.getText().toString());
                        SharedPrefHelper.getInstance(MainActivity.this).setCurrentUser(mUserName.getText().toString());
                    } else {
                        mUserName.setText(SharedPrefHelper.getInstance(MainActivity.this).getCurrentUser());
                        mUserName.dismissDropDown();
                    }
                    noUser = false;
                }
            }
        });


        String currentUser = SharedPrefHelper.getInstance(this).getCurrentUser();
        if (currentUser.equals("null")) {
            isEditingUsername = true;
            mUserName.setEnabled(true);
            mChangeUsername.setText("Save");
        } else {
            mUserName.setText(currentUser);
            noUser = false;
        }

        history = SharedPrefHelper.getInstance(this).getUsersHistory();
        setAutoCompleteSource();


        autoFocus = (CompoundButton) findViewById(R.id.auto_focus);
        useFlash = (CompoundButton) findViewById(R.id.use_flash);

        findViewById(R.id.read_barcode).setOnClickListener(this);
    }


    private void setAutoCompleteSource() {
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(
                this, android.R.layout.simple_list_item_1, history.toArray(new String[history.size()]));
        mUserName.setAdapter(adapter);
    }

    private void addSearchInput(String input) {
        if (!history.contains(input)) {
            history.add(input);
            setAutoCompleteSource();
        }
    }

    private void savePrefs() {
        SharedPrefHelper.getInstance(this).saveUsersHistory(history);
    }

    @Override
    protected void onStop() {
        super.onStop();
        savePrefs();
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.read_barcode) {
            if (noUser) {
                Toast.makeText(this, "Please enter your name..", Toast.LENGTH_SHORT).show();
                return;
            }
            if (TextUtils.isEmpty(mUserName.getText().toString())) {
                Toast.makeText(this, "Name cannot be empty..", Toast.LENGTH_SHORT).show();
                return;
            }
            // launch barcode activity.
            Intent intent = new Intent(this, BarcodeCaptureActivity.class);
            intent.putExtra(BarcodeCaptureActivity.AutoFocus, autoFocus.isChecked());
            intent.putExtra(BarcodeCaptureActivity.UseFlash, useFlash.isChecked());

            startActivityForResult(intent, RC_BARCODE_CAPTURE);
        }

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        /*if (requestCode == RC_BARCODE_CAPTURE) {
            if (resultCode == CommonStatusCodes.SUCCESS) {
                if (data != null) {
                    Barcode barcode = data.getParcelableExtra(BarcodeCaptureActivity.BarcodeObject);
                    statusMessage.setText(R.string.barcode_success);
                    barcodeValue.setText(barcode.displayValue);
                    Log.d(TAG, "Barcode read: " + barcode.displayValue);
                } else {
                    statusMessage.setText(R.string.barcode_failure);
                    Log.d(TAG, "No barcode captured, intent data is null");
                }
            } else {
                statusMessage.setText(String.format(getString(R.string.barcode_error),
                        CommonStatusCodes.getStatusCodeString(resultCode)));
            }
        } else {
            super.onActivityResult(requestCode, resultCode, data);
        }*/
    }
}
