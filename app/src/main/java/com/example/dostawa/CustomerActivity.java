package com.example.dostawa;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.icu.text.IDNA;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import org.w3c.dom.Text;

public class CustomerActivity extends AppCompatActivity {
    EditText cEmail, cPassword;
    Button cLogin, cRegistration, cRecoverP;
    FirebaseAuth cAuth;
    private int i = 5; //count the number of try to login

    private ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_customer);

        cAuth = FirebaseAuth.getInstance();
        progressDialog = new ProgressDialog(this);
        //проверка залогинен ли пользователь
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

        cEmail = (EditText) findViewById(R.id.cemail);
        cPassword = (EditText) findViewById(R.id.cpassword);
        cLogin = (Button) findViewById(R.id.clogin);
        cRegistration = (Button) findViewById(R.id.cregistration);
        cRecoverP = (Button) findViewById(R.id.recoverpasscustomer);

        cRegistration.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view){
                String email = cEmail.getText().toString();
                String password = cPassword.getText().toString();
                if (TextUtils.isEmpty(email) || TextUtils.isEmpty(password)){
                    Toast.makeText(CustomerActivity.this, "Puste pola!", Toast.LENGTH_SHORT).show();
                } else {
                    //даем пользователю знать что происходит
                    progressDialog.setTitle("Registracja!");
                    progressDialog.setMessage("Poczekaj dopóki stworzymy twój profil");
                    progressDialog.setCanceledOnTouchOutside(false);
                    progressDialog.show();

                    cAuth.createUserWithEmailAndPassword(cEmail.getText().toString(),
                            cPassword.getText().toString())
                            .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                                @Override
                                public void onComplete(@NonNull Task<AuthResult> task) {
                                    if (task.isSuccessful()) {
                                        cAuth.getCurrentUser().sendEmailVerification().addOnCompleteListener(new OnCompleteListener<Void>() {
                                            @Override
                                            public void onComplete(@NonNull Task<Void> task) {
                                                if (task.isSuccessful()) {
                                                    progressDialog.dismiss();
                                                    Toast.makeText(CustomerActivity.this, "Registration is successfully. Please check your email for verification", Toast.LENGTH_SHORT).show();
                                                    if (cAuth.getCurrentUser().isEmailVerified()) {

                                                        //перевод на другую активность, если логин успешен, делаем так чтобы нельзя было вернуться на старую страницу(логина)
                                                        Intent intent = new Intent(CustomerActivity.this, SecondCActivity.class);
                                                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_TASK_ON_HOME);
                                                        startActivity(intent);
//                                                        finish();
                                                    }
                                                } else {
                                                    Toast.makeText(CustomerActivity.this, task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                                                }
                                            }
                                        });
                                    } else {
                                        Toast.makeText(CustomerActivity.this, task.getException().getMessage(), Toast.LENGTH_SHORT).show();

                                    }
                                }
                            });
                }
            }
        });

        //если пользователь залогинен, то сразу перенаправляем на следующую активность
        if (user != null){
            finish();
            startActivity(new Intent(CustomerActivity.this, SecondCActivity.class));
        }

        cLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String email = cEmail.getText().toString();
                String password = cPassword.getText().toString();
                if (TextUtils.isEmpty(email) || TextUtils.isEmpty(password)){
                    Toast.makeText(CustomerActivity.this, "Puste pola!", Toast.LENGTH_SHORT).show();
                } else {
                    //даем пользователю знать что происходит
                    progressDialog.setTitle("Logowanie!");
                    progressDialog.setMessage("Sprawdzamy dane, poczekaj chwilkę");
                    progressDialog.setCanceledOnTouchOutside(false);
                    progressDialog.show();

                    cAuth.signInWithEmailAndPassword(cEmail.getText().toString(),
                            cPassword.getText().toString())
                            .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                                @Override
                                public void onComplete(@NonNull Task<AuthResult> task) {
                                    if (task.isSuccessful()) {
                                        progressDialog.dismiss();
                                        Toast.makeText(CustomerActivity.this, "Logowanie powiodło się", Toast.LENGTH_SHORT).show();

                                        //перевод на другую активность, если логин успешен, делаем так чтобы нельзя было вернуться на старую страницу(логина)
                                        Intent intent = new Intent(CustomerActivity.this, SecondCActivity.class);
                                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_TASK_ON_HOME);
                                        startActivity(intent);
//                                        finish();

                                    } else if (!task.isSuccessful()) {
                                        progressDialog.dismiss();
                                        Toast.makeText(CustomerActivity.this, "Logowanie się nie powiodło", Toast.LENGTH_SHORT).show();
                                        i--;
                                        if (i == 0) {
                                            cLogin.setEnabled(false);
                                            Toast.makeText(CustomerActivity.this, "Logowanie niedotępne", Toast.LENGTH_SHORT).show();
                                        }
                                        if (!cLogin.isEnabled()){
                                            Toast.makeText(CustomerActivity.this, "Sprobuje jeszcze po 15 minutach", Toast.LENGTH_SHORT).show();
                                            new CountDownTimer(6000, 1000){

                                                @Override
                                                public void onTick(long millisUntilFinished) {}

                                                @Override
                                                public void onFinish() {
                                                    Toast.makeText(CustomerActivity.this, "Morzesz sprobować zalogować się jeszcze raz", Toast.LENGTH_SHORT).show();
                                                    cLogin.setEnabled(true);
                                                    i = 5;
                                                }
                                            }.start();
                                        }
                                    } else {
                                        Toast.makeText(CustomerActivity.this, task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                                    }
                                }
                            });
                }
            }
        });

        cRecoverP.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(CustomerActivity.this, SecondCActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_TASK_ON_HOME);
                startActivity(intent);
            }
        });
    }
}

