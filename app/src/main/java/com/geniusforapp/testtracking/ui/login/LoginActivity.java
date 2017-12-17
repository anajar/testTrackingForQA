package com.geniusforapp.testtracking.ui.login;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.StringRes;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Patterns;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.ScrollView;
import android.widget.TextView;

import com.geniusforapp.testtracking.BuildConfig;
import com.geniusforapp.testtracking.R;
import com.geniusforapp.testtracking.ui.main.MainActivity;
import com.geniusforapp.testtracking.utils.KeyboardUtils;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;


public class LoginActivity extends AppCompatActivity {

    private FirebaseAuth firebaseAuth;
    private FirebaseDatabase database;

    private Toolbar toolbar;

    private ProgressBar progressBar;
    private ScrollView loginLayout;

    private EditText email;
    private EditText password;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        // init firebase auth
        initAuth();
        // init views
        initViews();
        // init toolbar
        initActionBar();
        // check user
        checkUser();

        if (BuildConfig.DEBUG) {
            email.setText("ahmadnajar10@gmail.com");
            password.setText("123456");
        }

    }

    private void checkUser() {
        FirebaseUser currentUser = firebaseAuth.getCurrentUser();
        if (currentUser != null) {
            startActivity(new Intent(this, MainActivity.class)
                    .setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK));
            overridePendingTransition(0, 0);
        }
    }

    private void initAuth() {
        firebaseAuth = FirebaseAuth.getInstance();
        database = FirebaseDatabase.getInstance();
    }

    private void initViews() {
        toolbar = findViewById(R.id.toolbar);
        progressBar = findViewById(R.id.progress_bar);
        loginLayout = findViewById(R.id.login_layout);


        email = findViewById(R.id.email);
        password = findViewById(R.id.password);
    }

    private void initActionBar() {
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_close);
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                break;
        }
        return super.onOptionsItemSelected(item);
    }


    public void onLoginClicked(View view) {
        String textEmail = email.getText().toString().trim().toLowerCase();
        String textPassword = password.getText().toString().trim();

        if (textEmail.isEmpty()) {
            showSnackBar(R.string.error_field_required);
            return;
        }
        if (textPassword.isEmpty()) {
            showSnackBar(R.string.error_field_required);
            return;
        }

        if (!Patterns.EMAIL_ADDRESS.matcher(textEmail).matches()) {
            showSnackBar(R.string.error_invalid_email);
            return;
        }
        if (textPassword.length() < 6) {
            showSnackBar(R.string.error_invalid_password);
            return;
        }
        email.setError(null);
        password.setError(null);
        attemptLogin(textEmail, textPassword);
    }


    public void attemptLogin(final String email, final String password) {
        KeyboardUtils.hideKeyboard(this);
        showLoading();
        firebaseAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            startActivity(new Intent(LoginActivity.this, MainActivity.class)
                                    .setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK));
                            overridePendingTransition(0, 0);
                            addUserToFireBase();
                        } else {
                            if (task.getException() instanceof FirebaseAuthUserCollisionException) {
                                showSnackBar(R.string.prompt_login_user);
                                loginUser(email, password);
                                return;
                            }
                            showContent();
                            showSnackBar(task.getException().getLocalizedMessage());

                        }
                    }
                });
    }


    private void loginUser(String email, String password) {
        firebaseAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            startActivity(new Intent(LoginActivity.this, MainActivity.class)
                                    .setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK));
                            overridePendingTransition(0, 0);
                        } else {
                            showContent();
                            showSnackBar(task.getException().getLocalizedMessage());
                        }
                    }
                });

    }


    public void addUserToFireBase() {
        DatabaseReference reference = database.getReference().child("users").child(firebaseAuth.getUid());
        reference.setValue(firebaseAuth.getCurrentUser());
        reference.push();
    }


    private void showLoading() {
        progressBar.setVisibility(View.VISIBLE);
        loginLayout.setVisibility(View.GONE);
    }

    private void showContent() {
        progressBar.setVisibility(View.GONE);
        loginLayout.setVisibility(View.VISIBLE);
    }

    public void showSnackBar(@StringRes int message) {
        showSnackBar(getString(message));
    }

    public void showSnackBar(String message) {
        Snackbar snackbar = Snackbar.make(toolbar, message, Snackbar.LENGTH_LONG);
        Snackbar.SnackbarLayout layout = (Snackbar.SnackbarLayout) snackbar.getView();
        layout.setPadding(0, 0, 0, 0);
        TextView textView = layout.findViewById(android.support.design.R.id.snackbar_text);
        textView.setVisibility(View.INVISIBLE);
        View snackView = LayoutInflater.from(this).inflate(R.layout.layout_snack_bar, null);
        TextView messageTextView = snackView.findViewById(R.id.error_text);
        messageTextView.setText(message);
        layout.addView(snackView, 0);
        snackbar.show();
    }


}

