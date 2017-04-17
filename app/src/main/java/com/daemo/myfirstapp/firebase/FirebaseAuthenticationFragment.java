package com.daemo.myfirstapp.firebase;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.content.LocalBroadcastManager;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;

import com.daemo.myfirstapp.MainActivity;
import com.daemo.myfirstapp.R;
import com.daemo.myfirstapp.common.Constants;
import com.daemo.myfirstapp.common.Utils;
import com.daemo.myfirstapp.firebase.database.models.User;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class FirebaseAuthenticationFragment extends MySuperFirebaseFragment implements View.OnClickListener {

    private FirebaseAuth mAuth;
    private DatabaseReference mDatabase;

    private EditText mEmailField;
    private EditText mPasswordField;


    private FirebaseAuth.AuthStateListener mAuthListener = new FirebaseAuth.AuthStateListener() {
        @Override
        public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
            FirebaseUser user = firebaseAuth.getCurrentUser();
            if (user != null)
                Log.d(Utils.getTag(this), "onAuthStateChanged:signed_in: " + user.getUid());
            else Log.d(Utils.getTag(this), "onAuthStateChanged:signed_out");
        }
    };

    private OnCompleteListener<AuthResult> mAuthCompleteListener = new OnCompleteListener<AuthResult>() {
        @Override
        public void onComplete(@NonNull Task<AuthResult> task) {
            Log.d(Utils.getTag(this), "signInWithEmail:onComplete:" + task.isSuccessful());

            // If sign in fails, display a message to the user.
            // If sign in succeeds the auth state listener will be notified and logic to handle the signed in user can be handled in the listener.
            if (!task.isSuccessful()) {
                Log.w(Utils.getTag(this), "signInWithEmail:failed", task.getException());
                return;
            }

            getMySuperActivity().showToast("Authentication succeeded!");


            FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
            if (user != null) {
                // Name, email address, and profile photo Url
                String name = user.getDisplayName();
                String email = user.getEmail();
                Uri photoUrl = user.getPhotoUrl();

                // The user's ID, unique to the Firebase project.
                // Do NOT use this value to authenticate with your backend server, if you have one.
                // Use FirebaseUser.getToken() instead.
                String uid = user.getUid();
            }
            getMySuperActivity().hideProgressDialog();
            Intent i = new Intent(Constants.ACTION_FIREBASE_LOGIN_LOGOUT, null, getContext(), MainActivity.class);
            LocalBroadcastManager.getInstance(getContext()).sendBroadcast(i);
        }
    };
    private OnFailureListener mAuthFailListener = new OnFailureListener() {
        @Override
        public void onFailure(@NonNull Exception e) {
            getMySuperActivity().showToast("Authentication failed: " + e.getMessage());
            getMySuperActivity().hideProgressDialog();
        }
    };

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mDatabase = FirebaseDatabase.getInstance().getReference();
        mAuth = FirebaseAuth.getInstance();
        mAuth.addAuthStateListener(mAuthListener);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_firebase_authentication, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Views
        mEmailField = (EditText) view.findViewById(R.id.field_email);
        mPasswordField = (EditText) view.findViewById(R.id.field_password);
        Button mSignInButton = (Button) view.findViewById(R.id.button_sign_in);
        Button mSignUpButton = (Button) view.findViewById(R.id.button_sign_up);
        Button mSignInAnonymousButton = (Button) view.findViewById(R.id.button_sign_in_anonymous);

        // Click listeners
        mSignInButton.setOnClickListener(this);
        mSignUpButton.setOnClickListener(this);
        mSignInAnonymousButton.setOnClickListener(this);
    }

    @Override
    public void onStart() {
        super.onStart();

        // Check auth on Activity start
        if (mAuth.getCurrentUser() != null && !mAuth.getCurrentUser().isAnonymous())
            onAuthSuccess(mAuth.getCurrentUser());
    }

    private void signIn() {
        Log.d(Utils.getTag(this), "signIn");
        if (!validateForm()) return;

        getMySuperActivity().showProgressDialog();
        String email = mEmailField.getText().toString();
        String password = mPasswordField.getText().toString();

        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(getActivity(), mAuthCompleteListener)
                .addOnFailureListener(getActivity(), mAuthFailListener);
    }

    private void signUp() {
        Log.d(Utils.getTag(this), "signUp");
        if (!validateForm()) return;

        getMySuperActivity().showProgressDialog();
        String email = mEmailField.getText().toString();
        String password = mPasswordField.getText().toString();

        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(getActivity(), mAuthCompleteListener)
                .addOnFailureListener(getActivity(), mAuthFailListener);
    }

    private void onAuthSuccess(FirebaseUser user) {
        String username = usernameFromEmail(user.getEmail());

        // Write new user
        writeNewUser(user.getUid(), username, user.getEmail());
    }

    private String usernameFromEmail(String email) {
        if (email.contains("@")) {
            return email.split("@")[0];
        } else {
            return email;
        }
    }

    private boolean validateForm() {
        boolean result = true;
        if (TextUtils.isEmpty(mEmailField.getText().toString())) {
            mEmailField.setError("Required");
            result = false;
        } else {
            mEmailField.setError(null);
        }

        if (TextUtils.isEmpty(mPasswordField.getText().toString())) {
            mPasswordField.setError("Required");
            result = false;
        } else {
            mPasswordField.setError(null);
        }

        return result;
    }

    // [START basic_write]
    private void writeNewUser(String userId, String name, String email) {
        User user = new User(name);

        mDatabase.child("users").child(userId).setValue(user);
    }
    // [END basic_write]

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.button_sign_in:
                signIn();
                break;
            case R.id.button_sign_up:
                signUp();
                break;
            case R.id.button_sign_in_anonymous:
                signInAnonymous();
                break;
        }
    }

    private void signInAnonymous() {
        mAuth.signInAnonymously()
                .addOnCompleteListener(getActivity(), mAuthCompleteListener)
                .addOnFailureListener(getActivity(), mAuthFailListener);
    }
}