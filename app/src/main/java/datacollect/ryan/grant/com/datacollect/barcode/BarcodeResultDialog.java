package datacollect.ryan.grant.com.datacollect.barcode;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.DialogFragment;
import android.support.v4.content.ContextCompat;
import android.text.InputType;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.vision.barcode.Barcode;

import java.io.IOException;

import datacollect.ryan.grant.com.datacollect.R;
import datacollect.ryan.grant.com.datacollect.storage.FileHelper;
import datacollect.ryan.grant.com.datacollect.storage.SharedPrefHelper;

/**
 * Created by Daniel on 4/13/2017.
 */

public class BarcodeResultDialog extends DialogFragment {

    private static final int RC_HANDLE_STORAGE_PERM = 3;

    public static final String ARG_UPC = "upc_arg";
    private static final String TAG = "Dialog-Result";
    private EditText mPrice;
    private EditText mLocation;
    private TextView mBarcodeValue;
    private Button mSave;

    private BarcodeCallback mCallback;

    private FileHelper mFileHelper;


    public static BarcodeResultDialog newInstance(String upc) {
        Bundle args = new Bundle();
        args.putString(ARG_UPC, upc);
        BarcodeResultDialog fragment = new BarcodeResultDialog();
        fragment.setArguments(args);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        getDialog().getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        return inflater.inflate(R.layout.dialog_barcode, container);
    }

    public static interface OnCompleteListener {
        public abstract void onComplete();
    }

    private OnCompleteListener mListener;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        try {
            this.mListener = (OnCompleteListener) context;
        } catch (final ClassCastException e) {
            throw new ClassCastException(context.toString() + " must implement OnCompleteListener");
        }
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mFileHelper = new FileHelper(getContext());
    }

    @Override
    public void onStart() {
        super.onStart();
        getDialog().getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);

    }

    @Override
    public void onDismiss(DialogInterface dialog) {
        super.onDismiss(dialog);
        mListener.onComplete();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        mBarcodeValue = (TextView) view.findViewById(R.id.barcode_value);
        String barcodeValue = getArguments().getString(ARG_UPC);
        mBarcodeValue.setText(barcodeValue);

        mPrice = (EditText) view.findViewById(R.id.barcode_price);
        mPrice.setRawInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL);
        mPrice.requestFocus();
        InputMethodManager imm = (InputMethodManager) getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.toggleSoftInput(InputMethodManager.SHOW_FORCED, InputMethodManager.HIDE_IMPLICIT_ONLY);
        mLocation = (EditText) view.findViewById(R.id.barcode_location);
        mLocation.setText(SharedPrefHelper.getInstance(getContext()).getCurrentLocation());

        mSave = (Button) view.findViewById(R.id.barcode_save);
        mSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                InputMethodManager imm = (InputMethodManager) getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(mPrice.getWindowToken(), 0);
                imm.hideSoftInputFromWindow(mLocation.getWindowToken(), 0);

                int rc = ContextCompat.checkSelfPermission(getContext(), Manifest.permission.WRITE_EXTERNAL_STORAGE);
                if (rc == PackageManager.PERMISSION_GRANTED) {
                    try {
                        if (TextUtils.isEmpty(mLocation.getText().toString())) {
                            Toast.makeText(getContext(), "Please enter the location..", Toast.LENGTH_SHORT).show();
                            return;
                        }
                        if (TextUtils.isEmpty(mPrice.getText().toString())) {
                            Toast.makeText(getContext(), "Please enter the price..", Toast.LENGTH_SHORT).show();
                            return;
                        }
                        writeData();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                } else {
                    requestStoragePermission();
                }


            }
        });

    }

    private void requestStoragePermission() {
        Log.w(TAG, "Storage permission is not granted. Requesting permission");

        final String[] permissions = new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE};
        requestPermissions(permissions, RC_HANDLE_STORAGE_PERM);

    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {

        Log.d(TAG, "Request code is: " + requestCode);

        if (requestCode != RC_HANDLE_STORAGE_PERM) {
            Log.d(TAG, "Got unexpected permission result: " + requestCode);
            super.onRequestPermissionsResult(requestCode, permissions, grantResults);
            return;
        }

        if (grantResults.length != 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            Log.d(TAG, "Storage permission granted - initialize the storage");
            // we have permission, so create the storage
            try {
                writeData();
            } catch (IOException e) {
                e.printStackTrace();
            }
            getDialog().dismiss();
            return;
        }
        Log.e(TAG, "Permission not granted: results len = " + grantResults.length +
                " Result code = " + (grantResults.length > 0 ? grantResults[0] : "(empty)"));
    }

    private void writeData() throws IOException {
        if (mFileHelper.isExternalStorageWritable())
            mFileHelper.writeToFile(
                    mBarcodeValue.getText().toString(),
                    mPrice.getText().toString(),
                    mLocation.getText().toString());
        SharedPrefHelper.getInstance(getContext()).setCurrentLocation(mLocation.getText().toString());
        Toast.makeText(getContext(), "Item Saved", Toast.LENGTH_SHORT).show();
        getDialog().dismiss();
    }
}
