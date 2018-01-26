package com.example.vincent.mybluetoothdevice.dialog;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.example.vincent.mybluetoothdevice.R;


/**
 * @author Administrator QQ:1032006226
 * @version v1.0
 * @name CustomDialog
 * @page com.vincent.dialog.simple
 * @class describe
 * @date 2018/1/25 9:47
 */

public class InputContentDialog extends BaseDialog {
    private static final String TAG = InputContentDialog.class.getSimpleName();
    private Context mContext;
    private EditText mEditText;
    private TextView mTextView;
    private InputContentListener inputContentListener;
    private boolean isCheckNull = false;
    private String hintMsg;

    public InputContentDialog setCheckNull(boolean checkNull,String hintMsg) {
        isCheckNull = checkNull;
        this.hintMsg = hintMsg;
        if(mEditText != null){
            mEditText.setHint(hintMsg);
        }
        return this;
    }

    public InputContentDialog setInputContentListener(InputContentListener inputContentListener) {
        this.inputContentListener = inputContentListener;
        return this;
    }

    public InputContentDialog(@NonNull Context context) {
        super(context, R.style.MyDialogBgIsTransparent);
        this.mContext = context;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.library_layout_input_content);
        setShowBottom(true);
        mEditText = findViewById(R.id.library_input_content_et);
        mTextView = findViewById(R.id.library_input_content_enter);
        mTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String content = mEditText.getText().toString();
                if(inputContentListener != null){
                    if(isCheckNull){
                        if(TextUtils.isEmpty(content)){
                            Toast.makeText(mContext,hintMsg, Toast.LENGTH_SHORT).show();
                        }else {
                            inputContentListener.input(content);
                            dismiss();
                        }
                    }else {
                        inputContentListener.input(content);
                        dismiss();
                    }
                }
            }
        });
    }

    @Override
    public void show() {
        super.show();
        if(mEditText != null){
            mEditText.post(new Runnable() {
                @Override
                public void run() {
                    Log.d(TAG, "run: ...");
                    InputMethodManager imm =
                            (InputMethodManager) mContext.getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.showSoftInput(mEditText, 0);
                }
            });
        }
    }

    public interface InputContentListener{
        void input(String content);
    }

}
