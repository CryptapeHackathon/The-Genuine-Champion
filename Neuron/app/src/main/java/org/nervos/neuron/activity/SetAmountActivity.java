package org.nervos.neuron.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.AppCompatButton;
import android.support.v7.widget.AppCompatEditText;
import android.support.v7.widget.AppCompatSpinner;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.View;
import android.widget.AdapterView;

import org.nervos.neuron.R;

public class SetAmountActivity extends BaseActivity {

    public static final String EXTRA_VALUE = "extra_value";
    public static final String EXTRA_CATEGORY = "extra_category";
    public static final int RESULT_CODE = 0x01;
    private AppCompatEditText amountEdit;
    private AppCompatButton okBtn;
    private AppCompatSpinner tokenSpinner;
    private String tokenCategory;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_set_amount);

        amountEdit = findViewById(R.id.edit_set_token_value);
        okBtn = findViewById(R.id.set_value_button);
        tokenSpinner = findViewById(R.id.spinner_token_category);

        initListener();
    }


    private void initListener() {
        tokenSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String[] categories = getResources().getStringArray(R.array.token_category);
                tokenCategory = categories[position];
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
        amountEdit.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                setOkButtonValid(!TextUtils.isEmpty(s));

            }
            @Override
            public void afterTextChanged(Editable s) {

            }
        });
        okBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                intent.putExtra(EXTRA_VALUE, amountEdit.getText().toString().trim());
                intent.putExtra(EXTRA_CATEGORY, tokenCategory);
                setResult(RESULT_CODE, intent);
                finish();
            }
        });
    }

    private void setOkButtonValid(boolean isValid) {
        if (isValid) {
            okBtn.setBackgroundResource(R.drawable.button_corner_blue_shape);
            okBtn.setTextColor(ContextCompat.getColor(mActivity, R.color.white));
        } else {
            okBtn.setBackgroundResource(R.drawable.button_corner_gray_shape);
            okBtn.setTextColor(ContextCompat.getColor(mActivity, R.color.default_gray_9));
        }
    }
}
