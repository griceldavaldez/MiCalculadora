package com.android.micalculadora;

import android.os.Bundle;
import com.google.android.material.snackbar.Snackbar;
import androidx.appcompat.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.TypedValue;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RelativeLayout;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class MainActivity extends AppCompatActivity {
    @BindView(R.id.etInput)
    EditText etInput;
    @BindView(R.id.contentMain)
    RelativeLayout contentMain;
    @BindView(R.id.btnClear)
    Button btnClear;
    @BindView(R.id.btnResult)
    Button btnResult;

    private boolean isEditInProgress = false;
    private int minLength;
    private int textSize;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);

        btnClear.findViewById(R.id.btnClear);
        btnResult.findViewById(R.id.btnResult);

        minLength = getResources().getInteger(R.integer.main_min_length);
        textSize = getResources().getInteger(R.integer.main_input_textSize);
        configEditText();
    }



    private void configEditText() {
        etInput.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                if (motionEvent.getAction() == MotionEvent.ACTION_UP) {
                    if (motionEvent.getRawX() >= (etInput.getRight() -
                            etInput.getCompoundDrawables()[Constantes.DRAWABLE_RIGHT].getBounds().width())) {
                        if (etInput.length() > 0) {
                            final int length = etInput.getText().length();
                            etInput.getText().delete(length - 1, length);
                        }
                    }
                    return true;
                }
                return false;
            }
        });

        etInput.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                if (!isEditInProgress &&
                        Metodos.canReplaceOperator(charSequence)) {
                    isEditInProgress = true;
                    etInput.getText().delete(etInput.getText().length() - 2, etInput.getText().length() - 1);
                }

                if (charSequence.length() > minLength) {
                    etInput.setTextSize(TypedValue.COMPLEX_UNIT_SP, textSize -
                            (((charSequence.length() - minLength) * 2) + (charSequence.length() - minLength)));
                } else {
                    etInput.setTextSize(TypedValue.COMPLEX_UNIT_SP, textSize);
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {
                if(etInput.getText().length() == 0){
                    etInput.setTextSize(22f);
                }
                isEditInProgress = false;
            }
        });

    }


    @OnClick({R.id.btnZero, R.id.btnOne, R.id.btnTwo, R.id.btnThree, R.id.btnFour, R.id.btnFive,
            R.id.btnSix, R.id.btnSeven, R.id.btnEight, R.id.btnNine, R.id.btnPoint })
    public void onClickNumbers(View view){
        final String valStr = ((Button) view).getText().toString();
        switch (view.getId()){
            case R.id.btnZero:
            case R.id.btnOne:
            case R.id.btnTwo:
            case R.id.btnThree:
            case R.id.btnFour:
            case R.id.btnFive:
            case R.id.btnSix:
            case R.id.btnSeven:
            case R.id.btnEight:
            case R.id.btnNine:
                etInput.getText().append(valStr);
                break;
            case R.id.btnPoint:
                final String operacion = etInput.getText().toString();
                final String operador = Metodos.getOperator(operacion);
                final int count = operacion.length() - operacion.replace(".", "").length();
                if(!operacion.contains(Constantes.POINT) || (count < 2 && (!operador.equals(Constantes.OPERATOR_NULL)))){
                    etInput.getText().append(valStr);
                }
                break;
        }
    }

    @OnClick({R.id.btnClear, R.id.btnDivision, R.id.btnMultiplication, R.id.btnSubtraction, R.id.btnAddition, R.id.btnResult})
    public void onClickControls(View view) {
        switch (view.getId()) {
            case R.id.btnClear:
                etInput.setText("");
                break;
            case R.id.btnDivision:
            case R.id.btnMultiplication:
            case R.id.btnSubtraction:
            case R.id.btnAddition:
                resolve(false);

                final String operador = ((Button) view).getText().toString();
                final String operacion = etInput.getText().toString();

                final String ultimoCaracter = operacion.isEmpty() ? "" :
                        operacion.substring(operacion.length() - 1);

                if (operador.equals(Constantes.OPERATOR_SUB)) {
                    if (operacion.isEmpty() ||
                            (!(ultimoCaracter.equals(Constantes.OPERATOR_SUB)) &&
                                    !(ultimoCaracter.equals(Constantes.POINT)))) {
                        etInput.getText().append(operador);
                    }
                } else {
                    if (!operacion.isEmpty() &&
                            !(ultimoCaracter.equals(Constantes.OPERATOR_SUB)) &&
                            !(ultimoCaracter.equals(Constantes.POINT))) {
                        etInput.getText().append(operador);
                    }
                }
                break;
            case R.id.btnResult:
                resolve(true);
                break;
        }
    }

    private void resolve(boolean fromResult) {
        Metodos.tryResolve(fromResult, etInput, new OnResolveCallback() {
            @Override
            public void onShowMessage(int errorRes) {
                showMessage(errorRes);
            }

            @Override
            public void onIsEditing() {
                isEditInProgress = true;
            }
        });
    }


    private void showMessage(int errorRes) {
        Snackbar.make(contentMain, errorRes, Snackbar.LENGTH_SHORT).show();
    }

}